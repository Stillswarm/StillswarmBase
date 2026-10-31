package com.stillswarm.base.firebase.telemetry

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.stillswarm.base.telemetry.*

/**
 * SDKs own offline storage/upload and uncaught-crash capture. This adapter adds no queue or worker.
 */
class FirebaseTelemetry
internal constructor(
    private val backend: FirebaseTelemetryBackend,
    analyticsEnabled: Boolean = false,
    crashReportingEnabled: Boolean = false,
    private val onFailure: (Exception) -> Unit = {},
) : Telemetry {
    constructor(
        analytics: FirebaseAnalytics,
        crashlytics: FirebaseCrashlytics,
        analyticsEnabled: Boolean = false,
        crashReportingEnabled: Boolean = false,
        onFailure: (Exception) -> Unit = {},
    ) : this(
        backend = SdkTelemetryBackend(analytics = analytics, crashlytics = crashlytics),
        analyticsEnabled = analyticsEnabled,
        crashReportingEnabled = crashReportingEnabled,
        onFailure = onFailure,
    )

    private val lock = Any()
    @Volatile private var analyticsAllowed = false
    @Volatile private var crashesAllowed = false
    override val analyticsEnabled: Boolean
        get() = analyticsAllowed

    override val crashReportingEnabled: Boolean
        get() = crashesAllowed

    init {
        setAnalyticsEnabled(analyticsEnabled)
        setCrashReportingEnabled(crashReportingEnabled)
    }

    override fun setAnalyticsEnabled(enabled: Boolean) =
        synchronized(lock) {
            if (!enabled) analyticsAllowed = false
            safely {
                backend.analyticsCollection(enabled)
                analyticsAllowed = enabled
            }
        }

    override fun setCrashReportingEnabled(enabled: Boolean) =
        synchronized(lock) {
            if (!enabled) crashesAllowed = false
            safely {
                backend.crashCollection(enabled)
                crashesAllowed = enabled
            }
        }

    override fun track(event: TelemetryEvent) =
        synchronized(lock) { if (analyticsAllowed) safely { backend.event(event) } }

    override fun screen(name: String) =
        synchronized(lock) {
            require(name.isNotBlank() && name.length <= 100) {
                "Screen name must contain 1–100 characters"
            }
            if (analyticsAllowed) safely { backend.screen(name) }
        }

    override fun breadcrumb(message: String) =
        synchronized(lock) { if (crashesAllowed) safely { backend.breadcrumb(message) } }

    override fun recordException(error: Throwable) =
        synchronized(lock) { if (crashesAllowed) safely { backend.exception(error) } }

    private inline fun safely(block: () -> Unit) {
        try {
            block()
        } catch (failure: Exception) {
            // Telemetry transport must not break the user action; diagnostics must not recurse
            // here.
            try {
                onFailure(failure)
            } catch (_: Exception) {}
        }
    }
}

internal interface FirebaseTelemetryBackend {
    fun analyticsCollection(enabled: Boolean)

    fun crashCollection(enabled: Boolean)

    fun event(event: TelemetryEvent)

    fun screen(name: String)

    fun breadcrumb(message: String)

    fun exception(error: Throwable)
}

private class SdkTelemetryBackend(
    private val analytics: FirebaseAnalytics,
    private val crashlytics: FirebaseCrashlytics,
) : FirebaseTelemetryBackend {
    override fun analyticsCollection(enabled: Boolean) {
        analytics.setAnalyticsCollectionEnabled(enabled)
    }

    override fun crashCollection(enabled: Boolean) {
        crashlytics.setCrashlyticsCollectionEnabled(enabled)
    }

    override fun event(event: TelemetryEvent) {
        analytics.logEvent(event.name, event.toBundle())
    }

    override fun screen(name: String) {
        val parameters = Bundle().apply { putString(FirebaseAnalytics.Param.SCREEN_NAME, name) }
        analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, parameters)
    }

    override fun breadcrumb(message: String) {
        crashlytics.log(message)
    }

    override fun exception(error: Throwable) {
        crashlytics.recordException(error)
    }
}

internal fun TelemetryEvent.toBundle(): Bundle =
    Bundle().apply {
        parameters.forEach { (key, value) ->
            when (value) {
                is TelemetryValue.Text -> putString(key, value.value)
                is TelemetryValue.Integer -> putLong(key, value.value)
                is TelemetryValue.Decimal -> putDouble(key, value.value)
                is TelemetryValue.Flag -> putLong(key, if (value.value) 1L else 0L)
            }
        }
    }
