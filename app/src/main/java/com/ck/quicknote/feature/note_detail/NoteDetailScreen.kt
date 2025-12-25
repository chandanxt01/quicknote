package com.ck.quicknote.feature.note_detail

import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import android.speech.RecognizerIntent
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.ck.quicknote.R
import com.ck.quicknote.core.common.UiEvent
import com.ck.quicknote.domain.model.Note
import com.ck.quicknote.feature.note_detail.components.TransparentHintTextField
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteDetailScreen(
    navController: NavController,
    noteColor: Int,
    viewModel: NoteDetailViewModel = hiltViewModel()
) {
    val titleState = viewModel.state.value.noteTitle
    val contentState = viewModel.state.value.noteContent
    val state = viewModel.state.value
    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // UI States for Sheets
    var showColorSheet by remember { mutableStateOf(false) }
    var showFolderSheet by remember { mutableStateOf(false) }

    // Identify current folder name for TopBar
    val currentFolderName = state.folders.find { it.id == state.folderId }?.name ?: "Select Folder"

    BackHandler {
        viewModel.onEvent(NoteDetailEvent.SaveNote)
    }

    // --- Pickers & Launchers ---
    val timePickerDialog = TimePickerDialog(
        context,
        { _, hourOfDay, minute ->
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
            calendar.set(Calendar.MINUTE, minute)
            viewModel.onEvent(NoteDetailEvent.SetReminder(calendar.timeInMillis))
        },
        calendar.get(Calendar.HOUR_OF_DAY),
        calendar.get(Calendar.MINUTE),
        false
    )

    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            timePickerDialog.show()
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.onEvent(NoteDetailEvent.ChangeImage(it.toString())) }
    }

    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            val resultText = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.get(0)
            resultText?.let { text ->
                val currentText = contentState.text
                val newText = if (currentText.isBlank()) text else "$currentText $text"
                viewModel.onEvent(NoteDetailEvent.EnteredContent(newText))
            }
        }
    }

    val noteBackgroundAnimatable = remember {
        Animatable(Color(if (noteColor != -1) noteColor else viewModel.state.value.noteColor))
    }

    LaunchedEffect(key1 = true) {
        viewModel.eventFlow.collectLatest { event ->
            when(event) {
                is UiEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(message = event.message)
                }
                is UiEvent.PopBackStack -> {
                    navController.navigateUp()
                }
                else -> {}
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    // Folder Selector in Title
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { showFolderSheet = true }
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = currentFolderName,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Select Folder"
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.onEvent(NoteDetailEvent.SaveNote) }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = stringResource(id = R.string.back_desc)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                actions = {
                    IconButton(onClick = { viewModel.onEvent(NoteDetailEvent.TogglePin) }) {
                        Icon(
                            imageVector = if (state.isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                            contentDescription = "Pin",
                            tint = if (state.isPinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            )
        },
        bottomBar = {
            BottomAppBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 3.dp
            ) {
                IconButton(onClick = { showColorSheet = true }) {
                    Icon(Icons.Outlined.Palette, contentDescription = "Color")
                }
                IconButton(onClick = { galleryLauncher.launch("image/*") }) {
                    Icon(Icons.Outlined.Image, contentDescription = "Image")
                }
                IconButton(onClick = {
                    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                    }
                    try { speechLauncher.launch(intent) } catch (e: Exception) { }
                }) {
                    Icon(Icons.Outlined.Mic, contentDescription = "Voice")
                }

                Spacer(modifier = Modifier.weight(1f))

                IconButton(onClick = { datePickerDialog.show() }) {
                    Icon(
                        imageVector = if (state.reminderTime != null) Icons.Filled.NotificationsActive else Icons.Outlined.Notifications,
                        contentDescription = "Reminder",
                        tint = if (state.reminderTime != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                }
                IconButton(onClick = { viewModel.onEvent(NoteDetailEvent.ToggleArchive) }) {
                    Icon(
                        imageVector = if (state.isArchived) Icons.Filled.Archive else Icons.Outlined.Archive,
                        contentDescription = "Archive",
                        tint = if (state.isArchived) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                }
                IconButton(onClick = { viewModel.onEvent(NoteDetailEvent.DeleteNote) }) {
                    Icon(Icons.Outlined.Delete, contentDescription = "Delete")
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(noteBackgroundAnimatable.value)
                .padding(padding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            state.reminderTime?.let { time ->
                SuggestionChip(
                    onClick = { viewModel.onEvent(NoteDetailEvent.SetReminder(null)) },
                    label = { Text("Reminder: ${java.text.SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(time))}") },
                    icon = { Icon(Icons.Default.Alarm, contentDescription = null) },
                    colors = SuggestionChipDefaults.suggestionChipColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            state.noteImageUri?.let { uri ->
                AsyncImage(
                    model = uri,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            TransparentHintTextField(
                text = titleState.text,
                hint = stringResource(id = R.string.enter_title_hint),
                onValueChange = { viewModel.onEvent(NoteDetailEvent.EnteredTitle(it)) },
                onFocusChange = { viewModel.onEvent(NoteDetailEvent.ChangeTitleFocus(it)) },
                isHintVisible = titleState.isHintVisible,
                singleLine = false,
                textStyle = TextStyle(
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )

            Text(
                text = "Last edited: Now",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            TransparentHintTextField(
                text = contentState.text,
                hint = stringResource(id = R.string.enter_content_hint),
                onValueChange = { viewModel.onEvent(NoteDetailEvent.EnteredContent(it)) },
                onFocusChange = { viewModel.onEvent(NoteDetailEvent.ChangeContentFocus(it)) },
                isHintVisible = contentState.isHintVisible,
                textStyle = TextStyle(
                    fontSize = 18.sp,
                    lineHeight = 28.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 400.dp)
            )

            Spacer(modifier = Modifier.height(100.dp))
        }

        // --- Color Picker Sheet ---
        if (showColorSheet) {
            ModalBottomSheet(
                onDismissRequest = { showColorSheet = false },
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Choose Background Color", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        Note.noteColors.forEach { color ->
                            val colorInt = color.toArgb()
                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                    .shadow(5.dp, CircleShape)
                                    .clip(CircleShape)
                                    .background(color)
                                    .border(width = 2.dp, color = if (state.noteColor == colorInt) MaterialTheme.colorScheme.primary else Color.Transparent, shape = CircleShape)
                                    .clickable {
                                        scope.launch { noteBackgroundAnimatable.animateTo(targetValue = color, animationSpec = tween(durationMillis = 500)) }
                                        viewModel.onEvent(NoteDetailEvent.ChangeColor(colorInt))
                                        showColorSheet = false
                                    }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }

        // --- Folder Selection Sheet ---
        if (showFolderSheet) {
            ModalBottomSheet(
                onDismissRequest = { showFolderSheet = false },
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Move to Folder", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 16.dp))
                    LazyColumn {
                        items(state.folders) { folder ->
                            val isSelected = state.folderId == folder.id
                            ListItem(
                                headlineContent = { Text(folder.name) },
                                leadingContent = {
                                    Icon(
                                        imageVector = if (isSelected) Icons.Default.FolderOpen else Icons.Outlined.Folder,
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
                                    folder.id?.let {
                                        viewModel.onEvent(NoteDetailEvent.ChangeFolder(it))
                                        showFolderSheet = false
                                    }
                                }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}