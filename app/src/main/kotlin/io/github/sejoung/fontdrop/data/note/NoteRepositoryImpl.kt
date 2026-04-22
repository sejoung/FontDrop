package io.github.sejoung.fontdrop.data.note

import io.github.sejoung.fontdrop.util.Clock
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class NoteRepositoryImpl(
    private val dao: NoteDao,
    private val clock: Clock,
) : NoteRepository {

    override fun observeNotes(): Flow<List<Note>> =
        dao.observeAll().map { list -> list.map(NoteEntity::toDomain) }

    override fun observeNote(id: Long): Flow<Note?> =
        dao.observeById(id).map { it?.toDomain() }

    override suspend fun createEmptyNote(fontId: String?): Long {
        val now = clock.nowMillis()
        return dao.upsert(
            NoteEntity(
                id = 0L,
                title = "",
                content = "",
                fontId = fontId,
                fontSizeSp = Note.DEFAULT_FONT_SIZE_SP,
                lineHeightMultiplier = Note.DEFAULT_LINE_HEIGHT_MULTIPLIER,
                createdAt = now,
                updatedAt = now,
            )
        )
    }

    override suspend fun saveNote(note: Note): Long {
        val now = clock.nowMillis()
        val entity = note
            .copy(
                createdAt = if (note.createdAt == 0L) now else note.createdAt,
                updatedAt = now,
            )
            .toEntity()
        return dao.upsert(entity)
    }

    override suspend fun deleteNote(id: Long) {
        dao.delete(id)
    }
}
