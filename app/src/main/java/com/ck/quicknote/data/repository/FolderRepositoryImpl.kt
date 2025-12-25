package com.ck.quicknote.data.repository

import com.ck.quicknote.data.local.dao.FolderDao
import com.ck.quicknote.data.mapper.toFolderEntity
import com.ck.quicknote.domain.model.Folder
import com.ck.quicknote.domain.repository.FolderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FolderRepositoryImpl(
    private val dao: FolderDao
) : FolderRepository {

    override fun getFolders(): Flow<List<Folder>> {
        // Map the FolderWithCount result to our Domain Folder model
        return dao.getFoldersWithCount().map { list ->
            list.map { item ->
                Folder(
                    id = item.folder.id,
                    name = item.folder.name,
                    timestamp = item.folder.timestamp,
                    // Map the count from DB to Domain model
                    noteCount = item.noteCount
                )
            }
        }
    }

    override suspend fun getFolderById(id: Long): Folder? {
        // Simple fetch for single folder (count not needed here usually, or fetch separately)
        val entity = dao.getFolderById(id) ?: return null
        return Folder(
            id = entity.id,
            name = entity.name,
            timestamp = entity.timestamp
        )
    }

    override suspend fun insertFolder(folder: Folder) {
        dao.insertFolder(folder.toFolderEntity())
    }

    override suspend fun deleteFolder(folder: Folder) {
        dao.deleteFolder(folder.toFolderEntity())
    }
}