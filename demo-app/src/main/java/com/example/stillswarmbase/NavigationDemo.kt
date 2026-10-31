package com.example.stillswarmbase

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.stillswarm.base.storage.defaultPreferenceStore
import com.stillswarm.base.ui.StillswarmTheme
import com.stillswarm.base.shell.pages.*
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

@Serializable internal data object SettingsPage : Destination

@Serializable internal data object AboutPage : Destination

@Serializable internal data object Gallery : Destination

@Serializable internal data object RoomDemo : Destination

@Serializable internal data object ConfigDemo : Destination

@Serializable internal data object TelemetryDemo : Destination

@Serializable internal data class NavigationDetails(val entryId: String) : Destination

internal class NavigationDemoViewModel(private val navigator: Navigator) : ViewModel() {
    fun openEntry() = navigator.navigate(NavigationDetails(entryId = "sample-entry"))

    fun openRoom() = navigator.navigate(RoomDemo)

    fun openConfig() = navigator.navigate(ConfigDemo)

    fun openTelemetry() = navigator.navigate(TelemetryDemo)

    fun back() = navigator.back()

    fun replaceEntry() = navigator.replace(NavigationDetails(entryId = "replacement-entry"))

    fun reset() = navigator.resetTo(Gallery)
}

@Composable
internal fun NavigationDemo(coordinator: NavigationCoordinator) {
    val controller = rememberNavController()
    NavigationHandler(coordinator = coordinator, navController = controller)
    val context = LocalContext.current.applicationContext
    val settingsModel = viewModel(initializer = {
        DemoSettingsViewModel(DemoSettingsRepository(defaultPreferenceStore(context)))
    })
    val settings by settingsModel.settings.collectAsStateWithLifecycle()
    StillswarmTheme(darkTheme = settings?.dark ?: androidx.compose.foundation.isSystemInDarkTheme()) {
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
                        PersistentComponentDemo(
                            onOpenNavigationDemo = model::openEntry,
                            onOpenRoomDemo = model::openRoom,
                            onOpenConfigDemo = model::openConfig,
                            onOpenTelemetryDemo = model::openTelemetry,
                            onOpenSettings = { coordinator.navigate(SettingsPage) },
                            onOpenAbout = { coordinator.navigate(AboutPage) },
                            model = settingsModel,
                        )
                    }
                )
                composable<SettingsPage>(
                    content = {
                        DemoSettingsPage(
                            model = settingsModel,
                            onBack = coordinator::back,
                            onAbout = { coordinator.navigate(AboutPage) },
                        )
                    },
                )
                composable<AboutPage>(
                    content = {
                        AboutScreen(
                            appName = "Stillswarm Base",
                            versionLabel = "Version ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
                            description = "Reusable Android foundations. Manrope headings, Inter body text, and simple components with dynamic colours off.",
                            onBack = coordinator::back,
                        )
                    },
                )
                composable<TelemetryDemo>(
                    content = { TelemetryDemoScreen(onBack = coordinator::back) },
                )
                composable<ConfigDemo>(
                    content = { ConfigDemoScreen(onBack = coordinator::back) },
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
}
