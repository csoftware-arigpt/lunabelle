package com.cycletracker.app.domain.model

import kotlinx.datetime.LocalDate

/**
 * A menstrual period (bleeding episode). [start] is day 1 of bleeding; [end] is the
 * last bleeding day (null while ongoing). Cycle length is measured day-1-to-day-1
 * between consecutive periods.
 */
data class Period(
    val id: String,
    val start: LocalDate,
    val end: LocalDate? = null,
)
