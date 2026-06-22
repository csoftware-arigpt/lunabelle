package com.cycletracker.app.ui.settings

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.cycletracker.app.R

/** A full HSV colour picker: saturation/value panel + hue slider + live preview & hex. */
@Composable
fun ColorPickerDialog(initial: Color, onDismiss: () -> Unit, onConfirm: (Long) -> Unit) {
    val start = remember(initial) { initial.toHsv3() }
    var hue by remember { mutableFloatStateOf(start.first) }
    var sat by remember { mutableFloatStateOf(start.second) }
    var value by remember { mutableFloatStateOf(start.third) }
    val current = Color.hsv(hue, sat, value)

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { onConfirm(current.toArgb().toLong() and 0xFFFFFFFFL) }) {
                Text(stringResource(R.string.color_done))
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.color_cancel)) } },
        title = { Text(stringResource(R.string.settings_theme_color)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                SatValPanel(hue, sat, value) { ns, nv -> sat = ns; value = nv }
                HueBar(hue) { hue = it }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(
                        Modifier.size(48.dp).clip(CircleShape).background(current)
                            .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape),
                    )
                    Text("#%06X".format(0xFFFFFF and current.toArgb()), style = MaterialTheme.typography.titleMedium)
                }
            }
        },
    )
}

@Composable
private fun SatValPanel(hue: Float, sat: Float, value: Float, onChange: (Float, Float) -> Unit) {
    val hueColor = Color.hsv(hue, 1f, 1f)
    Canvas(
        Modifier
            .fillMaxWidth()
            .height(190.dp)
            .clip(RoundedCornerShape(16.dp))
            .pointerInput(Unit) {
                detectTapGestures { p ->
                    onChange((p.x / size.width).coerceIn(0f, 1f), (1f - p.y / size.height).coerceIn(0f, 1f))
                }
            }
            .pointerInput(Unit) {
                detectDragGestures { change, _ ->
                    onChange(
                        (change.position.x / size.width).coerceIn(0f, 1f),
                        (1f - change.position.y / size.height).coerceIn(0f, 1f),
                    )
                }
            },
    ) {
        drawRect(Brush.horizontalGradient(listOf(Color.White, hueColor)))
        drawRect(Brush.verticalGradient(listOf(Color.Transparent, Color.Black)))
        val cx = sat * size.width
        val cy = (1f - value) * size.height
        drawCircle(Color.Black, radius = 16f, center = Offset(cx, cy), style = Stroke(width = 2f))
        drawCircle(Color.White, radius = 13f, center = Offset(cx, cy), style = Stroke(width = 3f))
    }
}

@Composable
private fun HueBar(hue: Float, onHue: (Float) -> Unit) {
    val hues = remember { (0..360 step 30).map { Color.hsv(it.toFloat(), 1f, 1f) } }
    Canvas(
        Modifier
            .fillMaxWidth()
            .height(30.dp)
            .clip(RoundedCornerShape(15.dp))
            .pointerInput(Unit) { detectTapGestures { p -> onHue((p.x / size.width * 360f).coerceIn(0f, 360f)) } }
            .pointerInput(Unit) { detectDragGestures { c, _ -> onHue((c.position.x / size.width * 360f).coerceIn(0f, 360f)) } },
    ) {
        drawRect(Brush.horizontalGradient(hues))
        val x = hue / 360f * size.width
        drawCircle(Color.White, radius = size.height / 2f - 2f, center = Offset(x, size.height / 2f), style = Stroke(width = 3f))
        drawCircle(Color.Black, radius = size.height / 2f - 2f, center = Offset(x, size.height / 2f), style = Stroke(width = 1f))
    }
}

private fun Color.toHsv3(): Triple<Float, Float, Float> {
    val r = red
    val g = green
    val b = blue
    val mx = maxOf(r, g, b)
    val mn = minOf(r, g, b)
    val d = mx - mn
    val v = mx
    val s = if (mx <= 0f) 0f else d / mx
    val h = when {
        d < 1e-4f -> 0f
        mx == r -> 60f * (((g - b) / d) % 6f)
        mx == g -> 60f * (((b - r) / d) + 2f)
        else -> 60f * (((r - g) / d) + 4f)
    }
    return Triple(((h % 360f) + 360f) % 360f, s, v)
}
