package com.ck.quicknote.feature.home.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.SortByAlpha
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.ck.quicknote.domain.util.NoteOrder
import com.ck.quicknote.domain.util.OrderType

@Composable
fun OrderSection(
    modifier: Modifier = Modifier,
    noteOrder: NoteOrder = NoteOrder.Date(OrderType.Descending),
    onOrderChange: (NoteOrder) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        // Section Title
        Text(
            text = "Sort By",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )

        // 1. Sort Type Chips (Horizontal Row)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SortFilterChip(
                text = "Title",
                icon = Icons.Default.SortByAlpha,
                selected = noteOrder is NoteOrder.Title,
                onClick = { onOrderChange(NoteOrder.Title(noteOrder.orderType)) }
            )
            SortFilterChip(
                text = "Date",
                icon = Icons.Default.CalendarToday,
                selected = noteOrder is NoteOrder.Date,
                onClick = { onOrderChange(NoteOrder.Date(noteOrder.orderType)) }
            )
            SortFilterChip(
                text = "Color",
                icon = Icons.Default.ColorLens,
                selected = noteOrder is NoteOrder.Color,
                onClick = { onOrderChange(NoteOrder.Color(noteOrder.orderType)) }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 2. Order Direction Chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SortFilterChip(
                text = "Ascending",
                icon = Icons.Default.ArrowUpward,
                selected = noteOrder.orderType is OrderType.Ascending,
                onClick = { onOrderChange(noteOrder.copy(OrderType.Ascending)) }
            )
            SortFilterChip(
                text = "Descending",
                icon = Icons.Default.ArrowDownward,
                selected = noteOrder.orderType is OrderType.Descending,
                onClick = { onOrderChange(noteOrder.copy(OrderType.Descending)) }
            )
        }
    }
}

// Reusable Modern Chip Component
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SortFilterChip(
    text: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(text) },
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
        },
        colors = FilterChipDefaults.filterChipColors(
            containerColor = Color.Transparent,
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
            selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
            selectedBorderColor = Color.Transparent,
            borderWidth = 1.dp
        ),
        shape = MaterialTheme.shapes.small
    )
}


