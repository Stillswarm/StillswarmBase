package com.stillswarm.base.testing

import com.stillswarm.base.storage.PreferenceKey
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class InMemoryPreferenceStoreTest {
    @Test
    fun fakeIsIsolatedAndUpdatesAreAtomic() = runTest {
        val count = PreferenceKey.int("count")
        val first = InMemoryPreferenceStore()
        val second = InMemoryPreferenceStore()
        coroutineScope { repeat(50) { launch { first.update(count) { it + 1 } } } }
        assertEquals(50, first.get(count))
        assertEquals(0, second.get(count))
    }
}
