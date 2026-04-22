package io.github.sejoung.fontdrop.ui.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import io.github.sejoung.fontdrop.data.font.FontFolderRepository
import io.github.sejoung.fontdrop.data.note.Note
import io.github.sejoung.fontdrop.data.note.NoteRepository
import io.github.sejoung.fontdrop.util.Clock
import io.github.sejoung.fontdrop.util.SystemClock
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class EditorViewModel(
    private val noteId: Long,
    private val noteRepository: NoteRepository,
    private val fontRepository: FontFolderRepository,
    private val clock: Clock = SystemClock,
    private val autoSaveDelayMs: Long = DEFAULT_AUTO_SAVE_DELAY_MS,
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditorUiState())
    val uiState: StateFlow<EditorUiState> = _uiState.asStateFlow()

    private var saveJob: Job? = null

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
        }
    }

    fun onTitleChange(title: String) {
        _uiState.update { it.copy(title = title) }
        scheduleSave()
    }

    fun onContentChange(content: String) {
        _uiState.update { it.copy(content = content) }
        scheduleSave()
    }

    fun onFontPickerToggle(show: Boolean) {
        _uiState.update { it.copy(showFontPicker = show) }
    }

    fun onFontSelected(fontId: String?) {
        _uiState.update { it.copy(fontId = fontId, showFontPicker = false) }
        scheduleSave()
    }

    fun onFontSizeDelta(delta: Int) {
        _uiState.update { current ->
            val next = (current.fontSizeSp + delta)
                .coerceIn(EditorUiState.MIN_FONT_SIZE_SP, EditorUiState.MAX_FONT_SIZE_SP)
            if (next == current.fontSizeSp) current else current.copy(fontSizeSp = next)
        }
        scheduleSave()
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
        scheduleSave()
    }

    suspend fun flushPendingSave() {
        saveJob?.cancel()
        saveJob = null
        save()
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
    }

    companion object {
        const val DEFAULT_AUTO_SAVE_DELAY_MS: Long = 500L

        fun factory(
            noteId: Long,
            noteRepository: NoteRepository,
            fontRepository: FontFolderRepository,
            clock: Clock = SystemClock,
            autoSaveDelayMs: Long = DEFAULT_AUTO_SAVE_DELAY_MS,
        ) = viewModelFactory {
            initializer {
                EditorViewModel(noteId, noteRepository, fontRepository, clock, autoSaveDelayMs)
            }
        }
    }
}
