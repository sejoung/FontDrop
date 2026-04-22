package io.github.sejoung.fontdrop.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.dp

@Immutable
data class FontDropRadius(
    val xs: androidx.compose.ui.unit.Dp = 6.dp,
    val s: androidx.compose.ui.unit.Dp = 10.dp,
    val m: androidx.compose.ui.unit.Dp = 14.dp,
    val l: androidx.compose.ui.unit.Dp = 20.dp,
    val xl: androidx.compose.ui.unit.Dp = 28.dp,
)

val FontDropRadii = FontDropRadius()

internal val Material3Shapes = Shapes(
    extraSmall = RoundedCornerShape(FontDropRadii.xs),
    small = RoundedCornerShape(FontDropRadii.s),
    medium = RoundedCornerShape(FontDropRadii.m),
    large = RoundedCornerShape(FontDropRadii.l),
    extraLarge = RoundedCornerShape(FontDropRadii.xl),
)
