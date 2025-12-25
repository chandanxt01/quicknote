package com.ck.quicknote.domain.usecase

import com.ck.quicknote.domain.usecase.AddFolderUseCase
import com.ck.quicknote.domain.usecase.AddNoteUseCase
import com.ck.quicknote.domain.usecase.DeleteFolderUseCase
import com.ck.quicknote.domain.usecase.DeleteNoteUseCase
import com.ck.quicknote.domain.usecase.GetFoldersUseCase
import com.ck.quicknote.domain.usecase.GetNoteUseCase
import com.ck.quicknote.domain.usecase.GetNotesUseCase

data class NoteUseCases(
    val getNotes: GetNotesUseCase,
    val deleteNote: DeleteNoteUseCase,
    val addNote: AddNoteUseCase,
    val getNote: GetNoteUseCase,
    val getFolders: GetFoldersUseCase,
    val addFolder: AddFolderUseCase,
    val deleteFolder: DeleteFolderUseCase // Added new use case
)