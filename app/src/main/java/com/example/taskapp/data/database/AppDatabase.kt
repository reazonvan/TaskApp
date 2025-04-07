package com.example.taskapp.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.taskapp.data.dao.TaskDao
import com.example.taskapp.data.dao.TeacherDao
import com.example.taskapp.data.model.Task
import com.example.taskapp.data.model.Teacher

@Database(
    entities = [Teacher::class, Task::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun teacherDao(): TeacherDao
    abstract fun taskDao(): TaskDao
} 