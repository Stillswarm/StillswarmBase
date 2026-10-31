package com.example.stillswarmbase

import android.app.Application
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.stillswarm.base.config.ConfigKey
import com.stillswarm.base.config.ConfigSession
import com.stillswarm.base.config.RemoteConfigSource
import com.stillswarm.base.firebase.config.FirebaseRemoteConfigSource
import com.stillswarm.base.storage.defaultPreferenceStore
import kotlinx.coroutines.*

internal object DemoConfigKeys {
    val welcome =
        ConfigKey.string(
            name = "welcome_text",
            defaultValue = "Welcome",
            validate = { it.isNotBlank() && it.length <= 80 },
        )
    val suggestions = ConfigKey.boolean(name = "suggestions_enabled", defaultValue = false)
    val limit = ConfigKey.int(
        name = "daily_limit",
        defaultValue = 5,
        validate = { it in 1..100 },
    )
    val all = listOf(welcome, suggestions, limit)
}

/** One config session per process, independent of Activity recreation and screen navigation. */
open class DemoApplication : Application() {
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    internal lateinit var config: ConfigSession
        private set

    protected open fun createRemoteSource(): RemoteConfigSource =
        FirebaseRemoteConfigSource(
            remoteConfig = FirebaseRemoteConfig.getInstance(),
            minimumFetchIntervalSeconds = if (BuildConfig.DEBUG) 0 else 43_200,
        )

    protected open val configNamespace: String = "demo_firebase"

    override fun onCreate() {
        super.onCreate()
        config =
            ConfigSession(
                keys = DemoConfigKeys.all,
                storage = defaultPreferenceStore(this),
                remoteSource = createRemoteSource(),
                fetchTimeoutMillis = 5_000,
                namespace = configNamespace,
            )
        appScope.launch {
            val started = android.os.SystemClock.elapsedRealtime()
            config.start()
            if (BuildConfig.DEBUG) {
                android.util.Log.i(
                    "DemoConfig",
                    "Startup finished in ${android.os.SystemClock.elapsedRealtime() - started}ms; " +
                        "fetch=${config.state.value.fetchStatus}; " +
                        "welcomeSource=${config.state.value.snapshot.source(DemoConfigKeys.welcome)}",
                )
            }
        }
    }
}
