package com.stillswarm.base.ui

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

object StillswarmFonts {
    val Heading = FontFamily(Font(R.font.manrope_semibold, FontWeight.SemiBold))
    val Body = FontFamily(
        Font(R.font.inter_regular, FontWeight.Normal),
        Font(R.font.inter_medium, FontWeight.Medium),
    )
}

/** Apps can replace font families while preserving the shared type scale. */
fun stillswarmTypography(
    headingFont: FontFamily = StillswarmFonts.Heading,
    bodyFont: FontFamily = StillswarmFonts.Body,
): Typography {
    fun heading(size: Int, lineHeight: Int) = TextStyle(
        fontFamily = headingFont, fontWeight = FontWeight.SemiBold,
        fontSize = size.sp, lineHeight = lineHeight.sp, letterSpacing = 0.sp,
    )
    fun body(size: Int, lineHeight: Int, weight: FontWeight = FontWeight.Normal) = TextStyle(
        fontFamily = bodyFont, fontWeight = weight,
        fontSize = size.sp, lineHeight = lineHeight.sp, letterSpacing = 0.sp,
    )
    return Typography(
        displayLarge = heading(57, 64), displayMedium = heading(45, 52), displaySmall = heading(36, 44),
        headlineLarge = heading(32, 40), headlineMedium = heading(28, 36), headlineSmall = heading(24, 32),
        titleLarge = heading(24, 32), titleMedium = heading(18, 26), titleSmall = heading(16, 24),
        bodyLarge = body(16, 24), bodyMedium = body(14, 20), bodySmall = body(12, 16),
        labelLarge = body(14, 20, FontWeight.Medium), labelMedium = body(12, 16, FontWeight.Medium),
        labelSmall = body(11, 16, FontWeight.Medium),
    )
}

val StillswarmTypography = stillswarmTypography()
