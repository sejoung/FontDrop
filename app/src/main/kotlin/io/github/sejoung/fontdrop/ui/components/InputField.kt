package io.github.sejoung.fontdrop.ui.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import io.github.sejoung.fontdrop.ui.theme.FontDropPalette
import io.github.sejoung.fontdrop.ui.theme.FontDropTheme

@Composable
fun FontDropInputField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    singleLine: Boolean = true,
    isError: Boolean = false,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        placeholder = placeholder?.let {
            { Text(text = it, style = FontDropTheme.type.bodyL, color = FontDropPalette.TextTertiary) }
        },
        singleLine = singleLine,
        isError = isError,
        textStyle = FontDropTheme.type.bodyL,
        shape = RoundedCornerShape(FontDropTheme.radius.m),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = FontDropPalette.BackgroundElevated,
            disabledContainerColor = FontDropPalette.BackgroundStrong,
            focusedBorderColor = FontDropPalette.Ink500,
            unfocusedBorderColor = FontDropPalette.BorderDefault,
            focusedTextColor = FontDropPalette.TextPrimary,
            unfocusedTextColor = FontDropPalette.TextPrimary,
            cursorColor = FontDropPalette.Ink700,
            errorBorderColor = FontDropPalette.ErrorWarm,
        ),
    )
}
