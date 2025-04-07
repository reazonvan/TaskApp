package com.example.taskapp.data.repository

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

// Расширение для Context для создания DataStore
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

// Модель настроек
data class AppSettings(
    val colorScheme: Int = 0,
    val animationIntensity: Float = 1.0f,
    val textSize: Int = 1,
    val dateFormat: Int = 0,
    val teachersSortType: Int = 0,
    val defaultShowNameFirst: Boolean = true,
    val notificationTime: Int = 60,
    val notificationSound: Boolean = true,
    val notificationVibration: Boolean = true,
    val doNotDisturbEnabled: Boolean = false,
    val doNotDisturbStart: Int = 22,
    val doNotDisturbEnd: Int = 7,
    val updateInterval: Int = 5000,
    val simplifiedMode: Boolean = false
)

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    // Ключи для хранения настроек
    private object PreferencesKeys {
        val COLOR_SCHEME = intPreferencesKey("color_scheme")
        val ANIMATION_INTENSITY = floatPreferencesKey("animation_intensity")
        val TEXT_SIZE = intPreferencesKey("text_size")
        val DATE_FORMAT = intPreferencesKey("date_format")
        val TEACHERS_SORT_TYPE = intPreferencesKey("teachers_sort_type")
        val DEFAULT_SHOW_NAME_FIRST = booleanPreferencesKey("default_show_name_first")
        val NOTIFICATION_TIME = intPreferencesKey("notification_time")
        val NOTIFICATION_SOUND = booleanPreferencesKey("notification_sound")
        val NOTIFICATION_VIBRATION = booleanPreferencesKey("notification_vibration")
        val DO_NOT_DISTURB_ENABLED = booleanPreferencesKey("do_not_disturb_enabled")
        val DO_NOT_DISTURB_START = intPreferencesKey("do_not_disturb_start")
        val DO_NOT_DISTURB_END = intPreferencesKey("do_not_disturb_end")
        val UPDATE_INTERVAL = intPreferencesKey("update_interval")
        val SIMPLIFIED_MODE = booleanPreferencesKey("simplified_mode")
    }
    
    // Получение всех настроек
    fun getSettings(): Flow<AppSettings> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            AppSettings(
                colorScheme = preferences[PreferencesKeys.COLOR_SCHEME] ?: 0,
                animationIntensity = preferences[PreferencesKeys.ANIMATION_INTENSITY] ?: 1.0f,
                textSize = preferences[PreferencesKeys.TEXT_SIZE] ?: 1,
                dateFormat = preferences[PreferencesKeys.DATE_FORMAT] ?: 0,
                teachersSortType = preferences[PreferencesKeys.TEACHERS_SORT_TYPE] ?: 0,
                defaultShowNameFirst = preferences[PreferencesKeys.DEFAULT_SHOW_NAME_FIRST] ?: true,
                notificationTime = preferences[PreferencesKeys.NOTIFICATION_TIME] ?: 60,
                notificationSound = preferences[PreferencesKeys.NOTIFICATION_SOUND] ?: true,
                notificationVibration = preferences[PreferencesKeys.NOTIFICATION_VIBRATION] ?: true,
                doNotDisturbEnabled = preferences[PreferencesKeys.DO_NOT_DISTURB_ENABLED] ?: false,
                doNotDisturbStart = preferences[PreferencesKeys.DO_NOT_DISTURB_START] ?: 22,
                doNotDisturbEnd = preferences[PreferencesKeys.DO_NOT_DISTURB_END] ?: 7,
                updateInterval = preferences[PreferencesKeys.UPDATE_INTERVAL] ?: 5000,
                simplifiedMode = preferences[PreferencesKeys.SIMPLIFIED_MODE] ?: false
            )
        }
    
    // Методы для обновления настроек
    suspend fun updateColorScheme(value: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.COLOR_SCHEME] = value
        }
    }
    
    suspend fun updateAnimationIntensity(value: Float) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ANIMATION_INTENSITY] = value
        }
    }
    
    suspend fun updateTextSize(value: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.TEXT_SIZE] = value
        }
    }
    
    suspend fun updateDateFormat(value: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DATE_FORMAT] = value
        }
    }
    
    suspend fun updateTeachersSortType(value: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.TEACHERS_SORT_TYPE] = value
        }
    }
    
    suspend fun updateDefaultShowNameFirst(value: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DEFAULT_SHOW_NAME_FIRST] = value
        }
    }
    
    suspend fun updateNotificationTime(value: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.NOTIFICATION_TIME] = value
        }
    }
    
    suspend fun updateNotificationSound(value: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.NOTIFICATION_SOUND] = value
        }
    }
    
    suspend fun updateNotificationVibration(value: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.NOTIFICATION_VIBRATION] = value
        }
    }
    
    suspend fun updateDoNotDisturbEnabled(value: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DO_NOT_DISTURB_ENABLED] = value
        }
    }
    
    suspend fun updateDoNotDisturbTime(startHour: Int, endHour: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DO_NOT_DISTURB_START] = startHour
            preferences[PreferencesKeys.DO_NOT_DISTURB_END] = endHour
        }
    }
    
    suspend fun updateDoNotDisturbStart(value: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DO_NOT_DISTURB_START] = value
        }
    }
    
    suspend fun updateDoNotDisturbEnd(value: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DO_NOT_DISTURB_END] = value
        }
    }
    
    suspend fun updateUpdateInterval(value: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.UPDATE_INTERVAL] = value
        }
    }
    
    suspend fun updateSimplifiedMode(value: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SIMPLIFIED_MODE] = value
        }
    }
    
    /**
     * Проверяет, активен ли канал уведомлений
     * Возвращает true, если канал существует и не отключен пользователем
     */
    fun isNotificationChannelEnabled(): Boolean {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = notificationManager.getNotificationChannel("deadline_notifications")
            
            // Канал может не существовать если приложение только что установлено
            if (channel == null) return false
            
            // Проверяем, не отключены ли уведомления для канала
            return channel.importance != NotificationManager.IMPORTANCE_NONE
        }
        
        // Для Android ниже O возвращаем true, так как индивидуальные каналы недоступны
        return true
    }
} 