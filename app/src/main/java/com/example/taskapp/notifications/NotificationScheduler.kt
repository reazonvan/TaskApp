package com.example.taskapp.notifications

import androidx.work.*
import com.example.taskapp.data.model.Task
import com.example.taskapp.data.repository.TaskRepository
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationScheduler @Inject constructor(
    private val workManager: WorkManager,
    private val taskRepository: TaskRepository
) {
    
    companion object {
        private const val WORK_NAME_PREFIX = "deadline_notification_"
    }
    
    /**
     * Планирует уведомление для задачи
     */
    suspend fun scheduleNotification(task: Task) {
        if (task.isCompleted || task.deadline == null || task.notificationSent) {
            return
        }
        
        // Вычисляем, когда показать уведомление (за notifyBeforeMinutes минут до дедлайна)
        val notificationTime = task.deadline - TimeUnit.MINUTES.toMillis(task.notifyBeforeMinutes.toLong())
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
} 