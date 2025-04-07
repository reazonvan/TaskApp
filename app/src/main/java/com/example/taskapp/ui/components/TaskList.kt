package com.example.taskapp.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.taskapp.data.model.Task
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically

@Composable
fun TaskList(
    tasks: List<Task>,
    onTaskClick: (Task) -> Unit,
    onTaskDelete: (Task) -> Unit = {},
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        itemsIndexed(
            items = tasks,
            key = { _, task -> task.id }
        ) { index, task ->
            // Создаем анимацию появления элемента списка
            val enterAnimation = fadeIn(animationSpec = tween(durationMillis = 300)) + 
                    slideInVertically(
                        animationSpec = tween(
                            durationMillis = 300,
                            delayMillis = index * 50
                        ),
                        initialOffsetY = { it / 2 }
                    )
                    
            AnimatedVisibility(
                visible = true,
                enter = enterAnimation
            ) {
                SwipeToDeleteTask(
                    onDelete = { onTaskDelete(task) }
                ) {
                    ModernTaskCard(
                        title = task.title,
                        description = task.description,
                        isCompleted = task.isCompleted,
                        onTaskClick = { onTaskClick(task) }
                    )
                }
            }
        }
    }
}

data class Task(
    val id: String,
    val title: String,
    val description: String,
    val isCompleted: Boolean
) 