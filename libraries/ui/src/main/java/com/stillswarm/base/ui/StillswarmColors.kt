package com.stillswarm.base.ui

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/** Neutral brand defaults. Error roles retain Material 3 defaults. */
object StillswarmColors {
    val Light =
        lightColorScheme(
            primary = Color(0xFF343434),
            onPrimary = Color.White,
            primaryContainer = Color(0xFFE5E5E5),
            onPrimaryContainer = Color(0xFF202020),
            secondary = Color(0xFF525252),
            onSecondary = Color.White,
            secondaryContainer = Color(0xFFEAEAEA),
            onSecondaryContainer = Color(0xFF202020),
            tertiary = Color(0xFF616161),
            onTertiary = Color.White,
            tertiaryContainer = Color(0xFFEDEDED),
            onTertiaryContainer = Color(0xFF202020),
            background = Color(0xFFFAFAFA),
            onBackground = Color(0xFF1C1C1C),
            surface = Color.White,
            onSurface = Color(0xFF1C1C1C),
            surfaceVariant = Color(0xFFF0F0F0),
            onSurfaceVariant = Color(0xFF616161),
            outline = Color(0xFF747474),
            outlineVariant = Color(0xFFE0E0E0),
            inverseSurface = Color(0xFF303030),
            inverseOnSurface = Color(0xFFF2F2F2),
            inversePrimary = Color(0xFFD6D6D6),
            surfaceTint = Color(0xFF343434),
            surfaceBright = Color.White,
            surfaceDim = Color(0xFFDDDDDD),
            surfaceContainerLowest = Color.White,
            surfaceContainerLow = Color(0xFFF7F7F7),
            surfaceContainer = Color(0xFFF2F2F2),
            surfaceContainerHigh = Color(0xFFECECEC),
            surfaceContainerHighest = Color(0xFFE6E6E6),
        )

    val Dark =
        darkColorScheme(
            primary = Color(0xFFD6D6D6),
            onPrimary = Color(0xFF202020),
            primaryContainer = Color(0xFF444444),
            onPrimaryContainer = Color(0xFFF2F2F2),
            secondary = Color(0xFFC6C6C6),
            onSecondary = Color(0xFF202020),
            secondaryContainer = Color(0xFF3C3C3C),
            onSecondaryContainer = Color(0xFFF2F2F2),
            tertiary = Color(0xFFBDBDBD),
            onTertiary = Color(0xFF202020),
            tertiaryContainer = Color(0xFF383838),
            onTertiaryContainer = Color(0xFFF2F2F2),
            background = Color(0xFF121212),
            onBackground = Color(0xFFF2F2F2),
            surface = Color(0xFF1E1E1E),
            onSurface = Color(0xFFF2F2F2),
            surfaceVariant = Color(0xFF303030),
            onSurfaceVariant = Color(0xFFBDBDBD),
            outline = Color(0xFF909090),
            outlineVariant = Color(0xFF383838),
            inverseSurface = Color(0xFFE5E5E5),
            inverseOnSurface = Color(0xFF202020),
            inversePrimary = Color(0xFF343434),
            surfaceTint = Color(0xFFD6D6D6),
            surfaceBright = Color(0xFF383838),
            surfaceDim = Color(0xFF121212),
            surfaceContainerLowest = Color(0xFF0D0D0D),
            surfaceContainerLow = Color(0xFF191919),
            surfaceContainer = Color(0xFF1E1E1E),
            surfaceContainerHigh = Color(0xFF282828),
            surfaceContainerHighest = Color(0xFF333333),
        )
}
