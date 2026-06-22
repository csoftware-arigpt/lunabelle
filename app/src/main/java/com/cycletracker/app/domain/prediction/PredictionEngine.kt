package com.cycletracker.app.domain.prediction

import com.cycletracker.app.domain.model.CyclePhase
import com.cycletracker.app.domain.model.DailyLog
import com.cycletracker.app.domain.model.Period
import kotlinx.datetime.LocalDate

/**
 * Pure-Kotlin menstrual-cycle prediction. Stateless and deterministic. Uses recency-weighted,
 * skip-aware, Bayesian-shrunk cycle averaging, plus symptothermal refinement from daily logs.
 */
interface PredictionEngine {
    fun cycleLengths(periods: List<Period>, config: PredictionConfig = PredictionConfig()): List<Int>
    fun stats(periods: List<Period>, config: PredictionConfig = PredictionConfig()): CycleStats
    fun predictNextPeriod(periods: List<Period>, config: PredictionConfig = PredictionConfig()): PeriodPrediction?

    /** Calendar ovulation estimate (next period − luteal offset). */
    fun estimateOvulation(periods: List<Period>, config: PredictionConfig = PredictionConfig()): OvulationEstimate?
    fun fertileWindow(periods: List<Period>, config: PredictionConfig = PredictionConfig()): FertileWindow?

    /** Ovulation refined with symptothermal signals (LH+, BBT shift, egg-white mucus) when available. */
    fun refineOvulation(periods: List<Period>, dailyLogs: List<DailyLog>, today: LocalDate, config: PredictionConfig = PredictionConfig()): OvulationEstimate?
    fun refineFertileWindow(periods: List<Period>, dailyLogs: List<DailyLog>, today: LocalDate, config: PredictionConfig = PredictionConfig()): FertileWindow?

    fun currentPhase(periods: List<Period>, today: LocalDate, config: PredictionConfig = PredictionConfig()): PhaseState

    /** Next future start date of the given phase (used to schedule per-phase reminders). */
    fun nextPhaseStart(phase: CyclePhase, periods: List<Period>, today: LocalDate, config: PredictionConfig = PredictionConfig()): LocalDate?

    fun rhythmFertileBand(periods: List<Period>, config: PredictionConfig = PredictionConfig()): IntRange?
}
