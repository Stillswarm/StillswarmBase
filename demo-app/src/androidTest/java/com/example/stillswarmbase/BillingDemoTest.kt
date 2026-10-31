package com.example.stillswarmbase

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import org.junit.Rule
import org.junit.Test

class BillingDemoTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Test
    fun pendingCompletionOfflineRestoreAndRefund() {
        compose.waitUntil(timeoutMillis = 5_000) {
            compose.onAllNodesWithText("Try payments").fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithText("Try payments").performScrollTo().performClick()
        compose.onNodeWithText("Next result: Pending").performScrollTo().performClick()
        compose.onNodeWithText("Unlock — ₹29 (simulated)").performScrollTo().performClick()
        compose
            .onNodeWithText("Payment pending — premium is locked")
            .performScrollTo()
            .assertIsDisplayed()
        compose.onNodeWithText("Unlock — ₹29 (simulated)").assertIsNotEnabled()
        compose.onNodeWithText("Complete pending payment").performScrollTo().performClick()
        compose.onNodeWithText("Premium active").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Simulate offline").performScrollTo().performClick()
        compose.onNodeWithText("Restore purchases").performScrollTo().performClick()
        compose.onNodeWithText("Premium active").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Go online").performScrollTo().performClick()
        compose.onNodeWithText("Simulate refund / reset").performScrollTo().performClick()
        compose.onNodeWithText("Premium locked").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Google Play checkout").performScrollTo().performClick()
        compose.onNodeWithText("Play Billing is not configured for this build.").assertIsDisplayed()
    }
}
