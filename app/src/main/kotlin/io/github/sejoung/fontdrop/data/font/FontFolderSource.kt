package io.github.sejoung.fontdrop.data.font

interface FontFolderSource {
    suspend fun listFontFiles(): List<FontFileEntry>
}
