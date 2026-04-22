package io.github.sejoung.fontdrop.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import io.github.sejoung.fontdrop.ui.theme.FontDropPalette
import io.github.sejoung.fontdrop.ui.theme.FontDropTheme

@Composable
fun EmptyState(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    action: @Composable (() -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(FontDropTheme.spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(FontDropTheme.spacing.sm),
    ) {
        Text(
            text = title,
            style = FontDropTheme.type.displayS,
            color = FontDropPalette.TextPrimary,
            textAlign = TextAlign.Center,
        )
        Text(
            text = description,
            style = FontDropTheme.type.bodyL,
            color = FontDropPalette.TextSecondary,
            textAlign = TextAlign.Center,
        )
        if (action != null) {
            action()
        }
    }
}
