package com.ck.quicknote.feature.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.ck.quicknote.core.common.PreferencesManager
import com.ck.quicknote.domain.util.NoteOrder
import com.ck.quicknote.domain.util.OrderType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    preferencesManager: PreferencesManager
) {
    // --- State Observation from Preferences ---
    var isLockEnabled by remember {
        mutableStateOf(preferencesManager.isAppLockEnabled())
    }

    // Observing Flows for reactive UI updates
    val isGridView by preferencesManager.isGridViewFlow.collectAsState()
    val currentSortOrder by preferencesManager.sortOrderFlow.collectAsState()

    // --- Local UI States ---
    var showThemeDialog by remember { mutableStateOf(false) }
    var currentTheme by remember { mutableIntStateOf(preferencesManager.getThemeMode()) }
    var showSortDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                // FONT UPDATE: Removed manual FontWeight.SemiBold
                // It will now use the global style from Type.kt (titleMedium is SemiBold by default there)
                title = { Text("Settings", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // --- Appearance ---
            SettingsGroup(title = "Appearance") {
                SettingsItem(
                    icon = Icons.Outlined.Palette,
                    title = "App Theme",
                    subtitle = when(currentTheme) {
                        1 -> "Light Mode"
                        2 -> "Dark Mode"
                        else -> "Follow System"
                    },
                    onClick = { showThemeDialog = true }
                )
                // Grid View Toggle
                SettingsSwitchItem(
                    icon = if (isGridView) Icons.Outlined.GridView else Icons.Outlined.ViewList,
                    title = "Grid View",
                    subtitle = "Show notes in a grid layout",
                    checked = isGridView,
                    onCheckedChange = {
                        preferencesManager.setGridView(it)
                    }
                )
            }

            // --- Notes Behavior ---
            SettingsGroup(title = "Notes Behavior") {
                // Sort Option
                SettingsItem(
                    icon = Icons.Outlined.Sort,
                    title = "Sort Notes By",
                    subtitle = when(currentSortOrder) {
                        is NoteOrder.Title -> "Title"
                        is NoteOrder.Date -> "Date Created"
                        is NoteOrder.Color -> "Color"
                    },
                    onClick = { showSortDialog = true }
                )

                SettingsSwitchItem(
                    icon = Icons.Outlined.VerticalAlignTop,
                    title = "New notes to top",
                    subtitle = "Add new notes to the top of the list",
                    checked = true,
                    onCheckedChange = { /* TODO */ }
                )
            }

            // --- Security ---
            SettingsGroup(title = "Security") {
                SettingsSwitchItem(
                    icon = Icons.Outlined.Lock,
                    title = "App Lock",
                    subtitle = "Biometric/PIN authentication",
                    checked = isLockEnabled,
                    onCheckedChange = {
                        isLockEnabled = it
                        preferencesManager.setAppLockEnabled(it)
                    }
                )
            }

            // --- Backup & Sync ---
            SettingsGroup(title = "Backup & Sync") {
                SettingsItem(
                    icon = Icons.Outlined.CloudUpload,
                    title = "Google Drive Backup",
                    subtitle = "Sync notes to cloud",
                    onClick = { /* TODO */ }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                SettingsItem(
                    icon = Icons.Outlined.Save,
                    title = "Local Backup",
                    subtitle = "Create a backup file on device",
                    onClick = { /* TODO */ }
                )
            }

            // --- About ---
            SettingsGroup(title = "About") {
                SettingsItem(
                    icon = Icons.Outlined.Info,
                    title = "Version",
                    subtitle = "1.0.0 (Production Build)",
                    onClick = { }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }

        // Theme Dialog
        if (showThemeDialog) {
            AlertDialog(
                onDismissRequest = { showThemeDialog = false },
                title = { Text("Choose Theme") },
                text = {
                    Column {
                        ThemeOption(0, "System Default", currentTheme) {
                            currentTheme = 0
                            preferencesManager.setThemeMode(0)
                            showThemeDialog = false
                        }
                        ThemeOption(1, "Light Mode", currentTheme) {
                            currentTheme = 1
                            preferencesManager.setThemeMode(1)
                            showThemeDialog = false
                        }
                        ThemeOption(2, "Dark Mode", currentTheme) {
                            currentTheme = 2
                            preferencesManager.setThemeMode(2)
                            showThemeDialog = false
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showThemeDialog = false }) { Text("Cancel") }
                }
            )
        }

        // Sort Dialog
        if (showSortDialog) {
            AlertDialog(
                onDismissRequest = { showSortDialog = false },
                title = { Text("Sort By") },
                text = {
                    Column {
                        val currentType = currentSortOrder.orderType

                        SortOption("Title", currentSortOrder is NoteOrder.Title) {
                            preferencesManager.setSortOrder(NoteOrder.Title(currentType))
                            showSortDialog = false
                        }
                        SortOption("Date Created", currentSortOrder is NoteOrder.Date) {
                            preferencesManager.setSortOrder(NoteOrder.Date(currentType))
                            showSortDialog = false
                        }
                        SortOption("Color", currentSortOrder is NoteOrder.Color) {
                            preferencesManager.setSortOrder(NoteOrder.Color(currentType))
                            showSortDialog = false
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                        SortOption("Ascending", currentType is OrderType.Ascending) {
                            preferencesManager.setSortOrder(currentSortOrder.copy(OrderType.Ascending))
                            showSortDialog = false
                        }
                        SortOption("Descending", currentType is OrderType.Descending) {
                            preferencesManager.setSortOrder(currentSortOrder.copy(OrderType.Descending))
                            showSortDialog = false
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showSortDialog = false }) { Text("Cancel") }
                }
            )
        }
    }
}

// --- Reusable Components ---

@Composable
fun SettingsGroup(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column {
        Text(text = title, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(start = 8.dp, bottom = 8.dp))
        Card(shape = MaterialTheme.shapes.large, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(vertical = 4.dp)) { content() }
        }
    }
}

@Composable
fun SettingsItem(icon: ImageVector, title: String, subtitle: String? = null, onClick: () -> Unit) {
    ListItem(
        modifier = Modifier.clickable { onClick() },
        headlineContent = { Text(title, fontWeight = FontWeight.Medium) },
        supportingContent = if (subtitle != null) { { Text(subtitle) } } else null,
        leadingContent = { Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
        colors = ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent)
    )
}

@Composable
fun SettingsSwitchItem(icon: ImageVector, title: String, subtitle: String? = null, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    ListItem(
        headlineContent = { Text(title, fontWeight = FontWeight.Medium) },
        supportingContent = if (subtitle != null) { { Text(subtitle) } } else null,
        leadingContent = { Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
        trailingContent = { Switch(checked = checked, onCheckedChange = onCheckedChange) },
        colors = ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent)
    )
}

@Composable
fun ThemeOption(id: Int, text: String, currentSelection: Int, onSelect: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().clickable { onSelect() }.padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        RadioButton(selected = (id == currentSelection), onClick = onSelect)
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = text)
    }
}

@Composable
fun SortOption(text: String, selected: Boolean, onSelect: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onSelect() }.padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onSelect)
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = text)
    }
}