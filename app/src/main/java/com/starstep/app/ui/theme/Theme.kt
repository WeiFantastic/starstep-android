package com.starstep.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * 星阶是沉浸式夜空产品，强制深色主题（不随系统切换），
 * 保证星空主视觉与星星色卡始终一致。
 */
private val StarStepColorScheme = darkColorScheme(
    primary = Accent,
    onPrimary = NightBg,
    primaryContainer = Deep,
    onPrimaryContainer = Ink,
    secondary = StarSky,
    onSecondary = NightBg,
    secondaryContainer = NightSurfaceHigh,
    onSecondaryContainer = Ink,
    tertiary = StarMint,
    onTertiary = NightBg,
    background = NightBg,
    onBackground = Ink,
    surface = NightSurface,
    onSurface = Ink,
    surfaceVariant = NightSurfaceHigh,
    onSurfaceVariant = InkSub,
    outline = NightLine,
    outlineVariant = NightLine,
    error = StarRose,
    onError = NightBg
)

@Composable
fun StarStepTheme(
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = false
                isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = StarStepColorScheme,
        typography = StarStepTypography,
        content = content
    )
}
