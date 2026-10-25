package com.stillswarm.base.navigation

import androidx.lifecycle.ViewModelStore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NavigationCoordinatorTest {
    private data class Screen(val id: String) : Destination

    @Test
    fun onlyLatestPendingRequestIsDelivered() = runTest {
        val coordinator = NavigationCoordinator()
        coordinator.navigate(Screen("first"))
        coordinator.navigate(Screen("latest"))
        assertEquals(NavigationRequest.Navigate(Screen("latest")), coordinator.requests.first())
    }

    @Test
    fun consumedRequestIsNotReplayedWhenCollectorRestarts() = runTest {
        val coordinator = NavigationCoordinator()
        coordinator.back()
        assertEquals(NavigationRequest.Back, coordinator.requests.first())
        var received = false
        val collector =
            backgroundScope.launch {
                coordinator.requests.first()
                received = true
            }
        runCurrent()
        assertFalse(received)
        coordinator.navigate(Screen("next"))
        collector.join()
        assertEquals(true, received)
    }

    @Test
    fun operationsAreDeliveredAsAtomicRequests() = runTest {
        val coordinator = NavigationCoordinator()
        coordinator.replace(Screen("replacement"))
        assertEquals(NavigationRequest.Replace(Screen("replacement")), coordinator.requests.first())
        coordinator.resetTo(Screen("home"))
        assertEquals(NavigationRequest.ResetTo(Screen("home")), coordinator.requests.first())
    }

    @Test
    fun requestsDuringCollectorGapReplaceEarlierRequests() = runTest {
        val coordinator = NavigationCoordinator()
        coordinator.back()
        coordinator.requests.first()
        coordinator.navigate(Screen("stale"))
        coordinator.resetTo(Screen("home"))
        assertEquals(NavigationRequest.ResetTo(Screen("home")), coordinator.requests.first())
    }

    @Test
    fun clearingOwnerDiscardsPendingRequestsAndClosesSession() = runTest {
        val coordinator = NavigationCoordinator()
        val store = ViewModelStore()
        store.put("navigation", coordinator)
        coordinator.navigate(Screen("pending"))
        store.clear()
        coordinator.back()
        var received = false
        val collector = backgroundScope.launch { coordinator.requests.collect { received = true } }
        runCurrent()
        assertFalse(received)
        collector.cancel()
    }
}
