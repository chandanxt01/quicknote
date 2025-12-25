package com.ck.quicknote.feature.folder

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ck.quicknote.domain.model.Folder
import com.ck.quicknote.domain.usecase.NoteUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FolderViewModel @Inject constructor(
    private val noteUseCases: NoteUseCases
) : ViewModel() {

    private val _state = mutableStateOf(FolderState())
    val state: State<FolderState> = _state

    init {
        getFolders()
    }

    fun onEvent(event: FolderEvent) {
        when (event) {
            is FolderEvent.CreateFolder -> {
                viewModelScope.launch {
                    try {
                        if (event.name.isNotBlank()) {
                            noteUseCases.addFolder(Folder(name = event.name))
                        }
                    } catch (e: Exception) {
                        // Handle error
                    }
                }
            }
            is FolderEvent.DeleteFolder -> {
                viewModelScope.launch {
                    // System folders (IDs < 0) or null IDs should not be deleted
                    if ((event.folder.id ?: 0) > 0) {
                        noteUseCases.deleteFolder(event.folder)
                    }
                }
            }
            is FolderEvent.RenameFolder -> {
                viewModelScope.launch {
                    try {
                        if (event.newName.isNotBlank() && (event.folder.id ?: 0) > 0) {
                            // Updating folder by inserting with same ID
                            val updatedFolder = event.folder.copy(name = event.newName)
                            noteUseCases.addFolder(updatedFolder)
                        }
                    } catch (e: Exception) {
                        // Handle error
                    }
                }
            }
        }
    }

    private fun getFolders() {
        noteUseCases.getFolders()
            .onEach { folders ->
                _state.value = state.value.copy(
                    folders = folders,
                    isLoading = false
                )
            }
            .launchIn(viewModelScope)
    }
}