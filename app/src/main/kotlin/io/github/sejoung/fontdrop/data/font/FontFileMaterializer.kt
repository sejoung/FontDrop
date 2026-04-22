package io.github.sejoung.fontdrop.data.font

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.ConcurrentHashMap

class FontFileMaterializer(
    private val reader: FontContentReader,
    private val cacheDirProvider: () -> File,
) {
    private val cache = ConcurrentHashMap<String, File>()
    private val locks = ConcurrentHashMap<String, Mutex>()

    suspend fun materialize(asset: FontAsset): File? = withContext(Dispatchers.IO) {
        val mutex = locks.getOrPut(asset.id) { Mutex() }
        mutex.withLock {
            cache[asset.id]?.takeIf { it.exists() }?.let { return@withLock it }
            val fontsDir = File(cacheDirProvider(), FontsSubdir).apply { mkdirs() }
            val target = File(fontsDir, "${asset.id}.${asset.extension.ifEmpty { "font" }}")
            if (target.exists() && asset.sizeBytes > 0 && target.length() == asset.sizeBytes) {
                cache[asset.id] = target
                return@withLock target
            }
            val copied = runCatching {
                reader.openInputStream(asset.uriString)?.use { input ->
                    target.outputStream().use { output -> input.copyTo(output) }
                    true
                } ?: false
            }.getOrElse {
                target.delete()
                return@withLock null
            }
            if (!copied) return@withLock null
            cache[asset.id] = target
            target
        }
    }

    fun invalidate() {
        cache.clear()
    }

    private companion object {
        const val FontsSubdir = "fonts"
    }
}
