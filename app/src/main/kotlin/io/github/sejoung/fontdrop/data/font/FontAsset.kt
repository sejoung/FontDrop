package io.github.sejoung.fontdrop.data.font

data class FontAsset(
    val id: String,
    val uriString: String,
    val displayName: String,
    val familyName: String,
    val extension: String,
    val sizeBytes: Long,
)
