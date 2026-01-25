package com.ryim.actin.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.ryim.actin.R
import com.ryim.actin.ui.ReusableComposables.AppBottomBar
import com.ryim.actin.ui.SettingScreenViewModel
import kotlinx.coroutines.launch
import com.ryim.actin.ui.theme.loadDarkMode
import com.ryim.actin.ui.theme.saveDarkMode


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

    //  Launchers for JSON export and export
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

    //  Launcher for TSV export
    var pendingTsv by remember { mutableStateOf<String?>(null) }

    val TSVExportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/tab-separated-values")
    ) { uri ->
        if (uri != null && pendingTsv != null) {
            context.contentResolver.openOutputStream(uri)?.use { stream ->
                stream.write(pendingTsv!!.toByteArray())
            }
        }
    }


    //  The main part of the composable
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(
                        modifier = Modifier.fillMaxWidth()
                            .padding(start = 12.dp),
//                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Settings",
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
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // --- Toggle 1 ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Enable notifications (to do)",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f)
                )

                var notificationsEnabled by remember { mutableStateOf(false) }

                Switch(
                    checked = notificationsEnabled,
                    onCheckedChange = { notificationsEnabled = it }
                )
            }

            // Dark mode toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Dark mode",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f)
                )

                val darkModeEnabled by loadDarkMode(context).collectAsState(initial = false)

                Switch(
                    checked = darkModeEnabled,
                    onCheckedChange = { enabled ->
                        scope.launch {
                            saveDarkMode(context, enabled)
                        }
                    }
                )
            }

            // --- Toggle 3 ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Dummy toggle",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f)
                )

                var autoSyncEnabled by remember { mutableStateOf(false) }

                Switch(
                    checked = autoSyncEnabled,
                    onCheckedChange = { autoSyncEnabled = it }
                )
            }

            HorizontalDivider(
                modifier = Modifier
                    .padding(horizontal = 16.dp), thickness = 1.dp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
            )

            Text(
                "Timer settings",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.align(Alignment.Start)
                    .padding(horizontal = 16.dp)
            )

            // Global timer toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Use global timer",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f)
                )

                var autoSyncEnabled by remember { mutableStateOf(false) }

                Switch(
                    checked = autoSyncEnabled,
                    onCheckedChange = { autoSyncEnabled = it }
                )
            }

            HorizontalDivider(
                modifier = Modifier
                    .padding(horizontal = 16.dp), thickness = 1.dp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
            )

            Text(
                "Exercise data management",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.align(Alignment.Start)
                    .padding(horizontal = 16.dp)
            )

                // Import data
                Box(
                    modifier = Modifier
                        .weight(1f)
//                        .height(64.dp)
                        .background(MaterialTheme.colorScheme.surface),
                    contentAlignment = Alignment.Center
                ) {
                    Button(
                        onClick = {
                            importLauncher.launch(arrayOf("application/json"))
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = Color.White
                        )
                    ) {
                        Text("Import JSON")
                    }
                }

                // Export data
                Box(
                    modifier = Modifier
                        .weight(1f)
//                        .height(64.dp)
                        .background(MaterialTheme.colorScheme.surface),
                    contentAlignment = Alignment.Center
                ) {
                    Button(
                        onClick = {
                            exportLauncher.launch("exercises.json")
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = Color.White
                        )
                    ) {
                        Text("Export JSON")
                    }
                }

                // Export TSV
                Box(
                    modifier = Modifier
                        .weight(1f)
//                        .height(64.dp)
                        .background(MaterialTheme.colorScheme.surface),
                    contentAlignment = Alignment.Center
                ) {
                    Button(
                        onClick = {
                            viewModel.requestTsvExport { tsv ->
                                pendingTsv = tsv
                                TSVExportLauncher.launch("exercises_export.tsv")
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = Color.White
                        )
                    ) {
                        Text("Export TSV")
                    }
                }
//            }

            HorizontalDivider(
                modifier = Modifier
                    .padding(horizontal = 16.dp), thickness = 1.dp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
            )

            Text(
                "Other",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.align(Alignment.Start)
                    .padding(horizontal = 16.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .background(MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.Center
            ) {
                Button(
                    onClick = { /* To do! */ },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary, // different from box
                        contentColor = Color.White
                    )
                ) {
                    Text("About")
                }
            }
        }

    }
}