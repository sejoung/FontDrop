package io.github.sejoung.fontdrop.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.sejoung.fontdrop.ui.theme.FontDropPalette
import io.github.sejoung.fontdrop.ui.theme.FontDropTheme

private val ButtonPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)

@Composable
fun FontDropPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = RoundedCornerShape(FontDropTheme.radius.m),
        colors = ButtonDefaults.buttonColors(
            containerColor = FontDropPalette.Ink900,
            contentColor = FontDropPalette.TextInverse,
        ),
        contentPadding = ButtonPadding,
    ) {
        Text(text = text, style = FontDropTheme.type.labelL)
    }
}

@Composable
fun FontDropSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = RoundedCornerShape(FontDropTheme.radius.m),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = FontDropPalette.Paper100,
            contentColor = FontDropPalette.Ink900,
        ),
        border = BorderStroke(1.dp, FontDropPalette.BorderDefault),
        contentPadding = ButtonPadding,
    ) {
        Text(text = text, style = FontDropTheme.type.labelL)
    }
}

@Composable
fun FontDropAccentButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = RoundedCornerShape(FontDropTheme.radius.m),
        colors = ButtonDefaults.buttonColors(
            containerColor = FontDropPalette.Gold400,
            contentColor = FontDropPalette.Ink900,
        ),
        contentPadding = ButtonPadding,
    ) {
        Text(text = text, style = FontDropTheme.type.labelL)
    }
}
