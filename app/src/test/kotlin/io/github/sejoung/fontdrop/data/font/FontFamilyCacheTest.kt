package io.github.sejoung.fontdrop.data.font

import androidx.compose.ui.text.font.FontFamily
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Before
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.File
import java.io.InputStream
import java.nio.file.Files

class FontFamilyCacheTest {

    private lateinit var cacheDir: File
    private lateinit var reader: FakeReader
    private lateinit var materializer: FontFileMaterializer
    private var factoryCalls = 0

    @Before
    fun setUp() {
        cacheDir = Files.createTempDirectory("fdcache").toFile()
        reader = FakeReader()
        materializer = FontFileMaterializer(reader = reader, cacheDirProvider = { cacheDir })
        factoryCalls = 0
    }

    @After
    fun tearDown() {
        cacheDir.deleteRecursively()
    }

    @Test
    fun `cachedOrNull returns null before load`() {
        val cache = buildCache()
        assertNull(cache.cachedOrNull("anything"))
    }

    @Test
    fun `familyFor materializes and caches the result`() = runTest {
        reader.put("content://a", byteArrayOf(1))
        val cache = buildCache()
        val asset = asset("a")

        val family = cache.familyFor(asset)

        assertNotNull(family)
        assertSame(family, cache.cachedOrNull("a"))
        assertEquals(1, factoryCalls)
    }

    @Test
    fun `repeated familyFor does not reinvoke factory`() = runTest {
        reader.put("content://a", byteArrayOf(1))
        val cache = buildCache()
        val asset = asset("a")

        val first = cache.familyFor(asset)
        val second = cache.familyFor(asset)

        assertSame(first, second)
        assertEquals(1, factoryCalls)
    }

    @Test
    fun `prewarm loads every asset exactly once`() = runTest {
        reader.put("content://a", byteArrayOf(1))
        reader.put("content://b", byteArrayOf(2))
        val cache = buildCache()

        cache.prewarm(listOf(asset("a"), asset("b"), asset("a")))

        assertNotNull(cache.cachedOrNull("a"))
        assertNotNull(cache.cachedOrNull("b"))
        assertEquals(2, factoryCalls)
    }

    @Test
    fun `prewarm skips entries whose source stream cannot be opened`() = runTest {
        reader.put("content://a", byteArrayOf(1))
        val cache = buildCache()

        cache.prewarm(listOf(asset("a"), asset("missing", uri = "content://none")))

        assertNotNull(cache.cachedOrNull("a"))
        assertNull(cache.cachedOrNull("missing"))
        assertEquals(1, factoryCalls)
    }

    @Test
    fun `prewarm caps the number of assets it eagerly loads`() = runTest {
        val total = FontFamilyCache.PREWARM_CAP + 5
        val assets = (0 until total).map { asset("asset-$it") }
        assets.forEach { reader.put(it.uriString, byteArrayOf(1)) }
        val cache = buildCache()

        cache.prewarm(assets)

        assertEquals(FontFamilyCache.PREWARM_CAP, factoryCalls)
        assertNotNull(cache.cachedOrNull("asset-0"))
        assertNull(cache.cachedOrNull("asset-${total - 1}"))
    }

    @Test
    fun `invalidate clears cache and materializer reloads from source`() = runTest {
        reader.put("content://a", byteArrayOf(1))
        val cache = buildCache()
        cache.familyFor(asset("a"))
        assertEquals(1, factoryCalls)
        assertEquals(1, reader.openCount)

        cache.invalidate()
        cache.familyFor(asset("a"))

        assertEquals(2, factoryCalls)
        assertEquals(2, reader.openCount)
    }

    private fun buildCache() = FontFamilyCache(
        materializer = materializer,
        familyFactory = {
            factoryCalls++
            FontFamily.Default
        },
    )

    private fun asset(id: String, uri: String = "content://$id") = FontAsset(
        id = id,
        uriString = uri,
        displayName = "$id.ttf",
        familyName = id,
        extension = "ttf",
        sizeBytes = 0,
    )

    private class FakeReader : FontContentReader {
        private val contents = mutableMapOf<String, ByteArray>()
        var openCount: Int = 0
            private set

        fun put(uri: String, bytes: ByteArray) {
            contents[uri] = bytes
        }

        override fun openInputStream(uriString: String): InputStream? {
            openCount += 1
            val bytes = contents[uriString] ?: return null
            return ByteArrayInputStream(bytes)
        }
    }
}
