package com.signalnest.app.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat

private val Blue400  = Color(0xFF60A5FA)
private val Blue600  = Color(0xFF2563EB)
private val Bg0      = Color(0xFF0E0E12)
private val Bg1      = Color(0xFF16161C)
private val Bg2      = Color(0xFF1E1E26)
private val OnSurf   = Color(0xFFE4E4EF)
private val Subtle   = Color(0xFF8E8EA0)

private val DarkColors = darkColorScheme(
    primary          = Blue400,
    onPrimary        = Color(0xFF001040),
    primaryContainer = Color(0xFF0D1E3E),
    secondary        = Color(0xFF7C7CFF),
    background       = Bg0,
    surface          = Bg1,
    surfaceVariant   = Bg2,
    onBackground     = OnSurf,
    onSurface        = OnSurf,
    onSurfaceVariant = Subtle,
    outline          = Color(0xFF3A3A4A),
    error            = Color(0xFFFF6B6B),
)

private val AmoledColors = DarkColors.copy(
    background     = Color.Black,
    surface        = Color(0xFF080810),
    surfaceVariant = Color(0xFF12121A),
)

private val LightColors = lightColorScheme(
    primary          = Blue600,
    onPrimary        = Color.White,
    primaryContainer = Color(0xFFDCEAFF),
    background       = Color(0xFFF4F6FF),
    surface          = Color.White,
    surfaceVariant   = Color(0xFFEEF0FF),
    onBackground     = Color(0xFF0E0E20),
    onSurface        = Color(0xFF0E0E20),
    onSurfaceVariant = Color(0xFF505070),
    outline          = Color(0xFFBBBBDD),
)

val SNTypography = Typography(
    headlineLarge  = TextStyle(fontWeight = FontWeight.Bold,     fontSize = 28.sp, lineHeight = 36.sp),
    headlineMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 22.sp, lineHeight = 28.sp),
    titleLarge     = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 18.sp, lineHeight = 24.sp),
    titleMedium    = TextStyle(fontWeight = FontWeight.Medium,   fontSize = 15.sp, lineHeight = 20.sp),
    titleSmall     = TextStyle(fontWeight = FontWeight.Medium,   fontSize = 13.sp, lineHeight = 18.sp),
    bodyLarge      = TextStyle(fontWeight = FontWeight.Normal,   fontSize = 15.sp, lineHeight = 22.sp),
    bodyMedium     = TextStyle(fontWeight = FontWeight.Normal,   fontSize = 13.sp, lineHeight = 18.sp),
    bodySmall      = TextStyle(fontWeight = FontWeight.Normal,   fontSize = 11.sp, lineHeight = 16.sp),
    labelSmall     = TextStyle(fontWeight = FontWeight.Medium,   fontSize = 10.sp, lineHeight = 14.sp, letterSpacing = 0.8.sp),
)

@Composable
fun SignalNestTheme(mode: String = "SYSTEM", amoled: Boolean = false, content: @Composable () -> Unit) {
    val isDark = when (mode) {
        "DARK"  -> true
        "LIGHT" -> false
        else    -> isSystemInDarkTheme()
    }
    val colors = when {
        isDark && amoled                           -> AmoledColors
        isDark && Build.VERSION.SDK_INT >= 31      -> dynamicDarkColorScheme(LocalContext.current)
        isDark                                     -> DarkColors
        !isDark && Build.VERSION.SDK_INT >= 31     -> dynamicLightColorScheme(LocalContext.current)
        else                                       -> LightColors
    }

    val view = LocalView.current
    if (!view.isInEditMode) SideEffect {
        val win = (view.context as Activity).window
        win.statusBarColor = colors.background.toArgb()
        WindowCompat.getInsetsController(win, view).isAppearanceLightStatusBars = !isDark
    }

    MaterialTheme(colorScheme = colors, typography = SNTypography, content = content)
}
