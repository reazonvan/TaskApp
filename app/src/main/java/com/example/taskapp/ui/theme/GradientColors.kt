package com.example.taskapp.ui.theme

import androidx.compose.ui.graphics.Color

// Цветовые темы для интерактивного фона
object InteractiveGradientColors {
    // Цвета для темы "все задачи выполнены"
    object Completed {
        // Светлая тема
        object Light {
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
        
        // Темная тема
        object Dark {
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
    
    // Цвета для темы "есть невыполненные задачи"
    object Uncompleted {
        // Светлая тема
        object Light {
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
        
        // Темная тема
        object Dark {
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