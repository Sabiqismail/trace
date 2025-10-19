package com.example.trace.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// --- Brand colors (shared)
private val Copper = Color(0xFFCBA77C)        // accent
private val Success = Color(0xFF3AA981)
private val Destructive = Color(0xFFE06B65)

// --------- DARK (night-first) ---------
private val DarkBg = Color(0xFF0F1113)        // canvas
private val DarkSurface = Color(0xFF15181B)   // cards
private val DarkElevated = Color(0xFF1B1F23)  // sheets/dialogs
private val DarkOn = Color(0xFFE8E6E3)        // primary text
private val DarkOnSecondary = Color(0xFFB8B4AE) // secondary text
private val DarkMuted = Color(0xFF8E8A84)     // dates / hint
private val DarkDivider = Color(0xFF2A2E31)   // hairlines

private val DarkColors: ColorScheme = darkColorScheme(
    // Material roles mapped to our guide
    primary = Copper,                      // used for accents
    onPrimary = DarkBg,                    // text on copper
    secondary = Success,                   // we use sparingly
    onSecondary = DarkOn,
    error = Destructive,
    onError = DarkBg,
    background = DarkBg,
    onBackground = DarkOn,
    surface = DarkSurface,
    onSurface = DarkOn,
    surfaceVariant = DarkElevated,         // elevated surfaces
    onSurfaceVariant = DarkOnSecondary,
    outline = DarkDivider,                 // dividers / trace line base
)

// --------- LIGHT (keep your current feel; minor mapping) ---------
private val LightBg = Color(0xFFF8F5F2)      // linen
private val LightOn = Color(0xFF3D3C3A)      // charcoal
private val LightDivider = Color(0x1A000000) // ~10% black

private val LightColors: ColorScheme = lightColorScheme(
    primary = Copper,
    onPrimary = Color.White,
    secondary = Success,
    onSecondary = Color.White,
    error = Destructive,
    onError = Color.White,
    background = LightBg,
    onBackground = LightOn,
    surface = LightBg,
    onSurface = LightOn,
    surfaceVariant = Color(0xFFEFECE9),
    onSurfaceVariant = LightOn.copy(alpha = 0.8f),
    outline = Color(0xFFDDDDDD).copy(alpha = 0.6f)
)

@Composable
fun TraceTheme(
    useDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (useDarkTheme) DarkColors else LightColors

    // NOTE:
    // - In dark mode, use copper with ~80–90% opacity if you place it on text-heavy surfaces.
    //   (You can do .copy(alpha = 0.85f) where needed in widgets.)
    // - For dividers/trace lines, prefer MaterialTheme.colorScheme.outline.

    MaterialTheme(
        colorScheme = colors,
        typography = Typography,  // or whatever name is used in your Type.kt
        content = content
    )
}