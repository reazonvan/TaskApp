package com.example.taskapp.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp

// Константы для анимаций
private object TaskCardAnimations {
    const val cardScale = 0.95f
    val cardElevation = 4.dp
}

@Composable
fun ModernTaskCard(
    title: String,
    description: String,
    isCompleted: Boolean,
    onTaskClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) TaskCardAnimations.cardScale else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )
    
    // Анимация перехода состояния задачи
    val completedProgress by animateFloatAsState(
        targetValue = if (isCompleted) 1f else 0f,
        animationSpec = tween(durationMillis = 300)
    )
    
    val cardBackground = lerp(
        MaterialTheme.colorScheme.surface,
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
        completedProgress
    )
    
    val textColor = lerp(
        MaterialTheme.colorScheme.onSurface,
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
        completedProgress
    )

    Card(
        modifier = modifier
            .scale(scale)
            .clickable {
                isPressed = true
                onTaskClick()
            },
        colors = CardDefaults.cardColors(
            containerColor = cardBackground
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = TaskCardAnimations.cardElevation
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = textColor,
                    textDecoration = if (isCompleted) TextDecoration.LineThrough else TextDecoration.None
                )
                
                Box {
                    // Анимация переключения иконки
                    Icon(
                        imageVector = if (isCompleted) Icons.Filled.CheckCircle else Icons.Outlined.CheckCircle,
                        contentDescription = "Статус задачи",
                        tint = if (isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
            
            Text(
                text = description,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                    alpha = 1f - (completedProgress * 0.3f)
                ),
                textDecoration = if (isCompleted) TextDecoration.LineThrough else TextDecoration.None
            )
        }
    }
}

// Линейная интерполяция между двумя цветами
private fun lerp(start: Color, stop: Color, fraction: Float): Color {
    return Color(
        red = start.red + (stop.red - start.red) * fraction,
        green = start.green + (stop.green - start.green) * fraction,
        blue = start.blue + (stop.blue - start.blue) * fraction,
        alpha = start.alpha + (stop.alpha - start.alpha) * fraction
    )
} 