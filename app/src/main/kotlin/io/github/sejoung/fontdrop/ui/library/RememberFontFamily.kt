package io.github.sejoung.fontdrop.ui.library

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.text.font.FontFamily
import io.github.sejoung.fontdrop.data.font.FontAsset
import io.github.sejoung.fontdrop.data.font.FontFamilyCache

@Composable
fun rememberFontFamily(
    asset: FontAsset,
    cache: FontFamilyCache,
): State<FontFamily?> {
    // Keyed on asset.id so switching assets produces a fresh state seeded from
    // the cache — avoids stale FontFamily values sticking around when the
    // caller rotates through different fonts at the same call site.
    val state = remember(asset.id) {
        mutableStateOf<FontFamily?>(cache.cachedOrNull(asset.id))
    }
    LaunchedEffect(asset.id) {
        if (state.value == null) {
            state.value = runCatching { cache.familyFor(asset) }.getOrNull()
        }
    }
    return state
}
