package com.example.taskapp.ui.screens

import android.content.Context
import android.media.MediaPlayer
import android.media.AudioAttributes
import android.media.AudioManager
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.taskapp.R
import com.example.taskapp.ui.components.SettingItem
import com.example.taskapp.ui.components.SettingsSection
import com.example.taskapp.ui.viewmodels.SettingsViewModel
import com.example.taskapp.notifications.TestNotificationManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import androidx.compose.runtime.rememberCoroutineScope

/**
 * Экран настроек для разработчиков с тестовыми функциями
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeveloperOptionsScreen(
    onNavigateBack: () -> Unit,
    isDarkTheme: Boolean,
    onShowOnboarding: () -> Unit,
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    
    // Счетчик для "секретов"
    var easterEggCounter by remember { mutableStateOf(0) }
    
    // Состояние для отображения диалога с пасхалкой
    var showEasterEggDialog by remember { mutableStateOf(false) }
    
    // Состояние для активации кнопки "Круто!"
    var isButtonEnabled by remember { mutableStateOf(false) }
    
    // MediaPlayer для воспроизведения звука пасхалки
    val mediaPlayer = remember { 
        MediaPlayer()
    }
    
    // Настраиваем MediaPlayer при входе на экран
    LaunchedEffect(Unit) {
        mediaPlayer.apply {
            // Предварительная инициализация для уменьшения задержек
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
        }
    }
    
    // Обработчик для активации кнопки через 3 секунды с использованием корутин вместо Handler
    val coroutineScope = rememberCoroutineScope()
    
    // Предварительно загружаем изображение для исключения задержек при его показе
    val easterEggDrawableId = R.drawable.easter_egg_image
    val easterEggImage = painterResource(id = easterEggDrawableId)
    
    // Функция для показа пасхалки
    fun showEasterEgg() {
        // Устанавливаем громкость на 50% в отдельной корутине
        coroutineScope.launch(Dispatchers.IO) {
            try {
                val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                val targetVolume = (maxVolume * 0.5).toInt()
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, targetVolume, 0)
                
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Громкость установлена на 50%", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Ошибка установки громкости: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Кнопка не активна в начале
        isButtonEnabled = false
        
        // Воспроизводим звук через корутину
        coroutineScope.launch(Dispatchers.IO) {
            try {
                withContext(Dispatchers.Main) {
                    mediaPlayer.apply {
                        reset()
                        setDataSource(context.resources.openRawResourceFd(R.raw.easter_egg_sound))
                        
                        // Асинхронная подготовка медиаплеера
                        prepareAsync()
                        
                        // Обработка события окончания подготовки
                        setOnPreparedListener { mp ->
                            mp.start()
                        }
                    }
                }
                
                // Активируем кнопку через 3 секунды с помощью корутины
                delay(3000)
                
                withContext(Dispatchers.Main) {
                    isButtonEnabled = true
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Ошибка воспроизведения звука: ${e.message}", Toast.LENGTH_SHORT).show()
                    // В случае ошибки кнопка должна быть активна сразу
                    isButtonEnabled = true
                }
            }
        }
        
        // Показываем диалог с изображением
        showEasterEggDialog = true
    }
    
    // Функция для закрытия пасхалки
    fun closeEasterEgg() {
        // Останавливаем звук в отдельной корутине
        coroutineScope.launch(Dispatchers.IO) {
            if (mediaPlayer.isPlaying) {
                mediaPlayer.stop()
            }
            
            withContext(Dispatchers.Main) {
                // Закрываем диалог
                showEasterEggDialog = false
            }
        }
    }
    
    // При уничтожении композиции освобождаем ресурсы MediaPlayer и Handler
    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer.release()
        }
    }
    
    // Диалог с пасхалкой
    if (showEasterEggDialog) {
        Dialog(
            onDismissRequest = { /* Ничего не делаем - диалог можно закрыть только по кнопке */ }
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Изображение пасхалки
                    Image(
                        painter = easterEggImage,
                        contentDescription = "Пасхалка",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                            .clip(MaterialTheme.shapes.medium)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Button(
                        onClick = { closeEasterEgg() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                        ),
                        enabled = isButtonEnabled
                    ) {
                        Text(
                            text = if (isButtonEnabled) "Круто!" else "Подождите...",
                            color = if (isButtonEnabled) 
                                MaterialTheme.colorScheme.onPrimary 
                            else 
                                MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Настройки разработчика") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                modifier = Modifier.statusBarsPadding()
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Предупреждение о разделе для разработчиков
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Внимание",
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "Эти настройки предназначены только для разработчиков. Использование некоторых функций может привести к ненормальному поведению приложения.",
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Секция тестирования уведомлений
            SettingsSection(title = "Тестирование уведомлений") {
                // Тестовое уведомление
                SettingItem(
                    title = "Отправить тестовое уведомление",
                    description = "Немедленная отправка тестового уведомления",
                    icon = Icons.Default.Notifications,
                    onClick = {
                        sendTestNotification(context)
                        Toast.makeText(context, "Тестовое уведомление отправлено", Toast.LENGTH_SHORT).show()
                    }
                )
                
                // Отложенное уведомление
                SettingItem(
                    title = "Отправить отложенное уведомление",
                    description = "Отправка уведомления через 10 секунд",
                    icon = Icons.Default.Timer,
                    onClick = {
                        sendDelayedNotification(context)
                        Toast.makeText(context, "Отложенное уведомление запланировано", Toast.LENGTH_SHORT).show()
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Секция тестирования онбординга
            SettingsSection(title = "Тестирование онбординга") {
                // Сброс статуса онбординга
                SettingItem(
                    title = "Сбросить онбординг",
                    description = "При следующем запуске будет показан онбординг",
                    icon = Icons.Default.Refresh,
                    onClick = {
                        settingsViewModel.setOnboardingCompleted(false)
                        Toast.makeText(context, "Онбординг сброшен", Toast.LENGTH_SHORT).show()
                    }
                )
                
                // Просмотр онбординга немедленно
                SettingItem(
                    title = "Показать онбординг сейчас",
                    description = "Переход на экран онбординга без перезапуска",
                    icon = Icons.Default.Preview,
                    onClick = {
                        onShowOnboarding()
                        Toast.makeText(context, "Переход на экран онбординга", Toast.LENGTH_SHORT).show()
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Секция отладочных инструментов
            SettingsSection(title = "Отладочные инструменты") {
                // Симуляция низкой производительности
                var isLowPerformanceModeEnabled by remember { mutableStateOf(false) }
                
                SettingItem(
                    title = "Режим низкой производительности",
                    description = "Симуляция работы на слабом устройстве",
                    icon = Icons.Default.Speed,
                    trailingContent = {
                        Switch(
                            checked = isLowPerformanceModeEnabled,
                            onCheckedChange = { isLowPerformanceModeEnabled = it }
                        )
                    }
                )
                
                // Очистка кэша приложения
                SettingItem(
                    title = "Очистить кэш приложения",
                    description = "Удаление временных файлов",
                    icon = Icons.Default.DeleteSweep,
                    onClick = {
                        // Будет реализовано позже
                        Toast.makeText(context, "Кэш очищен", Toast.LENGTH_SHORT).show()
                    }
                )
                
                // Принудительный краш для тестирования
                SettingItem(
                    title = "Симуляция краша приложения",
                    description = "Для тестирования отчётов об ошибках",
                    icon = Icons.Default.BugReport,
                    onClick = {
                        Toast.makeText(context, "Приложение сейчас аварийно завершится...", Toast.LENGTH_SHORT).show()
                        // Задержка, чтобы пользователь успел увидеть тост
                        coroutineScope.launch {
                            delay(1500)
                            throw RuntimeException("Тестовое исключение из раздела разработчика")
                        }
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Секция информации о приложении и устройстве
            SettingsSection(title = "Информация") {
                // Версия приложения
                SettingItem(
                    title = "Версия приложения",
                    description = "v1.0.0 (debug)",
                    icon = Icons.Default.Info
                )
                
                // Информация об устройстве
                SettingItem(
                    title = "Устройство",
                    description = "Android ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})",
                    icon = Icons.Default.PhoneAndroid
                )
                
                // Пасхалка
                SettingItem(
                    title = "О разработчике",
                    description = "Нажмите 5 раз для секрета",
                    icon = Icons.Default.Person,
                    onClick = {
                        easterEggCounter++
                        if (easterEggCounter >= 5) {
                            showEasterEgg()
                            easterEggCounter = 0
                        }
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// Функция для отправки тестового уведомления немедленно
private fun sendTestNotification(context: Context) {
    TestNotificationManager.sendTestNotification(context)
}

// Функция для отправки отложенного уведомления
private fun sendDelayedNotification(context: Context) {
    TestNotificationManager.scheduleDelayedNotification(context)
} 