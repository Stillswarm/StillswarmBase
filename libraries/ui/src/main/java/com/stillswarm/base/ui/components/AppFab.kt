package com.stillswarm.base.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.stillswarm.base.ui.Dimens

/** Omit label for an icon-only FAB. Supply a meaningful accessible label in either form. */
@Composable
fun AppFab(
    onClick: () -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier,
    label: String? = null,
    shape: Shape = RoundedCornerShape(Dimens.medium),
    border: BorderStroke? = null,
    icon: @Composable () -> Unit,
) {
    FloatingActionButton(
        onClick = onClick,
        modifier =
            (if (border == null) modifier else modifier.border(border = border, shape = shape))
                .semantics { this.contentDescription = contentDescription },
        shape = shape,
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        elevation =
            FloatingActionButtonDefaults.elevation(
                defaultElevation = Dimens.none,
                pressedElevation = Dimens.none,
                focusedElevation = Dimens.none,
                hoveredElevation = Dimens.none,
            ),
        content = {
            Row(
                modifier =
                    Modifier.defaultMinSize(minWidth = Dimens.fabSize, minHeight = Dimens.fabSize)
                        .padding(horizontal = Dimens.medium),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement =
                    Arrangement.spacedBy(Dimens.small, Alignment.CenterHorizontally),
                content = {
                    Box(
                        modifier = Modifier.size(Dimens.xlarge),
                        contentAlignment = Alignment.Center,
                        content = { icon() },
                    )
                    if (label != null)
                        Text(text = label, style = MaterialTheme.typography.labelLarge)
                },
            )
        },
    )
}
