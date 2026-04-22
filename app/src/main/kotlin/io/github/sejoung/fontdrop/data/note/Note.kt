package io.github.sejoung.fontdrop.data.note

data class Note(
    val id: Long = 0L,
    val title: String = "",
    val content: String = "",
    val fontId: String? = null,
    val fontSizeSp: Int = DEFAULT_FONT_SIZE_SP,
    val lineHeightMultiplier: Float = DEFAULT_LINE_HEIGHT_MULTIPLIER,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
) {
    val isBlank: Boolean get() = title.isBlank() && content.isBlank()

    companion object {
        const val DEFAULT_FONT_SIZE_SP: Int = 16
        const val DEFAULT_LINE_HEIGHT_MULTIPLIER: Float = 1.4f
    }
}
