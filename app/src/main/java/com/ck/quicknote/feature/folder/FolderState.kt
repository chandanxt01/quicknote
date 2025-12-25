package com.ck.quicknote.feature.folder

import com.ck.quicknote.domain.model.Folder

data class FolderState(
    val folders: List<Folder> = emptyList(),
    val isLoading: Boolean = false
)