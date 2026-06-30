package dev.whooslizi.screenposter.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import dev.whooslizi.screenposter.ui.screens.album.AlbumScreen
import dev.whooslizi.screenposter.ui.screens.editor.EditorScreen
import dev.whooslizi.screenposter.ui.screens.home.HomeScreen
import dev.whooslizi.screenposter.ui.screens.preview.PreviewScreen
import dev.whooslizi.screenposter.ui.screens.settings.SettingsScreen

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Album : Screen("album/{albumId}") {
        fun createRoute(albumId: Long) = "album/$albumId"
    }
    object Editor : Screen("editor/{wallpaperId}") {
        fun createRoute(wallpaperId: Long) = "editor/$wallpaperId"
    }
    object Preview : Screen("preview/{wallpaperId}") {
        fun createRoute(wallpaperId: Long) = "preview/$wallpaperId"
    }
    object Settings : Screen("settings")
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Screen.Home.route) {
        composable(Screen.Home.route) {
            HomeScreen(navController)
        }
        
        composable(
            route = Screen.Album.route,
            arguments = listOf(navArgument("albumId") { type = NavType.LongType })
        ) { backStackEntry ->
            val albumId = backStackEntry.arguments?.getLong("albumId") ?: -1L
            AlbumScreen(navController, albumId)
        }
        
        composable(
            route = Screen.Editor.route,
            arguments = listOf(navArgument("wallpaperId") { type = NavType.LongType })
        ) { backStackEntry ->
            val wallpaperId = backStackEntry.arguments?.getLong("wallpaperId") ?: -1L
            EditorScreen(navController, wallpaperId)
        }
        
        composable(
            route = Screen.Preview.route,
            arguments = listOf(navArgument("wallpaperId") { type = NavType.LongType })
        ) { backStackEntry ->
            val wallpaperId = backStackEntry.arguments?.getLong("wallpaperId") ?: -1L
            PreviewScreen(navController, wallpaperId)
        }
        
        composable(Screen.Settings.route) {
            SettingsScreen(navController)
        }
    }
}
