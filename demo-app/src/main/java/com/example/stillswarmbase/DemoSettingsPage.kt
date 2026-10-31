package com.example.stillswarmbase

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.material3.Text
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.stillswarm.base.shell.pages.*

@Composable
internal fun DemoSettingsPage(
    model: DemoSettingsViewModel,
    onBack: () -> Unit,
    onAbout: () -> Unit,
) {
    val settings by model.settings.collectAsStateWithLifecycle()
    val error by model.error.collectAsStateWithLifecycle()
    SettingsScreen(
        onBack = onBack,
        content = {
            error?.let { message ->
                item {
                    Text(text = message)
                    SettingsActionRow(
                        title = "Retry",
                        onClick = model::load,
                    )
                }
            }
            val current = settings
            if (current == null) item { Text(text = "Loading settings…") }
            else item {
                SettingsSection(
                    title = "Appearance",
                    content = {
                        SettingsSwitchRow(
                            title = "Dark theme",
                            checked = current.dark,
                            onCheckedChange = model::setDark,
                        )
                        SettingsSwitchRow(
                            title = "App colours",
                            checked = current.branded,
                            onCheckedChange = model::setBranded,
                            summary = "Applies to the component gallery. Shared pages keep the base colours.",
                        )
                    },
                )
            }
            item {
                SettingsSection(
                    title = "Information",
                    content = {
                        SettingsActionRow(
                            title = "About",
                            onClick = onAbout,
                        )
                    },
                )
            }
        },
    )
}
