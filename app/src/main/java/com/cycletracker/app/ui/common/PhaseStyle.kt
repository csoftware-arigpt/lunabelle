package com.cycletracker.app.ui.common

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import com.cycletracker.app.core.designsystem.theme.FollicularColor
import com.cycletracker.app.core.designsystem.theme.LutealColor
import com.cycletracker.app.core.designsystem.theme.MenstrualColor
import com.cycletracker.app.core.designsystem.theme.OvulationColor
import com.cycletracker.app.domain.model.CyclePhase

@Composable
@ReadOnlyComposable
fun CyclePhase.color(): Color = when (this) {
    CyclePhase.MENSTRUAL -> MenstrualColor
    CyclePhase.FOLLICULAR -> FollicularColor
    CyclePhase.OVULATORY -> OvulationColor
    CyclePhase.LUTEAL -> LutealColor
    CyclePhase.UNKNOWN -> MaterialTheme.colorScheme.primary
}

fun CyclePhase.emoji(): String = when (this) {
    CyclePhase.MENSTRUAL -> "🩸"
    CyclePhase.FOLLICULAR -> "🌱"
    CyclePhase.OVULATORY -> "✨"
    CyclePhase.LUTEAL -> "🌙"
    CyclePhase.UNKNOWN -> "🌸"
}
