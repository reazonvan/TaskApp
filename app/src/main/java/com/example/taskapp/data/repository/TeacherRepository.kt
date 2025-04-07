package com.example.taskapp.data.repository

import com.example.taskapp.data.dao.TeacherDao
import com.example.taskapp.data.model.Teacher
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class TeacherRepository @Inject constructor(
    private val teacherDao: TeacherDao
) {
    fun getAllTeachers(): Flow<List<Teacher>> = teacherDao.getAllTeachers()
    
    suspend fun getTeacherById(id: Long): Teacher? = teacherDao.getTeacherById(id)
    
    suspend fun addTeacher(name: String): Long {
        val teacher = Teacher(name = name)
        return teacherDao.insertTeacher(teacher)
    }
    
    suspend fun updateTeacher(teacher: Teacher) = teacherDao.updateTeacher(teacher)
    
    suspend fun deleteTeacher(teacherId: Long) {
        val teacher = getTeacherById(teacherId)
        teacher?.let { teacherToDelete ->
            teacherDao.deleteTeacher(teacherToDelete)
        }
    }

    fun forceRefresh() {
        // Implementation of forceRefresh method
    }
} 