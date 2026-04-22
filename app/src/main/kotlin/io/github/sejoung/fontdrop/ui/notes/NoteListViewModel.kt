package io.github.sejoung.fontdrop.ui.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import io.github.sejoung.fontdrop.data.font.FontAsset
import io.github.sejoung.fontdrop.data.font.FontFolderRepository
import io.github.sejoung.fontdrop.data.font.FontPrewarmer
import io.github.sejoung.fontdrop.data.note.Note
import io.github.sejoung.fontdrop.data.note.NoteRepository
import io.github.sejoung.fontdrop.ui.util.RelativeTime
import io.github.sejoung.fontdrop.util.Clock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.ZoneId

class NoteListViewModel(
    private val repository: NoteRepository,
    private val fontRepository: FontFolderRepository,
    private val prewarmer: FontPrewarmer,
    private val clock: Clock,
    private val zone: ZoneId = ZoneId.systemDefault(),
) : ViewModel() {

    private val _uiState = MutableStateFlow(NoteListUiState())
    val uiState: StateFlow<NoteListUiState> = _uiState.asStateFlow()

    private val _newNoteEvents = MutableStateFlow<Long?>(null)
    val newNoteEvents: StateFlow<Long?> = _newNoteEvents.asStateFlow()

    init {
        viewModelScope.launch {
            val fonts = runCatching { fontRepository.scan() }.getOrDefault(emptyList())
            val fontsById = fonts.associateBy { it.id }
            launch { prewarmer.prewarm(fonts) }
            repository.observeNotes().collect { notes ->
                val now = clock.nowMillis()
                _uiState.update {
                    NoteListUiState(
                        isLoading = false,
                        items = notes.map { it.toListItem(now, fontsById) },
                    )
                }
            }
        }
    }

    fun onCreateNote() {
        viewModelScope.launch {
            val defaultFontId = fontRepository.defaultFontId.first()
            val id = repository.createEmptyNote(fontId = defaultFontId)
            _newNoteEvents.value = id
        }
    }

    fun onNewNoteEventConsumed() {
        _newNoteEvents.value = null
    }

    private fun Note.toListItem(now: Long, fontsById: Map<String, FontAsset>): NoteListItem =
        NoteListItem(
            id = id,
            title = title.ifBlank { UNTITLED_LABEL },
            snippet = content.buildSnippet(),
            editedLabel = RelativeTime.format(now, updatedAt, zone),
            fontAsset = fontId?.let { fontsById[it] },
        )

    private fun String.buildSnippet(): String {
        val firstLine = lineSequence().firstOrNull { it.isNotBlank() }?.trim().orEmpty()
        return when {
            firstLine.isEmpty() -> EMPTY_SNIPPET
            firstLine.length <= MAX_SNIPPET_LENGTH -> firstLine
            else -> firstLine.take(MAX_SNIPPET_LENGTH).trimEnd() + "…"
        }
    }

    companion object {
        const val UNTITLED_LABEL = "Untitled"
        const val EMPTY_SNIPPET = "Start writing…"
        const val MAX_SNIPPET_LENGTH = 120

        fun factory(
            repository: NoteRepository,
            fontRepository: FontFolderRepository,
            prewarmer: FontPrewarmer,
            clock: Clock,
            zone: ZoneId = ZoneId.systemDefault(),
        ) = viewModelFactory {
            initializer { NoteListViewModel(repository, fontRepository, prewarmer, clock, zone) }
        }
    }
}
