package com.example.taskapp

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import com.example.taskapp.notifications.TestNotificationManager

@HiltAndroidApp
class TaskApp : Application(), Configuration.Provider {
    @Inject
    lateinit var workerFactory: HiltWorkerFactory
    
    override fun onCreate() {
        super.onCreate()
        
        // Инициализируем канал для тестовых уведомлений
        TestNotificationManager.createNotificationChannel(this)
        
        // Создаем канал уведомлений (требуется для Android 8.0+)
        createNotificationChannel()
    }
    
    // Реализация интерфейса Configuration.Provider
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "deadline_notifications"
            val channelName = "Уведомления о дедлайнах"
            val importance = NotificationManager.IMPORTANCE_HIGH
            
            val channel = NotificationChannel(channelId, channelName, importance).apply {
                description = "Канал для уведомлений о приближающихся дедлайнах"
                enableVibration(true)
                // Добавляем дополнительные настройки для повышения заметности
                setShowBadge(true) // Показывать бейдж на иконке приложения
                enableLights(true) // Включить световой индикатор
                lightColor = getColor(R.color.purple_500) // Установить цвет индикатора
                // Установить вибрацию
                vibrationPattern = longArrayOf(0, 500, 250, 500)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC // Показывать на экране блокировки
                // Не меняем importance, так как она уже установлена при создании канала
                // Включить звук по умолчанию
                setSound(android.provider.Settings.System.DEFAULT_NOTIFICATION_URI, android.media.AudioAttributes.Builder()
                    .setUsage(android.media.AudioAttributes.USAGE_NOTIFICATION)
                    .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build())
            }
            
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
} 