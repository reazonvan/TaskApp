package com.example.taskapp.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.taskapp.ui.screens.AboutScreen
import com.example.taskapp.ui.screens.SplashScreen
import com.example.taskapp.ui.screens.TaskScreen
import com.example.taskapp.ui.screens.TeacherScreen
import com.example.taskapp.ui.screens.TeachersScreen
import com.example.taskapp.ui.screens.SettingsScreen

// Определение маршрутов навигации
sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Teachers : Screen("teachers")
    object Tasks : Screen("tasks/{teacherId}") {
        fun createRoute(teacherId: Long) = "tasks/$teacherId"
    }
    object Settings : Screen("settings")
    object About : Screen("about")
}

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Splash.route,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    key: Int = 0 // Ключ для принудительной перекомпозиции
) {
    // Оптимизация: используем более быстрые и менее ресурсоемкие анимации
    val fastFadeIn = fadeIn(animationSpec = tween(200))
    val fastFadeOut = fadeOut(animationSpec = tween(200))
    
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Сплэш-скрин (стартовый экран)
        composable(
            route = Screen.Splash.route,
            // Для сплэш-скрина только fade-анимации, так как они наиболее производительные
            enterTransition = { fastFadeIn },
            exitTransition = { fastFadeOut }
        ) {
            SplashScreen(
                onSplashFinished = {
                    navController.navigate(Screen.Teachers.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }
        
        // Экран списка преподавателей
        composable(
            route = Screen.Teachers.route,
            enterTransition = { fastFadeIn },
            exitTransition = { fastFadeOut }
        ) {
            TeachersScreen(
                onNavigateToTeacher = { teacherId ->
                    navController.navigate(Screen.Tasks.createRoute(teacherId))
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                },
                isDarkTheme = isDarkTheme,
                onToggleTheme = onToggleTheme
            )
        }
        
        // Экран задач преподавателя
        composable(
            route = Screen.Tasks.route,
            enterTransition = {
                // Упрощаем анимацию для повышения производительности
                slideInHorizontally(
                    initialOffsetX = { it / 2 },
                    animationSpec = tween(200)
                ) + fastFadeIn
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { -it / 2 },
                    animationSpec = tween(200)
                ) + fastFadeOut
            }
        ) { backStackEntry ->
            val teacherId = backStackEntry.arguments?.getString("teacherId")?.toLongOrNull() ?: return@composable
            
            TaskScreen(
                teacherId = teacherId,
                onNavigateBack = { navController.popBackStack() },
                isDarkTheme = isDarkTheme,
                onToggleTheme = onToggleTheme
            )
        }
        
        // Экран настроек
        composable(
            route = Screen.Settings.route,
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { -it / 2 },
                    animationSpec = tween(200)
                ) + fastFadeIn
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { it / 2 },
                    animationSpec = tween(200)
                ) + fastFadeOut
            }
        ) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                isDarkTheme = isDarkTheme,
                onToggleTheme = onToggleTheme,
                onNavigateToAbout = { navController.navigate(Screen.About.route) }
            )
        }
        
        // Экран "О приложении"
        composable(
            route = Screen.About.route,
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { it / 2 },
                    animationSpec = tween(200)
                ) + fastFadeIn
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { -it / 2 },
                    animationSpec = tween(200)
                ) + fastFadeOut
            }
        ) {
            AboutScreen(
                onNavigateBack = { navController.popBackStack() },
                isDarkTheme = isDarkTheme
            )
        }
    }
} 