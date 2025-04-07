package com.example.taskapp.util

import com.example.taskapp.data.repository.AppSettings
import java.text.SimpleDateFormat
import java.util.*

/**
 * Утилитарный класс для форматирования дат с учетом настроек приложения
 */
object DateFormatUtil {
    
    /**
     * Получает форматтер даты на основе настроек
     * @param dateFormatPreference индекс формата даты из настроек
     * @return SimpleDateFormat с выбранным форматом
     */
    fun getDateFormatter(dateFormatPreference: Int): SimpleDateFormat {
        val pattern = when (dateFormatPreference) {
            0 -> "dd.MM.yyyy" // ДД.ММ.ГГГГ
            1 -> "MM/dd/yyyy" // ММ/ДД/ГГГГ
            2 -> "yyyy-MM-dd" // ГГГГ-ММ-ДД
            else -> "dd.MM.yyyy" // По умолчанию ДД.ММ.ГГГГ
        }
        return SimpleDateFormat(pattern, Locale.getDefault())
    }
    
    /**
     * Форматирует дату с учетом настроек приложения
     * @param timestamp метка времени для форматирования
     * @param settings настройки приложения
     * @return отформатированная строка с датой
     */
    fun formatDate(timestamp: Long, settings: AppSettings): String {
        val formatter = getDateFormatter(settings.dateFormat)
        return formatter.format(Date(timestamp))
    }
    
    /**
     * Форматирует дату в краткий вид (день-месяц)
     * @param timestamp метка времени для форматирования
     * @param settings настройки приложения
     * @return отформатированная строка с кратким представлением даты
     */
    fun formatShortDate(timestamp: Long, settings: AppSettings): String {
        val pattern = when (settings.dateFormat) {
            0 -> "dd.MM" // ДД.ММ
            1 -> "MM/dd" // ММ/ДД
            2 -> "MM-dd" // ММ-ДД
            else -> "dd.MM" // По умолчанию ДД.ММ
        }
        return SimpleDateFormat(pattern, Locale.getDefault()).format(Date(timestamp))
    }
    
    /**
     * Форматирует дату и время
     * @param timestamp метка времени для форматирования
     * @param settings настройки приложения
     * @return отформатированная строка с датой и временем
     */
    fun formatDateTime(timestamp: Long, settings: AppSettings): String {
        val datePattern = when (settings.dateFormat) {
            0 -> "dd.MM.yyyy" // ДД.ММ.ГГГГ
            1 -> "MM/dd/yyyy" // ММ/ДД/ГГГГ
            2 -> "yyyy-MM-dd" // ГГГГ-ММ-ДД
            else -> "dd.MM.yyyy" // По умолчанию ДД.ММ.ГГГГ
        }
        val timePattern = " HH:mm"
        return SimpleDateFormat(datePattern + timePattern, Locale.getDefault()).format(Date(timestamp))
    }
} 