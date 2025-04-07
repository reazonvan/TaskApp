package com.example.taskapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.taskapp.ui.theme.TaskAppTheme
import com.example.taskapp.ui.theme.AppTheme
import com.example.taskapp.data.repository.AppSettings
import com.example.taskapp.data.repository.SettingsRepository
import com.example.taskapp.navigation.NavGraph
import com.example.taskapp.navigation.Screen
import com.example.taskapp.ui.components.FloatingBackground
import com.example.taskapp.util.MemoryOptimizer
import com.example.taskapp.util.MonitorFrameRate
import com.example.taskapp.util.Cache
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.taskapp.ui.viewmodels.ThemeViewModel
import com.example.taskapp.ui.viewmodels.SettingsViewModel
import com.example.taskapp.navigation.setShouldShowOnboarding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.media.MediaPlayer
import androidx.core.view.WindowCompat

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val TAG = "MainActivity"
    // Настройки оптимизации
    private var isLowPerformanceMode = false
    
    @Inject
    lateinit var settingsRepository: SettingsRepository
    
    // Регистрируем обработчик запроса разрешений
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Log.d(TAG, "Разрешение на отправку уведомлений получено")
        } else {
            Log.d(TAG, "Разрешение на отправку уведомлений отклонено")
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Применяем оптимизации для улучшения времени запуска
        window.setBackgroundDrawable(null)
        
        // Настраиваем прозрачный статус бар и полноэкранный режим
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        // Предзагружаем часто используемые ресурсы в кэш
        preloadCommonResources()
        
        // Запрашиваем разрешение на отправку уведомлений для Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        
        // Запускаем периодическую очистку памяти в фоновом потоке с оптимизированной периодичностью
        lifecycleScope.launch(Dispatchers.Default) {
            while (isActive) {
                delay(60000) // Проверяем каждую минуту вместо 30 секунд
                MemoryOptimizer.trimMemory()
                
                // Проверяем необходимость агрессивной оптимизации
                isLowPerformanceMode = MemoryOptimizer.needsAggressiveOptimization(this@MainActivity)
                Log.d(TAG, "Память: ${MemoryOptimizer.getAvailableMemory(this@MainActivity) / 1048576}MB, " +
                        "Режим низкой производительности: $isLowPerformanceMode")
            }
        }
        
        setContent {
            // Создаем ViewModel через Hilt
            val themeViewModel: ThemeViewModel = viewModel()
            val settingsViewModel: SettingsViewModel = viewModel()
            val isDarkTheme by themeViewModel.isDarkTheme.collectAsState()
            
            // Получаем настройки через LaunchedEffect для предотвращения лишних перекомпозиций
            var animationIntensity by remember { mutableStateOf(0.5f) }
            var textSize by remember { mutableStateOf(1) }
            var simplifiedMode by remember { mutableStateOf(false) }
            var onboardingCompleted by remember { mutableStateOf(false) }
            
            LaunchedEffect(Unit) {
                settingsViewModel.animationIntensity.collect { 
                    animationIntensity = it
                }
            }
            
            LaunchedEffect(Unit) {
                settingsViewModel.textSize.collect {
                    textSize = it
                }
            }
            
            LaunchedEffect(Unit) {
                settingsViewModel.simplifiedMode.collect {
                    simplifiedMode = it
                }
            }
            
            LaunchedEffect(Unit) {
                settingsViewModel.onboardingCompleted.collect {
                    onboardingCompleted = it
                }
            }
            
            // Устанавливаем флаг онбординга в зависимости от настроек
            setShouldShowOnboarding(!onboardingCompleted)
            
            // Если включен упрощенный режим, принудительно активируем режим экономии ресурсов
            val effectiveLowPerformanceMode = isLowPerformanceMode || simplifiedMode
            
            // Создаем объект настроек для передачи в тему
            val appSettings = remember(animationIntensity, textSize, simplifiedMode) {
                AppSettings(
                    colorScheme = 0,
                    animationIntensity = animationIntensity,
                    textSize = textSize,
                    simplifiedMode = simplifiedMode
                )
            }
            
            AppTheme(
                isDarkTheme = isDarkTheme,
                appSettings = appSettings
            ) {
                // Мониторинг частоты кадров с более высоким порогом задержки
                MonitorFrameRate(triggerThresholdMs = 50) { slowFrameTime ->
                    Log.d(TAG, "Медленный кадр: $slowFrameTime мс")
                }
                
                // Основной контент приложения
                AppContent(
                    isLowPerformanceMode = effectiveLowPerformanceMode,
                    isDarkTheme = isDarkTheme,
                    onToggleTheme = themeViewModel::toggleTheme,
                    animationIntensity = animationIntensity,
                    onOnboardingComplete = { settingsViewModel.setOnboardingCompleted(true) }
                )
            }
        }
    }
    
    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        // Реагируем на системные запросы очистки памяти
        when (level) {
            ComponentActivity.TRIM_MEMORY_RUNNING_MODERATE,
            ComponentActivity.TRIM_MEMORY_RUNNING_LOW,
            ComponentActivity.TRIM_MEMORY_RUNNING_CRITICAL -> {
                MemoryOptimizer.trimCache()
            }
            ComponentActivity.TRIM_MEMORY_UI_HIDDEN -> {
                MemoryOptimizer.trimMemory()
            }
            ComponentActivity.TRIM_MEMORY_BACKGROUND,
            ComponentActivity.TRIM_MEMORY_MODERATE,
            ComponentActivity.TRIM_MEMORY_COMPLETE -> {
                MemoryOptimizer.trimMemory()
                isLowPerformanceMode = true
            }
        }
    }
    
    override fun onLowMemory() {
        super.onLowMemory()
        // При критически низкой памяти агрессивно очищаем ресурсы
        MemoryOptimizer.trimMemory()
        isLowPerformanceMode = true
    }

    // Метод для предзагрузки часто используемых ресурсов
    private fun preloadCommonResources() {
        // Запускаем загрузку в фоновом потоке, чтобы не задерживать UI
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Предзагружаем ресурсы для Easter Egg и часто используемые изображения
                val resourcesToPreload = listOf(
                    R.drawable.easter_egg_image,
                    R.mipmap.ic_launcher,
                    // Добавьте другие часто используемые изображения
                )
                Cache.preloadResources(this@MainActivity, resourcesToPreload)
                
                // Предзагружаем звуки
                val mediaPlayer = MediaPlayer().apply {
                    try {
                        setDataSource(resources.openRawResourceFd(R.raw.easter_egg_sound))
                        prepareAsync() // Асинхронная подготовка
                        setOnPreparedListener {
                            // Сразу освобождаем ресурсы после предзагрузки
                            it.reset()
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Ошибка предзагрузки звука: ${e.message}")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Ошибка предзагрузки ресурсов: ${e.message}")
            }
        }
    }
}

// Кэшируем спецификации анимаций для предотвращения создания новых объектов при перекомпозиции
@Composable
fun AppContent(
    isLowPerformanceMode: Boolean,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    animationIntensity: Float,
    onOnboardingComplete: () -> Unit
) {
    // Используем remember для кэширования контроллера навигации
    val navController = rememberNavController()
    
    // Отслеживаем текущий маршрут
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    
    // Определяем, должен ли быть виден фон с анимацией на текущем экране
    val shouldShowAnimatedBackground = remember(currentRoute) {
        // Не показываем анимированный фон на экранах с тяжелым контентом
        when (currentRoute) {
            Screen.Onboarding.route -> false // На онбординге отключаем анимацию
            else -> true
        }
    }
    
    // Обновляем UI при изменении маршрута
    val recomposeKey = remember { mutableStateOf(0) }
    
    // Обновляем UI при возвращении на главный экран
    LaunchedEffect(currentRoute) {
        if (currentRoute == Screen.Teachers.route) {
            recomposeKey.value++
        }
    }
    
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        // Применяем оптимизированный фон с плавающими элементами только когда нужно
        if (shouldShowAnimatedBackground) {
            // Параметры оптимизации зависят от режима производительности и интенсивности анимации
            val effectiveParticleCount = remember(isLowPerformanceMode, animationIntensity) {
                when {
                    isLowPerformanceMode -> 5 // Минимум частиц в режиме низкой производительности
                    animationIntensity < 0.2f -> 8 // Очень мало частиц при малой интенсивности
                    animationIntensity < 0.5f -> 12 
                    animationIntensity < 0.8f -> 20
                    else -> 30
                }
            }
            
            val effectiveAnimationDuration = remember(isLowPerformanceMode, animationIntensity) {
                when {
                    isLowPerformanceMode -> 15000 // Медленная анимация в режиме низкой производительности
                    animationIntensity < 0.2f -> 12000 
                    animationIntensity < 0.5f -> 10000
                    animationIntensity < 0.8f -> 8000
                    else -> 6000
                }
            }
            
            FloatingBackground(
                modifier = Modifier.fillMaxSize(),
                optimized = isLowPerformanceMode,
                particleCount = effectiveParticleCount,
                animationDuration = effectiveAnimationDuration
            ) {
                NavGraph(
                    navController = navController,
                    isDarkTheme = isDarkTheme,
                    onToggleTheme = onToggleTheme,
                    key = recomposeKey.value,
                    onOnboardingComplete = onOnboardingComplete
                )
            }
        } else {
            // Без анимированного фона
            NavGraph(
                navController = navController,
                isDarkTheme = isDarkTheme,
                onToggleTheme = onToggleTheme,
                key = recomposeKey.value,
                onOnboardingComplete = onOnboardingComplete
            )
        }
        
        // Запускаем периодическую очистку unused композаблов только в режиме низкой производительности
        LaunchedEffect(isLowPerformanceMode) {
            if (isLowPerformanceMode) {
                launch(Dispatchers.Default) {
                    while (isActive) {
                        delay(30000) // каждые 30 секунд
                        System.gc() // вызываем GC только в режиме низкой производительности
                    }
                }
            }
        }
    }
} 