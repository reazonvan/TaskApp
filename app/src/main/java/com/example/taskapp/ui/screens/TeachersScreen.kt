package com.example.taskapp.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.taskapp.data.model.Teacher
import com.example.taskapp.ui.components.AnimatedBackground
import com.example.taskapp.ui.components.InteractiveBackground
import com.example.taskapp.ui.theme.ParticleColors
import com.example.taskapp.ui.theme.TaskStatusColors
import com.example.taskapp.ui.viewmodels.TeachersViewModel
import com.example.taskapp.util.rememberSoundEffectHelper
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlin.random.Random
import kotlin.math.cos
import kotlin.math.sin
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.material3.Surface
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.focusable
import androidx.compose.ui.focus.FocusState
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import com.example.taskapp.ui.components.ThemeToggleButton
import com.example.taskapp.ui.components.SettingsButton
import com.example.taskapp.ui.components.CustomDropdownMenu
import com.example.taskapp.ui.components.CustomDropdownMenuItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeachersScreen(
    onNavigateToTeacher: (Long) -> Unit,
    onNavigateToSettings: () -> Unit,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    viewModel: TeachersViewModel = hiltViewModel()
) {
    val teachers by viewModel.teachers.collectAsState(initial = emptyList())
    val uncompletedTasksCountMap by viewModel.uncompletedTasksCount.collectAsState(initial = emptyMap())
    var showAddDialog by remember { mutableStateOf(false) }
    var newTeacherName by remember { mutableStateOf("") }
    
    // Обновляем данные каждые 2 секунды для актуализации счетчика
    LaunchedEffect(Unit) {
        while(true) {
            viewModel.refreshData()
            delay(2000)
        }
    }
    
    // Принудительно обновляем счетчики при изменении списка преподавателей
    LaunchedEffect(teachers) {
        viewModel.refreshData()
    }
    
    // Поиск и фильтрация
    var searchQuery by remember { mutableStateOf("") }
    var showOnlyWithTasks by remember { mutableStateOf(false) }
    
    // Фильтрация преподавателей
    val filteredTeachers = remember(teachers, searchQuery, showOnlyWithTasks, uncompletedTasksCountMap) {
        teachers.filter { teacher ->
            val matchesSearch = teacher.name.contains(searchQuery, ignoreCase = true) || 
                                teacher.subject.contains(searchQuery, ignoreCase = true)
            val hasUncompletedTasks = uncompletedTasksCountMap[teacher.id]?.let { it > 0 } ?: false
            
            matchesSearch && (!showOnlyWithTasks || hasUncompletedTasks)
        }
    }
    
    // Подсчет несданных работ
    val totalUncompletedTasks = uncompletedTasksCountMap.values.sum()
    val isAllTasksCompleted = totalUncompletedTasks == 0
    
    Box(modifier = Modifier.fillMaxSize()) {
        // Используем новый интерактивный фон вместо ThematicBackground
        InteractiveBackground(
            modifier = Modifier.fillMaxSize(),
            isDarkTheme = isDarkTheme,
            uncompletedTasksCount = totalUncompletedTasks,
            isAllTasksCompleted = isAllTasksCompleted,
            pulseStrength = if (isAllTasksCompleted) 1.5f else 1f // Увеличиваем интенсивность пульсации для выполненных задач
        )
        
        // Основной контент
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .background(Color.Transparent)
                .padding(horizontal = 16.dp)
                .padding(top = 16.dp)
        ) {
            // Верхняя панель с кнопками настроек и переключения темы
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Кнопка настроек слева
                SettingsButton(
                    onClick = onNavigateToSettings,
                    isDarkTheme = isDarkTheme
                )
                
                // Заголовок по центру
                Text(
                    text = "Преподаватели",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                
                // Кнопка переключения темы справа
                ThemeToggleButton(
                    isDarkTheme = isDarkTheme,
                    onToggleTheme = onToggleTheme
                )
            }
            
            // Блок статистики
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Цифра с количеством несданных работ
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = totalUncompletedTasks.toString(),
                        style = MaterialTheme.typography.displayLarge,
                        color = if (totalUncompletedTasks > 0) 
                            MaterialTheme.colorScheme.error 
                        else 
                            MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = if (totalUncompletedTasks == 0) 
                        "Все работы сданы" 
                    else 
                        "Несданных работ",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Поисковая строка
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    placeholder = { Text("Поиск преподавателей") },
                    leadingIcon = { 
                        Icon(
                            imageVector = Icons.Default.Search, 
                            contentDescription = "Поиск",
                            tint = MaterialTheme.colorScheme.primary
                        ) 
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        cursorColor = MaterialTheme.colorScheme.primary,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                    )
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                // Переключатель фильтра
                Switch(
                    checked = showOnlyWithTasks,
                    onCheckedChange = { showOnlyWithTasks = it },
                    thumbContent = if (showOnlyWithTasks) {
                        {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(SwitchDefaults.IconSize),
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    } else null
                )
                
                Text(
                    text = "С долгами",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Разделитель
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                thickness = 1.dp
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Заголовок списка
            if (teachers.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Преподаватели",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    
                    Text(
                        text = "${filteredTeachers.size} из ${teachers.size}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                }
            }
            
            // Списки
            if (teachers.isEmpty()) {
                // Пустое состояние
                EmptyState(
                    icon = Icons.Default.Add,
                    title = "Добавьте преподавателей",
                    subtitle = "Нажмите на кнопку '+' чтобы начать"
                )
            } else if (filteredTeachers.isEmpty()) {
                // Пустой результат поиска
                EmptyState(
                    icon = Icons.Default.Search,
                    title = "Ничего не найдено",
                    subtitle = if (searchQuery.isNotEmpty()) 
                        "Попробуйте другой запрос" 
                    else 
                        "Нет преподавателей с долгами"
                )
            } else {
                // Список преподавателей
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(
                        items = filteredTeachers,
                        key = { it.id }
                    ) { teacher ->
                        TeacherCard(
                            teacher = teacher,
                            uncompletedTasksCount = uncompletedTasksCountMap[teacher.id] ?: 0,
                            onClick = { onNavigateToTeacher(teacher.id) },
                            onDelete = { viewModel.deleteTeacher(teacher) },
                            onSwapNameSubject = { viewModel.swapTeacherNameSubject(teacher) }
                        )
                    }
                    
                    // Отступ внизу списка
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
        
        // FAB для добавления
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
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
            
            // Подсветка за кнопкой
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(48.dp)
                    .graphicsLayer {
                        scaleX = pulsationScale.value
                        scaleY = pulsationScale.value
                        alpha = 0.6f
                    }
            ) {
                // Эффект свечения
                val primary = MaterialTheme.colorScheme.primary
                Canvas(modifier = Modifier.size(48.dp)) {
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
        SmallFloatingActionButton(
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
                    showAddDialog = true 
                },
                containerColor = Color.Transparent, // Прозрачный контейнер для градиента
            contentColor = MaterialTheme.colorScheme.onPrimary,
            shape = CircleShape,
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
                    .size(40.dp)
                    .graphicsLayer {
                        scaleX = buttonScale.value
                        scaleY = buttonScale.value
                    }
            ) {
                // Улучшенная иконка
            Icon(
                imageVector = Icons.Default.Add,
                    contentDescription = "Добавить преподавателя",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(20.dp)
            )
            }
        }
        
        // Диалог добавления преподавателя
        if (showAddDialog) {
            var newTeacherSubject by remember { mutableStateOf("") }
            var displayNameFirst by remember { mutableStateOf(true) }
            val coroutineScope = rememberCoroutineScope()
            
            // Анимация для диалога
            var isDialogVisible by remember { mutableStateOf(false) }
            LaunchedEffect(key1 = true) {
                isDialogVisible = true
            }
            
            val dialogScale by animateFloatAsState(
                targetValue = if (isDialogVisible) 1f else 0.8f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
            
            val dialogAlpha by animateFloatAsState(
                targetValue = if (isDialogVisible) 1f else 0f,
                animationSpec = tween(300)
            )
            
            // Анимация для кнопок
            var isAddButtonPressed by remember { mutableStateOf(false) }
            val addButtonScale by animateFloatAsState(
                targetValue = if (isAddButtonPressed) 0.95f else 1f,
                animationSpec = tween(100)
            )
            
            var isCancelButtonPressed by remember { mutableStateOf(false) }
            val cancelButtonScale by animateFloatAsState(
                targetValue = if (isCancelButtonPressed) 0.95f else 1f,
                animationSpec = tween(100)
            )
            
            Dialog(
                onDismissRequest = { 
                    isDialogVisible = false
                    coroutineScope.launch {
                        delay(200)
                        showAddDialog = false
                    }
                },
                properties = DialogProperties(
                    dismissOnBackPress = true,
                    dismissOnClickOutside = true,
                    usePlatformDefaultWidth = false
                )
            ) {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    // Минимальная тень для эффекта "парения" над контентом
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 4.dp
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .graphicsLayer {
                            scaleX = dialogScale
                            scaleY = dialogScale
                            alpha = dialogAlpha
                        }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Заголовок с улучшенной типографикой
                        Text(
                            text = "Новый преподаватель",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        // Анимированная декоративная линия под заголовком
                        var lineWidth by remember { mutableStateOf(0.dp) }
                        LaunchedEffect(key1 = isDialogVisible) {
                            delay(100)
                            lineWidth = 120.dp
                        }
                        Box(
                            modifier = Modifier
                                .padding(bottom = 16.dp)
                                .width(lineWidth)
                                .animateContentSize(
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessLow
                                    )
                                )
                                .height(2.dp)
                                .background(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                            MaterialTheme.colorScheme.primary,
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                                        )
                                    ),
                                    shape = RoundedCornerShape(1.dp)
                                )
                        )
                        
                        // Поле для ФИО преподавателя с анимацией фокуса
                        var isFocused by remember { mutableStateOf(false) }
                        val fieldScale by animateFloatAsState(
                            targetValue = if (isFocused) 1.02f else 1f,
                            animationSpec = tween(200)
                        )
                        OutlinedTextField(
                            value = newTeacherName,
                            onValueChange = { newTeacherName = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                                .graphicsLayer {
                                    scaleX = fieldScale
                                    scaleY = fieldScale
                                }
                                .onFocusChanged { focusState ->
                                    isFocused = focusState.isFocused
                                },
                            placeholder = { 
                                Text(
                                    "ФИО преподавателя",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    )
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            textStyle = MaterialTheme.typography.bodyLarge,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                                cursorColor = MaterialTheme.colorScheme.primary,
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface
                            ),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                        
                        // Поле для предмета с анимацией фокуса
                        var isSubjectFocused by remember { mutableStateOf(false) }
                        val subjectFieldScale by animateFloatAsState(
                            targetValue = if (isSubjectFocused) 1.02f else 1f,
                            animationSpec = tween(200)
                        )
                        OutlinedTextField(
                            value = newTeacherSubject,
                            onValueChange = { newTeacherSubject = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 20.dp)
                                .graphicsLayer {
                                    scaleX = subjectFieldScale
                                    scaleY = subjectFieldScale
                                }
                                .onFocusChanged { focusState -> 
                                    isSubjectFocused = focusState.isFocused
                                },
                            placeholder = { 
                                Text(
                                    "Предмет",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    )
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.School,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            textStyle = MaterialTheme.typography.bodyLarge,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                                cursorColor = MaterialTheme.colorScheme.primary,
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface
                            ),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                        
                        // Текст "Что отображать первым"
                            Text(
                                text = "Что отображать первым:",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier
                                .align(Alignment.Start)
                                .padding(bottom = 12.dp)
                        )
                        
                        // Вариант "ФИО преподавателя" - с анимацией выбора
                        val nameFirstContainerColor by animateColorAsState(
                            targetValue = if (displayNameFirst) 
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f) 
                            else 
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            animationSpec = tween(300),
                            label = "name_first_color"
                            )
                            
                            Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(nameFirstContainerColor)
                                .clickable { 
                                    if (!displayNameFirst) {
                                        // Создаем эффект пружины при переключении
                                        coroutineScope.launch {
                                            val anim = Animatable(1f)
                                            anim.animateTo(
                                                targetValue = 0.95f,
                                                animationSpec = tween(100)
                                            )
                                            anim.animateTo(
                                                targetValue = 1f,
                                                animationSpec = spring(
                                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                                    stiffness = Spring.StiffnessLow
                                                )
                                            )
                                        }
                                    }
                                    displayNameFirst = true 
                                }
                                .padding(vertical = 12.dp, horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                            // Анимированная радиокнопка
                            val radioScale by animateFloatAsState(
                                targetValue = if (displayNameFirst) 1.2f else 1f,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessLow
                                ),
                                label = "radio_scale"
                            )
                            
                            Box(modifier = Modifier.size(24.dp)) {
                                RadioButton(
                                    selected = displayNameFirst,
                                    onClick = { displayNameFirst = true },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = MaterialTheme.colorScheme.primary,
                                        unselectedColor = MaterialTheme.colorScheme.outline
                                    ),
                                    modifier = Modifier
                                        .size(24.dp)
                                        .graphicsLayer {
                                            scaleX = radioScale
                                            scaleY = radioScale
                                        }
                                )
                            }
                            
                                Text(
                                    text = "ФИО преподавателя",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = if (displayNameFirst) FontWeight.Medium else FontWeight.Normal
                                ),
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(start = 12.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Вариант "Название предмета" - с анимацией выбора
                        val subjectFirstContainerColor by animateColorAsState(
                            targetValue = if (!displayNameFirst) 
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f) 
                            else 
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            animationSpec = tween(300),
                            label = "subject_first_color"
                        )
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(subjectFirstContainerColor)
                                .clickable { 
                                    if (displayNameFirst) {
                                        // Создаем эффект пружины при переключении
                                        coroutineScope.launch {
                                            val anim = Animatable(1f)
                                            anim.animateTo(
                                                targetValue = 0.95f,
                                                animationSpec = tween(100)
                                            )
                                            anim.animateTo(
                                                targetValue = 1f,
                                                animationSpec = spring(
                                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                                    stiffness = Spring.StiffnessLow
                                                )
                                            )
                                        }
                                    }
                                    displayNameFirst = false 
                                }
                                .padding(vertical = 12.dp, horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                            // Анимированная радиокнопка
                            val radioSubjectScale by animateFloatAsState(
                                targetValue = if (!displayNameFirst) 1.2f else 1f,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessLow
                                ),
                                label = "radio_subject_scale"
                            )
                            
                            Box(modifier = Modifier.size(24.dp)) {
                                RadioButton(
                                    selected = !displayNameFirst,
                                    onClick = { displayNameFirst = false },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = MaterialTheme.colorScheme.primary,
                                        unselectedColor = MaterialTheme.colorScheme.outline
                                    ),
                                    modifier = Modifier
                                        .size(24.dp)
                                        .graphicsLayer {
                                            scaleX = radioSubjectScale
                                            scaleY = radioSubjectScale
                                        }
                                )
                            }
                            
                                Text(
                                    text = "Название предмета",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = if (!displayNameFirst) FontWeight.Medium else FontWeight.Normal
                                ),
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(start = 12.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // Блок с кнопками - унифицированный дизайн
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Кнопка "Отмена" - обновленный стиль
                            OutlinedButton(
                                onClick = { 
                                    isDialogVisible = false 
                                    coroutineScope.launch {
                                        delay(200)
                                        showAddDialog = false
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .graphicsLayer {
                                        scaleX = cancelButtonScale
                                        scaleY = cancelButtonScale
                                    },
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.onSurface
                                ),
                                interactionSource = remember { MutableInteractionSource() }
                                    .also { interactionSource ->
                                        LaunchedEffect(interactionSource) {
                                            interactionSource.interactions.collect { interaction ->
                                                when (interaction) {
                                                    is PressInteraction.Press -> {
                                                        isCancelButtonPressed = true
                                                    }
                                                    is PressInteraction.Release, is PressInteraction.Cancel -> {
                                                        isCancelButtonPressed = false
                                                    }
                                                }
                                            }
                                        }
                                    }
                            ) {
                                Text(
                                    "Отмена",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                            }
                            
                            // Кнопка "Добавить" - с эффектом успешного добавления
                    Button(
                        onClick = {
                            if (newTeacherName.isNotBlank()) {
                                        // Анимируем успешное добавление
                                        coroutineScope.launch {
                                            // Визуальная обратная связь - успешное добавление
                                            val successAnim = Animatable(1f)
                                            launch {
                                                successAnim.animateTo(
                                                    targetValue = 1.15f,
                                                    animationSpec = tween(200)
                                                )
                                                successAnim.animateTo(
                                                    targetValue = 1f,
                                                    animationSpec = spring(
                                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                                        stiffness = Spring.StiffnessLow
                                                    )
                                                )
                                            }
                                        }
                                        
                                        // Добавляем преподавателя
                                viewModel.addTeacher(
                                    name = newTeacherName, 
                                    subject = newTeacherSubject, 
                                    displayNameFirst = displayNameFirst
                                )
                                newTeacherName = ""
                                        isDialogVisible = false
                                        coroutineScope.launch {
                                            delay(200)
                                showAddDialog = false
                            }
                                    }
                                },
                                enabled = newTeacherName.isNotBlank(),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .graphicsLayer {
                                        scaleX = addButtonScale
                                        scaleY = addButtonScale
                                    },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary,
                                    disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                                    disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                                ),
                                interactionSource = remember { MutableInteractionSource() }
                                    .also { interactionSource ->
                                        LaunchedEffect(interactionSource) {
                                            interactionSource.interactions.collect { interaction ->
                                                when (interaction) {
                                                    is PressInteraction.Press -> {
                                                        isAddButtonPressed = true
                                                    }
                                                    is PressInteraction.Release, is PressInteraction.Cancel -> {
                                                        isAddButtonPressed = false
                                                    }
                                                }
                                            }
                                        }
                                    }
                            ) {
                                Text(
                                    "Добавить",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyState(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier
                    .size(64.dp)
                    .padding(bottom = 16.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            )
            
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun TeacherCard(
    teacher: Teacher,
    uncompletedTasksCount: Int,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onSwapNameSubject: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var isSwapping by remember { mutableStateOf(false) }
    
    // Звуковые эффекты
    val soundHelper = rememberSoundEffectHelper()
    
    // Основная анимация исчезновения
    val textAlpha by animateFloatAsState(
        targetValue = if (isSwapping) 0f else 1f,
        animationSpec = tween(200, easing = FastOutLinearInEasing),
        label = "text_alpha"
    )
    
    // Анимация сдвига и масштабирования
    val textScale by animateFloatAsState(
        targetValue = if (isSwapping) 0.8f else 1f,
        animationSpec = tween(200, easing = FastOutLinearInEasing),
        label = "text_scale"
    )
    
    // Анимация вращения
    val cardRotation by animateFloatAsState(
        targetValue = if (isSwapping) 5f else 0f,
        animationSpec = tween(200, easing = FastOutLinearInEasing),
        label = "card_rotation"
    )
    
    // Анимация появления при завершении
    val isAnimationComplete = remember { mutableStateOf(false) }
    val appearanceScale by animateFloatAsState(
        targetValue = if (isAnimationComplete.value) 1f else 0.9f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "appearance_scale"
    )
    
    val appearanceAlpha by animateFloatAsState(
        targetValue = if (isAnimationComplete.value) 1f else 0f,
        animationSpec = tween(200),
        label = "appearance_alpha"
    )
    
    // Если анимация завершена, выполнить функцию обмена
    LaunchedEffect(isSwapping) {
        if (isSwapping) {
            // Проигрываем звук смены текста
            soundHelper.playSwapSound()
            isAnimationComplete.value = false
            
            // Ждем, пока текст исчезнет
            delay(200)
            
            // Обновляем данные
            onSwapNameSubject()
            
            // Запускаем анимацию появления
            delay(50)
            isAnimationComplete.value = true
            
            // Завершаем анимацию
            delay(300)
            isSwapping = false
        }
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp,
            pressedElevation = 8.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Аватар преподавателя
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                // Инициалы преподавателя
                val initials = remember(teacher.name) {
                    teacher.name.split(" ").take(2).joinToString("") { it.firstOrNull()?.toString() ?: "" }
                }
                
                Text(
                    text = initials,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Анимированный контейнер для текста
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(IntrinsicSize.Min)
            ) {
                // Эффект частиц при смене
                if (isSwapping) {
                    ParticleEffect(
                        modifier = Modifier.matchParentSize(),
                        isActive = isSwapping
                    )
                }
                
                // Анимация исчезновения
                if (!isAnimationComplete.value || !isSwapping) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .graphicsLayer {
                                alpha = textAlpha
                                scaleX = textScale
                                scaleY = textScale
                                rotationZ = cardRotation
                                // Эффект "выбрасывания" текста
                                translationX = if (isSwapping) 50f * (1f - textAlpha) else 0f
                            }
                    ) {
                        // Первая строка (имя или предмет)
                Text(
                    text = if (teacher.displayNameFirst) teacher.name else teacher.subject,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                        // Вторая строка (предмет или имя), если есть
                if ((teacher.displayNameFirst && teacher.subject.isNotBlank()) || 
                    (!teacher.displayNameFirst && teacher.name.isNotBlank())) {
                    Text(
                        text = if (teacher.displayNameFirst) teacher.subject else teacher.name,
                        style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
                
                // Анимация появления
                if (isAnimationComplete.value && isSwapping) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .graphicsLayer {
                                alpha = appearanceAlpha
                                scaleX = appearanceScale
                                scaleY = appearanceScale
                                // Эффект "влетания" текста
                                translationX = -50f * (1f - appearanceScale)
                            }
                    ) {
                        // Первая строка (имя или предмет) - обратный порядок
                        Text(
                            text = if (!teacher.displayNameFirst) teacher.name else teacher.subject,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        // Вторая строка (предмет или имя), если есть - обратный порядок
                        if ((!teacher.displayNameFirst && teacher.subject.isNotBlank()) || 
                            (teacher.displayNameFirst && teacher.name.isNotBlank())) {
                            Text(
                                text = if (!teacher.displayNameFirst) teacher.subject else teacher.name,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }
            
            if (uncompletedTasksCount > 0) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.error),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = uncompletedTasksCount.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onError
                    )
                }
            }
            
            // Кнопка с меню
            Box(
                modifier = Modifier.padding(start = 8.dp)
            ) {
                var expanded by remember { mutableStateOf(false) }
                
                IconButton(
                    onClick = { expanded = true }
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Меню",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                CustomDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    // Кнопка смены местами
                    CustomDropdownMenuItem(
                        text = "Поменять местами",
                        leadingIcon = Icons.Default.SwapVert,
                        leadingIconTint = MaterialTheme.colorScheme.primary,
                        onClick = {
                            isSwapping = true
                            expanded = false
                        }
                    )
                    
                    // Кнопка удаления
                    CustomDropdownMenuItem(
                        text = "Удалить",
                        leadingIcon = Icons.Default.Delete,
                        leadingIconTint = MaterialTheme.colorScheme.error,
                        onClick = {
                            showDeleteDialog = true
                            expanded = false
                        }
                    )
                }
            }
        }
    }
    
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Удаление преподавателя") },
            text = { 
                Text(
                    "Вы действительно хотите удалить преподавателя ${teacher.name}?" +
                    if (uncompletedTasksCount > 0) "\n\nВнимание: у этого преподавателя есть несданные работы ($uncompletedTasksCount шт)!" else ""
                ) 
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Удалить")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Отмена")
                }
            }
        )
    }
}

// Эффект частиц для анимации смены текста
@Composable
fun ParticleEffect(
    modifier: Modifier = Modifier,
    isActive: Boolean = false
) {
    val particles = remember { mutableStateListOf<SwapParticle>() }
    val density = LocalDensity.current
    
    // Получаем цвета темы вне LaunchedEffect
    val isDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val particleColors = if (isDarkTheme) {
        listOf(
            Color(0xFF7986CB),
            Color(0xFF5C6BC0),
            Color(0xFF3F51B5),
            Color(0xFF3949AB),
            Color(0xFFEC407A),
            Color(0xFFE91E63),
            Color(0xFFD81B60),
            Color(0xFFC2185B)
        )
    } else {
        listOf(
            Color(0xFF4FC3F7),
            Color(0xFF29B6F6),
            Color(0xFF03A9F4),
            Color(0xFF00BCD4),
            Color(0xFFF06292),
            Color(0xFFEC407A),
            Color(0xFFE91E63),
            Color(0xFFD81B60)
        )
    }
    
    // Функция для создания частицы на основе доступных цветов
    val createParticle = { _: Int ->
        // Разные типы частиц с разными параметрами
        val particleType = Random.nextInt(10)
        
        // Больше ярких цветов для улучшения визуального эффекта
        val particleColor = particleColors[Random.nextInt(particleColors.size)].copy(alpha = Random.nextFloat() * 0.4f + 0.6f) // Более заметные частицы
        
        // Разный размер частиц
        val sizeMultiplier = if (particleType > 8) 2.0f else 1.0f // Некоторые частицы больше
        val particleSize = with(density) { (Random.nextFloat() * 4 + 3).dp.toPx() * sizeMultiplier }
        
        // Расположение частиц - теперь в более интересном шаблоне
        val initialX = when {
            particleType < 3 -> (Random.nextFloat() * 100) - 180f // Слева
            particleType < 6 -> (Random.nextFloat() * 100) + 80f // Справа
            else -> (Random.nextFloat() * 160) - 80f // По центру
        }
        
        val initialY = when {
            particleType % 3 == 0 -> -10f + Random.nextFloat() * 20f // Сверху
            particleType % 3 == 1 -> 30f + Random.nextFloat() * 20f // Снизу
            else -> 10f + Random.nextFloat() * 20f // По центру
        }
        
        // Более разнообразные скорости для создания динамичного эффекта
        val speedMultiplier = if (particleType % 4 == 0) 1.5f else 1.0f
        val speed = (Random.nextFloat() * 2 + 1f) * speedMultiplier
        
        SwapParticle(
            initialX = initialX,
            initialY = initialY,
            color = particleColor,
            size = particleSize,
            speed = speed,
            angle = Random.nextFloat() * 360f,
            rotationSpeed = (Random.nextFloat() * 15f) - 7.5f, // Более быстрое вращение
            shape = when (particleType % 3) {
                0 -> SwapParticleShape.CIRCLE
                1 -> SwapParticleShape.SQUARE
                else -> SwapParticleShape.TRIANGLE
            }
        )
    }
    
    // Создаем частицы при активации
    LaunchedEffect(isActive) {
        if (isActive) {
            // Увеличиваем количество частиц для более впечатляющего эффекта
            for (i in 0 until 25) {
                particles.add(createParticle(i))
            }
            
            // Удаляем частицы через 800мс - дольше для лучшего эффекта
            kotlinx.coroutines.delay(800)
            particles.clear()
        }
    }
    
    // Анимация движения частиц
    val infiniteTransition = rememberInfiniteTransition(label = "particle_animation")
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing), // Увеличиваем время анимации
            repeatMode = RepeatMode.Restart
        ),
        label = "particle_progress"
    )
    
    Canvas(modifier = modifier) {
        for (particle in particles) {
            // Рассчитываем текущее положение частицы с нелинейной траекторией
            val progressCurve = if (particle.initialX < 0) {
                // Частицы слева двигаются с ускорением
                progress * progress 
            } else if (particle.initialX > 0) {
                // Частицы справа двигаются с замедлением
                1f - (1f - progress) * (1f - progress)
            } else {
                // Частицы по центру двигаются линейно
                progress
            }
            
            val x = particle.initialX + particle.speed * size.width * 0.4f * progressCurve * cos(Math.toRadians(particle.angle.toDouble())).toFloat()
            val y = particle.initialY + particle.speed * size.height * 0.4f * progressCurve * sin(Math.toRadians(particle.angle.toDouble())).toFloat()
            
            // Нелинейное исчезновение и уменьшение частиц для более плавного эффекта
            val particleScale = 1f - progressCurve * progressCurve * 0.7f
            val particleAlpha = when {
                progressCurve < 0.2f -> progressCurve * 5f // Плавное появление
                progressCurve > 0.8f -> (1f - progressCurve) * 5f // Плавное исчезновение
                else -> 1f // Полная видимость в середине
            }
            
            translate(left = x, top = y) {
                rotate(degrees = particle.rotationSpeed * 360f * progressCurve) {
                    scale(scale = particleScale) {
                        when (particle.shape) {
                            SwapParticleShape.CIRCLE -> drawCircle(
                                color = particle.color,
                                radius = particle.size / 2,
                                alpha = particleAlpha
                            )
                            SwapParticleShape.SQUARE -> drawRect(
                                color = particle.color,
                                topLeft = Offset(-particle.size / 2, -particle.size / 2),
                                size = Size(particle.size, particle.size),
                                alpha = particleAlpha
                            )
                            SwapParticleShape.TRIANGLE -> {
                                val path = Path().apply {
                                    moveTo(0f, -particle.size / 2)
                                    lineTo(particle.size / 2, particle.size / 2)
                                    lineTo(-particle.size / 2, particle.size / 2)
                                    close()
                                }
                                drawPath(
                                    path = path,
                                    color = particle.color,
                                    alpha = particleAlpha
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// Класс, описывающий частицу для анимации смены текста
data class SwapParticle(
    val initialX: Float,
    val initialY: Float,
    val color: Color,
    val size: Float,
    val speed: Float,
    val angle: Float,
    val rotationSpeed: Float,
    val shape: SwapParticleShape
)

// Типы форм частиц для анимации смены текста
enum class SwapParticleShape {
    CIRCLE, SQUARE, TRIANGLE
} 