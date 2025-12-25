package com.ck.quicknote.domain.usecase

import com.ck.quicknote.domain.model.Folder
import com.ck.quicknote.domain.repository.FolderRepository

class AddFolderUseCase(
    private val repository: FolderRepository
) {
    suspend operator fun invoke(folder: Folder) {
        if (folder.name.isBlank()) {
            throw Exception("Folder name cannot be empty")
        }
        repository.insertFolder(folder)
    }
}