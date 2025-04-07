package com.example.taskapp.data.dao

import androidx.room.*
import com.example.taskapp.data.model.Teacher
import kotlinx.coroutines.flow.Flow

@Dao
interface TeacherDao {
    @Query("SELECT * FROM teachers ORDER BY name ASC")
    fun getAllTeachers(): Flow<List<Teacher>>

    @Query("SELECT * FROM teachers WHERE id = :id")
    suspend fun getTeacherById(id: Long): Teacher?

    @Insert
    suspend fun insertTeacher(teacher: Teacher): Long

    @Update
    suspend fun updateTeacher(teacher: Teacher)

    @Delete
    suspend fun deleteTeacher(teacher: Teacher)
} 