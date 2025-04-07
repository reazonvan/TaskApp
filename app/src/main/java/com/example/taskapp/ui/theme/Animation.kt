package com.example.taskapp.ui.theme

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp

// Анимации для карточек задач
object TaskAnimations {
    val cardElevation = 4.dp
    val cardScale = 1.02f
    
    @Composable
    fun getCardAnimation() = updateTransition(
        targetState = true,
        label = "card"
    ).animateFloat(
        label = "scale",
        transitionSpec = {
            spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        }
    ) { targetState -> if (targetState) cardScale else 1f }
}

// Анимации для переходов между экранами
object NavigationAnimations {
    val duration = 300
    
    val slideInSpec: FiniteAnimationSpec<IntOffset> = tween(durationMillis = duration)
    val slideOutSpec: FiniteAnimationSpec<IntOffset> = tween(durationMillis = duration)
    
    val slideIn = slideInHorizontally(
        initialOffsetX = { fullWidth -> fullWidth },
        animationSpec = slideInSpec
    )
    
    val slideOut = slideOutHorizontally(
        targetOffsetX = { fullWidth -> -fullWidth },
        animationSpec = slideOutSpec
    )
}

// Анимации для кнопок
object ButtonAnimations {
    val scale = 0.95f
    val duration = 100
    
    @Composable
    fun getButtonAnimation() = scaleIn(
        initialScale = 1f,
        animationSpec = tween(durationMillis = duration)
    )
}

// Анимации для списков
object ListAnimations {
    val staggerDelay = 50
    
    @Composable
    fun getListItemAnimation(index: Int): EnterTransition {
        return fadeIn(
            animationSpec = tween(
                durationMillis = 300,
                delayMillis = index * staggerDelay
            )
        ) + slideInVertically(
            initialOffsetY = { fullHeight -> fullHeight / 2 },
            animationSpec = tween(
                durationMillis = 300,
                delayMillis = index * staggerDelay
            )
        )
    }
} 