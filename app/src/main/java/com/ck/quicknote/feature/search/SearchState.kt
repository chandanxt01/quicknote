package com.ck.quicknote.feature.search

import com.ck.quicknote.domain.model.Note

data class SearchState(
    val searchQuery: String = "",
    val notes: List<Note> = emptyList(),
    val hint: String = "Search notes...",
    val isPinnedFilterActive: Boolean = false,
    val isImageFilterActive: Boolean = false
)