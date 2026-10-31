package com.stillswarm.base.telemetry

import org.junit.Assert.*
import org.junit.Test

class TelemetryEventTest {
    @Test
    fun eventCopiesParameters() {
        val parameters = mutableMapOf<String, TelemetryValue>("count" to TelemetryValue.Integer(1))
        val event = TelemetryEvent(name = "item_click", parameters = parameters)
        parameters.clear()
        assertEquals(TelemetryValue.Integer(1), event.parameters["count"])
    }

    @Test
    fun invalidNamesAndReservedPrefixesAreRejected() {
        listOf(
                "",
                "bad-name",
                "1event",
                "firebase_event",
                "google_event",
                "ga_event",
                "screen_view",
                "a".repeat(41),
            )
            .forEach {
                try {
                    TelemetryEvent(it)
                    fail("Expected invalid name rejection")
                } catch (_: IllegalArgumentException) {}
            }
    }

    @Test
    fun parameterLimitsAndNonFiniteValuesAreRejected() {
        try {
            TelemetryValue.Decimal(Double.NaN)
            fail("Expected rejection")
        } catch (_: IllegalArgumentException) {}
        try {
            TelemetryValue.Text("x".repeat(101))
            fail("Expected rejection")
        } catch (_: IllegalArgumentException) {}
        try {
            TelemetryEvent(
                name = "event",
                parameters = (1..26).associate { "key$it" to TelemetryValue.Integer(it.toLong()) },
            )
            fail("Expected rejection")
        } catch (_: IllegalArgumentException) {}
    }
}
