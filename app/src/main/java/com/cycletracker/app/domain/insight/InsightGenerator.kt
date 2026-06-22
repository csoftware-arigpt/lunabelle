package com.cycletracker.app.domain.insight

import com.cycletracker.app.domain.model.Confidence
import com.cycletracker.app.domain.model.CyclePhase
import com.cycletracker.app.domain.model.DailyLog
import com.cycletracker.app.domain.model.Period
import com.cycletracker.app.domain.prediction.PredictionConfig
import com.cycletracker.app.domain.prediction.PredictionEngine
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil

/** Derives the day's insights from cycle history (+ optional daily logs for symptothermal refinement). */
class InsightGenerator(private val engine: PredictionEngine) {

    fun insightsForToday(
        periods: List<Period>,
        today: LocalDate,
        config: PredictionConfig = PredictionConfig(),
        dailyLogs: List<DailyLog> = emptyList(),
    ): List<Insight> {
        val result = mutableListOf<Insight>()
        val stats = engine.stats(periods, config)

        if (stats.completedCycleCount < config.minCyclesForPrediction) {
            result += Insight(InsightType.NEEDS_MORE_DATA, InsightSeverity.INFO)
        }
        if (stats.isIrregular) {
            result += Insight(InsightType.IRREGULAR_CYCLES, InsightSeverity.ATTENTION)
        }

        val pred = engine.predictNextPeriod(periods, config)
        if (pred != null && pred.confidence == Confidence.LOW &&
            stats.completedCycleCount >= config.minCyclesForPrediction
        ) {
            result += Insight(InsightType.LOW_CONFIDENCE, InsightSeverity.ATTENTION)
        }

        val ovulation = engine.refineOvulation(periods, dailyLogs, today, config)
        val fertile = engine.refineFertileWindow(periods, dailyLogs, today, config)
        if (ovulation != null && today == ovulation.day) {
            result += Insight(InsightType.OVULATION_TODAY, InsightSeverity.INFO)
        }
        if (fertile != null && today >= fertile.start && today <= fertile.end) {
            result += Insight(InsightType.FERTILE_WINDOW, InsightSeverity.INFO)
        } else if (ovulation != null && today.daysUntil(ovulation.day) in 1..2) {
            result += Insight(InsightType.OVULATION_SOON, InsightSeverity.INFO)
        }

        if (pred != null) {
            when {
                today > pred.rangeEnd -> result += Insight(InsightType.PERIOD_LATE, InsightSeverity.ATTENTION)
                today >= pred.rangeStart && today <= pred.nextStart -> result += Insight(InsightType.PERIOD_DUE, InsightSeverity.INFO)
                else -> {
                    val phase = engine.currentPhase(periods, today, config)
                    if (phase.phase == CyclePhase.LUTEAL && today.daysUntil(pred.nextStart) in 0..5) {
                        result += Insight(InsightType.PMS_LIKELY, InsightSeverity.INFO)
                    }
                }
            }
        }

        val mean = stats.meanLength
        if (mean != null && stats.completedCycleCount >= config.minCyclesForPrediction &&
            (mean < 21 || mean > 35)
        ) {
            result += Insight(InsightType.OUT_OF_RANGE_CYCLE, InsightSeverity.WARNING)
        }
        return result
    }
}
