package com.stillswarm.base.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.Dp

/** Resource-backed spacing scale and purpose-specific component measurements. */
object Dimens {
    val xxsmall: Dp
        @Composable @ReadOnlyComposable get() = dimensionResource(id = R.dimen.stillswarm_xxsmall)

    val xsmall: Dp
        @Composable @ReadOnlyComposable get() = dimensionResource(id = R.dimen.stillswarm_xsmall)

    val small: Dp
        @Composable @ReadOnlyComposable get() = dimensionResource(id = R.dimen.stillswarm_small)

    val medium: Dp
        @Composable @ReadOnlyComposable get() = dimensionResource(id = R.dimen.stillswarm_medium)

    val large: Dp
        @Composable @ReadOnlyComposable get() = dimensionResource(id = R.dimen.stillswarm_large)

    val xlarge: Dp
        @Composable @ReadOnlyComposable get() = dimensionResource(id = R.dimen.stillswarm_xlarge)

    val xxlarge: Dp
        @Composable @ReadOnlyComposable get() = dimensionResource(id = R.dimen.stillswarm_xxlarge)

    val none: Dp
        @Composable @ReadOnlyComposable get() = dimensionResource(id = R.dimen.stillswarm_none)

    val borderWidth: Dp
        @Composable
        @ReadOnlyComposable
        get() = dimensionResource(id = R.dimen.stillswarm_border_width)

    val progressStrokeWidth: Dp
        @Composable
        @ReadOnlyComposable
        get() = dimensionResource(id = R.dimen.stillswarm_progress_stroke_width)

    val progressSize: Dp
        @Composable
        @ReadOnlyComposable
        get() = dimensionResource(id = R.dimen.stillswarm_progress_size)

    val dialogIconSize: Dp
        @Composable
        @ReadOnlyComposable
        get() = dimensionResource(id = R.dimen.stillswarm_dialog_icon_size)

    val touchTargetSize: Dp
        @Composable
        @ReadOnlyComposable
        get() = dimensionResource(id = R.dimen.stillswarm_touch_target_size)

    val buttonHeight: Dp
        @Composable
        @ReadOnlyComposable
        get() = dimensionResource(id = R.dimen.stillswarm_button_height)

    val fabSize: Dp
        @Composable @ReadOnlyComposable get() = dimensionResource(id = R.dimen.stillswarm_fab_size)

    val dialogMaxWidth: Dp
        @Composable
        @ReadOnlyComposable
        get() = dimensionResource(id = R.dimen.stillswarm_dialog_max_width)

    val fabContentClearance: Dp
        @Composable
        @ReadOnlyComposable
        get() = dimensionResource(id = R.dimen.stillswarm_fab_content_clearance)
}
