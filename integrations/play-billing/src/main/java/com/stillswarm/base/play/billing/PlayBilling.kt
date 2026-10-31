package com.stillswarm.base.play.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.*
import com.stillswarm.base.billing.*
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** App-process owner. Pass an Activity only to launch checkout; it is never retained. */
class PlayBilling(
    context: Context,
    productIds: Set<String>,
    verifier: PurchaseVerifier,
    cache: EntitlementCache,
) : Billing, AutoCloseable {
    private val ids = productIds.toSet()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val connectionMutex = Mutex()
    private var closed = false
    private var launching = false
    private val client: BillingClient =
        BillingClient.newBuilder(context.applicationContext)
            .setListener { result, purchases ->
                when (result.responseCode) {
                    BillingClient.BillingResponseCode.OK -> {
                        session.checkoutChanged(
                            if (
                                purchases.orEmpty().any {
                                    it.purchaseState == Purchase.PurchaseState.PENDING
                                }
                            )
                                CheckoutState.Pending
                            else CheckoutState.Idle
                        )
                        scope.launch {
                            refresh()
                            if (
                                purchases.orEmpty().any { purchase ->
                                    purchase.products.any(state.value.ownedProductIds::contains)
                                }
                            )
                                session.checkoutChanged(CheckoutState.Purchased)
                        }
                    }
                    BillingClient.BillingResponseCode.USER_CANCELED ->
                        session.checkoutChanged(CheckoutState.Cancelled)
                    BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                        session.checkoutChanged(CheckoutState.Idle)
                        scope.launch { refresh() }
                    }
                    else -> fail("Purchase could not be completed. Please retry.")
                }
            }
            .enablePendingPurchases(
                PendingPurchasesParams.newBuilder().enableOneTimeProducts().build()
            )
            .enableAutoServiceReconnection()
            .build()

    private val backend: BillingBackend =
        object : BillingBackend {
            override suspend fun products(): List<BillingProduct> =
                details().mapNotNull { detail ->
                    val offer =
                        detail.oneTimePurchaseOfferDetailsList?.singleOrNull()
                            ?: return@mapNotNull null
                    if (offer.rentalDetails != null || offer.preorderDetails != null)
                        return@mapNotNull null
                    BillingProduct(
                        id = detail.productId,
                        title = detail.title,
                        formattedPrice = offer.formattedPrice,
                    )
                }

            override suspend fun purchases(): List<OwnedPurchase> {
                connect()
                return suspendCancellableCoroutine { continuation ->
                    client.queryPurchasesAsync(
                        QueryPurchasesParams.newBuilder()
                            .setProductType(BillingClient.ProductType.INAPP)
                            .build()
                    ) { result, purchases ->
                        if (!continuation.isActive) return@queryPurchasesAsync
                        if (result.responseCode != BillingClient.BillingResponseCode.OK) {
                            continuation.resumeWithException(
                                IllegalStateException("Purchase query failed")
                            )
                        } else
                            continuation.resume(
                                purchases
                                    .filter {
                                        it.purchaseState == Purchase.PurchaseState.PURCHASED ||
                                            it.purchaseState == Purchase.PurchaseState.PENDING
                                    }
                                    .map { purchase ->
                                        OwnedPurchase(
                                            token = purchase.purchaseToken,
                                            productIds = purchase.products.toSet(),
                                            pending =
                                                purchase.purchaseState ==
                                                    Purchase.PurchaseState.PENDING,
                                            acknowledged = purchase.isAcknowledged,
                                            originalJson = purchase.originalJson,
                                            signature = purchase.signature,
                                        )
                                    }
                            )
                    }
                }
            }

            override suspend fun acknowledge(token: String) {
                connect()
                suspendCancellableCoroutine<Unit> { continuation ->
                    client.acknowledgePurchase(
                        AcknowledgePurchaseParams.newBuilder().setPurchaseToken(token).build()
                    ) { result ->
                        if (!continuation.isActive) return@acknowledgePurchase
                        if (result.responseCode == BillingClient.BillingResponseCode.OK)
                            continuation.resume(Unit)
                        else
                            continuation.resumeWithException(
                                IllegalStateException("Acknowledgement failed")
                            )
                    }
                }
            }
        }
    private val session: BillingSession =
        BillingSession(
            productIds = ids,
            backend = backend,
            verifier = verifier,
            cache = cache,
        )
    override val state: kotlinx.coroutines.flow.StateFlow<BillingState>
        get() = session.state

    override suspend fun refresh() =
        withContext(Dispatchers.Main.immediate) {
            check(!closed) { "Billing is closed" }
            session.refresh()
        }

    /** Call on Activity resume. Also recovers checkout interrupted by process death. */
    suspend fun onResume() {
        refresh()
        if (state.value.checkout == CheckoutState.AwaitingResult)
            session.checkoutChanged(CheckoutState.Idle)
    }

    /** Fresh product details are fetched for every checkout; no Activity survives this call. */
    suspend fun purchase(activity: Activity, productId: String) =
        withContext(Dispatchers.Main.immediate) {
            check(!closed) { "Billing is closed" }
            require(productId in ids) { "Unknown product" }
            if (launching || state.value.checkout == CheckoutState.AwaitingResult)
                return@withContext
            launching = true
            session.checkoutChanged(CheckoutState.Preparing)
            try {
                val completed =
                    withTimeoutOrNull(15_000) {
                        val detail =
                            details().singleOrNull { it.productId == productId }
                                ?: error("Product unavailable")
                        // v1 supports a single eligible lifetime purchase option, never
                        // rentals/multi-offer selection.
                        val offer =
                            detail.oneTimePurchaseOfferDetailsList?.singleOrNull()
                                ?: error("Configure one eligible purchase option")
                        check(offer.rentalDetails == null && offer.preorderDetails == null)
                        check(!activity.isFinishing && !activity.isDestroyed)
                        val params =
                            BillingFlowParams.ProductDetailsParams.newBuilder()
                                .setProductDetails(detail)
                                .apply { offer.offerToken?.let { setOfferToken(it) } }
                                .build()
                        session.checkoutChanged(CheckoutState.AwaitingResult)
                        val result =
                            client.launchBillingFlow(
                                activity,
                                BillingFlowParams.newBuilder()
                                    .setProductDetailsParamsList(listOf(params))
                                    .build(),
                            )
                        when (result.responseCode) {
                            BillingClient.BillingResponseCode.OK -> Unit
                            BillingClient.BillingResponseCode.USER_CANCELED ->
                                session.checkoutChanged(CheckoutState.Cancelled)
                            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                                session.checkoutChanged(CheckoutState.Idle)
                                refresh()
                            }
                            else -> fail("Checkout is unavailable. Please retry.")
                        }
                        true
                    }
                if (completed == null) fail("Checkout timed out. Please retry.")
            } catch (cancelled: CancellationException) {
                session.checkoutChanged(CheckoutState.Idle)
                throw cancelled
            } catch (_: Exception) {
                fail("Product is unavailable. Check the Play configuration and retry.")
            } finally {
                launching = false
            }
        }

    private fun fail(message: String) {
        session.checkoutChanged(CheckoutState.Failed)
        session.reportFailure(message)
    }

    private suspend fun details(): List<ProductDetails> {
        connect()
        return suspendCancellableCoroutine { continuation ->
            val products =
                ids.map { id ->
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(id)
                        .setProductType(BillingClient.ProductType.INAPP)
                        .build()
                }
            client.queryProductDetailsAsync(
                QueryProductDetailsParams.newBuilder().setProductList(products).build()
            ) { result, response ->
                if (!continuation.isActive) return@queryProductDetailsAsync
                if (result.responseCode == BillingClient.BillingResponseCode.OK)
                    continuation.resume(response.productDetailsList)
                else continuation.resumeWithException(IllegalStateException("Product query failed"))
            }
        }
    }

    private suspend fun connect() =
        connectionMutex.withLock {
            check(!closed)
            if (client.isReady) return@withLock
            suspendCancellableCoroutine<Unit> { continuation ->
                client.startConnection(
                    object : BillingClientStateListener {
                        override fun onBillingSetupFinished(result: BillingResult) {
                            if (!continuation.isActive) return
                            if (result.responseCode == BillingClient.BillingResponseCode.OK)
                                continuation.resume(Unit)
                            else
                                continuation.resumeWithException(
                                    IllegalStateException("Billing unavailable")
                                )
                        }

                        override fun onBillingServiceDisconnected() {
                            if (continuation.isActive)
                                continuation.resumeWithException(
                                    IllegalStateException("Billing disconnected")
                                )
                        }
                    }
                )
            }
        }

    override fun close() {
        closed = true
        scope.cancel()
        client.endConnection()
    }
}
