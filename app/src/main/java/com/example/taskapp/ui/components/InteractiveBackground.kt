package com.example.taskapp.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.*
import kotlin.random.Random

/**
 * Интерактивный фон с эффектом нейронной сети и частицами
 */
@Composable
fun InteractiveBackground(
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean,
    uncompletedTasksCount: Int,
    isAllTasksCompleted: Boolean,
    pulseStrength: Float = 1f
) {
    // Принудительно обновляем компонент при изменении счетчика задач
    val recomposeKey = remember(isDarkTheme, uncompletedTasksCount, isAllTasksCompleted) { System.nanoTime() }
    
    // Выбираем цвета в зависимости от темы и статуса задач
    val primaryColor = if (isAllTasksCompleted) {
        if (isDarkTheme) Color(0xFF1A237E) else Color(0xFF64B5F6)
    } else {
        if (isDarkTheme) Color(0xFF880E4F) else Color(0xFFF06292)
    }
    
    val secondaryColor = if (isAllTasksCompleted) {
        if (isDarkTheme) Color(0xFF283593) else Color(0xFF90CAF9)
    } else {
        if (isDarkTheme) Color(0xFFAD1457) else Color(0xFFF8BBD0)
    }
    
    // Определяем схему цветов в зависимости от статуса задач и темы
    val colorPalette = if (isAllTasksCompleted) {
        if (isDarkTheme) {
            object {
                val primary = Color(0xFF1A237E)
                val secondary = Color(0xFF283593)
                val accent = Color(0xFF3949AB)
                val node = Color(0xFF5C6BC0)
                val connection = Color(0x409FA8DA)
                val touch = Color(0xFF7986CB)
            }
        } else {
            object {
                val primary = Color(0xFF64B5F6)
                val secondary = Color(0xFF90CAF9)
                val accent = Color(0xFF42A5F5)
                val node = Color(0xFF2196F3)
                val connection = Color(0x40BBDEFB)
                val touch = Color(0xFF1976D2)
            }
        }
    } else {
        if (isDarkTheme) {
            object {
                val primary = Color(0xFF880E4F)
                val secondary = Color(0xFFAD1457)
                val accent = Color(0xFFC2185B)
                val node = Color(0xFFD81B60)
                val connection = Color(0x40F48FB1)
                val touch = Color(0xFFF06292)
            }
        } else {
            object {
                val primary = Color(0xFFF06292)
                val secondary = Color(0xFFF8BBD0)
                val accent = Color(0xFFEC407A)
                val node = Color(0xFFE91E63)
                val connection = Color(0x40F48FB1)
                val touch = Color(0xFFC2185B)
            }
        }
    }
    
    // Используем key для принудительной перекомпозиции компонента
    key(recomposeKey) {
        // Цвета для частиц
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
        
        // Используем плотность экрана для масштабирования
        val density = LocalDensity.current
        
        // Класс для представления узла в нейронной сети
        class Node(
            var position: Offset,
            var size: Float,
            val connections: MutableList<Int> = mutableListOf(),
            var velocity: Offset = Offset(
                Random.nextFloat() * 0.5f - 0.25f,
                Random.nextFloat() * 0.5f - 0.25f
            ),
            var pulsePhase: Float = Random.nextFloat() * 2 * PI.toFloat()
        )
        
        // Класс для представления частицы
        class Particle(
            var position: Offset,
            var velocity: Offset,
            var size: Float,
            var color: Color,
            var alpha: Float = 1f,
            var lifetime: Float = Random.nextFloat() * 0.5f + 0.5f, // 0.5 - 1.0 секунды
            var rotation: Float = Random.nextFloat() * 360f
        )
        
        // Состояния для анимации
        val coroutineScope = rememberCoroutineScope()
        var touchPosition by remember { mutableStateOf<Offset?>(null) }
        var lastTouchTime by remember { mutableStateOf(0L) }
        var canvasSize by remember { mutableStateOf(Size.Zero) }
        var touchEffectAlpha by remember { mutableStateOf(0f) }
        
        // Создаем сетку узлов
        val nodes = remember {
            val nodeCount = 20 // Количество узлов в сетке
            val result = mutableListOf<Node>()
            
            // Создаем случайное расположение узлов
            for (i in 0 until nodeCount) {
                result.add(
                    Node(
                        position = Offset(
                            Random.nextFloat(),
                            Random.nextFloat()
                        ),
                        size = Random.nextFloat() * 3f + 2f
                    )
                )
            }
            
            // Устанавливаем соединения между узлами на основе расстояния
            for (i in result.indices) {
                for (j in i + 1 until result.size) {
                    // Если расстояние между узлами меньше порогового, создаем соединение
                    val threshold = 0.25f
                    val dx = result[i].position.x - result[j].position.x
                    val dy = result[i].position.y - result[j].position.y
                    val distance = sqrt(dx * dx + dy * dy)
                    
                    if (distance < threshold) {
                        result[i].connections.add(j)
                        result[j].connections.add(i)
                    }
                }
            }
            
            result
        }
        
        // Список частиц
        val particles = remember { mutableStateListOf<Particle>() }
        
        // Анимация пульсации
        val infiniteTransition = rememberInfiniteTransition(label = "pulse")
        val pulseValue by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 2 * PI.toFloat(),
            animationSpec = infiniteRepeatable(
                animation = tween(3000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "pulseAnimation"
        )
        
        // Состояние для хранения смещения фона
        var backgroundOffset by remember { mutableStateOf(Offset.Zero) }
        
        // Обработка тапа для создания частиц
        fun handleTap(offset: Offset) {
            touchPosition = offset
            lastTouchTime = System.currentTimeMillis()
            touchEffectAlpha = 1f
            
            // Запускаем анимацию затухания эффекта касания
            coroutineScope.launch {
                delay(50)
                val fadeTime = 500 // ms
                val startTime = System.currentTimeMillis()
                val initialAlpha = touchEffectAlpha
                
                while (touchEffectAlpha > 0) {
                    val elapsed = System.currentTimeMillis() - startTime
                    val progress = (elapsed / fadeTime.toFloat()).coerceIn(0f, 1f)
                    touchEffectAlpha = initialAlpha * (1f - progress)
                    delay(16) // примерно 60 FPS
                }
            }
            
            // Создаем частицы при тапе
            coroutineScope.launch {
                val particleCount = 15
                
                for (i in 0 until particleCount) {
                    val angle = Random.nextFloat() * 2 * PI.toFloat()
                    val speed = Random.nextFloat() * 4f + 2f
                    
                    particles.add(
                        Particle(
                            position = offset,
                            velocity = Offset(
                                cos(angle) * speed,
                                sin(angle) * speed
                            ),
                            size = Random.nextFloat() * 8f + 4f,
                            color = particleColors[Random.nextInt(particleColors.size)],
                            alpha = Random.nextFloat() * 0.5f + 0.5f
                        )
                    )
                }
                
                // Смещаем фон в сторону тапа
                val centerX = canvasSize.width / 2
                val centerY = canvasSize.height / 2
                val directionX = (offset.x - centerX) / canvasSize.width
                val directionY = (offset.y - centerY) / canvasSize.height
                
                // Анимируем смещение фона
                val startOffset = backgroundOffset
                val targetOffset = Offset(directionX * 20f, directionY * 20f)
                val animTime = 500L
                val startTime = System.currentTimeMillis()
                
                while (System.currentTimeMillis() - startTime < animTime) {
                    val progress = ((System.currentTimeMillis() - startTime) / animTime.toFloat()).coerceIn(0f, 1f)
                    // Используем эффект пружины для смещения
                    val springProgress = sin(progress * PI.toFloat() / 2)
                    backgroundOffset = Offset(
                        startOffset.x + (targetOffset.x - startOffset.x) * springProgress,
                        startOffset.y + (targetOffset.y - startOffset.y) * springProgress
                    )
                    delay(16)
                }
                
                // Возвращаем фон в исходное положение
                val returnStartTime = System.currentTimeMillis()
                val returnTime = 800L
                
                while (System.currentTimeMillis() - returnStartTime < returnTime) {
                    val progress = ((System.currentTimeMillis() - returnStartTime) / returnTime.toFloat()).coerceIn(0f, 1f)
                    // Используем кубическую кривую Безье для плавного возврата
                    val easeProgress = 1 - (1 - progress) * (1 - progress) * (1 - progress)
                    backgroundOffset = Offset(
                        targetOffset.x * (1 - easeProgress),
                        targetOffset.y * (1 - easeProgress)
                    )
                    delay(16)
                }
                
                backgroundOffset = Offset.Zero
            }
        }
        
        // Обновление и отрисовка частиц
        LaunchedEffect(Unit) {
            while (true) {
                // Обновляем частицы
                for (i in particles.size - 1 downTo 0) {
                    val particle = particles[i]
                    particle.position = Offset(
                        particle.position.x + particle.velocity.x,
                        particle.position.y + particle.velocity.y
                    )
                    particle.alpha -= 0.016f // Уменьшаем прозрачность для эффекта затухания
                    particle.rotation += particle.velocity.x * 2 // Добавляем вращение
                    
                    // Удаляем частицы, которые стали невидимыми
                    if (particle.alpha <= 0) {
                        particles.removeAt(i)
                    }
                }
                
                // Добавляем случайные частицы для эффекта движения
                if (Random.nextFloat() < 0.1f && particles.size < 50) {
                    val x = Random.nextFloat() * canvasSize.width
                    val y = Random.nextFloat() * canvasSize.height
                    
                    particles.add(
                        Particle(
                            position = Offset(x, y),
                            velocity = Offset(
                                (Random.nextFloat() - 0.5f) * 2f,
                                (Random.nextFloat() - 0.5f) * 2f
                            ),
                            size = Random.nextFloat() * 4f + 2f,
                            color = particleColors[Random.nextInt(particleColors.size)],
                            alpha = Random.nextFloat() * 0.3f + 0.1f, // Более прозрачные частицы для фона
                            lifetime = Random.nextFloat() * 1.5f + 0.5f, // Более длительное время жизни
                            rotation = Random.nextFloat() * 360f
                        )
                    )
                }
                
                // Обновляем узлы
                for (node in nodes) {
                    // Обновляем позицию на основе скорости
                    node.position = Offset(
                        (node.position.x + node.velocity.x * 0.001f).coerceIn(0f, 1f),
                        (node.position.y + node.velocity.y * 0.001f).coerceIn(0f, 1f)
                    )
                    
                    // Если узел достиг края, меняем направление
                    if (node.position.x <= 0f || node.position.x >= 1f) {
                        node.velocity = Offset(-node.velocity.x, node.velocity.y)
                    }
                    if (node.position.y <= 0f || node.position.y >= 1f) {
                        node.velocity = Offset(node.velocity.x, -node.velocity.y)
                    }
                }
                
                delay(16) // 60 FPS
            }
        }
        
        // Основной интерфейс
        Box(modifier = modifier.fillMaxSize()) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures { offset ->
                            handleTap(offset)
                        }
                    }
            ) {
                // Запоминаем размер канваса
                canvasSize = size
                
                // Рисуем фон с градиентом
                val gradientBrush = Brush.radialGradient(
                    colors = listOf(
                        primaryColor.copy(alpha = 0.4f),
                        secondaryColor.copy(alpha = 0.2f),
                        primaryColor.copy(alpha = 0.05f)
                    ),
                    center = Offset(size.width / 2 + backgroundOffset.x, size.height / 2 + backgroundOffset.y),
                    radius = size.width * 0.7f
                )
                
                // Рисуем основной фон
                drawRect(
                    brush = gradientBrush,
                    size = size
                )
                
                // Рисуем виньетку для большей глубины
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.4f)
                        ),
                        center = Offset(size.width / 2, size.height / 2),
                        radius = size.width * 0.8f
                    ),
                    size = size
                )
                
                // Расчет позиций узлов на канвасе
                val scaledNodes = nodes.map { node ->
                    Offset(
                        node.position.x * size.width + backgroundOffset.x,
                        node.position.y * size.height + backgroundOffset.y
                    )
                }
                
                // Рисуем соединения между узлами
                for (i in nodes.indices) {
                    val nodePos = scaledNodes[i]
                    
                    for (connIdx in nodes[i].connections) {
                        val connPos = scaledNodes[connIdx]
                        
                        // Рассчитываем расстояние между узлами для эффекта прозрачности
                        val dx = nodePos.x - connPos.x
                        val dy = nodePos.y - connPos.y
                        val distance = sqrt(dx * dx + dy * dy)
                        val maxDist = size.width * 0.25f
                        val alpha = (1 - (distance / maxDist).coerceIn(0f, 1f)) * 0.8f
                        
                        // Рисуем линию с эффектом пульсации и прозрачности
                        drawLine(
                            color = if (isAllTasksCompleted) {
                                if (isDarkTheme) Color(0x409FA8DA) else Color(0x40BBDEFB)
                            } else {
                                if (isDarkTheme) Color(0x40F48FB1) else Color(0x40F48FB1)
                            }.copy(
                                alpha = (alpha * (0.4f + 0.6f * (sin(pulseValue + nodes[i].pulsePhase) * 0.5f + 0.5f) * pulseStrength)).coerceIn(0f, 1f)
                            ),
                            start = nodePos,
                            end = connPos,
                            strokeWidth = 1.5f + 0.5f * sin(pulseValue + nodes[i].pulsePhase) * pulseStrength
                        )
                    }
                }
                
                // Рисуем узлы
                for (i in nodes.indices) {
                    val nodePos = scaledNodes[i]
                    val pulseEffect = sin(pulseValue + nodes[i].pulsePhase) * 0.5f + 0.5f
                    
                    // Радиус узла с эффектом пульсации
                    val radius = with(density) {
                        (nodes[i].size + 2f * pulseEffect * pulseStrength)
                    }
                    
                    // Рисуем внешний круг с эффектом пульсации
                    drawCircle(
                        color = if (isAllTasksCompleted) {
                            if (isDarkTheme) Color(0xFF5C6BC0) else Color(0xFF2196F3)
                        } else {
                            if (isDarkTheme) Color(0xFFD81B60) else Color(0xFFE91E63)
                        }.copy(alpha = (0.15f + 0.1f * pulseEffect * pulseStrength).coerceIn(0f, 1f)),
                        radius = radius * 2.5f,
                        center = nodePos
                    )
                    
                    // Рисуем средний круг
                    drawCircle(
                        color = if (isAllTasksCompleted) {
                            if (isDarkTheme) Color(0xFF5C6BC0) else Color(0xFF2196F3)
                        } else {
                            if (isDarkTheme) Color(0xFFD81B60) else Color(0xFFE91E63)
                        }.copy(alpha = (0.2f + 0.2f * pulseEffect * pulseStrength).coerceIn(0f, 1f)),
                        radius = radius * 1.5f,
                        center = nodePos
                    )
                    
                    // Рисуем внутренний круг
                    drawCircle(
                        color = if (isAllTasksCompleted) {
                            if (isDarkTheme) Color(0xFF5C6BC0) else Color(0xFF2196F3)
                        } else {
                            if (isDarkTheme) Color(0xFFD81B60) else Color(0xFFE91E63)
                        }.copy(alpha = (0.8f * pulseEffect * pulseStrength).coerceIn(0f, 1f)),
                        radius = radius,
                        center = nodePos
                    )
                    
                    // Рисуем обводку
                    drawCircle(
                        color = if (isAllTasksCompleted) {
                            if (isDarkTheme) Color(0xFF3949AB) else Color(0xFF42A5F5)
                        } else {
                            if (isDarkTheme) Color(0xFFC2185B) else Color(0xFFEC407A)
                        }.copy(alpha = (0.8f * pulseEffect * pulseStrength).coerceIn(0f, 1f)),
                        radius = radius,
                        center = nodePos,
                        style = Stroke(width = 1f)
                    )
                }
                
                // Рисуем частицы
                for (particle in particles) {
                    // Применяем вращение к частицам для более интересного эффекта
                    rotate(degrees = particle.rotation) {
                        drawCircle(
                            color = particle.color.copy(alpha = particle.alpha.coerceIn(0f, 1f)),
                            radius = particle.size,
                            center = particle.position
                        )
                    }
                }
                
                // Рисуем эффект касания (волны)
                touchPosition?.let { pos ->
                    val timeSinceTap = System.currentTimeMillis() - lastTouchTime
                    val radiusProgress = (timeSinceTap / 800f).coerceIn(0f, 1f)
                    
                    // Рисуем расходящиеся круги
                    for (i in 1..3) {
                        val delayedProgress = (radiusProgress - 0.1f * (3 - i)).coerceIn(0f, 1f)
                        val waveRadius = delayedProgress * size.width * 0.3f
                        
                        if (waveRadius > 0) {
                            drawCircle(
                                color = if (isAllTasksCompleted) {
                                    if (isDarkTheme) Color(0xFF7986CB) else Color(0xFF1976D2)
                                } else {
                                    if (isDarkTheme) Color(0xFFF06292) else Color(0xFFC2185B)
                                }.copy(alpha = (touchEffectAlpha * (1 - delayedProgress) * 0.3f).coerceIn(0f, 1f)),
                                radius = waveRadius,
                                center = pos,
                                style = Stroke(width = 1f + 2f * (1 - delayedProgress))
                            )
                        }
                    }
                }
            }
        }
    }
} 