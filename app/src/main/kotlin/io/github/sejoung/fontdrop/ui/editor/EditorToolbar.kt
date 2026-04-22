package io.github.sejoung.fontdrop.ui.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.FormatLineSpacing
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.TextFields
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.sejoung.fontdrop.ui.theme.FontDropPalette
import io.github.sejoung.fontdrop.ui.theme.FontDropTheme

@Composable
fun EditorToolbar(
    fontLabel: String,
    fontFamily: FontFamily,
    fontSizeSp: Int,
    lineHeightMultiplier: Float,
    onOpenFontPicker: () -> Unit,
    onFontSizeDelta: (Int) -> Unit,
    onLineHeightCycle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = FontDropPalette.BackgroundElevated,
        tonalElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = FontDropTheme.spacing.m,
                    vertical = FontDropTheme.spacing.s,
                ),
            horizontalArrangement = Arrangement.spacedBy(FontDropTheme.spacing.s),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            FontChip(
                label = fontLabel,
                fontFamily = fontFamily,
                onClick = onOpenFontPicker,
                modifier = Modifier.weight(1f),
            )
            SizeStepper(
                valueLabel = "${fontSizeSp}sp",
                onDecrement = { onFontSizeDelta(-EditorUiState.FONT_SIZE_STEP_SP) },
                onIncrement = { onFontSizeDelta(EditorUiState.FONT_SIZE_STEP_SP) },
            )
            LineHeightButton(
                multiplier = lineHeightMultiplier,
                onClick = onLineHeightCycle,
            )
        }
    }
}

@Composable
private fun FontChip(
    label: String,
    fontFamily: FontFamily,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.clip(RoundedCornerShape(FontDropTheme.radius.m)),
        color = FontDropPalette.Paper100,
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = FontDropTheme.spacing.sm,
                vertical = FontDropTheme.spacing.s,
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(FontDropTheme.spacing.s),
        ) {
            Icon(
                imageVector = Icons.Rounded.TextFields,
                contentDescription = null,
                tint = FontDropPalette.Ink700,
                modifier = Modifier.size(18.dp),
            )
            Text(
                text = label,
                style = FontDropTheme.type.labelL.copy(fontFamily = fontFamily),
                color = FontDropPalette.TextPrimary,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun SizeStepper(
    valueLabel: String,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(FontDropTheme.radius.m),
        color = FontDropPalette.Paper100,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onDecrement, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Rounded.Remove, contentDescription = "Decrease", tint = FontDropPalette.Ink700)
            }
            Text(
                text = valueLabel,
                style = FontDropTheme.type.labelM,
                color = FontDropPalette.TextPrimary,
                textAlign = TextAlign.Center,
                modifier = Modifier.width(40.dp),
            )
            IconButton(onClick = onIncrement, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Rounded.Add, contentDescription = "Increase", tint = FontDropPalette.Ink700)
            }
        }
    }
}

@Composable
private fun LineHeightButton(
    multiplier: Float,
    onClick: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(FontDropTheme.radius.m),
        color = FontDropPalette.Paper100,
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = FontDropTheme.spacing.sm)
                .height(36.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Icon(
                imageVector = Icons.Rounded.FormatLineSpacing,
                contentDescription = "Line spacing",
                tint = FontDropPalette.Ink700,
                modifier = Modifier.size(18.dp),
            )
            Text(
                text = "%.1fx".format(multiplier),
                style = FontDropTheme.type.labelM,
                color = FontDropPalette.TextPrimary,
            )
        }
    }
}
