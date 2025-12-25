package com.ck.quicknote.feature.util

sealed class Screen(val route: String) {
    object HomeScreen: Screen("home_screen")
    object NoteDetailScreen: Screen("note_detail_screen")
    object SearchScreen: Screen("search_screen")
    object SettingsScreen: Screen("settings_screen")
    object ArchiveScreen: Screen("archive_screen") // New Route
    object FolderScreen: Screen("folder_screen") // This Route

}