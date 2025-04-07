package com.example.taskapp.ui.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taskapp.data.dao.TaskDao
import com.example.taskapp.data.dao.TeacherDao
import com.example.taskapp.data.model.Task
import com.example.taskapp.data.model.Teacher
import com.example.taskapp.data.repository.TaskRepository
import com.example.taskapp.data.repository.TeacherRepository
import com.example.taskapp.notifications.NotificationScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class TaskViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val teacherRepository: TeacherRepository,
    private val notificationScheduler: NotificationScheduler,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _selectedTeacherId = MutableStateFlow<Long?>(null)
    val selectedTeacherId: StateFlow<Long?> = _selectedTeacherId.asStateFlow()

    private val teacherId: Long = savedStateHandle.get<String>("teacherId")?.toLongOrNull() ?: 0L
    
    // Инициализируем выбранного преподавателя при создании ViewModel
    init {
        selectTeacher(teacherId)
    }

    // Flow для получения информации о текущем преподавателе
    val teacher: StateFlow<Teacher?> = _selectedTeacherId
        .flatMapLatest { teacherId ->
            if (teacherId != null) {
                flow<Teacher?> {
                    emit(teacherRepository.getTeacherById(teacherId))
                }
            } else {
                flowOf<Teacher?>(null)
            }
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    val tasks = _selectedTeacherId.flatMapLatest { teacherId ->
        teacherId?.let { id ->
            taskRepository.getAllTasks(id)
        } ?: flowOf(emptyList())
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val teachers = teacherRepository.getAllTeachers()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val uncompletedCount = _selectedTeacherId.flatMapLatest { teacherId ->
        teacherId?.let { id ->
            taskRepository.getUncompletedTasksCount(id)
        } ?: flowOf(0)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    fun addTask(title: String, description: String, deadline: Long?, teacherId: Long, notifyBeforeMinutes: Int = 60) {
        viewModelScope.launch {
            val taskId = taskRepository.addTask(title, description, deadline, teacherId, notifyBeforeMinutes)
            
            // Планируем уведомление, если у задачи есть дедлайн
            if (deadline != null) {
                val task = taskRepository.getTaskById(taskId)
                if (task != null) {
                    notificationScheduler.scheduleNotification(task)
                }
            }
        }
    }

    fun updateTaskCompletion(taskId: Long, isCompleted: Boolean) {
        viewModelScope.launch {
            taskRepository.updateTaskCompletion(taskId, isCompleted)
            
            // Если задача выполнена, отменяем запланированное уведомление
            if (isCompleted) {
                notificationScheduler.cancelNotification(taskId)
            } else {
                // Если задача возвращена в невыполненные, планируем уведомление заново
                val task = taskRepository.getTaskById(taskId)
                if (task != null && task.deadline != null && !task.notificationSent) {
                    notificationScheduler.scheduleNotification(task)
                }
            }
        }
    }

    fun deleteTask(taskId: Long) {
        viewModelScope.launch {
            // Отменяем запланированное уведомление
            notificationScheduler.cancelNotification(taskId)
            
            // Удаляем задачу
            taskRepository.deleteTask(taskId)
        }
    }

    fun updateNotificationTime(taskId: Long, notifyBeforeMinutes: Int) {
        viewModelScope.launch {
            notificationScheduler.updateNotificationTime(taskId, notifyBeforeMinutes)
        }
    }

    fun selectTeacher(teacherId: Long?) {
        _selectedTeacherId.value = teacherId
    }

    fun formatDate(timestamp: Long?): String {
        if (timestamp == null) return "Без срока"
        val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    // Метод для инициализации уведомлений при запуске приложения
    fun initializeNotifications() {
        viewModelScope.launch {
            notificationScheduler.scheduleAllPendingNotifications()
        }
    }
} 