package com.ck.quicknote.feature.home.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// 1. Explicit Navigation State
enum class BottomBarDestination {
    SEARCH, FOLDERS, NOTES, SETTINGS
}

// 2. UI State
data class HomeBottomBarState(
    val currentDestination: BottomBarDestination
)

// 3. Actions
interface HomeBottomBarActions {
    fun onSearchClick()
    fun onFolderClick()
    fun onNotesClick()
    fun onSettingsClick()
    fun onAddClick()
}

// 4. Entry Point
@Composable
fun HomeBottomBar(
    currentDestination: BottomBarDestination,
    onSearchClick: () -> Unit,
    onFolderClick: () -> Unit,
    onNotesClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onAddClick: () -> Unit
) {
    val state = HomeBottomBarState(currentDestination)

    val actions = object : HomeBottomBarActions {
        override fun onSearchClick() = onSearchClick()
        override fun onFolderClick() = onFolderClick()
        override fun onNotesClick() = onNotesClick()
        override fun onSettingsClick() = onSettingsClick()
        override fun onAddClick() = onAddClick()
    }

    HomeBottomBarContent(state = state, actions = actions)
}

// 5. Pure Design Composable
@Composable
fun HomeBottomBarContent(
    state: HomeBottomBarState,
    actions: HomeBottomBarActions,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 8.dp,
        shadowElevation = 4.dp,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Group
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ModernBottomBarItem(
                    label = "Search",
                    icon = Icons.Outlined.Search,
                    selectedIcon = Icons.Filled.Search,
                    isSelected = state.currentDestination == BottomBarDestination.SEARCH,
                    onClick = actions::onSearchClick
                )

                ModernBottomBarItem(
                    label = "Folders",
                    icon = Icons.Outlined.Folder,
                    selectedIcon = Icons.Filled.Folder,
                    isSelected = state.currentDestination == BottomBarDestination.FOLDERS,
                    onClick = actions::onFolderClick
                )
            }

            // Center ADD Button
            Box(
                modifier = Modifier.width(60.dp),
                contentAlignment = Alignment.Center
            ) {
                CenterAddButton(onClick = actions::onAddClick)
            }

            // Right Group
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ModernBottomBarItem(
                    label = "Notes",
                    icon = Icons.Outlined.Description,
                    selectedIcon = Icons.Filled.Description,
                    isSelected = state.currentDestination == BottomBarDestination.NOTES,
                    onClick = actions::onNotesClick
                )

                ModernBottomBarItem(
                    label = "Settings",
                    icon = Icons.Outlined.Settings,
                    selectedIcon = Icons.Filled.Settings,
                    isSelected = state.currentDestination == BottomBarDestination.SETTINGS,
                    onClick = actions::onSettingsClick
                )
            }
        }
    }
}

// Special Center "Add" Button
@Composable
private fun CenterAddButton(onClick: () -> Unit) {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.90f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "AddButtonScale"
    )

    val containerColor by animateColorAsState(
        targetValue = if (isPressed) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.primary,
        label = "AddButtonColor"
    )

    val contentColor by animateColorAsState(
        targetValue = if (isPressed) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onPrimary,
        label = "AddButtonContentColor"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .scale(scale)
            .size(48.dp)
            .shadow(4.dp, CircleShape)
            .background(color = containerColor, shape = CircleShape)
            .clip(CircleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                onClick()
            }
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Create Note",
            tint = contentColor,
            modifier = Modifier.size(24.dp)
        )
    }
}

// 6. Modern Item Component (Icon Only - CIRCULAR SELECTION)
@Composable
private fun ModernBottomBarItem(
    label: String,
    icon: ImageVector,
    selectedIcon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val haptic = LocalHapticFeedback.current

    val iconColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "iconColor"
    )

    val pillColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent,
        label = "pillColor"
    )

    // Pill Background (Changed from expanding width to fixed circle size)
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .clip(CircleShape) // Changed to CircleShape (100% radius)
            .clickable {
                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                onClick()
            }
            .padding(4.dp)
            .size(48.dp) // Fixed Size (Square aspect ratio + CircleShape = Circle)
            .background(
                color = pillColor,
                shape = CircleShape
            )
    ) {
        Icon(
            imageVector = if (isSelected) selectedIcon else icon,
            contentDescription = label,
            tint = iconColor,
            modifier = Modifier.size(24.dp)
        )
    }
}