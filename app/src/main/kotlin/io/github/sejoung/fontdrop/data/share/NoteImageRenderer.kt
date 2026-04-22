package io.github.sejoung.fontdrop.data.share

import io.github.sejoung.fontdrop.data.font.FontAsset
import java.io.File

interface NoteImageRenderer {
    suspend fun render(
        title: String,
        content: String,
        fontSizeSp: Int,
        lineHeightMultiplier: Float,
        asset: FontAsset?,
    ): File?
}
