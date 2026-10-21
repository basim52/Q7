package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

private val IndustrialDarkColorScheme = darkColorScheme(
    primary = LaserCyan400,
    onPrimary = Slate950,
    primaryContainer = LaserCyan900,
    onPrimaryContainer = LaserCyan400,
    secondary = SparkOrange500,
    onSecondary = Color.White,
    secondaryContainer = SparkOrange900,
    onSecondaryContainer = SparkOrange400,
    tertiary = EmeraldSuccess,
    onTertiary = Slate950,
    background = Slate950,
    onBackground = Slate50,
    surface = Slate900,
    onSurface = Slate100,
    surfaceVariant = Slate800,
    onSurfaceVariant = Slate400,
    outline = Slate700,
    outlineVariant = Slate800,
    error = CutLineRed,
    onError = Color.White
)

private val IndustrialLightColorScheme = lightColorScheme(
    primary = LaserCyan600,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFCFFAFE),
    onPrimaryContainer = LaserCyan900,
    secondary = SparkOrange600,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFEDD5),
    onSecondaryContainer = SparkOrange900,
    tertiary = Color(0xFF059669),
    onTertiary = Color.White,
    background = Slate100,
    onBackground = Slate900,
    surface = Color.White,
    onSurface = Slate900,
    surfaceVariant = Slate200,
    onSurfaceVariant = Slate600,
    outline = Color(0xFFCBD5E1),
    outlineVariant = Slate200,
    error = CutLineRed,
    onError = Color.White
)

val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(22.dp)
)

@Composable
fun LaserForgeTheme(
    darkTheme: Boolean = true,
    isArabic: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) IndustrialDarkColorScheme else IndustrialLightColorScheme
    val layoutDirection = if (isArabic) LayoutDirection.Rtl else LayoutDirection.Ltr

    CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = buildAppTypography(isArabic),
            shapes = AppShapes,
            content = content
        )
    }
}
