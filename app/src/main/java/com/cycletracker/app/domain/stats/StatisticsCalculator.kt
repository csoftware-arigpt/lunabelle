package com.cycletracker.app.domain.stats

import com.cycletracker.app.domain.model.DailyLog
import com.cycletracker.app.domain.model.FlowLevel
import com.cycletracker.app.domain.model.Period
import com.cycletracker.app.domain.prediction.PredictionConfig
import com.cycletracker.app.domain.prediction.PredictionEngine
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil

/** One completed cycle as a chart point. */
data class CycleLengthPoint(val start: LocalDate, val lengthDays: Int)

/** Pure-Kotlin derivations for the History/Insights charts. */
class StatisticsCalculator(private val engine: PredictionEngine) {

    /** Per-cycle length series (day-1 to day-1) for consecutive periods, unfiltered. */
    fun cycleLengthHistory(periods: List<Period>): List<CycleLengthPoint> {
        val sorted = periods.sortedBy { it.start }
        return sorted.zipWithNext { a, b -> CycleLengthPoint(a.start, a.start.daysUntil(b.start)) }
    }

    /** Period (bleeding) length series for completed periods. */
    fun periodLengthHistory(periods: List<Period>): List<CycleLengthPoint> =
        periods.sortedBy { it.start }
            .mapNotNull { p -> p.end?.let { CycleLengthPoint(p.start, p.start.daysUntil(it) + 1) } }

    /** Count of each symptom code across the given logs (descending). */
    fun symptomFrequency(logs: List<DailyLog>): List<Pair<String, Int>> =
        logs.flatMap { it.symptoms }
            .groupingBy { it.symptomCode }
            .eachCount()
            .entries.sortedByDescending { it.value }
            .map { it.key to it.value }

    /** Distribution of flow intensities across logged days (excludes NONE). */
    fun flowDistribution(logs: List<DailyLog>): Map<FlowLevel, Int> =
        logs.filter { it.flow != FlowLevel.NONE }
            .groupingBy { it.flow }
            .eachCount()
}
