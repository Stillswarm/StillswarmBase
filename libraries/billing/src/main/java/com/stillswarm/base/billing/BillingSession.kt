package com.stillswarm.base.billing

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeoutOrNull

/** Serialized reconciliation; never consumes lifetime products. Own one session per app process. */
class BillingSession(
    productIds: Set<String>,
    private val backend: BillingBackend,
    private val verifier: PurchaseVerifier,
    private val cache: EntitlementCache,
    private val timeoutMillis: Long = 15_000,
) : Billing {
    private val allowed = productIds.toSet()

    init {
        require(allowed.isNotEmpty() && allowed.all { it.isNotBlank() })
        require(timeoutMillis > 0)
    }

    private val mutableState = MutableStateFlow(BillingState())
    override val state = mutableState.asStateFlow()
    private val mutex = Mutex()
    private var cacheLoaded = false

    override suspend fun refresh() =
        mutex.withLock {
            mutableState.update { it.copy(
                    loading = true,
                    error = null,
                ) }
            try {
                val completed =
                    withTimeoutOrNull(timeoutMillis) {
                        if (!cacheLoaded) {
                            val cached = cache.read().intersect(allowed)
                            mutableState.update { it.copy(ownedProductIds = cached) }
                            cacheLoaded = true
                        }
                        // Ownership is independent of catalog availability.
                        val purchases = backend.purchases()
                        val verified =
                            purchases.filter {
                                !it.pending &&
                                    it.productIds.any(allowed::contains) &&
                                    verifier.verify(it)
                            }
                        val owned = verified.flatMap { it.productIds }.toSet().intersect(allowed)
                        val pending =
                            purchases
                                .filter { it.pending }
                                .flatMap { it.productIds }
                                .toSet()
                                .intersect(allowed)
                        cache.write(owned)
                        mutableState.update {
                            it.copy(
                                ownedProductIds = owned,
                                pendingProductIds = pending,
                                reconciled = true,
                            )
                        }
                        // Grant durably first; retry any failed acknowledgement on the next
                        // refresh.
                        verified
                            .filter { !it.acknowledged && it.productIds.any(allowed::contains) }
                            .forEach { backend.acknowledge(it.token) }
                        val products = backend.products().filter { it.id in allowed }
                        mutableState.update { it.copy(products = products) }
                        true
                    }
                if (completed == null) reportFailure("Billing timed out. Please retry.")
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                reportFailure("Billing could not be refreshed. Please retry.")
            } finally {
                mutableState.update { it.copy(loading = false) }
            }
        }

    fun checkoutChanged(checkout: CheckoutState) {
        mutableState.update { it.copy(
                checkout = checkout,
                error = null,
            ) }
    }

    fun reportFailure(message: String) {
        mutableState.update { it.copy(error = message) }
    }
}
