package com.example.taskapp.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.material3.MaterialTheme
import kotlin.math.*
import kotlin.random.Random
import com.example.taskapp.ui.theme.GradientColors

// Предметы для фона
sealed class Subject {
    object Math : Subject()
    object Physics : Subject()
    object Chemistry : Subject()
    object Literature : Subject()
    object History : Subject()
}

// Состояние предмета
data class SubjectState(
    val subject: Subject,
    var x: Float,
    var y: Float,
    var rotation: Float,
    var scale: Float,
    var alpha: Float
)

@Composable
fun ThematicBackground(
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean,
    uncompletedTasksCount: Int,
    isAllTasksCompleted: Boolean
) {
    // Анимация волн
    val infiniteTransition = rememberInfiniteTransition(label = "waves")
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (isDarkTheme) 7000 else 5000,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_phase"
    )

    // Состояние предметов
    val subjects = remember {
        List(25) {
            SubjectState(
                subject = when (Random.nextInt(5)) {
                    0 -> Subject.Math
                    1 -> Subject.Physics
                    2 -> Subject.Chemistry
                    3 -> Subject.Literature
                    else -> Subject.History
                },
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                rotation = Random.nextFloat() * 360f,
                scale = Random.nextFloat() * 0.7f + 0.8f,
                alpha = Random.nextFloat() * 0.4f + 0.3f
            )
        }
    }

    // Анимация для праздничного режима
    val celebrationScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "celebration_scale"
    )

    // Анимация движения предметов
    val subjectOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "subject_movement"
    )

    // Анимация вращения предметов
    val subjectRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(30000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "subject_rotation"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        // Градиентный фон с учетом количества несданных работ
        val gradientColors = when {
            isAllTasksCompleted -> {
                if (isDarkTheme) {
                    listOf(
                        Color(0xFF1B5E20),
                        Color(0xFF2E7D32),
                        Color(0xFF388E3C)
                    )
                } else {
                    listOf(
                        Color(0xFFE8F5E9),
                        Color(0xFFC8E6C9),
                        Color(0xFF81C784)
                    )
                }
            }
            uncompletedTasksCount > 5 -> {
                if (isDarkTheme) {
                    listOf(
                        Color(0xFFB71C1C),
                        Color(0xFFC62828),
                        Color(0xFFD32F2F)
                    )
                } else {
                    listOf(
                        Color(0xFFFFEBEE),
                        Color(0xFFFFCDD2),
                        Color(0xFFEF9A9A)
                    )
                }
            }
            else -> {
                if (isDarkTheme) {
                    listOf(GradientColors.darkStart, GradientColors.darkEnd)
                } else {
                    listOf(GradientColors.lightStart, GradientColors.lightEnd)
                }
            }
        }

        val gradientBrush = Brush.verticalGradient(gradientColors)
        drawRect(brush = gradientBrush)

        // Рисуем волны с разной интенсивностью в зависимости от темы
        for (i in 0..2) {
            val phase = wavePhase + i * PI.toFloat() / 3f
            drawWave(
                phase = phase,
                amplitude = size.height * (if (isDarkTheme) 0.03f else 0.05f),
                color = if (isDarkTheme) {
                    GradientColors.accentDark.copy(alpha = 0.1f)
                } else {
                    GradientColors.accentLight.copy(alpha = 0.1f)
                }
            )
        }

        // Рисуем предметы
        subjects.forEachIndexed { index, subject ->
            // Вычисляем смещение для каждого предмета
            val offsetX = sin(subjectOffset * 2f * PI.toFloat() + index * 0.5f) * 0.05f
            val offsetY = cos(subjectOffset * 2f * PI.toFloat() + index * 0.3f) * 0.05f
            
            // Вычисляем вращение для каждого предмета
            val rotation = subject.rotation + subjectRotation * (if (index % 2 == 0) 1f else -1f) * 0.01f
            
            // Используем просто scale предмета без пульсации
            val scale = subject.scale

            drawSubject(
                subject = subject.subject,
                position = Offset(
                    (subject.x + offsetX) * size.width,
                    (subject.y + offsetY) * size.height
                ),
                rotation = rotation,
                scale = scale,
                alpha = subject.alpha
            )
        }
    }
}

private fun DrawScope.drawSubject(
    subject: Subject,
    position: Offset,
    rotation: Float,
    scale: Float,
    alpha: Float
) {
    rotate(rotation, position) {
        when (subject) {
            is Subject.Math -> {
                // Рисуем калькулятор - символ математики
                // Корпус калькулятора
                drawRoundRect(
                    color = Color(0xFF333333).copy(alpha = alpha),
                    topLeft = Offset(position.x - 20f * scale, position.y - 25f * scale),
                    size = androidx.compose.ui.geometry.Size(40f * scale, 50f * scale),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(5f * scale, 5f * scale),
                    style = Fill
                )
                // Экран калькулятора
                drawRoundRect(
                    color = Color(0xFF88FF88).copy(alpha = alpha),
                    topLeft = Offset(position.x - 15f * scale, position.y - 20f * scale),
                    size = androidx.compose.ui.geometry.Size(30f * scale, 15f * scale),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f * scale, 2f * scale),
                    style = Fill
                )
                // Кнопки калькулятора
                repeat(3) { row ->
                    repeat(3) { col ->
                        drawCircle(
                            color = Color(0xFF555555).copy(alpha = alpha),
                            radius = 5f * scale,
                            center = Offset(
                                position.x - 10f * scale + col * 10f * scale,
                                position.y + 5f * scale + row * 10f * scale
                            ),
                            style = Fill
                        )
                    }
                }
                // Кнопка "равно"
                drawRoundRect(
                    color = Color(0xFFFF9900).copy(alpha = alpha),
                    topLeft = Offset(position.x + 15f * scale, position.y + 5f * scale),
                    size = androidx.compose.ui.geometry.Size(5f * scale, 20f * scale),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(1f * scale, 1f * scale),
                    style = Fill
                )
                // Цифры на экране
                drawLine(
                    color = Color(0xFF000000).copy(alpha = alpha),
                    start = Offset(position.x - 10f * scale, position.y - 15f * scale),
                    end = Offset(position.x + 10f * scale, position.y - 15f * scale),
                    strokeWidth = 2f * scale
                )
                // Добавляем знак "равно" на экране
                drawLine(
                    color = Color(0xFF000000).copy(alpha = alpha),
                    start = Offset(position.x - 5f * scale, position.y - 12f * scale),
                    end = Offset(position.x + 5f * scale, position.y - 12f * scale),
                    strokeWidth = 1f * scale
                )
                drawLine(
                    color = Color(0xFF000000).copy(alpha = alpha),
                    start = Offset(position.x - 5f * scale, position.y - 9f * scale),
                    end = Offset(position.x + 5f * scale, position.y - 9f * scale),
                    strokeWidth = 1f * scale
                )
            }
            is Subject.Physics -> {
                // Рисуем формулу E=mc² - символ физики
                // Фон для формулы
                drawRoundRect(
                    color = Color(0xFF1A237E).copy(alpha = alpha * 0.8f),
                    topLeft = Offset(position.x - 30f * scale, position.y - 15f * scale),
                    size = androidx.compose.ui.geometry.Size(60f * scale, 30f * scale),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(5f * scale, 5f * scale),
                    style = Fill
                )
                
                // Буква E
                drawLine(
                    color = Color.White.copy(alpha = alpha),
                    start = Offset(position.x - 25f * scale, position.y - 5f * scale),
                    end = Offset(position.x - 15f * scale, position.y - 5f * scale),
                    strokeWidth = 3f * scale
                )
                drawLine(
                    color = Color.White.copy(alpha = alpha),
                    start = Offset(position.x - 25f * scale, position.y - 5f * scale),
                    end = Offset(position.x - 25f * scale, position.y + 5f * scale),
                    strokeWidth = 3f * scale
                )
                drawLine(
                    color = Color.White.copy(alpha = alpha),
                    start = Offset(position.x - 25f * scale, position.y + 5f * scale),
                    end = Offset(position.x - 15f * scale, position.y + 5f * scale),
                    strokeWidth = 3f * scale
                )
                drawLine(
                    color = Color.White.copy(alpha = alpha),
                    start = Offset(position.x - 25f * scale, position.y),
                    end = Offset(position.x - 20f * scale, position.y),
                    strokeWidth = 3f * scale
                )
                
                // Знак "="
                drawLine(
                    color = Color.White.copy(alpha = alpha),
                    start = Offset(position.x - 10f * scale, position.y - 3f * scale),
                    end = Offset(position.x - 5f * scale, position.y - 3f * scale),
                    strokeWidth = 3f * scale
                )
                drawLine(
                    color = Color.White.copy(alpha = alpha),
                    start = Offset(position.x - 10f * scale, position.y + 3f * scale),
                    end = Offset(position.x - 5f * scale, position.y + 3f * scale),
                    strokeWidth = 3f * scale
                )
                
                // Буква m
                drawLine(
                    color = Color.White.copy(alpha = alpha),
                    start = Offset(position.x, position.y - 5f * scale),
                    end = Offset(position.x, position.y + 5f * scale),
                    strokeWidth = 3f * scale
                )
                drawLine(
                    color = Color.White.copy(alpha = alpha),
                    start = Offset(position.x + 5f * scale, position.y - 5f * scale),
                    end = Offset(position.x + 5f * scale, position.y + 5f * scale),
                    strokeWidth = 3f * scale
                )
                drawLine(
                    color = Color.White.copy(alpha = alpha),
                    start = Offset(position.x + 10f * scale, position.y - 5f * scale),
                    end = Offset(position.x + 10f * scale, position.y + 5f * scale),
                    strokeWidth = 3f * scale
                )
                
                // Буква c
                drawLine(
                    color = Color.White.copy(alpha = alpha),
                    start = Offset(position.x + 15f * scale, position.y - 5f * scale),
                    end = Offset(position.x + 25f * scale, position.y - 5f * scale),
                    strokeWidth = 3f * scale
                )
                drawLine(
                    color = Color.White.copy(alpha = alpha),
                    start = Offset(position.x + 15f * scale, position.y - 5f * scale),
                    end = Offset(position.x + 15f * scale, position.y + 5f * scale),
                    strokeWidth = 3f * scale
                )
                drawLine(
                    color = Color.White.copy(alpha = alpha),
                    start = Offset(position.x + 15f * scale, position.y + 5f * scale),
                    end = Offset(position.x + 25f * scale, position.y + 5f * scale),
                    strokeWidth = 3f * scale
                )
                
                // Степень ²
                drawLine(
                    color = Color.White.copy(alpha = alpha),
                    start = Offset(position.x + 25f * scale, position.y - 10f * scale),
                    end = Offset(position.x + 30f * scale, position.y - 10f * scale),
                    strokeWidth = 2f * scale
                )
                drawLine(
                    color = Color.White.copy(alpha = alpha),
                    start = Offset(position.x + 25f * scale, position.y - 15f * scale),
                    end = Offset(position.x + 30f * scale, position.y - 15f * scale),
                    strokeWidth = 2f * scale
                )
            }
            is Subject.Chemistry -> {
                // Рисуем красивую колбу - символ химии
                // Основание колбы
                drawPath(
                    path = Path().apply {
                        moveTo(position.x - 15f * scale, position.y + 10f * scale)
                        lineTo(position.x - 20f * scale, position.y + 30f * scale)
                        lineTo(position.x + 20f * scale, position.y + 30f * scale)
                        lineTo(position.x + 15f * scale, position.y + 10f * scale)
                        close()
                    },
                    color = Color(0xFFE0E0E0).copy(alpha = alpha),
                    style = Fill
                )
                
                // Горлышко колбы
                drawRoundRect(
                    color = Color(0xFFE0E0E0).copy(alpha = alpha),
                    topLeft = Offset(position.x - 8f * scale, position.y - 25f * scale),
                    size = androidx.compose.ui.geometry.Size(16f * scale, 35f * scale),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(5f * scale, 5f * scale),
                    style = Fill
                )
                
                // Жидкость в колбе
                drawPath(
                    path = Path().apply {
                        moveTo(position.x - 15f * scale, position.y + 10f * scale)
                        lineTo(position.x - 20f * scale, position.y + 30f * scale)
                        lineTo(position.x + 20f * scale, position.y + 30f * scale)
                        lineTo(position.x + 15f * scale, position.y + 10f * scale)
                        close()
                    },
                    color = Color(0x8800BCD4).copy(alpha = alpha),
                    style = Fill
                )
                
                // Пузырьки в жидкости
                repeat(5) { i ->
                    drawCircle(
                        color = Color(0xFFFFFFFF).copy(alpha = alpha * 0.8f),
                        radius = 2f * scale,
                        center = Offset(
                            position.x - 10f * scale + i * 5f * scale,
                            position.y + 15f * scale + sin(i * 0.5f) * 5f * scale
                        ),
                        style = Fill
                    )
                }
                
                // Добавляем химическую формулу H2O
                drawRoundRect(
                    color = Color(0xFF1A237E).copy(alpha = alpha * 0.8f),
                    topLeft = Offset(position.x - 15f * scale, position.y - 35f * scale),
                    size = androidx.compose.ui.geometry.Size(30f * scale, 10f * scale),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f * scale, 2f * scale),
                    style = Fill
                )
                
                // Буква H
                drawLine(
                    color = Color.White.copy(alpha = alpha),
                    start = Offset(position.x - 12f * scale, position.y - 33f * scale),
                    end = Offset(position.x - 12f * scale, position.y - 27f * scale),
                    strokeWidth = 1.5f * scale
                )
                drawLine(
                    color = Color.White.copy(alpha = alpha),
                    start = Offset(position.x - 8f * scale, position.y - 33f * scale),
                    end = Offset(position.x - 8f * scale, position.y - 27f * scale),
                    strokeWidth = 1.5f * scale
                )
                drawLine(
                    color = Color.White.copy(alpha = alpha),
                    start = Offset(position.x - 12f * scale, position.y - 30f * scale),
                    end = Offset(position.x - 8f * scale, position.y - 30f * scale),
                    strokeWidth = 1.5f * scale
                )
                
                // Цифра 2
                drawLine(
                    color = Color.White.copy(alpha = alpha),
                    start = Offset(position.x - 4f * scale, position.y - 33f * scale),
                    end = Offset(position.x + 0f * scale, position.y - 33f * scale),
                    strokeWidth = 1.5f * scale
                )
                drawLine(
                    color = Color.White.copy(alpha = alpha),
                    start = Offset(position.x + 0f * scale, position.y - 33f * scale),
                    end = Offset(position.x + 0f * scale, position.y - 30f * scale),
                    strokeWidth = 1.5f * scale
                )
                drawLine(
                    color = Color.White.copy(alpha = alpha),
                    start = Offset(position.x + 0f * scale, position.y - 30f * scale),
                    end = Offset(position.x - 4f * scale, position.y - 27f * scale),
                    strokeWidth = 1.5f * scale
                )
                drawLine(
                    color = Color.White.copy(alpha = alpha),
                    start = Offset(position.x - 4f * scale, position.y - 27f * scale),
                    end = Offset(position.x + 0f * scale, position.y - 27f * scale),
                    strokeWidth = 1.5f * scale
                )
                
                // Буква O
                drawCircle(
                    color = Color.White.copy(alpha = alpha),
                    radius = 2f * scale,
                    center = Offset(position.x + 8f * scale, position.y - 30f * scale),
                    style = Stroke(width = 1.5f * scale)
                )
            }
            is Subject.Literature -> {
                // Рисуем перо и чернильницу - символ литературы
                // Чернильница
                drawPath(
                    path = Path().apply {
                        moveTo(position.x - 10f * scale, position.y + 10f * scale)
                        lineTo(position.x - 15f * scale, position.y + 20f * scale)
                        lineTo(position.x + 15f * scale, position.y + 20f * scale)
                        lineTo(position.x + 10f * scale, position.y + 10f * scale)
                        close()
                    },
                    color = Color(0xFF000000).copy(alpha = alpha),
                    style = Fill
                )
                // Чернила
                drawCircle(
                    color = Color(0xFF0000FF).copy(alpha = alpha),
                    radius = 8f * scale,
                    center = Offset(position.x, position.y + 15f * scale),
                    style = Fill
                )
                // Перо
                drawLine(
                    color = Color(0xFF8B4513).copy(alpha = alpha),
                    start = Offset(position.x, position.y - 20f * scale),
                    end = Offset(position.x, position.y + 10f * scale),
                    strokeWidth = 3f * scale
                )
                // Острие пера
                drawPath(
                    path = Path().apply {
                        moveTo(position.x, position.y - 20f * scale)
                        lineTo(position.x - 5f * scale, position.y - 15f * scale)
                        lineTo(position.x, position.y - 10f * scale)
                        lineTo(position.x + 5f * scale, position.y - 15f * scale)
                        close()
                    },
                    color = Color(0xFF8B4513).copy(alpha = alpha),
                    style = Fill
                )
                // Добавляем каплю чернил
                drawCircle(
                    color = Color(0xFF0000FF).copy(alpha = alpha),
                    radius = 3f * scale,
                    center = Offset(position.x, position.y - 25f * scale),
                    style = Fill
                )
            }
            is Subject.History -> {
                // Рисуем глобус - символ истории и географии
                // Основа глобуса
                drawCircle(
                    color = Color(0xFF1A237E).copy(alpha = alpha),
                    radius = 20f * scale,
                    center = position,
                    style = Fill
                )
                
                // Меридианы и параллели
                repeat(3) { i ->
                    drawArc(
                        color = Color.White.copy(alpha = alpha * 0.7f),
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = Offset(position.x - 20f * scale, position.y - 20f * scale + i * 10f * scale),
                        size = androidx.compose.ui.geometry.Size(40f * scale, 10f * scale),
                        style = Stroke(width = 1.5f * scale)
                    )
                }
                repeat(3) { i ->
                    drawArc(
                        color = Color.White.copy(alpha = alpha * 0.7f),
                        startAngle = 90f,
                        sweepAngle = 180f,
                        useCenter = false,
                        topLeft = Offset(position.x - 20f * scale + i * 10f * scale, position.y - 20f * scale),
                        size = androidx.compose.ui.geometry.Size(10f * scale, 40f * scale),
                        style = Stroke(width = 1.5f * scale)
                    )
                }
                
                // Континенты (более четко)
                // Северная Америка
                drawPath(
                    path = Path().apply {
                        moveTo(position.x - 15f * scale, position.y - 10f * scale)
                        lineTo(position.x - 10f * scale, position.y - 15f * scale)
                        lineTo(position.x - 5f * scale, position.y - 10f * scale)
                        lineTo(position.x - 10f * scale, position.y - 5f * scale)
                        close()
                    },
                    color = Color(0xFF4CAF50).copy(alpha = alpha),
                    style = Fill
                )
                
                // Южная Америка
                drawPath(
                    path = Path().apply {
                        moveTo(position.x - 10f * scale, position.y - 5f * scale)
                        lineTo(position.x - 5f * scale, position.y - 10f * scale)
                        lineTo(position.x - 5f * scale, position.y + 5f * scale)
                        lineTo(position.x - 10f * scale, position.y + 10f * scale)
                        close()
                    },
                    color = Color(0xFF4CAF50).copy(alpha = alpha),
                    style = Fill
                )
                
                // Европа и Азия
                drawPath(
                    path = Path().apply {
                        moveTo(position.x + 5f * scale, position.y - 10f * scale)
                        lineTo(position.x + 15f * scale, position.y - 5f * scale)
                        lineTo(position.x + 15f * scale, position.y + 5f * scale)
                        lineTo(position.x + 5f * scale, position.y + 10f * scale)
                        lineTo(position.x, position.y + 5f * scale)
                        lineTo(position.x, position.y - 5f * scale)
                        close()
                    },
                    color = Color(0xFF4CAF50).copy(alpha = alpha),
                    style = Fill
                )
                
                // Африка
                drawPath(
                    path = Path().apply {
                        moveTo(position.x - 5f * scale, position.y + 5f * scale)
                        lineTo(position.x + 5f * scale, position.y + 10f * scale)
                        lineTo(position.x + 5f * scale, position.y + 15f * scale)
                        lineTo(position.x - 5f * scale, position.y + 10f * scale)
                        close()
                    },
                    color = Color(0xFF4CAF50).copy(alpha = alpha),
                    style = Fill
                )
                
                // Австралия
                drawPath(
                    path = Path().apply {
                        moveTo(position.x + 10f * scale, position.y + 10f * scale)
                        lineTo(position.x + 15f * scale, position.y + 10f * scale)
                        lineTo(position.x + 15f * scale, position.y + 15f * scale)
                        lineTo(position.x + 10f * scale, position.y + 15f * scale)
                        close()
                    },
                    color = Color(0xFF4CAF50).copy(alpha = alpha),
                    style = Fill
                )
                
                // Подставка глобуса
                drawPath(
                    path = Path().apply {
                        moveTo(position.x - 15f * scale, position.y + 20f * scale)
                        lineTo(position.x - 10f * scale, position.y + 30f * scale)
                        lineTo(position.x + 10f * scale, position.y + 30f * scale)
                        lineTo(position.x + 15f * scale, position.y + 20f * scale)
                        close()
                    },
                    color = Color(0xFF8B4513).copy(alpha = alpha),
                    style = Fill
                )
                
                // Ось глобуса
                drawLine(
                    color = Color(0xFF8B4513).copy(alpha = alpha),
                    start = Offset(position.x, position.y + 20f * scale),
                    end = Offset(position.x, position.y + 30f * scale),
                    strokeWidth = 3f * scale
                )
            }
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