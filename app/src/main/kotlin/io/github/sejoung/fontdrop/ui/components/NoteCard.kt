package io.github.sejoung.fontdrop.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.sejoung.fontdrop.ui.theme.FontDropPalette
import io.github.sejoung.fontdrop.ui.theme.FontDropTheme

@Composable
fun NoteCard(
    title: String,
    snippet: String,
    editedLabel: String,
    modifier: Modifier = Modifier,
    fontFamily: FontFamily = FontFamily.Default,
    onClick: () -> Unit = {},
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
        shape = RoundedCornerShape(FontDropTheme.radius.l),
        colors = CardDefaults.cardColors(
            containerColor = FontDropPalette.NoteCardSurface,
            contentColor = FontDropPalette.TextPrimary,
        ),
        border = BorderStroke(1.dp, FontDropPalette.BorderSoft),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(FontDropTheme.spacing.m),
            verticalArrangement = Arrangement.spacedBy(FontDropTheme.spacing.s),
        ) {
            Text(
                text = title,
                style = FontDropTheme.type.headingS.copy(fontFamily = fontFamily),
                color = FontDropPalette.TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = snippet,
                style = FontDropTheme.type.bodyM.copy(fontFamily = fontFamily),
                color = FontDropPalette.TextSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                Text(
                    text = editedLabel,
                    style = FontDropTheme.type.labelS,
                    color = FontDropPalette.TextTertiary,
                )
            }
        }
    }
}
