package com.stillswarm.base.storage

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import java.io.IOException
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

@OptIn(ExperimentalCoroutinesApi::class)
class PreferenceStoreTest {
    @get:Rule val directory = TemporaryFolder()
    private val count = PreferenceKey.int("test.count", 5)
    private val label = PreferenceKey.string("test.label", "default")

    private fun store(scope: CoroutineScope, name: String = "test") =
        DataStorePreferenceStore(
            PreferenceDataStoreFactory.create(
                scope = scope,
                produceFile = { directory.root.resolve("$name.preferences_pb") },
            )
        )

    @Test
    fun missingDefaultsSetRemoveAndAllSupportedTypes() = runTest {
        val store = store(backgroundScope)
        assertEquals(5, store.get(count))
        store.set(count, 9)
        assertEquals(9, store.get(count))
        store.remove(count)
        assertEquals(5, store.get(count))
        suspend fun <T : Any> roundTrip(key: PreferenceKey<T>, value: T) {
            store.set(key, value)
            assertEquals(value, store.get(key))
        }
        roundTrip(PreferenceKey.boolean("boolean"), true)
        roundTrip(PreferenceKey.long("long"), Long.MAX_VALUE)
        roundTrip(PreferenceKey.float("float"), 1.25f)
        roundTrip(PreferenceKey.double("double"), 2.75)
        roundTrip(label, "saved")
        roundTrip(PreferenceKey.stringSet("set"), setOf("a", "b"))
    }

    @Test
    fun concurrentUpdatesDoNotLoseIncrements() = runTest {
        val store = store(backgroundScope)
        coroutineScope { repeat(100) { launch { store.update(count) { it + 1 } } } }
        assertEquals(105, store.get(count))
    }

    @Test
    fun multiKeyEditsCommitTogetherAndRollBackOnFailure() = runTest {
        val store = store(backgroundScope)
        store.edit {
            this[count] = 10
            this[label] = "committed"
        }
        try {
            store.edit {
                this[count] = 99
                remove(label)
                error("abort")
            }
            fail("Expected failed transaction")
        } catch (_: IllegalStateException) {}
        assertEquals(10, store.get(count))
        assertEquals("committed", store.get(label))
    }

    @Test
    fun observesChangesWithoutEmittingForUnrelatedKeys() = runTest {
        val store = store(backgroundScope)
        val values = mutableListOf<Int>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            store.observe(count).collect { values.add(it) }
        }
        store.get(count)
        runCurrent()
        store.set(label, "unrelated")
        runCurrent()
        store.set(count, 8)
        runCurrent()
        store.remove(count)
        runCurrent()
        assertEquals(listOf(5, 8, 5), values)
    }

    @Test
    fun persistsAfterStoreIsClosedAndReopened() = runTest {
        val firstJob = SupervisorJob()
        val first = store(CoroutineScope(coroutineContext + firstJob))
        first.set(label, "persisted")
        firstJob.cancelAndJoin()
        val reopened = store(backgroundScope)
        assertEquals("persisted", reopened.get(label))
    }

    @Test
    fun readAndWriteFailuresAreNotHiddenAsDefaultsOrSuccess() = runTest {
        val failure = IOException("disk failure")
        val broken =
            object : DataStore<Preferences> {
                override val data: Flow<Preferences> = flow { throw failure }

                override suspend fun updateData(
                    transform: suspend (Preferences) -> Preferences
                ): Preferences = throw failure
            }
        val store = DataStorePreferenceStore(broken)
        try {
            store.get(count)
            fail("Expected read failure")
        } catch (caught: IOException) {
            assertSame(failure, caught)
        }
        try {
            store.set(count, 3)
            fail("Expected write failure")
        } catch (caught: IOException) {
            assertSame(failure, caught)
        }
    }

    @Test
    fun retainedEditorCannotMutateCommittedState() = runTest {
        val store = store(backgroundScope)
        lateinit var escaped: PreferenceEditor
        store.edit { escaped = this }
        try {
            escaped[count] = 999
            fail("Editor should be expired")
        } catch (_: IllegalStateException) {}
        assertEquals(5, store.get(count))
    }
}
