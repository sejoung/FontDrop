package io.github.sejoung.fontdrop.data.note

import io.github.sejoung.fontdrop.util.FakeClock
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class NoteRepositoryTest {

    private lateinit var dao: FakeNoteDao
    private lateinit var clock: FakeClock
    private lateinit var repository: NoteRepository

    @Before
    fun setUp() {
        dao = FakeNoteDao()
        clock = FakeClock(current = 1_000L)
        repository = NoteRepositoryImpl(dao = dao, clock = clock)
    }

    @Test
    fun `createEmptyNote returns id and persists blank note with current timestamps`() = runTest {
        val id = repository.createEmptyNote()

        val stored = dao.snapshot().single()
        assertEquals(id, stored.id)
        assertEquals("", stored.title)
        assertEquals("", stored.content)
        assertNull(stored.fontId)
        assertEquals(Note.DEFAULT_FONT_SIZE_SP, stored.fontSizeSp)
        assertEquals(Note.DEFAULT_LINE_HEIGHT_MULTIPLIER, stored.lineHeightMultiplier, 0.0001f)
        assertEquals(1_000L, stored.createdAt)
        assertEquals(1_000L, stored.updatedAt)
    }

    @Test
    fun `saveNote updates updatedAt but keeps original createdAt`() = runTest {
        val id = repository.createEmptyNote()
        val original = repository.observeNote(id).first()!!
        clock.advanceBy(500L)

        repository.saveNote(original.copy(title = "Hello", content = "World"))

        val updated = repository.observeNote(id).first()!!
        assertEquals("Hello", updated.title)
        assertEquals("World", updated.content)
        assertEquals(1_000L, updated.createdAt)
        assertEquals(1_500L, updated.updatedAt)
    }

    @Test
    fun `saveNote with id 0 assigns new id`() = runTest {
        val newId = repository.saveNote(Note(title = "Fresh"))

        assertNotEquals(0L, newId)
        val stored = dao.snapshot().single()
        assertEquals(newId, stored.id)
        assertEquals("Fresh", stored.title)
        assertEquals(1_000L, stored.createdAt)
        assertEquals(1_000L, stored.updatedAt)
    }

    @Test
    fun `saveNote preserves non zero createdAt passed in by caller`() = runTest {
        clock.advanceTo(5_000L)
        val note = Note(title = "Imported", createdAt = 42L)

        val id = repository.saveNote(note)
        val stored = dao.snapshot().first { it.id == id }

        assertEquals(42L, stored.createdAt)
        assertEquals(5_000L, stored.updatedAt)
    }

    @Test
    fun `observeNotes is sorted by updatedAt descending`() = runTest {
        clock.advanceTo(10L); val a = repository.saveNote(Note(title = "A"))
        clock.advanceTo(30L); val c = repository.saveNote(Note(title = "C"))
        clock.advanceTo(20L); val b = repository.saveNote(Note(title = "B"))

        val list = repository.observeNotes().first()
        assertEquals(listOf("C", "B", "A"), list.map { it.title })
        assertEquals(listOf(c, b, a), list.map { it.id })
    }

    @Test
    fun `observeNote emits null for unknown id`() = runTest {
        val note = repository.observeNote(999L).first()
        assertNull(note)
    }

    @Test
    fun `deleteNote removes entry`() = runTest {
        val id = repository.createEmptyNote()
        assertNotNull(repository.observeNote(id).first())

        repository.deleteNote(id)

        assertNull(repository.observeNote(id).first())
        assertTrue(dao.snapshot().isEmpty())
    }

    @Test
    fun `font settings round trip through save and observe`() = runTest {
        val id = repository.saveNote(
            Note(
                title = "Styled",
                content = "body",
                fontId = "inter-hash",
                fontSizeSp = 22,
                lineHeightMultiplier = 1.8f,
            )
        )

        val note = repository.observeNote(id).first()!!
        assertEquals("inter-hash", note.fontId)
        assertEquals(22, note.fontSizeSp)
        assertEquals(1.8f, note.lineHeightMultiplier, 0.0001f)
    }
}
