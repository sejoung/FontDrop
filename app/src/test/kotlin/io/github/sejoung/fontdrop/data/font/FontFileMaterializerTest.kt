package io.github.sejoung.fontdrop.data.font

import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.nio.file.Files

class FontFileMaterializerTest {

    private lateinit var cacheDir: File
    private lateinit var reader: RecordingReader
    private lateinit var materializer: FontFileMaterializer

    @Before
    fun setUp() {
        cacheDir = Files.createTempDirectory("fontdrop-mat").toFile()
        reader = RecordingReader()
        materializer = FontFileMaterializer(reader = reader, cacheDirProvider = { cacheDir })
    }

    @After
    fun tearDown() {
        cacheDir.deleteRecursively()
    }

    @Test
    fun `writes bytes to cache fonts directory with id and extension`() = runTest {
        val bytes = byteArrayOf(1, 2, 3, 4, 5)
        reader.put("content://tree/A", bytes)

        val file = materializer.materialize(asset(id = "abc", ext = "ttf", uri = "content://tree/A"))

        requireNotNull(file)
        assertEquals("fonts", file.parentFile?.name)
        assertEquals("abc.ttf", file.name)
        assertArrayEquals(bytes, file.readBytes())
    }

    @Test
    fun `reuses cached file on repeated calls without re-reading source`() = runTest {
        reader.put("content://tree/A", byteArrayOf(9, 9, 9))
        val first = materializer.materialize(asset(id = "same", ext = "otf", uri = "content://tree/A"))
        val second = materializer.materialize(asset(id = "same", ext = "otf", uri = "content://tree/A"))

        assertEquals(first, second)
        assertEquals(1, reader.openCount)
    }

    @Test
    fun `recovers from deleted cache entry by re-reading source`() = runTest {
        reader.put("content://tree/A", byteArrayOf(1, 2))
        val first = requireNotNull(materializer.materialize(asset(id = "x", ext = "ttf", uri = "content://tree/A")))
        assertTrue(first.delete())
        materializer.invalidate()

        val second = materializer.materialize(asset(id = "x", ext = "ttf", uri = "content://tree/A"))

        assertNotNull(second)
        assertEquals(2, reader.openCount)
    }

    @Test
    fun `returns null when reader cannot open stream`() = runTest {
        val result = materializer.materialize(asset(id = "missing", uri = "content://tree/unknown"))
        assertNull(result)
    }

    @Test
    fun `returns null and cleans partial file when reader throws`() = runTest {
        reader.throwOnNext = IOException("boom")

        val result = materializer.materialize(asset(id = "bad", ext = "ttf", uri = "content://tree/A"))

        assertNull(result)
        val fontsDir = File(cacheDir, "fonts")
        val partial = File(fontsDir, "bad.ttf")
        assertTrue("partial file should not exist", !partial.exists())
    }

    @Test
    fun `falls back to default extension when asset extension is blank`() = runTest {
        reader.put("content://tree/A", byteArrayOf(0))
        val file = requireNotNull(materializer.materialize(asset(id = "noext", ext = "", uri = "content://tree/A")))
        assertEquals("noext.font", file.name)
    }

    private fun asset(
        id: String,
        ext: String = "ttf",
        uri: String = "content://tree/$id",
    ) = FontAsset(
        id = id,
        uriString = uri,
        displayName = "$id.$ext",
        familyName = id,
        extension = ext,
        sizeBytes = 0,
    )

    private class RecordingReader : FontContentReader {
        private val contents = mutableMapOf<String, ByteArray>()
        var openCount: Int = 0
            private set
        var throwOnNext: IOException? = null

        fun put(uri: String, bytes: ByteArray) {
            contents[uri] = bytes
        }

        override fun openInputStream(uriString: String): InputStream? {
            openCount += 1
            throwOnNext?.let {
                throwOnNext = null
                throw it
            }
            val bytes = contents[uriString] ?: return null
            return ByteArrayInputStream(bytes)
        }
    }
}
