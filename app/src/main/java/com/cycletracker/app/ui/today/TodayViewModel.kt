package com.cycletracker.app.ui.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cycletracker.app.core.time.AppClock
import com.cycletracker.app.domain.insight.Insight
import com.cycletracker.app.domain.insight.InsightGenerator
import com.cycletracker.app.domain.model.CyclePhase
import com.cycletracker.app.domain.model.DailyLog
import com.cycletracker.app.domain.model.Period
import com.cycletracker.app.domain.prediction.FertileWindow
import com.cycletracker.app.domain.prediction.OvulationEstimate
import com.cycletracker.app.domain.prediction.PeriodPrediction
import com.cycletracker.app.domain.prediction.PredictionEngine
import com.cycletracker.app.domain.repository.CycleRepository
import com.cycletracker.app.domain.repository.DailyLogRepository
import com.cycletracker.app.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil
import kotlinx.datetime.minus
import java.util.UUID
import javax.inject.Inject
import kotlin.math.roundToInt

data class TodayUiState(
    val loading: Boolean = true,
    val hasData: Boolean = false,
    val todayEpochDay: Long = 0,
    val cycleDay: Int = 0,
    val phase: CyclePhase = CyclePhase.UNKNOWN,
    val ringProgress: Float = 0f,
    val nextPeriod: PeriodPrediction? = null,
    val ovulation: OvulationEstimate? = null,
    val fertileWindow: FertileWindow? = null,
    val daysUntilNextPeriod: Int? = null,
    val daysUntilOvulation: Int? = null,
    val delayDays: Int? = null,
    val onPeriod: Boolean = false,
    val insights: List<Insight> = emptyList(),
)

@HiltViewModel
class TodayViewModel @Inject constructor(
    private val cycleRepository: CycleRepository,
    dailyLogRepository: DailyLogRepository,
    private val engine: PredictionEngine,
    private val insightGenerator: InsightGenerator,
    private val clock: AppClock,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    private val today = clock.today()
    private var currentPeriods: List<Period> = emptyList()

    val uiState: StateFlow<TodayUiState> = combine(
        cycleRepository.observePeriods(),
        dailyLogRepository.observeLogsBetween(today.minus(DatePeriod(days = 60)), today),
        settingsRepository.observeSettings(),
    ) { periods, logs, settings -> buildState(periods, logs, settings.delaySinceEpochDay) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TodayUiState(loading = true))

    private fun buildState(periods: List<Period>, logs: List<DailyLog>, delaySince: Long): TodayUiState {
        currentPeriods = periods
        val todayEpoch = today.toEpochDays().toLong()
        if (periods.isEmpty()) {
            return TodayUiState(loading = false, hasData = false, todayEpochDay = todayEpoch)
        }
        val phase = engine.currentPhase(periods, today)
        val next = engine.predictNextPeriod(periods)
        val ovulation = engine.refineOvulation(periods, logs, today)
        val fertile = engine.refineFertileWindow(periods, logs, today)
        val cycleLength = engine.stats(periods).meanLength?.roundToInt() ?: 28
        return TodayUiState(
            loading = false,
            hasData = true,
            todayEpochDay = todayEpoch,
            cycleDay = phase.cycleDay,
            phase = phase.phase,
            ringProgress = (phase.cycleDay.toFloat() / cycleLength).coerceIn(0f, 1f),
            nextPeriod = next,
            ovulation = ovulation,
            fertileWindow = fertile,
            daysUntilNextPeriod = next?.let { today.daysUntil(it.nextStart) },
            daysUntilOvulation = ovulation?.let { today.daysUntil(it.day) },
            delayDays = computeDelay(delaySince, next),
            onPeriod = delaySince == 0L && phase.phase == CyclePhase.MENSTRUAL,
            insights = insightGenerator.insightsForToday(periods, today, dailyLogs = logs),
        )
    }

    private fun computeDelay(delaySince: Long, next: PeriodPrediction?): Int? {
        if (delaySince > 0L) return LocalDate.fromEpochDays(delaySince.toInt()).daysUntil(today).coerceAtLeast(0)
        return next?.let { val d = today.daysUntil(it.nextStart); if (d < 0) -d else null }
    }

    /** Mark that menstruation started today (creates a new period from today) and clears any delay. */
    fun startPeriodToday() {
        viewModelScope.launch {
            if (currentPeriods.none { it.start == today }) {
                cycleRepository.upsertPeriod(Period(id = UUID.randomUUID().toString(), start = today, end = null))
            }
            settingsRepository.updateSettings { it.copy(delaySinceEpochDay = 0L) }
        }
    }

    /** Mark that menstruation is still ongoing today (extends the current period's end to today). */
    fun continuePeriod() {
        val last = currentPeriods.maxByOrNull { it.start } ?: return
        viewModelScope.launch {
            if (last.end != today) cycleRepository.upsertPeriod(last.copy(end = today))
            settingsRepository.updateSettings { it.copy(delaySinceEpochDay = 0L) }
        }
    }

    /**
     * Cancel the most recent period. [asDelay] = it was a delay (register a delay anchor so the
     * forecast recalculates and the ring shows the delay); otherwise it was logged by mistake.
     */
    fun cancelPeriod(asDelay: Boolean) {
        val last = currentPeriods.maxByOrNull { it.start } ?: return
        val anchor = last.start.toEpochDays().toLong()
        viewModelScope.launch {
            cycleRepository.deletePeriod(last.id)
            settingsRepository.updateSettings { it.copy(delaySinceEpochDay = if (asDelay) anchor else 0L) }
        }
    }
}
