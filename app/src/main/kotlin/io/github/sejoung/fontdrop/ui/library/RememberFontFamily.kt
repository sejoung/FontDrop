package io.github.sejoung.fontdrop.ui.library

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import androidx.compose.ui.text.font.FontFamily
import io.github.sejoung.fontdrop.data.font.FontAsset
import io.github.sejoung.fontdrop.data.font.FontFamilyCache

@Composable
fun rememberFontFamily(
    asset: FontAsset,
    cache: FontFamilyCache,
): State<FontFamily?> {
    val seeded = cache.cachedOrNull(asset.id)
    return produceState<FontFamily?>(initialValue = seeded, key1 = asset.id) {
        if (value == null) {
            value = runCatching { cache.familyFor(asset) }.getOrNull()
        }
    }
}
