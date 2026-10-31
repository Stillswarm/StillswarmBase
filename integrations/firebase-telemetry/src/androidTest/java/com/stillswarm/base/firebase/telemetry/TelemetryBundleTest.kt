package com.stillswarm.base.firebase.telemetry

import com.stillswarm.base.telemetry.TelemetryEvent
import com.stillswarm.base.telemetry.TelemetryValue
import org.junit.Assert.*
import org.junit.Test

class TelemetryBundleTest {
    @Test
    fun mapsValuesToSupportedFirebaseBundleTypes() {
        val bundle =
            TelemetryEvent(
                    name = "sample_event",
                    parameters =
                        mapOf(
                            "text" to TelemetryValue.Text("sample"),
                            "count" to TelemetryValue.Integer(42),
                            "ratio" to TelemetryValue.Decimal(1.5),
                            "enabled" to TelemetryValue.Flag(true),
                            "disabled" to TelemetryValue.Flag(false),
                        ),
                )
                .toBundle()
        assertEquals("sample", bundle.getString("text"))
        assertEquals(42L, bundle.getLong("count"))
        assertEquals(1.5, bundle.getDouble("ratio"), 0.0)
        assertEquals(1L, bundle.getLong("enabled"))
        assertEquals(0L, bundle.getLong("disabled"))
    }
}
