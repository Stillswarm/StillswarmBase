package com.example.stillswarmbase

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.stillswarm.base.billing.CheckoutState
import com.stillswarm.base.shell.StillswarmAppShell
import com.stillswarm.base.testing.FakeBilling
import com.stillswarm.base.ui.Dimens
import com.stillswarm.base.ui.StillswarmBaseTheme
import com.stillswarm.base.ui.components.*
import kotlinx.coroutines.launch

internal class BillingDemoViewModel : ViewModel() {
    val billing = FakeBilling()

    init {
        viewModelScope.launch { billing.refresh() }
    }

    fun buy() {
        viewModelScope.launch { billing.purchase() }
    }

    fun restore() {
        viewModelScope.launch { billing.refresh() }
    }

    fun complete() {
        viewModelScope.launch { billing.completePending() }
    }

    fun revoke() {
        viewModelScope.launch { billing.revoke() }
    }
}

@Composable
internal fun BillingDemoScreen(onBack: () -> Unit, onPlayBilling: () -> Unit) {
    val model = viewModel<BillingDemoViewModel>()
    val state by model.billing.state.collectAsStateWithLifecycle()
    var outcome by remember { mutableStateOf(FakeBilling.Outcome.Success) }
    var offline by remember { mutableStateOf(false) }
    StillswarmBaseTheme {
        StillswarmAppShell(
            topBar = {
                AppTopBar(
                    title = "Payments demo",
                    navigation =
                        AppBarIcon(
                            contentDescription = "Back",
                            onClick = onBack,
                            icon = {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                    contentDescription = null,
                                )
                            },
                        ),
                )
            },
            content = { insets ->
                Column(
                    modifier =
                        Modifier.fillMaxSize()
                            .padding(insets)
                            .verticalScroll(rememberScrollState())
                            .padding(Dimens.medium),
                    verticalArrangement = Arrangement.spacedBy(Dimens.medium),
                    content = {
                        Text(text = "Simulation — no real payments or charges.")
                        Text(
                            text = "Lifetime Premium",
                            style = MaterialTheme.typography.headlineSmall,
                        )
                        Text(text = "Unlock all premium features with a single purchase.")
                        val owned = FakeBilling.PRODUCT_ID in state.ownedProductIds
                        val pending = FakeBilling.PRODUCT_ID in state.pendingProductIds
                        Text(
                            text =
                                if (owned) "Premium active"
                                else if (pending) "Payment pending — premium is locked"
                                else "Premium locked"
                        )
                        Text(text = "Checkout: ${state.checkout}")
                        state.error?.let {
                            Text(
                                text = it,
                                color = MaterialTheme.colorScheme.error,
                            )
                        }
                        SuperButton(
                            text =
                                if (owned) "Premium unlocked"
                                else
                                    "Unlock — ${state.products.firstOrNull()?.formattedPrice ?: "Unavailable"}",
                            onClick = model::buy,
                            state =
                                when {
                                    state.loading || state.checkout == CheckoutState.Preparing ->
                                        ButtonState.Loading
                                    owned || pending || state.products.isEmpty() ->
                                        ButtonState.Disabled
                                    else -> ButtonState.Enabled
                                },
                        )
                        InlineButton(
                            text = "Restore purchases",
                            onClick = model::restore,
                            state = if (state.loading) ButtonState.Loading else ButtonState.Enabled,
                        )
                        InlineButton(
                            text = "Google Play checkout",
                            onClick = onPlayBilling,
                        )
                        HorizontalDivider()
                        Text(
                            text = "Test controls",
                            style = MaterialTheme.typography.titleMedium,
                        )
                        FakeBilling.Outcome.entries.forEach { option ->
                            InlineButton(
                                text = "${if (outcome == option) "✓ " else ""}Next result: $option",
                                onClick = {
                                    outcome = option
                                    model.billing.nextOutcome = option
                                },
                            )
                        }
                        InlineButton(
                            text = if (offline) "Go online" else "Simulate offline",
                            onClick = {
                                offline = !offline
                                model.billing.offline = offline
                            },
                        )
                        InlineButton(
                            text = "Complete pending payment",
                            onClick = model::complete,
                            state =
                                if (pending && !offline) ButtonState.Enabled
                                else ButtonState.Disabled,
                        )
                        InlineButton(
                            text = "Simulate refund / reset",
                            onClick = model::revoke,
                            state = if (!offline) ButtonState.Enabled else ButtonState.Disabled,
                        )
                    },
                )
            },
        )
    }
}
