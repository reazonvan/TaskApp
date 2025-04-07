package com.example.taskapp.data.dao

import androidx.room.*
import com.example.taskapp.data.model.Task
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks WHERE teacherId = :teacherId ORDER BY deadline ASC, createdAt DESC")
    fun getTasksForTeacher(teacherId: Long): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getTaskById(id: Long): Task?

    @Insert
    suspend fun insertTask(task: Task): Long

    @Update
    suspend fun updateTask(task: Task)

    @Delete
    suspend fun deleteTask(task: Task)

    @Query("SELECT COUNT(*) FROM tasks WHERE teacherId = :teacherId AND isCompleted = 0")
    fun getUncompletedTasksCount(teacherId: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM tasks WHERE teacherId = :teacherId AND isCompleted = 0")
    suspend fun getUncompletedTasksCountSync(teacherId: Long): Int

    @Query("SELECT * FROM tasks WHERE deadline IS NOT NULL AND isCompleted = 0 AND notificationSent = 0")
    suspend fun getTasksWithPendingNotifications(): List<Task>

    @Query("UPDATE tasks SET notificationSent = 1 WHERE id = :taskId")
    suspend fun markNotificationSent(taskId: Long)

    @Query("UPDATE tasks SET notifyBeforeMinutes = :minutes WHERE id = :taskId")
    suspend fun updateNotificationTime(taskId: Long, minutes: Int)

    @Query("SELECT * FROM tasks WHERE isCompleted = 0 AND deadline IS NOT NULL ORDER BY deadline ASC")
    suspend fun getAllActiveTasksWithDeadline(): List<Task>
} 