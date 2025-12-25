package com.ck.quicknote.feature.home

import com.ck.quicknote.domain.model.Folder
import com.ck.quicknote.domain.model.Note
import com.ck.quicknote.domain.util.NoteOrder

sealed class HomeEvent {
    data class Order(val noteOrder: NoteOrder): HomeEvent()
    data class DeleteNote(val note: Note): HomeEvent()
    object RestoreNote: HomeEvent()
    object ToggleOrderSection: HomeEvent()
    object ToggleView: HomeEvent()

    // Folder Events
    data class SelectFolder(val folder: Folder): HomeEvent()
    data class CreateFolder(val name: String): HomeEvent()

    // New Events for Context Menu
    data class RenameFolder(val folder: Folder, val newName: String): HomeEvent()
    data class DeleteFolder(val folder: Folder): HomeEvent()
    data class PinFolder(val folder: Folder): HomeEvent()
}