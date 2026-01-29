package com.ryim.actin.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.ryim.actin.domain.ExerciseEntry
import com.ryim.actin.domain.formattedDate
import com.ryim.actin.domain.localDate
import com.ryim.actin.ui.ExAddPrefill
import com.ryim.actin.ui.HomeScreenViewModel
import com.ryim.actin.ui.ReusableComposables.AppBottomBar
import com.ryim.actin.ui.ReusableComposables.AppTopBar
import com.ryim.actin.ui.ReusableComposables.SectionHeader
import com.ryim.actin.ui.SharedExAddViewModel
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.until
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToExAdd: () -> Unit,
    onDeleteExercise: (ExerciseEntry) -> Unit,
    onHome: () -> Unit,
    onProgress: () -> Unit,
    onExercise: () -> Unit,
    onSettings: () -> Unit,
    sharedExAddViewModel: SharedExAddViewModel,
    viewModel: HomeScreenViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    //  Refreshing apparatus
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refresh()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    //  Display only a loading screen rather than the empty data
    if (uiState.isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceContainer),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.secondary,
                trackColor = MaterialTheme.colorScheme.surfaceContainer
            )
        }
        return
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Actin"
            )
        },

        bottomBar = {
            AppBottomBar(
                selectedItem = 0,
                onHome = onHome,
                onProgress = onProgress,
                onExercise = onExercise,
                onSettings = onSettings
            )
        }

    ) { innerPadding ->
        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
//            Spacer(modifier = Modifier.height(32.dp))

            SectionHeader("Last 7 days")

//            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.width(90.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row {
                        WeeklyNumberSummary(
                            label = "Workouts",
                            thisWeekValue = uiState.thisWeekWorkoutCount,
                            lastWeekValue = uiState.lastWeekWorkoutCount
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .width(90.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row() {
                        WeeklyNumberSummary(
                            label = "Exercises",
                            thisWeekValue = uiState.thisWeekExercises.size,
                            lastWeekValue = uiState.lastWeekExercises.size
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .width(90.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row() {
                        WeeklySetsSummary(
                            thisWeekExercises = uiState.thisWeekExercises,
                            lastWeekExercises = uiState.lastWeekExercises
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .width(120.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row() {
                        WeeklyRepsSummary(
                            thisWeekExercises = uiState.thisWeekExercises,
                            lastWeekExercises = uiState.lastWeekExercises
                        )
                    }
                }

//                Spacer(modifier = Modifier.width(8.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))


            Button(
                onClick = { onExercise() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary, // different from box
                    contentColor = Color.White
                )
            ) {
                Text(
                    "Start a workout",
                    color = MaterialTheme.colorScheme.onSecondary
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = {
                    sharedExAddViewModel.setPrefill(
                        ExAddPrefill(
                            name = "",
                            sets = 3,
                            reps = List(3) { 8 },
                            weights = List(3) { "" },
                            useKg = true,
                            editMode = false,
                            timestamp = null,
                            workout = null,
                            id = UUID.randomUUID().toString()
                        )
                    )
                    onNavigateToExAdd()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary, // different from box
                    contentColor = Color.White
                )
            ) {
                Text(
                    "Start an individual exercise",
                    color = MaterialTheme.colorScheme.onSecondary
                )
            }

           SectionHeader("Recent activity")

            // History list for the exercises
            var entryToDelete by remember { mutableStateOf<ExerciseEntry?>(null) }
            val entries = uiState.latestExercises
            val zone = TimeZone.currentSystemDefault()
            val today = Clock.System.now().toLocalDateTime(zone).date

            entries.forEachIndexed { index, entry ->

                val entryDate = entry.localDate(zone)

                // Determine if we need a header
                val showHeader = when (index) {
                    0 -> true // always show header for the first item
                    else -> {
                        val prevDate = entries[index - 1].localDate(zone)
                        entryDate != prevDate
                    }
                }

                if (showHeader) {
                    val headerText = when (entryDate.daysUntil(today)) {
                        0 -> "Today"
                        1 -> "Yesterday"
                        else -> entry.formattedDate(zone)
                    }

                    Text(
                        text = headerText,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp)
                    )
                }

                // Your existing row
                ExerciseHistoryRow(
                    entry = entry,
                    sharedExAddViewModel = sharedExAddViewModel,
                    onNavigateToExAdd = onNavigateToExAdd,
                    onDeleteRequest = { entryToDelete = it }
                )

                // Divider logic (unchanged)
                if (index < entries.lastIndex) {
                    val currentTime = Instant.parse(entry.timestamp!!)
                    val nextTime = Instant.parse(entries[index + 1].timestamp!!)

                    val hoursBetween = nextTime.until(currentTime, DateTimeUnit.HOUR, TimeZone.UTC)

                    if (hoursBetween > 2) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            thickness = 1.dp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                        )
                    }
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
}

@Composable
fun ExerciseHistoryRow(
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
            Text(
                entry.name,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // How many items to show before truncating
            val maxItems = 4

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

        // Right side: the + button
        FilledIconButton(
            onClick = {
                sharedExAddViewModel.setPrefill(
                    ExAddPrefill(
                        name = entry.name,
                        sets = entry.sets,
                        reps = entry.reps,
                        weights = entry.weights.map { it.toString() },
                        useKg = entry.useKg,
                        editMode = false,          // 👈 ADD MODE
                        timestamp = entry.timestamp,
                        workout = null,
                        id = UUID.randomUUID().toString()
                    )
                )
                onNavigateToExAdd()
            },
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = MaterialTheme.colorScheme.secondary
            )
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add set")
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
                                editMode = true,
                                timestamp = entry.timestamp,
                                workout = null,
                                id = entry.id
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

@Composable
fun WeeklyNumberSummary(
    modifier: Modifier = Modifier,
    label: String,
    thisWeekValue: Int,
    lastWeekValue: Int,
//    numberWidth: Dp = 30.dp
) {
    val diff = thisWeekValue - lastWeekValue
    val diffText = if (diff >= 0) "+$diff" else diff.toString()

    val diffColor = when {
        diff > 0 -> Color(0xFF2E7D32)   // green
        diff < 0 -> Color(0xFFC62828)   // red
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(label, style = MaterialTheme.typography.labelMedium)

//        Row(
//            verticalAlignment = Alignment.CenterVertically,
//            horizontalArrangement = Arrangement.Center
//        ) {
            // Left item → right aligned
            Text(
                text = thisWeekValue.toString(),
                style = MaterialTheme.typography.titleLarge,
//                modifier = Modifier.width(numberWidth),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Right item → left aligned
            Text(
                text = diffText,
                style = MaterialTheme.typography.bodySmall,
                color = diffColor,
//                modifier = Modifier.width(numberWidth),
                textAlign = TextAlign.Center
            )
//        }

    }
}

fun List<ExerciseEntry>.totalSets(): Int =
    sumOf { it.sets }

fun List<ExerciseEntry>.totalReps(): Int =
    sumOf { entry -> entry.reps.sum() }


@Composable
fun WeeklySetsSummary(
    thisWeekExercises: List<ExerciseEntry>,
    lastWeekExercises: List<ExerciseEntry>,
    modifier: Modifier = Modifier
) {
    WeeklyNumberSummary(
        label = "Sets",
        thisWeekValue = thisWeekExercises.totalSets(),
        lastWeekValue = lastWeekExercises.totalSets(),
        modifier = modifier
    )
}

@Composable
fun WeeklyRepsSummary(
    thisWeekExercises: List<ExerciseEntry>,
    lastWeekExercises: List<ExerciseEntry>,
    modifier: Modifier = Modifier
) {
    WeeklyNumberSummary(
        label = "Reps",
        thisWeekValue = thisWeekExercises.totalReps(),
        lastWeekValue = lastWeekExercises.totalReps(),
        modifier = modifier
    )
}
