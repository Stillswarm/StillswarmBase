package com.stillswarm.base.config

import com.stillswarm.base.storage.PreferenceKey
import com.stillswarm.base.storage.PreferenceStore
import com.stillswarm.base.testing.FakeRemoteConfigSource
import com.stillswarm.base.testing.InMemoryPreferenceStore
import java.io.IOException
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ConfigSessionTest {
    private val enabled = ConfigKey.boolean(name = "enabled", defaultValue = false)
    private val limit = ConfigKey.int(name = "limit", defaultValue = 5, validate = { it in 1..100 })
    private val keys = listOf(enabled, limit)

    @Test
    fun defaultsAreImmediateAndNoSourceDoesNotFetch() = runTest {
        val session = ConfigSession(keys = keys, storage = InMemoryPreferenceStore())
        assertFalse(session[enabled])
        assertEquals(ConfigValueSource.Default, session.state.value.snapshot.source(limit))
        assertFalse(session.state.value.localLoaded)
        session.start()
        assertTrue(session.state.value.localLoaded)
        assertEquals(ConfigFetchStatus.Disabled, session.state.value.fetchStatus)
    }

    @Test
    fun precedencePersistenceAndRemovingOverrides() = runTest {
        val storage = InMemoryPreferenceStore()
        val session =
            ConfigSession(
                keys = keys,
                storage = storage,
                remoteSource = FakeRemoteConfigSource(values = mapOf("limit" to "10")),
            )
        session.start()
        session.setOverride(limit, 20)
        assertEquals(20, session[limit])
        assertEquals(ConfigValueSource.LocalOverride, session.state.value.snapshot.source(limit))
        val next = ConfigSession(keys = keys, storage = storage)
        next.start()
        assertEquals(20, next[limit])
        next.removeOverride(limit)
        assertEquals(10, next[limit])
        assertEquals(ConfigValueSource.Remote, next.state.value.snapshot.source(limit))
    }

    @Test
    fun timeoutKeepsCacheAndLateSourceDoesNotActivate() = runTest {
        val storage = InMemoryPreferenceStore()
        ConfigSession(
                keys = keys,
                storage = storage,
                remoteSource = FakeRemoteConfigSource(values = mapOf("limit" to "10")),
            )
            .start()
        val source = FakeRemoteConfigSource(values = mapOf("limit" to "99"), delayMillis = 10_000)
        val session =
            ConfigSession(
                keys = keys,
                storage = storage,
                remoteSource = source,
                fetchTimeoutMillis = 100,
            )
        session.start()
        advanceTimeBy(20_000)
        assertEquals(ConfigFetchStatus.TimedOut, session.state.value.fetchStatus)
        assertEquals(10, session[limit])
        session.start()
        assertEquals(1, source.fetchCount)
    }

    @Test
    fun concurrentStartsFetchOnlyOnce() = runTest {
        val source = FakeRemoteConfigSource(delayMillis = 100)
        val session =
            ConfigSession(keys = keys, storage = InMemoryPreferenceStore(), remoteSource = source)
        coroutineScope { repeat(10) { launch { session.start() } } }
        assertEquals(1, source.fetchCount)
    }

    @Test
    fun overrideDuringFetchWinsOverIncomingRemoteSnapshot() = runTest {
        val session =
            ConfigSession(
                keys = keys,
                storage = InMemoryPreferenceStore(),
                remoteSource =
                    FakeRemoteConfigSource(values = mapOf("limit" to "10"), delayMillis = 100),
            )
        val job = launch { session.start() }
        runCurrent()
        session.setOverride(limit, 25)
        job.join()
        assertEquals(25, session[limit])
        session.removeOverride(limit)
        assertEquals(10, session[limit])
    }

    @Test
    fun invalidRemoteRejectsWholeSnapshotAndPreservesCache() = runTest {
        val storage = InMemoryPreferenceStore()
        ConfigSession(
                keys = keys,
                storage = storage,
                remoteSource = FakeRemoteConfigSource(values = mapOf("limit" to "10")),
            )
            .start()
        val session =
            ConfigSession(
                keys = keys,
                storage = storage,
                remoteSource =
                    FakeRemoteConfigSource(values = mapOf("limit" to "0", "enabled" to "true")),
            )
        session.start()
        assertEquals(ConfigFetchStatus.Rejected, session.state.value.fetchStatus)
        assertFalse(session[enabled])
        assertEquals(10, session[limit])
    }

    @Test
    fun acceptedSnapshotReplacesOldKeysAndIgnoresUnknownOnes() = runTest {
        val storage = InMemoryPreferenceStore()
        ConfigSession(
                keys = keys,
                storage = storage,
                remoteSource = FakeRemoteConfigSource(values = mapOf("limit" to "10")),
            )
            .start()
        val session =
            ConfigSession(
                keys = keys,
                storage = storage,
                remoteSource =
                    FakeRemoteConfigSource(values = mapOf("enabled" to "true", "unknown" to "bad")),
            )
        session.start()
        assertEquals(5, session[limit])
        assertTrue(session[enabled])
    }

    @Test
    fun failedFetchAndCancellationDoNotRetry() = runTest {
        val source = FakeRemoteConfigSource(failure = IOException("offline"))
        val session =
            ConfigSession(keys = keys, storage = InMemoryPreferenceStore(), remoteSource = source)
        session.start()
        session.start()
        assertEquals(ConfigFetchStatus.Failed, session.state.value.fetchStatus)
        assertEquals(1, source.fetchCount)
        val slow = FakeRemoteConfigSource(delayMillis = 10_000)
        val cancelled =
            ConfigSession(keys = keys, storage = InMemoryPreferenceStore(), remoteSource = slow)
        val job = launch { cancelled.start() }
        runCurrent()
        job.cancelAndJoin()
        cancelled.start()
        assertEquals(ConfigFetchStatus.Cancelled, cancelled.state.value.fetchStatus)
        assertEquals(1, slow.fetchCount)
    }

    @Test
    fun invalidOverrideThrowsAndDoesNotChangeCurrentValue() = runTest {
        val session = ConfigSession(keys = keys, storage = InMemoryPreferenceStore())
        session.setOverride(limit, 30)
        try {
            session.setOverride(limit, 101)
            fail("Expected validation failure")
        } catch (_: IllegalArgumentException) {}
        assertEquals(30, session[limit])
    }

    @Test
    fun corruptCacheAndOldInvalidValuesFallBackWithDiagnostics() = runTest {
        val storage = InMemoryPreferenceStore()
        storage.set(PreferenceKey.string("config.default.remote"), "broken json")
        storage.set(
            PreferenceKey.string("config.default.overrides"),
            "{\"limit\":\"999\",\"enabled\":\"true\"}",
        )
        val session = ConfigSession(keys = keys, storage = storage)
        session.start()
        assertEquals(5, session[limit])
        assertTrue(session[enabled])
        assertTrue(ConfigIssue.InvalidStoredValues in session.state.value.issues)
    }

    @Test
    fun failedPersistenceDoesNotPublishUnsavedValues() = runTest {
        val memory = InMemoryPreferenceStore()
        val broken =
            object : PreferenceStore by memory {
                override suspend fun <T : Any> set(key: PreferenceKey<T>, value: T) {
                    throw IOException("disk")
                }
            }
        val session =
            ConfigSession(
                keys = keys,
                storage = broken,
                remoteSource = FakeRemoteConfigSource(values = mapOf("limit" to "10")),
            )
        session.start()
        assertEquals(5, session[limit])
        assertEquals(ConfigFetchStatus.Failed, session.state.value.fetchStatus)
        try {
            session.setOverride(limit, 20)
            fail("Expected persistence failure")
        } catch (_: IOException) {}
        assertEquals(5, session[limit])
        assertTrue(ConfigIssue.StorageWriteFailed in session.state.value.issues)
    }

    @Test
    fun readFailureDoesNotPermitOverwritingUnreadOverrides() = runTest {
        val memory = InMemoryPreferenceStore()
        val broken =
            object : PreferenceStore by memory {
                override suspend fun <T : Any> get(key: PreferenceKey<T>): T {
                    throw IOException("disk")
                }
            }
        val session = ConfigSession(keys = keys, storage = broken)
        session.start()
        assertTrue(ConfigIssue.StorageReadFailed in session.state.value.issues)
        try {
            session.setOverride(limit, 20)
            fail("Expected unread overrides to be protected")
        } catch (_: IllegalStateException) {}
    }

    @Test
    fun observationEmitsEffectiveChangesAndNamespacesAreIsolated() = runTest {
        val storage = InMemoryPreferenceStore()
        val session = ConfigSession(keys = keys, storage = storage, namespace = "first")
        val seen = mutableListOf<Int>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            session.observe(limit).collect { seen.add(it) }
        }
        session.setOverride(limit, 20)
        session.setOverride(enabled, true)
        session.clearOverrides()
        assertEquals(listOf(5, 20, 5), seen)
        session.setOverride(limit, 99)
        val other = ConfigSession(keys = keys, storage = storage, namespace = "second")
        other.start()
        assertEquals(5, other[limit])
    }

    @Test
    fun schemaRejectsDuplicatesAndMismatchedKeyInstances() {
        try {
            ConfigSession(keys = listOf(limit, limit), storage = InMemoryPreferenceStore())
            fail("Expected duplicate rejection")
        } catch (_: IllegalArgumentException) {}
        val session = ConfigSession(keys = keys, storage = InMemoryPreferenceStore())
        try {
            session[ConfigKey.int(name = "limit", defaultValue = 7)]
            fail("Expected unregistered key rejection")
        } catch (_: IllegalArgumentException) {}
    }

    @Test
    fun numericAndBooleanTypesAreStrict() {
        assertFalse(enabled.accepts("1"))
        assertFalse(limit.accepts("2147483648"))
        assertFalse(ConfigKey.double(name = "ratio", defaultValue = 1.0).accepts("NaN"))
        assertTrue(
            ConfigKey.long(name = "large", defaultValue = 0).accepts(Long.MAX_VALUE.toString())
        )
    }
}
