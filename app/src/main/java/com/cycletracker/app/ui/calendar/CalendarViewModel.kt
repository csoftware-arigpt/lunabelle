package com.cycletracker.app.ui.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cycletracker.app.core.time.AppClock
import com.cycletracker.app.domain.model.Period
import com.cycletracker.app.domain.model.SexualActivity
import com.cycletracker.app.domain.prediction.PredictionEngine
import com.cycletracker.app.domain.repository.CycleRepository
import com.cycletracker.app.domain.repository.DailyLogRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import java.util.UUID
import javax.inject.Inject

data class CalendarDay(
    val date: LocalDate,
    val epochDay: Long,
    val inPeriod: Boolean = false,
    val predictedPeriod: Boolean = false,
    val fertile: Boolean = false,
    val ovulation: Boolean = false,
    val hasLog: Boolean = false,
    val intimacy: Boolean = false,
    val isToday: Boolean = false,
)

data class CalendarUiState(
    val year: Int = 2000,
    val month: Int = 1,
    val leadingBlanks: Int = 0,
    val days: List<CalendarDay> = emptyList(),
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val cycleRepository: CycleRepository,
    dailyLogRepository: DailyLogRepository,
    private val engine: PredictionEngine,
    private val clock: AppClock,
) : ViewModel() {

    private val visible = MutableStateFlow(clock.today().let { it.year to it.monthNumber })

    val uiState: StateFlow<CalendarUiState> =
        combine(visible, cycleRepository.observePeriods()) { ym, periods -> ym to periods }
            .flatMapLatest { (ym, periods) ->
                val first = LocalDate(ym.first, ym.second, 1)
                val daysInMonth = first.plus(DatePeriod(months = 1)).minus(DatePeriod(days = 1)).dayOfMonth
                val last = LocalDate(ym.first, ym.second, daysInMonth)
                dailyLogRepository.observeLogsBetween(first, last).map { logs ->
                    val loggedDates = logs.map { it.date }.toSet()
                    val intimacyDates = logs.filter { it.sexualActivity != SexualActivity.NONE }.map { it.date }.toSet()
                    build(ym.first, ym.second, first, daysInMonth, periods, loggedDates, intimacyDates)
                }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CalendarUiState())

    private fun build(
        year: Int,
        month: Int,
        first: LocalDate,
        daysInMonth: Int,
        periods: List<Period>,
        loggedDates: Set<LocalDate>,
        intimacyDates: Set<LocalDate>,
    ): CalendarUiState {
        val today = clock.today()
        val periodDays = periods.flatMap { p ->
            val end = p.end ?: p.start.plus(DatePeriod(days = 4))
            generateRange(p.start, end)
        }.toSet()
        val nextPeriod = engine.predictNextPeriod(periods)
        val predicted = nextPeriod?.let {
            generateRange(it.nextStart, it.nextStart.plus(DatePeriod(days = (it.predictedPeriodLength - 1).coerceAtLeast(0))))
        }?.toSet().orEmpty()
        val fertile = engine.fertileWindow(periods)?.let { generateRange(it.start, it.end) }?.toSet().orEmpty()
        val ovulation = engine.estimateOvulation(periods)?.day

        val days = (1..daysInMonth).map { d ->
            val date = LocalDate(year, month, d)
            CalendarDay(
                date = date,
                epochDay = date.toEpochDays().toLong(),
                inPeriod = date in periodDays,
                predictedPeriod = date in predicted && date !in periodDays,
                fertile = date in fertile,
                ovulation = date == ovulation,
                hasLog = date in loggedDates,
                intimacy = date in intimacyDates,
                isToday = date == today,
            )
        }
        return CalendarUiState(year, month, (first.dayOfWeek.isoDayNumber - 1), days)
    }

    private fun generateRange(start: LocalDate, end: LocalDate): List<LocalDate> {
        if (end < start) return listOf(start)
        return (0..start.daysUntil(end)).map { start.plus(DatePeriod(days = it)) }
    }

    fun previousMonth() {
        visible.value = LocalDate(visible.value.first, visible.value.second, 1).minus(DatePeriod(months = 1)).let { it.year to it.monthNumber }
    }

    fun nextMonth() {
        visible.value = LocalDate(visible.value.first, visible.value.second, 1).plus(DatePeriod(months = 1)).let { it.year to it.monthNumber }
    }

    fun addPeriod(start: LocalDate, end: LocalDate) {
        viewModelScope.launch {
            cycleRepository.upsertPeriod(Period(id = UUID.randomUUID().toString(), start = start, end = end))
        }
    }
}
