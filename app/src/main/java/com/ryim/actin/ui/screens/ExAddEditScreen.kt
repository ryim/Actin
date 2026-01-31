package com.ryim.actin.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Remove
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import com.ryim.actin.ui.ExAddEditViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import com.ryim.actin.domain.monthAbbrev
import com.ryim.actin.ui.ExAddPrefill
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

    var minutes by remember { mutableStateOf(0) }
    var seconds by remember { mutableStateOf(30) }

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
                Button(
                    onClick = onBack,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        "Back",
//                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
                Button(
                    onClick = {
                        viewModel.saveExercise()
                        onBack()
                    },
                    enabled = canSave,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary, // different from box
                        contentColor = Color.Black
                    )
                ) {
                    Text(
                        "Confirm",
//                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
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

            val isNameError = uiState.name.isBlank()
            val suggestions = remember(uiState.name, prefill) {
                prefill?.listOfExercises
                    ?.filter { it.contains(uiState.name, ignoreCase = true) }
                    ?.sorted()
                    ?.take(6)
                    ?: emptyList()
            }

            val focusRequester = remember { FocusRequester() }
            var expanded by remember { mutableStateOf(false) }
            var isFocused by remember { mutableStateOf(false) }

            Column {
                TextField(
                    value = uiState.name,
                    onValueChange = {
                        viewModel.onNameChanged(it)
                        expanded = true
                    },
                    label = { Text("Exercise name") },
                    isError = isNameError,
                    supportingText = {
                        if (isNameError) Text("Name is required")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                        .onFocusChanged { state ->
                            isFocused = state.isFocused
                            if (!state.isFocused) expanded = false
                        },
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences
                    )
                )

                if (expanded && isFocused && suggestions.isNotEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                    ) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 200.dp) // limit size
                                .padding(8.dp)
                        ) {
                            items(suggestions) { suggestion ->
                                Text(
                                    text = suggestion,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            viewModel.onNameChanged(suggestion)
                                            expanded = false

                                            // Keep focus on the text field
                                            focusRequester.requestFocus()
                                        }
                                        .padding(12.dp)
                                )
                            }
                        }
                    }
                }
            }

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

            Text(
                text = "Number of sets",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp),
                textAlign = TextAlign.Start
            )

            // How many sets?
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    // Display-only number box
                    Box(
                        modifier = Modifier
                            .width(64.dp)
                            .height(32.dp)
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outline,
                                shape = MaterialTheme.shapes.medium
                            )
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = uiState.sets.toString(),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // ✅ Up arrow
                    IconButton(
                        onClick = { viewModel.incrementSets() }
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowUp,
                            contentDescription = "Increase"
                        )
                    }

                    // ✅ Down arrow
                    IconButton(
                        onClick = { viewModel.decrementSets() }
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Decrease"
                        )
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                // Labels
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = "Reps",
                    style = MaterialTheme.typography.labelSmall
                )
                Spacer(modifier = Modifier.width(64.dp))
                Text(
                    text = "Weight",
                    style = MaterialTheme.typography.labelSmall
                )
//                Spacer(modifier = Modifier.width(2.dp))
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                uiState.reps.forEachIndexed { index, repsValue ->

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {

                        // Row containing set title, sub-labels + controls
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            modifier = Modifier.fillMaxWidth()
                                .padding(horizontal = 16.dp)
                        ) {

                            // Main label
                            Text(
                                text = "Set ${index + 1}",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier.padding(start = 4.dp)
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            // --- Reps Column ---
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.weight(1f)
                            ) {

                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {

                                    // Display box
                                    Box(
                                        modifier = Modifier
                                            .width(60.dp)
                                            .height(32.dp)
                                            .border(
                                                width = 1.dp,
                                                color = MaterialTheme.colorScheme.outline,
                                                shape = MaterialTheme.shapes.medium
                                            )
                                            .padding(horizontal = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = repsValue.toString(),
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }

                                    // Up arrow
                                    IconButton(
                                        onClick = { viewModel.incrementRep(index) }
                                    ) {
                                        Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Increase reps")
                                    }

                                    // Down arrow
                                    IconButton(
                                        onClick = { viewModel.decrementRep(index) }
                                    ) {
                                        Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Decrease reps")
                                    }
                                }
                            }

                            // --- Weight Column ---
                            Box(
                                modifier = Modifier
                                    .width(80.dp)
                                    .height(32.dp)
                                    .border(
                                        width = 1.dp,
                                        color = MaterialTheme.colorScheme.outline,
                                        shape = MaterialTheme.shapes.small
                                    ),
                                contentAlignment = Alignment.Center   // ← centers the text vertically & horizontally
                            ) {
                                BasicTextField(
                                    value = uiState.weights[index],
                                    onValueChange = { newValue ->
                                        if (newValue.isEmpty() || newValue.matches(Regex("""\d*\.?\d*"""))) {
                                            viewModel.updateWeight(index, newValue)
                                        }
                                    },
                                    singleLine = true,
                                    textStyle = MaterialTheme.typography.bodySmall.copy(
                                        textAlign = TextAlign.Center,
                                        color = MaterialTheme.colorScheme.onSurface
                                    ),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier
                                        .fillMaxWidth()   // let the Box handle centering
                                        .padding(0.dp)    // no internal padding
                                )
                            }
                        }
                    }
                }
            }

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

            // Date stuff
            Text(
                text = "Date and time",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp),
                textAlign = TextAlign.Start
            )

            val monthAbb = monthAbbrev(uiState.month.toInt())

            Text(
                text = "${uiState.day} $monthAbb ${uiState.year} • ${uiState.hour}:${uiState.minute}",
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp),
                textAlign = TextAlign.Start
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
//                // Day
//                OutlinedTextField(
//                    value = uiState.day,
//                    onValueChange = { if (it.all(Char::isDigit)) viewModel.onDayChanged(it) },
//                    label = { Text("Day") },
//                    singleLine = true,
//                    modifier = Modifier.weight(1f),
//                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
//                )
//
//                // Month
//                OutlinedTextField(
//                    value = uiState.month,
//                    onValueChange = { if (it.all(Char::isDigit)) viewModel.onMonthChanged(it) },
//                    label = { Text("Month") },
//                    singleLine = true,
//                    modifier = Modifier.weight(1f),
//                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
//                )
//
//                // Year
//                OutlinedTextField(
//                    value = uiState.year,
//                    onValueChange = { if (it.all(Char::isDigit)) viewModel.onYearChanged(it) },
//                    label = { Text("Year") },
//                    singleLine = true,
//                    modifier = Modifier.weight(1f),
//                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
//                )

                Button(
                    onClick = {
                        showDatePicker = true
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(
                        "Change date",
                    )
                }

                Button(
                    onClick = {
                        showTimePicker = true
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(
                        "Change time",
                    )
                }

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
        }
    }
}

@Composable
fun NumberPicker(
    value: Int,
    range: IntRange,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = value.coerceIn(range)
    )

    // When scroll position changes, update the selected value
    LaunchedEffect(listState.firstVisibleItemIndex) {
        val index = listState.firstVisibleItemIndex
        val newValue = (range.first + index).coerceIn(range)
        if (newValue != value) {
            onValueChange(newValue)
        }
    }

    LazyColumn(
        state = listState,
        modifier = modifier.height(120.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        items(range.count()) { index ->
            val itemValue = range.first + index
            Text(
                text = itemValue.toString().padStart(2, '0'),
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
    }
}

@Composable
fun MinuteSecondPicker(
    minutes: Int,
    seconds: Int,
    onMinutesChange: (Int) -> Unit,
    onSecondsChange: (Int) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        NumberPicker(
            value = minutes,
            range = 0..59,
            onValueChange = onMinutesChange,
            modifier = Modifier.width(80.dp)
        )

        Text(
            text = ":",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        NumberPicker(
            value = seconds,
            range = 0..59,
            onValueChange = onSecondsChange,
            modifier = Modifier.width(80.dp)
        )
    }
}

@Composable
fun StepperSelector(
    label: String,
    value: Int,
    range: IntRange,
    onValueChange: (Int) -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {

        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            IconButton(
                onClick = {
                    if (value > range.first) onValueChange(value - 1)
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Remove,
                    contentDescription = "Decrease"
                )
            }

            Text(
                text = value.toString().padStart(2, '0'),
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.width(40.dp),
                textAlign = TextAlign.Center
            )

            IconButton(
                onClick = {
                    if (value < range.last) onValueChange(value + 1)
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Increase"
                )
            }
        }
    }
}

@Composable
fun MinuteSecondStepper(
    minutes: Int,
    seconds: Int,
    onMinutesChange: (Int) -> Unit,
    onSecondsChange: (Int) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        StepperSelector(
            label = "Min",
            value = minutes,
            range = 0..59,
            onValueChange = onMinutesChange
        )

        Spacer(Modifier.width(24.dp))

        StepperSelector(
            label = "Sec",
            value = seconds,
            range = 0..59,
            onValueChange = onSecondsChange
        )
    }
}

@Composable
fun StartTimerButton(
    minutes: Int,
    seconds: Int,
    onStart: () -> Unit = {}
) {
    Button(
        onClick = {
            onStart()
        }
    ) {
        Text("Start Timer")
    }
}
