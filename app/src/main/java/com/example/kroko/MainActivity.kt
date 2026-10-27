package com.example.kroko

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CrocodileApp()
                }
            }
        }
    }
}

sealed class Screen(val route: String) {
    object Setup : Screen("setup")
    object TeamPreview : Screen("team_preview")
    object Game : Screen("game")
    object Scoreboard : Screen("scoreboard")
}

@Composable
fun CrocodileApp(gameViewModel: GameViewModel = viewModel()) {
    val navController = rememberNavController()
    val uiState by gameViewModel.uiState.collectAsStateWithLifecycle()

    NavHost(
        navController = navController,
        startDestination = Screen.Setup.route
    ) {
        composable(Screen.Setup.route) {
            SetupScreen(
                onStartGame = { players, targetScore ->
                    gameViewModel.setupGame(players, targetScore)
                    navController.navigate(Screen.TeamPreview.route) {
                        popUpTo(Screen.Setup.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.TeamPreview.route) {
            TeamPreviewScreen(
                teams = uiState.teams,
                onStartActualGame = {
                    navController.navigate(Screen.Game.route) {
                        popUpTo(Screen.TeamPreview.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Game.route) {
            GameScreen(
                uiState = uiState,
                onStartTimer = { gameViewModel.startTimer() },
                onGuessed = {
                    gameViewModel.onWordGuessed()
                    if (uiState.gameOver) {
                        navController.navigate(Screen.Scoreboard.route)
                    }
                },
                onPass = { gameViewModel.onTimeExpired() },
                onNextTurn = {
                    if (uiState.gameOver) {
                        navController.navigate(Screen.Scoreboard.route)
                    } else {
                        gameViewModel.nextTurn()
                    }
                },
                onGoToScoreboard = { navController.navigate(Screen.Scoreboard.route) }
            )
        }

        composable(Screen.Scoreboard.route) {
            ScoreboardScreen(
                uiState = uiState,
                onContinueGame = { navController.popBackStack() },
                onResetGame = {
                    gameViewModel.resetGame()
                    navController.navigate(Screen.Setup.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}