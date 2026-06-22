package com.cycletracker.app.domain.model

import kotlinx.datetime.LocalDate

/** All trackable signals for a single calendar date. */
data class DailyLog(
    val date: LocalDate,
    val flow: FlowLevel = FlowLevel.NONE,
    val clots: Boolean = false,
    val cervicalMucus: CervicalMucus = CervicalMucus.NONE_DRY,
    val lhTest: LhTest = LhTest.NOT_TAKEN,
    val sexualActivity: SexualActivity = SexualActivity.NONE,
    val bbtCelsius: Double? = null,
    val weightKg: Double? = null,
    val notes: String? = null,
    val symptoms: List<SymptomEntry> = emptyList(),
) {
    val isEmpty: Boolean
        get() = flow == FlowLevel.NONE && !clots && cervicalMucus == CervicalMucus.NONE_DRY &&
            lhTest == LhTest.NOT_TAKEN && sexualActivity == SexualActivity.NONE &&
            bbtCelsius == null && weightKg == null && notes.isNullOrBlank() && symptoms.isEmpty()
}

/** A symptom logged for a day with its intensity. [symptomCode] is a stable enum code. */
data class SymptomEntry(
    val symptomCode: String,
    val intensity: Intensity,
)
