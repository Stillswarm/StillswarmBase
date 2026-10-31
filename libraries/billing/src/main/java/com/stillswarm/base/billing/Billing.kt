package com.stillswarm.base.billing

import kotlinx.coroutines.flow.StateFlow

/** One-time, non-consumable products only. Price is supplied by the store. */
data class BillingProduct(val id: String, val title: String, val formattedPrice: String)

enum class CheckoutState {
    Idle,
    Preparing,
    AwaitingResult,
    Cancelled,
    Pending,
    Purchased,
    Failed,
}

data class BillingState(
    val products: List<BillingProduct> = emptyList(),
    val ownedProductIds: Set<String> = emptySet(),
    val pendingProductIds: Set<String> = emptySet(),
    val loading: Boolean = false,
    val reconciled: Boolean = false,
    val checkout: CheckoutState = CheckoutState.Idle,
    val error: String? = null,
)

interface Billing {
    val state: StateFlow<BillingState>

    /** Also used for restore and foreground reconciliation. Failure preserves cached access. */
    suspend fun refresh()
}

data class OwnedPurchase(
    val token: String,
    val productIds: Set<String>,
    val pending: Boolean,
    val acknowledged: Boolean,
    val originalJson: String,
    val signature: String,
)

/** Throw on temporary verification failure; false means the purchase is invalid. */
fun interface PurchaseVerifier {
    suspend fun verify(purchase: OwnedPurchase): Boolean
}

interface BillingBackend {
    suspend fun products(): List<BillingProduct>

    suspend fun purchases(): List<OwnedPurchase>

    suspend fun acknowledge(token: String)
}

interface EntitlementCache {
    suspend fun read(): Set<String>

    suspend fun write(productIds: Set<String>)
}
