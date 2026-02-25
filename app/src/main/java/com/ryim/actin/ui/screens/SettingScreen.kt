package com.ryim.actin.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.ryim.actin.ui.ReusableComposables.AppBottomBar
import com.ryim.actin.ui.ReusableComposables.AppTopBar
import com.ryim.actin.ui.ReusableComposables.RoundRectButton
import com.ryim.actin.ui.ReusableComposables.SectionHeader
import com.ryim.actin.ui.ReusableComposables.StandardIconButton
import com.ryim.actin.ui.SettingScreenViewModel
import com.ryim.actin.ui.ThemeMode
import kotlinx.coroutines.launch
import com.ryim.actin.ui.theme.loadThemeMode
import com.ryim.actin.ui.theme.saveThemeMode


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingScreen(
    onHome: () -> Unit,
    onProgress: () -> Unit,
    onExercise: () -> Unit,
    onSettings: () -> Unit,
    viewModel: SettingScreenViewModel = hiltViewModel(),
) {
    //  Theme settings
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    //  Launchers for Exercise data JSON export and export
    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) viewModel.exportToUri(uri)
    }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) viewModel.importFromUri(uri)
    }

    //  Launcher for Exercise data TSV export
    var pendingTsv by remember { mutableStateOf<String?>(null) }

    val tsvExportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/tab-separated-values")
    ) { uri ->
        if (uri != null && pendingTsv != null) {
            context.contentResolver.openOutputStream(uri)?.use { stream ->
                stream.write(pendingTsv!!.toByteArray())
            }
        }
    }

    //  Launchers for workout data JSON export and export
    val workoutExportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) viewModel.exportWorkoutToUri(uri)
    }

    val workoutImportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) viewModel.importWorkoutFromUri(uri)
    }

    //  The main part of the composable
    Scaffold(
        topBar = {
            AppTopBar(
                title = "Settings"
            )
        },

        bottomBar = {
            AppBottomBar(
                selectedItem = 4,
                onHome = onHome,
                onProgress = onProgress,
                onExercise = onExercise,
                onSettings = onSettings
            )
        }

    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            //  ####################################################################################
            SectionHeader("General",
                padding = 0)

            // Dark mode selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Dark mode",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )

                var expanded by remember { mutableStateOf(false) }
                val themeMode by loadThemeMode(context).collectAsState(initial = ThemeMode.SYSTEM)

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
                                when (themeMode) {
                                    ThemeMode.SYSTEM -> "Use system setting"
                                    ThemeMode.LIGHT -> "Light"
                                    ThemeMode.DARK -> "Dark"
                                }
                            )

                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Select theme mode",
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        ThemeMode.entries.forEach { mode ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        when (mode) {
                                            ThemeMode.SYSTEM -> "Use system setting"
                                            ThemeMode.LIGHT -> "Light"
                                            ThemeMode.DARK -> "Dark"
                                        }
                                    )
                                },
                                onClick = {
                                    expanded = false
                                    scope.launch { saveThemeMode(context, mode) }
                                }
                            )
                        }
                    }
                }
            }

            //  ####################################################################################
            SectionHeader("Exercise data management",
                padding = 0)

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Import exercise data JSON",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )

                Row(
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                ) {
                    StandardIconButton(
                        icon = Icons.Default.FileUpload,
                        onClick = { importLauncher.launch(arrayOf("application/json")) }
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Export exercise data as JSON",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )

                Row(
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                ) {
                    StandardIconButton(
                        icon = Icons.Default.FileDownload,
                        onClick = { exportLauncher.launch("exercises.json") }
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Export exercise data as TSV",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )

                Row(
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                ) {
                    StandardIconButton(
                        icon = Icons.AutoMirrored.Filled.ViewList,
                        onClick = {
                            viewModel.requestTsvExport { tsv ->
                                pendingTsv = tsv
                                tsvExportLauncher.launch("exercises_export.tsv")
                            }
                        }
                    )
                }
            }

            //  ####################################################################################
            SectionHeader("Workout data management",
                padding = 0)

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Import workout data",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )

                Row(
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                ) {
                    StandardIconButton(
                        icon = Icons.Default.FileUpload,
                        onClick = { workoutImportLauncher.launch(arrayOf("application/json")) }
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Export workout data",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )

                Row(
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                ) {
                    StandardIconButton(
                        icon = Icons.Default.FileDownload,
                        onClick = { workoutExportLauncher.launch("workouts.json") }
                    )
                }
            }


            //  ####################################################################################
            SectionHeader("Other",
                padding = 0)

            Box(
                modifier = Modifier
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                RoundRectButton(
                    "About",
                    onClick = { /* To do! */ }
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                RoundRectButton(
                    text = "License",
                    onClick = { /* To do! */ }
                )
            }
        }

    }
}