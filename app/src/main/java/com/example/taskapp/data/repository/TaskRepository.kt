package com.example.taskapp.data.repository

import com.example.taskapp.data.dao.TaskDao
import com.example.taskapp.data.model.Task
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class TaskRepository @Inject constructor(
    private val taskDao: TaskDao
) {
    fun getAllTasks(teacherId: Long): Flow<List<Task>> = taskDao.getTasksForTeacher(teacherId)
    
    fun getUncompletedTasksCount(teacherId: Long): Flow<Int> = 
        taskDao.getUncompletedTasksCount(teacherId)
    
    suspend fun addTask(title: String, description: String, deadline: Long?, teacherId: Long, notifyBeforeMinutes: Int = 60): Long {
        val task = Task(
            title = title,
            description = description,
            deadline = deadline,
            teacherId = teacherId,
            notifyBeforeMinutes = notifyBeforeMinutes
        )
        return taskDao.insertTask(task)
    }
    
    suspend fun updateTaskCompletion(taskId: Long, isCompleted: Boolean) {
        taskDao.getTaskById(taskId)?.let { task ->
            taskDao.updateTask(task.copy(isCompleted = isCompleted))
        }
    }
    
    suspend fun deleteTask(taskId: Long) {
        taskDao.getTaskById(taskId)?.let { task ->
            taskDao.deleteTask(task)
        }
    }
    
    // Методы для работы с уведомлениями
    suspend fun getTasksWithPendingNotifications(): List<Task> = 
        taskDao.getTasksWithPendingNotifications()
        
    suspend fun markNotificationSent(taskId: Long) = 
        taskDao.markNotificationSent(taskId)
        
    suspend fun updateNotificationTime(taskId: Long, minutes: Int) = 
        taskDao.updateNotificationTime(taskId, minutes)
        
    suspend fun getAllActiveTasksWithDeadline(): List<Task> =
        taskDao.getAllActiveTasksWithDeadline()
        
    suspend fun getTaskById(taskId: Long): Task? =
        taskDao.getTaskById(taskId)
} 