package com.stillswarm.base.firebase.config

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigValue
import com.stillswarm.base.config.RemoteConfigSource
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.tasks.await

/**
 * Adapter only: ConfigSession owns the deadline, validation, persistence, and active app values.
 * Keep one owner of fetch/activate for this FirebaseRemoteConfig instance; do not add realtime
 * listeners.
 */
class FirebaseRemoteConfigSource
internal constructor(
    private val client: FirebaseConfigClient,
    private val minimumFetchIntervalSeconds: Long,
) : RemoteConfigSource {
    constructor(
        remoteConfig: FirebaseRemoteConfig,
        minimumFetchIntervalSeconds: Long = 43_200,
    ) : this(SdkConfigClient(remoteConfig), minimumFetchIntervalSeconds)

    init {
        require(minimumFetchIntervalSeconds >= 0)
    }

    override suspend fun fetch(): Map<String, String> {
        client.fetch(minimumFetchIntervalSeconds)
        currentCoroutineContext().ensureActive()
        client.activate()
        currentCoroutineContext().ensureActive()
        return client.remoteValues().toMap()
    }
}

internal interface FirebaseConfigClient {
    suspend fun fetch(minimumFetchIntervalSeconds: Long)

    suspend fun activate()

    fun remoteValues(): Map<String, String>
}

private class SdkConfigClient(private val config: FirebaseRemoteConfig) : FirebaseConfigClient {
    override suspend fun fetch(minimumFetchIntervalSeconds: Long) {
        config.fetch(minimumFetchIntervalSeconds).await()
    }

    override suspend fun activate() {
        config.activate().await()
    }

    override fun remoteValues() = remoteValuesOnly(config.all)
}

/** Firebase defaults/static fallbacks must not override the core ConfigKey defaults. */
internal fun remoteValuesOnly(values: Map<String, FirebaseRemoteConfigValue>): Map<String, String> =
    values
        .filterValues { it.source == FirebaseRemoteConfig.VALUE_SOURCE_REMOTE }
        .mapValues { it.value.asString() }
