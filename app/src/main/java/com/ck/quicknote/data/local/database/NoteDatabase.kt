package com.ck.quicknote.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.ck.quicknote.data.local.dao.FolderDao
import com.ck.quicknote.data.local.dao.NoteDao
import com.ck.quicknote.data.local.entity.FolderEntity
import com.ck.quicknote.data.local.entity.NoteEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Provider

@Database(
    entities = [NoteEntity::class, FolderEntity::class],
    version = 4,
    exportSchema = false
)
abstract class NoteDatabase: RoomDatabase() {
    abstract val noteDao: NoteDao
    abstract val folderDao: FolderDao // AppModule needs this

    // AppModule needs this Callback class to be public/accessible
    class Callback(
        private val database: Provider<NoteDatabase>
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            val folderDao = database.get().folderDao
            CoroutineScope(Dispatchers.IO).launch {
                folderDao.insertFolder(
                    FolderEntity(
                        name = "General",
                        timestamp = System.currentTimeMillis()
                    )
                )
            }
        }
    }
}