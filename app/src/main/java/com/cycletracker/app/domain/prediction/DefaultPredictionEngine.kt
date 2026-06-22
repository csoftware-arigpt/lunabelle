package com.cycletracker.app.domain.prediction

import com.cycletracker.app.domain.model.CervicalMucus
import com.cycletracker.app.domain.model.Confidence
import com.cycletracker.app.domain.model.CyclePhase
import com.cycletracker.app.domain.model.DailyLog
import com.cycletracker.app.domain.model.LhTest
import com.cycletracker.app.domain.model.Period
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlin.math.roundToInt

class DefaultPredictionEngine : PredictionEngine {

    override fun cycleLengths(periods: List<Period>, config: PredictionConfig): List<Int> =
        cleanCycleLengths(periods.sortedBy { it.start }, config)

    override fun stats(periods: List<Period>, config: PredictionConfig): CycleStats {
        val sorted = periods.sortedBy { it.start }
        val cleaned = cleanCycleLengths(sorted, config)
        val sdWindow = cleaned.takeLast(config.sdWindowSize)
        val sd = if (sdWindow.size >= 2) sampleStdDev(sdWindow) else null
        val periodLengths = completedPeriodLengths(sorted)
        return CycleStats(
            meanLength = if (cleaned.isNotEmpty()) mean(cleaned.takeLast(config.windowSize)) else null,
            sdLength = sd,
            meanPeriodLength = if (periodLengths.isNotEmpty()) mean(periodLengths.takeLast(config.windowSize)) else null,
            minLength = cleaned.minOrNull(),
            maxLength = cleaned.maxOrNull(),
            completedCycleCount = cleaned.size,
            isIrregular = (sd ?: 0.0) >= config.irregularSdThresholdDays,
        )
    }

    override fun predictNextPeriod(periods: List<Period>, config: PredictionConfig): PeriodPrediction? {
        val sorted = periods.sortedBy { it.start }
        if (sorted.isEmpty()) return null
        val lastStart = sorted.last().start
        val cleaned = cleanCycleLengths(sorted, config)

        val predictedLength: Int
        val confidence: Confidence
        val basedOn: Int
        if (cleaned.isEmpty()) {
            predictedLength = config.defaultCycleLength
            confidence = Confidence.NONE
            basedOn = 0
        } else {
            val window = cleaned.takeLast(config.windowSize)
            val recencyMean = weightedMean(window, config.recencyDecay)
            val n = window.size
            // Bayesian shrinkage toward the population prior (stabilises few-cycle predictions).
            val posterior = (config.priorStrength * config.priorCycleLength + n * recencyMean) /
                (config.priorStrength + n)
            predictedLength = posterior.roundToInt()
            basedOn = n
            val sd = sampleStdDev(cleaned.takeLast(config.sdWindowSize))
            confidence = when {
                n < config.minCyclesForPrediction -> Confidence.LOW
                sd >= config.irregularSdThresholdDays -> Confidence.LOW
                sd < config.tightSdThresholdDays -> Confidence.HIGH
                else -> Confidence.MEDIUM
            }
        }

        val nextStart = lastStart.plus(predictedLength, DateTimeUnit.DAY)
        val sdForBand = if (cleaned.size >= 2) sampleStdDev(cleaned.takeLast(config.sdWindowSize)) else 0.0
        val k = if (confidence == Confidence.LOW) 1.5 else 1.0
        val minBand = if (confidence == Confidence.HIGH) 1 else 2
        val band = (sdForBand * k).roundToInt().coerceAtLeast(minBand)

        val periodLengths = completedPeriodLengths(sorted)
        val predictedPeriodLength =
            if (periodLengths.isNotEmpty()) mean(periodLengths.takeLast(config.windowSize)).roundToInt()
            else config.defaultPeriodLength

        return PeriodPrediction(
            nextStart = nextStart,
            rangeStart = nextStart.minus(band, DateTimeUnit.DAY),
            rangeEnd = nextStart.plus(band, DateTimeUnit.DAY),
            predictedPeriodLength = predictedPeriodLength,
            confidence = confidence,
            basedOnCycles = basedOn,
        )
    }

    override fun estimateOvulation(periods: List<Period>, config: PredictionConfig): OvulationEstimate? {
        val pred = predictNextPeriod(periods, config) ?: return null
        val day = pred.nextStart.minus(config.lutealOffsetDays, DateTimeUnit.DAY)
        return OvulationEstimate(day, minOf(pred.confidence, Confidence.MEDIUM))
    }

    override fun fertileWindow(periods: List<Period>, config: PredictionConfig): FertileWindow? {
        val ov = estimateOvulation(periods, config) ?: return null
        return FertileWindow(ov.day.minus(config.fertileWindowPreDays, DateTimeUnit.DAY), ov.day)
    }

    override fun refineOvulation(
        periods: List<Period>,
        dailyLogs: List<DailyLog>,
        today: LocalDate,
        config: PredictionConfig,
    ): OvulationEstimate? {
        val sorted = periods.sortedBy { it.start }
        val calendar = estimateOvulation(sorted, config)
        if (sorted.isEmpty()) return calendar
        val cycleStart = sorted.last().start
        val logs = dailyLogs.filter { it.date >= cycleStart && it.date <= today }.sortedBy { it.date }

        // 1) Positive LH test: ovulation ~1 day after the surge (prospective, most specific).
        logs.lastOrNull { it.lhTest == LhTest.POSITIVE }?.let {
            return OvulationEstimate(it.date.plus(1, DateTimeUnit.DAY), Confidence.HIGH)
        }
        // 2) Sustained BBT rise: ovulation ~1 day before the rise (retrospective).
        detectBbtShift(logs, config)?.let { return OvulationEstimate(it, Confidence.MEDIUM) }
        // 3) Egg-white cervical mucus peak ~ ovulation day.
        logs.lastOrNull { it.cervicalMucus == CervicalMucus.EGG_WHITE }?.let {
            return OvulationEstimate(it.date, Confidence.MEDIUM)
        }
        return calendar
    }

    override fun refineFertileWindow(
        periods: List<Period>,
        dailyLogs: List<DailyLog>,
        today: LocalDate,
        config: PredictionConfig,
    ): FertileWindow? {
        val ov = refineOvulation(periods, dailyLogs, today, config) ?: return null
        return FertileWindow(ov.day.minus(config.fertileWindowPreDays, DateTimeUnit.DAY), ov.day)
    }

    override fun currentPhase(periods: List<Period>, today: LocalDate, config: PredictionConfig): PhaseState {
        val sorted = periods.filter { it.start <= today }.sortedBy { it.start }
        if (sorted.isEmpty()) return PhaseState(CyclePhase.UNKNOWN, 0)
        val last = sorted.last()
        val cycleDay = last.start.daysUntil(today) + 1
        if (cycleDay <= 0) return PhaseState(CyclePhase.UNKNOWN, cycleDay)

        val pred = predictNextPeriod(sorted, config)
        if (pred != null && today > pred.rangeEnd) return PhaseState(CyclePhase.UNKNOWN, cycleDay)

        val predictedLength = pred?.let { last.start.daysUntil(it.nextStart) } ?: config.defaultCycleLength
        val ovCycleDay = predictedLength - config.lutealOffsetDays + 1
        val periodEndDay = last.end?.let { last.start.daysUntil(it) + 1 }
            ?: (pred?.predictedPeriodLength ?: config.defaultPeriodLength)

        val phase = when {
            cycleDay <= periodEndDay -> CyclePhase.MENSTRUAL
            cycleDay in (ovCycleDay - 1)..(ovCycleDay + 1) -> CyclePhase.OVULATORY
            cycleDay < ovCycleDay -> CyclePhase.FOLLICULAR
            else -> CyclePhase.LUTEAL
        }
        return PhaseState(phase, cycleDay)
    }

    override fun nextPhaseStart(
        phase: CyclePhase,
        periods: List<Period>,
        today: LocalDate,
        config: PredictionConfig,
    ): LocalDate? {
        val sorted = periods.sortedBy { it.start }
        if (sorted.isEmpty()) return null
        val pred = predictNextPeriod(sorted, config) ?: return null
        val lastStart = sorted.last().start
        val length = lastStart.daysUntil(pred.nextStart).coerceAtLeast(1)
        val ovulation = pred.nextStart.minus(config.lutealOffsetDays, DateTimeUnit.DAY)
        val candidate = when (phase) {
            CyclePhase.MENSTRUAL -> pred.nextStart
            CyclePhase.FOLLICULAR -> lastStart.plus(pred.predictedPeriodLength, DateTimeUnit.DAY)
            CyclePhase.OVULATORY -> ovulation
            CyclePhase.LUTEAL -> ovulation.plus(1, DateTimeUnit.DAY)
            CyclePhase.UNKNOWN -> return null
        }
        return if (candidate < today) candidate.plus(length, DateTimeUnit.DAY) else candidate
    }

    override fun rhythmFertileBand(periods: List<Period>, config: PredictionConfig): IntRange? {
        val cleaned = cleanCycleLengths(periods.sortedBy { it.start }, config)
        if (cleaned.size < 6) return null
        val window = cleaned.takeLast(config.sdWindowSize)
        return (window.min() - 18)..(window.max() - 11)
    }

    // --- internals ---

    private fun rawCycleLengths(sorted: List<Period>): List<Int> =
        sorted.zipWithNext { a, b -> a.start.daysUntil(b.start) }

    private fun completedPeriodLengths(sorted: List<Period>): List<Int> =
        sorted.mapNotNull { p -> p.end?.let { e -> p.start.daysUntil(e) + 1 } }.filter { it in 1..14 }

    /**
     * Cleaning pipeline: drop non-positive lengths and gaps (> max); skip-impute likely-missed
     * "multi-cycles" by splitting them into equal parts; then winsorize to mean ± k·SD.
     */
    private fun cleanCycleLengths(sorted: List<Period>, config: PredictionConfig): List<Int> {
        val raw = rawCycleLengths(sorted).filter { it in 1..config.outlierMaxCycleDays }
        if (raw.size < 2) return raw
        val center = median(raw)
        val imputed = raw.flatMap { length ->
            if (center > 0 && length >= center * config.skippedLogMultiplier) {
                val parts = (length / center).roundToInt().coerceAtLeast(2)
                val each = (length.toDouble() / parts).roundToInt()
                List(parts) { each }
            } else {
                listOf(length)
            }
        }
        if (imputed.size < 2) return imputed
        val m = mean(imputed)
        val sd = sampleStdDev(imputed)
        if (sd == 0.0) return imputed
        val lo = m - config.outlierSdMultiplier * sd
        val hi = m + config.outlierSdMultiplier * sd
        return imputed.map { it.toDouble().coerceIn(lo, hi).roundToInt() }
    }

    /** Detect a sustained BBT rise; returns the estimated ovulation day (~1 day before the rise). */
    private fun detectBbtShift(logs: List<DailyLog>, config: PredictionConfig): LocalDate? {
        val temps = logs.filter { it.bbtCelsius != null }
        if (temps.size < 9) return null
        for (i in 6 until temps.size) {
            val baseline = temps.subList(i - 6, i).mapNotNull { it.bbtCelsius }.average()
            val sustained = (i until minOf(i + 3, temps.size)).all {
                (temps[it].bbtCelsius ?: 0.0) >= baseline + config.bbtShiftThresholdC
            }
            if (sustained) return temps[i].date.minus(1, DateTimeUnit.DAY)
        }
        return null
    }
}
