package io.github.sejoung.fontdrop.data.note

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class FakeNoteDao : NoteDao {

    private val notes = MutableStateFlow<List<NoteEntity>>(emptyList())
    private var nextId: Long = 1L

    fun snapshot(): List<NoteEntity> = notes.value

    override fun observeAll(): Flow<List<NoteEntity>> =
        notes.map { list -> list.sortedByDescending { it.updatedAt } }

    override fun observeById(id: Long): Flow<NoteEntity?> =
        notes.map { list -> list.firstOrNull { it.id == id } }

    override suspend fun upsert(entity: NoteEntity): Long {
        val saved = if (entity.id == 0L) entity.copy(id = nextId++) else entity
        notes.update { current ->
            current.filterNot { it.id == saved.id } + saved
        }
        return saved.id
    }

    override suspend fun delete(id: Long) {
        notes.update { current -> current.filterNot { it.id == id } }
    }
}
