package com.cycletracker.app.domain.prediction

import kotlin.math.pow
import kotlin.math.sqrt

internal fun mean(values: List<Int>): Double =
    if (values.isEmpty()) 0.0 else values.sum().toDouble() / values.size

/** Recency-weighted mean: the last element gets weight 1, older ones decay by [decay]. */
internal fun weightedMean(values: List<Int>, decay: Double): Double {
    if (values.isEmpty()) return 0.0
    val n = values.size
    var weightSum = 0.0
    var valueSum = 0.0
    values.forEachIndexed { i, v ->
        val w = decay.pow((n - 1 - i).toDouble())
        weightSum += w
        valueSum += w * v
    }
    return if (weightSum == 0.0) mean(values) else valueSum / weightSum
}

/** Sample standard deviation (n-1). Returns 0.0 for fewer than 2 values. */
internal fun sampleStdDev(values: List<Int>): Double {
    if (values.size < 2) return 0.0
    val m = mean(values)
    val variance = values.sumOf { (it - m) * (it - m) } / (values.size - 1)
    return sqrt(variance)
}

internal fun median(values: List<Int>): Double {
    if (values.isEmpty()) return 0.0
    val s = values.sorted()
    val mid = s.size / 2
    return if (s.size % 2 == 1) s[mid].toDouble() else (s[mid - 1] + s[mid]) / 2.0
}
