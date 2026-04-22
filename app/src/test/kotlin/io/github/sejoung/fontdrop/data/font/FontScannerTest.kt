package io.github.sejoung.fontdrop.data.font

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FontScannerTest {

    @Test
    fun `filters out files with unsupported extensions`() {
        val entries = listOf(
            entry("Inter.ttf"),
            entry("readme.txt"),
            entry("image.png"),
            entry("Roboto.otf"),
        )

        val result = FontScanner.scan(entries)

        assertEquals(listOf("Inter", "Roboto"), result.map { it.familyName })
    }

    @Test
    fun `accepts uppercase and mixed case extensions`() {
        val entries = listOf(
            entry("Display.TTF"),
            entry("Serif.Otf"),
        )

        val result = FontScanner.scan(entries)

        assertEquals(2, result.size)
        assertEquals(setOf("ttf", "otf"), result.map { it.extension }.toSet())
    }

    @Test
    fun `sorts fonts alphabetically by family name case insensitively`() {
        val entries = listOf(
            entry("Zilla Slab.ttf"),
            entry("arial.ttf"),
            entry("Inter.otf"),
        )

        val result = FontScanner.scan(entries)

        assertEquals(listOf("arial", "Inter", "Zilla Slab"), result.map { it.familyName })
    }

    @Test
    fun `derives family name from file name without extension`() {
        val entries = listOf(entry("Source Serif Pro.ttf"))

        val result = FontScanner.scan(entries)

        val asset = result.single()
        assertEquals("Source Serif Pro", asset.familyName)
        assertEquals("Source Serif Pro.ttf", asset.displayName)
        assertEquals("ttf", asset.extension)
    }

    @Test
    fun `rejects entries with no file name stem`() {
        val entries = listOf(
            entry(".ttf"),
            entry(".otf"),
        )

        val result = FontScanner.scan(entries)

        assertTrue(result.isEmpty())
    }

    @Test
    fun `assigns stable ids derived from uri`() {
        val sameUri = "content://tree/A/file/1"
        val first = FontScanner.scan(listOf(entry("A.ttf", uri = sameUri))).single()
        val second = FontScanner.scan(listOf(entry("A.ttf", uri = sameUri))).single()

        assertEquals(first.id, second.id)

        val differentUri = FontScanner
            .scan(listOf(entry("A.ttf", uri = "content://tree/A/file/2")))
            .single()
        assertNotEquals(first.id, differentUri.id)
    }

    @Test
    fun `preserves reported size bytes`() {
        val result = FontScanner.scan(listOf(entry("Big.ttf", size = 1_234_567)))
        assertEquals(1_234_567L, result.single().sizeBytes)
    }

    private fun entry(
        name: String,
        uri: String = "content://tree/file/$name",
        size: Long = 0,
    ) = FontFileEntry(uriString = uri, displayName = name, sizeBytes = size)
}
