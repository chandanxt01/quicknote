package com.ck.quicknote.feature.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.ck.quicknote.R
import com.ck.quicknote.domain.model.Note
import com.ck.quicknote.feature.note_detail.components.TransparentHintTextField
import com.ck.quicknote.feature.util.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    navController: NavController,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val state = viewModel.state.value
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Scaffold(
        topBar = {
            Column {
                // Search Bar Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = stringResource(id = R.string.back_desc)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TransparentHintTextField(
                        text = state.searchQuery,
                        hint = stringResource(id = R.string.search_hint),
                        onValueChange = {
                            viewModel.onEvent(SearchEvent.EnteredQuery(it))
                        },
                        onFocusChange = { },
                        isHintVisible = state.searchQuery.isBlank(),
                        singleLine = true,
                        textStyle = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.focusRequester(focusRequester)
                    )
                }

                // Filter Chips Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    FilterChip(
                        selected = state.isPinnedFilterActive,
                        onClick = { viewModel.onEvent(SearchEvent.TogglePinnedFilter) },
                        label = { Text("Pinned") },
                        leadingIcon = {
                            if (state.isPinnedFilterActive) {
                                Icon(Icons.Default.PushPin, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    FilterChip(
                        selected = state.isImageFilterActive,
                        onClick = { viewModel.onEvent(SearchEvent.ToggleImageFilter) },
                        label = { Text("Has Image") },
                        leadingIcon = {
                            if (state.isImageFilterActive) {
                                Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        }
                    )
                }
                HorizontalDivider()
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            LazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.Adaptive(160.dp),
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalItemSpacing = 16.dp
            ) {
                items(state.notes) { note ->
                    SearchNoteItem(
                        note = note,
                        searchQuery = state.searchQuery,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                navController.navigate(
                                    Screen.NoteDetailScreen.route +
                                            "?noteId=${note.id}&noteColor=${note.color}"
                                )
                            }
                    )
                }
            }
        }
    }
}

@Composable
fun SearchNoteItem(
    note: Note,
    searchQuery: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(note.color)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Highlighted Title
            Text(
                text = getHighlightedText(note.title, searchQuery, MaterialTheme.colorScheme.primary),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Highlighted Content
            Text(
                text = getHighlightedText(note.content, searchQuery, MaterialTheme.colorScheme.primary),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 5,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// Function to highlight matched text
@Composable
fun getHighlightedText(text: String, query: String, highlightColor: Color): androidx.compose.ui.text.AnnotatedString {
    if (query.isBlank()) return buildAnnotatedString { append(text) }

    return buildAnnotatedString {
        val lowerText = text.lowercase()
        val lowerQuery = query.lowercase()
        var startIndex = 0

        while (true) {
            val index = lowerText.indexOf(lowerQuery, startIndex)
            if (index == -1) {
                append(text.substring(startIndex))
                break
            }

            // Append non-matching part
            append(text.substring(startIndex, index))

            // Append matching part with Highlight Style
            withStyle(style = SpanStyle(
                background = highlightColor.copy(alpha = 0.3f),
                fontWeight = FontWeight.ExtraBold
            )) {
                append(text.substring(index, index + query.length))
            }

            startIndex = index + query.length
        }
    }
}