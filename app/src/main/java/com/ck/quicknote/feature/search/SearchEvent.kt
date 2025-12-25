package com.ck.quicknote.feature.search

sealed class SearchEvent {
    data class EnteredQuery(val value: String): SearchEvent()
    object TogglePinnedFilter: SearchEvent()
    object ToggleImageFilter: SearchEvent()
}