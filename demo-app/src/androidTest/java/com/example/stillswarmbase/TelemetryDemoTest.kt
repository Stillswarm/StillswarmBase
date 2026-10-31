package com.example.stillswarmbase

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.stillswarm.base.testing.RecordingTelemetry
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class TelemetryDemoTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Test
    fun recordsImpressionClicksAndNonFatalWithoutRecompositionDuplicates() {
        val recorder =
            (compose.activity.application as DemoApplication).telemetry as RecordingTelemetry
        val initialScreens = recorder.screens.size
        val initialEvents = recorder.events.size
        val initialErrors = recorder.exceptions.size
        try {
            compose.waitUntil(timeoutMillis = 5_000) {
                compose
                    .onAllNodesWithText(text = "Try telemetry")
                    .fetchSemanticsNodes()
                    .isNotEmpty()
            }
            compose.onNodeWithText(text = "Try telemetry").performScrollTo().performClick()
            compose.waitForIdle()
            assertEquals(initialScreens + 1, recorder.screens.size)
            assertEquals("component_impression", recorder.events.last().name)
            compose.onNodeWithText(text = "Send sample event").performScrollTo().performClick()
            compose.waitForIdle()
            assertEquals("button_click", recorder.events.last().name)
            compose.onNodeWithText(text = "Toggle analytics").performScrollTo().performClick()
            compose.onNodeWithText(text = "Send sample event").assertIsNotEnabled()
            assertEquals(initialEvents + 2, recorder.events.size)
            assertEquals(initialScreens + 1, recorder.screens.size)
            compose
                .onNodeWithText(text = "Report sample non-fatal")
                .performScrollTo()
                .performClick()
            compose.waitForIdle()
            assertEquals(initialErrors + 1, recorder.exceptions.size)
            compose.onNodeWithText(text = "Toggle crash reporting").performScrollTo().performClick()
            compose.onNodeWithText(text = "Report sample non-fatal").assertIsNotEnabled()
        } finally {
            recorder.setAnalyticsEnabled(true)
            recorder.setCrashReportingEnabled(true)
        }
    }
}
