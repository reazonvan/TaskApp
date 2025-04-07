package com.example.taskapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.example.taskapp.data.repository.AppSettings

// Создаем CompositionLocal для настроек приложения
val LocalAppSettings = compositionLocalOf { AppSettings() }

// Единая цветовая схема для всего приложения
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF2196F3),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFBBDEFB),
    onPrimaryContainer = Color(0xFF1976D2),
    secondary = Color(0xFF03A9F4),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFB3E5FC),
    onSecondaryContainer = Color(0xFF0288D1),
    tertiary = Color(0xFF00BCD4),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFB2EBF2),
    onTertiaryContainer = Color(0xFF0097A7),
    error = Color(0xFFE91E63),
    onError = Color.White,
    errorContainer = Color(0xFFF8BBD0),
    onErrorContainer = Color(0xFFC2185B),
    background = Color(0xFFF5F5F5),
    onBackground = Color(0xFF212121),
    surface = Color.White,
    onSurface = Color(0xFF212121),
    surfaceVariant = Color(0xFFEEEEEE),
    onSurfaceVariant = Color(0xFF757575),
    outline = Color(0xFFBDBDBD),
    outlineVariant = Color(0xFFE0E0E0)
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF64B5F6),
    onPrimary = Color(0xFF0D47A1),
    primaryContainer = Color(0xFF1A237E),
    onPrimaryContainer = Color(0xFFBBDEFB),
    secondary = Color(0xFF4FC3F7),
    onSecondary = Color(0xFF01579B),
    secondaryContainer = Color(0xFF0D47A1),
    onSecondaryContainer = Color(0xFFB3E5FC),
    tertiary = Color(0xFF4DD0E1),
    onTertiary = Color(0xFF006064),
    tertiaryContainer = Color(0xFF006064),
    onTertiaryContainer = Color(0xFFB2EBF2),
    error = Color(0xFFF06292),
    onError = Color(0xFF880E4F),
    errorContainer = Color(0xFF880E4F),
    onErrorContainer = Color(0xFFF8BBD0),
    background = Color(0xFF121212),
    onBackground = Color(0xFFE0E0E0),
    surface = Color(0xFF1E1E1E),
    onSurface = Color(0xFFE0E0E0),
    surfaceVariant = Color(0xFF2D2D2D),
    onSurfaceVariant = Color(0xFFBDBDBD),
    outline = Color(0xFF424242),
    outlineVariant = Color(0xFF303030)
)

// Цвета для состояний задач
val TaskStatusColors = object {
    val completed = object {
        val light = Color(0xFF4CAF50)
        val dark = Color(0xFF81C784)
    }
    
    val uncompleted = object {
        val light = Color(0xFFE91E63)
        val dark = Color(0xFFF06292)
    }
    
    val inProgress = object {
        val light = Color(0xFFFFC107)
        val dark = Color(0xFFFFD54F)
    }
}

// Цвета для интерактивного фона
val InteractiveBackgroundColors = object {
    val completed = object {
        val light = object {
            val primary = Color(0xFF64B5F6)
            val secondary = Color(0xFF90CAF9)
            val accent = Color(0xFF42A5F5)
            val node = Color(0xFF2196F3)
            val connection = Color(0x40BBDEFB)
            val particle = listOf(
                Color(0xFF4FC3F7),
                Color(0xFF29B6F6),
                Color(0xFF03A9F4),
                Color(0xFF00BCD4)
            )
            val touch = Color(0xFF1976D2)
        }
        
        val dark = object {
            val primary = Color(0xFF1A237E)
            val secondary = Color(0xFF283593)
            val accent = Color(0xFF3949AB)
            val node = Color(0xFF5C6BC0)
            val connection = Color(0x409FA8DA)
            val particle = listOf(
                Color(0xFF7986CB),
                Color(0xFF5C6BC0),
                Color(0xFF3F51B5),
                Color(0xFF3949AB)
            )
            val touch = Color(0xFF7986CB)
        }
    }
    
    val uncompleted = object {
        val light = object {
            val primary = Color(0xFFF06292)
            val secondary = Color(0xFFF8BBD0)
            val accent = Color(0xFFEC407A)
            val node = Color(0xFFE91E63)
            val connection = Color(0x40F48FB1)
            val particle = listOf(
                Color(0xFFF06292),
                Color(0xFFEC407A),
                Color(0xFFE91E63),
                Color(0xFFD81B60)
            )
            val touch = Color(0xFFC2185B)
        }
        
        val dark = object {
            val primary = Color(0xFF880E4F)
            val secondary = Color(0xFFAD1457)
            val accent = Color(0xFFC2185B)
            val node = Color(0xFFD81B60)
            val connection = Color(0x40F48FB1)
            val particle = listOf(
                Color(0xFFEC407A),
                Color(0xFFE91E63),
                Color(0xFFD81B60),
                Color(0xFFC2185B)
            )
            val touch = Color(0xFFF06292)
        }
    }
}

// Цвета для частиц и эффектов
val ParticleColors = object {
    val light = listOf(
        Color(0xFF4FC3F7),
        Color(0xFF29B6F6),
        Color(0xFF03A9F4),
        Color(0xFF00BCD4),
        Color(0xFFF06292),
        Color(0xFFEC407A),
        Color(0xFFE91E63),
        Color(0xFFD81B60)
    )
    
    val dark = listOf(
        Color(0xFF7986CB),
        Color(0xFF5C6BC0),
        Color(0xFF3F51B5),
        Color(0xFF3949AB),
        Color(0xFFEC407A),
        Color(0xFFE91E63),
        Color(0xFFD81B60),
        Color(0xFFC2185B)
    )
}

// Получение масштаба текста в зависимости от настроек
fun getTextSizeMultiplier(textSizePreference: Int): Float {
    return when (textSizePreference) {
        0 -> 0.85f // Маленький
        1 -> 1.0f  // Средний (стандартный)
        2 -> 1.2f  // Большой
        else -> 1.0f
    }
}

// Функция для изменения размера в зависимости от настроек
fun TextUnit.applyFontScale(textSizePreference: Int): TextUnit {
    return this.times(getTextSizeMultiplier(textSizePreference))
}

// Единая тема приложения
@Composable
fun AppTheme(
    isDarkTheme: Boolean = isSystemInDarkTheme(),
    appSettings: AppSettings = AppSettings(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (isDarkTheme) DarkColorScheme else LightColorScheme
    
    // Предоставляем настройки через CompositionLocal
    CompositionLocalProvider(
        LocalAppSettings provides appSettings
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography.scale(appSettings.textSize),
            content = content
        )
    }
}

// Расширение для определения темной темы
val androidx.compose.material3.ColorScheme.isDark: Boolean
    get() = background.luminance() < 0.5f 