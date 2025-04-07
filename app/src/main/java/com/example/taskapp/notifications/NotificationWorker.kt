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
import com.example.taskapp.data.repository.TaskRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

@HiltWorker
class NotificationWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val taskRepository: TaskRepository
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
            
            // Проверяем, что задача еще не выполнена и дедлайн не истек
            if (task.isCompleted || task.deadline == null || System.currentTimeMillis() > task.deadline) {
                return@withContext Result.success()
            }
            
            // Отправляем уведомление
            sendNotification(task.id, task.title, task.description, task.deadline)
            
            // Отмечаем, что уведомление было отправлено
            taskRepository.markNotificationSent(task.id)
            
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
    
    private fun sendNotification(taskId: Long, title: String, description: String, deadline: Long) {
        // Рассчитываем оставшееся время до дедлайна
        val timeUntilDeadline = deadline - System.currentTimeMillis()
        val hoursUntil = TimeUnit.MILLISECONDS.toHours(timeUntilDeadline)
        
        // Создаем текст уведомления в зависимости от оставшегося времени
        val notificationText = when {
            hoursUntil <= 1 -> "Срок сдачи менее чем через час!"
            hoursUntil <= 3 -> "Срок сдачи через $hoursUntil ${hoursUntil.formatHours()}"
            hoursUntil <= 24 -> "Срок сдачи сегодня, через $hoursUntil ${hoursUntil.formatHours()}"
            hoursUntil <= 48 -> "Срок сдачи завтра"
            else -> "До срока сдачи осталось ${hoursUntil/24} ${(hoursUntil/24).formatDays()}"
        }
        
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
        
        // Строим уведомление
        val notificationBuilder = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification) // убедитесь, что иконка существует
            .setContentTitle(title)
            .setContentText("$notificationText: $description")
            .setStyle(NotificationCompat.BigTextStyle().bigText("$notificationText\n$description"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(0, 250, 250, 250))
        
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