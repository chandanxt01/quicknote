package com.ck.quicknote.feature.home

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items as lazyItems
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items as staggeredItems
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.ck.quicknote.R
import com.ck.quicknote.domain.model.Folder
import com.ck.quicknote.feature.home.components.BottomBarDestination
import com.ck.quicknote.feature.home.components.HomeBottomBar
import com.ck.quicknote.feature.home.components.HomeGlassHeader
import com.ck.quicknote.feature.home.components.HomeTopBar
import com.ck.quicknote.feature.home.components.NoteItem
import com.ck.quicknote.feature.home.components.OrderSection
import com.ck.quicknote.feature.util.Screen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state = viewModel.state.value
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Bottom Sheet for Folder Selection
    var showFolderSheet by remember { mutableStateOf(false) }

    val noteDeletedMsg = stringResource(id = R.string.note_deleted_msg)
    val undoLabel = stringResource(id = R.string.undo_action)

    // Scroll States used for FAB animation
    val gridState = rememberLazyStaggeredGridState()
    val listState = rememberLazyListState()

    val isScrolling = if (state.isGridView) {
        gridState.isScrollInProgress
    } else {
        listState.isScrollInProgress
    }

    val fabScale by animateFloatAsState(
        targetValue = if (isScrolling) 0f else 1f,
        label = "FabScale"
    )

    // Determine current bottom bar destination based on selected folder
    val currentDestination = if (state.selectedFolder.id == -1L) {
        BottomBarDestination.NOTES
    } else {
        BottomBarDestination.FOLDERS
    }

    // Fixed Header Height space
    val headerHeight = 90.dp

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            HomeBottomBar(
                currentDestination = currentDestination,
                onSearchClick = { navController.navigate(Screen.SearchScreen.route) },
                // FIX: Navigate to Folder Screen instead of showing sheet
                onFolderClick = { navController.navigate(Screen.FolderScreen.route) },
                onNotesClick = {
                    val allFolder = state.folders.firstOrNull { it.id == -1L } ?: Folder(id = -1, name = "All")
                    viewModel.onEvent(HomeEvent.SelectFolder(allFolder))
                },
                onSettingsClick = { navController.navigate(Screen.SettingsScreen.route) },
                onAddClick = { navController.navigate(Screen.NoteDetailScreen.route) }
            )
        }
        // FAB is now integrated into BottomBar via onAddClick
    ) { padding ->

        Box(modifier = Modifier.fillMaxSize()) {

            // --- Main Content (Scrollable) ---
            val listContentPadding = PaddingValues(
                top = headerHeight,
                bottom = padding.calculateBottomPadding() + 16.dp,
                start = 16.dp,
                end = 16.dp
            )

            if (state.isGridView) {
                LazyVerticalStaggeredGrid(
                    state = gridState,
                    columns = StaggeredGridCells.Adaptive(160.dp),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = listContentPadding,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalItemSpacing = 12.dp
                ) {
                    // 1. Folder Chips Row (Scrollable horizontally inside grid)
                    item(span = StaggeredGridItemSpan.FullLine) {
                        HomeTopBar(
                            state = state,
                            onEvent = viewModel::onEvent
                        )
                    }

                    // 2. Sort Section
                    item(span = StaggeredGridItemSpan.FullLine) {
                        AnimatedVisibility(
                            visible = state.isOrderSectionVisible,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            OrderSection(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                                noteOrder = state.noteOrder,
                                onOrderChange = { viewModel.onEvent(HomeEvent.Order(it)) }
                            )
                        }
                    }

                    // 3. Notes List
                    if (state.notes.isEmpty()) {
                        item(span = StaggeredGridItemSpan.FullLine) {
                            EmptyStateView(
                                isArchive = state.selectedFolder.isArchive,
                                modifier = Modifier.fillMaxWidth().padding(top = 100.dp)
                            )
                        }
                    } else {
                        staggeredItems(state.notes) { note ->
                            NoteItem(
                                note = note,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        navController.navigate(Screen.NoteDetailScreen.route + "?noteId=${note.id}&noteColor=${note.color}")
                                    },
                                onDeleteClick = {
                                    viewModel.onEvent(HomeEvent.DeleteNote(note))
                                    scope.launch {
                                        val result = snackbarHostState.showSnackbar(message = noteDeletedMsg, actionLabel = undoLabel)
                                        if (result == SnackbarResult.ActionPerformed) {
                                            viewModel.onEvent(HomeEvent.RestoreNote)
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = listContentPadding,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 1. Folder Chips Row
                    item {
                        HomeTopBar(
                            state = state,
                            onEvent = viewModel::onEvent
                        )
                    }

                    // 2. Sort Section
                    item {
                        AnimatedVisibility(
                            visible = state.isOrderSectionVisible,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            OrderSection(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                                noteOrder = state.noteOrder,
                                onOrderChange = { viewModel.onEvent(HomeEvent.Order(it)) }
                            )
                        }
                    }

                    // 3. Notes List
                    if (state.notes.isEmpty()) {
                        item {
                            EmptyStateView(
                                isArchive = state.selectedFolder.isArchive,
                                modifier = Modifier.fillMaxWidth().padding(top = 100.dp)
                            )
                        }
                    } else {
                        lazyItems(state.notes) { note ->
                            NoteItem(
                                note = note,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        navController.navigate(Screen.NoteDetailScreen.route + "?noteId=${note.id}&noteColor=${note.color}")
                                    },
                                onDeleteClick = {
                                    viewModel.onEvent(HomeEvent.DeleteNote(note))
                                    scope.launch {
                                        val result = snackbarHostState.showSnackbar(message = noteDeletedMsg, actionLabel = undoLabel)
                                        if (result == SnackbarResult.ActionPerformed) {
                                            viewModel.onEvent(HomeEvent.RestoreNote)
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }

            // --- Glass Header (Fixed Overlay) ---
            HomeGlassHeader(
                isGridView = state.isGridView,
                onSortClick = { viewModel.onEvent(HomeEvent.ToggleOrderSection) },
                onViewClick = { viewModel.onEvent(HomeEvent.ToggleView) },
                onProfileClick = { navController.navigate(Screen.SettingsScreen.route) },
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }

        // --- Folder Selection Sheet (Bottom Sheet for Switching Folders) ---
        // This is optional if HomeTopBar chips are enough, but good for "More" folders
        if (showFolderSheet) {
            ModalBottomSheet(
                onDismissRequest = { showFolderSheet = false },
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Select Folder", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 16.dp))
                    LazyColumn {
                        lazyItems(state.folders) { folder ->
                            val isSelected = state.selectedFolder.id == folder.id
                            ListItem(
                                headlineContent = { Text(folder.name) },
                                leadingContent = {
                                    Icon(
                                        imageVector = if (isSelected) Icons.Default.Folder else Icons.Outlined.Folder,
                                        contentDescription = null,
                                        tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                trailingContent = {
                                    if (isSelected) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    }
                                },
                                modifier = Modifier.clickable {
                                    viewModel.onEvent(HomeEvent.SelectFolder(folder))
                                    showFolderSheet = false
                                }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(48.dp))
                }
            }
        }
    }
}

// Extracted Empty State Component
@Composable
fun EmptyStateView(
    isArchive: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = if(isArchive) Icons.Filled.Archive else Icons.Filled.Description,
            contentDescription = null,
            modifier = Modifier
                .size(100.dp)
                .padding(bottom = 16.dp),
            tint = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        )
        Text(
            text = if (isArchive) "No archived notes" else "Capture your thoughts",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = if (isArchive) "Archived notes will appear here" else "Tap the + button to create a note",
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}