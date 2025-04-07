package com.example.taskapp.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.DismissDirection
import androidx.compose.material3.DismissValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeToDeleteTask(
    onDelete: () -> Unit,
    content: @Composable () -> Unit
) {
    var isRemoved by remember { mutableStateOf(false) }
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                isRemoved = true
                true
            } else {
                false
            }
        }
    )

    LaunchedEffect(key1 = isRemoved) {
        if (isRemoved) {
            delay(300) // Задержка для анимации
            onDelete()
        }
    }

    AnimatedVisibility(
        visible = !isRemoved,
        exit = fadeOut(
            animationSpec = tween(durationMillis = 300)
        ) + shrinkHorizontally(
            animationSpec = tween(durationMillis = 300),
            shrinkTowards = Alignment.Start
        )
    ) {
        SwipeToDismissBox(
            state = dismissState,
            backgroundContent = {
                DeleteBackground(dismissState)
            },
            content = {
                content()
            },
            enableDismissFromStartToEnd = false,
            enableDismissFromEndToStart = true
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeleteBackground(dismissState: SwipeToDismissBoxState) {
    val color = when (dismissState.currentValue) {
        SwipeToDismissBoxValue.Settled -> Color.Transparent
        SwipeToDismissBoxValue.StartToEnd -> Color(0xFF4CAF50)
        SwipeToDismissBoxValue.EndToStart -> Color(0xFFFF5252)
    }

    // Определяем выравнивание на основе текущего значения
    val alignment = when (dismissState.currentValue) {
        SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
        SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
        SwipeToDismissBoxValue.Settled -> Alignment.Center
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color)
            .padding(horizontal = 20.dp),
        contentAlignment = alignment
    ) {
        Icon(
            imageVector = Icons.Default.Delete,
            contentDescription = "Удалить",
            tint = Color.White
        )
    }
} 