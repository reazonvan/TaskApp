package com.example.taskapp.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import com.example.taskapp.data.repository.AppSettings
import com.example.taskapp.ui.theme.LocalAppSettings
import com.example.taskapp.util.DateFormatUtil
import java.util.*

/**
 * Компонент для отображения даты с учетом настроек
 */
@Composable
fun DateDisplay(
    timestamp: Long,
    modifier: Modifier = Modifier,
    showTime: Boolean = false,
    isShort: Boolean = false
) {
    // Получаем настройки из композиционного локала
    val appSettings = LocalAppSettings.current
    
    // Форматируем дату в соответствии с настройками
    val formattedDate = when {
        showTime -> DateFormatUtil.formatDateTime(timestamp, appSettings)
        isShort -> DateFormatUtil.formatShortDate(timestamp, appSettings)
        else -> DateFormatUtil.formatDate(timestamp, appSettings)
    }
    
    Text(
        text = formattedDate,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier
    )
}

/**
 * Компонент для отображения даты, который принимает список дат и выбирает ближайшую
 * к текущему времени для отображения
 */
@Composable
fun NextDeadlineDisplay(
    timestamps: List<Long>,
    modifier: Modifier = Modifier,
    emptyText: String = "Нет дедлайнов",
    showTime: Boolean = true,
    isShort: Boolean = false
) {
    // Получаем настройки из композиционного локала
    val appSettings = LocalAppSettings.current
    
    // Если список пуст, показываем заглушку
    if (timestamps.isEmpty()) {
        Text(
            text = emptyText,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = modifier
        )
        return
    }
    
    // Находим ближайший дедлайн в будущем
    val now = System.currentTimeMillis()
    val nextDeadline = timestamps.filter { it > now }.minOrNull() ?: timestamps.maxOrNull()!!
    
    // Форматируем дату
    val formattedDate = when {
        showTime -> DateFormatUtil.formatDateTime(nextDeadline, appSettings)
        isShort -> DateFormatUtil.formatShortDate(nextDeadline, appSettings)
        else -> DateFormatUtil.formatDate(nextDeadline, appSettings)
    }
    
    Text(
        text = formattedDate,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier
    )
} 