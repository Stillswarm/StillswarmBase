package com.example.stillswarmbase

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.espresso.Espresso.pressBack
import org.junit.Rule
import org.junit.Test

class NavigationDemoTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Test
    fun viewModelNavigationAndBackReturnToGallery() {
        compose.onNodeWithText(text = "Try navigation").performClick()
        compose.onNodeWithText(text = "Entry: sample-entry").assertIsDisplayed()
        compose.onNodeWithText(
            text = "Back",
            substring = false,
        ).performClick()
        compose.onNodeWithText(text = "Component gallery").assertIsDisplayed()
    }

    @Test
    fun replaceDoesNotLeavePreviousDetailsOnStack() {
        compose.onNodeWithText(text = "Try navigation").performClick()
        compose.onNodeWithText(text = "Replace this screen").performClick()
        compose.onNodeWithText(text = "Entry: replacement-entry").assertIsDisplayed()
        pressBack()
        compose.onNodeWithText(text = "Component gallery").assertIsDisplayed()
    }

    @Test
    fun resetCreatesNewRootAndRecreationDoesNotReplayNavigation() {
        compose.onNodeWithText(text = "Try navigation").performClick()
        compose.onNodeWithText(text = "Entry: sample-entry").assertIsDisplayed()
        compose.activityRule.scenario.recreate()
        compose.onNodeWithText(text = "Entry: sample-entry").assertIsDisplayed()
        compose.onNodeWithText(
            text = "Back",
            substring = false,
        ).performClick()
        compose.onNodeWithText(text = "Component gallery").assertIsDisplayed()
        compose.onNodeWithText(text = "Try navigation").performClick()
        compose.onNodeWithText(text = "Reset to gallery").performClick()
        compose.onNodeWithText(text = "Component gallery").assertIsDisplayed()
        // A fresh detail entry should return directly to the new root.
        compose.onNodeWithText(text = "Try navigation").performClick()
        pressBack()
        compose.onNodeWithText(text = "Component gallery").assertIsDisplayed()
    }
}
