package com.ryim.actin.ui.screens.progressScreenTabs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ryim.actin.domain.ExerciseEntry
import com.ryim.actin.domain.formatTimestampPretty
import com.ryim.actin.ui.ExAddPrefill
import com.ryim.actin.ui.FullHistoryUIState
import com.ryim.actin.ui.SharedExAddViewModel

@Composable
fun FullHistoryTab(
    uiState: FullHistoryUIState,
    sharedExAddViewModel: SharedExAddViewModel,
    onNavigateToExAdd: () -> Unit,
    onDeleteExercise: (ExerciseEntry) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // History list for the exercises

        var entryToDelete by remember { mutableStateOf<ExerciseEntry?>(null) }

        LazyColumn {
            items(uiState.allExercises) { entry ->
                FullHistoryRow(
                    entry = entry,
                    sharedExAddViewModel = sharedExAddViewModel,
                    onNavigateToExAdd = onNavigateToExAdd,
                    onDeleteRequest = { entryToDelete = it }
                )

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                )
            }
        }
        if (entryToDelete != null) {
            AlertDialog(
                onDismissRequest = { entryToDelete = null },
                title = { Text("Delete exercise?") },
                text = { Text("This action cannot be undone.") },
                confirmButton = {
                    TextButton(onClick = {
                        onDeleteExercise(entryToDelete!!)
                        entryToDelete = null
                    }) { Text("Delete") }
                },
                dismissButton = {
                    TextButton(onClick = { entryToDelete = null }) { Text("Cancel") }
                }
            )
        }
    }
}

@Composable
fun FullHistoryRow(
    entry: ExerciseEntry,
    sharedExAddViewModel: SharedExAddViewModel,
    onNavigateToExAdd: () -> Unit,
    onDeleteRequest: (ExerciseEntry) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {

        // Left side: your reps/weights table
        Column (
            modifier = Modifier.weight(1f) // <-- THIS FIXES THE OVERFLOW
        ){
            // LEFT SIDE: Name + Date on same row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                // Exercise name (takes all remaining space)
                Text(
                    text = entry.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                // Date (fixed width, right aligned)
                Text(
                    text = formatTimestampPretty(entry.timestamp.toString()),
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.End,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            // How many items to show before truncating
            val maxItems = 7

            // ----- REPS ROW -----
            Row(verticalAlignment = Alignment.CenterVertically) {

                Text(
                    "Reps",
                    modifier = Modifier.width(40.dp),
                    style = MaterialTheme.typography.bodySmall
                )

                val repsToShow = entry.reps.take(maxItems)
                val repsOverflow = entry.reps.size > maxItems

                repsToShow.forEach { rep ->
                    Text(
                        rep.toString(),
                        modifier = Modifier.width(40.dp),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                if (repsOverflow) {
                    Text(
                        "…",
                        modifier = Modifier.width(40.dp),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            // ----- WEIGHTS ROW -----
            Row(verticalAlignment = Alignment.CenterVertically) {

                fun formatWeight(value: Float): String {
                    return when {
                        value == 0f -> "-"                                   // Show "-" for 0.0
                        value % 1f == 0f -> value.toInt().toString()         // Show integer if ends with .0
                        else -> String.format("%.1f", value)                 // Otherwise show 1 decimal place
                    }
                }

                var weightHeader = "lb"
                if (entry.useKg) {
                    weightHeader = "kg"
                }

                Text(
                    weightHeader,
                    modifier = Modifier.width(40.dp),
                    style = MaterialTheme.typography.bodySmall
                )

                val weightsToShow = entry.weights.take(maxItems)
                val weightsOverflow = entry.weights.size > maxItems

                weightsToShow.forEach { weight ->
                    Text(
                        formatWeight(weight),
                        modifier = Modifier.width(40.dp),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                if (weightsOverflow) {
                    Text(
                        "",
                        modifier = Modifier.width(40.dp),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        var menuExpanded by remember { mutableStateOf(false) }

        Box {
            IconButton(onClick = { menuExpanded = true }) {
                Icon(Icons.Default.MoreVert, contentDescription = "Menu")
            }

            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false }
            ) {

                // Add
                DropdownMenuItem(
                    text = { Text("Add") },
                    onClick = {
                        menuExpanded = false

                        sharedExAddViewModel.setPrefill(
                            ExAddPrefill(
                                name = entry.name,
                                sets = entry.sets,
                                reps = entry.reps,
                                weights = entry.weights.map { it.toString() },
                                useKg = entry.useKg,
                                editMode = false,   // ADD mode
                                timestamp = entry.timestamp,
                                workout = null
                            )
                        )

                        onNavigateToExAdd()
                    }
                )

                // Edit
                DropdownMenuItem(
                    text = { Text("Edit") },
                    onClick = {
                        menuExpanded = false

                        sharedExAddViewModel.setPrefill(
                            ExAddPrefill(
                                name = entry.name,
                                sets = entry.sets,
                                reps = entry.reps,
                                weights = entry.weights.map { it.toString() },
                                useKg = entry.useKg,
                                editMode = true,    // EDIT mode
                                timestamp = entry.timestamp,
                                workout = null
                            )
                        )

                        onNavigateToExAdd()
                    }
                )

                // Delete
                DropdownMenuItem(
                    text = { Text("Delete") },
                    onClick = {
                        menuExpanded = false
                        onDeleteRequest(entry)
                    }
                )
            }
        }
    }
}
