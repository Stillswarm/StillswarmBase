package com.stillswarm.base.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import com.stillswarm.base.ui.Dimens

enum class CardLayout {
    Vertical,
    Horizontal,
}

/** A null onClick creates a non-interactive card. Caller owns content and its semantics. */
@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    layout: CardLayout = CardLayout.Vertical,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    leadingContent: (@Composable () -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null,
    shape: Shape = RoundedCornerShape(Dimens.medium),
    border: BorderStroke? =
        BorderStroke(Dimens.borderWidth, MaterialTheme.colorScheme.outlineVariant),
    content: @Composable ColumnScope.() -> Unit,
) {
    val body: @Composable () -> Unit = {
        if (layout == CardLayout.Vertical) {
            Column(
                modifier = Modifier.padding(Dimens.medium),
                verticalArrangement = Arrangement.spacedBy(Dimens.small),
                content = {
                    leadingContent?.invoke()
                    content()
                    trailingContent?.invoke()
                },
            )
        } else {
            Row(
                modifier = Modifier.padding(Dimens.medium),
                horizontalArrangement = Arrangement.spacedBy(Dimens.small),
                verticalAlignment = Alignment.CenterVertically,
                content = {
                    leadingContent?.invoke()
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(Dimens.xsmall),
                        content = content,
                    )
                    trailingContent?.invoke()
                },
            )
        }
    }
    if (onClick == null)
        Surface(
            modifier = modifier,
            shape = shape,
            border = border,
            content = body,
        )
    else
        Surface(
            onClick = onClick,
            enabled = enabled,
            modifier = modifier,
            shape = shape,
            border = border,
            content = body,
        )
}

@Composable
fun VLCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    leadingContent: (@Composable () -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null,
    shape: Shape = RoundedCornerShape(Dimens.medium),
    border: BorderStroke? =
        BorderStroke(Dimens.borderWidth, MaterialTheme.colorScheme.outlineVariant),
    content: @Composable ColumnScope.() -> Unit,
) =
    AppCard(
        modifier = modifier,
        layout = CardLayout.Vertical,
        onClick = onClick,
        enabled = enabled,
        leadingContent = leadingContent,
        trailingContent = trailingContent,
        shape = shape,
        border = border,
        content = content,
    )

@Composable
fun HLCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    leadingContent: (@Composable () -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null,
    shape: Shape = RoundedCornerShape(Dimens.medium),
    border: BorderStroke? =
        BorderStroke(Dimens.borderWidth, MaterialTheme.colorScheme.outlineVariant),
    content: @Composable ColumnScope.() -> Unit,
) =
    AppCard(
        modifier = modifier,
        layout = CardLayout.Horizontal,
        onClick = onClick,
        enabled = enabled,
        leadingContent = leadingContent,
        trailingContent = trailingContent,
        shape = shape,
        border = border,
        content = content,
    )
