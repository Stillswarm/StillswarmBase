package com.stillswarm.base.firebase.telemetry

import com.stillswarm.base.telemetry.TelemetryEvent
import org.junit.Assert.*
import org.junit.Test

class FirebaseTelemetryTest {
    private class Backend : FirebaseTelemetryBackend {
        val calls = mutableListOf<String>()
        var failEvents = false

        override fun analyticsCollection(enabled: Boolean) {
            calls.add("analytics:$enabled")
        }

        override fun crashCollection(enabled: Boolean) {
            calls.add("crashes:$enabled")
        }

        override fun event(event: TelemetryEvent) {
            if (failEvents) error("sdk failure")
            calls.add(event.name)
        }

        override fun screen(name: String) {
            calls.add("screen:$name")
        }

        override fun breadcrumb(message: String) {
            calls.add("log:$message")
        }

        override fun exception(error: Throwable) {
            calls.add("error:${error.message}")
        }
    }

    @Test
    fun disabledByDefaultAndNoReplayWhenEnabled() {
        val backend = Backend()
        val telemetry = FirebaseTelemetry(backend = backend)
        telemetry.track(TelemetryEvent("ignored"))
        telemetry.recordException(Exception("ignored"))
        telemetry.setAnalyticsEnabled(true)
        telemetry.track(TelemetryEvent("click"))
        telemetry.screen("home")
        assertEquals(
            listOf("analytics:false", "crashes:false", "analytics:true", "click", "screen:home"),
            backend.calls,
        )
    }

    @Test
    fun collectionControlsAreIndependent() {
        val backend = Backend()
        val telemetry =
            FirebaseTelemetry(
                backend = backend,
                analyticsEnabled = true,
                crashReportingEnabled = true,
            )
        telemetry.setAnalyticsEnabled(false)
        telemetry.track(TelemetryEvent("ignored"))
        telemetry.breadcrumb("save")
        telemetry.recordException(Exception("failure"))
        telemetry.setCrashReportingEnabled(false)
        telemetry.recordException(Exception("ignored"))
        assertFalse(backend.calls.contains("ignored"))
        assertTrue(backend.calls.contains("error:failure"))
        assertTrue(backend.calls.contains("log:save"))
        assertFalse(backend.calls.contains("error:ignored"))
    }

    @Test
    fun sdkAndDiagnosticFailuresDoNotBreakAppActions() {
        val backend = Backend().apply { failEvents = true }
        var failures = 0
        val telemetry =
            FirebaseTelemetry(
                backend = backend,
                analyticsEnabled = true,
                onFailure = {
                    failures++
                    error("diagnostic failure")
                },
            )
        telemetry.track(TelemetryEvent("click"))
        assertEquals(1, failures)
    }
}
