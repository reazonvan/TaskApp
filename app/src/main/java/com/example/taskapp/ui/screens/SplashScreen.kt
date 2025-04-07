package com.example.taskapp.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.random.Random

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    // Анимации для элементов сплэш-скрина
    var showIcon by remember { mutableStateOf(false) }
    var showTitle by remember { mutableStateOf(false) }
    var showSubtitle by remember { mutableStateOf(false) }
    var showParticles by remember { mutableStateOf(false) }
    
    // Анимация иконки
    val iconScale by animateFloatAsState(
        targetValue = if (showIcon) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "IconScale"
    )
    
    // Вращение иконки
    val iconRotation = remember { Animatable(0f) }
    
    // Запускаем анимации последовательно
    LaunchedEffect(Unit) {
        delay(300)
        showIcon = true
        
        // Вращение иконки
        iconRotation.animateTo(
            targetValue = 360f,
            animationSpec = tween(
                durationMillis = 1000,
                easing = FastOutSlowInEasing
            )
        )
        
        delay(300)
        showTitle = true
        
        delay(200)
        showSubtitle = true
        
        delay(200)
        showParticles = true
        
        // Завершаем сплэш-скрин через 2 секунды
        delay(1500)
        onSplashFinished()
    }
    
    // Градиент для фона (статичный, чтобы уменьшить перерисовки)
    val gradientBrush = remember {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFF5B8DEF),
                Color(0xFF365BB5)
            )
        )
    }
    
    // Градиентный фон
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradientBrush),
        contentAlignment = Alignment.Center
    ) {
        // Плавающие частицы (за иконкой)
        if (showParticles) {
            FloatingParticles()
        }
        
        // Иконка приложения
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .scale(iconScale)
                    .graphicsLayer { 
                        rotationZ = iconRotation.value
                    },
                contentAlignment = Alignment.Center
            ) {
                // Круг под иконкой
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFC3D4FF))
                )
                
                // Иконка
                Icon(
                    imageVector = Icons.Filled.School,
                    contentDescription = "Преподаватели",
                    tint = Color(0xFF0D47A1),
                    modifier = Modifier.size(80.dp)
                )
                
                // Маленькая иконка
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset((-10).dp, (-10).dp)
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF2196F3))
                        .padding(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Assignment,
                        contentDescription = "Задания",
                        tint = Color.White
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Название приложения
            AnimatedVisibility(
                visible = showTitle,
                enter = fadeIn() + expandVertically(
                    expandFrom = Alignment.Top,
                    animationSpec = tween(durationMillis = 500)
                )
            ) {
                Text(
                    text = "Мои Преподаватели",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 32.sp
                    ),
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Подзаголовок
            AnimatedVisibility(
                visible = showSubtitle,
                enter = fadeIn() + expandHorizontally(
                    expandFrom = Alignment.Start,
                    animationSpec = tween(durationMillis = 500)
                )
            ) {
                Text(
                    text = "Учет заданий и дедлайнов",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center
                )
            }
        }
        
        // Индикатор загрузки внизу экрана
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 64.dp)
        ) {
            if (showSubtitle) {
                PulsatingDots()
            }
        }
    }
}

@Composable
fun FloatingParticles() {
    // Уменьшаем количество частиц
    val particles = remember {
        List(15) {
            Particle(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                size = Random.nextFloat() * 8f + 4f,
                speed = Random.nextFloat() * 0.003f + 0.001f,
                alpha = Random.nextFloat() * 0.3f + 0.2f
            )
        }
    }
    
    // Кэшируем все анимации в одном ключе, чтобы избежать создания множества анимаций
    val transition = rememberInfiniteTransition("particles")
    
    // Создаем только одну анимацию для Y-координаты
    val yOffset by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 10000,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "particle-y-position"
    )
    
    // Создаем только одну анимацию для X-координаты
    val xOffset by transition.animateFloat(
        initialValue = -0.05f,
        targetValue = 0.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 5000,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "particle-x-position"
    )
    
    // Рисуем частицы с заранее вычисленными офсетами
    Box(modifier = Modifier.fillMaxSize()) {
        particles.forEach { particle ->
            // Вычисляем позицию с учетом общих офсетов
            val y = (particle.y + (yOffset * particle.speed * 10)) % 1f
            val x = particle.x + (xOffset * (1 - particle.speed * 100))
            
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .graphicsLayer {
                        translationX = x * size.width
                        translationY = y * size.height
                    }
            ) {
                Box(
                    modifier = Modifier
                        .size(particle.size.dp)
                        .clip(RoundedCornerShape(percent = 50))
                        .background(
                            Color.White.copy(alpha = particle.alpha)
                        )
                )
            }
        }
    }
}

@Composable
fun PulsatingDots() {
    // Используем одну единственную анимацию для всех точек
    val transition = rememberInfiniteTransition("dots")
    val baseScale by transition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 700,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot-base-scale"
    )
    
    Row(
        modifier = Modifier
            .wrapContentSize()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Используем смещения по фазе вместо отдельных анимаций
        repeat(3) { index ->
            val phaseOffset = index * 0.25f
            val dotScale = if (baseScale + phaseOffset > 1f) 
                2f - (baseScale + phaseOffset) 
            else 
                baseScale + phaseOffset
                
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .scale(dotScale)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.8f))
            )
        }
    }
}

// Класс для хранения данных о частицах
private data class Particle(
    val x: Float,
    val y: Float,
    val size: Float,
    val speed: Float,
    val alpha: Float
) 