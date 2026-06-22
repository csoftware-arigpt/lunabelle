package com.cycletracker.app.ui.common

import kotlinx.datetime.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

private val mediumFormatter: DateTimeFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)

/** Locale-aware medium date, e.g. "Jul 14, 2026" / "14 июл. 2026 г." */
fun LocalDate.formatMedium(): String =
    java.time.LocalDate.of(year, monthNumber, dayOfMonth).format(mediumFormatter)
