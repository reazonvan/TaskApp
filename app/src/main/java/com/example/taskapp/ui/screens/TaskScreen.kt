package com.example.taskapp.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.taskapp.data.model.Task
import com.example.taskapp.ui.components.DatePicker
import com.example.taskapp.ui.components.LoadingAnimation
import com.example.taskapp.ui.components.AddTaskDialog
import com.example.taskapp.ui.components.SwipeToDeleteTask
import com.example.taskapp.ui.components.TaskList
import com.example.taskapp.ui.viewmodels.TaskViewModel
import com.example.taskapp.ui.viewmodels.TeachersViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.material3.ExperimentalMaterial3Api
import com.example.taskapp.ui.components.AnimatedButton
import com.example.taskapp.ui.components.NotificationTimePicker
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.rounded.Add
import androidx.compose.ui.layout.ContentScale
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Assessment
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Notifications
import com.example.taskapp.ui.components.ThemeToggleButton
import androidx.compose.ui.layout.onGloballyPositioned
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

// Обновляем счетчики на главном экране при добавлении, удалении или изменении задачи
private fun updateTeachersData(
    onNavigateBack: () -> Unit,
    teachersViewModel: TeachersViewModel
) {
    // Обновляем данные для обновления счетчика на главном экране
    teachersViewModel.refreshData()
    // Возвращаемся на предыдущий экран
    onNavigateBack()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskScreen(
    teacherId: Long,
    onNavigateBack: () -> Unit,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    viewModel: TaskViewModel = hiltViewModel(),
    teachersViewModel: TeachersViewModel = hiltViewModel()
) {
    // Получаем данные из ViewModel
    val teacher by viewModel.teacher.collectAsState()
    val tasksState by viewModel.tasks.collectAsState(initial = emptyList())
    val uncompletedCount by viewModel.uncompletedCount.collectAsState(initial = 0)
    
    // Отслеживаем изменения в задачах для обновления счетчика на главном экране
    LaunchedEffect(tasksState, uncompletedCount) {
        teachersViewModel.refreshData()
    }
    
    // Состояние диалога добавления задачи
    var showAddDialog by remember { mutableStateOf(false) }
    
    // Переменные для добавления новой задачи
    var newTaskTitle by remember { mutableStateOf("") }
    var newTaskDescription by remember { mutableStateOf("") }
    var newTaskDeadline by remember { mutableStateOf<Long?>(null) }
    var notifyBeforeMinutes by remember { mutableStateOf<Long?>(60L) } // По умолчанию за 1 час (60 минут)
    
    // Анимация появления заголовка
    val titleAlpha = remember { Animatable(0f) }
    LaunchedEffect(key1 = true) {
        titleAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800)
        )
    }
    
    // Эффект размытия для фона при открытии диалога
    val blurRadius by animateDpAsState(
        targetValue = if (showAddDialog) 10.dp else 0.dp,
        animationSpec = tween(durationMillis = 300)
    )
    
    // Градиент для верхней панели
    val primaryContainer = MaterialTheme.colorScheme.primaryContainer
    val gradientColors = listOf(
        primaryContainer,
        primaryContainer.copy(alpha = 0.8f),
        primaryContainer.copy(alpha = 0.6f)
    )
    
    // Предварительно обновляем счетчики при закрытии экрана
    val enhancedNavigateBack = {
        updateTeachersData(onNavigateBack, teachersViewModel)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column(
                        horizontalAlignment = Alignment.Start,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Задания",
                            modifier = Modifier.alpha(titleAlpha.value),
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = "преподавателя",
                            modifier = Modifier.alpha(titleAlpha.value),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Normal
                            )
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            enhancedNavigateBack()
                        },
                        modifier = Modifier
                            .padding(8.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ArrowBack,
                            contentDescription = "Назад",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                },
                actions = {
                    // Кнопка переключения темы
                    ThemeToggleButton(
                        isDarkTheme = isDarkTheme,
                        onToggleTheme = onToggleTheme
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                modifier = Modifier
                    .background(
                        Brush.verticalGradient(
                            colors = gradientColors
                        )
                    )
                    .statusBarsPadding() // Добавляем отступ для системной панели
            )
        },
        floatingActionButton = {
            // Кнопка с анимацией
            AnimatedVisibility(
                visible = true,
                enter = fadeIn() + scaleIn() + slideInVertically { it },
                exit = fadeOut() + scaleOut()
            ) {
                val buttonScale = remember { Animatable(1f) }
                val pulsationScale = remember { Animatable(1f) }
                val coroutineScope = rememberCoroutineScope()
                
                // Эффект пульсации
                LaunchedEffect(key1 = true) {
                    while(true) {
                        // Плавная пульсация кнопки
                        pulsationScale.animateTo(
                            targetValue = 1.1f,
                            animationSpec = tween(1000, easing = LinearEasing)
                        )
                        pulsationScale.animateTo(
                            targetValue = 1f,
                            animationSpec = tween(1000, easing = LinearEasing)
                        )
                        delay(1000) // Задержка между пульсациями
                    }
                }
                
                Box(contentAlignment = Alignment.Center) {
                    // Подсветка за кнопкой
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(64.dp)
                            .graphicsLayer {
                                scaleX = pulsationScale.value
                                scaleY = pulsationScale.value
                                alpha = 0.6f
                            }
                    ) {
                        // Эффект свечения
                        val primary = MaterialTheme.colorScheme.primary
                        Canvas(modifier = Modifier.size(64.dp)) {
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        primary.copy(alpha = 0.5f),
                                        primary.copy(alpha = 0.1f),
                                        Color.Transparent
                                    ),
                                    center = center,
                                    radius = size.minDimension / 2
                                ),
                                radius = size.minDimension / 2
                            )
                        }
                    }
                    
                    // Основная кнопка
                    FloatingActionButton(
                        onClick = {
                            coroutineScope.launch {
                                // Анимация нажатия
                                buttonScale.animateTo(
                                    targetValue = 0.85f,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessLow
                                    )
                                )
                                buttonScale.animateTo(
                                    targetValue = 1f,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessLow
                                    )
                                )
                            }
                            
                            // Сбрасываем значения полей при открытии диалога
                            newTaskTitle = ""
                            newTaskDescription = ""
                            newTaskDeadline = null
                            notifyBeforeMinutes = 60L
                            
                            showAddDialog = true
                        },
                        shape = CircleShape,
                        containerColor = Color.Transparent, // Прозрачный контейнер для градиента
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        elevation = FloatingActionButtonDefaults.elevation(
                            defaultElevation = 0.dp, // Убираем встроенную тень
                            pressedElevation = 0.dp
                        ),
                        modifier = Modifier
                            // Красивый градиент кнопки
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.secondary
                                    )
                                ),
                                shape = CircleShape
                            )
                            .shadow(8.dp, CircleShape)
                            .size(56.dp)
                            .graphicsLayer {
                                scaleX = buttonScale.value
                                scaleY = buttonScale.value
                            }
                    ) {
                        // Улучшенная иконка
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = "Добавить несданную работу",
                            modifier = Modifier.size(30.dp),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
                .blur(blurRadius) // Применяем эффект размытия
        ) {
            // Статистика долгов - красивая карточка с градиентом
            AnimatedVisibility(
                visible = true,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .shadow(
                            elevation = 8.dp,
                            shape = RoundedCornerShape(16.dp),
                            spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        ),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f),
                                        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f)
                                    )
                                )
                            )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Иконка статистики
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(
                                                MaterialTheme.colorScheme.primary,
                                                MaterialTheme.colorScheme.secondary
                                            )
                                        )
                                    )
                                    .padding(12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Assessment,
                                    contentDescription = null,
                                    tint = Color.White
                                )
                            }
                            
                            Spacer(modifier = Modifier.width(16.dp))
                            
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "Статистика",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Несданных работ: $uncompletedCount",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                            
                            // Визуальное представление соотношения
                            if (tasksState.isNotEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.surface)
                                        .padding(4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    // Расчёт соотношения
                                    val completedRatio = tasksState.count { it.isCompleted }.toFloat() / tasksState.size
                                    
                                    // Определяем цвет прогресс-бара в зависимости от процента выполнения
                                    val progressColor = when {
                                        completedRatio < 0.3f -> Color(0xFFE57373) // красный
                                        completedRatio < 0.7f -> Color(0xFFFFB74D) // оранжевый
                                        else -> Color(0xFF81C784) // зеленый
                                    }
                                    
                                    // Обновляем значение процента с анимацией
                                    var animatedPercentage by remember { mutableStateOf(0f) }
                                    LaunchedEffect(completedRatio) {
                                        animate(
                                            initialValue = animatedPercentage,
                                            targetValue = completedRatio,
                                            animationSpec = tween(durationMillis = 1000)
                                        ) { value, _ ->
                                            animatedPercentage = value
                                        }
                                    }
                                    
                                    // Фон для прогресс-бара
                                    Box(
                                        modifier = Modifier
                                            .size(56.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.surfaceVariant)
                                    )
                                    
                                    // Прогресс-бар
                                    CircularProgressIndicator(
                                        progress = { animatedPercentage },
                                        modifier = Modifier.size(56.dp),
                                        strokeWidth = 6.dp,
                                        color = progressColor,
                                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                                    )
                                    
                                    // Процент в центре
                                    Text(
                                        text = "${(animatedPercentage * 100).toInt()}%",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = progressColor
                                    )
                                    
                                    // Эффект конфети при достижении 100%
                                    if (completedRatio >= 0.999f) {
                                        val confettiColors = listOf(
                                            Color(0xFFFFC107),
                                            Color(0xFF4CAF50),
                                            Color(0xFF2196F3),
                                            Color(0xFFF44336),
                                            Color(0xFF9C27B0)
                                        )
                                        
                                        // Запускаем эффект салюта
                                        SimpleSalutEffect(confettiColors)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            // Если список пуст, показываем сообщение
            if (tasksState.isEmpty()) {
                EmptyStateMessage()
            } else {
                // Показываем список задач с анимациями
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    itemsIndexed(
                        items = tasksState,
                        key = { _, task -> task.id }
                    ) { index, task ->
                        // Анимация появления каждого элемента
                        val itemAppearDelay = index * 50 // Задержка для стаггер-эффекта
                        var itemVisible by remember { mutableStateOf(false) }
                        
                        LaunchedEffect(Unit) {
                            delay(itemAppearDelay.toLong())
                            itemVisible = true
                        }
                        
                        AnimatedVisibility(
                            visible = itemVisible,
                            enter = slideInHorizontally { it / 2 } + 
                                    fadeIn(animationSpec = tween(durationMillis = 300))
                        ) {
                            TaskItem(
                                task = task,
                                onToggleCompletion = {
                                    // Используем ViewModel для обновления задачи
                                    viewModel.updateTaskCompletion(task.id, !task.isCompleted)
                                },
                                onDelete = {
                                    // Используем ViewModel для удаления задачи
                                    viewModel.deleteTask(task.id)
                                },
                                onUpdateNotificationTime = { newNotifyBeforeMinutes ->
                                    // Используем ViewModel для обновления времени уведомления
                                    viewModel.updateNotificationTime(task.id, newNotifyBeforeMinutes)
                                }
                            )
                        }
                    }
                    // Дополнительное пространство внизу для FAB
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
        
        // Диалог добавления задачи
        if (showAddDialog) {
            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                shape = RoundedCornerShape(16.dp),
                title = { 
                    Text(
                        text = "Добавить несданную работу",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    ) 
                },
                text = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = newTaskTitle,
                            onValueChange = { newTaskTitle = it },
                            label = { Text("Название работы") },
                            placeholder = { Text("Например: Курсовая работа") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f)
                            )
                        )
                        
                        OutlinedTextField(
                            value = newTaskDescription,
                            onValueChange = { newTaskDescription = it },
                            label = { Text("Описание") },
                            placeholder = { Text("Опишите задание") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f)
                            ),
                            minLines = 3
                        )
                        
                        // Выбор даты дедлайна
                        DatePicker(
                            selectedDate = newTaskDeadline,
                            onDateSelected = { newTaskDeadline = it }
                        )
                        
                        // Выбор времени уведомления
                        if (newTaskDeadline != null) {
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            NotificationTimePicker(
                                selectedMinutes = notifyBeforeMinutes?.toInt() ?: 60,
                                onTimeSelected = { selectedTime -> 
                                    notifyBeforeMinutes = selectedTime?.toLong()
                                }
                            )
                        }
                        
                        // Кнопки действий
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(
                                onClick = { showAddDialog = false }
                            ) {
                                Text("Отмена")
                            }
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            Button(
                                onClick = {
                                    if (newTaskTitle.isNotBlank()) {
                                        // Используем ViewModel для добавления задачи
                                        viewModel.addTask(
                                            title = newTaskTitle,
                                            description = newTaskDescription,
                                            deadline = newTaskDeadline,
                                            teacherId = teacherId,
                                            notifyBeforeMinutes = notifyBeforeMinutes?.toInt() ?: 60
                                        )
                                        showAddDialog = false
                                    }
                                },
                                shape = RoundedCornerShape(8.dp),
                                enabled = newTaskTitle.isNotBlank()
                            ) {
                                Text("Добавить")
                            }
                        }
                    }
                },
                confirmButton = {
                    // Пустой контент, так как кнопки определены в тексте
                },
                dismissButton = {
                    // Пустой контент, так как кнопки определены в тексте
                }
            )
        }
    }
}

@Composable
fun TaskItem(
    task: Task,
    onToggleCompletion: () -> Unit,
    onDelete: () -> Unit,
    onUpdateNotificationTime: (Int) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showNotificationTimePicker by remember { mutableStateOf(false) }
    
    // Анимации для карточки
    val scale = remember { Animatable(1f) }
    val alpha by animateFloatAsState(
        targetValue = if (task.isCompleted) 0.7f else 1f,
        animationSpec = tween(durationMillis = 300)
    )
    
    // Цвета в зависимости от статуса дедлайна
    val isOverdue = task.deadline != null && task.deadline < System.currentTimeMillis() && !task.isCompleted
    val cardBorderColor = when {
        isOverdue -> MaterialTheme.colorScheme.error
        task.isCompleted -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.outlineVariant
    }
    
    // Форматирование даты
    val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale("ru"))
    val dateString = task.deadline?.let { dateFormat.format(Date(it)) } ?: "Без срока"
    
    SwipeToDeleteTask(
        onDelete = onDelete
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .graphicsLayer {
                    scaleX = scale.value
                    scaleY = scale.value
                    this.alpha = alpha
                }
                .border(
                    width = 1.dp,
                    color = cardBorderColor,
                    shape = RoundedCornerShape(12.dp)
                ),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = if (task.isCompleted) 1.dp else 3.dp
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Checkbox для отметки о выполнении
                    Checkbox(
                        checked = task.isCompleted,
                        onCheckedChange = { onToggleCompletion() },
                        colors = CheckboxDefaults.colors(
                            checkedColor = MaterialTheme.colorScheme.primary,
                            uncheckedColor = MaterialTheme.colorScheme.outline
                        ),
                        modifier = Modifier
                            .size(24.dp)
                            .padding(end = 8.dp)
                    )
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = task.title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        
                        if (task.description.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = task.description,
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                    
                    // Иконка удаления
                    IconButton(
                        onClick = { showDeleteConfirm = true }
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Delete,
                            contentDescription = "Удалить",
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                        )
                    }
                }
                
                // Дедлайн
                if (task.deadline != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.CalendarToday,
                            contentDescription = null,
                            tint = if (isOverdue) 
                                MaterialTheme.colorScheme.error 
                            else 
                                MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        
                        Spacer(modifier = Modifier.width(4.dp))
                        
                        // Статус дедлайна
                        val deadlineLabel = when {
                            isOverdue -> "Просрочено: $dateString"
                            task.isCompleted -> "Сдано: $dateString"
                            else -> "Срок: $dateString"
                        }
                        
                        Text(
                            text = deadlineLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isOverdue) 
                                MaterialTheme.colorScheme.error 
                            else 
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            modifier = Modifier.weight(1f)
                        )
                        
                        // Кнопка настройки уведомления (только для задач с дедлайном и без выполнения)
                        if (!task.isCompleted && task.deadline > System.currentTimeMillis()) {
                            TextButton(
                                onClick = { showNotificationTimePicker = true },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Notifications,
                                    contentDescription = "Настроить уведомление",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Уведомить за ${formatNotificationTime(task.notifyBeforeMinutes?.toInt() ?: 60)}",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    
    // Диалог подтверждения удаления
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            shape = RoundedCornerShape(16.dp),
            title = { Text("Удалить работу") },
            text = { Text("Вы уверены, что хотите удалить работу \"${task.title}\"?") },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete()
                        showDeleteConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Удалить")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showDeleteConfirm = false }
                ) {
                    Text("Отмена")
                }
            }
        )
    }
    
    // Диалог выбора времени уведомления
    if (showNotificationTimePicker) {
        var dialogVisible by remember { mutableStateOf(false) }
        
        LaunchedEffect(key1 = true) {
            dialogVisible = true
        }
        
        // Анимация для диалога
        val scale by animateFloatAsState(
            targetValue = if (dialogVisible) 1f else 0.8f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
        
        val alpha by animateFloatAsState(
            targetValue = if (dialogVisible) 1f else 0f,
            animationSpec = tween(durationMillis = 300)
        )
        
        AlertDialog(
            onDismissRequest = { 
                dialogVisible = false
                coroutineScope.launch {
                    delay(150)
                    showNotificationTimePicker = false
                }
            },
            modifier = Modifier
                .fillMaxWidth(0.92f)  // Уменьшаем ширину диалога
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    this.alpha = alpha
                },
            shape = RoundedCornerShape(20.dp),
            title = {
                // Более компактный заголовок
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.primaryContainer
                                )
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Notifications,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Text(
                        text = "Настройка уведомления",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 0.dp)
                ) {
                    // Компактная информация о задаче
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .padding(6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Assignment,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Text(
                            text = task.title,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // Более компактный выбор времени
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outlineVariant,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(4.dp)
                    ) {
                        NotificationTimePicker(
                            selectedMinutes = task.notifyBeforeMinutes,
                            onTimeSelected = { selectedTime -> 
                                onUpdateNotificationTime(selectedTime ?: 60)
                                dialogVisible = false
                                coroutineScope.launch {
                                    delay(150)
                                    showNotificationTimePicker = false
                                }
                            }
                        )
                    }
                }
            },
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    // Компактные кнопки
                    TextButton(
                        onClick = {
                            dialogVisible = false
                            coroutineScope.launch {
                                delay(150)
                                showNotificationTimePicker = false
                            }
                        },
                        modifier = Modifier.padding(end = 4.dp)
                    ) {
                        Text("Отмена", style = MaterialTheme.typography.bodyMedium)
                    }
                    
                    Button(
                        onClick = {
                            dialogVisible = false
                            coroutineScope.launch {
                                delay(150)
                                showNotificationTimePicker = false
                            }
                        },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("Готово", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            },
            dismissButton = null // Убираем отдельную кнопку отмены, она уже есть в confirmButton
        )
    }
}

// Функция для форматирования времени уведомления в читаемый вид
private fun formatNotificationTime(minutes: Int): String {
    return when {
        minutes < 60 -> "$minutes мин."
        minutes < 1440 -> "${minutes / 60} ч."
        minutes < 10080 -> "${minutes / 1440} дн."
        else -> "${minutes / 10080} нед."
    }
}

@Composable
fun EmptyStateMessage() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Анимация появления текста
        var textVisible by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) {
            delay(300)
            textVisible = true
        }

        AnimatedVisibility(
            visible = textVisible,
            enter = fadeIn(animationSpec = tween(durationMillis = 800)) + 
                    slideInVertically(
                        animationSpec = tween(durationMillis = 800),
                        initialOffsetY = { it / 2 }
                    )
        ) {
            Text(
                text = "У вас нет несданных работ",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        LoadingAnimation()
        
        Spacer(modifier = Modifier.height(24.dp))
        
        AnimatedVisibility(
            visible = textVisible,
            enter = fadeIn(animationSpec = tween(durationMillis = 800, delayMillis = 300)) + 
                    slideInVertically(
                        animationSpec = tween(durationMillis = 800, delayMillis = 300),
                        initialOffsetY = { it / 2 }
                    )
        ) {
            Text(
                text = "Нажмите на кнопку '+', чтобы добавить новую несданную работу",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
        }
    }
}

// Простой эффект салюта
@Composable
private fun SimpleSalutEffect(colors: List<Color>) {
    // Центр салюта - центр экрана
    var centerX by remember { mutableStateOf(0f) }
    var centerY by remember { mutableStateOf(0f) }
    
    // Увеличиваем количество линий до 24 для большей насыщенности
    val salutLines = remember {
        List(24) {
            val angle = it * 15f // Угол в градусах (360 / 24 = 15)
            val radians = Math.toRadians(angle.toDouble()).toFloat()
            val color = colors[it % colors.size]
            val speedFactor = Random.nextFloat() * 0.4f + 0.8f // Разная скорость распространения (0.8-1.2)
            SalutLine(
                angle = radians,
                length = Random.nextFloat() * 150f + 150f, // Длина линии от 150 до 300
                color = color,
                particleSize = Random.nextFloat() * 6f + 6f, // Размер частиц от 6 до 12
                speedFactor = speedFactor,
                // Случайное смещение для плавного появления частиц
                startOffset = Random.nextFloat() * 0.1f
            )
        }
    }
    
    // Добавляем второй взрыв с меньшим количеством линий
    val secondSalutLines = remember {
        List(16) {
            val angle = it * 22.5f + 7.5f // Смещаем относительно первого салюта
            val radians = Math.toRadians(angle.toDouble()).toFloat()
            val color = colors[(it + 2) % colors.size]
            val speedFactor = Random.nextFloat() * 0.3f + 0.7f // Разная скорость (0.7-1.0)
            SalutLine(
                angle = radians,
                length = Random.nextFloat() * 100f + 120f, // Чуть короче первого
                color = color,
                particleSize = Random.nextFloat() * 5f + 5f, // Размер частиц от 5 до 10
                speedFactor = speedFactor,
                startOffset = 0.15f + Random.nextFloat() * 0.1f // Начинается чуть позже первого
            )
        }
    }
    
    // Состояние анимации
    var animationProgress by remember { mutableStateOf(0f) }
    val animationDuration = 2000 // 2 секунды (увеличиваем длительность)
    
    // Эффект мерцания для частиц
    val sparkleEffect = remember { Animatable(0f) }
    LaunchedEffect(key1 = Unit) {
        // Быстрое мерцание для эффекта искр
        while (true) {
            sparkleEffect.animateTo(
                targetValue = 1f,
                animationSpec = tween(500, easing = LinearEasing)
            )
            sparkleEffect.animateTo(
                targetValue = 0.7f,
                animationSpec = tween(500, easing = LinearEasing)
            )
        }
    }
    
    // Запускаем анимацию
    LaunchedEffect(key1 = Unit) {
        animate(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = animationDuration,
                easing = LinearEasing
            )
        ) { value, _ ->
            animationProgress = value
        }
    }
    
    // Рисуем салют
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .onGloballyPositioned { coordinates ->
                // Получаем центр canvas
                centerX = coordinates.size.width / 2f
                centerY = coordinates.size.height / 2f
            }
    ) {
        // Если анимация только началась, рисуем стартовую ракету
        if (animationProgress < 0.25f) {
            val rocketY = size.height - (size.height * animationProgress * 4)
            
            // Рисуем ракету
            drawCircle(
                color = Color.White,
                radius = 6f,
                center = Offset(centerX, rocketY),
                alpha = (1f - (animationProgress * 4)).coerceIn(0f, 1f)
            )
            
            // Рисуем искры за ракетой
            val sparkCount = 3
            for (i in 0 until sparkCount) {
                val sparkOffset = 5 + i * 8
                val sparkRadius = 3f - i * 0.8f
                val sparkAlpha = (0.8f - i * 0.2f - animationProgress * 4).coerceIn(0f, 1f)
                
                drawCircle(
                    color = Color.Yellow,
                    radius = sparkRadius,
                    center = Offset(centerX, rocketY + sparkOffset),
                    alpha = sparkAlpha
                )
            }
            
            // Рисуем яркий след
            drawLine(
                color = Color.White,
                start = Offset(centerX, rocketY + 5),
                end = Offset(centerX, rocketY + 40),
                strokeWidth = 3f,
                alpha = (0.7f - (animationProgress * 4)).coerceIn(0f, 1f)
            )
        } 
        // Когда ракета достигает верха, начинаем рисовать взрыв
        else {
            val explodeProgress = (animationProgress - 0.25f) / 0.75f // Нормализуем прогресс
            
            // Рисуем яркое ядро взрыва
            if (explodeProgress < 0.3f) {
                val coreSize = 40f * (explodeProgress * 3f).coerceIn(0f, 1f)
                val coreAlpha = (1f - explodeProgress * 3f).coerceIn(0f, 1f)
                
                // Внутреннее свечение - белый центр
                drawCircle(
                    color = Color.White,
                    radius = coreSize * 0.5f,
                    center = Offset(centerX, centerY),
                    alpha = coreAlpha
                )
                
                // Внешний ореол - желтое свечение
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.Yellow.copy(alpha = coreAlpha),
                            Color.Yellow.copy(alpha = 0f)
                        ),
                        center = Offset(centerX, centerY),
                        radius = coreSize
                    ),
                    radius = coreSize,
                    center = Offset(centerX, centerY)
                )
            }
            
            // Рисуем линии салюта от центра
            val allLines = salutLines + secondSalutLines
            allLines.forEach { line ->
                // Учитываем смещение начала для каждой линии
                val lineProgress = ((explodeProgress - line.startOffset) / (1f - line.startOffset)).coerceIn(0f, 1f)
                
                // Длина линии зависит от прогресса анимации и скорости
                val currentLength = line.length * lineProgress * line.speedFactor
                
                // Если линия еще не должна появиться, пропускаем
                if (lineProgress <= 0) return@forEach
                
                // Вычисляем прозрачность (уменьшается ближе к концу анимации)
                val lineAlpha = ((1f - lineProgress) * 0.8f + 0.2f).coerceIn(0f, 1f)
                
                // Рисуем частицы вдоль линии
                val steps = 8 // Больше частиц (было 6)
                for (i in 0..steps) {
                    val ratio = i.toFloat() / steps
                    val x = centerX + cos(line.angle) * currentLength * ratio
                    val y = centerY + sin(line.angle) * currentLength * ratio
                    
                    // Размер частицы зависит от позиции на линии
                    val baseSizeFactor = 1f - ratio * 0.6f  // Уменьшается к концу
                    val timeFactor = 1f - lineProgress * 0.3f  // Уменьшается со временем
                    
                    // Добавляем пульсацию размеру частиц для эффекта мерцания
                    val pulseEffect = if (i % 2 == 0) sparkleEffect.value else 1f - (sparkleEffect.value - 0.7f) / 0.3f
                    val particleSize = line.particleSize * baseSizeFactor * timeFactor * pulseEffect
                    
                    // Варьируем прозрачность для эффекта мерцания
                    val particleAlpha = (lineAlpha * (1f - ratio * 0.4f) * 
                            (if (i % 3 == 0) sparkleEffect.value else 1f)).coerceIn(0f, 1f)
                    
                    // Рисуем частицу
                    drawCircle(
                        color = line.color,
                        radius = particleSize,
                        center = Offset(x, y),
                        alpha = particleAlpha
                    )
                    
                    // Для части частиц добавляем мерцающее свечение
                    if (i % 3 == 0 && ratio < 0.7f) {
                        drawCircle(
                            color = Color.White,
                            radius = particleSize * 0.5f,
                            center = Offset(x, y),
                            alpha = (particleAlpha * 0.7f * sparkleEffect.value).coerceIn(0f, 1f)
                        )
                    }
                }
            }
        }
    }
    
    // Повторяем анимацию по окончании
    LaunchedEffect(key1 = animationProgress) {
        if (animationProgress >= 0.99f) {
            delay(800) // Увеличиваем паузу между взрывами
            animationProgress = 0f
        }
    }
}

// Улучшенная версия класса для хранения данных линии салюта
private data class SalutLine(
    val angle: Float,   // Угол в радианах
    val length: Float,  // Максимальная длина линии
    val color: Color,   // Цвет линии
    val particleSize: Float, // Размер частиц
    val speedFactor: Float = 1f, // Коэффициент скорости распространения
    val startOffset: Float = 0f  // Смещение по времени начала (0-1)
) 