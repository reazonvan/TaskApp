package com.example.taskapp.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.taskapp.ui.components.InteractiveBackground
import com.example.taskapp.ui.viewmodels.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    onNavigateToAbout: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    // Состояния настроек
    val colorScheme by viewModel.colorScheme.collectAsState()
    val animationIntensity by viewModel.animationIntensity.collectAsState()
    val textSize by viewModel.textSize.collectAsState()
    
    val dateFormat by viewModel.dateFormat.collectAsState()
    val teachersSortType by viewModel.teachersSortType.collectAsState()
    val defaultShowNameFirst by viewModel.defaultShowNameFirst.collectAsState()
    
    val notificationTime by viewModel.notificationTime.collectAsState()
    val notificationSound by viewModel.notificationSound.collectAsState()
    val notificationVibration by viewModel.notificationVibration.collectAsState()
    val doNotDisturbStart by viewModel.doNotDisturbStart.collectAsState()
    val doNotDisturbEnd by viewModel.doNotDisturbEnd.collectAsState()
    val doNotDisturbEnabled by viewModel.doNotDisturbEnabled.collectAsState()
    
    val updateInterval by viewModel.updateInterval.collectAsState()
    val simplifiedMode by viewModel.simplifiedMode.collectAsState()
    
    Box(modifier = Modifier.fillMaxSize()) {
        // Фоновый интерактивный элемент
        InteractiveBackground(
            modifier = Modifier.fillMaxSize(),
            isDarkTheme = isDarkTheme,
            uncompletedTasksCount = 0,
            isAllTasksCompleted = true,
            pulseStrength = animationIntensity
        )
        
        // Основной контент
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent)
        ) {
            // Верхняя панель с кнопкой возврата и заголовком
            TopAppBar(
                title = {
                    Text(
                        text = "Настройки",
                        style = MaterialTheme.typography.headlineMedium
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .padding(8.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Назад"
                        )
                    }
                },
                actions = {
                    // Кнопка переключения темы
                    Switch(
                        checked = isDarkTheme,
                        onCheckedChange = { onToggleTheme() },
                        thumbContent = {
                            Icon(
                                imageVector = if (isDarkTheme) Icons.Default.DarkMode else Icons.Default.LightMode,
                                contentDescription = null,
                                modifier = Modifier.size(SwitchDefaults.IconSize)
                            )
                        },
                        modifier = Modifier.padding(end = 16.dp)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
            
            // Список настроек
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Раздел Интерфейс
                item {
                    SettingsHeader(
                        icon = Icons.Default.Palette,
                        title = "Интерфейс"
                    )
                }
                
                // Настройка интенсивности анимации
                item {
                    SliderSetting(
                        title = "Интенсивность анимации",
                        value = animationIntensity,
                        onValueChange = { viewModel.updateAnimationIntensity(it) },
                        valueRange = 0f..2f,
                        steps = 20,
                        icon = Icons.Default.Animation
                    )
                }
                
                // Настройка размера текста
                item {
                    ChoiceSetting(
                        title = "Размер текста",
                        options = listOf("Маленький", "Средний", "Большой"),
                        selectedIndex = textSize,
                        onSelect = { viewModel.updateTextSize(it) },
                        icon = Icons.Default.FormatSize
                    )
                }
                
                // Разделитель
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    SettingsHeader(
                        icon = Icons.Default.DataObject,
                        title = "Отображение данных"
                    )
                }
                
                // Формат даты
                item {
                    ChoiceSetting(
                        title = "Формат даты",
                        options = listOf("ДД.ММ.ГГГГ", "ММ/ДД/ГГГГ", "ГГГГ-ММ-ДД"),
                        selectedIndex = dateFormat,
                        onSelect = { viewModel.updateDateFormat(it) },
                        icon = Icons.Default.DateRange
                    )
                }
                
                // Сортировка преподавателей
                item {
                    ChoiceSetting(
                        title = "Сортировка преподавателей",
                        options = listOf("По алфавиту", "По долгам", "По дате добавления"),
                        selectedIndex = teachersSortType,
                        onSelect = { viewModel.updateTeachersSortType(it) },
                        icon = Icons.Default.Sort
                    )
                }
                
                // Что показывать первым
                item {
                    SwitchSetting(
                        title = "Показывать имя преподавателя первым",
                        description = "Имя преподавателя отображается перед названием предмета",
                        checked = defaultShowNameFirst,
                        onCheckedChange = { viewModel.updateDefaultShowNameFirst(it) },
                        icon = Icons.Default.Person
                    )
                }
                
                // Разделитель
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    SettingsHeader(
                        icon = Icons.Default.Notifications,
                        title = "Уведомления"
                    )
                }
                
                // Время напоминания
                item {
                    ChoiceSetting(
                        title = "Напоминать о дедлайне за",
                        options = listOf("15 минут", "30 минут", "1 час", "3 часа", "6 часов", "1 день"),
                        selectedIndex = when (notificationTime) {
                            15 -> 0
                            30 -> 1
                            60 -> 2
                            180 -> 3
                            360 -> 4
                            1440 -> 5
                            else -> 2
                        },
                        onSelect = {
                            val minutes = when (it) {
                                0 -> 15
                                1 -> 30
                                2 -> 60
                                3 -> 180
                                4 -> 360
                                5 -> 1440
                                else -> 60
                            }
                            viewModel.updateNotificationTime(minutes)
                        },
                        icon = Icons.Default.Timer
                    )
                }
                
                // Звук уведомлений
                item {
                    SwitchSetting(
                        title = "Звук уведомлений",
                        description = "Воспроизводить звук при уведомлениях",
                        checked = notificationSound,
                        onCheckedChange = { viewModel.updateNotificationSound(it) },
                        icon = Icons.Default.VolumeUp
                    )
                }
                
                // Вибрация уведомлений
                item {
                    SwitchSetting(
                        title = "Вибрация при уведомлениях",
                        description = "Устройство будет вибрировать при уведомлениях",
                        checked = notificationVibration,
                        onCheckedChange = { viewModel.updateNotificationVibration(it) },
                        icon = Icons.Default.Vibration
                    )
                }
                
                // Кнопка отправки тестового уведомления
                item {
                    var showDialog by remember { mutableStateOf(false) }
                    var testResult by remember { mutableStateOf<Pair<Boolean, String?>?>(null) }
                    var isLoading by remember { mutableStateOf(false) }
                    
                    TestNotificationButton(
                        onClick = {
                            isLoading = true
                            viewModel.sendTestNotification { result ->
                                testResult = result
                                showDialog = true
                                isLoading = false
                            }
                        },
                        isLoading = isLoading
                    )
                    
                    // Диалог с результатом отправки тестового уведомления
                    if (showDialog) {
                        AlertDialog(
                            onDismissRequest = { 
                                showDialog = false
                                testResult = null
                            },
                            title = {
                                Text(text = if (testResult?.first == true) "Успешно!" else "Ошибка")
                            },
                            text = {
                                Text(
                                    text = testResult?.second ?: 
                                        if (testResult?.first == true) "Тестовое уведомление отправлено. Проверьте панель уведомлений." 
                                        else "Не удалось отправить тестовое уведомление."
                                )
                            },
                            confirmButton = {
                                TextButton(onClick = { 
                                    showDialog = false
                                    testResult = null
                                }) {
                                    Text("ОК")
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = if (testResult?.first == true) Icons.Default.CheckCircle else Icons.Default.Error,
                                    contentDescription = null,
                                    tint = if (testResult?.first == true) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                                )
                            }
                        )
                    }
                }
                
                // Режим "Не беспокоить"
                item {
                    SwitchSetting(
                        title = "Режим \"Не беспокоить\"",
                        description = "В указанное время уведомления не будут отображаться",
                        checked = doNotDisturbEnabled,
                        onCheckedChange = { viewModel.updateDoNotDisturbEnabled(it) },
                        icon = Icons.Default.DoNotDisturb
                    )
                }
                
                // Время "Не беспокоить"
                item {
                    if (doNotDisturbEnabled) {
                        TimeRangeSetting(
                            title = "Время режима \"Не беспокоить\"",
                            startHour = doNotDisturbStart,
                            endHour = doNotDisturbEnd,
                            onTimeRangeChanged = { start, end ->
                                viewModel.updateDoNotDisturbStart(start)
                                viewModel.updateDoNotDisturbEnd(end)
                            }
                        )
                    }
                }
                
                // Разделитель
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    SettingsHeader(
                        icon = Icons.Default.Speed,
                        title = "Производительность"
                    )
                }
                
                // Интервал обновления
                item {
                    ChoiceSetting(
                        title = "Интервал обновления данных",
                        options = listOf("1 секунда", "3 секунды", "5 секунд", "10 секунд", "30 секунд"),
                        selectedIndex = when (updateInterval) {
                            1000 -> 0
                            3000 -> 1
                            5000 -> 2
                            10000 -> 3
                            30000 -> 4
                            else -> 2
                        },
                        onSelect = {
                            val interval = when (it) {
                                0 -> 1000
                                1 -> 3000
                                2 -> 5000
                                3 -> 10000
                                4 -> 30000
                                else -> 5000
                            }
                            viewModel.updateUpdateInterval(interval)
                        },
                        icon = Icons.Default.Update
                    )
                }
                
                // Упрощенный режим
                item {
                    SwitchSetting(
                        title = "Упрощенный режим",
                        description = "Отключает часть анимаций для экономии заряда батареи",
                        checked = simplifiedMode,
                        onCheckedChange = { viewModel.updateSimplifiedMode(it) },
                        icon = Icons.Default.BatteryChargingFull
                    )
                }
                
                // Добавляем раздел "О приложении" в самый конец
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    SettingsHeader(
                        icon = Icons.Default.Info,
                        title = "Информация"
                    )
                }
                
                // Кнопка "О приложении"
                item {
                    ActionSetting(
                        title = "О приложении",
                        description = "Информация о версии и настройках",
                        onClick = onNavigateToAbout,
                        icon = Icons.Default.Info
                    )
                }
                
                // Отступ внизу списка
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
fun SettingsHeader(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
    }
    
    HorizontalDivider(
        modifier = Modifier.padding(vertical = 8.dp),
        color = MaterialTheme.colorScheme.outlineVariant
    )
}

@Composable
fun SwitchSetting(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .clickable { onCheckedChange(!checked) }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.alpha(0.7f)
            )
        }
        
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            thumbContent = if (checked) {
                {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(SwitchDefaults.IconSize)
                    )
                }
            } else null
        )
    }
}

@Composable
fun ChoiceSetting(
    title: String,
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    var expanded by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Text(
                    text = options.getOrNull(selectedIndex) ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.alpha(0.7f)
                )
            }
            
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth(0.7f)
        ) {
            options.forEachIndexed { index, option ->
                DropdownMenuItem(
                    text = { Text(text = option) },
                    onClick = {
                        onSelect(index)
                        expanded = false
                    },
                    leadingIcon = {
                        if (index == selectedIndex) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeRangeSetting(
    title: String,
    startHour: Int,
    endHour: Int,
    onTimeRangeChanged: (Int, Int) -> Unit
) {
    var start by remember { mutableStateOf(startHour) }
    var end by remember { mutableStateOf(endHour) }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(16.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Начало периода
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "С",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                OutlinedCard(
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .width(100.dp),
                    onClick = {}
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = String.format("%02d:00", start),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
                
                Row(
                    modifier = Modifier.padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            start = (start - 1).coerceIn(0, 23)
                            onTimeRangeChanged(start, end)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Remove,
                            contentDescription = "Уменьшить"
                        )
                    }
                    
                    IconButton(
                        onClick = {
                            start = (start + 1).coerceIn(0, 23)
                            onTimeRangeChanged(start, end)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Увеличить"
                        )
                    }
                }
            }
            
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = null,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            
            // Конец периода
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "До",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                OutlinedCard(
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .width(100.dp),
                    onClick = {}
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = String.format("%02d:00", end),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
                
                Row(
                    modifier = Modifier.padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            end = (end - 1).coerceIn(0, 23)
                            onTimeRangeChanged(start, end)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Remove,
                            contentDescription = "Уменьшить"
                        )
                    }
                    
                    IconButton(
                        onClick = {
                            end = (end + 1).coerceIn(0, 23)
                            onTimeRangeChanged(start, end)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Увеличить"
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SliderSetting(
    title: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    steps: Int = 0,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Отображение процента
            val percent = ((value - valueRange.start) / (valueRange.endInclusive - valueRange.start) * 100).toInt()
            Text(
                text = "$percent%",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            steps = steps,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun ActionSetting(
    title: String,
    description: String,
    onClick: () -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Surface(
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Иконка
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .padding(end = 16.dp)
                    .size(24.dp)
            )
            
            // Текст и описание
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                if (description.isNotEmpty()) {
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Стрелка вправо
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun TestNotificationButton(
    onClick: () -> Unit,
    isLoading: Boolean
) {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isLoading, onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(24.dp)
                        .padding(end = 16.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Icon(
                    imageVector = Icons.Default.NotificationsActive,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier
                        .padding(end = 16.dp)
                        .size(24.dp)
                )
            }
            
            Text(
                text = if (isLoading) "Отправка..." else "Отправить тестовое уведомление",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
} 