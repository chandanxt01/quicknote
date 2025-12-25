package com.ck.quicknote.feature.note_detail

import android.app.Application
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ck.quicknote.core.common.AlarmScheduler
import com.ck.quicknote.core.common.Constants
import com.ck.quicknote.core.common.UiEvent
import com.ck.quicknote.domain.model.InvalidNoteException
import com.ck.quicknote.domain.model.Note
import com.ck.quicknote.domain.usecase.NoteUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NoteDetailViewModel @Inject constructor(
    private val noteUseCases: NoteUseCases,
    savedStateHandle: SavedStateHandle,
    application: Application
) : ViewModel() {

    private val _state = mutableStateOf(NoteDetailState())
    val state: State<NoteDetailState> = _state

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    private var currentNoteId: Int? = null
    private val alarmScheduler = AlarmScheduler(application)

    init {
        // 1. Fetch Folders
        noteUseCases.getFolders()
            .onEach { folders ->
                _state.value = state.value.copy(
                    folders = folders,
                    folderId = state.value.folderId ?: folders.firstOrNull()?.id
                )
            }
            .launchIn(viewModelScope)

        // 2. Fetch Note Detail
        savedStateHandle.get<Int>(Constants.NOTE_ID)?.let { noteId ->
            if(noteId != -1) {
                viewModelScope.launch {
                    noteUseCases.getNote(noteId)?.also { note ->
                        currentNoteId = note.id
                        _state.value = state.value.copy(
                            noteTitle = state.value.noteTitle.copy(
                                text = note.title,
                                isHintVisible = false
                            ),
                            noteContent = state.value.noteContent.copy(
                                text = note.content,
                                isHintVisible = false
                            ),
                            noteColor = note.color,
                            noteImageUri = note.imageUri,
                            isPinned = note.isPinned,
                            isArchived = note.isArchived,
                            reminderTime = note.reminder,
                            folderId = note.folderId
                        )
                    }
                }
            }
        }
    }

    fun onEvent(event: NoteDetailEvent) {
        when(event) {
            is NoteDetailEvent.EnteredTitle -> {
                _state.value = state.value.copy(
                    noteTitle = state.value.noteTitle.copy(text = event.value)
                )
            }
            is NoteDetailEvent.ChangeTitleFocus -> {
                _state.value = state.value.copy(
                    noteTitle = state.value.noteTitle.copy(
                        isHintVisible = !event.focusState.isFocused && state.value.noteTitle.text.isBlank()
                    )
                )
            }
            is NoteDetailEvent.EnteredContent -> {
                _state.value = state.value.copy(
                    noteContent = state.value.noteContent.copy(text = event.value)
                )
            }
            is NoteDetailEvent.ChangeContentFocus -> {
                _state.value = state.value.copy(
                    noteContent = state.value.noteContent.copy(
                        isHintVisible = !event.focusState.isFocused && state.value.noteContent.text.isBlank()
                    )
                )
            }
            is NoteDetailEvent.ChangeColor -> {
                _state.value = state.value.copy(noteColor = event.color)
            }
            is NoteDetailEvent.ChangeImage -> {
                _state.value = state.value.copy(noteImageUri = event.imageUri)
            }
            is NoteDetailEvent.TogglePin -> {
                _state.value = state.value.copy(isPinned = !state.value.isPinned)
            }
            is NoteDetailEvent.ToggleArchive -> {
                _state.value = state.value.copy(isArchived = !state.value.isArchived)
            }
            is NoteDetailEvent.SetReminder -> {
                _state.value = state.value.copy(reminderTime = event.time)
            }
            is NoteDetailEvent.ChangeFolder -> {
                _state.value = state.value.copy(folderId = event.folderId)
            }
            is NoteDetailEvent.DeleteNote -> {
                viewModelScope.launch {
                    if (currentNoteId != null) {
                        val noteToDelete = Note(
                            id = currentNoteId,
                            title = "", content = "", timestamp = 0L, color = 0
                        )
                        noteUseCases.deleteNote(noteToDelete)
                    }
                    _eventFlow.emit(UiEvent.PopBackStack)
                }
            }
            is NoteDetailEvent.SaveNote -> {
                viewModelScope.launch {
                    val title = state.value.noteTitle.text
                    val content = state.value.noteContent.text
                    val hasImage = state.value.noteImageUri != null

                    // FIX: Agar note bilkul khali hai (Title, Content aur Image nahi hai)
                    if (title.isBlank() && content.isBlank() && !hasImage) {
                        if (currentNoteId != null) {
                            // Agar purana note tha aur ab khali kar diya, to delete kar do
                            val noteToDelete = Note(
                                id = currentNoteId,
                                title = "", content = "", timestamp = 0L, color = 0
                            )
                            noteUseCases.deleteNote(noteToDelete)
                        }
                        // Chupchap wapas jao (Save mat karo, Error mat dikhao)
                        _eventFlow.emit(UiEvent.PopBackStack)
                        return@launch
                    }

                    try {
                        val finalFolderId = state.value.folderId ?: state.value.folders.firstOrNull()?.id

                        // FIX: Agar Title khali hai par Content hai, to "Untitled" naam de do
                        // taaki Validation Error na aaye
                        val finalTitle = if (title.isBlank()) "Untitled" else title
                        // FIX: Agar Content khali hai (par Title/Image hai), to empty space de do
                        val finalContent = if (content.isBlank()) " " else content

                        val note = Note(
                            title = finalTitle,
                            content = finalContent,
                            timestamp = System.currentTimeMillis(),
                            color = state.value.noteColor,
                            id = currentNoteId,
                            imageUri = state.value.noteImageUri,
                            isPinned = state.value.isPinned,
                            isArchived = state.value.isArchived,
                            reminder = state.value.reminderTime,
                            folderId = finalFolderId
                        )
                        noteUseCases.addNote(note)

                        if (note.reminder != null) {
                            alarmScheduler.schedule(note)
                        } else {
                            alarmScheduler.cancel(note)
                        }

                        _eventFlow.emit(UiEvent.PopBackStack)
                    } catch(e: InvalidNoteException) {
                        _eventFlow.emit(
                            UiEvent.ShowSnackbar(message = e.message ?: "Couldn't save note")
                        )
                    }
                }
            }
        }
    }
}