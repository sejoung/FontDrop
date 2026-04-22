package io.github.sejoung.fontdrop.ui.notes

import io.github.sejoung.fontdrop.data.note.Note
import io.github.sejoung.fontdrop.ui.library.MainDispatcherRule
import io.github.sejoung.fontdrop.util.FakeClock
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.LocalDateTime
import java.time.ZoneOffset

@OptIn(ExperimentalCoroutinesApi::class)
class NoteListViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `emits items sorted by updatedAt desc with relative timestamps`() = runTest {
        val now = millisOf(2026, 4, 22, 12, 0)
        val earlier = millisOf(2026, 4, 22, 8, 0)
        val yesterday = millisOf(2026, 4, 21, 20, 0)
        val repo = FakeNoteRepository(
            initial = listOf(
                Note(id = 1, title = "Older", content = "body", updatedAt = yesterday),
                Note(id = 2, title = "Fresh", content = "line", updatedAt = earlier),
            ),
        )
        val vm = NoteListViewModel(repo, FakeClock(now), ZoneOffset.UTC)

        val state = vm.uiState.value
        assertFalse(state.isLoading)
        assertEquals(listOf("Fresh", "Older"), state.items.map { it.title })
        assertEquals("4h ago", state.items[0].editedLabel)
        assertEquals("Yesterday", state.items[1].editedLabel)
    }

    @Test
    fun `blank title falls back to Untitled`() = runTest {
        val repo = FakeNoteRepository(
            initial = listOf(
                Note(id = 1, title = "", content = "first line", updatedAt = 1L),
            ),
        )
        val vm = NoteListViewModel(repo, FakeClock(100L), ZoneOffset.UTC)

        assertEquals("Untitled", vm.uiState.value.items.single().title)
    }

    @Test
    fun `snippet uses first non blank content line trimmed`() = runTest {
        val repo = FakeNoteRepository(
            initial = listOf(
                Note(
                    id = 1,
                    title = "Draft",
                    content = "\n   \n  Hello there  \n second line\n",
                    updatedAt = 1L,
                ),
            ),
        )
        val vm = NoteListViewModel(repo, FakeClock(100L), ZoneOffset.UTC)

        assertEquals("Hello there", vm.uiState.value.items.single().snippet)
    }

    @Test
    fun `snippet truncates long content and appends ellipsis`() = runTest {
        val longLine = "x".repeat(200)
        val repo = FakeNoteRepository(
            initial = listOf(Note(id = 1, title = "L", content = longLine, updatedAt = 1L)),
        )
        val vm = NoteListViewModel(repo, FakeClock(100L), ZoneOffset.UTC)

        val snippet = vm.uiState.value.items.single().snippet
        assertEquals(NoteListViewModel.MAX_SNIPPET_LENGTH + 1, snippet.length)
        assertTrue(snippet.endsWith("…"))
    }

    @Test
    fun `empty content note falls back to placeholder snippet`() = runTest {
        val repo = FakeNoteRepository(
            initial = listOf(Note(id = 1, title = "Empty", content = "", updatedAt = 1L)),
        )
        val vm = NoteListViewModel(repo, FakeClock(100L), ZoneOffset.UTC)

        assertEquals(NoteListViewModel.EMPTY_SNIPPET, vm.uiState.value.items.single().snippet)
    }

    @Test
    fun `onCreateNote creates entity and emits id via event`() = runTest {
        val repo = FakeNoteRepository()
        val vm = NoteListViewModel(repo, FakeClock(100L), ZoneOffset.UTC)

        vm.onCreateNote()

        val emittedId = vm.newNoteEvents.value
        assertNotEquals(null, emittedId)
        assertEquals(repo.createdNoteId, emittedId)

        vm.onNewNoteEventConsumed()
        assertNull(vm.newNoteEvents.value)
    }

    private fun millisOf(year: Int, month: Int, day: Int, hour: Int, minute: Int): Long =
        LocalDateTime.of(year, month, day, hour, minute).toInstant(ZoneOffset.UTC).toEpochMilli()
}
