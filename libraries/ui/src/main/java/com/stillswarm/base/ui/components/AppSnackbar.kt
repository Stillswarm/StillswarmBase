package com.stillswarm.base.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.stringResource
import com.stillswarm.base.ui.Dimens
import com.stillswarm.base.ui.R

@Composable
fun AppSnackbar(
    message: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    dismissLabel: String = stringResource(R.string.stillswarm_dismiss_notification),
    shape: Shape = RoundedCornerShape(Dimens.small),
    border: BorderStroke? = null,
) {
    Snackbar(
        modifier =
            if (border == null) modifier else modifier.border(border = border, shape = shape),
        shape = shape,
        containerColor = MaterialTheme.colorScheme.inverseSurface,
        contentColor = MaterialTheme.colorScheme.inverseOnSurface,
        dismissActionContentColor = MaterialTheme.colorScheme.inverseOnSurface,
        dismissAction = {
            IconButton(
                onClick = onDismiss,
                content = {
                    Icon(imageVector = Icons.Rounded.Close, contentDescription = dismissLabel)
                },
            )
        },
    ) {
        Text(text = message, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun AppSnackbarHost(hostState: SnackbarHostState, modifier: Modifier = Modifier) {
    SnackbarHost(
        hostState = hostState,
        modifier = modifier,
        snackbar = { data ->
            AppSnackbar(
                message = data.visuals.message,
                onDismiss = data::dismiss,
                modifier = Modifier.padding(Dimens.small),
            )
        },
    )
}

suspend fun SnackbarHostState.showAppSnackbar(
    message: String,
    duration: SnackbarDuration = SnackbarDuration.Short,
): SnackbarResult =
    showSnackbar(
        message = message,
        actionLabel = null,
        withDismissAction = true,
        duration = duration,
    )
