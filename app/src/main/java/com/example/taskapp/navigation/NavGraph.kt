package com.example.taskapp.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
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
import com.example.taskapp.ui.screens.OnboardingScreen
import com.example.taskapp.ui.screens.DeveloperOptionsScreen

// Определение маршрутов навигации
sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Onboarding : Screen("onboarding")
    object Teachers : Screen("teachers")
    object Tasks : Screen("tasks/{teacherId}") {
        fun createRoute(teacherId: Long) = "tasks/$teacherId"
    }
    object Settings : Screen("settings")
    object About : Screen("about")
    object DeveloperOptions : Screen("developer_options")
    object ViewOnboarding : Screen("view_onboarding") // Просмотр онбординга без сброса статуса
}

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Splash.route,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    key: Int = 0, // Ключ для принудительной перекомпозиции
    onOnboardingComplete: () -> Unit // Колбэк для завершения онбординга
) {
    // Оптимизация: используем более быстрые и менее ресурсоемкие анимации
    val fastFadeIn = fadeIn(animationSpec = tween(200))
    val fastFadeOut = fadeOut(animationSpec = tween(200))
    
    // Красивая анимация перехода от онбординга к главной странице
    val onboardingExitTransition: ExitTransition = fadeOut(
        animationSpec = tween(300, easing = LinearOutSlowInEasing)
    ) + slideOutHorizontally(
        animationSpec = tween(500, easing = FastOutSlowInEasing),
        targetOffsetX = { -it / 2 }
    )
    
    // Красивая анимация входа на главную страницу
    val teachersEnterTransition: EnterTransition = fadeIn(
        animationSpec = tween(400, delayMillis = 100, easing = LinearOutSlowInEasing)
    ) + scaleIn(
        animationSpec = tween(500, delayMillis = 100, easing = FastOutSlowInEasing),
        initialScale = 0.9f
    )
    
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
                    // При первом запуске направляем на онбординг, иначе на главный экран
                    val destination = if (shouldShowOnboarding) Screen.Onboarding.route else Screen.Teachers.route
                    navController.navigate(destination) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }
        
        // Экран онбординга
        composable(
            route = Screen.Onboarding.route,
            enterTransition = { fastFadeIn },
            exitTransition = { onboardingExitTransition }
        ) {
            OnboardingScreen(
                onOnboardingFinished = {
                    // Отмечаем, что онбординг завершен
                    onOnboardingComplete()
                    // Переходим на главный экран с красивой анимацией
                    navController.navigate(Screen.Teachers.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }
        
        // Экран просмотра онбординга без сброса статуса
        composable(
            route = Screen.ViewOnboarding.route,
            enterTransition = { fastFadeIn },
            exitTransition = { fastFadeOut }
        ) {
            OnboardingScreen(
                onOnboardingFinished = {
                    // Просто возвращаемся назад
                    navController.popBackStack()
                }
            )
        }
        
        // Экран списка преподавателей
        composable(
            route = Screen.Teachers.route,
            enterTransition = { 
                // Используем специальную анимацию при переходе с онбординга
                if (initialState.destination?.route == Screen.Onboarding.route) {
                    teachersEnterTransition
                } else {
                    fastFadeIn
                }
            },
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
                onNavigateToAbout = { navController.navigate(Screen.About.route) },
                onNavigateToDeveloperOptions = { navController.navigate(Screen.DeveloperOptions.route) }
            )
        }
        
        // Экран настроек разработчика
        composable(
            route = Screen.DeveloperOptions.route,
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
            DeveloperOptionsScreen(
                onNavigateBack = { navController.popBackStack() },
                isDarkTheme = isDarkTheme,
                onShowOnboarding = { navController.navigate(Screen.ViewOnboarding.route) }
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
                isDarkTheme = isDarkTheme,
                onNavigateToDeveloperOptions = { navController.navigate(Screen.DeveloperOptions.route) }
            )
        }
    }
}

// Переменная для определения, нужно ли показывать онбординг
// В реальном приложении это значение будет загружаться из настроек
private var shouldShowOnboarding = true

// Функция для установки флага отображения онбординга
fun setShouldShowOnboarding(show: Boolean) {
    shouldShowOnboarding = show
} 