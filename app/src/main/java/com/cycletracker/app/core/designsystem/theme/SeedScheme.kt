package com.cycletracker.app.core.designsystem.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/** A single accent colour the whole Material 3 scheme is derived from (theme customisation). */
private fun hsl(hDeg: Float, s: Float, l: Float): Color {
    val h = ((hDeg % 360f) + 360f) % 360f
    val c = (1f - abs(2f * l - 1f)) * s
    val hp = h / 60f
    val x = c * (1f - abs(hp % 2f - 1f))
    val (r, g, b) = when {
        hp < 1f -> Triple(c, x, 0f)
        hp < 2f -> Triple(x, c, 0f)
        hp < 3f -> Triple(0f, c, x)
        hp < 4f -> Triple(0f, x, c)
        hp < 5f -> Triple(x, 0f, c)
        else -> Triple(c, 0f, x)
    }
    val m = l - c / 2f
    return Color((r + m).coerceIn(0f, 1f), (g + m).coerceIn(0f, 1f), (b + m).coerceIn(0f, 1f))
}

private data class Hsl(val h: Float, val s: Float, val l: Float)

private fun Color.toHsl(): Hsl {
    val mx = max(red, max(green, blue))
    val mn = min(red, min(green, blue))
    val l = (mx + mn) / 2f
    val d = mx - mn
    if (d < 1e-4f) return Hsl(330f, 0.9f, l)
    val s = d / (1f - abs(2f * l - 1f))
    val h = when (mx) {
        red -> 60f * (((green - blue) / d) % 6f)
        green -> 60f * (((blue - red) / d) + 2f)
        else -> 60f * (((red - green) / d) + 4f)
    }
    return Hsl(((h % 360f) + 360f) % 360f, s, l)
}

/** Build a vivid, high-contrast Material 3 [ColorScheme] from one seed colour. */
fun colorSchemeFromSeed(seedArgb: Long, dark: Boolean): ColorScheme {
    val base = Color(seedArgb).toHsl()
    val h = base.h
    val ht = (h + 60f) % 360f
    val aS = base.s.coerceIn(0.85f, 1.0f)   // vivid accent chroma
    val nS = 0.10f                           // subtle surface tint (keeps text contrast high)
    return if (dark) {
        darkColorScheme(
            primary = hsl(h, aS, 0.82f),
            onPrimary = hsl(h, aS, 0.16f),
            primaryContainer = hsl(h, aS * 0.9f, 0.34f),
            onPrimaryContainer = hsl(h, aS * 0.7f, 0.95f),
            secondary = hsl(h, 0.60f, 0.82f),
            onSecondary = hsl(h, 0.60f, 0.16f),
            secondaryContainer = hsl(h, 0.55f, 0.32f),
            onSecondaryContainer = hsl(h, 0.45f, 0.95f),
            tertiary = hsl(ht, aS, 0.80f),
            onTertiary = hsl(ht, aS, 0.16f),
            tertiaryContainer = hsl(ht, aS * 0.8f, 0.32f),
            onTertiaryContainer = hsl(ht, aS * 0.6f, 0.95f),
            background = hsl(h, nS * 1.3f, 0.07f),
            onBackground = hsl(h, nS, 0.96f),
            surface = hsl(h, nS * 1.3f, 0.07f),
            onSurface = hsl(h, nS, 0.96f),
            surfaceVariant = hsl(h, nS, 0.30f),
            onSurfaceVariant = hsl(h, nS, 0.84f),
            surfaceDim = hsl(h, nS * 1.3f, 0.06f),
            surfaceBright = hsl(h, nS, 0.28f),
            surfaceContainerLowest = hsl(h, nS * 1.3f, 0.05f),
            surfaceContainerLow = hsl(h, nS * 1.2f, 0.11f),
            surfaceContainer = hsl(h, nS * 1.2f, 0.14f),
            surfaceContainerHigh = hsl(h, nS * 1.1f, 0.18f),
            surfaceContainerHighest = hsl(h, nS, 0.23f),
            outline = hsl(h, nS, 0.62f),
            outlineVariant = hsl(h, nS, 0.32f),
            inverseSurface = hsl(h, nS, 0.95f),
            inverseOnSurface = hsl(h, nS, 0.16f),
            inversePrimary = hsl(h, aS, 0.45f),
            surfaceTint = hsl(h, aS, 0.82f),
            scrim = Color(0xFF000000),
        )
    } else {
        lightColorScheme(
            primary = hsl(h, aS, 0.46f),
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = hsl(h, aS * 0.85f, 0.86f),
            onPrimaryContainer = hsl(h, aS, 0.13f),
            secondary = hsl(h, 0.55f, 0.44f),
            onSecondary = Color(0xFFFFFFFF),
            secondaryContainer = hsl(h, 0.50f, 0.88f),
            onSecondaryContainer = hsl(h, 0.55f, 0.14f),
            tertiary = hsl(ht, aS, 0.44f),
            onTertiary = Color(0xFFFFFFFF),
            tertiaryContainer = hsl(ht, aS * 0.7f, 0.86f),
            onTertiaryContainer = hsl(ht, aS, 0.13f),
            background = hsl(h, nS, 0.99f),
            onBackground = hsl(h, nS, 0.09f),
            surface = hsl(h, nS, 0.99f),
            onSurface = hsl(h, nS, 0.09f),
            surfaceVariant = hsl(h, nS * 1.6f, 0.90f),
            onSurfaceVariant = hsl(h, nS, 0.32f),
            surfaceDim = hsl(h, nS, 0.87f),
            surfaceBright = hsl(h, nS, 0.99f),
            surfaceContainerLowest = Color(0xFFFFFFFF),
            surfaceContainerLow = hsl(h, nS, 0.97f),
            surfaceContainer = hsl(h, nS, 0.95f),
            surfaceContainerHigh = hsl(h, nS, 0.92f),
            surfaceContainerHighest = hsl(h, nS, 0.90f),
            outline = hsl(h, nS, 0.50f),
            outlineVariant = hsl(h, nS, 0.78f),
            inverseSurface = hsl(h, nS, 0.18f),
            inverseOnSurface = hsl(h, nS, 0.97f),
            inversePrimary = hsl(h, aS, 0.82f),
            surfaceTint = hsl(h, aS, 0.46f),
            scrim = Color(0xFF000000),
        )
    }
}
