package com.ck.quicknote.core.di

import android.app.Application
import androidx.room.Room
import com.ck.quicknote.core.common.Constants
import com.ck.quicknote.core.common.PreferencesManager
import com.ck.quicknote.data.local.database.NoteDatabase
import com.ck.quicknote.data.repository.FolderRepositoryImpl
import com.ck.quicknote.data.repository.NoteRepositoryImpl
import com.ck.quicknote.domain.repository.FolderRepository
import com.ck.quicknote.domain.repository.NoteRepository
import com.ck.quicknote.domain.usecase.*
import com.ck.quicknote.domain.usecase.NoteUseCases
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Provider
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideNoteDatabase(app: Application, provider: Provider<NoteDatabase>): NoteDatabase {
        return Room.databaseBuilder(
            app,
            NoteDatabase::class.java,
            Constants.DATABASE_NAME
        )
            .addCallback(NoteDatabase.Callback(provider))
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideNoteRepository(db: NoteDatabase): NoteRepository {
        return NoteRepositoryImpl(db.noteDao)
    }

    @Provides
    @Singleton
    fun provideFolderRepository(db: NoteDatabase): FolderRepository {
        return FolderRepositoryImpl(db.folderDao)
    }

    @Provides
    @Singleton
    fun provideNoteUseCases(
        noteRepository: NoteRepository,
        folderRepository: FolderRepository
    ): NoteUseCases {
        return NoteUseCases(
            getNotes = GetNotesUseCase(noteRepository),
            deleteNote = DeleteNoteUseCase(noteRepository),
            addNote = AddNoteUseCase(noteRepository),
            getNote = GetNoteUseCase(noteRepository),
            getFolders = GetFoldersUseCase(folderRepository),
            addFolder = AddFolderUseCase(folderRepository),
            deleteFolder = DeleteFolderUseCase(folderRepository) // Injected
        )
    }

    @Provides
    @Singleton
    fun providePreferencesManager(app: Application): PreferencesManager {
        return PreferencesManager(app)
    }
}