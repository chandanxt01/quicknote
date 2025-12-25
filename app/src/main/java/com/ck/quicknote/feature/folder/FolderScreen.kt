package com.ck.quicknote.feature.folder

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.DriveFileRenameOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.ck.quicknote.core.designsystem.components.QuickNoteAlertDialog // Import Global Alert
import com.ck.quicknote.domain.model.Folder
import com.ck.quicknote.feature.folder.components.FolderCard
import com.ck.quicknote.feature.folder.components.FolderEmptyState
import com.ck.quicknote.feature.folder.components.FolderTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FolderScreen(
    navController: NavController,
    viewModel: FolderViewModel = hiltViewModel()
) {
    val state = viewModel.state.value
    val haptic = LocalHapticFeedback.current

    // UI States
    var showCreateSheet by remember { mutableStateOf(false) }
    var newFolderName by remember { mutableStateOf("") }

    // Selection State
    var selectedFolder by remember { mutableStateOf<Folder?>(null) }
    val isSelectionMode = selectedFolder != null

    // Dialog States
    var showRenameDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) } // State for Delete Confirmation
    var folderRenameText by remember { mutableStateOf("") }

    BackHandler(enabled = isSelectionMode) {
        selectedFolder = null
    }

    Scaffold(
        topBar = {
            FolderTopBar(
                isSelectionMode = isSelectionMode,
                selectedFolderName = selectedFolder?.name,
                onBackClick = { navController.popBackStack() },
                onRenameClick = {
                    folderRenameText = selectedFolder?.name ?: ""
                    showRenameDialog = true
                },
                onDeleteClick = {
                    // Show Confirmation Dialog instead of deleting directly
                    showDeleteDialog = true
                },
                onCancelSelection = { selectedFolder = null }
            )
        },
        floatingActionButton = {
            if (!isSelectionMode) {
                FloatingActionButton(
                    onClick = { showCreateSheet = true },
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp),
                    shape = CircleShape
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Create Folder"
                    )
                }
            }
        }
    ) { padding ->
        if (state.folders.isEmpty()) {
            FolderEmptyState(
                modifier = Modifier.padding(padding),
                onAddFolderClick = { showCreateSheet = true }
            )
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(160.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.folders) { folder ->
                    FolderCard(
                        folder = folder,
                        onClick = {
                            if (isSelectionMode) {
                                selectedFolder = null
                            } else {
                                navController.popBackStack()
                            }
                        },
                        onLongClick = {
                            if ((folder.id ?: 0) > 0) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                selectedFolder = folder
                            }
                        }
                    )
                }
            }
        }
    }

    // --- Global Delete Confirmation Dialog ---
    if (showDeleteDialog && selectedFolder != null) {
        QuickNoteAlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            onConfirm = {
                viewModel.onEvent(FolderEvent.DeleteFolder(selectedFolder!!))
                selectedFolder = null // Exit selection mode
                showDeleteDialog = false
            },
            title = "Delete Folder?",
            text = "Are you sure you want to delete '${selectedFolder?.name}'? Notes inside this folder might be moved to Trash.",
            icon = Icons.Default.Warning,
            confirmText = "Delete",
            isDestructive = true // Red Button
        )
    }

    // --- Rename Dialog (Input Dialog - kept local for now) ---
    if (showRenameDialog) {
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            icon = {
                Icon(Icons.Outlined.DriveFileRenameOutline, contentDescription = null)
            },
            title = {
                Text(
                    text = "Rename Folder",
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                OutlinedTextField(
                    value = folderRenameText,
                    onValueChange = { folderRenameText = it },
                    label = { Text("Folder Name") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (folderRenameText.isNotBlank() && selectedFolder != null) {
                            viewModel.onEvent(FolderEvent.RenameFolder(selectedFolder!!, folderRenameText))
                            showRenameDialog = false
                            selectedFolder = null
                        }
                    }
                ) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) { Text("Cancel") }
            },
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            shape = RoundedCornerShape(28.dp)
        )
    }

    // --- Create Folder Bottom Sheet ---
    if (showCreateSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                showCreateSheet = false
                newFolderName = ""
            },
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            dragHandle = { BottomSheetDefaults.DragHandle() },
            windowInsets = WindowInsets.ime
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 24.dp)
                    .navigationBarsPadding()
                    .imePadding(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Filled.CreateNewFolder,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "New Folder",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = newFolderName,
                    onValueChange = { newFolderName = it },
                    placeholder = { Text("e.g., Work, Ideas") },
                    label = { Text("Folder Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (newFolderName.isNotBlank()) {
                                viewModel.onEvent(FolderEvent.CreateFolder(newFolderName))
                                showCreateSheet = false
                                newFolderName = ""
                            }
                        }
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        if (newFolderName.isNotBlank()) {
                            viewModel.onEvent(FolderEvent.CreateFolder(newFolderName))
                            showCreateSheet = false
                            newFolderName = ""
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(25.dp)
                ) {
                    Text(
                        text = "Create Folder",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}