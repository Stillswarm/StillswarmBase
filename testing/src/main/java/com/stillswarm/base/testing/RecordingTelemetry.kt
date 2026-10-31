package com.stillswarm.base.testing

import com.stillswarm.base.telemetry.*

/** Thread-safe recorder for tests. Disabled calls are dropped, never replayed after enabling. */
class RecordingTelemetry : Telemetry {
    private val lock = Any()
    private val recordedEvents = mutableListOf<TelemetryEvent>()
    private val recordedScreens = mutableListOf<String>()
    private val recordedBreadcrumbs = mutableListOf<String>()
    private val recordedExceptions = mutableListOf<Throwable>()
    @Volatile private var analyticsAllowed = true
    override val analyticsEnabled
        get() = analyticsAllowed

    @Volatile private var crashesAllowed = true
    override val crashReportingEnabled
        get() = crashesAllowed

    val events
        get() = synchronized(lock) { recordedEvents.toList() }

    val screens
        get() = synchronized(lock) { recordedScreens.toList() }

    val breadcrumbs
        get() = synchronized(lock) { recordedBreadcrumbs.toList() }

    val exceptions
        get() = synchronized(lock) { recordedExceptions.toList() }

    override fun setAnalyticsEnabled(enabled: Boolean) =
        synchronized(lock) { analyticsAllowed = enabled }

    override fun setCrashReportingEnabled(enabled: Boolean) =
        synchronized(lock) { crashesAllowed = enabled }

    override fun track(event: TelemetryEvent) =
        synchronized(lock) {
            if (analyticsEnabled) {
                recordedEvents.add(event)
            }
            Unit
        }

    override fun screen(name: String) =
        synchronized(lock) {
            require(name.isNotBlank() && name.length <= 100)
            if (analyticsEnabled) recordedScreens.add(name)
            Unit
        }

    override fun breadcrumb(message: String) =
        synchronized(lock) {
            if (crashReportingEnabled) recordedBreadcrumbs.add(message)
            Unit
        }

    override fun recordException(error: Throwable) =
        synchronized(lock) {
            if (crashReportingEnabled) recordedExceptions.add(error)
            Unit
        }
}
