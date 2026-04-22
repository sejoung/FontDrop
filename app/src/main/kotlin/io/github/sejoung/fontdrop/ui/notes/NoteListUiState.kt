package io.github.sejoung.fontdrop.ui.notes

import io.github.sejoung.fontdrop.data.font.FontAsset

data class NoteListUiState(
    val isLoading: Boolean = true,
    val items: List<NoteListItem> = emptyList(),
)

data class NoteListItem(
    val id: Long,
    val title: String,
    val snippet: String,
    val editedLabel: String,
    val fontAsset: FontAsset? = null,
)
