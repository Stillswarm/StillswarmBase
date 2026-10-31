package com.stillswarm.base.billing

import kotlinx.coroutines.*
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class BillingSessionTest {
    private class Cache : EntitlementCache {
        var ids = setOf("premium")
        var failWrite = false

        override suspend fun read() = ids

        override suspend fun write(productIds: Set<String>) {
            check(!failWrite)
            ids = productIds
        }
    }

    private class Backend : BillingBackend {
        var owned = emptyList<OwnedPurchase>()
        var failQuery = false
        var failAck = false
        var failProducts = false
        var delayMillis = 0L
        val acknowledged = mutableListOf<String>()

        override suspend fun purchases(): List<OwnedPurchase> {
            delay(delayMillis)
            check(!failQuery)
            return owned
        }

        override suspend fun products(): List<BillingProduct> {
            check(!failProducts)
            return listOf(BillingProduct(
                id = "premium",
                title = "Premium",
                formattedPrice = "₹29",
            ))
        }

        override suspend fun acknowledge(token: String) {
            check(!failAck)
            acknowledged += token
            owned = owned.map { it.copy(acknowledged = true) }
        }
    }

    private fun purchase(pending: Boolean = false) =
        OwnedPurchase(
            token = "token",
            productIds = setOf("premium"),
            pending = pending,
            acknowledged = false,
            originalJson = "signed",
            signature = "signature",
        )

    private fun session(backend: Backend, cache: Cache) =
        BillingSession(
            productIds = setOf("premium"),
            backend = backend,
            cache = cache,
            verifier = PurchaseVerifier { true },
            timeoutMillis = 100,
        )

    @Test
    fun offlineFailurePreservesCachedAccessAndSuccessfulEmptyQueryRevokesIt() = runTest {
        val backend = Backend().apply { failQuery = true }
        val cache = Cache()
        val billing = session(backend, cache)
        billing.refresh()
        assertEquals(setOf("premium"), billing.state.value.ownedProductIds)
        assertFalse(billing.state.value.reconciled)
        assertNotNull(billing.state.value.error)
        backend.failQuery = false
        billing.refresh()
        assertTrue(billing.state.value.ownedProductIds.isEmpty())
        assertTrue(cache.ids.isEmpty())
        assertTrue(billing.state.value.reconciled)
    }

    @Test
    fun pendingDoesNotUnlockOrAcknowledgeThenCompletionGrantsOnce() = runTest {
        val backend = Backend().apply { owned = listOf(purchase(pending = true)) }
        val cache = Cache().apply { ids = emptySet() }
        val billing = session(backend, cache)
        billing.refresh()
        assertTrue(billing.state.value.ownedProductIds.isEmpty())
        assertEquals(setOf("premium"), billing.state.value.pendingProductIds)
        assertTrue(backend.acknowledged.isEmpty())
        backend.owned = listOf(purchase())
        billing.refresh()
        billing.refresh()
        assertEquals(setOf("premium"), cache.ids)
        assertEquals(listOf("token"), backend.acknowledged)
    }

    @Test
    fun acknowledgementFailureRetainsGrantAndRetries() = runTest {
        val backend =
            Backend().apply {
                owned = listOf(purchase())
                failAck = true
            }
        val billing = session(backend, Cache())
        billing.refresh()
        assertEquals(setOf("premium"), billing.state.value.ownedProductIds)
        assertNotNull(billing.state.value.error)
        backend.failAck = false
        billing.refresh()
        assertEquals(listOf("token"), backend.acknowledged)
        assertNull(billing.state.value.error)
    }

    @Test
    fun failedPersistenceNeverAcknowledges() = runTest {
        val backend = Backend().apply { owned = listOf(purchase()) }
        val cache =
            Cache().apply {
                ids = emptySet()
                failWrite = true
            }
        val billing = session(backend, cache)
        billing.refresh()
        assertTrue(billing.state.value.ownedProductIds.isEmpty())
        assertTrue(backend.acknowledged.isEmpty())
        assertNotNull(billing.state.value.error)
    }

    @Test
    fun invalidPurchaseIsNotGrantedOrAcknowledged() = runTest {
        val backend = Backend().apply { owned = listOf(purchase()) }
        val billing =
            BillingSession(
                productIds = setOf("premium"),
                backend = backend,
                cache = Cache(),
                verifier = PurchaseVerifier { false },
            )
        billing.refresh()
        assertTrue(billing.state.value.ownedProductIds.isEmpty())
        assertTrue(backend.acknowledged.isEmpty())
    }

    @Test
    fun transientVerificationFailureKeepsCache() = runTest {
        val backend = Backend().apply { owned = listOf(purchase()) }
        val billing =
            BillingSession(
                productIds = setOf("premium"),
                backend = backend,
                cache = Cache(),
                verifier = PurchaseVerifier { error("Server unavailable") },
            )
        billing.refresh()
        assertEquals(setOf("premium"), billing.state.value.ownedProductIds)
        assertNotNull(billing.state.value.error)
        assertTrue(backend.acknowledged.isEmpty())
    }

    @Test
    fun timeoutPreservesCacheAndClearsLoading() = runTest {
        val backend = Backend().apply { delayMillis = 1_000 }
        val billing = session(backend, Cache())
        billing.refresh()
        assertEquals(setOf("premium"), billing.state.value.ownedProductIds)
        assertFalse(billing.state.value.loading)
        assertNotNull(billing.state.value.error)
    }

    @Test
    fun catalogFailureDoesNotBlockOwnershipReconciliation() = runTest {
        val backend = Backend().apply { failProducts = true }
        val billing = session(backend, Cache())
        billing.refresh()
        assertTrue(billing.state.value.ownedProductIds.isEmpty())
        assertTrue(billing.state.value.reconciled)
    }

    @Test
    fun callerTimeoutPropagatesAndClearsLoading() = runTest {
        val backend = Backend().apply { delayMillis = 1_000 }
        val billing = session(backend, Cache())
        try {
            withTimeout(10) { billing.refresh() }
            fail("Expected caller cancellation")
        } catch (_: TimeoutCancellationException) {
            assertFalse(billing.state.value.loading)
            assertNull(billing.state.value.error)
        }
    }

    @Test
    fun unrelatedProductsAreNeverVerifiedGrantedOrAcknowledged() = runTest {
        val backend =
            Backend().apply { owned = listOf(purchase().copy(productIds = setOf("unrelated"))) }
        val billing =
            BillingSession(
                productIds = setOf("premium"),
                backend = backend,
                verifier = PurchaseVerifier { error("Must not verify unrelated purchases") },
                cache = Cache(),
            )
        billing.refresh()
        assertTrue(billing.state.value.ownedProductIds.isEmpty())
        assertTrue(backend.acknowledged.isEmpty())
        assertNull(billing.state.value.error)
    }
}
