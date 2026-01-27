package com.ryim.actin.ui.screens.progressScreenTabs

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.ryim.actin.ui.DataPoint
import com.ryim.actin.ui.FullHistoryUIState
import com.ryim.actin.ui.WeeklyCount
import kotlinx.datetime.LocalDate
import kotlinx.datetime.format
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.char
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import com.ryim.actin.ui.MetricType
import com.ryim.actin.ui.ProgressScreenViewModel
import com.ryim.actin.ui.ReusableComposables.SectionHeader
import com.ryim.actin.ui.TimePeriod
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.daysUntil
import kotlinx.datetime.plus
import kotlin.math.ceil
import kotlin.math.roundToInt

@Composable
fun GraphsTab(
    viewModel: ProgressScreenViewModel,
    uiState: FullHistoryUIState,
) {
    val scrollState = rememberScrollState()

    // Update graph whenever metric changes
    LaunchedEffect(uiState.selectedMetric, uiState.selectedExerciseName) {
        viewModel.updateGraphData(uiState.selectedExerciseName, uiState.selectedMetric)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SectionHeader("Recent exercises")
        Spacer(modifier = Modifier.height(24.dp))

        WeeklyBarChart(
            barColor = MaterialTheme.colorScheme.secondary,
            labelColor = MaterialTheme.colorScheme.onSurface,
            data = uiState.weeklyCounts,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(16.dp)
        )

        SectionHeader("Performance by exercise")
        Spacer(modifier = Modifier.height(24.dp))

        GraphSelectors(
            uiState = uiState,
            viewModel = viewModel
        )

        Spacer(Modifier.height(16.dp))

        LineGraph(
            lines = uiState.multiGraphData,
            colors = listOf(
                MaterialTheme.colorScheme.secondary,
                MaterialTheme.colorScheme.tertiary,
                MaterialTheme.colorScheme.primary,
                MaterialTheme.colorScheme.error,
                MaterialTheme.colorScheme.outline,
                MaterialTheme.colorScheme.inversePrimary
            ),
            labelColor = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(16.dp)
        )
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
//                        val chartBottomPadding = 60f
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

private fun Float.ceilToInt(): Int = ceil(this).toInt()

@Composable
fun GraphSelectors(
    uiState: FullHistoryUIState,
    viewModel: ProgressScreenViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        // ─────────────────────────────
        // Row 1: Metric + Time Period
        // ─────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // Metric
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Metric",
                    style = MaterialTheme.typography.labelMedium
                )

                MetricSelector(
                    uiState = uiState,
                    onMetricSelected = { metric ->
                        uiState.selectedExerciseName?.let { exercise ->
                            viewModel.setMetric(metric, exercise)
                        }
                    }
                )
            }

            // Time Period
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Time Period",
                    style = MaterialTheme.typography.labelMedium
                )

                TimePeriodSelector(
                    uiState = uiState,
                    onPeriodSelected = { period ->
                        viewModel.setTimePeriod(period)
                    }
                )
            }
        }

        // ─────────────────────────────
        // Row 2: Exercise Selector
        // ─────────────────────────────
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "Exercise",
                style = MaterialTheme.typography.labelMedium
            )

            ExerciseSelector(
                uiState = uiState,
                onExerciseSelected = { name ->
                    viewModel.setSelectedExercise(name)
                }
            )
        }
    }
}


@Composable
fun MetricSelector(
    uiState: FullHistoryUIState,
    onMetricSelected: (MetricType) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        OutlinedButton(
            onClick = { expanded = true },
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
            ) {
                Text(
                    uiState.selectedMetric.label,
                    modifier = Modifier.padding(start = 4.dp)
                )

                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Select metric",
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            MetricType.entries.forEach { metric ->
                DropdownMenuItem(
                    text = { Text(metric.label) },
                    onClick = {
                        expanded = false
                        onMetricSelected(metric)
                    }
                )
            }
        }
    }
}

@Composable
fun ExerciseSelector(
    uiState: FullHistoryUIState,
    onExerciseSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        OutlinedButton(
            onClick = { expanded = true },
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
            ) {
                Text(
                    uiState.selectedExerciseName ?: "Select exercise",
                    modifier = Modifier.padding(start = 4.dp)
                )

                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Select exercise"
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            uiState.exerciseNames.forEach { name ->
                DropdownMenuItem(
                    text = { Text(name) },
                    onClick = {
                        expanded = false
                        onExerciseSelected(name)
                    }
                )
            }
        }
    }
}

@Composable
fun TimePeriodSelector(
    uiState: FullHistoryUIState,
    onPeriodSelected: (TimePeriod) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        OutlinedButton(
            onClick = { expanded = true },
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
            ) {
                Text(
                    uiState.selectedTimePeriod.label,
                    modifier = Modifier.padding(start = 4.dp)
                )

                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Select time period"
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            TimePeriod.entries.forEach { period ->
                DropdownMenuItem(
                    text = { Text(period.label) },
                    onClick = {
                        expanded = false
                        onPeriodSelected(period)
                    }
                )
            }
        }
    }
}



@Composable
fun LineGraph(
    lines: List<List<DataPoint>>,
    modifier: Modifier = Modifier,
    colors: List<Color> = listOf(
        Color.Cyan,
        Color.Red,
        Color.Green,
        Color.Magenta,
        Color.Yellow,
        Color.Blue
    ),
    labelColor: Color = Color.Black
) {
    if (lines.isEmpty() || lines.all { it.isEmpty() }) return

    // Flatten all points to compute global X/Y scales
    val allPoints = lines.flatten().sortedBy { it.date }
    val startDate = allPoints.first().date

    // Global X offsets
    val allXOffsets = allPoints.map { startDate.daysUntil(it.date).toFloat() }
    val maxX = allXOffsets.maxOrNull() ?: 0f

    // Global Y range
    val minY = allPoints.minOf { it.value }
    val maxY = allPoints.maxOf { it.value }.coerceAtLeast(minY + 1f)

    // Text paint for labels
    val textPaint = remember {
        android.graphics.Paint().apply {
            color = labelColor.toArgb()
            textSize = 28f
            isAntiAlias = true
        }
    }

    Canvas(
        modifier = modifier
    ) {
        val chartLeftPadding = 80f
        val chartBottomPadding = 60f

        val chartWidth = size.width - chartLeftPadding
        val chartHeight = size.height - chartBottomPadding

        val xScale = if (maxX == 0f) 1f else chartWidth / maxX
        val yScale = chartHeight / (maxY - minY)

        // --- Compute Y-axis ticks (rounded UP, no extra tick below data) ---
        val maxTicks = 4
        val rawStep = ((maxY - minY) / maxTicks).roundToInt().coerceAtLeast(1)

        val yAxisMin = ((minY + rawStep - 1) / rawStep).toInt() * rawStep
        val yAxisMax = ((maxY + rawStep - 1) / rawStep).toInt() * rawStep

        // --- Draw Y-axis grid lines + labels ---
        for (value in yAxisMin..yAxisMax step rawStep) {
            val y = chartHeight - (value - minY) * yScale

            drawLine(
                color = Color.LightGray.copy(alpha = 0.4f),
                start = Offset(chartLeftPadding, y),
                end = Offset(size.width, y),
                strokeWidth = 2f
            )

            drawContext.canvas.nativeCanvas.drawText(
                value.toString(),
                chartLeftPadding - 30f,
                y + 10f,
                textPaint
            )
        }

        // --- X-axis ticks at regular intervals ---
        val numberOfTicks = 6
        val interval = maxX / numberOfTicks

        val xAxisTicks = (0..numberOfTicks).map { i ->
            (i * interval).toInt()
        }

        xAxisTicks.forEach { dayOffset ->
            val x = chartLeftPadding + dayOffset * xScale

            val date = startDate.plus(dayOffset, DateTimeUnit.DAY)

            val label = date.format(
                LocalDate.Format {
                    dayOfMonth()
                    char(' ')
                    monthName(MonthNames.ENGLISH_ABBREVIATED)
                }
            )

            drawContext.canvas.nativeCanvas.save()
            drawContext.canvas.nativeCanvas.translate(x, size.height - 20f)
            drawContext.canvas.nativeCanvas.rotate(-30f)
            textPaint.textAlign = android.graphics.Paint.Align.RIGHT
            drawContext.canvas.nativeCanvas.drawText(
                label,
                0f,
                0f,
                textPaint
            )
            drawContext.canvas.nativeCanvas.restore()
        }

        // --- Draw each line series ---
        lines.forEachIndexed { lineIndex, series ->
            if (series.isEmpty()) return@forEachIndexed

            val sortedSeries = series.sortedBy { it.date }
            val seriesOffsets = sortedSeries.map { startDate.daysUntil(it.date).toFloat() }

            val path = Path()

            sortedSeries.forEachIndexed { i, point ->
                val x = chartLeftPadding + seriesOffsets[i] * xScale
                val y = chartHeight - (point.value - minY) * yScale

                if (i == 0) path.moveTo(x, y)
                else path.lineTo(x, y)
            }

            drawPath(
                path = path,
                color = colors[lineIndex % colors.size],
                style = Stroke(width = 4f)
            )
        }
    }
}