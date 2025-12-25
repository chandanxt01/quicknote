package com.ck.quicknote.feature.home

import com.ck.quicknote.domain.model.Folder
import com.ck.quicknote.domain.model.Note
import com.ck.quicknote.domain.util.NoteOrder
import com.ck.quicknote.domain.util.OrderType

data class HomeState(
    val notes: List<Note> = emptyList(),
    val noteOrder: NoteOrder = NoteOrder.Date(OrderType.Descending),
    val isOrderSectionVisible: Boolean = false,
    val isGridView: Boolean = true,

    // Folder State
    val folders: List<Folder> = emptyList(), // "All", "Work", "Archive", etc.
    val selectedFolder: Folder = Folder(id = -1, name = "All") // Default: All
)