package com.ck.quicknote.feature.home.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.ColorUtils
import coil.compose.AsyncImage
import com.ck.quicknote.core.common.DateUtils
import com.ck.quicknote.domain.model.Note

@Composable
fun NoteItem(
    note: Note,
    modifier: Modifier = Modifier,
    onDeleteClick: () -> Unit
) {
    val formattedDate = DateUtils.getRelativeTime(note.timestamp)
    val isDarkTheme = isSystemInDarkTheme()

    // 1. Color Logic (Original Style Restored)
    val noteColor = Color(note.color)

    // Light Mode: Use Note Color directly (Pastel Colors)
    // Dark Mode: Use Surface Container color for better readability
    val containerColor = if (isDarkTheme) {
        MaterialTheme.colorScheme.surfaceContainer // Dark Grayish
    } else {
        noteColor // Original Pastel Color
    }

    // Text Color Logic
    val titleColor = if (isDarkTheme)
        MaterialTheme.colorScheme.onSurface
    else
        Color.Black.copy(alpha = 0.9f)

    val bodyColor = if (isDarkTheme)
        MaterialTheme.colorScheme.onSurfaceVariant
    else
        Color.Black.copy(alpha = 0.7f)

    val dateColor = if (isDarkTheme)
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
    else
        Color.Black.copy(alpha = 0.4f)

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            note.imageUri?.let { uri ->
                AsyncImage(
                    model = uri,
                    contentDescription = "Note Image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                if (note.title.isNotBlank()) {
                    Text(
                        text = note.title,
                        style = MaterialTheme.typography.titleMedium, // Global Font
                        color = titleColor,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }

                if (note.isPinned) {
                    Icon(
                        imageVector = Icons.Default.PushPin,
                        contentDescription = "Pinned",
                        tint = titleColor.copy(alpha = 0.5f),
                        modifier = Modifier
                            .size(18.dp)
                            .offset(x = 4.dp, y = (-2).dp)
                    )
                }
            }

            if (note.title.isNotBlank() && note.content.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
            }

            if (note.content.isNotBlank()) {
                Text(
                    text = note.content,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        lineHeight = 20.sp
                    ),
                    color = bodyColor,
                    maxLines = 6,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = formattedDate,
                style = MaterialTheme.typography.labelSmall,
                color = dateColor
            )
        }
    }
}