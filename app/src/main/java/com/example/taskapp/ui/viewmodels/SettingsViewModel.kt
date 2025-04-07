package com.example.taskapp.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taskapp.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {
    
    // Состояния настроек интерфейса
    private val _colorScheme = MutableStateFlow(0)
    val colorScheme: StateFlow<Int> = _colorScheme.asStateFlow()
    
    private val _animationIntensity = MutableStateFlow(1.0f)
    val animationIntensity: StateFlow<Float> = _animationIntensity.asStateFlow()
    
    private val _textSize = MutableStateFlow(1)
    val textSize: StateFlow<Int> = _textSize.asStateFlow()
    
    // Состояния настроек отображения данных
    private val _dateFormat = MutableStateFlow(0)
    val dateFormat: StateFlow<Int> = _dateFormat.asStateFlow()
    
    private val _teachersSortType = MutableStateFlow(0)
    val teachersSortType: StateFlow<Int> = _teachersSortType.asStateFlow()
    
    private val _defaultShowNameFirst = MutableStateFlow(true)
    val defaultShowNameFirst: StateFlow<Boolean> = _defaultShowNameFirst.asStateFlow()
    
    // Состояния настроек уведомлений
    private val _notificationTime = MutableStateFlow(60)
    val notificationTime: StateFlow<Int> = _notificationTime.asStateFlow()
    
    private val _notificationSound = MutableStateFlow(true)
    val notificationSound: StateFlow<Boolean> = _notificationSound.asStateFlow()
    
    private val _notificationVibration = MutableStateFlow(true)
    val notificationVibration: StateFlow<Boolean> = _notificationVibration.asStateFlow()
    
    private val _doNotDisturbEnabled = MutableStateFlow(false)
    val doNotDisturbEnabled: StateFlow<Boolean> = _doNotDisturbEnabled.asStateFlow()
    
    private val _doNotDisturbStart = MutableStateFlow(22)
    val doNotDisturbStart: StateFlow<Int> = _doNotDisturbStart.asStateFlow()
    
    private val _doNotDisturbEnd = MutableStateFlow(7)
    val doNotDisturbEnd: StateFlow<Int> = _doNotDisturbEnd.asStateFlow()
    
    // Состояния настроек производительности
    private val _updateInterval = MutableStateFlow(5000)
    val updateInterval: StateFlow<Int> = _updateInterval.asStateFlow()
    
    private val _simplifiedMode = MutableStateFlow(false)
    val simplifiedMode: StateFlow<Boolean> = _simplifiedMode.asStateFlow()
    
    init {
        loadSettings()
    }
    
    // Метод загрузки настроек из репозитория
    private fun loadSettings() {
        viewModelScope.launch {
            settingsRepository.getSettings().collect { settings ->
                _colorScheme.value = settings.colorScheme
                _animationIntensity.value = settings.animationIntensity
                _textSize.value = settings.textSize
                _dateFormat.value = settings.dateFormat
                _teachersSortType.value = settings.teachersSortType
                _defaultShowNameFirst.value = settings.defaultShowNameFirst
                _notificationTime.value = settings.notificationTime
                _notificationSound.value = settings.notificationSound
                _notificationVibration.value = settings.notificationVibration
                _doNotDisturbEnabled.value = settings.doNotDisturbEnabled
                _doNotDisturbStart.value = settings.doNotDisturbStart
                _doNotDisturbEnd.value = settings.doNotDisturbEnd
                _updateInterval.value = settings.updateInterval
                _simplifiedMode.value = settings.simplifiedMode
            }
        }
    }
    
    // Методы обновления настроек
    fun updateColorScheme(value: Int) {
        _colorScheme.value = value
        viewModelScope.launch {
            settingsRepository.updateColorScheme(value)
        }
    }
    
    fun updateAnimationIntensity(value: Float) {
        _animationIntensity.value = value
        viewModelScope.launch {
            settingsRepository.updateAnimationIntensity(value)
        }
    }
    
    fun updateTextSize(value: Int) {
        _textSize.value = value
        viewModelScope.launch {
            settingsRepository.updateTextSize(value)
        }
    }
    
    fun updateDateFormat(value: Int) {
        _dateFormat.value = value
        viewModelScope.launch {
            settingsRepository.updateDateFormat(value)
        }
    }
    
    fun updateTeachersSortType(value: Int) {
        _teachersSortType.value = value
        viewModelScope.launch {
            settingsRepository.updateTeachersSortType(value)
        }
    }
    
    fun updateDefaultShowNameFirst(value: Boolean) {
        _defaultShowNameFirst.value = value
        viewModelScope.launch {
            settingsRepository.updateDefaultShowNameFirst(value)
        }
    }
    
    fun updateNotificationTime(value: Int) {
        _notificationTime.value = value
        viewModelScope.launch {
            settingsRepository.updateNotificationTime(value)
        }
    }
    
    fun updateNotificationSound(value: Boolean) {
        _notificationSound.value = value
        viewModelScope.launch {
            settingsRepository.updateNotificationSound(value)
        }
    }
    
    fun updateNotificationVibration(value: Boolean) {
        _notificationVibration.value = value
        viewModelScope.launch {
            settingsRepository.updateNotificationVibration(value)
        }
    }
    
    fun updateDoNotDisturbEnabled(value: Boolean) {
        _doNotDisturbEnabled.value = value
        viewModelScope.launch {
            settingsRepository.updateDoNotDisturbEnabled(value)
        }
    }
    
    fun updateDoNotDisturbTime(start: Int, end: Int) {
        _doNotDisturbStart.value = start
        _doNotDisturbEnd.value = end
        viewModelScope.launch {
            settingsRepository.updateDoNotDisturbTime(start, end)
        }
    }
    
    fun updateUpdateInterval(value: Int) {
        _updateInterval.value = value
        viewModelScope.launch {
            settingsRepository.updateUpdateInterval(value)
        }
    }
    
    fun updateSimplifiedMode(value: Boolean) {
        _simplifiedMode.value = value
        viewModelScope.launch {
            settingsRepository.updateSimplifiedMode(value)
        }
    }
} 