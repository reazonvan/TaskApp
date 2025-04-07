package com.example.taskapp

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.taskapp.ui.theme.TaskAppTheme
import com.example.taskapp.navigation.NavGraph
import com.example.taskapp.navigation.Screen
import com.example.taskapp.ui.components.FloatingBackground
import com.example.taskapp.util.MemoryOptimizer
import com.example.taskapp.util.MonitorFrameRate
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.taskapp.ui.viewmodels.ThemeViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val TAG = "MainActivity"
    // Настройки оптимизации
    private var isLowPerformanceMode = false
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Запускаем периодическую очистку памяти в фоновом потоке
        lifecycleScope.launch(Dispatchers.Default) {
            while (isActive) {
                delay(30000) // Проверяем каждые 30 секунд
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
            val isDarkTheme by themeViewModel.isDarkTheme.collectAsState()
            
            TaskAppTheme(darkTheme = isDarkTheme) {
                // Мониторинг частоты кадров
                MonitorFrameRate { slowFrameTime ->
                    Log.d(TAG, "Медленный кадр: $slowFrameTime мс")
                }
                
                // Основной контент приложения
                AppContent(isLowPerformanceMode, isDarkTheme, themeViewModel::toggleTheme)
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
}

// Кэшируем спецификации анимаций для предотвращения создания новых объектов при перекомпозиции
@Composable
fun AppContent(
    isLowPerformanceMode: Boolean,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit
) {
    // Используем remember для кэширования контроллера навигации
    val navController = rememberNavController()
    
    // Отслеживаем текущий маршрут
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    
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
        // Применяем оптимизированный фон с плавающими элементами
        // Параметры оптимизации зависят от режима производительности
                    FloatingBackground(
                        modifier = Modifier.fillMaxSize(),
            optimized = isLowPerformanceMode,
            particleCount = if (isLowPerformanceMode) 15 else 30,
            animationDuration = if (isLowPerformanceMode) 10000 else 6000
        ) {
            NavGraph(
                navController = navController,
                isDarkTheme = isDarkTheme,
                onToggleTheme = onToggleTheme,
                key = recomposeKey.value
            )
        }
        
        // Запускаем периодическую очистку unused композаблов
        LaunchedEffect(Unit) {
            launch(Dispatchers.Default) {
                while (isActive) {
                    delay(15000) // каждые 15 секунд
                    System.gc() // инициируем сборку мусора для очистки неиспользуемых compose объектов
                }
            }
        }
    }
} 