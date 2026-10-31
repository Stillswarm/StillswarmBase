package com.stillswarm.base.config

import com.stillswarm.base.storage.PreferenceKey
import com.stillswarm.base.storage.PreferenceStore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * One process-scoped instance per namespace. Call start at cold start in an app-owned scope.
 * Defaults are available immediately; localLoaded indicates the initial storage read has completed; inspect issues for failures.
 * Repeated/concurrent start calls never repeat the remote fetch. Create a new session next process.
 */
class ConfigSession(
    keys: List<ConfigKey<*>>,
    private val storage: PreferenceStore,
    private val remoteSource: RemoteConfigSource? = null,
    private val fetchTimeoutMillis: Long = 2_000,
    namespace: String = "default",
) {
    private val keysByName = keys.associateBy { it.name }
    private val remoteKey = PreferenceKey.string("config.$namespace.remote", "{}")
    private val overrideKey = PreferenceKey.string("config.$namespace.overrides", "{}")
    private val startMutex = Mutex()
    private val mutationMutex = Mutex()
    private var started = false
    private var overrideReadFailed = false
    private var remoteValues: Map<String, String> = emptyMap()
    private var overrides: Map<String, String> = emptyMap()
    private val mutableState = MutableStateFlow(ConfigState(snapshot = ConfigSnapshot(keys = keysByName)))
    val state: StateFlow<ConfigState> = mutableState.asStateFlow()

    init {
        require(keysByName.size == keys.size) { "Duplicate config key names" }
        require(fetchTimeoutMillis > 0)
        require(namespace.matches(Regex("[A-Za-z0-9_-]+"))) { "Invalid namespace" }
    }

    operator fun <T : Any> get(key: ConfigKey<T>): T = state.value.snapshot[key]

    fun <T : Any> observe(key: ConfigKey<T>): Flow<T> {
        requireRegistered(key)
        return state.map { it.snapshot[key] }.distinctUntilChanged()
    }

    suspend fun start() =
        startMutex.withLock {
            if (started) return@withLock
            started = true
            try {
                mutationMutex.withLock {
                    loadLocal()
                    publish(
                        status =
                            if (remoteSource == null) ConfigFetchStatus.Disabled
                            else ConfigFetchStatus.Fetching
                    )
                }
                val source = remoteSource ?: return@withLock
                val fetched =
                    try {
                        withTimeoutOrNull(fetchTimeoutMillis) { source.fetch().toMap() }
                    } catch (cancelled: CancellationException) {
                        throw cancelled
                    } catch (_: Exception) {
                        mutationMutex.withLock { publish(status = ConfigFetchStatus.Failed) }
                        return@withLock
                    }
                mutationMutex.withLock {
                    if (fetched == null) {
                        publish(status = ConfigFetchStatus.TimedOut)
                    } else if (
                        fetched.any { (name, value) -> keysByName[name]?.accepts(value) == false }
                    ) {
                        publish(status = ConfigFetchStatus.Rejected)
                    } else {
                        val accepted = fetched.filterKeys { it in keysByName }
                        try {
                            // One durable write per snapshot. A failed save keeps the previous
                            // session values.
                            storage.set(remoteKey, Json.encodeToString(accepted))
                            remoteValues = accepted
                            publish(status = ConfigFetchStatus.Applied)
                        } catch (cancelled: CancellationException) {
                            throw cancelled
                        } catch (_: Exception) {
                            issue(ConfigIssue.StorageWriteFailed)
                            publish(status = ConfigFetchStatus.Failed)
                        }
                    }
                }
            } catch (cancelled: CancellationException) {
                mutableState.update { it.copy(fetchStatus = ConfigFetchStatus.Cancelled) }
                throw cancelled
            }
        }

    suspend fun <T : Any> setOverride(key: ConfigKey<T>, value: T) {
        requireRegistered(key)
        val encoded = key.serialize(value)
        mutateOverrides { it + (key.name to encoded) }
    }

    suspend fun removeOverride(key: ConfigKey<*>) {
        requireRegistered(key)
        mutateOverrides { it - key.name }
    }

    /**
     * Clears only this config namespace's overrides, leaving remote cache and other preferences
     * intact.
     */
    suspend fun clearOverrides() = mutateOverrides { emptyMap() }

    private suspend fun mutateOverrides(change: (Map<String, String>) -> Map<String, String>) {
        mutationMutex.withLock {
            // Also supports local-only callers that edit before start; start still owns the remote
            // fetch.
            if (!state.value.localLoaded) loadLocal()
            if (overrideReadFailed) {
                overrides = readValues(overrideKey)
                check(!overrideReadFailed) {
                    "Cannot edit overrides until stored overrides can be read"
                }
            }
            val next = change(overrides).toMap()
            try {
                storage.set(overrideKey, Json.encodeToString(next))
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (failure: Exception) {
                issue(ConfigIssue.StorageWriteFailed)
                throw failure
            }
            overrides = next
            publish()
        }
    }

    private suspend fun loadLocal() {
        if (state.value.localLoaded) return
        remoteValues = readValues(remoteKey)
        overrides = readValues(overrideKey)
        mutableState.update {
            it.copy(
                localLoaded = true,
                snapshot =
                    ConfigSnapshot(
                        keys = keysByName,
                        remote = remoteValues,
                        overrides = overrides,
                    ),
            )
        }
    }

    private suspend fun readValues(key: PreferenceKey<String>): Map<String, String> {
        val raw =
            try {
                storage.get(key)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                if (key === overrideKey) overrideReadFailed = true
                issue(ConfigIssue.StorageReadFailed)
                return emptyMap()
            }
        if (key === overrideKey) overrideReadFailed = false
        val values =
            try {
                Json.decodeFromString<Map<String, String>>(raw)
            } catch (_: Exception) {
                issue(ConfigIssue.InvalidStoredValues)
                return emptyMap()
            }
        return values.filter { (name, value) ->
            val keyDefinition = keysByName[name]
            when {
                keyDefinition == null -> false
                keyDefinition.accepts(value) -> true
                else -> {
                    issue(ConfigIssue.InvalidStoredValues)
                    false
                }
            }
        }
    }

    private fun requireRegistered(key: ConfigKey<*>) {
        require(keysByName[key.name] === key) {
            "Use the registered ConfigKey instance: ${key.name}"
        }
    }

    private fun issue(issue: ConfigIssue) {
        mutableState.update { it.copy(issues = it.issues + issue) }
    }

    private fun publish(status: ConfigFetchStatus = state.value.fetchStatus) {
        mutableState.update {
            it.copy(
                snapshot =
                    ConfigSnapshot(
                        keys = keysByName,
                        remote = remoteValues,
                        overrides = overrides,
                    ),
                fetchStatus = status,
            )
        }
    }
}
