package io.github.sejoung.fontdrop.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import io.github.sejoung.fontdrop.ui.theme.FontDropPalette
import io.github.sejoung.fontdrop.ui.theme.FontDropTheme

@Composable
fun FontDropChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val bg = if (selected) FontDropPalette.Ink900 else FontDropPalette.Paper100
    val fg = if (selected) FontDropPalette.TextInverse else FontDropPalette.TextSecondary
    Text(
        text = label,
        style = FontDropTheme.type.labelM,
        color = fg,
        modifier = modifier
            .clip(CircleShape)
            .background(bg)
            .clickable { onClick() }
            .padding(PaddingValues(horizontal = 14.dp, vertical = 8.dp)),
    )
}
