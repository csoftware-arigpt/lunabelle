package com.cycletracker.app.ui.insights

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cycletracker.app.core.time.AppClock
import com.cycletracker.app.domain.insight.Insight
import com.cycletracker.app.domain.insight.InsightGenerator
import com.cycletracker.app.domain.model.CyclePhase
import com.cycletracker.app.domain.prediction.PredictionEngine
import com.cycletracker.app.domain.repository.CycleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class InsightsUiState(
    val hasData: Boolean = false,
    val cycleDay: Int = 0,
    val phase: CyclePhase = CyclePhase.UNKNOWN,
    val insights: List<Insight> = emptyList(),
)

@HiltViewModel
class InsightsViewModel @Inject constructor(
    cycleRepository: CycleRepository,
    private val engine: PredictionEngine,
    private val insightGenerator: InsightGenerator,
    private val clock: AppClock,
) : ViewModel() {
    val uiState: StateFlow<InsightsUiState> = cycleRepository.observePeriods().map { periods ->
        if (periods.isEmpty()) {
            InsightsUiState(hasData = false)
        } else {
            val today = clock.today()
            val phase = engine.currentPhase(periods, today)
            InsightsUiState(
                hasData = true,
                cycleDay = phase.cycleDay,
                phase = phase.phase,
                insights = insightGenerator.insightsForToday(periods, today),
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), InsightsUiState())
}
