package com.ck.quicknote.feature.note_detail

import androidx.compose.ui.graphics.Color
import com.ck.quicknote.domain.model.Folder
import com.ck.quicknote.domain.model.Note

data class NoteTextFieldState(
    val text: String = "",
    val hint: String = "",
    val isHintVisible: Boolean = true
)

data class NoteDetailState(
    val noteTitle: NoteTextFieldState = NoteTextFieldState(hint = "Enter title..."),
    val noteContent: NoteTextFieldState = NoteTextFieldState(hint = "Enter some content..."),
    val noteColor: Int = Note.noteColors.random().toArgb(),
    val noteImageUri: String? = null,
    val isPinned: Boolean = false,
    val isArchived: Boolean = false,

    // यह लाइन चेक करें:
    val reminderTime: Long? = null,

    // Folder Logic
    val folders: List<Folder> = emptyList(),
    val folderId: Long? = null
)

fun Int.toColor() = Color(this)
fun Color.toArgb() = this.value.toInt()