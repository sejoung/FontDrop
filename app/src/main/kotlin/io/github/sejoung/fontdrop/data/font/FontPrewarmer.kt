package io.github.sejoung.fontdrop.data.font

interface FontPrewarmer {
    suspend fun ensureLoaded(asset: FontAsset)
    suspend fun prewarm(assets: Collection<FontAsset>)
}
