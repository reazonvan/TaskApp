package com.example.taskapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun NeomorphicCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 12.dp,
    shadowElevation: Dp = 8.dp,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    onClick: () -> Unit = {},
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(shadowElevation)
    ) {
        // Тень снизу справа
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(cornerRadius))
                .shadow(
                    elevation = shadowElevation,
                    shape = RoundedCornerShape(cornerRadius),
                    spotColor = Color.Black.copy(alpha = 0.2f),
                    ambientColor = Color.Black.copy(alpha = 0.2f)
                )
                .background(Color.Transparent)
        ) {
            // Основная карточка
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(cornerRadius))
                    .background(backgroundColor)
                    .clickable { onClick() }
                    .padding(16.dp),
                content = { content() }
            )
        }
    }
} 