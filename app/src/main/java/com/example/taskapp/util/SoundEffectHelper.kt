package com.example.taskapp.util

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

/**
 * Вспомогательный класс для воспроизведения звуковых эффектов в приложении
 */
class SoundEffectHelper {
    private var soundPool: SoundPool? = null
    private var swapSoundId: Int = 0
    private var successSoundId: Int = 0
    private var notificationSoundId: Int = 0
    
    /**
     * Инициализация SoundPool и загрузка звуковых эффектов
     */
    fun initialize(context: Context) {
        // Инициализируем SoundPool
        val attributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        
        soundPool = SoundPool.Builder()
            .setMaxStreams(5)
            .setAudioAttributes(attributes)
            .build()
        
        // Проверяем наличие ресурсов
        val hasSwapSound = resourceExists(context, "swap_sound", "raw")
        val hasSuccessSound = resourceExists(context, "success_sound", "raw")
        val hasNotificationSound = resourceExists(context, "notification_sound", "raw")
        
        // Загружаем звуковые эффекты из ресурсов
        try {
            // Звук смены элементов
            if (hasSwapSound) {
                val swapSoundResId = getResourceId(context, "swap_sound", "raw")
                if (swapSoundResId > 0) {
                    swapSoundId = soundPool?.load(context, swapSoundResId, 1) ?: 0
                }
            }
            
            // Звук успешного действия
            if (hasSuccessSound) {
                val successSoundResId = getResourceId(context, "success_sound", "raw")
                if (successSoundResId > 0) {
                    successSoundId = soundPool?.load(context, successSoundResId, 1) ?: 0
                }
            }
            
            // Звук уведомления
            if (hasNotificationSound) {
                val notificationSoundResId = getResourceId(context, "notification_sound", "raw")
                if (notificationSoundResId > 0) {
                    notificationSoundId = soundPool?.load(context, notificationSoundResId, 1) ?: 0
                }
            }
        } catch (e: Exception) {
            // Если ресурс не найден, просто логируем ошибку
            android.util.Log.e("SoundEffectHelper", "Error loading sound: ${e.message}")
        }
    }
    
    /**
     * Проверка наличия ресурса в приложении
     */
    private fun resourceExists(context: Context, resourceName: String, resourceType: String): Boolean {
        val resourceId = context.resources.getIdentifier(resourceName, resourceType, context.packageName)
        return resourceId != 0
    }
    
    /**
     * Получение идентификатора ресурса
     */
    private fun getResourceId(context: Context, resourceName: String, resourceType: String): Int {
        return context.resources.getIdentifier(resourceName, resourceType, context.packageName)
    }
    
    /**
     * Освобождение ресурсов SoundPool
     */
    fun release() {
        soundPool?.release()
        soundPool = null
    }
    
    /**
     * Воспроизведение звука смены элементов
     */
    fun playSwapSound() {
        if (swapSoundId > 0) {
            soundPool?.play(swapSoundId, 0.7f, 0.7f, 1, 0, 1.0f)
        }
    }
    
    /**
     * Воспроизведение звука успешного действия
     */
    fun playSuccessSound() {
        if (successSoundId > 0) {
            soundPool?.play(successSoundId, 1.0f, 1.0f, 1, 0, 1.0f)
        }
    }
    
    /**
     * Воспроизведение звука уведомления
     */
    fun playNotificationSound() {
        if (notificationSoundId > 0) {
            soundPool?.play(notificationSoundId, 0.8f, 0.8f, 1, 0, 1.0f)
        }
    }
}

/**
 * Composable-функция для управления жизненным циклом SoundEffectHelper
 */
@Composable
fun rememberSoundEffectHelper(): SoundEffectHelper {
    val context = LocalContext.current
    val soundHelper = remember { SoundEffectHelper() }
    
    // Инициализация и освобождение ресурсов при добавлении/удалении из композиции
    DisposableEffect(soundHelper) {
        soundHelper.initialize(context)
        onDispose {
            soundHelper.release()
        }
    }
    
    return soundHelper
} 