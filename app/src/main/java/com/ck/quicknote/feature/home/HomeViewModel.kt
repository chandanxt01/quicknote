package com.ck.quicknote.feature.home

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ck.quicknote.core.common.PreferencesManager
import com.ck.quicknote.domain.model.Folder
import com.ck.quicknote.domain.model.Note
import com.ck.quicknote.domain.usecase.NoteUseCases
import com.ck.quicknote.domain.util.NoteOrder
import com.ck.quicknote.domain.util.OrderType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val noteUseCases: NoteUseCases,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _state = mutableStateOf(HomeState())
    val state: State<HomeState> = _state

    private var recentlyDeletedNote: Note? = null
    private var getNotesJob: Job? = null
    private var getFoldersJob: Job? = null

    private var allNotes: List<Note> = emptyList()

    private val folderAll = Folder(id = -1, name = "All")
    private val folderArchive = Folder(id = -2, name = "Archive")

    init {
        getFolders()

        preferencesManager.isGridViewFlow
            .onEach { isGrid ->
                _state.value = state.value.copy(isGridView = isGrid)
            }
            .launchIn(viewModelScope)

        preferencesManager.sortOrderFlow
            .onEach { order ->
                getNotes(order)
            }
            .launchIn(viewModelScope)
    }

    fun onEvent(event: HomeEvent) {
        when (event) {
            is HomeEvent.Order -> {
                if (state.value.noteOrder::class == event.noteOrder::class &&
                    state.value.noteOrder.orderType == event.noteOrder.orderType
                ) {
                    return
                }
                preferencesManager.setSortOrder(event.noteOrder)
            }
            is HomeEvent.DeleteNote -> {
                viewModelScope.launch {
                    noteUseCases.deleteNote(event.note)
                    recentlyDeletedNote = event.note
                }
            }
            is HomeEvent.RestoreNote -> {
                viewModelScope.launch {
                    noteUseCases.addNote(recentlyDeletedNote ?: return@launch)
                    recentlyDeletedNote = null
                }
            }
            is HomeEvent.ToggleOrderSection -> {
                _state.value = state.value.copy(
                    isOrderSectionVisible = !state.value.isOrderSectionVisible
                )
            }
            is HomeEvent.ToggleView -> {
                preferencesManager.setGridView(!state.value.isGridView)
            }
            is HomeEvent.SelectFolder -> {
                _state.value = state.value.copy(selectedFolder = event.folder)
                applyFilter()
            }
            is HomeEvent.CreateFolder -> {
                viewModelScope.launch {
                    try {
                        noteUseCases.addFolder(Folder(name = event.name))
                    } catch (e: Exception) { }
                }
            }
            // New Folder Actions
            is HomeEvent.RenameFolder -> {
                viewModelScope.launch {
                    try {
                        // Rename: Just insert/update with same ID but new Name
                        val updatedFolder = event.folder.copy(name = event.newName)
                        noteUseCases.addFolder(updatedFolder)
                    } catch (e: Exception) { }
                }
            }
            is HomeEvent.DeleteFolder -> {
                viewModelScope.launch {
                    noteUseCases.deleteFolder(event.folder)
                    // If deleted folder was selected, switch to All
                    if (state.value.selectedFolder.id == event.folder.id) {
                        _state.value = state.value.copy(selectedFolder = folderAll)
                        applyFilter()
                    }
                }
            }
            is HomeEvent.PinFolder -> {
                // TODO: Implement Pin logic in DB (Requires schema update)
                // For now, this is a placeholder
            }
        }
    }

    private fun getFolders() {
        getFoldersJob?.cancel()
        getFoldersJob = noteUseCases.getFolders()
            .onEach { dbFolders ->
                val uiFolders = listOf(folderAll) + dbFolders + listOf(folderArchive)
                // Ensure selected folder still exists (or reset if deleted)
                _state.value = state.value.copy(folders = uiFolders)
            }
            .launchIn(viewModelScope)
    }

    private fun getNotes(noteOrder: NoteOrder) {
        getNotesJob?.cancel()
        getNotesJob = noteUseCases.getNotes(noteOrder)
            .onEach { notes ->
                allNotes = notes
                applyFilter()
                _state.value = state.value.copy(noteOrder = noteOrder)
            }
            .launchIn(viewModelScope)
    }

    private fun applyFilter() {
        val selected = state.value.selectedFolder
        val filteredNotes = when (selected.id) {
            -1L -> allNotes.filter { !it.isArchived }
            -2L -> allNotes.filter { it.isArchived }
            else -> allNotes.filter { !it.isArchived && it.folderId == selected.id }
        }
        _state.value = state.value.copy(notes = filteredNotes)
    }
}