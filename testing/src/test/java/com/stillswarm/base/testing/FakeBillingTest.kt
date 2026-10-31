package com.stillswarm.base.testing

import com.stillswarm.base.billing.CheckoutState
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class FakeBillingTest {
    @Test
    fun cancelledAndFailedPurchasesNeverUnlock() = runTest {
        val billing = FakeBilling()
        billing.refresh()
        billing.nextOutcome = FakeBilling.Outcome.Cancelled
        billing.purchase()
        assertEquals(CheckoutState.Cancelled, billing.state.value.checkout)
        assertTrue(billing.state.value.ownedProductIds.isEmpty())
        billing.nextOutcome = FakeBilling.Outcome.Failure
        billing.purchase()
        assertEquals(CheckoutState.Failed, billing.state.value.checkout)
        assertNotNull(billing.state.value.error)
        assertTrue(billing.state.value.ownedProductIds.isEmpty())
    }

    @Test
    fun successfulPurchaseRestoresOfflineThenRefundRemovesAccess() = runTest {
        val billing = FakeBilling()
        billing.purchase()
        assertEquals(setOf(FakeBilling.PRODUCT_ID), billing.state.value.ownedProductIds)
        billing.offline = true
        billing.refresh()
        assertEquals(setOf(FakeBilling.PRODUCT_ID), billing.state.value.ownedProductIds)
        billing.offline = false
        billing.revoke()
        assertTrue(billing.state.value.ownedProductIds.isEmpty())
    }
}
