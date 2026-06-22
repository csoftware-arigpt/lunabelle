package com.cycletracker.app.domain.prediction

/**
 * Tunable parameters for the prediction engine. Defaults are clinically grounded and now
 * include recency weighting, a Bayesian population prior, and a BBT-shift threshold.
 */
data class PredictionConfig(
    val windowSize: Int = 6,
    val lutealOffsetDays: Int = 13,
    val defaultCycleLength: Int = 28,
    val defaultPeriodLength: Int = 5,
    val outlierMaxCycleDays: Int = 90,
    val outlierSdMultiplier: Double = 2.5,
    val skippedLogMultiplier: Double = 1.8,
    val irregularSdThresholdDays: Double = 7.0,
    val tightSdThresholdDays: Double = 4.0,
    val fertileWindowPreDays: Int = 5,
    val minCyclesForPrediction: Int = 3,
    val sdWindowSize: Int = 12,
    // Algorithm upgrade:
    val recencyDecay: Double = 0.85,      // weight = decay^(age); recent cycles count more
    val priorCycleLength: Double = 29.0,  // population prior mean for Bayesian shrinkage
    val priorStrength: Double = 2.0,      // equivalent prior observations
    val bbtShiftThresholdC: Double = 0.2, // sustained rise (Celsius) signalling ovulation
)
