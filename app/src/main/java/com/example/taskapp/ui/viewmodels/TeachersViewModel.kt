package com.example.taskapp.ui.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taskapp.data.dao.TaskDao
import com.example.taskapp.data.dao.TeacherDao
import com.example.taskapp.data.model.Task
import com.example.taskapp.data.model.Teacher
import com.example.taskapp.util.ExportUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll

@HiltViewModel
class TeachersViewModel @Inject constructor(
    private val teacherDao: TeacherDao,
    private val taskDao: TaskDao,
    @ApplicationContext private val context: Context
) : ViewModel() {

    // Триггер обновления данных
    private val _refreshTrigger = MutableStateFlow(0)
    
    val teachers = teacherDao.getAllTeachers()
        .combine(_refreshTrigger) { teachers, _ -> teachers }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    // Получаем количество несданных задач для каждого преподавателя
    val uncompletedTasksCount = teachers
        .map { teacherList ->
            teacherList.associate { teacher ->
                teacher.id to taskDao.getUncompletedTasksCountSync(teacher.id)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyMap()
        )

    // Функция для принудительного обновления данных
    fun refreshData() {
        viewModelScope.launch {
            // Принудительно считываем количество несданных задач для каждого преподавателя
            val teacherList = teachers.value
            val updatedMap = mutableMapOf<Long, Int>()

            // Параллельное чтение количества задач
            teacherList.forEach { teacher ->
                val count = taskDao.getUncompletedTasksCountSync(teacher.id)
                updatedMap[teacher.id] = count
            }
            
            // Обновляем внутренний триггер, чтобы вызвать перекомпозицию
            _refreshTrigger.value += 1
        }
    }

    // Перегрузка для упрощенного добавления преподавателя
    fun addTeacher(name: String) {
        addTeacher(name, "Предмет", true)
    }

    fun addTeacher(name: String, subject: String, displayNameFirst: Boolean) {
        viewModelScope.launch {
            teacherDao.insertTeacher(
                Teacher(
                    name = name,
                    subject = subject,
                    displayNameFirst = displayNameFirst
                )
            )
        }
    }

    fun deleteTeacher(teacher: Teacher) {
        viewModelScope.launch {
            teacherDao.deleteTeacher(teacher)
        }
    }
    
    fun swapTeacherNameSubject(teacher: Teacher) {
        viewModelScope.launch {
            val updatedTeacher = teacher.copy(
                displayNameFirst = !teacher.displayNameFirst
            )
            teacherDao.updateTeacher(updatedTeacher)
        }
    }
    
    fun exportData() {
        viewModelScope.launch {
            val teachersList = teachers.value
            val tasksMap = mutableMapOf<Long, List<Task>>()
            
            teachersList.forEach { teacher ->
                val tasks = taskDao.getTasksForTeacher(teacher.id).first()
                tasksMap[teacher.id] = tasks
            }
            
            val uri = ExportUtil.exportToTextFile(context, teachersList, tasksMap)
            uri?.let { ExportUtil.shareFile(context, it) }
        }
    }
} 