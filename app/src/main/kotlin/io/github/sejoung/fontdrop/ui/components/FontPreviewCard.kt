package io.github.sejoung.fontdrop.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import io.github.sejoung.fontdrop.ui.theme.FontDropPalette
import io.github.sejoung.fontdrop.ui.theme.FontDropTheme

@Composable
fun FontPreviewCard(
    fontName: String,
    styleLabel: String,
    previewText: String,
    modifier: Modifier = Modifier,
    previewFontFamily: FontFamily = FontFamily.Serif,
    onClick: () -> Unit = {},
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
        shape = RoundedCornerShape(FontDropTheme.radius.l),
        colors = CardDefaults.cardColors(
            containerColor = Color.White,
            contentColor = FontDropPalette.TextPrimary,
        ),
        border = BorderStroke(1.dp, FontDropPalette.BorderSoft),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(
                horizontal = FontDropTheme.spacing.m,
                vertical = FontDropTheme.spacing.l,
            ),
            verticalArrangement = Arrangement.spacedBy(FontDropTheme.spacing.s),
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
                Icon(
                    imageVector = Icons.Rounded.ChevronRight,
                    contentDescription = null,
                    tint = FontDropPalette.TextSecondary,
                )
            }
            Text(
                text = previewText,
                style = FontDropTheme.type.displayS.copy(fontFamily = previewFontFamily),
                color = FontDropPalette.TextPrimary,
            )
        }
    }
}
