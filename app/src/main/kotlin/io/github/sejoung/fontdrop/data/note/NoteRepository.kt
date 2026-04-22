package io.github.sejoung.fontdrop.data.note

import kotlinx.coroutines.flow.Flow

interface NoteRepository {
    fun observeNotes(): Flow<List<Note>>

    fun observeNote(id: Long): Flow<Note?>

    suspend fun createEmptyNote(fontId: String? = null): Long

    suspend fun saveNote(note: Note): Long

    suspend fun deleteNote(id: Long)
}
