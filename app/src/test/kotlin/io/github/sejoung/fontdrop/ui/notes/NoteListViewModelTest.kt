package io.github.sejoung.fontdrop.ui.notes

import io.github.sejoung.fontdrop.data.font.FakeFontPrewarmer
import io.github.sejoung.fontdrop.data.font.FontAsset
import io.github.sejoung.fontdrop.data.font.FontFolderRepository
import io.github.sejoung.fontdrop.data.note.Note
import io.github.sejoung.fontdrop.ui.library.MainDispatcherRule
import io.github.sejoung.fontdrop.util.FakeClock
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
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
        val vm = buildViewModel(repo, now = now)

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
        val vm = buildViewModel(repo)

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
        val vm = buildViewModel(repo)

        assertEquals("Hello there", vm.uiState.value.items.single().snippet)
    }

    @Test
    fun `snippet truncates long content and appends ellipsis`() = runTest {
        val longLine = "x".repeat(200)
        val repo = FakeNoteRepository(
            initial = listOf(Note(id = 1, title = "L", content = longLine, updatedAt = 1L)),
        )
        val vm = buildViewModel(repo)

        val snippet = vm.uiState.value.items.single().snippet
        assertEquals(NoteListViewModel.MAX_SNIPPET_LENGTH + 1, snippet.length)
        assertTrue(snippet.endsWith("…"))
    }

    @Test
    fun `empty content note falls back to placeholder snippet`() = runTest {
        val repo = FakeNoteRepository(
            initial = listOf(Note(id = 1, title = "Empty", content = "", updatedAt = 1L)),
        )
        val vm = buildViewModel(repo)

        assertEquals(NoteListViewModel.EMPTY_SNIPPET, vm.uiState.value.items.single().snippet)
    }

    @Test
    fun `onCreateNote creates entity and emits id via event`() = runTest {
        val repo = FakeNoteRepository()
        val vm = buildViewModel(repo)

        vm.onCreateNote()

        val emittedId = vm.newNoteEvents.value
        assertNotEquals(null, emittedId)
        assertEquals(repo.createdNoteId, emittedId)

        vm.onNewNoteEventConsumed()
        assertNull(vm.newNoteEvents.value)
    }

    @Test
    fun `list item carries the font asset associated with the note`() = runTest {
        val inter = asset("inter")
        val roboto = asset("roboto")
        val repo = FakeNoteRepository(
            initial = listOf(
                Note(id = 1, title = "Styled", content = "hi", fontId = "inter", updatedAt = 1L),
                Note(id = 2, title = "Plain", content = "hi", fontId = null, updatedAt = 2L),
            ),
        )
        val vm = buildViewModel(repo, fonts = listOf(inter, roboto))

        val byTitle = vm.uiState.value.items.associateBy { it.title }
        assertEquals(inter, byTitle["Styled"]?.fontAsset)
        assertNull(byTitle["Plain"]?.fontAsset)
    }

    @Test
    fun `font asset is null when the stored fontId no longer exists in the folder`() = runTest {
        val repo = FakeNoteRepository(
            initial = listOf(Note(id = 1, title = "Orphan", fontId = "gone", updatedAt = 1L)),
        )
        val vm = buildViewModel(repo, fonts = emptyList())

        assertNull(vm.uiState.value.items.single().fontAsset)
    }

    @Test
    fun `onCreateNote seeds the new note with the persisted default font`() = runTest {
        val inter = asset("inter")
        val repo = FakeNoteRepository()
        val fontRepo = StubFontFolderRepository(fonts = listOf(inter), defaultFontId = "inter")
        val vm = NoteListViewModel(
            repository = repo,
            fontRepository = fontRepo,
            prewarmer = FakeFontPrewarmer(),
            clock = FakeClock(100L),
            zone = ZoneOffset.UTC,
        )

        vm.onCreateNote()

        val created = repo.noteById(repo.createdNoteId)
        assertEquals("inter", created?.fontId)
    }

    @Test
    fun `onCreateNote leaves fontId null when no default is set`() = runTest {
        val repo = FakeNoteRepository()
        val vm = buildViewModel(repo)

        vm.onCreateNote()

        assertNull(repo.noteById(repo.createdNoteId)?.fontId)
    }

    @Test
    fun `init prewarms the font cache so cards render with the right family immediately`() = runTest {
        val inter = asset("inter")
        val roboto = asset("roboto")
        val prewarmer = FakeFontPrewarmer()
        val repo = FakeNoteRepository()

        buildViewModel(repo, fonts = listOf(inter, roboto), prewarmer = prewarmer)

        assertEquals(listOf(listOf("inter", "roboto")), prewarmer.prewarmed)
    }

    private fun buildViewModel(
        noteRepo: FakeNoteRepository,
        fonts: List<FontAsset> = emptyList(),
        prewarmer: FakeFontPrewarmer = FakeFontPrewarmer(),
        now: Long = 100L,
    ) = NoteListViewModel(
        repository = noteRepo,
        fontRepository = StubFontFolderRepository(fonts),
        prewarmer = prewarmer,
        clock = FakeClock(now),
        zone = ZoneOffset.UTC,
    )

    private fun asset(id: String) = FontAsset(
        id = id,
        uriString = "content://$id",
        displayName = "$id.ttf",
        familyName = id,
        extension = "ttf",
        sizeBytes = 0,
    )

    private fun millisOf(year: Int, month: Int, day: Int, hour: Int, minute: Int): Long =
        LocalDateTime.of(year, month, day, hour, minute).toInstant(ZoneOffset.UTC).toEpochMilli()
}

private class StubFontFolderRepository(
    private val fonts: List<FontAsset>,
    defaultFontId: String? = null,
) : FontFolderRepository {
    private val folder = MutableStateFlow<String?>(null)
    override val selectedFolderUri: Flow<String?> = folder
    private val default = MutableStateFlow(defaultFontId)
    override val defaultFontId: Flow<String?> = default
    override suspend fun setSelectedFolder(uriString: String) { folder.value = uriString }
    override suspend fun clearSelectedFolder() { folder.value = null }
    override suspend fun setDefaultFontId(id: String?) { default.value = id }
    override suspend fun scan(): List<FontAsset> = fonts
}
