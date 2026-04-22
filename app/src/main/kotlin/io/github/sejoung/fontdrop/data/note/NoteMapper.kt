package io.github.sejoung.fontdrop.data.note

internal fun NoteEntity.toDomain(): Note = Note(
    id = id,
    title = title,
    content = content,
    fontId = fontId,
    fontSizeSp = fontSizeSp,
    lineHeightMultiplier = lineHeightMultiplier,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

internal fun Note.toEntity(): NoteEntity = NoteEntity(
    id = id,
    title = title,
    content = content,
    fontId = fontId,
    fontSizeSp = fontSizeSp,
    lineHeightMultiplier = lineHeightMultiplier,
    createdAt = createdAt,
    updatedAt = updatedAt,
)
