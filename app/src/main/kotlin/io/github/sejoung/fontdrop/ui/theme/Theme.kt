package io.github.sejoung.fontdrop.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.foundation.isSystemInDarkTheme

private val LightColors = lightColorScheme(
    primary = FontDropPalette.Ink900,
    onPrimary = FontDropPalette.TextInverse,
    primaryContainer = FontDropPalette.Ink700,
    onPrimaryContainer = FontDropPalette.TextInverse,
    secondary = FontDropPalette.Gold500,
    onSecondary = FontDropPalette.Ink900,
    secondaryContainer = FontDropPalette.Gold400,
    onSecondaryContainer = FontDropPalette.Ink900,
    tertiary = FontDropPalette.Clay500,
    onTertiary = FontDropPalette.TextInverse,
    background = FontDropPalette.BackgroundBase,
    onBackground = FontDropPalette.TextPrimary,
    surface = FontDropPalette.BackgroundElevated,
    onSurface = FontDropPalette.TextPrimary,
    surfaceVariant = FontDropPalette.BackgroundStrong,
    onSurfaceVariant = FontDropPalette.TextSecondary,
    outline = FontDropPalette.BorderDefault,
    outlineVariant = FontDropPalette.BorderSoft,
    error = FontDropPalette.ErrorWarm,
    onError = FontDropPalette.TextInverse,
)

private val DarkColors = darkColorScheme(
    primary = FontDropPalette.Gold400,
    onPrimary = FontDropPalette.Ink900,
    primaryContainer = FontDropPalette.Ink700,
    onPrimaryContainer = FontDropPalette.TextInverse,
    secondary = FontDropPalette.Gold500,
    onSecondary = FontDropPalette.Ink900,
    tertiary = FontDropPalette.Clay500,
    onTertiary = FontDropPalette.Ink900,
    background = FontDropPalette.DarkBase,
    onBackground = FontDropPalette.DarkTextPrimary,
    surface = FontDropPalette.DarkElevated,
    onSurface = FontDropPalette.DarkTextPrimary,
    surfaceVariant = FontDropPalette.DarkCard,
    onSurfaceVariant = FontDropPalette.Paper100,
    outline = FontDropPalette.Clay500,
    outlineVariant = FontDropPalette.Ink500,
    error = FontDropPalette.ErrorWarm,
    onError = FontDropPalette.TextInverse,
)

val LocalFontDropSpacing = staticCompositionLocalOf { FontDropSpaces }
val LocalFontDropRadius = staticCompositionLocalOf { FontDropRadii }
val LocalFontDropTypography = staticCompositionLocalOf { FontDropType }

object FontDropTheme {
    val spacing @Composable get() = LocalFontDropSpacing.current
    val radius @Composable get() = LocalFontDropRadius.current
    val type @Composable get() = LocalFontDropTypography.current
}

@Composable
fun FontDropTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val scheme = if (darkTheme) DarkColors else LightColors
    CompositionLocalProvider(
        LocalFontDropSpacing provides FontDropSpaces,
        LocalFontDropRadius provides FontDropRadii,
        LocalFontDropTypography provides FontDropType,
    ) {
        MaterialTheme(
            colorScheme = scheme,
            typography = Material3Typography,
            shapes = Material3Shapes,
            content = content,
        )
    }
}
