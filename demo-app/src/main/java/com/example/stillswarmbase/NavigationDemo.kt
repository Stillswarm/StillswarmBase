package com.example.stillswarmbase

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.stillswarm.base.navigation.Destination
import com.stillswarm.base.navigation.NavigationCoordinator
import com.stillswarm.base.navigation.Navigator
import com.stillswarm.base.shell.NavigationHandler
import com.stillswarm.base.shell.StillswarmAppShell
import com.stillswarm.base.ui.Dimens
import com.stillswarm.base.ui.StillswarmBaseTheme
import com.stillswarm.base.ui.components.AppTopBar
import com.stillswarm.base.ui.components.InlineButton
import kotlinx.serialization.Serializable
import com.example.stillswarmbase.room.RoomDemoScreen

@Serializable internal data object Gallery : Destination

@Serializable internal data object RoomDemo : Destination

@Serializable internal data class NavigationDetails(val entryId: String) : Destination

internal class NavigationDemoViewModel(private val navigator: Navigator) : ViewModel() {
    fun openEntry() = navigator.navigate(NavigationDetails(entryId = "sample-entry"))

    fun openRoom() = navigator.navigate(RoomDemo)

    fun back() = navigator.back()

    fun replaceEntry() = navigator.replace(NavigationDetails(entryId = "replacement-entry"))

    fun reset() = navigator.resetTo(Gallery)
}

@Composable
internal fun NavigationDemo() {
    // Created outside NavHost: retained by the Activity across configuration changes.
    val coordinator: NavigationCoordinator = viewModel()
    val controller = rememberNavController()
    NavigationHandler(coordinator = coordinator, navController = controller)
    NavHost(
        navController = controller,
        startDestination = Gallery,
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None },
        popExitTransition = { ExitTransition.None },
        builder = {
            composable<Gallery>(
                content = {
                    val model = viewModel(initializer = { NavigationDemoViewModel(coordinator) })
                    PersistentComponentDemo(onOpenNavigationDemo = model::openEntry, onOpenRoomDemo = model::openRoom)
                }
            )
            composable<RoomDemo>(
                content = { RoomDemoScreen(onBack = coordinator::back) },
            )
            composable<NavigationDetails>(
                content = { entry ->
                    val route = entry.toRoute<NavigationDetails>()
                    val model = viewModel(initializer = { NavigationDemoViewModel(coordinator) })
                    StillswarmBaseTheme {
                        StillswarmAppShell(
                            topBar = { AppTopBar(title = "Navigation demo") },
                            content = { padding ->
                                Column(
                                    modifier = Modifier.padding(padding).padding(Dimens.medium),
                                    verticalArrangement = Arrangement.spacedBy(Dimens.medium),
                                    content = {
                                        Text(text = "Entry: ${route.entryId}")
                                        Text(text = "These actions are sent by a ViewModel.")
                                        InlineButton(
                                            text = "Back",
                                            onClick = model::back,
                                        )
                                        InlineButton(
                                            text = "Replace this screen",
                                            onClick = model::replaceEntry,
                                        )
                                        InlineButton(
                                            text = "Reset to gallery",
                                            onClick = model::reset,
                                        )
                                    },
                                )
                            },
                        )
                    }
                }
            )
        },
    )
}
