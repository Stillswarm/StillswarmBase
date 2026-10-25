package com.example.stillswarmbase

import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import com.stillswarm.base.storage.PreferenceKey
import com.stillswarm.base.storage.defaultPreferenceStore
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test

class PreferencesDemoTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Test
    fun themeToggleWritesToStorageAndSurvivesRecreation() {
        val store = defaultPreferenceStore(compose.activity)
        val dark = PreferenceKey.boolean("demo.dark")
        val original = runBlocking { store.get(dark) }
        try {
            compose.waitUntil(timeoutMillis = 5_000) {
                compose.onAllNodesWithContentDescription(label = "Dark appearance")
                    .fetchSemanticsNodes().isNotEmpty()
            }
            compose.onNodeWithContentDescription(label = "Dark appearance").performClick()
            compose.waitUntil(timeoutMillis = 5_000) {
                runBlocking { store.get(dark) } == !original
            }
            compose.activityRule.scenario.recreate()
            compose.waitUntil(timeoutMillis = 5_000) {
                compose.onAllNodesWithContentDescription(label = "Dark appearance")
                    .fetchSemanticsNodes().isNotEmpty()
            }
            val toggle = compose.onNodeWithContentDescription(label = "Dark appearance")
            if (original) toggle.assertIsOff() else toggle.assertIsOn()
        } finally {
            runBlocking { store.set(dark, original) }
        }
    }
}
