package com.ck.quicknote.core.common

import android.content.Context
import android.content.SharedPreferences
import com.ck.quicknote.domain.util.NoteOrder
import com.ck.quicknote.domain.util.OrderType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PreferencesManager(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("quick_note_prefs", Context.MODE_PRIVATE)

    // --- App Lock ---
    fun isAppLockEnabled(): Boolean {
        return sharedPreferences.getBoolean("is_app_lock_enabled", false)
    }

    fun setAppLockEnabled(isEnabled: Boolean) {
        sharedPreferences.edit().putBoolean("is_app_lock_enabled", isEnabled).apply()
    }

    // --- Theme ---
    fun getThemeMode(): Int {
        return sharedPreferences.getInt("theme_mode", 0)
    }

    fun setThemeMode(mode: Int) {
        sharedPreferences.edit().putInt("theme_mode", mode).apply()
    }

    // --- View Mode (Grid/List) ---
    // StateFlow allows ViewModels to observe changes instantly
    private val _isGridViewFlow = MutableStateFlow(getGridView())
    val isGridViewFlow: StateFlow<Boolean> = _isGridViewFlow.asStateFlow()

    private fun getGridView(): Boolean {
        return sharedPreferences.getBoolean("is_grid_view", true)
    }

    fun setGridView(isGrid: Boolean) {
        sharedPreferences.edit().putBoolean("is_grid_view", isGrid).apply()
        _isGridViewFlow.value = isGrid // Update Flow
    }

    // --- Sort Order ---
    // Default: Date Descending
    private val _sortOrderFlow = MutableStateFlow(getSortOrder())
    val sortOrderFlow: StateFlow<NoteOrder> = _sortOrderFlow.asStateFlow()

    private fun getSortOrder(): NoteOrder {
        val type = sharedPreferences.getString("sort_type", "Date") ?: "Date"
        val direction = sharedPreferences.getString("sort_direction", "Desc") ?: "Desc"

        val orderType = if (direction == "Asc") OrderType.Ascending else OrderType.Descending

        return when(type) {
            "Title" -> NoteOrder.Title(orderType)
            "Date" -> NoteOrder.Date(orderType)
            "Color" -> NoteOrder.Color(orderType)
            else -> NoteOrder.Date(orderType)
        }
    }

    fun setSortOrder(order: NoteOrder) {
        val type = when(order) {
            is NoteOrder.Title -> "Title"
            is NoteOrder.Date -> "Date"
            is NoteOrder.Color -> "Color"
        }
        val direction = when(order.orderType) {
            is OrderType.Ascending -> "Asc"
            is OrderType.Descending -> "Desc"
        }

        sharedPreferences.edit()
            .putString("sort_type", type)
            .putString("sort_direction", direction)
            .apply()

        _sortOrderFlow.value = order // Update Flow
    }
}