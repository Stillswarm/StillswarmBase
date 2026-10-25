package com.stillswarm.base.ui

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

object StillswarmFonts {
    val Heading = FontFamily(Font(resId = R.font.manrope_semibold, weight = FontWeight.SemiBold))
    val Body =
        FontFamily(
            Font(resId = R.font.inter_regular, weight = FontWeight.Normal),
            Font(resId = R.font.inter_medium, weight = FontWeight.Medium),
        )
}

/** Apps can replace font families while preserving the shared type scale. */
fun stillswarmTypography(
    headingFont: FontFamily = StillswarmFonts.Heading,
    bodyFont: FontFamily = StillswarmFonts.Body,
): Typography {
    fun heading(size: Int, lineHeight: Int) =
        TextStyle(
            fontFamily = headingFont,
            fontWeight = FontWeight.SemiBold,
            fontSize = size.sp,
            lineHeight = lineHeight.sp,
            letterSpacing = 0.sp,
        )
    fun body(size: Int, lineHeight: Int, weight: FontWeight = FontWeight.Normal) =
        TextStyle(
            fontFamily = bodyFont,
            fontWeight = weight,
            fontSize = size.sp,
            lineHeight = lineHeight.sp,
            letterSpacing = 0.sp,
        )
    return Typography(
        displayLarge = heading(size = 57, lineHeight = 64),
        displayMedium = heading(size = 45, lineHeight = 52),
        displaySmall = heading(size = 36, lineHeight = 44),
        headlineLarge = heading(size = 32, lineHeight = 40),
        headlineMedium = heading(size = 28, lineHeight = 36),
        headlineSmall = heading(size = 24, lineHeight = 32),
        titleLarge = heading(size = 24, lineHeight = 32),
        titleMedium = heading(size = 18, lineHeight = 26),
        titleSmall = heading(size = 16, lineHeight = 24),
        bodyLarge = body(size = 16, lineHeight = 24),
        bodyMedium = body(size = 14, lineHeight = 20),
        bodySmall = body(size = 12, lineHeight = 16),
        labelLarge = body(
            size = 14,
            lineHeight = 20,
            weight = FontWeight.Medium,
        ),
        labelMedium = body(
            size = 12,
            lineHeight = 16,
            weight = FontWeight.Medium,
        ),
        labelSmall = body(
            size = 11,
            lineHeight = 16,
            weight = FontWeight.Medium,
        ),
    )
}

val StillswarmTypography = stillswarmTypography()
