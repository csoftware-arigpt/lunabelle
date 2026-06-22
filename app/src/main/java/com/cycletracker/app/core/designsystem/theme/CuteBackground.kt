package com.cycletracker.app.core.designsystem.theme

import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush

/**
 * Vivid "unicorn" pastel wash (pink -> lavender -> teal) behind the main screens.
 * Semi-transparent over the surface so it reads as a bright, colourful background in both themes
 * while keeping foreground text legible.
 */
@Composable
fun Modifier.cuteBackground(): Modifier = this.background(
    Brush.verticalGradient(
        listOf(
            MenstrualColor.copy(alpha = 0.55f),
            LutealColor.copy(alpha = 0.40f),
            FollicularColor.copy(alpha = 0.40f),
            OvulationColor.copy(alpha = 0.45f),
        ),
    ),
)
