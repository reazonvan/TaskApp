package com.example.taskapp.ui.hilt

import androidx.lifecycle.ViewModelProvider
import com.example.taskapp.ui.viewmodels.SettingsViewModel
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent

@EntryPoint
@InstallIn(ActivityComponent::class)
interface SettingsViewModelFactoryEntryPoint {
    fun settingsViewModelFactory(): ViewModelProvider.Factory
} 