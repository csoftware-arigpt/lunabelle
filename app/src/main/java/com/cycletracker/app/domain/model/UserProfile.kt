package com.cycletracker.app.domain.model

/** User-configurable cycle parameters that feed the prediction engine. */
data class UserProfile(
    val birthYear: Int? = null,
    val defaultCycleLength: Int = 28,
    val avgPeriodLength: Int = 5,
    val lutealOffsetDays: Int = 13,
    val predictionWindow: Int = 6,
    val goalMode: GoalMode = GoalMode.TRACKING_ONLY,
    val heightCm: Int? = null,
)
