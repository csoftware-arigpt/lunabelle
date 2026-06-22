package com.cycletracker.app.core.designsystem.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.cycletracker.app.domain.model.ThemeMode

/**
 * Material 3 Expressive theme. The whole colour scheme can be derived from a single [seedColor]
 * (0 = use the built-in cute pink palette). Dynamic Material You is opt-in.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CycleTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    seedColor: Long = 0L,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val dark = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        seedColor != 0L -> colorSchemeFromSeed(seedColor, dark)
        dark -> DarkColors
        else -> LightColors
    }
    MaterialExpressiveTheme(
        colorScheme = colorScheme,
        motionScheme = MotionScheme.expressive(),
        shapes = CycleShapes,
        typography = CycleTypography,
        content = content,
    )
}
