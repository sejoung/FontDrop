package io.github.sejoung.fontdrop.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class FontDropShadow(
    val offsetY: Dp,
    val blur: Dp,
    val color: Color,
)

object FontDropShadows {
    val s1 = FontDropShadow(2.dp, 8.dp, Color(0x14173C34))
    val s2 = FontDropShadow(6.dp, 18.dp, Color(0x1A173C34))
    val s3 = FontDropShadow(10.dp, 28.dp, Color(0x24173C34))
}
