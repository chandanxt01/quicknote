package com.ck.quicknote.domain.usecase

import com.ck.quicknote.domain.model.Note
import com.ck.quicknote.domain.repository.NoteRepository

class DeleteNoteUseCase(
    private val repository: NoteRepository
) {
    suspend operator fun invoke(note: Note) {
        repository.deleteNote(note)
    }
}