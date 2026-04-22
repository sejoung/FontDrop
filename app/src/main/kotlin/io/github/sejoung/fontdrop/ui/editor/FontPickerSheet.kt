package io.github.sejoung.fontdrop.ui.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import io.github.sejoung.fontdrop.data.font.FontAsset
import io.github.sejoung.fontdrop.data.font.FontFamilyCache
import io.github.sejoung.fontdrop.ui.components.FontPreviewCard
import io.github.sejoung.fontdrop.ui.library.rememberFontFamily
import io.github.sejoung.fontdrop.ui.theme.FontDropPalette
import io.github.sejoung.fontdrop.ui.theme.FontDropTheme
import io.github.sejoung.fontdrop.ui.util.FONT_PREVIEW_SENTENCE

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FontPickerSheet(
    fonts: List<FontAsset>,
    selectedFontId: String?,
    fontFamilyCache: FontFamilyCache,
    onSelect: (String?) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = FontDropPalette.BackgroundBase,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(FontDropTheme.spacing.s),
        ) {
            Text(
                text = "Choose font",
                style = FontDropTheme.type.headingM,
                color = FontDropPalette.TextPrimary,
                modifier = Modifier.padding(horizontal = FontDropTheme.spacing.m),
            )
            if (fonts.isEmpty()) {
                Text(
                    text = "No fonts loaded yet. Add .ttf or .otf files in the Fonts tab.",
                    style = FontDropTheme.type.bodyM,
                    color = FontDropPalette.TextSecondary,
                    modifier = Modifier.padding(FontDropTheme.spacing.m),
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(
                        horizontal = FontDropTheme.spacing.m,
                        vertical = FontDropTheme.spacing.s,
                    ),
                    verticalArrangement = Arrangement.spacedBy(FontDropTheme.spacing.sm),
                ) {
                    item {
                        FontPreviewCard(
                            fontName = "System default",
                            styleLabel = "Neutral sans",
                            previewText = FONT_PREVIEW_SENTENCE,
                            previewFontFamily = FontFamily.Default,
                            selected = selectedFontId == null,
                            onClick = { onSelect(null) },
                        )
                    }
                    items(fonts, key = { it.id }) { font ->
                        val family by rememberFontFamily(asset = font, cache = fontFamilyCache)
                        FontPreviewCard(
                            fontName = font.familyName,
                            styleLabel = ".${font.extension}",
                            previewText = FONT_PREVIEW_SENTENCE,
                            previewFontFamily = family ?: FontFamily.Default,
                            isLoading = family == null,
                            selected = font.id == selectedFontId,
                            onClick = { onSelect(font.id) },
                        )
                    }
                }
            }
        }
    }
}
