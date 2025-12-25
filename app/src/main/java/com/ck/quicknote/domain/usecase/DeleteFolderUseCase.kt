package com.ck.quicknote.domain.usecase

import com.ck.quicknote.domain.model.Folder
import com.ck.quicknote.domain.repository.FolderRepository

class DeleteFolderUseCase(
    private val repository: FolderRepository
) {
    suspend operator fun invoke(folder: Folder) {
        // System folders (id < 0) cannot be deleted
        if ((folder.id ?: 0) < 0) return
        repository.deleteFolder(folder)
    }
}