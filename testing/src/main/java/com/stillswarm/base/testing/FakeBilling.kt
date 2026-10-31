package com.stillswarm.base.testing

import com.stillswarm.base.billing.*

/** Simulation only. Never use this implementation to grant production access. */
class FakeBilling : Billing {
    enum class Outcome {
        Success,
        Cancelled,
        Pending,
        Failure,
    }

    var nextOutcome = Outcome.Success
    var offline = false
    private var owned = emptyList<OwnedPurchase>()
    private var cached = emptySet<String>()
    private val backend =
        object : BillingBackend {
            override suspend fun products(): List<BillingProduct> {
                check(!offline)
                return listOf(
                    BillingProduct(
                        id = PRODUCT_ID,
                        title = "Lifetime Premium",
                        formattedPrice = "₹29 (simulated)",
                    )
                )
            }

            override suspend fun purchases(): List<OwnedPurchase> {
                check(!offline)
                return owned
            }

            override suspend fun acknowledge(token: String) {
                check(!offline)
                owned = owned.map { if (it.token == token) it.copy(acknowledged = true) else it }
            }
        }
    private val session =
        BillingSession(
            productIds = setOf(PRODUCT_ID),
            backend = backend,
            verifier = PurchaseVerifier { true },
            cache =
                object : EntitlementCache {
                    override suspend fun read() = cached

                    override suspend fun write(productIds: Set<String>) {
                        cached = productIds
                    }
                },
        )
    override val state
        get() = session.state

    override suspend fun refresh() = session.refresh()

    suspend fun purchase() {
        if (offline) {
            session.checkoutChanged(CheckoutState.Failed)
            session.reportFailure("Simulated offline checkout failure.")
            return
        }
        when (nextOutcome) {
            Outcome.Cancelled -> session.checkoutChanged(CheckoutState.Cancelled)
            Outcome.Failure -> {
                session.checkoutChanged(CheckoutState.Failed)
                session.reportFailure("Simulated payment failure. Try again.")
            }
            Outcome.Pending,
            Outcome.Success -> {
                val pending = nextOutcome == Outcome.Pending
                owned =
                    listOf(
                        OwnedPurchase(
                            token = "fake-token",
                            productIds = setOf(PRODUCT_ID),
                            pending = pending,
                            acknowledged = false,
                            originalJson = "",
                            signature = "",
                        )
                    )
                refresh()
                session.checkoutChanged(
                    if (pending) CheckoutState.Pending else CheckoutState.Purchased
                )
            }
        }
    }

    suspend fun completePending() {
        owned = owned.map { it.copy(pending = false) }
        refresh()
    }

    suspend fun revoke() {
        owned = emptyList()
        refresh()
        session.checkoutChanged(CheckoutState.Idle)
    }

    companion object {
        const val PRODUCT_ID = "premium_lifetime"
    }
}
