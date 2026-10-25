package com.stillswarm.base.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.stillswarm.base.ui.Dimens
import com.stillswarm.base.ui.R

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AppDialog(
    title: String,
    confirmText: String,
    onConfirm: () -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    dismissText: String = stringResource(R.string.stillswarm_cancel),
    destructive: Boolean = false,
    confirmState: ButtonState = ButtonState.Enabled,
    icon: (@Composable () -> Unit)? = null,
    properties: DialogProperties = DialogProperties(),
    shape: Shape = RoundedCornerShape(Dimens.xlarge),
    border: BorderStroke? = null,
    actionShape: Shape = RoundedCornerShape(Dimens.small),
    content: @Composable () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = properties,
        content = {
            Surface(
                modifier = modifier.widthIn(max = Dimens.dialogMaxWidth),
                shape = shape,
                border = border,
                tonalElevation = Dimens.none,
                content = {
                    Column(
                        modifier = Modifier.padding(Dimens.xlarge),
                        verticalArrangement = Arrangement.spacedBy(Dimens.medium),
                        content = {
                            if (icon != null) {
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.Center,
                                    content = {
                                        Box(
                                            modifier = Modifier.size(Dimens.dialogIconSize),
                                            contentAlignment = Alignment.Center,
                                            content = { icon() },
                                        )
                                    },
                                )
                            }
                            Text(text = title, style = MaterialTheme.typography.titleLarge)
                            ProvideTextStyle(MaterialTheme.typography.bodyMedium) {
                                CompositionLocalProvider(
                                    LocalContentColor provides
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                ) {
                                    content()
                                }
                            }
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement =
                                    Arrangement.spacedBy(Dimens.xsmall, Alignment.End),
                                verticalArrangement = Arrangement.spacedBy(Dimens.xsmall),
                                content = {
                                    TextButton(
                                        onClick = onDismissRequest,
                                        shape = actionShape,
                                        contentPadding =
                                            PaddingValues(
                                                horizontal = Dimens.small,
                                                vertical = Dimens.xsmall,
                                            ),
                                        content = { Text(text = dismissText) },
                                    )
                                    val scheme = MaterialTheme.colorScheme
                                    val background =
                                        if (destructive) scheme.error else scheme.primary
                                    val foreground =
                                        if (destructive) scheme.onError else scheme.onPrimary
                                    Button(
                                        onClick = onConfirm,
                                        enabled = confirmState == ButtonState.Enabled,
                                        shape = actionShape,
                                        contentPadding =
                                            PaddingValues(
                                                horizontal = Dimens.small,
                                                vertical = Dimens.xsmall,
                                            ),
                                        colors =
                                            ButtonDefaults.buttonColors(
                                                containerColor = background,
                                                contentColor = foreground,
                                                disabledContainerColor =
                                                    if (confirmState == ButtonState.Loading)
                                                        background
                                                    else scheme.onSurface.copy(alpha = .12f),
                                                disabledContentColor =
                                                    if (confirmState == ButtonState.Loading)
                                                        foreground
                                                    else scheme.onSurface.copy(alpha = .38f),
                                            ),
                                        content = {
                                            if (confirmState == ButtonState.Loading) {
                                                CircularProgressIndicator(
                                                    modifier = Modifier.size(Dimens.progressSize),
                                                    color = LocalContentColor.current,
                                                    strokeWidth = Dimens.progressStrokeWidth,
                                                )
                                                Spacer(Modifier.width(Dimens.xsmall))
                                            }
                                            Text(text = confirmText)
                                        },
                                    )
                                },
                            )
                        },
                    )
                },
            )
        },
    )
}
