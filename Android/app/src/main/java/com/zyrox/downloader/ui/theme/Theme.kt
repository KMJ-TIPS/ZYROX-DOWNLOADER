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
    Color(0xFF080D1B), Color(0xFF111C32), Color(0xFF1B2B49),
    Color(0xFFF5F8FF), Color(0xFFA4B4D0), Color(0xFF8094B6),
    Color(0xFF111C32), Color(0xFF2D4167),
    Color(0xFF92B7FF), Color(0xFF67DFFF), Color(0xFFC6ADFF),
    Color(0xFF7DE6BE), Color(0xFFFFA3B6), true
)
private val Light = AuroraColors(
    Color(0xFFF2F6FF), Color(0xFFFFFFFF), Color(0xFFE4ECFA),
    Color(0xFF102044), Color(0xFF526889), Color(0xFF61799E),
    Color(0xFFFFFFFF), Color(0xFFCDDBF1),
    Color(0xFF245AD4), Color(0xFF157EA6), Color(0xFF8060BA),
    Color(0xFF168464), Color(0xFFB63D54), false
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
        primary = a.primary, onPrimary = Color(0xFF0B2149), primaryContainer = Color(0xFF233D68), onPrimaryContainer = a.primary,
        secondary = a.cyan, onSecondary = Color(0xFF073347), secondaryContainer = a.surfaceHigh, onSecondaryContainer = a.cyan,
        tertiary = a.magenta, onTertiary = Color(0xFF342047),
        background = a.bg, onBackground = a.text, surface = a.surface, onSurface = a.text,
        surfaceVariant = a.surfaceHigh, onSurfaceVariant = a.textDim, outline = a.glassBorder, error = a.danger
    ) else lightColorScheme(
        primary = a.primary, onPrimary = Color.White, primaryContainer = Color(0xFFDCE7FF), onPrimaryContainer = a.text,
        secondary = a.cyan, secondaryContainer = Color(0xFFD8EFFA), onSecondaryContainer = a.primary,
        tertiary = a.magenta,
        background = a.bg, onBackground = a.text, surface = a.surface, onSurface = a.text,
        surfaceVariant = a.surfaceHigh, onSurfaceVariant = a.textDim, outline = a.glassBorder, error = a.danger
    )
    CompositionLocalProvider(LocalAurora provides a) {
        MaterialTheme(colorScheme = scheme, typography = Typography,
            shapes = Shapes(extraSmall = RoundedCornerShape(8.dp), small = RoundedCornerShape(16.dp), medium = RoundedCornerShape(22.dp), large = RoundedCornerShape(28.dp), extraLarge = RoundedCornerShape(34.dp)), content = content)
    }
}
