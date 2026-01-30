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
import com.ryim.actin.ui.ProgressScreenViewModel
import com.ryim.actin.ui.HomeScreenViewModel
import com.ryim.actin.ui.SharedExAddViewModel
import com.ryim.actin.ui.SharedWorkoutViewModel
import com.ryim.actin.ui.WorkoutListScreenViewModel
import com.ryim.actin.ui.screens.EditWorkoutScreen
import com.ryim.actin.ui.screens.HomeScreen
import com.ryim.actin.ui.screens.ExAddEditScreen
import com.ryim.actin.ui.screens.ProgressScreen
import com.ryim.actin.ui.screens.WorkoutListScreen
import com.ryim.actin.ui.screens.SettingScreen
import com.ryim.actin.ui.screens.SplashScreen
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
        startDestination = "splash",
        route = "root"
    ) {
        // --- Splash Screen ---
        composable("splash") {
            SplashScreen(
                onTimeout = {
                    navController.navigate("home") {
                        popUpTo("splash") { inclusive = true } // remove splash from back stack
                    }
                }
            )
        }

        composable("home") { backStackEntry ->

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
                onNavigateToExAdd = { navController.navigate("exAddEdit") },
                onDeleteExercise = { entry ->
                    mainViewModel.deleteExercise(entry)
                    navController.navigate("home")
                },
                onHome = { navController.navigate("home") },
                onProgress = { navController.navigate("ProgressScreen") },
                onExercise = { navController.navigate("WorkoutListScreen") },
                onSettings = { navController.navigate("SettingScreen") }
            )
        }

        composable("exAddEdit") { backStackEntry ->
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
                }
            )
        }

        composable("ProgressScreen") { backStackEntry ->

            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry("root")
            }

            val sharedExAddViewModel: SharedExAddViewModel = hiltViewModel(parentEntry)
            val logViewModel: ProgressScreenViewModel = hiltViewModel()

            ProgressScreen(
                sharedExAddViewModel = sharedExAddViewModel,
                onHome = { navController.navigate("home") },
                onProgress = { navController.navigate("ProgressScreen") },
                onExercise = { navController.navigate("WorkoutListScreen") },
                onSettings = { navController.navigate("SettingScreen") },
                onNavigateToExAdd = { navController.navigate("exAddEdit") },
                onDeleteExercise = { entry ->
                    logViewModel.deleteExercise(entry)
                    navController.navigate("ProgressScreen")
                }
            )
        }

        composable("WorkoutListScreen") {

            val listViewModel: WorkoutListScreenViewModel = hiltViewModel()

            WorkoutListScreen(
                sharedWorkoutViewModel = sharedWorkoutViewModel,
                onHome = { navController.navigate("home") },
                onProgress = { navController.navigate("ProgressScreen") },
                onExercise = { navController.navigate("WorkoutListScreen") },
                onSettings = { navController.navigate("SettingScreen") },
                onNewWorkout = { navController.navigate("WorkoutEditScreen") },

                onRunWorkout = { workout ->
                    sharedWorkoutViewModel.selectWorkout(workout)
                    navController.navigate("WorkoutRunScreen")
                },

                onEditWorkout = { workout ->
                    sharedWorkoutViewModel.selectWorkout(workout)
                    navController.navigate("WorkoutEditScreen")
                },

                onDeleteWorkout = { workout ->
                    listViewModel.deleteWorkout(workout)
                },
                navController = navController,
                viewModel = listViewModel
            )
        }


        composable("WorkoutRunScreen") { backStackEntry ->

            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry("root")
            }
            val sharedExAddViewModel: SharedExAddViewModel = hiltViewModel(parentEntry)

            WorkoutRunScreen(
                onHome = { navController.navigate("home") },
                onProgress = { navController.navigate("ProgressScreen") },
                onExercise = { navController.navigate("WorkoutListScreen") },
                onSettings = { navController.navigate("SettingScreen") },

                // ⭐ These two must be passed
                sharedWorkoutViewModel = sharedWorkoutViewModel,
                sharedExAddViewModel = sharedExAddViewModel,

                onNavigateToExAdd = { navController.navigate("exAddEdit") }
            )
        }

        composable("WorkoutEditScreen") {
            EditWorkoutScreen(
                onBack = { navController.popBackStack() },
                sharedWorkoutViewModel = sharedWorkoutViewModel
            )
        }
        
        composable(route = "SettingScreen") {
            SettingScreen(
                onHome = { navController.navigate("home") },
                onProgress = { navController.navigate("ProgressScreen") },
                onExercise = { navController.navigate("WorkoutListScreen") },
                onSettings = { navController.navigate("SettingScreen") },
            )
        }
    }
}