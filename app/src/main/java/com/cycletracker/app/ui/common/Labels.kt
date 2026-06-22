package com.cycletracker.app.ui.common

import androidx.annotation.StringRes
import com.cycletracker.app.R
import com.cycletracker.app.domain.insight.InsightType
import com.cycletracker.app.domain.model.CervicalMucus
import com.cycletracker.app.domain.model.Confidence
import com.cycletracker.app.domain.model.CyclePhase
import com.cycletracker.app.domain.model.FlowLevel
import com.cycletracker.app.domain.model.Intensity
import com.cycletracker.app.domain.model.LhTest
import com.cycletracker.app.domain.model.SexualActivity

@StringRes
fun CyclePhase.labelRes(): Int = when (this) {
    CyclePhase.MENSTRUAL -> R.string.phase_menstrual
    CyclePhase.FOLLICULAR -> R.string.phase_follicular
    CyclePhase.OVULATORY -> R.string.phase_ovulatory
    CyclePhase.LUTEAL -> R.string.phase_luteal
    CyclePhase.UNKNOWN -> R.string.phase_unknown
}

@StringRes
fun FlowLevel.labelRes(): Int = when (this) {
    FlowLevel.NONE -> R.string.flow_none
    FlowLevel.SPOTTING -> R.string.flow_spotting
    FlowLevel.LIGHT -> R.string.flow_light
    FlowLevel.MEDIUM -> R.string.flow_medium
    FlowLevel.HEAVY -> R.string.flow_heavy
}

@StringRes
fun Intensity.labelRes(): Int = when (this) {
    Intensity.NONE -> R.string.intensity_none
    Intensity.MILD -> R.string.intensity_mild
    Intensity.MODERATE -> R.string.intensity_moderate
    Intensity.SEVERE -> R.string.intensity_severe
}

@StringRes
fun Confidence.labelRes(): Int = when (this) {
    Confidence.NONE -> R.string.confidence_none
    Confidence.LOW -> R.string.confidence_low
    Confidence.MEDIUM -> R.string.confidence_medium
    Confidence.HIGH -> R.string.confidence_high
}

@StringRes
fun CervicalMucus.labelRes(): Int = when (this) {
    CervicalMucus.NONE_DRY -> R.string.mucus_none
    CervicalMucus.STICKY -> R.string.mucus_sticky
    CervicalMucus.CREAMY -> R.string.mucus_creamy
    CervicalMucus.WATERY -> R.string.mucus_watery
    CervicalMucus.EGG_WHITE -> R.string.mucus_egg_white
}

@StringRes
fun LhTest.labelRes(): Int = when (this) {
    LhTest.NOT_TAKEN -> R.string.lh_not_taken
    LhTest.NEGATIVE -> R.string.lh_negative
    LhTest.POSITIVE -> R.string.lh_positive
}

@StringRes
fun SexualActivity.labelRes(): Int = when (this) {
    SexualActivity.NONE -> R.string.sex_none
    SexualActivity.PROTECTED -> R.string.sex_protected
    SexualActivity.UNPROTECTED -> R.string.sex_unprotected
}

@StringRes
fun InsightType.labelRes(): Int = when (this) {
    InsightType.NEEDS_MORE_DATA -> R.string.insight_needs_more_data
    InsightType.LOW_CONFIDENCE -> R.string.insight_low_confidence
    InsightType.IRREGULAR_CYCLES -> R.string.insight_irregular
    InsightType.FERTILE_WINDOW -> R.string.insight_fertile_window
    InsightType.OVULATION_SOON -> R.string.insight_ovulation_soon
    InsightType.OVULATION_TODAY -> R.string.insight_ovulation_today
    InsightType.PERIOD_DUE -> R.string.insight_period_due
    InsightType.PERIOD_LATE -> R.string.insight_period_late
    InsightType.PMS_LIKELY -> R.string.insight_pms_likely
    InsightType.OUT_OF_RANGE_CYCLE -> R.string.insight_out_of_range
}
