package com.example.taskapp.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.taskapp.data.repository.SettingsRepository
import com.example.taskapp.notifications.NotificationScheduler
import com.example.taskapp.ui.viewmodels.SettingsViewModel
import javax.inject.Inject

/**
 * Фабрика для создания ViewModel в средах, где Hilt не может автоматически
 * обработать построение зависимостей (например, в @Composable функциях)
 */
class HiltViewModelFactory @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val notificationScheduler: NotificationScheduler
) : ViewModelProvider.Factory {
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(SettingsViewModel::class.java) -> {
                SettingsViewModel(settingsRepository, notificationScheduler) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
} 