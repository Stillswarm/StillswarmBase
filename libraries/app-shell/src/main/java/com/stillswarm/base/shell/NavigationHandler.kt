package com.stillswarm.base.shell

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavHostController
import com.stillswarm.base.navigation.NavigationCoordinator
import com.stillswarm.base.navigation.NavigationRequest

/**
 * Install once beside the app's NavHost, outside destination content. The NavHost owns restoration
 * and system Back. This handler executes feature requests.
 */
@Composable
fun NavigationHandler(coordinator: NavigationCoordinator, navController: NavHostController) {
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(
        key1 = coordinator,
        key2 = navController,
        key3 = lifecycleOwner,
        block = {
            lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                coordinator.requests.collect { request ->
                    when (request) {
                        is NavigationRequest.OpenDeepLink -> {
                            navController.navigate(request.root) {
                                popUpTo(navController.graph.id) { inclusive = false }
                                launchSingleTop = true
                            }
                            if (request.destination != request.root) {
                                navController.navigate(request.destination) { launchSingleTop = true }
                            }
                        }

                        is NavigationRequest.Navigate ->
                            navController.navigate(request.destination) { launchSingleTop = true }

                        NavigationRequest.Back -> {
                            if (navController.previousBackStackEntry != null) {
                                navController.popBackStack()
                            }
                        }

                        is NavigationRequest.Replace ->
                            navController.navigate(request.destination) {
                                navController.currentDestination?.let { current ->
                                    popUpTo(current.id) { inclusive = true }
                                }
                                launchSingleTop = true
                            }

                        is NavigationRequest.ResetTo ->
                            navController.navigate(request.destination) {
                                popUpTo(navController.graph.id) { inclusive = false }
                                launchSingleTop = true
                            }
                    }
                }
            }
        },
    )
}
