package com.example.stillswarmbase

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import com.stillswarm.base.shell.StillswarmAppShell
import com.stillswarm.base.ui.*
import com.stillswarm.base.ui.components.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { NavigationDemo() }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ComponentDemo(
    initialDark: Boolean = isSystemInDarkTheme(),
    onOpenNavigationDemo: () -> Unit = {},
    onOpenRoomDemo: () -> Unit = {},
    initialBranded: Boolean = false,
    onDarkChanged: (Boolean) -> Unit = {},
    onBrandedChanged: (Boolean) -> Unit = {},
) {
    var dark by rememberSaveable { mutableStateOf(initialDark) }
    var branded by rememberSaveable { mutableStateOf(initialBranded) }
    var loading by remember { mutableStateOf(false) }
    var showSheet by rememberSaveable { mutableStateOf(false) }
    var showDelete by rememberSaveable { mutableStateOf(false) }
    var showAbout by rememberSaveable { mutableStateOf(false) }
    var entryExists by rememberSaveable { mutableStateOf(true) }
    var name by rememberSaveable { mutableStateOf("") }
    var reminders by rememberSaveable { mutableStateOf(false) }
    var searchMode by rememberSaveable { mutableStateOf(0) }
    var query by rememberSaveable { mutableStateOf("") }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    fun notify(message: String) {
        scope.launch { snackbar.showAppSnackbar(message) }
    }
    val save: () -> Unit = {
        scope.launch {
            loading = true
            try {
                delay(1000)
                notify("Changes saved")
            } finally {
                loading = false
            }
        }
    }
    StillswarmTheme(
        darkTheme = dark,
        lightColorScheme =
            if (branded)
                StillswarmColors.Light.copy(primary = Color(0xFF245A45), onPrimary = Color.White)
            else StillswarmColors.Light,
        darkColorScheme =
            if (branded)
                StillswarmColors.Dark.copy(
                    primary = Color(0xFFA3D3B8),
                    onPrimary = Color(0xFF103828),
                )
            else StillswarmColors.Dark,
        content = {
            StillswarmAppShell(
                snackbarHostState = snackbar,
                topBar = {
                    AppTopBar(
                        title = if (searchMode == 1) null else "Component gallery",
                        search =
                            if (searchMode == 0) null
                            else
                                AppTopBarSearch(
                                    query = query,
                                    onQueryChange = { query = it },
                                    placeholder = "Search cards",
                                ),
                        action =
                            AppTopBarAction.Overflow(
                                contentDescription = "More options",
                                items =
                                    listOf(
                                        AppMenuItem(
                                            text = "Title only",
                                            onClick = { searchMode = 0 },
                                        ),
                                        AppMenuItem(
                                            text = "Search only",
                                            onClick = { searchMode = 1 },
                                        ),
                                        AppMenuItem(
                                            text = "Title and search",
                                            onClick = { searchMode = 2 },
                                        ),
                                        AppMenuItem(text = "About", onClick = { showAbout = true }),
                                    ),
                                icon = { DemoIcon(Icons.Rounded.MoreVert) },
                            ),
                    )
                },
                floatingActionButton = {
                    AppFab(
                        onClick = { showSheet = true },
                        contentDescription = "Add entry",
                        label = "Add entry",
                        icon = { DemoIcon(Icons.Rounded.Add) },
                    )
                },
                content = { padding ->
                    Column(
                        modifier =
                            Modifier.padding(padding)
                                .verticalScroll(rememberScrollState())
                                .padding(Dimens.medium),
                        verticalArrangement = Arrangement.spacedBy(Dimens.medium),
                        content = {
                            Toggle(
                                label = "Dark appearance",
                                checked = dark,
                                onCheckedChange = {
                                    dark = it
                                    onDarkChanged(it)
                                },
                            )
                            Toggle(
                                label = "App colour override",
                                checked = branded,
                                onCheckedChange = {
                                    branded = it
                                    onBrandedChanged(it)
                                },
                            )
                            InlineButton(text = "Try navigation", onClick = onOpenNavigationDemo)
                            InlineButton(
                                text = "Try Room storage",
                                onClick = onOpenRoomDemo,
                            )
                            Text(text = "Buttons", style = MaterialTheme.typography.titleMedium)
                            SuperButton(
                                text = "Save changes",
                                onClick = save,
                                modifier = Modifier.fillMaxWidth(),
                                state = if (loading) ButtonState.Loading else ButtonState.Enabled,
                                loadingText = "Saving…",
                            )
                            SuperButton(
                                text = "Disabled",
                                onClick = {},
                                modifier = Modifier.fillMaxWidth(),
                                state = ButtonState.Disabled,
                            )
                            SuperButton(
                                text = "Loading state",
                                onClick = {},
                                modifier = Modifier.fillMaxWidth(),
                                state = ButtonState.Loading,
                                loadingText = "Saving…",
                            )
                            InlineButton(
                                text = "Save changes",
                                onClick = save,
                                state = if (loading) ButtonState.Loading else ButtonState.Enabled,
                                loadingText = "Saving…",
                            )
                            InlineButton(
                                text = "Disabled",
                                onClick = {},
                                state = ButtonState.Disabled,
                            )
                            InlineButton(
                                text = "Loading state",
                                onClick = {},
                                state = ButtonState.Loading,
                                loadingText = "Saving…",
                            )
                            Text(
                                text = "Listing cards",
                                style = MaterialTheme.typography.titleMedium,
                            )
                            if (
                                entryExists &&
                                    (searchMode == 0 ||
                                        "Morning routine".contains(query, ignoreCase = true))
                            ) {
                                VLCard(
                                    modifier = Modifier.fillMaxWidth(),
                                    onClick = { showSheet = true },
                                    content = {
                                        Text(
                                            text = "Morning routine",
                                            style = MaterialTheme.typography.titleMedium,
                                        )
                                        Text(
                                            text = "A little space to plan your day.",
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    },
                                )
                                HLCard(
                                    modifier = Modifier.fillMaxWidth(),
                                    onClick = { showSheet = true },
                                    leadingContent = { DemoIcon(Icons.Rounded.Description) },
                                    content = {
                                        Text(
                                            text = "Morning routine",
                                            style = MaterialTheme.typography.titleMedium,
                                        )
                                        Text(
                                            text = "Updated today",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    },
                                )
                                InlineButton(
                                    text = "Delete sample entry",
                                    onClick = { showDelete = true },
                                )
                            } else {
                                Text(
                                    text =
                                        if (entryExists) "No matching cards"
                                        else "Sample entry deleted"
                                )
                                if (!entryExists)
                                    InlineButton(
                                        text = "Restore sample",
                                        onClick = { entryExists = true },
                                    )
                            }
                            Text(text = "Overlays", style = MaterialTheme.typography.titleMedium)
                            SuperButton(
                                text = "Open bottom sheet",
                                onClick = { showSheet = true },
                                modifier = Modifier.fillMaxWidth(),
                            )
                            InlineButton(
                                text = "Show snackbar",
                                onClick = { notify("Changes saved") },
                            )
                            StillswarmBaseTheme {
                                Surface(
                                    shape = MaterialTheme.shapes.medium,
                                    content = {
                                        Column(
                                            modifier = Modifier.padding(Dimens.medium),
                                            verticalArrangement =
                                                Arrangement.spacedBy(Dimens.small),
                                            content = {
                                                Text(
                                                    text = "Shared screen appearance",
                                                    style = MaterialTheme.typography.titleMedium,
                                                )
                                                Text(
                                                    text =
                                                        "Base screens retain neutral colours and fonts.",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                )
                                                InlineButton(
                                                    text = "Reset app colours",
                                                    onClick = {
                                                        branded = false
                                                        onBrandedChanged(false)
                                                    },
                                                )
                                            },
                                        )
                                    },
                                )
                            }
                            Spacer(Modifier.height(Dimens.fabContentClearance))
                        },
                    )
                },
            )
            if (showSheet)
                AppBottomSheet(
                    onDismissRequest = { showSheet = false },
                    title = "New entry",
                    content = {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text(text = "Entry name") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                        )
                        Toggle(
                            label = "Enable reminders",
                            checked = reminders,
                            onCheckedChange = { reminders = it },
                        )
                        SuperButton(
                            text = "Save entry",
                            onClick = {
                                showSheet = false
                                notify("Entry saved")
                            },
                            modifier = Modifier.fillMaxWidth(),
                            state =
                                if (name.isBlank()) ButtonState.Disabled else ButtonState.Enabled,
                        )
                    },
                )
            if (showDelete)
                AppDialog(
                    title = "Delete entry?",
                    confirmText = "Delete entry",
                    destructive = true,
                    icon = {
                        Icon(
                            imageVector = Icons.Rounded.Warning,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            tint = MaterialTheme.colorScheme.error,
                        )
                    },
                    onConfirm = {
                        entryExists = false
                        showDelete = false
                        notify("Entry deleted")
                    },
                    onDismissRequest = { showDelete = false },
                    content = {
                        Text(
                            text =
                                "“Morning routine” will be permanently deleted. This cannot be undone."
                        )
                    },
                )
            if (showAbout)
                StillswarmBaseTheme {
                    AppDialog(
                        title = "Stillswarm Base",
                        confirmText = "Done",
                        dismissText = "Close",
                        onConfirm = { showAbout = false },
                        onDismissRequest = { showAbout = false },
                        content = {
                            Text(
                                text =
                                    "Reusable Android components. Manrope headings and Inter body text, with dynamic colours off."
                            )
                        },
                    )
                }
        },
    )
}

@Composable
private fun DemoIcon(imageVector: ImageVector) =
    Icon(imageVector = imageVector, contentDescription = null)

@Composable
private fun Toggle(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        content = {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f),
            )
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                modifier = Modifier.semantics { contentDescription = label },
            )
        },
    )
}

@Preview(name = "Components light", showBackground = true)
@Composable
private fun LightPreview() = ComponentDemo(false)

@Preview(name = "Components dark", showBackground = true)
@Composable
private fun DarkPreview() = ComponentDemo(true)
