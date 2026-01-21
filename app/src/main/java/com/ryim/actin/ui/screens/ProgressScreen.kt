package com.ryim.actin.ui.screens

import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.ryim.actin.R
import com.ryim.actin.domain.ExerciseEntry
import com.ryim.actin.domain.formatTimestampPretty
import com.ryim.actin.ui.ExAddPrefill
import com.ryim.actin.ui.FullHistoryUIState
import com.ryim.actin.ui.ProgressScreenViewModel
import com.ryim.actin.ui.SharedExAddViewModel
import com.ryim.actin.ui.WeeklyCount
import kotlinx.datetime.LocalDate
import kotlinx.datetime.format
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.char

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressScreen(
    onHome: () -> Unit,
    onProgress: () -> Unit,
    onExercise: () -> Unit,
    onSettings: () -> Unit,
    sharedExAddViewModel: SharedExAddViewModel,
    onNavigateToExAdd: () -> Unit,
    onDeleteExercise: (ExerciseEntry) -> Unit,
    viewModel: ProgressScreenViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Graphs", "Full history")

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
                            "Progress",
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                },
                navigationIcon = {
                    Icon(
                        painter = painterResource(R.drawable.logo8),
                        contentDescription = "App icon",
                        tint = Color.Unspecified,
                        modifier = Modifier
                            .padding(start = 12.dp)
                            .size(54.dp)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        },

        bottomBar = {

            var selectedItem by remember { mutableIntStateOf(1) }

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
                    icon = { Icon(Icons.Default.BarChart, contentDescription = "Progress") },
                    label = { Text("Progress") },
                    colors = navBarItemColors
                )

                // Exercise configuration
                NavigationBarItem(
                    selected = selectedItem == 2,
                    onClick = onExercise,
                    icon = { Icon(Icons.Default.EditNote, contentDescription = "Workouts") },
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
                .fillMaxSize()
        ) {

            SecondaryTabRow(
                selectedTabIndex = selectedTab,
                indicator = {
                    SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(selectedTab),
                        color = MaterialTheme.colorScheme.secondary,
                        height = 5.dp
                    )
                },
                divider = {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.primary,
                        thickness = 2.dp
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }

            when (selectedTab) {
                0 -> GraphsTab(
                    uiState = uiState
                )
                1 -> FullHistoryTab(
                    uiState = uiState,
                    sharedExAddViewModel = sharedExAddViewModel,
                    onNavigateToExAdd = onNavigateToExAdd,
                    onDeleteExercise = onDeleteExercise
                )
            }
        }
    }
}

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


@Composable
fun GraphsTab(
    uiState: FullHistoryUIState,
    ) {
    Column(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            "Recent exercises",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.align(Alignment.Start)
                .padding(horizontal = 16.dp)
        )

        HorizontalDivider(
            modifier = Modifier
                .padding(horizontal = 16.dp), thickness = 1.dp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        WeeklyBarChart(
            barColor = MaterialTheme.colorScheme.secondary,
            labelColor = MaterialTheme.colorScheme.onSurface,
            data = uiState.weeklyCounts,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(16.dp)
        )

                // Your existing list UI using uiState.allExercises

    }
}

@Composable
fun WeeklyBarChart(
    data: List<WeeklyCount>,
    modifier: Modifier = Modifier,
    barColor: Color = Color(0xFF4CAF50),
    labelColor: Color = Color.DarkGray,
    textSize: Float = 32f
) {
    if (data.isEmpty()) return

    val maxCount = data.maxOf { it.count }.coerceAtLeast(1)

    // Tooltip state
    var selectedIndex by remember { mutableStateOf<Int?>(null) }

    val textPaint = remember {
        Paint().asFrameworkPaint().apply {
            isAntiAlias = true
            color = labelColor.toArgb()
            textAlign = android.graphics.Paint.Align.CENTER
            this.textSize = textSize
        }
    }

    Canvas(
        modifier = modifier
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        val pos = event.changes.firstOrNull()?.position ?: continue

                        // Compute layout constants here too
                        val chartLeftPadding = 80f
                        val chartBottomPadding = 60f
                        val chartWidth = size.width - chartLeftPadding
                        val barWidth = chartWidth / data.size

                        val x = pos.x - chartLeftPadding
                        if (x >= 0f) {
                            val index = (x / barWidth).toInt()
                            if (index in data.indices) {
                                selectedIndex = index
                            }
                        }

                        event.changes.forEach { it.consume() }
                    }
                }
            }
    ) {
        val chartLeftPadding = 80f
        val chartBottomPadding = 60f

        val chartWidth = size.width - chartLeftPadding
        val chartHeight = size.height - chartBottomPadding

        val barWidth = chartWidth / data.size
        val heightPerUnit = chartHeight / maxCount

        // --- Draw bars + X-axis labels ---
        data.forEachIndexed { index, week ->
            val barHeight = week.count * heightPerUnit

            val left = chartLeftPadding + index * barWidth
            val top = chartHeight - barHeight

            // Bar
            drawRect(
                color = barColor,
                topLeft = Offset(left, top),
                size = Size(barWidth * 0.8f, barHeight)
            )

            // X-axis label (week start date)
            val label = week.weekStart.format(
                LocalDate.Format {
                    dayOfMonth()
                    char(' ')
                    monthName(MonthNames.ENGLISH_ABBREVIATED)
                }
            )

            // Compute the center of the bar
            val barCenter = left + (barWidth * 0.4f)
            drawContext.canvas.nativeCanvas.save()
            drawContext.canvas.nativeCanvas.translate(barCenter, size.height - 20f) // Move pivot to where the label should end (right-aligned)
            drawContext.canvas.nativeCanvas.rotate(-30f)    // Rotate around that pivot
            textPaint.textAlign = android.graphics.Paint.Align.RIGHT    // Draw text with RIGHT alignment
            drawContext.canvas.nativeCanvas.drawText(
                label,
                0f,
                0f,
                textPaint.apply { color = labelColor.toArgb() },
            )
            drawContext.canvas.nativeCanvas.restore()
        }

        // --- Draw Y-axis grid lines + labels ---
        val maxTicks = 4
        val rawStep = (maxCount.toFloat() / maxTicks).ceilToInt().coerceAtLeast(1)
        val maxLabelValue = ((maxCount + rawStep - 1) / rawStep) * rawStep
        for (value in 0..maxLabelValue step rawStep) {
            val y = chartHeight - (value * heightPerUnit)

            // Grid line
            drawLine(
                color = Color.LightGray.copy(alpha = 0.4f),
                start = Offset(chartLeftPadding, y),
                end = Offset(size.width, y),
                strokeWidth = 2f
            )

            // Y-axis label
            drawContext.canvas.nativeCanvas.drawText(
                value.toString(),
                chartLeftPadding - 30f,
                y + 10f,
                textPaint
            )
        }

        // Draw tooltip if a bar is selected
        selectedIndex?.let { index ->
            val week = data[index]
            val barHeight = week.count * heightPerUnit
            val left = chartLeftPadding + index * barWidth + (barWidth * 0.4f)
            val top = chartHeight - barHeight

            // Tooltip background
            val tooltipWidth = 120f
            val tooltipHeight = 60f

            drawRoundRect(
                color = Color.Black.copy(alpha = 0.8f),
                topLeft = Offset(left - tooltipWidth / 2, top - tooltipHeight - 20f),
                size = Size(tooltipWidth, tooltipHeight),
                cornerRadius = CornerRadius(12f, 12f)
            )

            // Tooltip text
            drawContext.canvas.nativeCanvas.drawText(
                "${week.count}",
                left,
                top - tooltipHeight / 2 - 10f,
                textPaint.apply { color = Color.White.toArgb() }
            )
        }
    }
}

private fun Float.ceilToInt(): Int = kotlin.math.ceil(this).toInt()
