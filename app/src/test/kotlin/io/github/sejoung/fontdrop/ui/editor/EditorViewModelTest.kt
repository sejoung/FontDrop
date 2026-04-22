package io.github.sejoung.fontdrop.ui.editor

import io.github.sejoung.fontdrop.data.font.FakeFontPrewarmer
import io.github.sejoung.fontdrop.data.font.FontAsset
import io.github.sejoung.fontdrop.data.font.FontFolderRepository
import io.github.sejoung.fontdrop.data.note.Note
import io.github.sejoung.fontdrop.ui.library.MainDispatcherRule
import io.github.sejoung.fontdrop.ui.notes.FakeNoteRepository
import io.github.sejoung.fontdrop.util.FakeClock
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EditorViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(dispatcher = StandardTestDispatcher())

    @Test
    fun `init loads note fields and available fonts`() = runTest {
        val note = Note(id = 1, title = "T", content = "Hello", fontId = "inter", updatedAt = 100L)
        val notes = FakeNoteRepository(listOf(note))
        val fonts = FakeFontFolderRepository(fonts = listOf(asset("inter"), asset("roboto")))

        val prewarmer = FakeFontPrewarmer()
        val vm = EditorViewModel(
            noteId = 1,
            noteRepository = notes,
            fontRepository = fonts,
            prewarmer = prewarmer,
            clock = FakeClock(1000L),
            autoSaveDelayMs = 0L,
        )
        advanceUntilIdle()

        val state = vm.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.noteExists)
        assertEquals("T", state.title)
        assertEquals("Hello", state.content)
        assertEquals("inter", state.fontId)
        assertEquals(setOf("inter", "roboto"), state.availableFonts.map { it.id }.toSet())
    }

    @Test
    fun `missing note marks noteExists false and skips save`() = runTest {
        val notes = FakeNoteRepository()
        val vm = EditorViewModel(
            noteId = 42,
            noteRepository = notes,
            fontRepository = FakeFontFolderRepository(),
            prewarmer = FakeFontPrewarmer(),
            clock = FakeClock(0L),
            autoSaveDelayMs = 0L,
        )
        advanceUntilIdle()

        vm.onTitleChange("should not save")
        advanceUntilIdle()

        val state = vm.uiState.value
        assertFalse(state.noteExists)
        assertNull(notes.noteById(42))
    }

    @Test
    fun `title edits debounce before saving`() = runTest {
        val notes = FakeNoteRepository(listOf(Note(id = 1, title = "", content = "")))
        val vm = EditorViewModel(
            noteId = 1,
            noteRepository = notes,
            fontRepository = FakeFontFolderRepository(),
            prewarmer = FakeFontPrewarmer(),
            clock = FakeClock(0L),
            autoSaveDelayMs = 500L,
        )
        advanceUntilIdle()

        vm.onTitleChange("a")
        advanceTimeBy(100)
        vm.onTitleChange("ab")
        advanceTimeBy(100)
        vm.onTitleChange("abc")

        advanceTimeBy(300)
        runCurrent()
        assertEquals("", notes.noteById(1)?.title)

        advanceTimeBy(250)
        runCurrent()
        assertEquals("abc", notes.noteById(1)?.title)
    }

    @Test
    fun `font size stays within bounds`() = runTest {
        val notes = FakeNoteRepository(listOf(Note(id = 1, fontSizeSp = Note.DEFAULT_FONT_SIZE_SP)))
        val vm = EditorViewModel(
            noteId = 1,
            noteRepository = notes,
            fontRepository = FakeFontFolderRepository(),
            prewarmer = FakeFontPrewarmer(),
            clock = FakeClock(0L),
            autoSaveDelayMs = 0L,
        )
        advanceUntilIdle()

        repeat(20) { vm.onFontSizeDelta(EditorUiState.FONT_SIZE_STEP_SP) }
        advanceUntilIdle()
        assertEquals(EditorUiState.MAX_FONT_SIZE_SP, vm.uiState.value.fontSizeSp)

        repeat(40) { vm.onFontSizeDelta(-EditorUiState.FONT_SIZE_STEP_SP) }
        advanceUntilIdle()
        assertEquals(EditorUiState.MIN_FONT_SIZE_SP, vm.uiState.value.fontSizeSp)
    }

    @Test
    fun `line height cycles through presets`() = runTest {
        val notes = FakeNoteRepository(
            listOf(
                Note(
                    id = 1,
                    lineHeightMultiplier = EditorUiState.LINE_HEIGHT_PRESETS.first(),
                )
            )
        )
        val vm = EditorViewModel(
            noteId = 1,
            noteRepository = notes,
            fontRepository = FakeFontFolderRepository(),
            prewarmer = FakeFontPrewarmer(),
            clock = FakeClock(0L),
            autoSaveDelayMs = 0L,
        )
        advanceUntilIdle()

        val emitted = mutableListOf(vm.uiState.value.lineHeightMultiplier)
        repeat(EditorUiState.LINE_HEIGHT_PRESETS.size) {
            vm.onLineHeightCycle()
            emitted += vm.uiState.value.lineHeightMultiplier
        }

        val expected = EditorUiState.LINE_HEIGHT_PRESETS + EditorUiState.LINE_HEIGHT_PRESETS.first()
        assertEquals(expected, emitted)
    }

    @Test
    fun `selecting a font persists and closes picker`() = runTest {
        val notes = FakeNoteRepository(listOf(Note(id = 1)))
        val vm = EditorViewModel(
            noteId = 1,
            noteRepository = notes,
            fontRepository = FakeFontFolderRepository(fonts = listOf(asset("inter"))),
            prewarmer = FakeFontPrewarmer(),
            clock = FakeClock(0L),
            autoSaveDelayMs = 0L,
        )
        advanceUntilIdle()

        vm.onFontPickerToggle(true)
        vm.onFontSelected("inter")
        advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals("inter", state.fontId)
        assertFalse(state.showFontPicker)
        assertEquals("inter", notes.noteById(1)?.fontId)
    }

    @Test
    fun `editor prewarms the current font first and then the rest`() = runTest {
        val inter = asset("inter")
        val roboto = asset("roboto")
        val note = Note(id = 1, fontId = "inter")
        val prewarmer = FakeFontPrewarmer()
        val vm = EditorViewModel(
            noteId = 1,
            noteRepository = FakeNoteRepository(listOf(note)),
            fontRepository = FakeFontFolderRepository(fonts = listOf(inter, roboto)),
            prewarmer = prewarmer,
            clock = FakeClock(0L),
            autoSaveDelayMs = 0L,
        )
        advanceUntilIdle()

        assertEquals(listOf("inter"), prewarmer.ensured)
        assertEquals(listOf(listOf("inter", "roboto")), prewarmer.prewarmed)
        // keep referenced to avoid unused-var warning
        @Suppress("UNUSED_VARIABLE") val unused = vm
    }

    @Test
    fun `selecting a font eagerly loads it so the change shows instantly`() = runTest {
        val inter = asset("inter")
        val prewarmer = FakeFontPrewarmer()
        val vm = EditorViewModel(
            noteId = 1,
            noteRepository = FakeNoteRepository(listOf(Note(id = 1))),
            fontRepository = FakeFontFolderRepository(fonts = listOf(inter)),
            prewarmer = prewarmer,
            clock = FakeClock(0L),
            autoSaveDelayMs = 0L,
        )
        advanceUntilIdle()
        prewarmer.ensured.clear()

        vm.onFontSelected("inter")
        advanceUntilIdle()

        assertEquals(listOf("inter"), prewarmer.ensured)
    }

    @Test
    fun `flushPendingSave persists latest draft without waiting for debounce`() = runTest {
        val notes = FakeNoteRepository(listOf(Note(id = 1, title = "")))
        val vm = EditorViewModel(
            noteId = 1,
            noteRepository = notes,
            fontRepository = FakeFontFolderRepository(),
            prewarmer = FakeFontPrewarmer(),
            clock = FakeClock(0L),
            autoSaveDelayMs = 5_000L,
        )
        advanceUntilIdle()

        vm.onTitleChange("immediate")
        vm.flushPendingSave()
        advanceUntilIdle()

        assertEquals("immediate", notes.noteById(1)?.title)
    }

    private fun asset(id: String) = FontAsset(
        id = id,
        uriString = "content://$id",
        displayName = "$id.ttf",
        familyName = id,
        extension = "ttf",
        sizeBytes = 0,
    )
}

private class FakeFontFolderRepository(
    private val fonts: List<FontAsset> = emptyList(),
) : FontFolderRepository {
    private val folder = MutableStateFlow<String?>(null)
    override val selectedFolderUri: Flow<String?> = folder
    override suspend fun setSelectedFolder(uriString: String) { folder.value = uriString }
    override suspend fun clearSelectedFolder() { folder.value = null }
    override suspend fun scan(): List<FontAsset> = fonts
}
