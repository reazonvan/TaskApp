package com.example.taskapp.di

import android.content.Context
import androidx.room.Room
import androidx.work.WorkManager
import com.example.taskapp.data.dao.TaskDao
import com.example.taskapp.data.dao.TeacherDao
import com.example.taskapp.data.database.AppDatabase
import com.example.taskapp.data.database.MIGRATION_1_2
import com.example.taskapp.data.repository.TaskRepository
import com.example.taskapp.data.repository.TeacherRepository
import com.example.taskapp.data.repository.ThemeRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "taskapp_database"
        )
        .addMigrations(MIGRATION_1_2)
        .fallbackToDestructiveMigration()
        .build()
    }
    
    @Provides
    @Singleton
    fun provideTaskDao(database: AppDatabase): TaskDao {
        return database.taskDao()
    }
    
    @Provides
    @Singleton
    fun provideTeacherDao(database: AppDatabase): TeacherDao {
        return database.teacherDao()
    }
    
    @Provides
    @Singleton
    fun provideTaskRepository(taskDao: TaskDao): TaskRepository {
        return TaskRepository(taskDao)
    }
    
    @Provides
    @Singleton
    fun provideTeacherRepository(teacherDao: TeacherDao): TeacherRepository {
        return TeacherRepository(teacherDao)
    }
    
    @Provides
    @Singleton
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager {
        return WorkManager.getInstance(context)
    }
    
    @Provides
    @Singleton
    fun provideContext(@ApplicationContext context: Context): Context {
        return context
    }
    
    @Provides
    @Singleton
    fun provideThemeRepository(
        @ApplicationContext context: Context
    ): ThemeRepository {
        return ThemeRepository(context)
    }
} 