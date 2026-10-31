package com.stillswarm.base.config

enum class ConfigValueSource {
    Default,
    Remote,
    LocalOverride,
}

enum class ConfigFetchStatus {
    NotStarted,
    Disabled,
    Fetching,
    Applied,
    TimedOut,
    Failed,
    Rejected,
    Cancelled,
}

enum class ConfigIssue {
    StorageReadFailed,
    InvalidStoredValues,
    StorageWriteFailed,
}

/**
 * Immutable effective values and their provenance. Read multiple keys from one snapshot for
 * consistency.
 */
class ConfigSnapshot
internal constructor(
    private val keys: Map<String, ConfigKey<*>>,
    private val remote: Map<String, String> = emptyMap(),
    private val overrides: Map<String, String> = emptyMap(),
) {
    operator fun <T : Any> get(key: ConfigKey<T>): T {
        checkKey(key)
        return (overrides[key.name] ?: remote[key.name])?.let(key::parse) ?: key.defaultValue
    }

    fun source(key: ConfigKey<*>): ConfigValueSource {
        checkKey(key)
        return when (key.name) {
            in overrides -> ConfigValueSource.LocalOverride
            in remote -> ConfigValueSource.Remote
            else -> ConfigValueSource.Default
        }
    }

    private fun checkKey(key: ConfigKey<*>) {
        require(keys[key.name] === key) { "Use the registered ConfigKey instance: ${key.name}" }
    }
}

data class ConfigState(
    val snapshot: ConfigSnapshot,
    val localLoaded: Boolean = false,
    val fetchStatus: ConfigFetchStatus = ConfigFetchStatus.NotStarted,
    val issues: Set<ConfigIssue> = emptySet(),
)

/** Return a complete snapshot. Missing keys revert to defaults; unknown keys are ignored. */
fun interface RemoteConfigSource {
    /**
     * Must cooperate with coroutine cancellation. No connectivity listener or retry loop is needed.
     */
    suspend fun fetch(): Map<String, String>
}
