package com.example.taskapp.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.BlendMode
import kotlin.math.*
import kotlin.random.Random
import com.example.taskapp.ui.theme.GradientColors

@Composable
fun AnimatedBackground(
    modifier: Modifier = Modifier,
    particleCount: Int = 50
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    
    // Анимация волн
    val infiniteTransition = rememberInfiniteTransition(label = "waves")
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_phase"
    )

    // Состояние частиц
    class Particle(
        var x: Float,
        var y: Float,
        var speed: Float,
        var size: Float,
        var alpha: Float
    )

    val particles = remember {
        List(particleCount) {
            Particle(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                speed = Random.nextFloat() * 0.02f + 0.01f,
                size = Random.nextFloat() * 8f + 4f,
                alpha = Random.nextFloat() * 0.5f + 0.1f
            )
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        // Градиентный фон
        val gradientBrush = Brush.verticalGradient(
            colors = if (isDark) {
                listOf(GradientColors.darkStart, GradientColors.darkEnd)
            } else {
                listOf(GradientColors.lightStart, GradientColors.lightEnd)
            }
        )
        drawRect(brush = gradientBrush)

        // Рисуем волны
        for (i in 0..2) {
            val phase = wavePhase + i * PI.toFloat() / 3f
            drawWave(
                phase = phase,
                amplitude = size.height * 0.05f,
                color = if (isDark) {
                    GradientColors.accentDark.copy(alpha = 0.1f)
                } else {
                    GradientColors.accentLight.copy(alpha = 0.1f)
                }
            )
        }

        // Рисуем частицы
        particles.forEach { particle ->
            // Обновляем позицию частицы
            particle.y -= particle.speed
            if (particle.y < -0.1f) {
                particle.y = 1.1f
                particle.x = Random.nextFloat()
            }

            drawCircle(
                color = if (isDark) {
                    GradientColors.accentDark.copy(alpha = particle.alpha)
                } else {
                    GradientColors.accentLight.copy(alpha = particle.alpha)
                },
                radius = particle.size,
                center = Offset(
                    particle.x * size.width,
                    particle.y * size.height
                ),
                blendMode = BlendMode.Plus
            )
        }
    }
}

private fun DrawScope.drawWave(
    phase: Float,
    amplitude: Float,
    color: Color
) {
    val path = Path()
    val width = size.width
    val height = size.height
    val wavelength = width * 0.5f

    path.moveTo(0f, height)
    var x = 0f
    while (x <= width + wavelength) {
        val y = height * 0.5f + amplitude * sin(2f * PI.toFloat() * (x / wavelength) + phase)
        path.lineTo(x, y)
        x += 5f
    }
    path.lineTo(width, height)
    path.close()

    drawPath(
        path = path,
        color = color,
        style = Fill
    )
} 