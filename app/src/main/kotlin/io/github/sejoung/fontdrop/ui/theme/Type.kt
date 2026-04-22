package io.github.sejoung.fontdrop.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val UiFamily = FontFamily.Default

@Immutable
data class FontDropTypography(
    val displayL: TextStyle,
    val displayM: TextStyle,
    val displayS: TextStyle,
    val headingL: TextStyle,
    val headingM: TextStyle,
    val headingS: TextStyle,
    val bodyL: TextStyle,
    val bodyM: TextStyle,
    val bodyS: TextStyle,
    val labelL: TextStyle,
    val labelM: TextStyle,
    val labelS: TextStyle,
)

val FontDropType = FontDropTypography(
    displayL = TextStyle(fontFamily = UiFamily, fontSize = 40.sp, lineHeight = 48.sp, fontWeight = FontWeight.Bold),
    displayM = TextStyle(fontFamily = UiFamily, fontSize = 32.sp, lineHeight = 40.sp, fontWeight = FontWeight.Bold),
    displayS = TextStyle(fontFamily = UiFamily, fontSize = 28.sp, lineHeight = 36.sp, fontWeight = FontWeight.SemiBold),
    headingL = TextStyle(fontFamily = UiFamily, fontSize = 24.sp, lineHeight = 32.sp, fontWeight = FontWeight.Bold),
    headingM = TextStyle(fontFamily = UiFamily, fontSize = 20.sp, lineHeight = 28.sp, fontWeight = FontWeight.Bold),
    headingS = TextStyle(fontFamily = UiFamily, fontSize = 18.sp, lineHeight = 24.sp, fontWeight = FontWeight.SemiBold),
    bodyL = TextStyle(fontFamily = UiFamily, fontSize = 16.sp, lineHeight = 24.sp, fontWeight = FontWeight.Normal),
    bodyM = TextStyle(fontFamily = UiFamily, fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.Normal),
    bodyS = TextStyle(fontFamily = UiFamily, fontSize = 12.sp, lineHeight = 18.sp, fontWeight = FontWeight.Normal),
    labelL = TextStyle(fontFamily = UiFamily, fontSize = 14.sp, lineHeight = 18.sp, fontWeight = FontWeight.SemiBold),
    labelM = TextStyle(fontFamily = UiFamily, fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.SemiBold),
    labelS = TextStyle(fontFamily = UiFamily, fontSize = 11.sp, lineHeight = 14.sp, fontWeight = FontWeight.SemiBold),
)

internal val Material3Typography = Typography(
    displayLarge = FontDropType.displayL,
    displayMedium = FontDropType.displayM,
    displaySmall = FontDropType.displayS,
    headlineLarge = FontDropType.headingL,
    headlineMedium = FontDropType.headingM,
    headlineSmall = FontDropType.headingS,
    titleLarge = FontDropType.headingM,
    titleMedium = FontDropType.headingS,
    titleSmall = FontDropType.labelL,
    bodyLarge = FontDropType.bodyL,
    bodyMedium = FontDropType.bodyM,
    bodySmall = FontDropType.bodyS,
    labelLarge = FontDropType.labelL,
    labelMedium = FontDropType.labelM,
    labelSmall = FontDropType.labelS,
)
