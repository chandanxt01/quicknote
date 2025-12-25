package com.ck.quicknote.domain.usecase

import com.ck.quicknote.domain.model.Folder
import com.ck.quicknote.domain.repository.FolderRepository
import kotlinx.coroutines.flow.Flow

class GetFoldersUseCase(
    private val repository: FolderRepository
) {
    operator fun invoke(): Flow<List<Folder>> {
        return repository.getFolders()
    }
}