package com.cycletracker.app.domain.prediction

import com.cycletracker.app.domain.model.Confidence
import com.cycletracker.app.domain.model.CyclePhase
import kotlinx.datetime.LocalDate

/** Predicted next period with an uncertainty band. */
data class PeriodPrediction(
    val nextStart: LocalDate,
    val rangeStart: LocalDate,
    val rangeEnd: LocalDate,
    val predictedPeriodLength: Int,
    val confidence: Confidence,
    val basedOnCycles: Int,
)

/** Estimated ovulation day. Confidence is capped at MEDIUM (luteal phase varies). */
data class OvulationEstimate(val day: LocalDate, val confidence: Confidence)

/** Fertile window: 6 days ending on (and including) ovulation day. */
data class FertileWindow(val start: LocalDate, val end: LocalDate)

/** Current cycle phase and 1-based cycle day. */
data class PhaseState(val phase: CyclePhase, val cycleDay: Int)

/** Aggregate statistics over the user's history. */
data class CycleStats(
    val meanLength: Double?,
    val sdLength: Double?,
    val meanPeriodLength: Double?,
    val minLength: Int?,
    val maxLength: Int?,
    val completedCycleCount: Int,
    val isIrregular: Boolean,
)
