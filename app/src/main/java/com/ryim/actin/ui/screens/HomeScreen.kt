package com.ryim.actin.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.ryim.actin.R
import com.ryim.actin.domain.ExerciseEntry
import com.ryim.actin.ui.ExAddPrefill
import com.ryim.actin.ui.HomeScreenViewModel
import com.ryim.actin.ui.SharedExAddViewModel

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


    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(
                        modifier = Modifier.fillMaxWidth()
                            .padding(start = 12.dp),
                    ) {
                        Text(
                            "Actin",
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                },
                navigationIcon = {
                    Image(
                        painter = painterResource(R.drawable.logo8),
                        contentDescription = "App icon",
                        modifier = Modifier
                            .padding(start = 12.dp)
                            .size(54.dp)
                    )

                },
                actions = {
                    // Button to add new exercise
                    Box(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.primary)
                            .padding(end = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
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
                                        workout = null
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
                                "New",
                                color = MaterialTheme.colorScheme.onSecondary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        },

        bottomBar = {

            var selectedItem by remember { mutableIntStateOf(0) }

            val navBarItemColors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.secondary,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                selectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
            )

            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 4.dp
            ) {

                // Home
                NavigationBarItem(
                    selected = selectedItem == 0,
                    onClick = onHome,
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home") },
                    colors = navBarItemColors
                )

                // Progress
                NavigationBarItem(
                    selected = selectedItem == 1,
                    onClick = onProgress,
                    icon = { Icon(Icons.Default.BarChart, contentDescription = "Home") },
                    label = { Text("Progress") },
                    colors = navBarItemColors
                )

                // Exercise configuration
                NavigationBarItem(
                    selected = selectedItem == 2,
                    onClick = onExercise,
                    icon = { Icon(Icons.Default.EditNote, contentDescription = "Placeholder 2") },
                    label = { Text("Workouts") },
                    colors = navBarItemColors
                )

                // --- Settings ---
                NavigationBarItem(
                    selected = selectedItem == 4,
                    onClick = onSettings,
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                    label = { Text("Settings") },
                    colors = navBarItemColors
                )
            }
        }

    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            Text(
                "Last seven days",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.align(Alignment.Start)
                    .padding(horizontal = 16.dp)
            )

            HorizontalDivider(
                modifier = Modifier
                    .padding(horizontal = 16.dp), thickness = 1.dp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
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

                Spacer(modifier = Modifier.width(8.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                "Recent activity",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.align(Alignment.Start)
                    .padding(horizontal = 16.dp)
            )

            HorizontalDivider(
                modifier = Modifier
                    .padding(horizontal = 16.dp), thickness = 1.dp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
            )

            // History list for the exercises
            var entryToDelete by remember { mutableStateOf<ExerciseEntry?>(null) }

            LazyColumn {
                items(uiState.latestExercises) { entry ->
                    ExerciseHistoryRow(
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
                        workout = null
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
