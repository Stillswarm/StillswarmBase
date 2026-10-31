package com.stillswarm.base.shell.pages

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.stillswarm.base.ui.StillswarmColors
import com.stillswarm.base.ui.StillswarmTheme
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class SharedPagesTest {
    @get:Rule val compose = createComposeRule()

    @Test fun switchRowHasOneToggleTargetAndDisabledRowsDoNotAct() {
        val checked = mutableStateOf(false)
        var calls = 0
        compose.setContent {
            SettingsScreen(
                onBack = {},
                content = { item {
                    SettingsSection(
                        title = "Appearance",
                        content = {
                            SettingsSwitchRow(
                                title = "Dark theme",
                                checked = checked.value,
                                onCheckedChange = { checked.value = it; calls++ },
                            )
                            SettingsActionRow(
                                title = "Unavailable",
                                onClick = { calls++ },
                                enabled = false,
                            )
                        },
                    )
                } },
            )
        }
        compose.onAllNodes(isToggleable()).assertCountEquals(1)
        compose.onNodeWithText("Dark theme").performClick().assertIsOn()
        compose.onNodeWithText("Unavailable").assertIsNotEnabled().performClick()
        compose.runOnIdle { assertEquals(1, calls) }
    }

    @Test fun aboutUsesBaseColoursAndDispatchesActionsAndBack() {
        var backs = 0
        var actions = 0
        var primary = Color.Unspecified
        compose.setContent {
            StillswarmTheme(
                darkTheme = false,
                lightColorScheme = StillswarmColors.Light.copy(primary = Color.Magenta),
                content = {
                    AboutScreen(
                        appName = "Sample app",
                        versionLabel = "Version 1",
                        description = "Description",
                        onBack = { backs++ },
                        actions = listOf(AboutAction(
                            title = "Support",
                            onClick = { actions++ },
                        )),
                        footer = { primary = MaterialTheme.colorScheme.primary },
                    )
                },
            )
        }
        compose.onNodeWithText("Support").performScrollTo().performClick()
        compose.onNodeWithContentDescription("Back").performClick()
        compose.runOnIdle {
            assertEquals(1, backs)
            assertEquals(1, actions)
            assertEquals(StillswarmColors.Light.primary, primary)
        }
    }
}
