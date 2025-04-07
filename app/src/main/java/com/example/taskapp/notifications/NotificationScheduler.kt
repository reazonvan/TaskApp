package com.example.taskapp.notifications

import androidx.work.*
import com.example.taskapp.data.model.Task
import com.example.taskapp.data.repository.SettingsRepository
import com.example.taskapp.data.repository.TaskRepository
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import android.content.Context
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.taskapp.MainActivity
import com.example.taskapp.R
import com.example.taskapp.data.repository.AppSettings
import com.example.taskapp.util.DateFormatUtil

@Singleton
class NotificationScheduler @Inject constructor(
    private val workManager: WorkManager,
    private val taskRepository: TaskRepository,
    private val settingsRepository: SettingsRepository,
    private val context: Context
) {
    
    companion object {
        private const val WORK_NAME_PREFIX = "deadline_notification_"
        private const val TEST_NOTIFICATION_ID = 999999
    }
    
    /**
     * Планирует уведомление для задачи с учетом глобальных настроек
     */
    suspend fun scheduleNotification(task: Task) {
        if (task.isCompleted || task.deadline == null || task.notificationSent) {
            return
        }
        
        // Получаем настройки приложения
        val settings = settingsRepository.getSettings().first()
        
        // Используем глобальную настройку времени уведомления, если у задачи не установлено свое
        // Или если в настройках указано переопределять время для всех задач
        val effectiveNotifyBeforeMinutes = if (task.useCustomNotificationTime) {
            task.notifyBeforeMinutes
        } else {
            settings.notificationTime
        }
        
        // Вычисляем, когда показать уведомление (за effectiveNotifyBeforeMinutes минут до дедлайна)
        val notificationTime = task.deadline - TimeUnit.MINUTES.toMillis(effectiveNotifyBeforeMinutes.toLong())
        val currentTime = System.currentTimeMillis()
        
        // Если время уведомления уже прошло, не планируем его
        if (notificationTime <= currentTime) {
            return
        }
        
        // Вычисляем задержку в миллисекундах
        val delayMillis = notificationTime - currentTime
        
        // Создаем данные для Worker'а
        val inputData = workDataOf(
            NotificationWorker.TASK_ID_KEY to task.id
        )
        
        // Создаем запрос на выполнение работы с задержкой
        val workRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
            .setInputData(inputData)
            .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
            // Уведомления должны показываться даже при ограничениях системы
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .addTag(task.id.toString())
            .build()
        
        // Планируем работу, используя уникальное имя на основе ID задачи
        workManager.enqueueUniqueWork(
            "$WORK_NAME_PREFIX${task.id}",
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }
    
    /**
     * Отменяет запланированное уведомление
     */
    fun cancelNotification(taskId: Long) {
        workManager.cancelUniqueWork("$WORK_NAME_PREFIX$taskId")
    }
    
    /**
     * Планирует все ожидающие уведомления (для использования при запуске приложения или после перезагрузки)
     */
    suspend fun scheduleAllPendingNotifications() {
        val tasks = taskRepository.getAllActiveTasksWithDeadline()
        tasks.forEach { task -> 
            if (!task.notificationSent && task.deadline != null) {
                scheduleNotification(task)
            }
        }
    }
    
    /**
     * Обновляет время уведомления для задачи
     */
    suspend fun updateNotificationTime(taskId: Long, notifyBeforeMinutes: Int) {
        taskRepository.updateNotificationTime(taskId, notifyBeforeMinutes)
        
        // Отменяем старое уведомление
        cancelNotification(taskId)
        
        // Планируем новое с обновленным временем
        val task = taskRepository.getTaskById(taskId)
        if (task != null) {
            scheduleNotification(task)
        }
    }
    
    /**
     * Перепланирует все уведомления после изменения настроек
     */
    suspend fun rescheduleAllNotificationsAfterSettingsChange() {
        // Отменяем все запланированные уведомления
        val tasks = taskRepository.getAllActiveTasksWithDeadline()
        tasks.forEach { task -> 
            cancelNotification(task.id)
        }
        
        // Заново планируем с новыми настройками
        scheduleAllPendingNotifications()
    }
    
    /**
     * Отправляет тестовое уведомление для проверки работы системы уведомлений
     * @return Пара (успех: Boolean, сообщение об ошибке или null)
     */
    suspend fun sendTestNotification(): Pair<Boolean, String?> {
        // Проверяем разрешение на уведомления для Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val notificationPermissionGranted = context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) == 
                android.content.pm.PackageManager.PERMISSION_GRANTED
            
            if (!notificationPermissionGranted) {
                return Pair(false, "Отсутствует разрешение на отправку уведомлений. " +
                    "Пожалуйста, откройте настройки приложения и предоставьте разрешение на уведомления.")
            }
        }
        
        // Проверяем, включен ли канал уведомлений
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = notificationManager.getNotificationChannel(NotificationWorker.NOTIFICATION_CHANNEL_ID)
            
            if (channel != null && channel.importance == NotificationManager.IMPORTANCE_NONE) {
                return Pair(false, "Уведомления для приложения отключены в настройках системы. " +
                    "Пожалуйста, включите канал 'Уведомления о дедлайнах' в настройках приложения.")
            }
        }
        
        // Получаем настройки приложения
        val settings = settingsRepository.getSettings().first()
        
        // Создаем текст уведомления
        val title = "Тестовое уведомление"
        val description = "Это уведомление отправлено для проверки работы системы уведомлений"
        
        // Intent для открытия приложения при клике
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            TEST_NOTIFICATION_ID,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or 
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) 
                    PendingIntent.FLAG_IMMUTABLE 
                else 0
        )
        
        // Строим уведомление с учетом настроек
        val notificationBuilder = NotificationCompat.Builder(context, NotificationWorker.NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(description)
            .setStyle(NotificationCompat.BigTextStyle().bigText(description))
            .setPriority(NotificationCompat.PRIORITY_MAX) // Максимальный приоритет
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
        
        // Делаем уведомление более заметным
        notificationBuilder.setColor(context.getColor(R.color.purple_500))
        notificationBuilder.setColorized(true)
        
        // Устанавливаем важность уведомления как высокую
        notificationBuilder.setDefaults(NotificationCompat.DEFAULT_ALL)
        
        // Применяем настройки вибрации
        if (settings.notificationVibration) {
            // Более заметный паттерн вибрации
            notificationBuilder.setVibrate(longArrayOf(0, 500, 250, 500, 250, 500))
        }
        
        // Если звук уведомлений отключен, явно устанавливаем тихое уведомление
        if (!settings.notificationSound) {
            notificationBuilder.setSilent(true)
        } else {
            // Устанавливаем звук по умолчанию
            notificationBuilder.setDefaults(NotificationCompat.DEFAULT_SOUND)
        }
        
        try {
            // Показываем уведомление
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(TEST_NOTIFICATION_ID, notificationBuilder.build())
            return Pair(true, null)
        } catch (e: Exception) {
            return Pair(false, "Ошибка при отправке уведомления: ${e.message}")
        }
    }
} 