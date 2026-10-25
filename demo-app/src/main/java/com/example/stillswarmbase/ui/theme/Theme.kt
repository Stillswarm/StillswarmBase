package com.example.stillswarmbase.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import com.stillswarm.base.ui.StillswarmTheme

@Composable
fun StillswarmBaseTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) = StillswarmTheme(darkTheme = darkTheme, content = content)
