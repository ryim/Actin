package com.ryim.actin.ui.screens.ProgressScreenTabs

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.ryim.actin.ui.FullHistoryUIState
import com.ryim.actin.ui.WeeklyCount
import kotlinx.datetime.LocalDate
import kotlinx.datetime.format
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.char

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