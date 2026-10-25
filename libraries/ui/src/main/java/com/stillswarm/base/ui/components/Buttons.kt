package com.stillswarm.base.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.*
import com.stillswarm.base.ui.Dimens

/** Loading and disabled states never invoke onClick. */
enum class ButtonState {
    Enabled,
    Disabled,
    Loading,
}

@Composable
fun SuperButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    state: ButtonState = ButtonState.Enabled,
    loadingText: String = text,
    leadingIcon: (@Composable () -> Unit)? = null,
    shape: Shape = RoundedCornerShape(Dimens.small),
    border: BorderStroke? = null,
) {
    val loading = state == ButtonState.Loading
    val scheme = MaterialTheme.colorScheme
    Button(
        onClick = onClick,
        modifier = modifier.heightIn(min = Dimens.buttonHeight),
        enabled = state == ButtonState.Enabled,
        shape = shape,
        border = border,
        contentPadding = PaddingValues(horizontal = Dimens.xlarge, vertical = Dimens.small),
        colors =
            ButtonDefaults.buttonColors(
                disabledContainerColor =
                    if (loading) scheme.surfaceContainerHighest
                    else scheme.onSurface.copy(alpha = .12f),
                disabledContentColor =
                    if (loading) scheme.onSurfaceVariant
                    else scheme.onSurface.copy(alpha = .38f),
            ),
        content = {
            ButtonContent(
                text = text,
                loadingText = loadingText,
                loading = loading,
                icon = leadingIcon,
            )
        },
    )
}

@Composable
fun InlineButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    state: ButtonState = ButtonState.Enabled,
    loadingText: String = text,
    leadingIcon: (@Composable () -> Unit)? = null,
    shape: Shape = RoundedCornerShape(Dimens.small),
    border: BorderStroke? = null,
) {
    TextButton(
        onClick = onClick,
        modifier = modifier.heightIn(min = Dimens.touchTargetSize),
        enabled = state == ButtonState.Enabled,
        shape = shape,
        border = border,
        contentPadding = PaddingValues(Dimens.small),
        colors =
            ButtonDefaults.textButtonColors(
                disabledContentColor =
                    if (state == ButtonState.Loading) MaterialTheme.colorScheme.onSurfaceVariant
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = .38f)
            ),
        content = {
            ButtonContent(
                text = text,
                loadingText = loadingText,
                loading = state == ButtonState.Loading,
                icon = leadingIcon,
            )
        },
    )
}

/** Both labels participate in measurement, so a state transition doesn't resize the button. */
@Composable
private fun ButtonContent(
    text: String,
    loadingText: String,
    loading: Boolean,
    icon: (@Composable () -> Unit)?,
) {
    Box(
        contentAlignment = Alignment.Center,
        content = {
            Row(
                modifier = if (loading) Modifier.alpha(0f).clearAndSetSemantics {} else Modifier,
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimens.xsmall),
                content = {
                    if (icon != null)
                        Box(
                            modifier = Modifier.size(Dimens.large),
                            contentAlignment = Alignment.Center,
                            content = { icon() },
                        )
                    Text(text = text, style = MaterialTheme.typography.labelLarge)
                },
            )
            Row(
                modifier = if (!loading) Modifier.alpha(0f).clearAndSetSemantics {} else Modifier,
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimens.xsmall),
                content = {
                    if (loading)
                        CircularProgressIndicator(
                            modifier = Modifier.size(Dimens.progressSize),
                            color = LocalContentColor.current,
                            strokeWidth = Dimens.progressStrokeWidth,
                        )
                    else Spacer(Modifier.size(Dimens.progressSize))
                    Text(text = loadingText, style = MaterialTheme.typography.labelLarge)
                },
            )
        },
    )
}
