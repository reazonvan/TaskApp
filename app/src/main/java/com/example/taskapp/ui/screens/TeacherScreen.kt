package com.example.taskapp.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.taskapp.data.model.Teacher
import com.example.taskapp.ui.components.LoadingAnimation
import com.example.taskapp.ui.viewmodels.TeachersViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Временная заглушка для успешной компиляции
// Будет заменена на полноценную реализацию с TeachersViewModel позже
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherScreen(
    onTeacherClick: (Long) -> Unit
) {
    // Демо-данные преподавателей
    val teachers = remember {
        mutableStateListOf(
            Teacher(id = 1L, name = "Петров И.Д.", subject = "Математика", displayNameFirst = false),
            Teacher(id = 2L, name = "Сидорова А.В.", subject = "Физика", displayNameFirst = true),
            Teacher(id = 3L, name = "Иванов С.М.", subject = "Информатика", displayNameFirst = false)
        )
    }
    
    // Стейт для диалога добавления преподавателя
    var showAddDialog by remember { mutableStateOf(false) }
    var newTeacherName by remember { mutableStateOf("") }
    var newTeacherSubject by remember { mutableStateOf("") }
    var displaySubjectFirst by remember { mutableStateOf(false) }
    
    // UI код для простого отображения списка преподавателей
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Мои преподаватели") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                modifier = Modifier.statusBarsPadding()
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = "Добавить преподавателя"
                )
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            items(teachers) { teacher ->
                TeacherItemWithBadge(
                    teacher = teacher,
                    onTeacherClick = { onTeacherClick(teacher.id) },
                    uncompletedTasksCount = 0
                )
            }
        }
    }
}

/* Закомментировано для избежания конфликта с функцией из TeachersScreen.kt
@Composable
fun TeacherItem(
    teacher: Teacher,
    onTeacherClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable(onClick = onTeacherClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Text(
            text = teacher.name,
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.titleMedium
        )
    }
}
*/

// Переименовываем для избежания конфликта
@Composable
fun TeacherItemWithBadge(
    teacher: Teacher,
    onTeacherClick: () -> Unit,
    uncompletedTasksCount: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable(onClick = onTeacherClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = teacher.name,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleMedium
            )
            
            Badge {
                Text(text = uncompletedTasksCount.toString())
            }
        }
    }
}

@Composable
fun EmptyTeachersMessage() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Нет преподавателей. Добавьте нового преподавателя.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
    }
} 