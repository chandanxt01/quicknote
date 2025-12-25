package com.ck.quicknote.data.mapper

import com.ck.quicknote.data.local.entity.FolderEntity
import com.ck.quicknote.data.local.entity.NoteEntity
import com.ck.quicknote.domain.model.Folder
import com.ck.quicknote.domain.model.Note

fun NoteEntity.toNote(): Note {
    return Note(
        id = id,
        title = title,
        content = content,
        timestamp = timestamp,
        color = color,
        isPinned = isPinned,
        isArchived = isArchived,
        imageUri = imageUri,
        reminder = reminder,
        folderId = folderId
    )
}

fun Note.toNoteEntity(): NoteEntity {
    return NoteEntity(
        id = id,
        title = title,
        content = content,
        timestamp = timestamp,
        color = color,
        isPinned = isPinned,
        isArchived = isArchived,
        imageUri = imageUri,
        reminder = reminder,
        folderId = folderId
    )
}

// Folder Mappers (Required by Repository)
fun FolderEntity.toFolder(): Folder {
    return Folder(
        id = id,
        name = name,
        timestamp = timestamp
    )
}

fun Folder.toFolderEntity(): FolderEntity {
    return FolderEntity(
        id = id,
        name = name,
        timestamp = timestamp
    )
}