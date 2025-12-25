package com.ck.quicknote.feature.archive

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ck.quicknote.domain.usecase.NoteUseCases
import com.ck.quicknote.domain.util.NoteOrder
import com.ck.quicknote.domain.util.OrderType
import com.ck.quicknote.feature.home.HomeState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class ArchiveViewModel @Inject constructor(
    private val noteUseCases: NoteUseCases
) : ViewModel() {

    private val _state = mutableStateOf(HomeState())
    val state: State<HomeState> = _state

    private var getNotesJob: Job? = null

    init {
        getNotes()
    }

    private fun getNotes() {
        getNotesJob?.cancel()
        // Default sorting by Date Descending
        getNotesJob = noteUseCases.getNotes(NoteOrder.Date(OrderType.Descending))
            .onEach { notes ->
                _state.value = state.value.copy(
                    // Filter: Only show notes that ARE archived
                    notes = notes.filter { it.isArchived }
                )
            }
            .launchIn(viewModelScope)
    }
}