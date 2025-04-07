package com.example.taskapp.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

// Создаем DataStore для настроек приложения
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "theme_preferences")

@Singleton
class ThemeRepository @Inject constructor(
    private val context: Context
) {
    // Ключ для сохранения темы
    private val IS_DARK_THEME = booleanPreferencesKey("is_dark_theme")
    
    // Получаем текущую тему из DataStore
    val isDarkTheme: Flow<Boolean> = context.dataStore.data
        .map { preferences -> 
            // По умолчанию используем системную тему
            preferences[IS_DARK_THEME] ?: false
        }
    
    // Меняем тему
    suspend fun toggleDarkTheme() {
        context.dataStore.edit { preferences ->
            val current = preferences[IS_DARK_THEME] ?: false
            preferences[IS_DARK_THEME] = !current
        }
    }
    
    // Устанавливаем конкретное значение
    suspend fun setDarkTheme(isDark: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[IS_DARK_THEME] = isDark
        }
    }
} 