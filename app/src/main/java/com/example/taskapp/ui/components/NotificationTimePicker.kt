package com.example.taskapp.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.layout.ExperimentalLayoutApi

/**
 * Компонент выбора времени уведомления перед дедлайном с возможностью ввода пользовательского значения
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun NotificationTimePicker(
    selectedMinutes: Int,
    onTimeSelected: (Int?) -> Unit
) {
    val options = remember {
        listOf(
            NotificationTimeOption(15, "15 минут"),
            NotificationTimeOption(30, "30 минут"),
            NotificationTimeOption(60, "1 час"),
            NotificationTimeOption(120, "2 часа"),
            NotificationTimeOption(240, "4 часа"),
            NotificationTimeOption(720, "12 часов"),
            NotificationTimeOption(1440, "1 день"),
            NotificationTimeOption(4320, "3 дня"),
            NotificationTimeOption(10080, "1 неделя")
        )
    }
    
    // Добавляем опцию "Свой вариант"
    var showCustomOption by remember { mutableStateOf(false) }
    var customHours by remember { mutableStateOf("0") }
    var customMinutes by remember { mutableStateOf("0") }
    
    // Проверяем, является ли выбранное время одним из предустановленных вариантов
    val isCustomTime = selectedMinutes > 0 && 
                       options.none { it.minutes == selectedMinutes }
    
    // Если выбрано пользовательское время, отображаем его
    LaunchedEffect(key1 = selectedMinutes) {
        if (isCustomTime) {
            showCustomOption = true
            customHours = (selectedMinutes / 60).toString()
            customMinutes = (selectedMinutes % 60).toString()
        }
    }
    
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Заголовок более компактный
        Text(
            text = "Уведомить за:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
        )
        
        // Более компактный FlowRow вместо LazyRow для лучшего размещения чипов
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            maxItemsInEachRow = 3  // Максимум 3 чипа в строке
        ) {
            // Чип для отключения уведомлений
            FilterChip(
                selected = selectedMinutes <= 0,
                onClick = { onTimeSelected(null) },
                label = { 
                    Text(
                        "Не уведомлять",
                        style = MaterialTheme.typography.bodySmall
                    ) 
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.NotificationsOff,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.errorContainer,
                    selectedLeadingIconColor = MaterialTheme.colorScheme.error,
                    selectedLabelColor = MaterialTheme.colorScheme.onErrorContainer
                ),
                modifier = Modifier.height(32.dp)
            )
            
            // Предустановленные варианты
            options.forEach { option ->
                FilterChip(
                    selected = selectedMinutes == option.minutes,
                    onClick = { onTimeSelected(option.minutes) },
                    label = { 
                        Text(
                            option.label,
                            style = MaterialTheme.typography.bodySmall
                        ) 
                    },
                    leadingIcon = if (selectedMinutes == option.minutes) {
                        {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    } else null,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLeadingIconColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    modifier = Modifier.height(32.dp)
                )
            }
            
            // Кнопка "Свой вариант"
            FilterChip(
                selected = showCustomOption,
                onClick = { showCustomOption = !showCustomOption },
                label = { 
                    Text(
                        "Другое",
                        style = MaterialTheme.typography.bodySmall
                    ) 
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                    selectedLeadingIconColor = MaterialTheme.colorScheme.secondary,
                    selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                ),
                modifier = Modifier.height(32.dp)
            )
        }
        
        // Пользовательский ввод времени
        AnimatedVisibility(
            visible = showCustomOption,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, start = 4.dp, end = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Ввод часов
                OutlinedTextField(
                    value = customHours,
                    onValueChange = { 
                        if (it.isEmpty() || it.toIntOrNull() != null) {
                            customHours = it
                            val totalMinutes = (customHours.toIntOrNull() ?: 0) * 60 + 
                                              (customMinutes.toIntOrNull() ?: 0)
                            if (totalMinutes > 0) {
                                onTimeSelected(totalMinutes)
                            }
                        }
                    },
                    label = { Text("Часы", style = MaterialTheme.typography.bodySmall) },
                    textStyle = MaterialTheme.typography.bodyMedium,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                // Ввод минут
                OutlinedTextField(
                    value = customMinutes,
                    onValueChange = { 
                        if (it.isEmpty() || (it.toIntOrNull() != null && it.toIntOrNull()!! < 60)) {
                            customMinutes = it
                            val totalMinutes = (customHours.toIntOrNull() ?: 0) * 60 + 
                                              (customMinutes.toIntOrNull() ?: 0)
                            if (totalMinutes > 0) {
                                onTimeSelected(totalMinutes)
                            }
                        }
                    },
                    label = { Text("Минуты", style = MaterialTheme.typography.bodySmall) },
                    textStyle = MaterialTheme.typography.bodyMedium,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    singleLine = true
                )
            }
        }
    }
}

/**
 * Форматирует текст подсказки для пользовательского ввода
 */
private fun formatTimeHint(minutes: Int): String {
    return when {
        minutes < 60 -> "$minutes минут"
        minutes == 60 -> "1 час"
        minutes < 1440 -> {
            val hours = minutes / 60
            val mins = minutes % 60
            if (mins == 0) "$hours ${formatHours(hours)}" 
            else "$hours ${formatHours(hours)} $mins ${formatMinutes(mins)}"
        }
        minutes == 1440 -> "1 день"
        minutes < 10080 -> {
            val days = minutes / 1440
            val hours = (minutes % 1440) / 60
            if (hours == 0) "$days ${formatDays(days)}" 
            else "$days ${formatDays(days)} $hours ${formatHours(hours)}"
        }
        minutes == 10080 -> "1 неделя"
        else -> {
            val weeks = minutes / 10080
            val days = (minutes % 10080) / 1440
            if (days == 0) "$weeks ${formatWeeks(weeks)}" 
            else "$weeks ${formatWeeks(weeks)} $days ${formatDays(days)}"
        }
    }
}

private fun formatMinutes(minutes: Int): String {
    return when {
        minutes % 10 == 1 && minutes % 100 != 11 -> "минута"
        minutes % 10 in 2..4 && (minutes % 100 < 10 || minutes % 100 >= 20) -> "минуты"
        else -> "минут"
    }
}

private fun formatHours(hours: Int): String {
    return when {
        hours % 10 == 1 && hours % 100 != 11 -> "час"
        hours % 10 in 2..4 && (hours % 100 < 10 || hours % 100 >= 20) -> "часа"
        else -> "часов"
    }
}

private fun formatDays(days: Int): String {
    return when {
        days % 10 == 1 && days % 100 != 11 -> "день"
        days % 10 in 2..4 && (days % 100 < 10 || days % 100 >= 20) -> "дня"
        else -> "дней"
    }
}

private fun formatWeeks(weeks: Int): String {
    return when {
        weeks % 10 == 1 && weeks % 100 != 11 -> "неделя"
        weeks % 10 in 2..4 && (weeks % 100 < 10 || weeks % 100 >= 20) -> "недели"
        else -> "недель"
    }
}

/**
 * Модель опции времени уведомления
 */
data class NotificationTimeOption(
    val minutes: Int,
    val label: String
) 