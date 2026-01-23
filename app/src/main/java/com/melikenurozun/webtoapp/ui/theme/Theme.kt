package com.melikenurozun.webtoapp.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryPurple,
    onPrimary = Color.White,
    primaryContainer = PrimaryPurpleDark,
    onPrimaryContainer = Color.White,
    
    secondary = SecondaryPink,
    onSecondary = Color.White,
    secondaryContainer = SecondaryPinkDark,
    onSecondaryContainer = Color.White,
    
    tertiary = TertiaryTeal,
    onTertiary = Color.Black,
    tertiaryContainer = TertiaryTealDark,
    onTertiaryContainer = Color.White,
    
    background = BackgroundDark,
    onBackground = TextPrimaryDark,
    
    surface = SurfaceDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = TextSecondaryDark,
    
    error = ErrorRed,
    onError = Color.White,
    
    outline = Color(0xFF4A4A6A)
)

// AMOLED True Black Theme
private val AmoledColorScheme = darkColorScheme(
    primary = PrimaryPurple,
    onPrimary = Color.White,
    primaryContainer = PrimaryPurpleDark,
    onPrimaryContainer = Color.White,
    
    secondary = SecondaryPink,
    onSecondary = Color.White,
    secondaryContainer = SecondaryPinkDark,
    onSecondaryContainer = Color.White,
    
    tertiary = TertiaryTeal,
    onTertiary = Color.Black,
    tertiaryContainer = TertiaryTealDark,
    onTertiaryContainer = Color.White,
    
    background = BackgroundAmoled,  // Pure black
    onBackground = TextPrimaryDark,
    
    surface = SurfaceAmoled,        // Near black
    onSurface = TextPrimaryDark,
    surfaceVariant = SurfaceVariantAmoled,
    onSurfaceVariant = TextSecondaryDark,
    
    error = ErrorRed,
    onError = Color.White,
    
    outline = Color(0xFF2A2A2A)
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryPurple,
    onPrimary = Color.White,
    primaryContainer = PrimaryPurpleLight,
    onPrimaryContainer = PrimaryPurpleDark,
    
    secondary = SecondaryPink,
    onSecondary = Color.White,
    secondaryContainer = SecondaryPinkLight,
    onSecondaryContainer = SecondaryPinkDark,
    
    tertiary = TertiaryTeal,
    onTertiary = Color.White,
    tertiaryContainer = TertiaryTealLight,
    onTertiaryContainer = TertiaryTealDark,
    
    background = BackgroundLight,
    onBackground = TextPrimaryLight,
    
    surface = SurfaceLight,
    onSurface = TextPrimaryLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = TextSecondaryLight,
    
    error = ErrorRed,
    onError = Color.White,
    
    outline = Color(0xFFD0D0E0)
)

@Composable
fun WebtoAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    amoledMode: Boolean = false, // New parameter for AMOLED
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme && amoledMode -> AmoledColorScheme  // AMOLED mode
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = if (amoledMode && darkTheme) Color.Black.toArgb() else colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}