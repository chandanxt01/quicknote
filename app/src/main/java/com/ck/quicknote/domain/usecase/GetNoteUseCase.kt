package com.ck.quicknote.domain.usecase

import com.ck.quicknote.domain.model.Note
import com.ck.quicknote.domain.repository.NoteRepository

class GetNoteUseCase(
    private val repository: NoteRepository
) {
    suspend operator fun invoke(id: Int): Note? {
        return repository.getNoteById(id)
    }
}