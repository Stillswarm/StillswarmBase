package com.example.stillswarmbase

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.espresso.Espresso.closeSoftKeyboard
import com.stillswarm.base.config.ConfigKey
import com.stillswarm.base.config.ConfigFetchStatus
import com.stillswarm.base.config.ConfigValueSource
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test

class ConfigDemoTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    private fun awaitText(text: String) {
        compose.waitUntil(timeoutMillis = 5_000) {
            compose.onAllNodesWithText(text = text).fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun editValidateAndClearAnOverride() {
        val config = (compose.activity.application as DemoApplication).config
        compose.waitUntil(timeoutMillis = 5_000) {
            config.state.value.fetchStatus == ConfigFetchStatus.Applied
        }
        val original = config.state.value.snapshot
        try {
            awaitText("Try config")
            compose.onNodeWithText(text = "Try config").performScrollTo().performClick()
            compose.onNode(hasSetTextAction()).performTextInput("37")
            closeSoftKeyboard()
            compose.onNodeWithText(text = "Save limit override").performScrollTo().performClick()
            awaitText("Daily limit: 37 (LocalOverride)")
            compose.activityRule.scenario.recreate()
            awaitText("Daily limit: 37 (LocalOverride)")
            compose.onNode(hasSetTextAction()).performTextReplacement("101")
            closeSoftKeyboard()
            compose.onNodeWithText(text = "Save limit override").performScrollTo().performClick()
            awaitText("Enter a limit from 1 to 100.")
            awaitText("Daily limit: 37 (LocalOverride)")
            compose.onNodeWithText(text = "Clear local overrides").performScrollTo().performClick()
            awaitText("Daily limit: 10 (Remote)")
        } finally {
            runBlocking {
                suspend fun <T : Any> restore(key: ConfigKey<T>) {
                    if (original.source(key) == ConfigValueSource.LocalOverride) config.setOverride(key, original[key])
                    else config.removeOverride(key)
                }
                restore(DemoConfigKeys.limit)
                restore(DemoConfigKeys.suggestions)
                restore(DemoConfigKeys.welcome)
            }
        }
    }
}
