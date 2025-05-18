package com.example.stringcanvas.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.example.stringcanvas.domain.models.ThemeOption


// Добавьте эту фигуру в ваш файл с темами или в этот же файл
val CapsuleShape = RoundedCornerShape(50) // Полностью закругленные концы для капсульной формы

// --- Основные цвета ---
val SoftBlack = Color(0xFF1E1E1E)  // Мягкий черный
val SoftWhite = Color(0xFFF8F8F8)  // Мягкий белый
val LightGray = Color(0xFFE0E0E0)  // Светло-серый
val DarkGray = Color(0xFF424242)   // Темно-серый
val VeryLightGray = Color(0xFFF1F1F1)  // Очень светлый серый

// --- Приглушенные акцентные цвета ---
val MutedBlue = Color(0xFF6D8BBA)      // Приглушенный синий
val DustyTeal = Color(0xFF7AA9A3)     // Пыльно-бирюзовый
val WarmBeige = Color(0xFFD4C4A8)     // Теплый бежевый
val SoftOlive = Color(0xFFA8B197)     // Мягкий оливковый
val MutedRose = Color(0xFFC4A6A6)     // Приглушенный розовый

// --- Цвета ошибок и состояний ---
val ErrorRedLight = Color(0xFFD16666)  // Мягкий красный для светлой темы
val ErrorRedDark = Color(0xFFB74C4C)   // Приглушенный красный для темной темы
val SuccessGreen = Color(0xFF7A9D7A)   // Мягкий зеленый для успешных операций
val WarningYellow = Color(0xFFD4B771)  // Приглушенный желтый для предупреждений

// --- Полные цветовые схемы ---
private val DarkColorScheme = darkColorScheme(
    primary = MutedBlue,
    onPrimary = SoftWhite,
    primaryContainer = MutedBlue.copy(alpha = 0.2f),
    onPrimaryContainer = MutedBlue.copy(alpha = 0.8f),

    secondary = DustyTeal,
    onSecondary = SoftWhite,
    secondaryContainer = DustyTeal.copy(alpha = 0.2f),
    onSecondaryContainer = DustyTeal.copy(alpha = 0.8f),

    tertiary = WarmBeige,
    onTertiary = SoftBlack,
    tertiaryContainer = WarmBeige.copy(alpha = 0.2f),
    onTertiaryContainer = WarmBeige.copy(alpha = 0.8f),

    background = SoftBlack,
    onBackground = LightGray,

    surface = DarkGray,
    onSurface = LightGray,

    surfaceVariant = DarkGray.copy(red = 0.26f, green = 0.26f, blue = 0.28f),
    onSurfaceVariant = LightGray.copy(alpha = 0.8f),

    error = ErrorRedDark,
    onError = SoftWhite,
    errorContainer = ErrorRedDark.copy(alpha = 0.2f),
    onErrorContainer = ErrorRedDark.copy(alpha = 0.8f),

    outline = LightGray.copy(alpha = 0.5f),
    outlineVariant = LightGray.copy(alpha = 0.3f),

    scrim = Color.Black.copy(alpha = 0.5f),
//    inverseSurface = LightGray,
//    inverseOnSurface = SoftBlack,
    inversePrimary = MutedBlue.copy(alpha = 0.5f),

    inverseSurface    = DarkGray,           // ← был LightGray
    inverseOnSurface  = LightGray,          // ← был SoftBlack
)

private val LightColorScheme = lightColorScheme(
    primary = MutedBlue,
    onPrimary = SoftWhite,
    primaryContainer = MutedBlue.copy(alpha = 0.1f),
    onPrimaryContainer = MutedBlue.copy(alpha = 0.9f),

    secondary = DustyTeal,
    onSecondary = SoftWhite,
    secondaryContainer = DustyTeal.copy(alpha = 0.1f),
    onSecondaryContainer = DustyTeal.copy(alpha = 0.9f),

    tertiary = WarmBeige,
    onTertiary = SoftBlack,
    tertiaryContainer = WarmBeige.copy(alpha = 0.1f),
    onTertiaryContainer = WarmBeige.copy(alpha = 0.9f),

    background = SoftWhite,
    onBackground = SoftBlack,

    surface = VeryLightGray,
    onSurface = SoftBlack,

    surfaceVariant = Color(0xFFDCE4E8),//LightGray.copy(red = 0.96f, green = 0.96f, blue = 0.96f),
    onSurfaceVariant = SoftBlack.copy(alpha = 0.8f),

    error = ErrorRedLight,
    onError = SoftWhite,
    errorContainer = ErrorRedLight.copy(alpha = 0.1f),
    onErrorContainer = ErrorRedLight.copy(alpha = 0.9f),

    outline = DarkGray.copy(alpha = 0.3f),
    outlineVariant = DarkGray.copy(alpha = 0.1f),

    scrim = Color.Black.copy(alpha = 0.3f),
    inversePrimary = MutedBlue.copy(alpha = 0.3f),

    inverseSurface    = VeryLightGray,      // ← был SoftBlack
    inverseOnSurface  = SoftBlack,          // ← был SoftWhite
)


@Composable
fun StringCanvasTheme(
    option: ThemeOption,
    content: @Composable () -> Unit
) {
    val dark = when (option) {
        ThemeOption.SYSTEM -> isSystemInDarkTheme()
        ThemeOption.DARK -> true
        ThemeOption.LIGHT -> false
    }

    val colors = if (dark) DarkColorScheme else LightColorScheme

    // Применяем цвета системных панелей
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.setDecorFitsSystemWindows(window, false)

            WindowCompat.getInsetsController(window, view).apply {
                // Для светлой темы - темные иконки, для темной - светлые
                isAppearanceLightStatusBars = !dark
                isAppearanceLightNavigationBars = !dark

                // Устанавливаем цвета фона
                window.statusBarColor = Color.Transparent.toArgb()
                window.navigationBarColor = if (dark) {
                    // Для темной темы - темный фон
                    Color(0xFF1E1E1E).toArgb()
                } else {
                    // Для светлой темы - светлый фон
                    Color(0xFFF8F8F8).toArgb()
                }
            }
        }
    }

    MaterialTheme(
        colorScheme = colors,
        typography = Typography,
        content = content
    )
}
