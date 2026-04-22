package io.github.sejoung.fontdrop.data.font

object FontScanner {
    private val SupportedExtensions = setOf("ttf", "otf")

    fun scan(entries: List<FontFileEntry>): List<FontAsset> {
        return entries
            .mapNotNull { entry -> entry.toAssetOrNull() }
            .sortedBy { it.familyName.lowercase() }
    }

    private fun FontFileEntry.toAssetOrNull(): FontAsset? {
        val ext = displayName.substringAfterLast('.', missingDelimiterValue = "").lowercase()
        if (ext !in SupportedExtensions) return null
        val family = displayName.substringBeforeLast('.', missingDelimiterValue = displayName)
        if (family.isBlank()) return null
        return FontAsset(
            id = stableIdFor(uriString),
            uriString = uriString,
            displayName = displayName,
            familyName = family,
            extension = ext,
            sizeBytes = sizeBytes,
        )
    }

    private fun stableIdFor(uriString: String): String {
        var hash = 1125899906842597L
        for (ch in uriString) {
            hash = 31 * hash + ch.code
        }
        return hash.toString(16)
    }
}
