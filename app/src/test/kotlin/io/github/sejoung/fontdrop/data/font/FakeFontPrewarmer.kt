package io.github.sejoung.fontdrop.data.font

class FakeFontPrewarmer : FontPrewarmer {
    val ensured = mutableListOf<String>()
    val prewarmed = mutableListOf<List<String>>()

    override suspend fun ensureLoaded(asset: FontAsset) {
        ensured += asset.id
    }

    override suspend fun prewarm(assets: Collection<FontAsset>) {
        prewarmed += assets.map { it.id }
    }
}
