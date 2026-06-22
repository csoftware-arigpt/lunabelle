package com.cycletracker.app.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cycletracker.app.core.time.AppClock
import com.cycletracker.app.domain.prediction.CycleStats
import com.cycletracker.app.domain.prediction.PredictionEngine
import com.cycletracker.app.domain.repository.CycleRepository
import com.cycletracker.app.domain.repository.DailyLogRepository
import com.cycletracker.app.domain.stats.StatisticsCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.minus
import javax.inject.Inject

data class HistoryUiState(
    val hasData: Boolean = false,
    val stats: CycleStats? = null,
    val cycleLengths: List<Int> = emptyList(),
    val symptomFrequency: List<Pair<String, Int>> = emptyList(),
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HistoryViewModel @Inject constructor(
    cycleRepository: CycleRepository,
    dailyLogRepository: DailyLogRepository,
    private val engine: PredictionEngine,
    private val statistics: StatisticsCalculator,
    clock: AppClock,
) : ViewModel() {

    private val today = clock.today()

    val uiState: StateFlow<HistoryUiState> = combine(
        cycleRepository.observePeriods(),
        dailyLogRepository.observeLogsBetween(today.minus(DatePeriod(years = 1)), today),
    ) { periods, logs ->
        if (periods.size < 2) {
            HistoryUiState(hasData = false)
        } else {
            HistoryUiState(
                hasData = true,
                stats = engine.stats(periods),
                cycleLengths = engine.cycleLengths(periods),
                symptomFrequency = statistics.symptomFrequency(logs).take(8),
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HistoryUiState())
}
