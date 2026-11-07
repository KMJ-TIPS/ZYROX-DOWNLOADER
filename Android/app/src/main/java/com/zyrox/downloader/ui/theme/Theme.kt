package com.zyrox.downloader.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zyrox.downloader.core.Prefs

/** Shared colour tokens. Legacy property names keep the download screens source compatible. */
@Immutable
data class AuroraColors(
    val bg: Color, val surface: Color, val surfaceHigh: Color,
    val text: Color, val textDim: Color, val textFaint: Color,
    val glass: Color, val glassBorder: Color,
    val violet: Color, val cyan: Color, val magenta: Color,
    val success: Color, val danger: Color, val isDark: Boolean
) {
    val primary get() = violet
    val gradient get() = Brush.linearGradient(listOf(violet, cyan))
    val gradientWarm get() = Brush.linearGradient(listOf(magenta, cyan))
    val gradientSoft get() = Brush.linearGradient(listOf(violet.copy(alpha = .13f), cyan.copy(alpha = .035f)))
}
private val Dark = AuroraColors(
    Color(0xFF0A1014), Color(0xFF121D23), Color(0xFF1D2A30),
    Color(0xFFF4F4EB), Color(0xFFA2AFB2), Color(0xFF7D9297),
    Color(0xFF121D23), Color(0xFF2B3C42),
    Color(0xFFB6F5C5), Color(0xFF7EE0CC), Color(0xFFBDB4EF),
    Color(0xFFB6F5C5), Color(0xFFFFA3A3), true
)
private val Light = AuroraColors(
    Color(0xFFF2F6F0), Color(0xFFFFFFFF), Color(0xFFE5EDE6),
    Color(0xFF102621), Color(0xFF52675F), Color(0xFF61776F),
    Color(0xFFFFFFFF), Color(0xFFD6E2D9),
    Color(0xFF176548), Color(0xFF167565), Color(0xFF72659F),
    Color(0xFF176548), Color(0xFFB63D43), false
)
val LocalAurora = staticCompositionLocalOf { Dark }
private val Typography = Typography(
    displaySmall = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.ExtraBold, fontSize = 34.sp, lineHeight = 37.sp, letterSpacing = (-1.3).sp),
    headlineMedium = TextStyle(fontWeight = FontWeight.Bold, fontSize = 29.sp, lineHeight = 34.sp, letterSpacing = (-.9).sp),
    titleLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 21.sp, lineHeight = 27.sp, letterSpacing = (-.5).sp),
    titleMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 22.sp, letterSpacing = (-.25).sp),
    bodyLarge = TextStyle(fontSize = 15.sp, lineHeight = 23.sp),
    bodyMedium = TextStyle(fontSize = 14.sp, lineHeight = 21.sp),
    bodySmall = TextStyle(fontSize = 12.sp, lineHeight = 18.sp),
    labelLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = (-.1).sp),
    labelSmall = TextStyle(fontWeight = FontWeight.Medium, fontSize = 11.sp, lineHeight = 16.sp, letterSpacing = .35.sp)
)
@Composable
fun ZyroxTheme(mode: Prefs.ThemeMode = Prefs.ThemeMode.System, content: @Composable () -> Unit) {
    val dark = when (mode) { Prefs.ThemeMode.Dark -> true; Prefs.ThemeMode.Light -> false; else -> isSystemInDarkTheme() }
    val a = if (dark) Dark else Light
    val scheme = if (dark) darkColorScheme(
        primary = a.primary, onPrimary = Color(0xFF102B20), primaryContainer = Color(0xFF203D31), onPrimaryContainer = a.primary,
        secondary = a.cyan, onSecondary = Color(0xFF0C2F2A), secondaryContainer = a.surfaceHigh, onSecondaryContainer = a.cyan,
        tertiary = a.magenta, onTertiary = Color(0xFF29233D),
        background = a.bg, onBackground = a.text, surface = a.surface, onSurface = a.text,
        surfaceVariant = a.surfaceHigh, onSurfaceVariant = a.textDim, outline = a.glassBorder, error = a.danger
    ) else lightColorScheme(
        primary = a.primary, onPrimary = Color.White, primaryContainer = Color(0xFFD7EFDC), onPrimaryContainer = a.text,
        secondary = a.cyan, secondaryContainer = Color(0xFFD9EEE5), onSecondaryContainer = a.primary,
        tertiary = a.magenta,
        background = a.bg, onBackground = a.text, surface = a.surface, onSurface = a.text,
        surfaceVariant = a.surfaceHigh, onSurfaceVariant = a.textDim, outline = a.glassBorder, error = a.danger
    )
    CompositionLocalProvider(LocalAurora provides a) {
        MaterialTheme(colorScheme = scheme, typography = Typography,
            shapes = Shapes(extraSmall = RoundedCornerShape(8.dp), small = RoundedCornerShape(12.dp), medium = RoundedCornerShape(16.dp), large = RoundedCornerShape(22.dp), extraLarge = RoundedCornerShape(28.dp)), content = content)
    }
}
