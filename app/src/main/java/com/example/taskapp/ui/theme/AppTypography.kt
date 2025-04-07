package com.example.taskapp.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Единая типографика для всего приложения
val AppTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp
    ),
    displayMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 45.sp,
        lineHeight = 52.sp,
        letterSpacing = 0.sp
    ),
    displaySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = 0.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    titleSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)

// Расширение для масштабирования типографики в зависимости от настроек
fun Typography.scale(textSizePreference: Int): Typography {
    val scaleFactor = getTextSizeMultiplier(textSizePreference)
    
    return copy(
        displayLarge = displayLarge.copy(fontSize = displayLarge.fontSize * scaleFactor),
        displayMedium = displayMedium.copy(fontSize = displayMedium.fontSize * scaleFactor),
        displaySmall = displaySmall.copy(fontSize = displaySmall.fontSize * scaleFactor),
        headlineLarge = headlineLarge.copy(fontSize = headlineLarge.fontSize * scaleFactor),
        headlineMedium = headlineMedium.copy(fontSize = headlineMedium.fontSize * scaleFactor),
        headlineSmall = headlineSmall.copy(fontSize = headlineSmall.fontSize * scaleFactor),
        titleLarge = titleLarge.copy(fontSize = titleLarge.fontSize * scaleFactor),
        titleMedium = titleMedium.copy(fontSize = titleMedium.fontSize * scaleFactor),
        titleSmall = titleSmall.copy(fontSize = titleSmall.fontSize * scaleFactor),
        bodyLarge = bodyLarge.copy(fontSize = bodyLarge.fontSize * scaleFactor),
        bodyMedium = bodyMedium.copy(fontSize = bodyMedium.fontSize * scaleFactor),
        bodySmall = bodySmall.copy(fontSize = bodySmall.fontSize * scaleFactor),
        labelLarge = labelLarge.copy(fontSize = labelLarge.fontSize * scaleFactor),
        labelMedium = labelMedium.copy(fontSize = labelMedium.fontSize * scaleFactor),
        labelSmall = labelSmall.copy(fontSize = labelSmall.fontSize * scaleFactor)
    )
} 