package com.example.taskapp.notifications

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.taskapp.MainActivity
import com.example.taskapp.R
import com.example.taskapp.data.repository.AppSettings
import com.example.taskapp.data.repository.SettingsRepository
import com.example.taskapp.data.repository.TaskRepository
import com.example.taskapp.util.DateFormatUtil
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.concurrent.TimeUnit

@HiltWorker
class NotificationWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val taskRepository: TaskRepository,
    private val settingsRepository: SettingsRepository
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val TASK_ID_KEY = "task_id"
        const val NOTIFICATION_CHANNEL_ID = "deadline_notifications"
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val taskId = inputData.getLong(TASK_ID_KEY, -1)
            if (taskId == -1L) {
                return@withContext Result.failure()
            }

            val task = taskRepository.getTaskById(taskId) ?: return@withContext Result.failure()
            
            // Получаем настройки
            val settings = settingsRepository.getSettings().first()
            
            // Проверяем режим "Не беспокоить"
            if (isInDoNotDisturbTime(settings)) {
                return@withContext Result.retry()
            }
            
            // Проверяем, что задача еще не выполнена и дедлайн не истек
            if (task.isCompleted || task.deadline == null || System.currentTimeMillis() > task.deadline) {
                return@withContext Result.success()
            }
            
            // Отправляем уведомление с учетом настроек
            sendNotification(task.id, task.title, task.description, task.deadline, settings)
            
            // Отмечаем, что уведомление было отправлено
            taskRepository.markNotificationSent(task.id)
            
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
    
    // Проверяем, находимся ли мы в режиме "Не беспокоить"
    private fun isInDoNotDisturbTime(settings: AppSettings): Boolean {
        if (!settings.doNotDisturbEnabled) return false
        
        val calendar = Calendar.getInstance()
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        
        // Проверяем, попадает ли текущее время в диапазон "Не беспокоить"
        return when {
            // Если время начала меньше времени окончания (например, 22:00 - 7:00)
            settings.doNotDisturbStart < settings.doNotDisturbEnd -> {
                currentHour in settings.doNotDisturbStart until settings.doNotDisturbEnd
            }
            // Если время начала больше времени окончания (например, 22:00 - 7:00)
            settings.doNotDisturbStart > settings.doNotDisturbEnd -> {
                currentHour >= settings.doNotDisturbStart || currentHour < settings.doNotDisturbEnd
            }
            // Если время начала равно времени окончания, считаем, что режим отключен
            else -> false
        }
    }
    
    private fun sendNotification(taskId: Long, title: String, description: String, deadline: Long, settings: AppSettings) {
        // Рассчитываем оставшееся время до дедлайна
        val timeUntilDeadline = deadline - System.currentTimeMillis()
        val hoursUntil = TimeUnit.MILLISECONDS.toHours(timeUntilDeadline)
        
        // Форматируем дату в соответствии с настройками
        val formattedDate = DateFormatUtil.formatDate(deadline, settings)
        
        // Создаем текст уведомления в зависимости от оставшегося времени
        val notificationText = when {
            hoursUntil <= 1 -> "Срок сдачи менее чем через час!"
            hoursUntil <= 3 -> "Срок сдачи через $hoursUntil ${hoursUntil.formatHours()}"
            hoursUntil <= 24 -> "Срок сдачи сегодня, через $hoursUntil ${hoursUntil.formatHours()}"
            hoursUntil <= 48 -> "Срок сдачи завтра"
            else -> "До срока сдачи осталось ${hoursUntil/24} ${(hoursUntil/24).formatDays()}"
        }
        
        // Добавляем дату в зависимости от формата
        val fullNotificationText = "$notificationText (Срок: $formattedDate)"
        
        // Intent для открытия приложения при клике на уведомление
        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra("task_id", taskId)
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            taskId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or 
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) 
                        PendingIntent.FLAG_IMMUTABLE 
                    else 0
        )
        
        // Строим уведомление с учетом настроек
        val notificationBuilder = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(fullNotificationText)
            .setStyle(NotificationCompat.BigTextStyle().bigText("$fullNotificationText\n$description"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
        
        // Применяем настройки вибрации
        if (settings.notificationVibration) {
            notificationBuilder.setVibrate(longArrayOf(0, 250, 250, 250))
        }
        
        // Если звук уведомлений отключен, явно устанавливаем тихое уведомление
        if (!settings.notificationSound) {
            notificationBuilder.setSilent(true)
        }
        
        // Показываем уведомление
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(taskId.toInt(), notificationBuilder.build())
    }
    
    // Вспомогательная функция для форматирования текста
    private fun Long.formatHours(): String {
        return when {
            this % 10 == 1L && this % 100 != 11L -> "час"
            this % 10 in 2..4 && (this % 100 < 10 || this % 100 >= 20) -> "часа"
            else -> "часов"
        }
    }
    
    private fun Long.formatDays(): String {
        return when {
            this % 10 == 1L && this % 100 != 11L -> "день"
            this % 10 in 2..4 && (this % 100 < 10 || this % 100 >= 20) -> "дня"
            else -> "дней"
        }
    }
} 