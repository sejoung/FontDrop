package io.github.sejoung.fontdrop.ui.editor

import io.github.sejoung.fontdrop.data.font.FontAsset
import io.github.sejoung.fontdrop.data.note.Note

data class EditorUiState(
    val isLoading: Boolean = true,
    val noteExists: Boolean = false,
    val title: String = "",
    val content: String = "",
    val fontId: String? = null,
    val fontSizeSp: Int = Note.DEFAULT_FONT_SIZE_SP,
    val lineHeightMultiplier: Float = Note.DEFAULT_LINE_HEIGHT_MULTIPLIER,
    val availableFonts: List<FontAsset> = emptyList(),
    val showFontPicker: Boolean = false,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
) {
    val selectedFont: FontAsset? get() = availableFonts.firstOrNull { it.id == fontId }

    companion object {
        const val MIN_FONT_SIZE_SP: Int = 12
        const val MAX_FONT_SIZE_SP: Int = 40
        const val FONT_SIZE_STEP_SP: Int = 2
        val LINE_HEIGHT_PRESETS: List<Float> = listOf(1.2f, 1.4f, 1.8f)
    }
}
