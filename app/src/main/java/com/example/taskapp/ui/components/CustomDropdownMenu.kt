package com.example.taskapp.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import kotlinx.coroutines.delay

/**
 * Адаптивное выпадающее меню с равномерными отступами
 */
@Composable
fun CustomDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val transformOrigin = remember { TransformOrigin(0.5f, 0f) }
    
    // Анимация для появления и исчезновения меню
    val transition = updateTransition(targetState = expanded, label = "dropdown_transition")
    
    // Анимация масштаба для эффекта появления
    val scale by transition.animateFloat(
        transitionSpec = {
            if (targetState) {
                spring(dampingRatio = Spring.DampingRatioMediumBouncy)
            } else {
                spring(dampingRatio = Spring.DampingRatioNoBouncy)
            }
        },
        label = "scale"
    ) { state -> if (state) 1f else 0.85f }
    
    // Анимация прозрачности для плавного появления/исчезновения
    val alpha by transition.animateFloat(
        transitionSpec = { tween(150) },
        label = "alpha"
    ) { state -> if (state) 1f else 0f }
    
    // Анимация тени для эффекта подъема
    val elevation by transition.animateFloat(
        transitionSpec = { tween(150) },
        label = "elevation"
    ) { state -> if (state) 8f else 0f }
    
    if (transition.currentState || transition.targetState) {
        Popup(
            onDismissRequest = onDismissRequest,
            properties = PopupProperties(
                focusable = true,
                dismissOnBackPress = true,
                dismissOnClickOutside = true
            )
        ) {
            Box(
                modifier = modifier
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        this.alpha = alpha
                        this.transformOrigin = transformOrigin
                        this.shadowElevation = elevation
                    }
            ) {
                // Адаптивное меню с равномерными отступами
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(8.dp),
                    shadowElevation = 4.dp,
                    tonalElevation = 2.dp,
                    modifier = Modifier.wrapContentSize()
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        content()
                    }
                }
            }
        }
    }
}

/**
 * Адаптивный элемент выпадающего меню с равномерными отступами
 */
@Composable
fun CustomDropdownMenuItem(
    text: String,
    onClick: () -> Unit,
    leadingIcon: ImageVector? = null,
    leadingIconTint: Color = MaterialTheme.colorScheme.primary,
    trailingContent: @Composable (() -> Unit)? = null,
    selected: Boolean = false
) {
    val interactionSource = remember { MutableInteractionSource() }
    
    // Анимация выбранного пункта
    val backgroundColor = if (selected) 
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
    else 
        Color.Transparent
    
    // Анимация нажатия
    var isPressed by remember { mutableStateOf(false) }
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1.0f,
        animationSpec = tween(100),
        label = "press_scale"
    )

    Surface(
        modifier = Modifier
            .wrapContentWidth()
            .heightIn(min = 36.dp)
            .padding(vertical = 2.dp)
            .graphicsLayer {
                scaleX = pressScale
                scaleY = pressScale
            },
        color = backgroundColor,
        shape = RoundedCornerShape(4.dp),
        onClick = { 
            isPressed = true
            onClick() 
        },
        interactionSource = interactionSource
    ) {
        Row(
            modifier = Modifier
                .defaultMinSize(minWidth = 100.dp)
                .wrapContentWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Иконка
            if (leadingIcon != null) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = leadingIconTint,
                    modifier = Modifier.size(18.dp)
                )
            }
            
            // Текст
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal
                ),
                color = if (selected) 
                    MaterialTheme.colorScheme.primary 
                else 
                    MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f, fill = false)
            )
            
            // Дополнительный контент (справа)
            if (trailingContent != null) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    trailingContent()
                }
            }
        }
    }
} 