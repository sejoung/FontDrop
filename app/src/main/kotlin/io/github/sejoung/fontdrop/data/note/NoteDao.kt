package io.github.sejoung.fontdrop.data.note

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {

    @Query("SELECT * FROM notes ORDER BY updated_at DESC")
    fun observeAll(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE id = :id LIMIT 1")
    fun observeById(id: Long): Flow<NoteEntity?>

    @Upsert
    suspend fun upsert(entity: NoteEntity): Long

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun delete(id: Long)
}
