package com.example.tasksnap.presentation.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tasksnap.domain.model.Priority

/**
 * Visual badge showing task priority.
 * Color-coded: HIGH = red, MEDIUM = orange, LOW = green.
 */
@Composable
fun PriorityBadge(priority: Priority) {
    val (backgroundColor, textColor) = when (priority) {
        Priority.HIGH -> Color(0xFFFF6B6B) to Color.White
        Priority.MEDIUM -> Color(0xFFFFA500) to Color.White
        Priority.LOW -> Color(0xFF51CF66) to Color.White
    }

    Box(
        modifier = Modifier
            .background(backgroundColor, shape = RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = priority.name,
            color = textColor,
            fontSize = 10.sp
        )
    }
}