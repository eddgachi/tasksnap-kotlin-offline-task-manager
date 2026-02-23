package com.example.tasksnap.presentation.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary            = Indigo600,
    onPrimary          = Color.White,
    primaryContainer   = Indigo100,
    onPrimaryContainer = Indigo950,

    secondary            = Violet600,
    onSecondary          = Color.White,
    secondaryContainer   = Violet100,
    onSecondaryContainer = Indigo950,

    background    = Slate50,
    onBackground  = Slate900,

    surface        = Color.White,
    onSurface      = Slate800,
    surfaceVariant = Slate100,
    onSurfaceVariant = Slate500,

    outline        = Slate300,
    outlineVariant = Slate200,

    error            = Red600,
    onError          = Color.White,
    errorContainer   = Red100,
    onErrorContainer = Red900,
)

private val DarkColors = darkColorScheme(
    primary            = Indigo500,
    onPrimary          = Indigo950,
    primaryContainer   = Indigo600,
    onPrimaryContainer = Indigo100,

    secondary            = Violet600,
    onSecondary          = Color.White,
    secondaryContainer   = Violet600,
    onSecondaryContainer = Violet100,

    background    = Slate900,
    onBackground  = Slate100,

    surface        = Slate800,
    onSurface      = Slate100,
    surfaceVariant = Slate700,
    onSurfaceVariant = Slate400,

    outline        = Slate500,
    outlineVariant = Slate700,

    error            = Red600,
    onError          = Color.White,
    errorContainer   = Red900,
    onErrorContainer = Red100,
)

@Composable
fun TaskManagerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content,
    )
}
