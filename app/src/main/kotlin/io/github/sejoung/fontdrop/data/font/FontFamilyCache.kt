package io.github.sejoung.fontdrop.data.font

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.File
import java.util.concurrent.ConcurrentHashMap

class FontFamilyCache(
    private val materializer: FontFileMaterializer,
    private val familyFactory: (File) -> FontFamily = { file -> FontFamily(Font(file)) },
) : FontPrewarmer {

    private val cache = ConcurrentHashMap<String, FontFamily>()
    private val locks = ConcurrentHashMap<String, Mutex>()

    fun cachedOrNull(assetId: String): FontFamily? = cache[assetId]

    suspend fun familyFor(asset: FontAsset): FontFamily? {
        cache[asset.id]?.let { return it }
        val mutex = locks.getOrPut(asset.id) { Mutex() }
        return mutex.withLock {
            cache[asset.id]?.let { return@withLock it }
            val file = materializer.materialize(asset) ?: return@withLock null
            val family = familyFactory(file)
            cache[asset.id] = family
            family
        }
    }

    override suspend fun ensureLoaded(asset: FontAsset) {
        runCatching { familyFor(asset) }
    }

    override suspend fun prewarm(assets: Collection<FontAsset>) {
        // Cap to avoid pegging disk/IO on folders with hundreds of fonts. Any
        // asset beyond the cap still loads on-demand via ensureLoaded / familyFor.
        assets.asSequence().take(PREWARM_CAP).forEach {
            runCatching { familyFor(it) }
        }
    }

    fun invalidate() {
        cache.clear()
        locks.clear()
        materializer.invalidate()
    }

    companion object {
        const val PREWARM_CAP: Int = 50
    }
}
