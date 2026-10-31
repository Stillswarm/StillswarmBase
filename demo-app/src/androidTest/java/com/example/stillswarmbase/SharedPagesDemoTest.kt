package com.example.stillswarmbase

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import org.junit.Rule
import org.junit.Test

class SharedPagesDemoTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Test fun opensSettingsAndAboutAndReturnsToGallery() {
        compose.waitUntil(timeoutMillis = 5_000) {
            compose.onAllNodesWithContentDescription("More options").fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithContentDescription("More options").performClick()
        compose.onNodeWithText("Settings").performClick()
        compose.onNodeWithText("Appearance").assertIsDisplayed()
        compose.onNodeWithText("About").performScrollTo().performClick()
        compose.onNodeWithText("Version ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})").assertIsDisplayed()
        compose.onNodeWithContentDescription("Back").performClick()
        compose.onNodeWithText("Appearance").assertIsDisplayed()
        compose.onNodeWithContentDescription("Back").performClick()
        compose.onNodeWithContentDescription("More options").assertIsDisplayed()
    }
}
