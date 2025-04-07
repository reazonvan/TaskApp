package com.example.taskapp.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.taskapp.data.dao.TaskDao
import com.example.taskapp.data.dao.TeacherDao
import com.example.taskapp.data.model.Task
import com.example.taskapp.data.model.Teacher

// Миграция с версии 1 на версию 2
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Добавляем новое поле useCustomNotificationTime в таблицу tasks
        database.execSQL("ALTER TABLE tasks ADD COLUMN useCustomNotificationTime INTEGER NOT NULL DEFAULT 0")
    }
}

@Database(
    entities = [Teacher::class, Task::class],
    version = 2, // Увеличиваем версию с 1 до 2
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun teacherDao(): TeacherDao
    abstract fun taskDao(): TaskDao
} 