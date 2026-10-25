package com.stillswarm.base.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import com.stillswarm.base.ui.Dimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBottomSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null,
    shape: Shape = RoundedCornerShape(topStart = Dimens.xlarge, topEnd = Dimens.xlarge),
    border: BorderStroke? = null,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    content: @Composable () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier =
            if (border == null) modifier else modifier.border(border = border, shape = shape),
        sheetState = sheetState,
        shape = shape,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = Dimens.none,
        content = {
            Column(
                modifier =
                    Modifier.fillMaxWidth()
                        .padding(
                            start = Dimens.xlarge,
                            end = Dimens.xlarge,
                            bottom = Dimens.xlarge,
                        ),
                verticalArrangement = Arrangement.spacedBy(Dimens.medium),
                content = {
                    if (title != null)
                        Text(text = title, style = MaterialTheme.typography.titleMedium)
                    content()
                },
            )
        },
    )
}
