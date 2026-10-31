package com.example.stillswarmbase

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class DeepLinkTest {
    @get:Rule val compose = createEmptyComposeRule()
    private val context: Context
        get() = ApplicationProvider.getApplicationContext()

    private fun link(path: String) =
        Intent(
            Intent.ACTION_VIEW,
            Uri.parse("stillswarm-demo://app/$path"),
            context,
            MainActivity::class.java,
        )

    private fun awaitText(text: String) {
        compose.waitUntil(timeoutMillis = 5_000) {
            compose.onAllNodesWithText(text = text).fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun coldLinkAndRecreationDoNotReplayConsumedNavigation() {
        ActivityScenario.launch<MainActivity>(link("entry/cold-entry")).use { scenario ->
            awaitText("Entry: cold-entry")
            scenario.recreate()
            awaitText("Entry: cold-entry")
            compose.onNodeWithText(text = "Back").performClick()
            awaitText("Component gallery")
            scenario.recreate()
            awaitText("Component gallery")
            compose.onNodeWithText(text = "Entry: cold-entry").assertDoesNotExist()
        }
    }

    @Test
    fun warmAndRepeatedLinksReplaceTheStackAndKeepSameActivity() {
        val launchIntent = link("gallery")
        ActivityScenario.launch<MainActivity>(launchIntent).use { scenario ->
            awaitText("Component gallery")
            lateinit var original: MainActivity
            scenario.onActivity { original = it }
            try {
                compose.runOnIdle {
                    original.startActivity(link("entry/first").addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP))
                }
                awaitText("Entry: first")
                compose.runOnIdle {
                    assertTrue(
                        androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry.getInstance()
                            .getActivitiesInStage(androidx.test.runner.lifecycle.Stage.RESUMED)
                            .contains(original)
                    )
                    original.startActivity(link("entry/first").addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP))
                }
                compose.waitForIdle()
                compose.runOnIdle {
                    original.startActivity(link("entry/second").addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP))
                }
                awaitText("Entry: second")
                compose.onNodeWithText(text = "Back").performClick()
                awaitText("Component gallery")
                compose.runOnIdle {
                    original.startActivity(link("entry/second").addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP))
                }
                awaitText("Entry: second")
            } finally {
                // ActivityScenario matches lifecycle events using its original launch intent.
                // Restore it only for test cleanup after real onNewIntent deliveries changed it.
                compose.runOnIdle { original.intent = launchIntent }
            }
        }
    }

    @Test
    fun rejectedWarmLinkLeavesCurrentScreenUnchanged() {
        val launchIntent = link("entry/valid")
        ActivityScenario.launch<MainActivity>(launchIntent).use { scenario ->
            awaitText("Entry: valid")
            lateinit var original: MainActivity
            scenario.onActivity { original = it }
            try {
                compose.runOnIdle {
                    original.startActivity(link("entry/%2F").addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP))
                }
                compose.waitUntil(timeoutMillis = 5_000) {
                    original.intent.data?.encodedPath == "/entry/%2F"
                }
                compose.onNodeWithText(text = "Entry: valid").assertIsDisplayed()
                compose.runOnIdle {
                    original.startActivity(link("unknown").addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP))
                }
                compose.waitUntil(timeoutMillis = 5_000) {
                    original.intent.data?.path == "/unknown"
                }
                compose.onNodeWithText(text = "Entry: valid").assertIsDisplayed()
            } finally {
                compose.runOnIdle { original.intent = launchIntent }
            }
        }
    }

    @Test
    fun malformedColdLinkFallsBackToGallery() {
        ActivityScenario.launch<MainActivity>(link("entry/%2F")).use {
            awaitText("Component gallery")
        }
    }

    @Test
    fun manifestResolvesExternalRoomLink() {
        val intent =
            Intent(Intent.ACTION_VIEW, Uri.parse("stillswarm-demo://app/room"))
                .addCategory(Intent.CATEGORY_BROWSABLE)
                .setPackage(context.packageName)
        assertNotNull(context.packageManager.resolveActivity(intent, 0))
        ActivityScenario.launch<MainActivity>(intent).use {
            awaitText("Room demo")
            compose.onNodeWithContentDescription(label = "Back").performClick()
            awaitText("Component gallery")
        }
    }
}
