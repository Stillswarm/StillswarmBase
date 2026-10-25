package com.stillswarm.base.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import com.stillswarm.base.ui.Dimens

class AppBarIcon(
    val contentDescription: String,
    val onClick: () -> Unit,
    val icon: @Composable () -> Unit,
)

class AppMenuItem(val text: String, val onClick: () -> Unit, val enabled: Boolean = true)

/** A single trailing action, either a direct icon or one overflow-menu trigger. */
sealed interface AppTopBarAction {
    class Direct(val action: AppBarIcon) : AppTopBarAction

    class Overflow(
        val contentDescription: String,
        val items: List<AppMenuItem>,
        val icon: @Composable () -> Unit,
    ) : AppTopBarAction
}

class AppTopBarSearch(
    val query: String,
    val onQueryChange: (String) -> Unit,
    val placeholder: String,
)

/** Title only, search only, or title with a second search row. Exactly one trailing slot. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    modifier: Modifier = Modifier,
    title: String? = null,
    search: AppTopBarSearch? = null,
    navigation: AppBarIcon? = null,
    action: AppTopBarAction? = null,
    shape: Shape = androidx.compose.ui.graphics.RectangleShape,
    border: BorderStroke? = null,
) {
    require(title != null || search != null) { "AppTopBar requires a title or search field." }
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surface,
        shape = shape,
        border = border,
        content = {
            Column {
                CenterAlignedTopAppBar(
                    title = {
                        if (title != null)
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        else if (search != null) SearchField(search)
                    },
                    navigationIcon = {
                        if (navigation != null) ActionIcon(navigation)
                        else Spacer(Modifier.size(Dimens.touchTargetSize))
                    },
                    actions = {
                        if (action != null) TopBarAction(action)
                        else Spacer(Modifier.size(Dimens.touchTargetSize))
                    },
                    colors =
                        TopAppBarDefaults.centerAlignedTopAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                )
                if (title != null && search != null) {
                    Box(
                        modifier =
                            Modifier.fillMaxWidth()
                                .padding(
                                    start = Dimens.medium,
                                    end = Dimens.medium,
                                    bottom = Dimens.small,
                                ),
                        content = { SearchField(search) },
                    )
                }
            }
        },
    )
}

@Composable
private fun ActionIcon(action: AppBarIcon) {
    IconButton(
        onClick = action.onClick,
        modifier =
            Modifier.size(Dimens.touchTargetSize).semantics {
                contentDescription = action.contentDescription
            },
        content = {
            Box(
                modifier = Modifier.size(Dimens.xlarge),
                contentAlignment = Alignment.Center,
                content = { action.icon() },
            )
        },
    )
}

@Composable
private fun TopBarAction(action: AppTopBarAction) {
    when (action) {
        is AppTopBarAction.Direct -> ActionIcon(action.action)
        is AppTopBarAction.Overflow -> {
            var expanded by remember { mutableStateOf(false) }
            Box {
                ActionIcon(
                    AppBarIcon(
                        contentDescription = action.contentDescription,
                        onClick = { expanded = true },
                        icon = action.icon,
                    )
                )
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    shape = RoundedCornerShape(Dimens.small),
                    containerColor = MaterialTheme.colorScheme.surface,
                    content = {
                        action.items.forEach { item ->
                            DropdownMenuItem(
                                text = { Text(text = item.text) },
                                enabled = item.enabled,
                                onClick = {
                                    expanded = false
                                    item.onClick()
                                },
                            )
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun SearchField(search: AppTopBarSearch) {
    TextField(
        value = search.query,
        onValueChange = search.onQueryChange,
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        placeholder = { Text(text = search.placeholder) },
        leadingIcon = { Icon(imageVector = Icons.Rounded.Search, contentDescription = null) },
        shape = RoundedCornerShape(Dimens.small),
        textStyle = MaterialTheme.typography.bodyLarge,
        colors =
            TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
            ),
    )
}
