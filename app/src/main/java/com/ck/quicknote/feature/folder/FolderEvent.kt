package com.ck.quicknote.feature.folder

import com.ck.quicknote.domain.model.Folder

sealed class FolderEvent {
    data class CreateFolder(val name: String): FolderEvent()
    data class DeleteFolder(val folder: Folder): FolderEvent()
    data class RenameFolder(val folder: Folder, val newName: String): FolderEvent() // Added
}