package com.example.taskapp.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalConfiguration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

// Класс для представления плавающего предмета с сокращенными полями для экономии памяти
data class FloatingItem(
    val x: Float,
    val y: Float,
    val size: Float,
    val rotation: Float,
    val type: ItemType,
    val alpha: Float
)

// Типы предметов - более тематические для приложения задач
enum class ItemType {
    CHECKBOX, CALENDAR, CLOCK, DOCUMENT, PIN, STAR
}

// Кэшированные анимационные спецификации для предотвращения создания новых объектов
private val linearEasing = LinearEasing
private val linearOutSlowInEasing = LinearOutSlowInEasing
private val simpleRepeatMode = RepeatMode.Reverse

@Composable
fun FloatingBackground(
    modifier: Modifier = Modifier,
    itemCount: Int = 15,
    baseColor: Color = Color.Gray.copy(alpha = 0.1f),
    animationDuration: Int = 5000,
    particleCount: Int = 15,
    optimized: Boolean = false,
    content: @Composable () -> Unit = {}
) {
    // Кэшируем конфигурацию устройства для оптимизации под конкретный экран
    val config = LocalConfiguration.current
    val screenWidth = config.screenWidthDp
    
    // Оптимизированная версия использует меньше элементов и упрощенную отрисовку
    val effectiveItemCount = when {
        optimized -> particleCount.coerceAtMost(8)
        screenWidth < 600 -> itemCount.coerceAtMost(12) // Для маленьких экранов еще больше ограничиваем
        else -> itemCount
    }
    
    // Используем LocalDensity для учета плотности экрана при рендеринге
    val density = LocalDensity.current
    
    // Используем derivedStateOf для вычисления оптимальных размеров элементов
    val (minSize, maxSize) = remember(optimized, screenWidth) {
        if (optimized) {
            Pair(8f, 16f) // Маленькие размеры для оптимизированного режима
        } else if (screenWidth < 600) {
            Pair(15f, 25f) // Средние размеры для телефонов
        } else {
            Pair(20f, 35f) // Большие размеры для планшетов
        }
    }
    
    // Используем remember с ключом optimized, чтобы пересоздать элементы при изменении режима оптимизации
    val items = remember(optimized, effectiveItemCount, minSize, maxSize) {
        List(effectiveItemCount) {
            val itemType = when {
                optimized -> {
                    // В оптимизированном режиме используем только самые простые фигуры
                    when (it % 3) {
                        0 -> ItemType.CHECKBOX
                        1 -> ItemType.STAR
                        else -> ItemType.CALENDAR
                    }
                }
                else -> ItemType.values()[it % ItemType.values().size]
            }
            
            FloatingItem(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                size = Random.nextFloat() * (maxSize - minSize) + minSize,
                rotation = Random.nextFloat() * 360f,
                type = itemType,
                alpha = if (optimized) {
                    // Уменьшаем прозрачность для снижения нагрузки на рендеринг
                    Random.nextFloat() * 0.15f + 0.05f
                } else {
                    Random.nextFloat() * 0.25f + 0.1f
                }
            )
        }
    }

    // Используем кэшированный инфинитный переход вместо создания нового при каждой перекомпозиции
    val infiniteTransition = rememberInfiniteTransition(label = "floating_items_transition")
    
    // Более продолжительная анимация требует меньше перерисовок
    val effectiveAnimationDuration = if (optimized) {
        animationDuration.coerceAtLeast(8000)
    } else {
        animationDuration
    }
    
    // Создаем кэшированные спецификации анимации для повторного использования
    val primaryAnimSpec = remember(effectiveAnimationDuration, optimized) {
        infiniteRepeatable<Float>(
            animation = tween(
                durationMillis = effectiveAnimationDuration,
                easing = if (optimized) linearOutSlowInEasing else linearEasing
            ),
            repeatMode = simpleRepeatMode
        )
    }
    
    val secondaryAnimSpec = remember(effectiveAnimationDuration, optimized) {
        infiniteRepeatable<Float>(
            animation = tween(
                durationMillis = effectiveAnimationDuration + 2000,
                easing = if (optimized) linearOutSlowInEasing else linearEasing
            ),
            repeatMode = simpleRepeatMode
        )
    }
    
    val rotationAnimSpec = remember(effectiveAnimationDuration, optimized) {
        infiniteRepeatable<Float>(
            animation = tween(
                durationMillis = if (optimized) effectiveAnimationDuration * 6 else effectiveAnimationDuration * 4,
                easing = linearEasing
            ),
            repeatMode = RepeatMode.Restart
        )
    }
    
    // Создаем анимации для каждого предмета с оптимизацией, используя кэшированные спецификации
    val animatedItems = items.mapIndexed { index, item ->
        // Смещения по X, разные для каждого индекса, но с общей спецификацией анимации
        val offsetX by infiniteTransition.animateFloat(
            initialValue = item.x,
            targetValue = item.x + (Random.nextFloat() * 0.06f - 0.03f) * (if (optimized) 0.5f else 1f),
            animationSpec = if (index % 2 == 0) primaryAnimSpec else secondaryAnimSpec,
            label = "item_x_$index"
        )
        
        // Смещения по Y, разные для каждого индекса, но с общей спецификацией анимации
        val offsetY by infiniteTransition.animateFloat(
            initialValue = item.y,
            targetValue = item.y + (Random.nextFloat() * 0.06f - 0.03f) * (if (optimized) 0.5f else 1f),
            animationSpec = if (index % 2 == 0) secondaryAnimSpec else primaryAnimSpec,
            label = "item_y_$index"
        )
        
        // Вращение, одинаковая спецификация анимации для всех элементов
        val rotation by infiniteTransition.animateFloat(
            initialValue = item.rotation,
            targetValue = item.rotation + 360f,
            animationSpec = rotationAnimSpec,
            label = "item_rotation_$index"
        )
        
        Triple(offsetX, offsetY, rotation) to item
    }
    
    // Помечаем композицию как стабильную, чтобы избежать ненужных перекомпозиций
    val stableModifier = remember(modifier) { modifier }
    
    // Общий цвет для всех элементов для снижения количества создаваемых объектов Color
    val backgroundBaseColor = remember(baseColor) { baseColor }
    
    Box(modifier = stableModifier.fillMaxSize()) {
        // Рисуем только фон
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            
            // Сгруппируем элементы по типу, чтобы минимизировать переключение стилей рисования
            val groupedItems = animatedItems.groupBy { (_, item) -> item.type }
            
            // Отрисовываем элементы группами для оптимизации рендеринга
            groupedItems.forEach { (type, items) ->
                // Для каждого типа рисуем все элементы сразу
                items.forEach { (animation, item) ->
                    val (offsetX, offsetY, rotation) = animation
                    
                    // В оптимизированном режиме используем более простые фигуры и меньше деталей
                    if (optimized) {
                        drawOptimizedItem(offsetX, offsetY, rotation, item, backgroundBaseColor, canvasWidth, canvasHeight)
                    } else {
                        // Отрисовываем предмет с полными деталями
                        rotate(rotation) {
                            when (type) {
                                ItemType.CHECKBOX -> drawCheckbox(offsetX, offsetY, item.size, backgroundBaseColor.copy(alpha = item.alpha), canvasWidth, canvasHeight)
                                ItemType.CALENDAR -> drawCalendar(offsetX, offsetY, item.size, backgroundBaseColor.copy(alpha = item.alpha), canvasWidth, canvasHeight)
                                ItemType.CLOCK -> drawClock(offsetX, offsetY, item.size, backgroundBaseColor.copy(alpha = item.alpha), canvasWidth, canvasHeight)
                                ItemType.DOCUMENT -> drawDocument(offsetX, offsetY, item.size, backgroundBaseColor.copy(alpha = item.alpha), canvasWidth, canvasHeight)
                                ItemType.PIN -> drawPin(offsetX, offsetY, item.size, backgroundBaseColor.copy(alpha = item.alpha), canvasWidth, canvasHeight)
                                ItemType.STAR -> drawStar(offsetX, offsetY, item.size, backgroundBaseColor.copy(alpha = item.alpha), canvasWidth, canvasHeight)
                            }
                        }
                    }
                }
            }
        }
        
        // Контент поверх фона
        content()
    }
}

// Оптимизированная отрисовка для повышения производительности
private fun DrawScope.drawOptimizedItem(
    x: Float, 
    y: Float, 
    rotation: Float, 
    item: FloatingItem, 
    baseColor: Color, 
    canvasWidth: Float, 
    canvasHeight: Float
) {
    // Используем более простые формы для отрисовки и минимизируем создание новых объектов цвета
    val alpha = item.alpha
    val color = baseColor.copy(alpha = alpha)
    val centerX = x * canvasWidth
    val centerY = y * canvasHeight
    val halfSize = item.size / 2
    
    // Оптимизированная отрисовка в зависимости от типа
    when (item.type) {
        ItemType.CHECKBOX -> {
            // Упрощенный чекбокс - просто квадрат
            drawRect(
                color = color,
                topLeft = Offset(centerX - halfSize, centerY - halfSize),
                size = Size(item.size, item.size),
                style = Stroke(width = item.size / 12)
            )
        }
        ItemType.STAR, ItemType.PIN -> {
            // Упрощенная звезда или пин - просто круг
            drawCircle(
                color = color,
                radius = halfSize,
                center = Offset(centerX, centerY),
                style = Stroke(width = item.size / 12)
            )
        }
        else -> {
            // Все остальные типы отображаем как простой прямоугольник
            val width = item.size
            val height = item.size * 0.8f
            drawRect(
                color = color,
                topLeft = Offset(centerX - width/2, centerY - height/2),
                size = Size(width, height),
                style = Stroke(width = item.size / 15)
            )
        }
    }
}

private fun DrawScope.drawCheckbox(x: Float, y: Float, size: Float, color: Color, canvasWidth: Float, canvasHeight: Float) {
    // Рисуем рамку чекбокса
    drawRect(
        color = color,
        topLeft = Offset(x * canvasWidth - size/2, y * canvasHeight - size/2),
        size = Size(size, size),
        style = Stroke(width = size / 10)
    )
    
    // Рисуем галочку внутри чекбокса
    val path = Path().apply {
        moveTo(x * canvasWidth - size * 0.3f, y * canvasHeight)
        lineTo(x * canvasWidth - size * 0.1f, y * canvasHeight + size * 0.2f)
        lineTo(x * canvasWidth + size * 0.3f, y * canvasHeight - size * 0.3f)
    }
    
    drawPath(
        path = path,
        color = color,
        style = Stroke(width = size / 10, cap = StrokeCap.Round)
    )
}

private fun DrawScope.drawCalendar(x: Float, y: Float, size: Float, color: Color, canvasWidth: Float, canvasHeight: Float) {
    // Рисуем основную рамку календаря
    drawRect(
        color = color,
        topLeft = Offset(x * canvasWidth - size/2, y * canvasHeight - size/2),
        size = Size(size, size)
    )
    
    // Верхняя полоса календаря (месяц)
    drawRect(
        color = color.copy(alpha = 0.7f),
        topLeft = Offset(x * canvasWidth - size/2, y * canvasHeight - size/2),
        size = Size(size, size * 0.2f)
    )
    
    // Рисуем линии календаря
    val lineSpacing = size / 4
    for (i in 1..2) {
        // Горизонтальные
        drawLine(
            color = color.copy(alpha = 0.5f),
            start = Offset(x * canvasWidth - size/2, y * canvasHeight - size/2 + lineSpacing * i),
            end = Offset(x * canvasWidth + size/2, y * canvasHeight - size/2 + lineSpacing * i),
            strokeWidth = size / 20
        )
    }
    
    for (i in 1..2) {
        // Вертикальные
        drawLine(
            color = color.copy(alpha = 0.5f),
            start = Offset(x * canvasWidth - size/2 + lineSpacing * i, y * canvasHeight - size/2 + size * 0.2f),
            end = Offset(x * canvasWidth - size/2 + lineSpacing * i, y * canvasHeight + size/2),
            strokeWidth = size / 20
        )
    }
}

private fun DrawScope.drawClock(x: Float, y: Float, size: Float, color: Color, canvasWidth: Float, canvasHeight: Float) {
    // Рисуем круг часов
    drawCircle(
        color = color,
        radius = size / 2,
        center = Offset(x * canvasWidth, y * canvasHeight),
        style = Stroke(width = size / 10)
    )
    
    // Часовая стрелка
    drawLine(
        color = color,
        start = Offset(x * canvasWidth, y * canvasHeight),
        end = Offset(
            x * canvasWidth + cos(45f * Math.PI.toFloat() / 180f) * size * 0.3f,
            y * canvasHeight + sin(45f * Math.PI.toFloat() / 180f) * size * 0.3f
        ),
        strokeWidth = size / 15
    )
    
    // Минутная стрелка
    drawLine(
        color = color,
        start = Offset(x * canvasWidth, y * canvasHeight),
        end = Offset(
            x * canvasWidth + cos(240f * Math.PI.toFloat() / 180f) * size * 0.4f,
            y * canvasHeight + sin(240f * Math.PI.toFloat() / 180f) * size * 0.4f
        ),
        strokeWidth = size / 20
    )
    
    // Центральная точка
    drawCircle(
        color = color,
        radius = size / 15,
        center = Offset(x * canvasWidth, y * canvasHeight)
    )
    
    // Отметки времени
    for (i in 0..11) {
        val angle = i * 30f * Math.PI.toFloat() / 180f
        val startRatio = if (i % 3 == 0) 0.4f else 0.45f
        
        drawLine(
            color = color,
            start = Offset(
                x * canvasWidth + cos(angle) * size * startRatio,
                y * canvasHeight + sin(angle) * size * startRatio
            ),
            end = Offset(
                x * canvasWidth + cos(angle) * size * 0.5f,
                y * canvasHeight + sin(angle) * size * 0.5f
            ),
            strokeWidth = if (i % 3 == 0) size / 15 else size / 20
        )
    }
}

private fun DrawScope.drawDocument(x: Float, y: Float, size: Float, color: Color, canvasWidth: Float, canvasHeight: Float) {
    // Основной документ
    drawRect(
        color = color,
        topLeft = Offset(x * canvasWidth - size/2, y * canvasHeight - size/2),
        size = Size(size, size * 1.3f)
    )
    
    // Линии текста
    val lineOffset = size * 0.15f
    for (i in 0..4) {
        drawLine(
            color = color.copy(alpha = 0.5f),
            start = Offset(x * canvasWidth - size * 0.35f, y * canvasHeight - size * 0.3f + i * lineOffset),
            end = Offset(x * canvasWidth + size * 0.35f, y * canvasHeight - size * 0.3f + i * lineOffset),
            strokeWidth = size / 20
        )
    }
    
    // Загиб страницы
    val cornerPath = Path().apply {
        moveTo(x * canvasWidth + size/2, y * canvasHeight - size/2)
        lineTo(x * canvasWidth + size/2, y * canvasHeight - size/2 + size * 0.3f)
        lineTo(x * canvasWidth + size/2 - size * 0.3f, y * canvasHeight - size/2)
        close()
    }
    
    drawPath(
        path = cornerPath,
        color = color.copy(alpha = 0.7f)
    )
}

private fun DrawScope.drawPin(x: Float, y: Float, size: Float, color: Color, canvasWidth: Float, canvasHeight: Float) {
    // Верхняя часть кнопки
    drawCircle(
        color = color,
        radius = size / 3,
        center = Offset(x * canvasWidth, y * canvasHeight - size * 0.2f)
    )
    
    // Острие кнопки
    val pinPath = Path().apply {
        moveTo(x * canvasWidth, y * canvasHeight - size * 0.2f)
        lineTo(x * canvasWidth + size * 0.2f, y * canvasHeight + size * 0.2f)
        lineTo(x * canvasWidth - size * 0.2f, y * canvasHeight + size * 0.2f)
        close()
    }
    
    drawPath(
        path = pinPath,
        color = color
    )
}

private fun DrawScope.drawStar(x: Float, y: Float, size: Float, color: Color, canvasWidth: Float, canvasHeight: Float) {
    val center = Offset(x * canvasWidth, y * canvasHeight)
    val outerRadius = size / 2
    val innerRadius = size / 5
    val path = Path()
    
    for (i in 0 until 10) {
        val radius = if (i % 2 == 0) outerRadius else innerRadius
        val angle = Math.PI.toFloat() / 5f * i - Math.PI.toFloat() / 2f
        
        val px = center.x + radius * cos(angle)
        val py = center.y + radius * sin(angle)
        
        if (i == 0) {
            path.moveTo(px, py)
        } else {
            path.lineTo(px, py)
        }
    }
    
    path.close()
    drawPath(path, color)
} 