package com.stillswarm.base.shell.pages

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import com.stillswarm.base.shell.R
import com.stillswarm.base.ui.Dimens

/** Apps supply actions only when available, e.g. support, privacy, licenses, or store listing. */
class AboutAction(
    val title: String,
    val onClick: () -> Unit,
    val summary: String? = null,
    val enabled: Boolean = true,
)

@Composable
fun AboutScreen(
    appName: String,
    versionLabel: String,
    description: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    title: String = stringResource(R.string.stillswarm_about_title),
    backContentDescription: String = stringResource(R.string.stillswarm_navigate_back),
    appIcon: (@Composable () -> Unit)? = null,
    actions: List<AboutAction> = emptyList(),
    footer: (@Composable ColumnScope.() -> Unit)? = null,
) {
    BasePage(
        title = title,
        onBack = onBack,
        modifier = modifier,
        backContentDescription = backContentDescription,
        content = {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = Dimens.xlarge),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(Dimens.small),
                    content = {
                        appIcon?.let { icon -> Box(
                            modifier = Modifier.size(Dimens.fabSize),
                            content = { icon() },
                        ) }
                        Text(
                            text = appName,
                            modifier = Modifier.semantics { heading() },
                            style = MaterialTheme.typography.headlineSmall,
                            textAlign = TextAlign.Center,
                        )
                        Text(
                            text = versionLabel,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )
                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                        )
                    },
                )
            }
            if (actions.isNotEmpty()) item {
                Surface(
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    content = {
                        Column {
                            actions.forEach { action ->
                                SettingsActionRow(
                                    title = action.title,
                                    onClick = action.onClick,
                                    summary = action.summary,
                                    enabled = action.enabled,
                                )
                            }
                        }
                    },
                )
            }
            footer?.let { slot -> item { Column(content = slot) } }
        },
    )
}
