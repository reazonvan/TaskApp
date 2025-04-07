package com.example.taskapp.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taskapp.data.repository.ThemeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ThemeViewModel @Inject constructor(
    private val themeRepository: ThemeRepository
) : ViewModel() {
    
    // Наблюдаем за состоянием темы
    val isDarkTheme: StateFlow<Boolean> = themeRepository.isDarkTheme
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )
    
    // Переключаем тему
    fun toggleTheme() {
        viewModelScope.launch {
            themeRepository.toggleDarkTheme()
        }
    }
    
    // Устанавливаем определенную тему
    fun setDarkTheme(isDark: Boolean) {
        viewModelScope.launch {
            themeRepository.setDarkTheme(isDark)
        }
    }
} 