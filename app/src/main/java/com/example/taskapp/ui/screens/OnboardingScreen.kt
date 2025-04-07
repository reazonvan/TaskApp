package com.example.taskapp.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.HorizontalPagerIndicator
import com.google.accompanist.pager.calculateCurrentOffsetForPage
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

// Функция линейной интерполяции для анимаций
private fun lerp(start: Float, stop: Float, fraction: Float): Float {
    return start + fraction * (stop - start)
}

// Модель данных для страницы онбординга
data class OnboardingPage(
    val title: String,
    val description: String,
    val icon: ImageVector
)

@OptIn(ExperimentalPagerApi::class)
@Composable
fun OnboardingScreen(
    onOnboardingFinished: () -> Unit
) {
    // Список страниц онбординга
    val pages = listOf(
        OnboardingPage(
            title = "Добро пожаловать!",
            description = "Мы рады, что вы выбрали наше приложение для управления задачами и отслеживания преподавателей.",
            icon = Icons.Default.School
        ),
        OnboardingPage(
            title = "Преподаватели",
            description = "На главном экране вы увидите список ваших преподавателей. Добавляйте их, нажав на '+' в нижнем правом углу.",
            icon = Icons.Default.Person
        ),
        OnboardingPage(
            title = "Задачи",
            description = "Нажмите на преподавателя, чтобы увидеть список задач. Добавляйте новые задачи и отмечайте их как выполненные.",
            icon = Icons.Default.Assignment
        ),
        OnboardingPage(
            title = "Уведомления",
            description = "Вы будете получать уведомления о предстоящих дедлайнах, чтобы всегда быть в курсе важных задач.",
            icon = Icons.Default.Notifications
        ),
        OnboardingPage(
            title = "Настройки",
            description = "Настройте приложение под себя: выберите тему, размер текста и другие параметры в меню настроек.",
            icon = Icons.Default.Settings
        )
    )

    // Состояние приветствия
    var showWelcomeAnimation by remember { mutableStateOf(true) }
    var showPager by remember { mutableStateOf(false) }
    var exitAnimation by remember { mutableStateOf(false) }
    
    // Состояние страниц
    val pagerState = rememberPagerState()
    val coroutineScope = rememberCoroutineScope()
    
    // Анимации для завершения онбординга
    val contentAlpha by animateFloatAsState(
        targetValue = if (exitAnimation) 0f else 1f,
        animationSpec = tween(500, easing = FastOutSlowInEasing),
        label = "content-alpha"
    )
    
    val contentScale by animateFloatAsState(
        targetValue = if (exitAnimation) 1.1f else 1f,
        animationSpec = tween(500, easing = FastOutSlowInEasing),
        label = "content-scale"
    )
    
    // Запускаем приветственную анимацию при первой композиции
    LaunchedEffect(Unit) {
        // Показываем анимацию приветствия 3 секунды
        delay(3000)
        showWelcomeAnimation = false
        delay(300) // Небольшая задержка перед показом пейджера
        showPager = true
    }
    
    // Наблюдаем за exitAnimation, чтобы запустить onOnboardingFinished
    LaunchedEffect(exitAnimation) {
        if (exitAnimation) {
            delay(500) // Даем анимации завершиться
            onOnboardingFinished()
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Приветственная анимация
        AnimatedVisibility(
            visible = showWelcomeAnimation,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut()
        ) {
            WelcomeAnimation()
        }
        
        // Пейджер с инструкциями
        AnimatedVisibility(
            visible = showPager,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        alpha = contentAlpha
                        scaleX = contentScale
                        scaleY = contentScale
                    },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Заголовок
                Text(
                    text = "Инструкция",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(top = 32.dp, bottom = 16.dp)
                )
                
                // Пейджер с инструкциями
                HorizontalPager(
                    count = pages.size,
                    state = pagerState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                ) { page ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp)
                            .graphicsLayer {
                                val pageOffset = calculateCurrentOffsetForPage(page).absoluteValue
                                
                                alpha = lerp(
                                    start = 0.5f,
                                    stop = 1f,
                                    fraction = 1f - pageOffset.coerceIn(0f, 1f)
                                )
                                
                                scaleX = lerp(
                                    start = 0.85f,
                                    stop = 1f,
                                    fraction = 1f - pageOffset.coerceIn(0f, 1f)
                                )
                                scaleY = lerp(
                                    start = 0.85f,
                                    stop = 1f,
                                    fraction = 1f - pageOffset.coerceIn(0f, 1f)
                                )
                            }
                    ) {
                        OnboardingPageContent(
                            page = pages[page]
                        )
                    }
                }
                
                // Индикаторы страниц
                HorizontalPagerIndicator(
                    pagerState = pagerState,
                    modifier = Modifier
                        .padding(16.dp)
                        .animateContentSize(),
                    activeColor = MaterialTheme.colorScheme.primary,
                    inactiveColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f),
                    indicatorWidth = 12.dp,
                    indicatorHeight = 6.dp,
                    indicatorShape = RoundedCornerShape(4.dp),
                    spacing = 8.dp
                )
                
                // Дополнительный текст с номером текущей страницы
                AnimatedVisibility(
                    visible = pagerState.currentPage < pages.size - 1,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut()
                ) {
                    Text(
                        text = "${pagerState.currentPage + 1} из ${pages.size}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                
                // Кнопки навигации
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Кнопка "Назад"
                    TextButton(
                        onClick = {
                            coroutineScope.launch {
                                if (pagerState.currentPage > 0) {
                                    pagerState.animateScrollToPage(pagerState.currentPage - 1)
                                }
                            }
                        },
                        enabled = pagerState.currentPage > 0
                    ) {
                        Text("Назад")
                    }
                    
                    // Кнопка "Далее" или "Начать"
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                if (pagerState.currentPage < pages.size - 1) {
                                    // Переход к следующей странице
                                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                } else {
                                    // Запускаем анимацию выхода перед завершением онбординга
                                    exitAnimation = true
                                }
                            }
                        },
                        modifier = Modifier.animateContentSize(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMedium
                            )
                        ),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (pagerState.currentPage == pages.size - 1) 
                                MaterialTheme.colorScheme.primary 
                            else 
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                        ),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = if (pagerState.currentPage == pages.size - 1) 8.dp else 4.dp
                        )
                    ) {
                        val isLastPage = pagerState.currentPage == pages.size - 1
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = if (isLastPage) "Начать" else "Далее"
                            )
                            if (isLastPage) {
                                // Анимированная пульсирующая иконка для кнопки "Начать"
                                val infiniteTransition = rememberInfiniteTransition("button-icon")
                                val scale by infiniteTransition.animateFloat(
                                    initialValue = 0.8f,
                                    targetValue = 1.2f,
                                    animationSpec = infiniteRepeatable(
                                        animation = tween(800, easing = FastOutSlowInEasing),
                                        repeatMode = RepeatMode.Reverse
                                    ),
                                    label = "icon-pulsate"
                                )
                                
                                Icon(
                                    imageVector = Icons.Default.ArrowForward,
                                    contentDescription = "Начать",
                                    modifier = Modifier.scale(scale)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OnboardingPageContent(page: OnboardingPage) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Анимация иконки
        val infiniteTransition = rememberInfiniteTransition("icon-animation")
        val scale by infiniteTransition.animateFloat(
            initialValue = 0.9f,
            targetValue = 1.1f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "icon-scale"
        )
        
        // Иконка с кругом
        Box(
            modifier = Modifier
                .size(120.dp)
                .scale(scale)
                .clip(RoundedCornerShape(60.dp))
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = page.icon,
                contentDescription = page.title,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(64.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Заголовок
        Text(
            text = page.title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Описание
        Text(
            text = page.description,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
    }
}

@Composable
fun WelcomeAnimation() {
    // Анимация для масштабирования
    val infiniteTransition = rememberInfiniteTransition("welcome-animation")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "welcome-scale"
    )
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.tertiary
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Значок приложения с анимацией
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .scale(scale)
                    .clip(RoundedCornerShape(70.dp))
                    .background(MaterialTheme.colorScheme.background.copy(alpha = 0.9f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.School,
                    contentDescription = "Добро пожаловать",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(80.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(40.dp))
            
            // Текст приветствия
            Text(
                text = "Добро пожаловать!",
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 32.sp,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Подзаголовок
            Text(
                text = "Мы рады видеть вас в нашем приложении",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Индикатор загрузки
            CircularProgressIndicator(
                color = Color.White,
                strokeWidth = 2.dp,
                modifier = Modifier.size(32.dp)
            )
        }
    }
} 