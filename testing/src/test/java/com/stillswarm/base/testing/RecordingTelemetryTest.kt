package com.stillswarm.base.testing

import com.stillswarm.base.telemetry.*
import org.junit.Assert.*
import org.junit.Test

class RecordingTelemetryTest {
    @Test
    fun recordsAndDropsDisabledCallsIndependently() {
        val telemetry = RecordingTelemetry()
        telemetry.track(TelemetryEvent("click"))
        telemetry.screen("home")
        telemetry.setAnalyticsEnabled(false)
        telemetry.track(TelemetryEvent("ignored"))
        telemetry.recordException(Exception("failure"))
        telemetry.setCrashReportingEnabled(false)
        telemetry.recordException(Exception("ignored"))
        assertEquals(listOf("click"), telemetry.events.map { it.name })
        assertEquals(listOf("home"), telemetry.screens)
        assertEquals(1, telemetry.exceptions.size)
        assertTrue(RecordingTelemetry().events.isEmpty())
    }
}
