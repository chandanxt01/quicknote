package com.ck.quicknote.domain.repository

import com.ck.quicknote.domain.model.Folder
import kotlinx.coroutines.flow.Flow

interface FolderRepository {

    fun getFolders(): Flow<List<Folder>>

    suspend fun getFolderById(id: Long): Folder?

    suspend fun insertFolder(folder: Folder)

    suspend fun deleteFolder(folder: Folder)
}