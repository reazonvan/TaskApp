package com.example.taskapp.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.Index

@Entity(
    tableName = "tasks",
    foreignKeys = [
        ForeignKey(
            entity = Teacher::class,
            parentColumns = ["id"],
            childColumns = ["teacherId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("teacherId")]
)
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val teacherId: Long,
    val title: String,
    val description: String = "",
    val deadline: Long? = null,
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    
    // Время в минутах до дедлайна, когда нужно показать уведомление
    // Например: 60 (за час до дедлайна), 1440 (за день) и т.д.
    val notifyBeforeMinutes: Int = 60,
    
    // Флаг, указывающий, использовать ли пользовательское время уведомления
    // Если false, будет использовано время, установленное в глобальных настройках
    val useCustomNotificationTime: Boolean = false,
    
    // Флаг, указывающий, что уведомление уже было отправлено
    val notificationSent: Boolean = false
) 