package com.ryim.actin.navigation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ryim.actin.ui.LogScreenViewModel
import com.ryim.actin.ui.HomeScreenViewModel
import com.ryim.actin.ui.SharedExAddViewModel
import com.ryim.actin.ui.SharedWorkoutViewModel
import com.ryim.actin.ui.screens.EditWorkoutScreen
import com.ryim.actin.ui.screens.HomeScreen
import com.ryim.actin.ui.screens.ExAddEditScreen
import com.ryim.actin.ui.screens.TimerScreen
import com.ryim.actin.ui.screens.ProgressScreen
import com.ryim.actin.ui.screens.WorkoutListScreen
import com.ryim.actin.ui.screens.SettingScreen
import com.ryim.actin.ui.screens.WorkoutRunScreen
import com.ryim.actin.ui.theme.loadDarkMode
import com.ryim.actin.ui.theme.MyTheme

@Composable
fun LaunchAppByTheme() {
    val context = LocalContext.current
    val darkModeEnabled by loadDarkMode(context).collectAsState(initial = false)

//    AppNavHost()
    MyTheme(darkTheme = darkModeEnabled) {
        AppNavHost()
    }
}

@Composable
fun AppNavHost(navController: NavHostController = rememberNavController()) {

    val sharedWorkoutViewModel: SharedWorkoutViewModel = hiltViewModel()

    NavHost(
        navController,
        startDestination = "main",
        route = "root"
    ) {
        composable("main") { backStackEntry ->

            // Get the parent NavGraph entry (the root graph)
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry("root")
            }

            // Shared ViewModel scoped to the root graph
            val sharedExAddViewModel: SharedExAddViewModel = hiltViewModel(parentEntry)

            // Your existing HomeScreen ViewModel
            val mainViewModel: HomeScreenViewModel = hiltViewModel()

            HomeScreen(
                sharedExAddViewModel = sharedExAddViewModel,
                onNavigateToExAdd = { navController.navigate("exAdd") },
                onDeleteExercise = { entry ->
                    mainViewModel.deleteExercise(entry)
                    navController.navigate("main")
                },
                onHome = { navController.navigate("main") },
                onProgress = { navController.navigate("GraphScreen") },
                onExercise = { navController.navigate("WorkoutListScreen") },
                onSettings = { navController.navigate("SettingScreen") }
            )
        }

        composable("exAdd") { backStackEntry ->
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry("root")   // or whatever your top-level graph route is
            }
            val sharedExAddViewModel: SharedExAddViewModel = hiltViewModel(parentEntry)

            val prefill = sharedExAddViewModel.prefill.collectAsState().value

            ExAddEditScreen(
                prefill = prefill,
                onBack = {
                    sharedExAddViewModel.clearPrefill()
                    navController.popBackStack()
//                    navController.navigate("main")
                }
            )
        }

//        composable(route = "exAdd") {
//
//            // Get the shared ViewModel that holds the prefill data
//            val sharedExAddViewModel: SharedExAddViewModel = hiltViewModel()
//            val prefill = sharedExAddViewModel.prefill.collectAsState().value
//
//            // Get your existing ExAddViewModel
//            val exAddViewModel: ExAddViewModel = hiltViewModel()
//
//            // Push the prefill data into ExAddViewModel when the screen opens
//            LaunchedEffect(prefill) {
//                prefill?.let {
//                    exAddViewModel.setPrefillParams(
//                        name = it.name,
//                        oldSets = it.sets,
//                        oldReps = it.reps,
//                        oldWeights = it.weights,
//                        oldUseKg = it.useKg,
//                        editMode = it.editMode,
//                        oldTimestamp = it.timestamp,
//                        workout = it.workout
//                    )
//                }
//            }
//
//            ExAddEditScreen(
//                name = exAddViewModel.uiState.collectAsState().value.name,
//                oldSets = exAddViewModel.uiState.collectAsState().value.sets,
//                oldReps = exAddViewModel.uiState.collectAsState().value.reps,
//                oldWeights = exAddViewModel.uiState.collectAsState().value.weights,
//                oldUseKg = exAddViewModel.uiState.collectAsState().value.useKg,
//                editMode = exAddViewModel.uiState.collectAsState().value.editMode,
//                oldTimestamp = exAddViewModel.uiState.collectAsState().value.timestamp,
//                workout = exAddViewModel.uiState.collectAsState().value.workout,
//                onBack = {
//                    sharedExAddViewModel.clearPrefill()
//                    navController.navigate("main")
//                }
//            )
//        }

        composable("GraphScreen") { backStackEntry ->

            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry("root")
            }

            val sharedExAddViewModel: SharedExAddViewModel = hiltViewModel(parentEntry)
            val logViewModel: LogScreenViewModel = hiltViewModel()

            ProgressScreen(
                sharedExAddViewModel = sharedExAddViewModel,
                onHome = { navController.navigate("main") },
                onProgress = { navController.navigate("GraphScreen") },
                onExercise = { navController.navigate("WorkoutListScreen") },
                onSettings = { navController.navigate("SettingScreen") },
                onNavigateToExAdd = { navController.navigate("exAdd") },
                onDeleteExercise = { entry ->
                    logViewModel.deleteExercise(entry)
                    navController.navigate("GraphScreen")
                }
            )
        }

        composable(route = "WorkoutListScreen") {
            WorkoutListScreen(
                onHome = { navController.navigate("main") },
                onProgress = { navController.navigate("GraphScreen") },
                onExercise = { navController.navigate("WorkoutListScreen") },
                onSettings = { navController.navigate("SettingScreen") },
                onNewWorkout = { navController.navigate("WorkoutEditScreen") },
                onRunWorkout = { workout ->
                    sharedWorkoutViewModel.selectWorkout(workout)
                    navController.navigate("WorkoutRunScreen")
                }
//                onExerciseAdd = { name, sets, reps, weights, useKg, editMode, timestamp ->
//                    val repsStr = reps.joinToString(",")
//                    val weightsStr = weights.joinToString(",")
//                    navController.navigate("exAdd/$name/$sets/$repsStr/$weightsStr/$useKg/$editMode/$timestamp")
//                }
            )
        }
        composable("WorkoutRunScreen") { backStackEntry ->

            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry("root")
            }

            val sharedWorkoutViewModel: SharedWorkoutViewModel = hiltViewModel(parentEntry)
            val sharedExAddViewModel: SharedExAddViewModel = hiltViewModel(parentEntry)

            WorkoutRunScreen(
                onHome = { navController.navigate("main") },
                onProgress = { navController.navigate("GraphScreen") },
                onExercise = { navController.navigate("WorkoutListScreen") },
                onSettings = { navController.navigate("SettingScreen") },

                // ⭐ These two must be passed
                sharedWorkoutViewModel = sharedWorkoutViewModel,
                sharedExAddViewModel = sharedExAddViewModel,

                onNavigateToExAdd = { navController.navigate("exAdd") }
            )
        }

        composable(route = "WorkoutEditScreen") {
            EditWorkoutScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(route = "TimerScreen") {
            TimerScreen(
                onHome = { navController.navigate("main") },
                onGraph = { navController.navigate("GraphScreen") },
                onExercise = { navController.navigate("WorkoutListScreen") },
                onTimer = { navController.navigate("TimerScreen") },
                onSettings = { navController.navigate("SettingScreen") },
            )
        }
        composable(route = "SettingScreen") {
            SettingScreen(
                onHome = { navController.navigate("main") },
                onProgress = { navController.navigate("GraphScreen") },
                onExercise = { navController.navigate("WorkoutListScreen") },
                onTimer = { navController.navigate("TimerScreen") },
                onSettings = { navController.navigate("SettingScreen") },
            )
        }
    }
}