package com.cycletracker.app.core.designsystem.expressive

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/** Isolation boundary for Material 3 Expressive (alpha) components. */

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CycleDayRing(
    progress: Float,
    modifier: Modifier = Modifier,
    ringSize: Int = 240,
    color: Color = MaterialTheme.colorScheme.primary,
    center: @Composable () -> Unit = {},
) {
    Box(modifier = modifier.size(ringSize.dp), contentAlignment = Alignment.Center) {
        CircularWavyProgressIndicator(
            progress = { progress.coerceIn(0f, 1f) },
            modifier = Modifier.size(ringSize.dp),
            color = color,
        )
        center()
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PhaseProgressBar(progress: Float, modifier: Modifier = Modifier, color: Color = MaterialTheme.colorScheme.primary) {
    LinearWavyProgressIndicator(
        progress = { progress.coerceIn(0f, 1f) },
        modifier = modifier,
        color = color,
    )
}
