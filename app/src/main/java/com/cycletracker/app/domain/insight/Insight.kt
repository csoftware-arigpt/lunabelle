package com.cycletracker.app.domain.insight

enum class InsightType {
    NEEDS_MORE_DATA,
    LOW_CONFIDENCE,
    IRREGULAR_CYCLES,
    FERTILE_WINDOW,
    OVULATION_SOON,
    OVULATION_TODAY,
    PERIOD_DUE,
    PERIOD_LATE,
    PMS_LIKELY,
    OUT_OF_RANGE_CYCLE,
}

enum class InsightSeverity { INFO, ATTENTION, WARNING }

/** A language-agnostic insight; the UI renders localized RU/EN text from [type]. */
data class Insight(val type: InsightType, val severity: InsightSeverity)
