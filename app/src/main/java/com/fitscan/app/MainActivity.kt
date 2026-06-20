package com.fitscan.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.fitscan.app.ui.screens.camera.CameraScreen
import com.fitscan.app.ui.screens.camera.CameraViewModel
import com.fitscan.app.ui.screens.camera.CameraViewModelFactory
import com.fitscan.app.ui.screens.history.HistoryScreen
import com.fitscan.app.ui.screens.history.HistoryViewModel
import com.fitscan.app.ui.screens.history.HistoryViewModelFactory
import com.fitscan.app.ui.screens.home.HomeScreen
import com.fitscan.app.ui.screens.home.HomeViewModel
import com.fitscan.app.ui.screens.home.HomeViewModelFactory
import com.fitscan.app.ui.screens.profile.ProfileScreen
import com.fitscan.app.ui.screens.result.ResultScreen
import com.fitscan.app.ui.screens.result.ResultViewModel
import com.fitscan.app.ui.screens.result.ResultViewModelFactory
import com.fitscan.app.ui.screens.upload.UploadScreen
import com.fitscan.app.ui.screens.upload.UploadViewModel
import com.fitscan.app.ui.screens.upload.UploadViewModelFactory
import com.fitscan.app.ui.theme.FitScanTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val app = application as FitScanApp

        setContent {
            FitScanTheme {
                FitScanAppNavigation(app = app)
            }
        }
    }
}

@Composable
fun FitScanAppNavigation(app: FitScanApp) {
    val navController = rememberNavController()
    
    val homeViewModel: HomeViewModel = viewModel(factory = HomeViewModelFactory(app.repository))
    val cameraViewModel: CameraViewModel = viewModel(factory = CameraViewModelFactory(app.analyzeImageUseCase))
    val uploadViewModel: UploadViewModel = viewModel(factory = UploadViewModelFactory(app.analyzeImageUseCase))
    val resultViewModel: ResultViewModel = viewModel(factory = ResultViewModelFactory(app.repository))
    val historyViewModel: HistoryViewModel = viewModel(factory = HistoryViewModelFactory(app.repository))

    NavHost(
        navController = navController,
        startDestination = "home",
        modifier = Modifier.fillMaxSize()
    ) {
        composable("home") {
            HomeScreen(
                viewModel = homeViewModel,
                onNavigateToCamera = { navController.navigate("camera") },
                onNavigateToUpload = { navController.navigate("upload") },
                onNavigateToHistory = { navController.navigate("history") },
                onNavigateToProfile = { navController.navigate("profile") }
            )
        }

        composable("camera") {
            CameraScreen(
                viewModel = cameraViewModel,
                poseDetector = app.poseDetector,
                onNavigateToResult = { scanId ->
                    navController.navigate("result/$scanId") {
                        popUpTo("home")
                    }
                }
            )
        }

        composable("upload") {
            UploadScreen(
                viewModel = uploadViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToResult = { scanId ->
                    navController.navigate("result/$scanId") {
                        popUpTo("home")
                    }
                }
            )
        }

        composable(
            route = "result/{scanId}",
            arguments = listOf(navArgument("scanId") { type = NavType.IntType })
        ) { backStackEntry ->
            val scanId = backStackEntry.arguments?.getInt("scanId") ?: 0
            ResultScreen(
                scanId = scanId,
                viewModel = resultViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("history") {
            HistoryScreen(
                viewModel = historyViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToResult = { scanId -> navController.navigate("result/$scanId") },
                onNavigateToCamera = { navController.navigate("camera") },
                onNavigateToProfile = { navController.navigate("profile") }
            )
        }

        composable("profile") {
            ProfileScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToCamera = { navController.navigate("camera") },
                onNavigateToHistory = { navController.navigate("history") },
                onClearAllScans = {
                    historyViewModel.clearAllHistory()
                    navController.navigate("home") {
                        popUpTo("home") { inclusive = true }
                    }
                }
            )
        }
    }
}
