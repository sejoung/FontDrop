package io.github.sejoung.fontdrop.ui.theme

import androidx.compose.ui.graphics.Color

object FontDropPalette {
    val Ink900 = Color(0xFF173C34)
    val Ink700 = Color(0xFF2D5A4E)
    val Ink500 = Color(0xFF4E786C)

    val Paper50 = Color(0xFFFFF9F0)
    val Paper100 = Color(0xFFF8EEDF)
    val Paper200 = Color(0xFFEEDBC0)

    val Gold400 = Color(0xFFE4AE59)
    val Gold500 = Color(0xFFD89C3F)
    val Gold600 = Color(0xFFBB7E2E)

    val Clay300 = Color(0xFFD8B48C)
    val Clay500 = Color(0xFFB3825F)
    val Clay700 = Color(0xFF875D40)

    // Semantic text tokens: shifted one step deeper than the original spec so
    // every role meets WCAG AA on warm paper surfaces. Brand values still come
    // from the Ink scale above (Primary=Ink900, Secondary=Ink700, Tertiary=Ink500).
    val TextPrimary = Color(0xFF173C34)
    val TextSecondary = Color(0xFF2D5A4E)
    val TextTertiary = Color(0xFF4E786C)
    val TextInverse = Color(0xFFFFF9F0)

    val BackgroundBase = Color(0xFFFFF9F0)
    val BackgroundElevated = Color(0xFFFFF4E3)
    val BackgroundStrong = Color(0xFFF3E5CF)
    val NoteCardSurface = Color(0xFFFFFDF8)

    val BorderSoft = Color(0xFFE8D9C3)
    val BorderDefault = Color(0xFFDCC7AB)
    val BorderStrong = Color(0xFFBFA37E)

    val Success = Color(0xFF4E786C)
    val Warning = Color(0xFFD89C3F)
    val ErrorWarm = Color(0xFFB85C4D)
    val Info = Color(0xFF7A8F89)

    val DarkBase = Color(0xFF14211E)
    val DarkElevated = Color(0xFF1C2E2A)
    val DarkCard = Color(0xFF243934)
    val DarkTextPrimary = Color(0xFFF8EEDF)
}
