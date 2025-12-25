package com.ck.quicknote.feature.note_detail

import androidx.compose.ui.focus.FocusState

sealed class NoteDetailEvent {
    data class EnteredTitle(val value: String): NoteDetailEvent()
    data class ChangeTitleFocus(val focusState: FocusState): NoteDetailEvent()
    data class EnteredContent(val value: String): NoteDetailEvent()
    data class ChangeContentFocus(val focusState: FocusState): NoteDetailEvent()
    data class ChangeColor(val color: Int): NoteDetailEvent()
    data class ChangeImage(val imageUri: String?): NoteDetailEvent()

    // यह लाइन चेक करें, यह मिसिंग होने पर एरर आता है:
    data class SetReminder(val time: Long?): NoteDetailEvent()

    object TogglePin: NoteDetailEvent()
    object ToggleArchive: NoteDetailEvent()
    object SaveNote: NoteDetailEvent()
    object DeleteNote: NoteDetailEvent()

    data class ChangeFolder(val folderId: Long): NoteDetailEvent()
}