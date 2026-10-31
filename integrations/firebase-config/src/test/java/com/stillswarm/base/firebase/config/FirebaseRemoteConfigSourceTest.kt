package com.stillswarm.base.firebase.config

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigValue
import java.io.IOException
import kotlinx.coroutines.*
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class FirebaseRemoteConfigSourceTest {
    private class Client : FirebaseConfigClient {
        val calls = mutableListOf<String>()
        var fetchDelay = 0L
        var fetchFailure: Exception? = null
        var activationFailure: Exception? = null

        override suspend fun fetch(minimumFetchIntervalSeconds: Long) {
            calls.add("fetch:$minimumFetchIntervalSeconds")
            delay(fetchDelay)
            fetchFailure?.let { throw it }
        }

        override suspend fun activate() {
            calls.add("activate")
            activationFailure?.let { throw it }
        }

        override fun remoteValues(): Map<String, String> {
            calls.add("read")
            return mapOf("limit" to "10")
        }
    }

    @Test
    fun fetchThenActivateThenRead() = runTest {
        val client = Client()
        val source = FirebaseRemoteConfigSource(client = client, minimumFetchIntervalSeconds = 60)
        assertEquals(mapOf("limit" to "10"), source.fetch())
        assertEquals(listOf("fetch:60", "activate", "read"), client.calls)
    }

    @Test
    fun failedFetchNeverActivatesOrReturnsOldSdkValues() = runTest {
        val failure = IOException("offline")
        val client = Client().apply { fetchFailure = failure }
        try {
            FirebaseRemoteConfigSource(client = client, minimumFetchIntervalSeconds = 0).fetch()
            fail("Expected failure")
        } catch (caught: IOException) {
            assertSame(failure, caught)
        }
        assertEquals(listOf("fetch:0"), client.calls)
    }

    @Test
    fun failedActivationPropagates() = runTest {
        val failure = IOException("activation failed")
        val client = Client().apply { activationFailure = failure }
        try {
            FirebaseRemoteConfigSource(client = client, minimumFetchIntervalSeconds = 0).fetch()
            fail("Expected failure")
        } catch (caught: IOException) {
            assertSame(failure, caught)
        }
        assertEquals(listOf("fetch:0", "activate"), client.calls)
    }

    @Test
    fun timeoutDoesNotActivateOrReadLateValues() = runTest {
        val client = Client().apply { fetchDelay = 1_000 }
        val result =
            withTimeoutOrNull(100) {
                FirebaseRemoteConfigSource(client = client, minimumFetchIntervalSeconds = 0).fetch()
            }
        assertNull(result)
        assertEquals(listOf("fetch:0"), client.calls)
    }

    @Test
    fun filtersFirebaseDefaultsAndStaticValues() {
        fun value(text: String, source: Int) =
            object : FirebaseRemoteConfigValue {
                override fun asString() = text

                override fun asLong() = text.toLong()

                override fun asDouble() = text.toDouble()

                override fun asByteArray() = text.toByteArray()

                override fun asBoolean() = text.toBoolean()

                override fun getSource() = source
            }
        val actual =
            remoteValuesOnly(
                mapOf(
                    "remote" to value("true", FirebaseRemoteConfig.VALUE_SOURCE_REMOTE),
                    "default" to value("false", FirebaseRemoteConfig.VALUE_SOURCE_DEFAULT),
                    "static" to value("", FirebaseRemoteConfig.VALUE_SOURCE_STATIC),
                )
            )
        assertEquals(mapOf("remote" to "true"), actual)
    }

    @Test
    fun negativeFetchIntervalIsRejected() {
        try {
            FirebaseRemoteConfigSource(client = Client(), minimumFetchIntervalSeconds = -1)
            fail("Expected invalid interval rejection")
        } catch (_: IllegalArgumentException) {}
    }
}
