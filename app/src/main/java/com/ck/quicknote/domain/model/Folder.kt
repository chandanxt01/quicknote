package com.ck.quicknote.domain.model

data class Folder(
    val id: Long? = null,
    val name: String,
    val isArchive: Boolean = false,
    val timestamp: Long = System.currentTimeMillis(),
    val noteCount: Int = 0 // New field for counting notes
)