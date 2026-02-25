package com.ryim.actin.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.ryim.actin.ui.ExAddEditViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import com.ryim.actin.domain.formatTimestampPretty
import com.ryim.actin.domain.monthAbbrev
import com.ryim.actin.ui.ExAddPrefill
import com.ryim.actin.ui.ReusableComposables.ButtonMode
import com.ryim.actin.ui.ReusableComposables.RoundRectButton
import com.ryim.actin.ui.ReusableComposables.SectionHeader
import com.ryim.actin.ui.ReusableComposables.SuggestionTextField
import com.ryim.actin.ui.ReusableComposables.UpDownCounter
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalMaterial3Api::class, ExperimentalTime::class)
@Composable
fun ExAddEditScreen(
    prefill: ExAddPrefill?,
    onBack: () -> Unit = {}
) {
    val viewModel: ExAddEditViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsState()

    // Date/time pickers
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()
    val timePickerState = rememberTimePickerState(
        initialHour = uiState.hour.toInt(),
        initialMinute = uiState.minute.toInt()
    )

    val canSave = uiState.name.isNotBlank()

//    var minutes by remember { mutableStateOf(0) }
//    var seconds by remember { mutableStateOf(30) }

    LaunchedEffect(prefill) {
        prefill?.let {
            viewModel.setPrefillParams(
                name = it.name,
                oldSets = it.sets,
                oldReps = it.reps,
                oldWeights = it.weights,
                oldUseKg = it.useKg,
                editMode = it.editMode,
                oldTimestamp = it.timestamp,
                workout = it.workout,
                id = it.id
            )
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        //  The main scaffold for the screen
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Exercise",
                                color = Color.White,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                )
            },
            bottomBar = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .navigationBarsPadding()   // ← This is the magic line
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {

                    RoundRectButton(
                        onClick = {
                            if (prefill?.editMode ?: false) {
                                viewModel.restoreOriginal(prefill)
                            } else {
                                viewModel.deleteCurrentExercise()
                            }
                            onBack()
                        },
                        text = "Cancel",
                        mode = ButtonMode.Blue
                    )

                    RoundRectButton(
                        onClick = {
                            viewModel.saveExercise()
                            onBack()
                        },
                        enabled = canSave,
                        text = "Confirm"
                    )
                }
            }

        ) { innerPadding ->

            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                Spacer(modifier = Modifier.height(8.dp))

                //  The name field
                val suggestions = remember(uiState.name, prefill) {
                    prefill?.listOfExercises
                        ?.filter { it.contains(uiState.name, ignoreCase = true) }
                        ?.sorted()
                        ?.take(6)
                        ?: emptyList()
                }

                SuggestionTextField(
                    value = uiState.name,
                    onValueChange = { viewModel.onNameChanged(it) },
                    suggestions = suggestions,
                    label = "Exercise name",
                    modifier = Modifier.fillMaxWidth()
                )

    //            MinuteSecondStepper(
    //                minutes = minutes,
    //                seconds = seconds,
    //                onMinutesChange = { minutes = it },
    //                onSecondsChange = { seconds = it }
    //            )
    //
    //            StartTimerButton(minutes, seconds) {
    //                scheduleTimerNotification(context, minutes, seconds)
    //            }

                SectionHeader("Your data")

                // How many sets?
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Text(
                        text = "Number of sets",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier
                            .padding(start = 16.dp),
                        textAlign = TextAlign.Start
                    )

                    UpDownCounter(
                        sets = uiState.sets,
                        onIncrement = { viewModel.incrementSets() },
                        onDecrement = { viewModel.decrementSets() }
                    )
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    uiState.reps.forEachIndexed { index, repsValue ->

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {

                            // ⭐ Actual row of controls
                            Row(
                                verticalAlignment = Alignment.Bottom,
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                            ) {

                                // Set label
                                Box(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Set ${index + 1}",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        modifier = Modifier
                                            .padding(bottom = 6.dp)
                                    )
                                }

                                Row(
                                    horizontalArrangement = Arrangement.SpaceEvenly,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    // Reps counter (NO weight!)
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.width(IntrinsicSize.Min)
                                    ) {
                                        if (index == 0) {
                                            Text(
                                                text = "Reps",
                                                style = MaterialTheme.typography.labelMedium,
                                                textAlign = TextAlign.Center
                                            )
                                        }

                                        Spacer(Modifier.height(4.dp))

                                        UpDownCounter(
                                            sets = repsValue,
                                            onIncrement = { viewModel.incrementRep(index) },
                                            onDecrement = { viewModel.decrementRep(index) }
                                        )
                                    }

                                    // Weight input (this one can still use weight)
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.width(IntrinsicSize.Min)
                                    ) {
                                        if (index == 0) {
                                            Text(
                                                text = "Weight",
                                                style = MaterialTheme.typography.labelLarge,
                                                textAlign = TextAlign.Center,
                                                modifier = Modifier.padding(bottom = 4.dp)
                                            )
                                        }

//                                    Spacer(Modifier.width(1.dp))

                                        Box(
                                            modifier = Modifier
                                                .width(80.dp)   // give it a natural width
                                                .height(32.dp)
                                                .border(
                                                    width = 1.dp,
                                                    color = MaterialTheme.colorScheme.outline,
                                                    shape = MaterialTheme.shapes.small
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            BasicTextField(
                                                value = uiState.weights[index],
                                                onValueChange = { newValue ->
                                                    if (newValue.isEmpty() || newValue.matches(
                                                            Regex(
                                                                """\d*\.?\d*"""
                                                            )
                                                        )
                                                    ) {
                                                        viewModel.updateWeight(index, newValue)
                                                    }
                                                },
                                                singleLine = true,
                                                textStyle = MaterialTheme.typography.bodySmall.copy(
                                                    textAlign = TextAlign.Center,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                ),
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        }
                                    }
                                }

                                Box(modifier = Modifier.weight(1f)) {}
                            }
                        }
                    }
                }

                // Date stuff
                SectionHeader("Date and time")

                Text(
                    text = formatTimestampPretty(uiState.timestamp),
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp),
                    textAlign = TextAlign.Center
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {

                    RoundRectButton(
                        onClick = {
                            showDatePicker = true
                        },
                        text = "Change date",
                        mode = ButtonMode.Blue
                    )

                    RoundRectButton(
                        onClick = {
                            showTimePicker = true
                        },
                        text = "Change time",
                        mode = ButtonMode.Blue
                    )


                    if (showDatePicker) {
                        DatePickerDialog(
                            onDismissRequest = { showDatePicker = false },
                            confirmButton = {
                                TextButton(onClick = {
                                    val millis = datePickerState.selectedDateMillis
                                    if (millis != null) {
                                        val date = Instant
                                            .fromEpochMilliseconds(millis)
                                            .toLocalDateTime(TimeZone.currentSystemDefault())
                                            .date

                                        viewModel.updateDate(
                                            date.dayOfMonth,
                                            date.monthNumber,
                                            date.year
                                        )

                                    }
                                    showDatePicker = false
                                }) { Text("OK") }
                            },
                            dismissButton = {
                                TextButton(onClick = { showDatePicker = false }) {
                                    Text("Cancel")
                                }
                            }
                        ) {
                            DatePicker(state = datePickerState)
                        }
                    }

                    if (showTimePicker) {
                        TimePickerDialog(
                            onDismissRequest = { showTimePicker = false },
                            confirmButton = {
                                TextButton(onClick = {
                                    viewModel.updateTime(
                                        timePickerState.hour,
                                        timePickerState.minute
                                    )
                                    showTimePicker = false
                                }) { Text("OK") }
                            },
                            dismissButton = {
                                TextButton(onClick = { showTimePicker = false }) {
                                    Text("Cancel")
                                }
                            },
                            title = { Text("Select time") }
                        ) {
                            TimePicker(state = timePickerState)
                        }
                    }
                }

                SectionHeader("Settings")

                //  Button to use KG or lb
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = if (uiState.useKg) "Using kg" else "Using lb",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.weight(1f)
                    )

                    Switch(
                        checked = uiState.useKg,
                        onCheckedChange = { viewModel.onUseKgChanged(it) }
                    )
                }
            }
        }

        if (uiState.autosaved) {
            AutosaveOverlay(
                modifier = Modifier
                    .align(Alignment.Center)
            )
        }
    }
}

@Composable
fun AutosaveOverlay(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier
            .padding(16.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary),
        tonalElevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Save,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary
            )
            Spacer(Modifier.width(8.dp))
            Text(
                "Saved",
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

//@Composable
//fun NumberPicker(
//    value: Int,
//    range: IntRange,
//    onValueChange: (Int) -> Unit,
//    modifier: Modifier = Modifier
//) {
//    val listState = rememberLazyListState(
//        initialFirstVisibleItemIndex = value.coerceIn(range)
//    )
//
//    // When scroll position changes, update the selected value
//    LaunchedEffect(listState.firstVisibleItemIndex) {
//        val index = listState.firstVisibleItemIndex
//        val newValue = (range.first + index).coerceIn(range)
//        if (newValue != value) {
//            onValueChange(newValue)
//        }
//    }
//
//    LazyColumn(
//        state = listState,
//        modifier = modifier.height(120.dp),
//        horizontalAlignment = Alignment.CenterHorizontally
//    ) {
//        items(range.count()) { index ->
//            val itemValue = range.first + index
//            Text(
//                text = itemValue.toString().padStart(2, '0'),
//                style = MaterialTheme.typography.headlineMedium,
//                modifier = Modifier.padding(vertical = 8.dp)
//            )
//        }
//    }
//}

//@Composable
//fun MinuteSecondPicker(
//    minutes: Int,
//    seconds: Int,
//    onMinutesChange: (Int) -> Unit,
//    onSecondsChange: (Int) -> Unit
//) {
//    Row(
//        horizontalArrangement = Arrangement.Center,
//        verticalAlignment = Alignment.CenterVertically
//    ) {
//        NumberPicker(
//            value = minutes,
//            range = 0..59,
//            onValueChange = onMinutesChange,
//            modifier = Modifier.width(80.dp)
//        )
//
//        Text(
//            text = ":",
//            style = MaterialTheme.typography.headlineMedium,
//            modifier = Modifier.padding(horizontal = 8.dp)
//        )
//
//        NumberPicker(
//            value = seconds,
//            range = 0..59,
//            onValueChange = onSecondsChange,
//            modifier = Modifier.width(80.dp)
//        )
//    }
//}

//@Composable
//fun StepperSelector(
//    label: String,
//    value: Int,
//    range: IntRange,
//    onValueChange: (Int) -> Unit
//) {
//    Column(horizontalAlignment = Alignment.CenterHorizontally) {
//
//        Text(
//            text = label,
//            style = MaterialTheme.typography.titleMedium
//        )
//
//        Row(
//            verticalAlignment = Alignment.CenterVertically,
//            horizontalArrangement = Arrangement.Center
//        ) {
//            IconButton(
//                onClick = {
//                    if (value > range.first) onValueChange(value - 1)
//                }
//            ) {
//                Icon(
//                    imageVector = Icons.Default.Remove,
//                    contentDescription = "Decrease"
//                )
//            }
//
//            Text(
//                text = value.toString().padStart(2, '0'),
//                style = MaterialTheme.typography.headlineMedium,
//                modifier = Modifier.width(40.dp),
//                textAlign = TextAlign.Center
//            )
//
//            IconButton(
//                onClick = {
//                    if (value < range.last) onValueChange(value + 1)
//                }
//            ) {
//                Icon(
//                    imageVector = Icons.Default.Add,
//                    contentDescription = "Increase"
//                )
//            }
//        }
//    }
//}

//@Composable
//fun MinuteSecondStepper(
//    minutes: Int,
//    seconds: Int,
//    onMinutesChange: (Int) -> Unit,
//    onSecondsChange: (Int) -> Unit
//) {
//    Row(
//        horizontalArrangement = Arrangement.Center,
//        verticalAlignment = Alignment.CenterVertically
//    ) {
//        StepperSelector(
//            label = "Min",
//            value = minutes,
//            range = 0..59,
//            onValueChange = onMinutesChange
//        )
//
//        Spacer(Modifier.width(24.dp))
//
//        StepperSelector(
//            label = "Sec",
//            value = seconds,
//            range = 0..59,
//            onValueChange = onSecondsChange
//        )
//    }
//}

//@Composable
//fun StartTimerButton(
//    minutes: Int,
//    seconds: Int,
//    onStart: () -> Unit = {}
//) {
//    Button(
//        onClick = {
//            onStart()
//        }
//    ) {
//        Text("Start Timer")
//    }
//}
