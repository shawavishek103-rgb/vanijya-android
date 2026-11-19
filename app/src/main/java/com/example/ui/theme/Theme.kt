package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.example.core.database.ThemeMode

@Immutable
data class VanijyaGlassColors(
    val isDark: Boolean,
    val isAmoled: Boolean,
    val bgGradientStart: Color,
    val bgGradientMid: Color,
    val bgGradientEnd: Color,
    val glowPrimary: Color,
    val glowSecondary: Color,
    val glowTertiary: Color,
    val cardFill: Color,
    val elevatedFill: Color,
    val borderStart: Color,
    val borderEnd: Color,
    val subtleSurface: Color
) {
    val backgroundGradientStart: Color get() = bgGradientStart
    val backgroundGradientMid: Color get() = bgGradientMid
    val backgroundGradientEnd: Color get() = bgGradientEnd
    val glowTopStart: Color get() = glowPrimary
    val glowTopEnd: Color get() = glowSecondary
    val glowBottomCenter: Color get() = glowTertiary
    val cardFillElevated: Color get() = elevatedFill
    val borderGradientStart: Color get() = borderStart
    val borderGradientEnd: Color get() = borderEnd
    val pillFill: Color get() = subtleSurface
    val subtleDivider: Color get() = borderEnd
    val shadowColor: Color get() = if (isDark) Color(0x55000000) else Color(0x1F1E293B)
}

val LocalVanijyaGlass = staticCompositionLocalOf {
    VanijyaGlassColors(
        isDark = true,
        isAmoled = false,
        bgGradientStart = DarkBackground,
        bgGradientMid = DarkGradientMid,
        bgGradientEnd = DarkGradientEnd,
        glowPrimary = Color(0xFF4F46E5).copy(alpha = 0.28f),
        glowSecondary = EmeraldPrimary.copy(alpha = 0.20f),
        glowTertiary = AmberSale.copy(alpha = 0.14f),
        cardFill = GlassFillDark,
        elevatedFill = GlassFillDarkElevated,
        borderStart = GlassBorderDarkStart,
        borderEnd = GlassBorderDarkEnd,
        subtleSurface = Color(0x1FFFFFFF)
    )
}

private val LightColorScheme = lightColorScheme(
    primary = VioletPrimary,
    onPrimary = Color.White,
    primaryContainer = VioletSoftContainerLight,
    onPrimaryContainer = IndigoDeep,
    secondary = EmeraldPrimary,
    onSecondary = Color.White,
    secondaryContainer = EmeraldContainerLight,
    onSecondaryContainer = Color(0xFF053826),
    tertiary = GoldPrimary,
    onTertiary = Color(0xFF281B00),
    tertiaryContainer = GoldContainerLight,
    onTertiaryContainer = Color(0xFF3A2800),
    error = CoralError,
    onError = Color.White,
    errorContainer = CoralContainerLight,
    onErrorContainer = Color(0xFF4A0E0E),
    background = LightBackground,
    onBackground = LightOnSurface,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    surfaceDim = Color(0xFFE8ECF4),
    surfaceBright = Color(0xFFFFFFFF),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF8F9FC),
    surfaceContainer = Color(0xFFF1F5F9),
    surfaceContainerHigh = Color(0xFFEAEFF7),
    surfaceContainerHighest = Color(0xFFE2E8F0),
    outline = Color(0xFF94A3B8),
    outlineVariant = Color(0xFFCBD5E1)
)

private val DarkColorScheme = darkColorScheme(
    primary = VioletBright,
    onPrimary = Color(0xFF140C3D),
    primaryContainer = VioletSoftContainerDark,
    onPrimaryContainer = Color(0xFFE5DEFF),
    secondary = EmeraldBright,
    onSecondary = Color(0xFF003824),
    secondaryContainer = EmeraldContainerDark,
    onSecondaryContainer = Color(0xFFB5F7DC),
    tertiary = GoldBright,
    onTertiary = Color(0xFF3B2800),
    tertiaryContainer = GoldContainerDark,
    onTertiaryContainer = Color(0xFFFFE4A8),
    error = CoralBright,
    onError = Color(0xFF470A0A),
    errorContainer = CoralContainerDark,
    onErrorContainer = Color(0xFFFFD8D8),
    background = DarkBackground,
    onBackground = DarkOnSurface,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    surfaceDim = Color(0xFF0B0E20),
    surfaceBright = Color(0xFF252D54),
    surfaceContainerLowest = Color(0xFF090C1C),
    surfaceContainerLow = Color(0xFF121731),
    surfaceContainer = Color(0xFF161C3A),
    surfaceContainerHigh = Color(0xFF1D2448),
    surfaceContainerHighest = Color(0xFF252E5A),
    outline = Color(0xFF64748B),
    outlineVariant = Color(0xFF3B4578)
)

private val AmoledColorScheme = DarkColorScheme.copy(
    background = AmoledBackground,
    onBackground = DarkOnSurface,
    surface = AmoledSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = AmoledSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    surfaceDim = Color(0xFF000000),
    surfaceBright = Color(0xFF1A203C),
    surfaceContainerLowest = Color(0xFF000000),
    surfaceContainerLow = Color(0xFF070A16),
    surfaceContainer = Color(0xFF0C1022),
    surfaceContainerHigh = Color(0xFF121830),
    surfaceContainerHighest = Color(0xFF192140),
    outline = Color(0xFF64748B),
    outlineVariant = Color(0xFF2D355E)
)

val VanijyaShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(22.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun VanijyaTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val systemDark = isSystemInDarkTheme()
    val effectiveMode = when (themeMode) {
        ThemeMode.SYSTEM -> if (systemDark) ThemeMode.DARK else ThemeMode.LIGHT
        else -> themeMode
    }

    val colorScheme = when (effectiveMode) {
        ThemeMode.LIGHT -> LightColorScheme
        ThemeMode.DARK -> DarkColorScheme
        ThemeMode.AMOLED -> AmoledColorScheme
        ThemeMode.SYSTEM -> LightColorScheme
    }

    val glassColors = when (effectiveMode) {
        ThemeMode.LIGHT -> VanijyaGlassColors(
            isDark = false,
            isAmoled = false,
            bgGradientStart = LightBackground,
            bgGradientMid = LightGradientMid,
            bgGradientEnd = LightGradientEnd,
            glowPrimary = Color(0xFF93C5FD).copy(alpha = 0.44f),   // soft sky blue glow
            glowSecondary = Color(0xFF6EE7B7).copy(alpha = 0.38f), // soft mint green glow
            glowTertiary = Color(0xFFFDE68A).copy(alpha = 0.30f),  // warm cream/ivory glow
            cardFill = GlassFillLight,
            elevatedFill = GlassFillLightElevated,
            borderStart = GlassBorderLightStart,
            borderEnd = GlassBorderLightEnd,
            subtleSurface = Color(0xB3FFFFFF)
        )
        ThemeMode.AMOLED -> VanijyaGlassColors(
            isDark = true,
            isAmoled = true,
            bgGradientStart = AmoledBackground,
            bgGradientMid = AmoledBackground,
            bgGradientEnd = AmoledBackground,
            glowPrimary = VioletPrimary.copy(alpha = 0.16f),
            glowSecondary = EmeraldPrimary.copy(alpha = 0.12f),
            glowTertiary = AmberSale.copy(alpha = 0.08f),
            cardFill = GlassFillAmoled,
            elevatedFill = Color(0x33141B38),
            borderStart = GlassBorderAmoledStart,
            borderEnd = GlassBorderAmoledEnd,
            subtleSurface = Color(0x26FFFFFF)
        )
        else -> VanijyaGlassColors(
            isDark = true,
            isAmoled = false,
            bgGradientStart = DarkBackground,
            bgGradientMid = DarkGradientMid,
            bgGradientEnd = DarkGradientEnd,
            glowPrimary = Color(0xFF4F46E5).copy(alpha = 0.32f),
            glowSecondary = EmeraldPrimary.copy(alpha = 0.22f),
            glowTertiary = AmberSale.copy(alpha = 0.16f),
            cardFill = GlassFillDark,
            elevatedFill = GlassFillDarkElevated,
            borderStart = GlassBorderDarkStart,
            borderEnd = GlassBorderDarkEnd,
            subtleSurface = Color(0x29FFFFFF)
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            runCatching {
                val window = (view.context as? Activity)?.window ?: return@SideEffect
                val controller = WindowCompat.getInsetsController(window, view)
                val isLight = effectiveMode == ThemeMode.LIGHT
                controller.isAppearanceLightStatusBars = isLight
                controller.isAppearanceLightNavigationBars = isLight
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = VanijyaShapes
    ) {
        CompositionLocalProvider(
            LocalVanijyaGlass provides glassColors,
            LocalContentColor provides colorScheme.onSurface
        ) {
            content()
        }
    }
}
