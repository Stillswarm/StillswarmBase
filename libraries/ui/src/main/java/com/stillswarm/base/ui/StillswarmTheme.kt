package com.stillswarm.base.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf

private val LocalStillswarmDarkTheme = staticCompositionLocalOf<Boolean?> { null }

/** App theme with explicit overrides. Wallpaper-derived dynamic colours are never used. */
@Composable
fun StillswarmTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    lightColorScheme: ColorScheme = StillswarmColors.Light,
    darkColorScheme: ColorScheme = StillswarmColors.Dark,
    typography: Typography = StillswarmTypography,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalStillswarmDarkTheme provides darkTheme) {
        MaterialTheme(
            colorScheme = if (darkTheme) darkColorScheme else lightColorScheme,
            typography = typography,
            content = content,
        )
    }
}

/** Wrap shared Settings/About/Privacy screens to reset app branding, preserving light/dark mode. */
@Composable
fun StillswarmBaseTheme(
    darkTheme: Boolean = LocalStillswarmDarkTheme.current ?: isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    StillswarmTheme(darkTheme = darkTheme, content = content)
}
