package com.example.taskapp.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Кнопка настроек с анимацией вращения
 */
@Composable
fun SettingsButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean = false
) {
    // Анимация цветов
    val backgroundColor by animateColorAsState(
        targetValue = if (isDarkTheme) {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
        } else {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        },
        animationSpec = tween(durationMillis = 500),
        label = "backgroundColor"
    )
    
    val iconTint by animateColorAsState(
        targetValue = if (isDarkTheme) {
            Color(0xFFBDBDBD) // Светло-серый для темной темы
        } else {
            Color(0xFF455A64) // Темно-синий для светлой темы
        },
        animationSpec = tween(durationMillis = 500),
        label = "iconTint"
    )
    
    // Постоянное медленное вращение
    val infiniteTransition = rememberInfiniteTransition(label = "infiniteTransition")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 15000,
                easing = FastOutSlowInEasing
            )
        ),
        label = "rotation"
    )
    
    // Масштабирование при нажатии
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 200),
        label = "scale"
    )
    
    Box(
        modifier = modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                onClick()
            }
            .scale(scale)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Settings,
            contentDescription = "Настройки",
            tint = iconTint,
            modifier = Modifier
                .size(24.dp)
                .rotate(rotation)
        )
    }
} 