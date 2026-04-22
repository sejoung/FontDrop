package io.github.sejoung.fontdrop.ui.library

import io.github.sejoung.fontdrop.data.font.FontAsset

data class FontLibraryUiState(
    val hasSelectedFolder: Boolean = false,
    val folderUri: String? = null,
    val isLoading: Boolean = false,
    val fonts: List<FontAsset> = emptyList(),
    val selectedFontId: String? = null,
    val errorMessage: String? = null,
)
