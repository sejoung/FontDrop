package io.github.sejoung.fontdrop.ui.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import io.github.sejoung.fontdrop.data.font.FontFolderRepository
import io.github.sejoung.fontdrop.data.font.FontPrewarmer
import io.github.sejoung.fontdrop.data.note.Note
import io.github.sejoung.fontdrop.data.note.NoteRepository
import io.github.sejoung.fontdrop.data.share.NoteImageRenderer
import io.github.sejoung.fontdrop.util.Clock
import io.github.sejoung.fontdrop.util.SystemClock
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class EditorViewModel(
    private val noteId: Long,
    private val noteRepository: NoteRepository,
    private val fontRepository: FontFolderRepository,
    private val prewarmer: FontPrewarmer,
    private val imageRenderer: NoteImageRenderer,
    private val clock: Clock = SystemClock,
    private val autoSaveDelayMs: Long = DEFAULT_AUTO_SAVE_DELAY_MS,
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditorUiState())
    val uiState: StateFlow<EditorUiState> = _uiState.asStateFlow()

    private val _deleteCompleted = MutableStateFlow(false)
    val deleteCompleted: StateFlow<Boolean> = _deleteCompleted.asStateFlow()

    private val shareChannel = Channel<File>(capacity = Channel.BUFFERED)
    val shareEvents: Flow<File> = shareChannel.receiveAsFlow()

    private var saveJob: Job? = null

    // Tracks whether the user actually edited anything since the last persist.
    // Without it, every requestFlush (DisposableEffect on screen exit, share
    // flow, etc.) would touch the DB and the repository stamps updatedAt=now
    // on every save, causing "edited time" to drift just by opening a note.
    private var hasPendingChanges: Boolean = false

    init {
        viewModelScope.launch {
            val note = noteRepository.observeNote(noteId).first()
            val fonts = runCatching { fontRepository.scan() }.getOrDefault(emptyList())
            if (note == null) {
                _uiState.update {
                    it.copy(isLoading = false, noteExists = false, availableFonts = fonts)
                }
                return@launch
            }
            _uiState.update {
                it.copy(
                    isLoading = false,
                    noteExists = true,
                    title = note.title,
                    content = note.content,
                    fontId = note.fontId,
                    fontSizeSp = note.fontSizeSp,
                    lineHeightMultiplier = note.lineHeightMultiplier,
                    availableFonts = fonts,
                    createdAt = note.createdAt,
                    updatedAt = note.updatedAt,
                )
            }
            val selected = fonts.firstOrNull { it.id == note.fontId }
            launch {
                if (selected != null) prewarmer.ensureLoaded(selected)
                prewarmer.prewarm(fonts)
            }
        }
    }

    fun onTitleChange(title: String) {
        _uiState.update { it.copy(title = title) }
        markDirtyAndScheduleSave()
    }

    fun onContentChange(content: String) {
        _uiState.update { it.copy(content = content) }
        markDirtyAndScheduleSave()
    }

    fun onFontPickerToggle(show: Boolean) {
        _uiState.update { it.copy(showFontPicker = show) }
    }

    fun onFontSelected(fontId: String?) {
        val current = _uiState.value
        _uiState.update { it.copy(fontId = fontId, showFontPicker = false) }
        val asset = _uiState.value.availableFonts.firstOrNull { it.id == fontId }
        if (asset != null) {
            viewModelScope.launch { prewarmer.ensureLoaded(asset) }
        }
        if (current.fontId != fontId) markDirtyAndScheduleSave()
    }

    fun onFontSizeDelta(delta: Int) {
        val before = _uiState.value.fontSizeSp
        _uiState.update { current ->
            val next = (current.fontSizeSp + delta)
                .coerceIn(EditorUiState.MIN_FONT_SIZE_SP, EditorUiState.MAX_FONT_SIZE_SP)
            if (next == current.fontSizeSp) current else current.copy(fontSizeSp = next)
        }
        if (_uiState.value.fontSizeSp != before) markDirtyAndScheduleSave()
    }

    fun onLineHeightCycle() {
        _uiState.update { current ->
            val presets = EditorUiState.LINE_HEIGHT_PRESETS
            val currentIndex = presets
                .indexOfFirst { kotlin.math.abs(it - current.lineHeightMultiplier) < 0.01f }
                .takeIf { it >= 0 } ?: 0
            val nextIndex = (currentIndex + 1) % presets.size
            current.copy(lineHeightMultiplier = presets[nextIndex])
        }
        markDirtyAndScheduleSave()
    }

    suspend fun flushPendingSave() {
        saveJob?.cancel()
        saveJob = null
        // NonCancellable so the save completes even when the caller (e.g. a
        // disposed UI coroutine during rotation) is being cancelled mid-flight.
        withContext(NonCancellable) { save() }
    }

    fun onDeleteNote() {
        viewModelScope.launch {
            saveJob?.cancel()
            saveJob = null
            // Switch to a loading screen briefly and mark the note as gone so any
            // in-flight requestFlush bails before the DB row disappears.
            _uiState.update { it.copy(isLoading = true, noteExists = false) }
            withContext(NonCancellable) { noteRepository.deleteNote(noteId) }
            _deleteCompleted.value = true
        }
    }

    fun onDeleteEventConsumed() {
        _deleteCompleted.value = false
    }

    fun onShareNote() {
        viewModelScope.launch {
            // Persist the latest draft first so the rendered image matches the
            // user's current content exactly (no race against autosave debounce).
            flushPendingSave()
            val state = _uiState.value
            if (!state.noteExists) return@launch
            val file = imageRenderer.render(
                title = state.title,
                content = state.content,
                fontSizeSp = state.fontSizeSp,
                lineHeightMultiplier = state.lineHeightMultiplier,
                asset = state.selectedFont,
            ) ?: return@launch
            shareChannel.send(file)
        }
    }

    /** Fire-and-forget flush safe to call from DisposableEffect.onDispose.  */
    fun requestFlush() {
        viewModelScope.launch { flushPendingSave() }
    }

    private fun markDirtyAndScheduleSave() {
        hasPendingChanges = true
        scheduleSave()
    }

    private fun scheduleSave() {
        if (!_uiState.value.noteExists) return
        saveJob?.cancel()
        saveJob = viewModelScope.launch {
            delay(autoSaveDelayMs)
            save()
        }
    }

    private suspend fun save() {
        if (!hasPendingChanges) return
        val state = _uiState.value
        if (!state.noteExists) return
        val note = Note(
            id = noteId,
            title = state.title,
            content = state.content,
            fontId = state.fontId,
            fontSizeSp = state.fontSizeSp,
            lineHeightMultiplier = state.lineHeightMultiplier,
            createdAt = state.createdAt,
            updatedAt = state.updatedAt,
        )
        noteRepository.saveNote(note)
        hasPendingChanges = false
    }

    override fun onCleared() {
        shareChannel.close()
        super.onCleared()
    }

    companion object {
        const val DEFAULT_AUTO_SAVE_DELAY_MS: Long = 500L

        fun factory(
            noteId: Long,
            noteRepository: NoteRepository,
            fontRepository: FontFolderRepository,
            prewarmer: FontPrewarmer,
            imageRenderer: NoteImageRenderer,
            clock: Clock = SystemClock,
            autoSaveDelayMs: Long = DEFAULT_AUTO_SAVE_DELAY_MS,
        ) = viewModelFactory {
            initializer {
                EditorViewModel(
                    noteId, noteRepository, fontRepository, prewarmer,
                    imageRenderer, clock, autoSaveDelayMs,
                )
            }
        }
    }
}
