package com.ck.quicknote.feature.folder.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.DriveFileRenameOutline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FolderTopBar(
    isSelectionMode: Boolean,
    selectedFolderName: String?,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    onBackClick: () -> Unit,
    // onAddFolderClick removed (Moved to FAB)
    onRenameClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onCancelSelection: () -> Unit
) {
    val containerColor by animateColorAsState(
        targetValue = if (isSelectionMode)
            MaterialTheme.colorScheme.secondaryContainer
        else
            MaterialTheme.colorScheme.surface,
        animationSpec = tween(400),
        label = "AppBarColor"
    )

    val contentColor by animateColorAsState(
        targetValue = if (isSelectionMode)
            MaterialTheme.colorScheme.onSecondaryContainer
        else
            MaterialTheme.colorScheme.onSurface,
        animationSpec = tween(400),
        label = "AppBarContentColor"
    )

    Column(modifier = Modifier.background(containerColor)) {
        TopAppBar(
            title = {
                AnimatedContent(
                    targetState = isSelectionMode,
                    transitionSpec = {
                        (fadeIn() + slideInVertically { height -> height }) togetherWith
                                (fadeOut() + slideOutVertically { height -> -height })
                    },
                    label = "TitleAnimation"
                ) { selectionMode ->
                    Text(
                        text = if (selectionMode) selectedFolderName ?: "Selected" else "Folders",
                        // FIXED: Removed .copy(fontWeight = FontWeight.SemiBold)
                        // It will now use the global style from Type.kt
                        style = MaterialTheme.typography.titleLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = contentColor
                    )
                }
            },
            navigationIcon = {
                IconButton(
                    onClick = if (isSelectionMode) onCancelSelection else onBackClick
                ) {
                    AnimatedContent(
                        targetState = isSelectionMode,
                        label = "NavIconAnimation"
                    ) { selectionMode ->
                        if (selectionMode) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close selection",
                                tint = contentColor
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = contentColor
                            )
                        }
                    }
                }
            },
            actions = {
                AnimatedContent(
                    targetState = isSelectionMode,
                    label = "ActionsAnimation"
                ) { selectionMode ->
                    if (selectionMode) {
                        Row {
                            IconButton(onClick = onRenameClick) {
                                Icon(
                                    imageVector = Icons.Outlined.DriveFileRenameOutline,
                                    contentDescription = "Rename",
                                    tint = contentColor
                                )
                            }
                            IconButton(onClick = onDeleteClick) {
                                Icon(
                                    imageVector = Icons.Outlined.Delete,
                                    contentDescription = "Delete",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    } else {
                        // Empty for normal mode (Add button is now FAB)
                    }
                }
            },
            scrollBehavior = scrollBehavior,
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent,
                scrolledContainerColor = Color.Transparent,
                titleContentColor = contentColor,
                navigationIconContentColor = contentColor,
                actionIconContentColor = contentColor
            )
        )

        // Separator Line
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
        )
    }
}