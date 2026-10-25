package com.stillswarm.base.shell

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.stillswarm.base.ui.components.AppSnackbarHost

/** Provides a root snackbar host; features remain responsible for business state. */
@Composable
fun StillswarmAppShell(
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    topBar: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        snackbarHost = { AppSnackbarHost(hostState = snackbarHostState) },
        topBar = topBar,
        floatingActionButton = floatingActionButton,
        content = content,
    )
}
