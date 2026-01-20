package com.ryim.actin

// Main imports
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge

// Local imports
import com.ryim.actin.navigation.LaunchAppByTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LaunchAppByTheme()
        }
    }
}

//
// --- Previews ---
//

//@Preview(showBackground = true)
//@Composable
//fun SplashScreenPreview() {
//    ComposeTutorialTheme {
//        SplashScreen() // preview splash only
//    }
//}
//
//@Preview(showBackground = true)
//@Composable
//fun MainScreenPreview() {
//    ComposeTutorialTheme {
//        MainScreen(
//            onAddEx = {},
//            onHome = {},
//            onGraph = {},
//            onExercise = {},
//            onTimer = {},
//            onSettings = {},
//            onExerciseAdd = {}
//        ) // preview main only
//    }
//}
//
//@Preview(showBackground = true)
//@Composable
//fun AddExScreenPreview() {
//    ComposeTutorialTheme {
//        ExAddScreen(
//            onBack = {},
//            name = ""
//        ) // preview main only
//    }
//}