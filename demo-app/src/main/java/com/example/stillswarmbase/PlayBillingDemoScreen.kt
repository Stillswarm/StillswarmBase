package com.example.stillswarmbase

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.stillswarm.base.billing.CheckoutState
import com.stillswarm.base.shell.StillswarmAppShell
import com.stillswarm.base.ui.Dimens
import com.stillswarm.base.ui.StillswarmBaseTheme
import com.stillswarm.base.ui.components.*
import kotlinx.coroutines.launch

@Composable
internal fun PlayBillingDemoScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val app = context.applicationContext as DemoApplication
    val billing = app.playBilling
    val scope = rememberCoroutineScope()
    StillswarmBaseTheme {
        StillswarmAppShell(
            topBar = {
                AppTopBar(
                    title = "Google Play checkout",
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
                        if (billing == null) {
                            Text(
                                text =
                                    app.billingSetupError
                                        ?: "Play Billing is not configured for this build."
                            )
                            Text(
                                text =
                                    "Add billing.productId and billing.publicKey to local.properties, then rebuild. See integrations/play-billing/README.md for setup."
                            )
                        } else {
                            val state by billing.state.collectAsStateWithLifecycle()
                            LaunchedEffect(billing) { billing.refresh() }
                            Text(
                                text = "Lifetime Premium",
                                style = MaterialTheme.typography.headlineSmall,
                            )
                            Text(
                                text =
                                    "This opens Google Play. Only license tester accounts receive test payment methods."
                            )
                            val owned = BuildConfig.BILLING_PRODUCT_ID in state.ownedProductIds
                            val pending = BuildConfig.BILLING_PRODUCT_ID in state.pendingProductIds
                            val busy =
                                state.loading ||
                                    state.checkout == CheckoutState.Preparing ||
                                    state.checkout == CheckoutState.AwaitingResult
                            val product = state.products.singleOrNull()
                            Text(
                                text =
                                    if (owned) "Premium active"
                                    else if (pending) "Payment pending" else "Premium locked"
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
                                    else "Unlock — ${product?.formattedPrice ?: "Unavailable"}",
                                onClick = {
                                    context.activity()?.let { activity ->
                                        scope.launch {
                                            billing.purchase(
                                                activity,
                                                BuildConfig.BILLING_PRODUCT_ID,
                                            )
                                        }
                                    }
                                },
                                state =
                                    when {
                                        busy -> ButtonState.Loading
                                        owned || pending || product == null -> ButtonState.Disabled
                                        else -> ButtonState.Enabled
                                    },
                            )
                            InlineButton(
                                text = "Restore purchases",
                                onClick = { scope.launch { billing.refresh() } },
                                state = if (busy) ButtonState.Loading else ButtonState.Enabled,
                            )
                            if (product == null && !state.loading)
                                Text(
                                    text =
                                        "Product unavailable. Check the active product, testing account, and Play installation."
                                )
                        }
                    },
                )
            },
        )
    }
}

private tailrec fun Context.activity(): Activity? =
    when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.activity()
        else -> null
    }
