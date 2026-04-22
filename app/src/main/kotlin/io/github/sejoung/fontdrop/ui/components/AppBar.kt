package io.github.sejoung.fontdrop.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import io.github.sejoung.fontdrop.ui.theme.FontDropTheme

@Composable
fun FontDropTopBar(
    title: String,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    onLeadingClick: (() -> Unit)? = null,
    trailingIcon: ImageVector? = null,
    onTrailingClick: (() -> Unit)? = null,
    trailingContent: @Composable RowScope.() -> Unit = {},
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = FontDropTheme.spacing.s),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.padding(end = FontDropTheme.spacing.xs)) {
            if (leadingIcon != null) {
                IconButton(onClick = { onLeadingClick?.invoke() }) {
                    Icon(
                        imageVector = leadingIcon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
        Text(
            text = title,
            style = FontDropTheme.type.headingS,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        if (trailingIcon != null) {
            IconButton(onClick = { onTrailingClick?.invoke() }) {
                Icon(
                    imageVector = trailingIcon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
        trailingContent()
    }
}
