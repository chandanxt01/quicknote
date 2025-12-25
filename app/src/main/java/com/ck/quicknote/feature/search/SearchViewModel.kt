package com.ck.quicknote.feature.search

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ck.quicknote.domain.model.Note
import com.ck.quicknote.domain.usecase.NoteUseCases
import com.ck.quicknote.domain.util.NoteOrder
import com.ck.quicknote.domain.util.OrderType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val noteUseCases: NoteUseCases
) : ViewModel() {

    private val _state = mutableStateOf(SearchState())
    val state: State<SearchState> = _state

    private var searchJob: Job? = null
    // Cache all notes to filter locally
    private var allNotes: List<Note> = emptyList()

    init {
        // Load notes initially
        noteUseCases.getNotes(NoteOrder.Date(OrderType.Descending))
            .onEach { notes ->
                allNotes = notes
                // Re-run search if notes update (e.g. syncing)
                executeSearch()
            }
            .launchIn(viewModelScope)
    }

    fun onEvent(event: SearchEvent) {
        when(event) {
            is SearchEvent.EnteredQuery -> {
                _state.value = state.value.copy(searchQuery = event.value)

                // Debounce search to avoid lag
                searchJob?.cancel()
                searchJob = viewModelScope.launch {
                    delay(300L)
                    executeSearch()
                }
            }
            is SearchEvent.TogglePinnedFilter -> {
                _state.value = state.value.copy(
                    isPinnedFilterActive = !state.value.isPinnedFilterActive
                )
                executeSearch()
            }
            is SearchEvent.ToggleImageFilter -> {
                _state.value = state.value.copy(
                    isImageFilterActive = !state.value.isImageFilterActive
                )
                executeSearch()
            }
        }
    }

    private fun executeSearch() {
        val query = state.value.searchQuery.lowercase()
        val isPinnedOnly = state.value.isPinnedFilterActive
        val isImageOnly = state.value.isImageFilterActive

        // If nothing searched and no filters, show empty list
        if(query.isBlank() && !isPinnedOnly && !isImageOnly) {
            _state.value = state.value.copy(notes = emptyList())
            return
        }

        val filteredNotes = allNotes.filter { note ->
            // 1. Text Match
            val matchesQuery = if (query.isNotBlank()) {
                note.title.lowercase().contains(query) ||
                        note.content.lowercase().contains(query)
            } else true

            // 2. Filter Match
            val matchesPin = if (isPinnedOnly) note.isPinned else true
            val matchesImage = if (isImageOnly) note.imageUri != null else true

            matchesQuery && matchesPin && matchesImage
        }
        _state.value = state.value.copy(notes = filteredNotes)
    }
}