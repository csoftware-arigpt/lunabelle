package com.cycletracker.app.ui.common

import androidx.annotation.StringRes
import com.cycletracker.app.R

data class SymptomDef(val code: String, @StringRes val labelRes: Int)
data class SymptomCategory(@StringRes val titleRes: Int, val symptoms: List<SymptomDef>)

/** Stable symptom codes (stored in DB) grouped into localized categories. */
val SYMPTOM_CATALOG: List<SymptomCategory> = listOf(
    SymptomCategory(
        R.string.cat_pain,
        listOf(
            SymptomDef("cramps", R.string.sym_cramps),
            SymptomDef("headache", R.string.sym_headache),
            SymptomDef("back_pain", R.string.sym_back_pain),
            SymptomDef("breast_tenderness", R.string.sym_breast_tenderness),
            SymptomDef("ovulation_pain", R.string.sym_ovulation_pain),
        ),
    ),
    SymptomCategory(
        R.string.cat_physical,
        listOf(
            SymptomDef("bloating", R.string.sym_bloating),
            SymptomDef("fatigue", R.string.sym_fatigue),
            SymptomDef("acne", R.string.sym_acne),
            SymptomDef("nausea", R.string.sym_nausea),
            SymptomDef("cravings", R.string.sym_cravings),
        ),
    ),
    SymptomCategory(
        R.string.cat_mood,
        listOf(
            SymptomDef("irritability", R.string.sym_irritability),
            SymptomDef("anxiety", R.string.sym_anxiety),
            SymptomDef("low_mood", R.string.sym_low_mood),
            SymptomDef("mood_swings", R.string.sym_mood_swings),
        ),
    ),
    SymptomCategory(
        R.string.cat_energy,
        listOf(
            SymptomDef("high_energy", R.string.sym_high_energy),
            SymptomDef("low_energy", R.string.sym_low_energy),
            SymptomDef("insomnia", R.string.sym_insomnia),
        ),
    ),
    SymptomCategory(
        R.string.cat_digestion,
        listOf(
            SymptomDef("constipation", R.string.sym_constipation),
            SymptomDef("diarrhea", R.string.sym_diarrhea),
        ),
    ),
    SymptomCategory(
        R.string.cat_other,
        listOf(
            SymptomDef("high_libido", R.string.sym_high_libido),
            SymptomDef("low_libido", R.string.sym_low_libido),
        ),
    ),
)

val SYMPTOM_LABELS: Map<String, Int> =
    SYMPTOM_CATALOG.flatMap { it.symptoms }.associate { it.code to it.labelRes }
