package io.github.sejoung.fontdrop.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.background
import io.github.sejoung.fontdrop.ui.theme.FontDropPalette
import io.github.sejoung.fontdrop.ui.theme.FontDropTheme

@Composable
fun FontPreviewCard(
    fontName: String,
    styleLabel: String,
    previewText: String,
    modifier: Modifier = Modifier,
    previewFontFamily: FontFamily = FontFamily.Serif,
    isLoading: Boolean = false,
    selected: Boolean = false,
    onClick: () -> Unit = {},
) {
    val border = if (selected) {
        BorderStroke(2.dp, FontDropPalette.Ink700)
    } else {
        BorderStroke(1.dp, FontDropPalette.BorderSoft)
    }
    val container = if (selected) FontDropPalette.BackgroundElevated else Color.White

    Card(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
        shape = RoundedCornerShape(FontDropTheme.radius.l),
        colors = CardDefaults.cardColors(
            containerColor = container,
            contentColor = FontDropPalette.TextPrimary,
        ),
        border = border,
        elevation = CardDefaults.cardElevation(defaultElevation = if (selected) 4.dp else 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(
                horizontal = FontDropTheme.spacing.m,
                vertical = FontDropTheme.spacing.l,
            ),
            verticalArrangement = Arrangement.spacedBy(FontDropTheme.spacing.sm),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = fontName,
                        style = FontDropTheme.type.headingS,
                        color = FontDropPalette.TextPrimary,
                    )
                    Text(
                        text = styleLabel,
                        style = FontDropTheme.type.labelM,
                        color = FontDropPalette.TextTertiary,
                    )
                }
                if (selected) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(FontDropPalette.Gold500),
                    )
                }
            }
            if (isLoading) {
                Text(
                    text = "Loading preview…",
                    style = FontDropTheme.type.bodyM,
                    color = FontDropPalette.TextTertiary,
                )
            } else {
                BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                    val previewFontSize = when {
                        maxWidth < 280.dp -> 40.sp
                        maxWidth < 360.dp -> 48.sp
                        else -> 56.sp
                    }
                    val previewLineHeight = when {
                        maxWidth < 280.dp -> 48.sp
                        maxWidth < 360.dp -> 56.sp
                        else -> 64.sp
                    }
                    Text(
                        text = "Aa",
                        style = FontDropTheme.type.displayL.copy(
                            fontFamily = previewFontFamily,
                            fontSize = previewFontSize,
                            lineHeight = previewLineHeight,
                        ),
                        color = FontDropPalette.TextPrimary,
                        maxLines = 1,
                    )
                }
                Text(
                    text = previewText,
                    style = FontDropTheme.type.bodyL.copy(fontFamily = previewFontFamily),
                    color = FontDropPalette.TextSecondary,
                    maxLines = 2,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                )
            }
        }
    }
}
