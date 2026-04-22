package app.krafted.tigerluckysnap

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import app.krafted.tigerluckysnap.model.Difficulty
import app.krafted.tigerluckysnap.model.GameMode
import app.krafted.tigerluckysnap.ui.GameOverScreen
import app.krafted.tigerluckysnap.ui.GameScreen
import app.krafted.tigerluckysnap.ui.HomeScreen
import app.krafted.tigerluckysnap.ui.LeaderboardScreen
import app.krafted.tigerluckysnap.ui.SplashScreen
import app.krafted.tigerluckysnap.ui.theme.TigerLuckySnapTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TigerLuckySnapTheme {
                TigerApp()
            }
        }
    }
}

@Composable
fun TigerApp() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "splash") {
        composable("splash") {
            SplashScreen(onNavigateToHome = {
                navController.navigate("home") {
                    popUpTo("splash") { inclusive = true }
                }
            })
        }
        composable("home") {
            HomeScreen(
                onStartGame = { mode, difficulty ->
                    navController.navigate("game/${mode.name}/${difficulty.name}")
                },
                onViewLeaderboard = { navController.navigate("leaderboard") }
            )
        }
        composable(
            route = "game/{mode}/{difficulty}",
            arguments = listOf(
                navArgument("mode") { type = NavType.StringType },
                navArgument("difficulty") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val mode = GameMode.valueOf(
                backStackEntry.arguments?.getString("mode") ?: GameMode.SOLO.name
            )
            val difficulty = Difficulty.valueOf(
                backStackEntry.arguments?.getString("difficulty") ?: Difficulty.MEDIUM.name
            )
            GameScreen(
                mode = mode,
                difficulty = difficulty,
                onGameOver = { score ->
                    navController.navigate("gameOver/$score/${mode.name}/${difficulty.name}") {
                        popUpTo("home")
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            route = "gameOver/{score}/{mode}/{difficulty}",
            arguments = listOf(
                navArgument("score") { type = NavType.IntType },
                navArgument("mode") { type = NavType.StringType },
                navArgument("difficulty") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val score = backStackEntry.arguments?.getInt("score") ?: 0
            val mode =
                GameMode.valueOf(backStackEntry.arguments?.getString("mode") ?: GameMode.SOLO.name)
            val difficulty = Difficulty.valueOf(
                backStackEntry.arguments?.getString("difficulty") ?: Difficulty.MEDIUM.name
            )
            GameOverScreen(
                score = score,
                gameMode = mode,
                onPlayAgain = {
                    navController.navigate("game/${mode.name}/${difficulty.name}") {
                        popUpTo("gameOver/{score}/{mode}/{difficulty}") { inclusive = true }
                    }
                },
                onHome = {
                    navController.navigate("home") {
                        popUpTo("home") { inclusive = false }
                    }
                }
            )
        }
        composable("leaderboard") {
            LeaderboardScreen(onBack = { navController.popBackStack() })
        }
    }
}
