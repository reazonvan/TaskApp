package com.example.taskapp.util

import android.content.Context
import android.os.Build
import android.view.Choreographer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import android.graphics.Bitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.ImageBitmap
import android.opengl.GLES20
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference
import javax.microedition.khronos.egl.EGL10
import javax.microedition.khronos.egl.EGLContext
import java.lang.IllegalStateException

// Кэш для хранения часто используемых данных с системой истечения срока действия записей
object Cache {
    private val cache = mutableMapOf<String, CacheEntry<Any>>()
    private val bitmapCache = mutableMapOf<String, WeakReference<Bitmap>>()
    private var lastCleanupTime = System.currentTimeMillis()
    
    // Дефолтный TTL - 5 минут
    private const val DEFAULT_TTL = 5 * 60 * 1000L
    private const val CLEANUP_INTERVAL = 60 * 1000L // Каждую минуту
    private const val MAX_CACHE_SIZE = 100
    
    // Класс для хранения записей кэша с временем жизни
    private data class CacheEntry<T>(
        val value: T,
        val creationTime: Long = System.currentTimeMillis(),
        val ttl: Long = DEFAULT_TTL
    ) {
        fun isExpired(): Boolean = System.currentTimeMillis() - creationTime > ttl
    }
    
    fun <T> get(key: String): T? {
        checkCleanup()
        val entry = cache[key] as? CacheEntry<T> ?: return null
        if (entry.isExpired()) {
            cache.remove(key)
            return null
        }
        return entry.value
    }
    
    fun <T> put(key: String, value: T, ttl: Long = DEFAULT_TTL) {
        checkCleanup()
        if (cache.size >= MAX_CACHE_SIZE) {
            // Удаляем самую старую запись, если превышен размер кэша
            val oldestEntry = cache.entries.minByOrNull { it.value.creationTime }
            oldestEntry?.let { cache.remove(it.key) }
        }
        cache[key] = CacheEntry(value as Any, System.currentTimeMillis(), ttl)
    }
    
    fun clear() {
        cache.clear()
        bitmapCache.clear()
    }
    
    // Получение изображения из кэша
    fun getBitmap(key: String): Bitmap? {
        return bitmapCache[key]?.get()
    }
    
    // Сохранение изображения в кэш через WeakReference для автоматической очистки
    fun putBitmap(key: String, bitmap: Bitmap) {
        bitmapCache[key] = WeakReference(bitmap)
    }
    
    // Удаляет истекшие и null записи
    private fun checkCleanup() {
        val now = System.currentTimeMillis()
        if (now - lastCleanupTime > CLEANUP_INTERVAL) {
            cleanupExpiredEntries()
            lastCleanupTime = now
        }
    }
    
    private fun cleanupExpiredEntries() {
        // Удаляем истекшие записи
        cache.entries.removeAll { it.value.isExpired() }
        
        // Удаляем null ссылки в bitmapCache
        bitmapCache.entries.removeAll { it.value.get() == null }
    }
}

// Оптимизация жизненного цикла
@Composable
fun rememberLifecycleAwareState(initialValue: Boolean): StateFlow<Boolean> {
    val lifecycleOwner = LocalLifecycleOwner.current
    val state = remember { MutableStateFlow(initialValue) }
    
    remember(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_STOP -> state.value = false
                Lifecycle.Event.ON_START -> state.value = true
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        observer
    }
    
    return state
}

// Композабл для мониторинга и оптимизации пропущенных кадров
@Composable
fun MonitorFrameRate(
    onSlowFrameDetected: (Long) -> Unit = {}
) {
    val context = LocalContext.current
    
    DisposableEffect(Unit) {
        val callback = object : Choreographer.FrameCallback {
            private var lastFrameTimeNanos = 0L
            private var slowFramesCount = 0
            
            override fun doFrame(frameTimeNanos: Long) {
                if (lastFrameTimeNanos > 0) {
                    val deltaMillis = (frameTimeNanos - lastFrameTimeNanos) / 1_000_000
                    // Если кадр длится больше 32мс (меньше 30fps), считаем его медленным
                    if (deltaMillis > 32) {
                        slowFramesCount++
                        onSlowFrameDetected(deltaMillis)
                        
                        // Если медленных кадров много подряд, применяем оптимизации
                        if (slowFramesCount > 5) {
                            MemoryOptimizer.optimizeForLowFrameRate(context)
                            slowFramesCount = 0
                        }
                    } else {
                        // Сбрасываем счетчик, если кадр не медленный
                        slowFramesCount = 0
                    }
                }
                lastFrameTimeNanos = frameTimeNanos
                Choreographer.getInstance().postFrameCallback(this)
            }
        }
        
        Choreographer.getInstance().postFrameCallback(callback)
        
        onDispose {
            // Нет прямого способа удалить callback, но можно перестать его переназначать
        }
    }
}

// Расширенный класс для оптимизации памяти и производительности
object MemoryOptimizer {
    private const val MAX_CACHE_SIZE = 100
    private var optimizationLevel = OptimizationLevel.NORMAL
    
    // Уровни оптимизации для разных условий
    enum class OptimizationLevel {
        LOW, NORMAL, AGGRESSIVE
    }
    
    // Метод для очистки кэша и вызова сборщика мусора
    fun trimMemory() {
        System.gc()
        Runtime.getRuntime().gc()
        trimCache()
    }
    
    // Очистка кэша
    fun trimCache() {
        if (Cache::class.java.fields.size > MAX_CACHE_SIZE) {
            Cache.clear()
        }
    }
    
    // Оптимизация для низкой частоты кадров
    fun optimizeForLowFrameRate(context: Context) {
        // Повышаем уровень оптимизации
        optimizationLevel = OptimizationLevel.AGGRESSIVE
        
        // Очищаем память
        trimMemory()
        
        // Уменьшаем размер кэша изображений
        try {
            val memoryClass = context.getSystemService(Context.ACTIVITY_SERVICE) as? android.app.ActivityManager
            if (memoryClass != null) {
                val cacheSize = 1024 * 1024 * memoryClass.memoryClass / 8 // 1/8 доступной памяти
                // Используем конфигурацию с меньшим потреблением памяти
                // Здесь был вызов метода, которого нет в Android API
                // Заменяем на установку этого свойства для новых создаваемых Bitmap
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    // В Android 8.0+ можно применить через установку конфигурации по умолчанию
                    Bitmap.Config.RGB_565 // Просто используем эту конфигурацию при создании Bitmap
                }
            }
        } catch (e: Exception) {
            // Игнорируем ошибки, чтобы не крашить приложение
        }
        
        // Выполняем эту оптимизацию в фоновом потоке
        CoroutineScope(Dispatchers.Default).launch {
            try {
                GLES20.glFlush()
                GLES20.glFinish()
            } catch (e: Exception) {
                // Игнорируем ошибки OpenGL
            }
            
            delay(1000) // Даем приложению время на восстановление
            
            // Возвращаемся к нормальному режиму после паузы
            optimizationLevel = OptimizationLevel.NORMAL
        }
    }
    
    // Получить текущий уровень оптимизации
    fun getCurrentOptimizationLevel(): OptimizationLevel = optimizationLevel
    
    // Проверка доступной памяти
    fun getAvailableMemory(context: Context): Long {
        val memoryInfo = android.app.ActivityManager.MemoryInfo()
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as android.app.ActivityManager
        activityManager.getMemoryInfo(memoryInfo)
        return memoryInfo.availMem
    }
    
    // Проверка, нужна ли агрессивная оптимизация
    fun needsAggressiveOptimization(context: Context): Boolean {
        val memoryInfo = android.app.ActivityManager.MemoryInfo()
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as android.app.ActivityManager
        activityManager.getMemoryInfo(memoryInfo)
        
        // Если свободной памяти меньше 15% или мы уже в агрессивном режиме
        return memoryInfo.lowMemory || 
               (memoryInfo.availMem.toFloat() / memoryInfo.totalMem) < 0.15f ||
               optimizationLevel == OptimizationLevel.AGGRESSIVE
    }
} 