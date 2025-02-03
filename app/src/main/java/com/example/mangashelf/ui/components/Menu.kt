package com.example.mangashelf.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.mangashelf.ui.viewmodel.SortType

@Composable
fun SortDropdownMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    onSortSelected: (SortType) -> Unit
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss
    ) {
        SortMenuItem(
            text = "By Year",
            icon = Icons.Default.DateRange,
            onClick = { onSortSelected(SortType.YEAR_ASC) }
        )
        Divider()
        SortMenuItem(
            text = "Score (High to Low)",
            onClick = { onSortSelected(SortType.SCORE_DESC) }
        )
        SortMenuItem(
            text = "Score (Low to High)",
            onClick = { onSortSelected(SortType.SCORE_ASC) }
        )
        SortMenuItem(
            text = "Popularity (High to Low)",
            onClick = { onSortSelected(SortType.POPULARITY_DESC) }
        )
        SortMenuItem(
            text = "Popularity (Low to High)",
            onClick = { onSortSelected(SortType.POPULARITY_ASC) }
        )
    }
}

@Composable
fun SortMenuItem(
    text: String,
    icon: ImageVector? = null,
    onClick: () -> Unit
) {
    DropdownMenuItem(
        text = { Text(text) },
        onClick = onClick,
        leadingIcon = icon?.let { { Icon(it, contentDescription = null) } }
    )
}