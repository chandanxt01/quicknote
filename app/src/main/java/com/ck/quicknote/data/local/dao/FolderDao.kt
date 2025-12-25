package com.ck.quicknote.data.local.dao

import androidx.room.*
import com.ck.quicknote.data.local.entity.FolderEntity
import kotlinx.coroutines.flow.Flow

// Helper class for Room result
data class FolderWithCount(
    @Embedded val folder: FolderEntity,
    val noteCount: Int
)

@Dao
interface FolderDao {

    // Updated Query: Joins folders with notes and counts them
    // Note: We only count unarchived notes (isArchived = 0) for regular folders
    @Query("SELECT f.*, COUNT(n.id) as noteCount FROM folders f LEFT JOIN note_table n ON f.id = n.folderId AND n.isArchived = 0 GROUP BY f.id ORDER BY f.timestamp DESC")
    fun getFoldersWithCount(): Flow<List<FolderWithCount>>

    @Query("SELECT * FROM folders WHERE id = :id")
    suspend fun getFolderById(id: Long): FolderEntity?

    @Query("SELECT * FROM folders WHERE name = :name LIMIT 1")
    suspend fun getFolderByName(name: String): FolderEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFolder(folder: FolderEntity): Long

    @Delete
    suspend fun deleteFolder(folder: FolderEntity)
}