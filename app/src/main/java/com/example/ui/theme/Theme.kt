package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import com.example.core.preferences.ThemeMode

private val LightColorScheme = lightColorScheme(
    primary = EducorePrimary,
    onPrimary = LightSurface,
    primaryContainer = EducorePrimaryContainer,
    onPrimaryContainer = EducoreOnPrimaryContainer,
    secondary = EducoreSecondary,
    onSecondary = LightSurface,
    secondaryContainer = EducoreSecondaryContainer,
    onSecondaryContainer = EducoreOnSecondaryContainer,
    tertiary = EducorePurple,
    onTertiary = LightSurface,
    tertiaryContainer = EducorePurpleBg,
    onTertiaryContainer = EducorePurple,
    background = LightCanvas,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightTextSecondary,
    outline = LightBorder,
    error = EducoreError,
    onError = LightSurface,
    errorContainer = EducoreErrorBg,
    onErrorContainer = EducoreError
)

private val DarkColorScheme = darkColorScheme(
    primary = EducorePrimaryLight,
    onPrimary = DarkCanvas,
    primaryContainer = EducorePrimaryDark,
    onPrimaryContainer = LightSurface,
    secondary = EducoreSecondary,
    onSecondary = DarkCanvas,
    secondaryContainer = DarkSurfaceVariant,
    onSecondaryContainer = LightSurface,
    tertiary = EducorePurple,
    onTertiary = DarkCanvas,
    tertiaryContainer = DarkSurfaceVariant,
    onTertiaryContainer = LightSurface,
    background = DarkCanvas,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkBorder,
    error = EducoreError,
    onError = DarkCanvas,
    errorContainer = DarkSurfaceVariant,
    onErrorContainer = EducoreError
)

@Composable
fun EducoreTheme(
    themeMode: ThemeMode = ThemeMode.LIGHT,
    content: @Composable () -> Unit
) {
    val isDark = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    val colorScheme = if (isDark) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

// Backward compatibility alias
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    EducoreTheme(
        themeMode = if (darkTheme) ThemeMode.DARK else ThemeMode.LIGHT,
        content = content
    )
}
