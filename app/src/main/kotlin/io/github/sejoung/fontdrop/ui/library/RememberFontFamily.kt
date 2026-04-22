package io.github.sejoung.fontdrop.ui.library

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import io.github.sejoung.fontdrop.data.font.FontAsset
import io.github.sejoung.fontdrop.data.font.FontFileMaterializer

@Composable
fun rememberFontFamily(
    asset: FontAsset,
    materializer: FontFileMaterializer,
): State<FontFamily?> {
    return produceState<FontFamily?>(initialValue = null, asset.id) {
        value = runCatching {
            val file = materializer.materialize(asset) ?: return@runCatching null
            FontFamily(Font(file))
        }.getOrNull()
    }
}
