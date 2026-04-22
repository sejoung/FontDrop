package io.github.sejoung.fontdrop.data.note

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [NoteEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class FontDropDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
}
