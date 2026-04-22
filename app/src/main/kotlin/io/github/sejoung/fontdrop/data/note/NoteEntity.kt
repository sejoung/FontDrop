package io.github.sejoung.fontdrop.data.note

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val title: String,
    val content: String,
    @ColumnInfo(name = "font_id") val fontId: String?,
    @ColumnInfo(name = "font_size_sp") val fontSizeSp: Int,
    @ColumnInfo(name = "line_height_multiplier") val lineHeightMultiplier: Float,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
)
