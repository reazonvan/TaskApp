package com.example.taskapp.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.taskapp.notifications.NotificationScheduler
import com.example.taskapp.ui.theme.LocalAppSettings
import com.example.taskapp.ui.viewmodels.SettingsViewModel
import com.example.taskapp.util.DateFormatUtil
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    onNavigateBack: () -> Unit,
    isDarkTheme: Boolean,
    onNavigateToDeveloperOptions: () -> Unit
) {
    // Используем hiltViewModel для получения ViewModel
    val settingsViewModel: SettingsViewModel = hiltViewModel()
    val appSettings = LocalAppSettings.current
    
    // Получаем NotificationScheduler через hiltViewModel
    val notificationScheduler = settingsViewModel.notificationScheduler
    
    // Добавляем CoroutineScope для запуска отправки уведомления
    val coroutineScope = rememberCoroutineScope()
    
    // Контекст для работы с уведомлениями
    val context = LocalContext.current
    
    // Проверка разрешения на отправку уведомлений
    val hasNotificationPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    } else {
        true // До Android 13 разрешение не требуется явно запрашивать
    }
    
    // Лаунчер для запроса разрешения
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // После получения разрешения отправляем уведомление
            coroutineScope.launch {
                notificationScheduler.sendTestNotification()
            }
        } else {
            // Показываем сообщение, если разрешение не получено
            Toast.makeText(
                context,
                "Для отправки уведомлений необходимо разрешение",
                Toast.LENGTH_LONG
            ).show()
        }
    }
    
    // Получаем значения настроек для отображения
    val animationIntensity by settingsViewModel.animationIntensity.collectAsState()
    val textSize by settingsViewModel.textSize.collectAsState()
    val dateFormat by settingsViewModel.dateFormat.collectAsState()
    val notificationTime by settingsViewModel.notificationTime.collectAsState()
    val simplifiedMode by settingsViewModel.simplifiedMode.collectAsState()
    
    // Примеры форматирования даты
    val today = System.currentTimeMillis()
    val dateFormatNames = listOf("ДД.ММ.ГГГГ", "ММ/ДД/ГГГГ", "ГГГГ-ММ-ДД")
    val textSizeNames = listOf("Маленький", "Средний", "Большой")
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "О приложении",
                        style = MaterialTheme.typography.headlineMedium
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .padding(8.dp)
                            .clip(MaterialTheme.shapes.small)
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Назад"
                        )
                    }
                },
                modifier = Modifier.statusBarsPadding()
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.Start
            ) {
                // Заголовок блока с информацией
                Text(
                    text = "Информация",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                // Блок с информацией о приложении
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        InfoRow(title = "Версия", value = "1.0.0")
                        InfoRow(title = "Сборка", value = "10")
                        InfoRow(title = "Дата сборки", value = DateFormatUtil.formatDateTime(today, appSettings))
                        InfoRow(title = "Разработчик", value = "Петров И. Д.")
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Заголовок блока с текущими настройками
                Text(
                    text = "Текущие настройки",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                // Блок с текущими настройками
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        InfoRow(title = "Тема", value = if (isDarkTheme) "Темная" else "Светлая")
                        InfoRow(title = "Размер текста", value = textSizeNames[textSize])
                        InfoRow(title = "Интенсивность анимации", value = "${(animationIntensity * 100).toInt()}%")
                        InfoRow(title = "Формат даты", value = dateFormatNames[dateFormat])
                        InfoRow(title = "Пример даты", value = DateFormatUtil.formatDate(today, appSettings))
                        InfoRow(title = "Время уведомлений", value = formatNotificationTime(notificationTime))
                        InfoRow(title = "Упрощенный режим", value = if (simplifiedMode) "Включен" else "Выключен")
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Блок с описанием приложения
                Text(
                    text = "О приложении",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "TaskApp - приложение для управления задачами и дедлайнами. " +
                                   "Оно позволяет организовать работу с преподавателями, отслеживать " +
                                   "задачи и получать уведомления о приближающихся дедлайнах.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "© 2025 TaskApp Team. Все права защищены.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Добавляем предупреждение, если разрешение не получено
                if (!hasNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Требуется разрешение на отправку уведомлений",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Кнопка "Настройки разработчика" в самом конце экрана
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToDeveloperOptions() }
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Code,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = "Настройки разработчика",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Text(
                                    text = "Тестовые функции и инструменты отладки",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.5f)
                        )
                    }
                }
                
                // Дополнительный отступ внизу для лучшего просмотра
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun InfoRow(
    title: String,
    value: String,
    titleColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = titleColor,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = valueColor
        )
    }
}

private fun formatNotificationTime(minutes: Int): String {
    return when {
        minutes < 60 -> "$minutes мин. до дедлайна"
        minutes == 60 -> "1 час до дедлайна"
        minutes < 1440 -> "${minutes / 60} ч. до дедлайна"
        minutes == 1440 -> "1 день до дедлайна"
        else -> "${minutes / 1440} дн. до дедлайна"
    }
} 