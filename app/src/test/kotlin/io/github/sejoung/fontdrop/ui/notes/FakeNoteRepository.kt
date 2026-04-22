package io.github.sejoung.fontdrop.ui.notes

import io.github.sejoung.fontdrop.data.note.Note
import io.github.sejoung.fontdrop.data.note.NoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class FakeNoteRepository(
    initial: List<Note> = emptyList(),
) : NoteRepository {

    private val notes = MutableStateFlow(initial)
    private var nextId: Long = (initial.maxOfOrNull { it.id } ?: 0L) + 1

    val currentNotes: List<Note> get() = notes.value

    fun noteById(id: Long): Note? = notes.value.firstOrNull { it.id == id }

    var createdNoteId: Long = 0L
        private set

    var saveInvocations: Int = 0
        private set

    override fun observeNotes(): Flow<List<Note>> =
        notes.map { list -> list.sortedByDescending { it.updatedAt } }

    override fun observeNote(id: Long): Flow<Note?> =
        notes.map { list -> list.firstOrNull { it.id == id } }

    override suspend fun createEmptyNote(fontId: String?): Long {
        val id = nextId++
        createdNoteId = id
        notes.update { current ->
            current + Note(id = id, title = "", content = "", fontId = fontId)
        }
        return id
    }

    override suspend fun saveNote(note: Note): Long {
        saveInvocations += 1
        val assigned = if (note.id == 0L) nextId++ else note.id
        notes.update { current ->
            current.filterNot { it.id == assigned } + note.copy(id = assigned)
        }
        return assigned
    }

    override suspend fun deleteNote(id: Long) {
        notes.update { current -> current.filterNot { it.id == id } }
    }
}
