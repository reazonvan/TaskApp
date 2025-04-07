package com.example.taskapp.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.taskapp.MainActivity
import com.example.taskapp.R
import java.util.concurrent.Executors

/**
 * Класс для отправки тестовых уведомлений
 */
object TestNotificationManager {
    // Константы для уведомлений
    private const val CHANNEL_ID = "test_notifications"
    private const val CHANNEL_NAME = "Тестовые уведомления"
    private const val CHANNEL_DESCRIPTION = "Канал для тестовых уведомлений"
    private const val TEST_NOTIFICATION_ID = 1000
    private const val DELAYED_NOTIFICATION_ID = 1001
    
    // Исполнитель для фоновых задач
    private val executor = Executors.newSingleThreadExecutor()
    private val handler = Handler(Looper.getMainLooper())
    
    /**
     * Инициализация канала уведомлений
     */
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESCRIPTION
            }
            
            // Регистрация канала
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) 
                as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    /**
     * Отправка тестового уведомления
     */
    fun sendTestNotification(context: Context) {
        // Создаем канал уведомлений, если он еще не создан
        createNotificationChannel(context)
        
        // Intent для открытия приложения при нажатии на уведомление
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent, 
            PendingIntent.FLAG_IMMUTABLE
        )
        
        // Создаем уведомление
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.app_icon) // Убедитесь, что такой drawable существует
            .setContentTitle("Тестовое уведомление")
            .setContentText("Это тестовое уведомление для отладки")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
        
        // Показываем уведомление
        with(NotificationManagerCompat.from(context)) {
            try {
                notify(TEST_NOTIFICATION_ID, builder.build())
            } catch (e: SecurityException) {
                // Обработка случая, когда разрешения не предоставлены
                e.printStackTrace()
            }
        }
    }
    
    /**
     * Отправка отложенного уведомления через собственный исполнитель
     */
    fun scheduleDelayedNotification(context: Context, delaySeconds: Int = 10) {
        // Создаем канал уведомлений, если он еще не создан
        createNotificationChannel(context)
        
        // Используем собственный executor для отложенного уведомления
        executor.execute {
            try {
                // Ждем указанное время
                Thread.sleep(delaySeconds * 1000L)
                
                // Затем отправляем уведомление
                handler.post {
                    sendDelayedNotification(context)
                }
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }
    }
    
    /**
     * Отправка отложенного уведомления
     */
    private fun sendDelayedNotification(context: Context) {
        // Intent для открытия приложения при нажатии на уведомление
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent, 
            PendingIntent.FLAG_IMMUTABLE
        )
        
        // Создаем уведомление
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.app_icon) // Убедитесь, что такой drawable существует
            .setContentTitle("Отложенное уведомление")
            .setContentText("Это отложенное уведомление отправлено через 10 секунд")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
        
        // Показываем уведомление
        with(NotificationManagerCompat.from(context)) {
            try {
                notify(DELAYED_NOTIFICATION_ID, builder.build())
            } catch (e: SecurityException) {
                // Обработка случая, когда разрешения не предоставлены
                e.printStackTrace()
            }
        }
    }
} 