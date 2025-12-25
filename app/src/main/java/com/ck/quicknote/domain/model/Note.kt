package com.ck.quicknote.domain.model

import androidx.compose.ui.graphics.Color

data class Note(
    val id: Int? = null,
    val title: String,
    val content: String,
    val timestamp: Long,
    val color: Int,
    val isPinned: Boolean = false,
    val isArchived: Boolean = false,
    val imageUri: String? = null,
    val reminder: Long? = null,
    val folderId: Long? = null // यह Field ज़रूरी है
) {
    companion object {
        val noteColors = listOf(
            Color(0xFFF8D7DA),
            Color(0xFFD4EDDA),
            Color(0xFFFFF3CD),
            Color(0xFFD1ECF1),
            Color(0xFFE2E3E5),
            Color(0xFFF3E5F5)
        )
    }
}

class InvalidNoteException(message: String): Exception(message)