package com.stillswarm.base.shell.pages

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import com.stillswarm.base.shell.R
import com.stillswarm.base.shell.StillswarmAppShell
import com.stillswarm.base.ui.Dimens
import com.stillswarm.base.ui.StillswarmBaseTheme
import com.stillswarm.base.ui.components.AppTopBar
import com.stillswarm.base.ui.components.AppBarIcon

/** Layout only: app ViewModels own settings state, persistence, and actions. */
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    title: String = stringResource(R.string.stillswarm_settings_title),
    backContentDescription: String = stringResource(R.string.stillswarm_navigate_back),
    content: LazyListScope.() -> Unit,
) {
    BasePage(
        title = title,
        onBack = onBack,
        modifier = modifier,
        backContentDescription = backContentDescription,
        content = content,
    )
}

@Composable
internal fun BasePage(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier,
    backContentDescription: String,
    content: LazyListScope.() -> Unit,
) {
    StillswarmBaseTheme {
        StillswarmAppShell(
            topBar = {
                AppTopBar(
                    title = title,
                    navigation = AppBarIcon(
                        contentDescription = backContentDescription,
                        onClick = onBack,
                        icon = {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = null,
                            )
                        },
                    ),
                )
            },
            content = { insets ->
                LazyColumn(
                    modifier = modifier.fillMaxSize().padding(insets),
                    contentPadding = PaddingValues(Dimens.medium),
                    verticalArrangement = Arrangement.spacedBy(Dimens.xlarge),
                    content = content,
                )
            },
        )
    }
}

@Composable
fun SettingsSection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Dimens.xsmall),
        content = {
            Text(
                text = title,
                modifier = Modifier.padding(horizontal = Dimens.medium).semantics { heading() },
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Surface(
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                content = { Column(content = content) },
            )
        },
    )
}

@Composable
fun SettingsActionRow(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    summary: String? = null,
    enabled: Boolean = true,
    leadingIcon: (@Composable () -> Unit)? = null,
) {
    SettingsRowContent(
        title = title,
        summary = summary,
        enabled = enabled,
        modifier = modifier.clickable(
            enabled = enabled,
            role = Role.Button,
            onClick = onClick,
        ),
        leadingIcon = leadingIcon,
        trailing = {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = null,
            )
        },
    )
}

/** One accessible switch target: the whole row toggles once; the inner switch is display-only. */
@Composable
fun SettingsSwitchRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    summary: String? = null,
    enabled: Boolean = true,
    leadingIcon: (@Composable () -> Unit)? = null,
) {
    SettingsRowContent(
        title = title,
        summary = summary,
        enabled = enabled,
        modifier = modifier.toggleable(
            value = checked,
            enabled = enabled,
            role = Role.Switch,
            onValueChange = onCheckedChange,
        ),
        leadingIcon = leadingIcon,
        trailing = {
            Switch(
                checked = checked,
                onCheckedChange = null,
                enabled = enabled,
            )
        },
    )
}

@Composable
private fun SettingsRowContent(
    title: String,
    summary: String?,
    enabled: Boolean,
    modifier: Modifier,
    leadingIcon: (@Composable () -> Unit)?,
    trailing: @Composable () -> Unit,
) {
    val contentColor = if (enabled) MaterialTheme.colorScheme.onSurface
        else MaterialTheme.colorScheme.onSurface.copy(alpha = .38f)
    CompositionLocalProvider(LocalContentColor provides contentColor) {
        Row(
            modifier = modifier.fillMaxWidth().heightIn(min = Dimens.touchTargetSize)
                .padding(horizontal = Dimens.medium, vertical = Dimens.small),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimens.medium),
            content = {
                leadingIcon?.let { icon -> Box(
                    modifier = Modifier.size(Dimens.xlarge),
                    content = { icon() },
                ) }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(Dimens.xxsmall),
                    content = {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.bodyLarge,
                        )
                        summary?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (enabled) MaterialTheme.colorScheme.onSurfaceVariant else contentColor,
                            )
                        }
                    },
                )
                trailing()
            },
        )
    }
}
