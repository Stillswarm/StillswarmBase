package com.stillswarm.base.navigation

/** App-owned routes implement this marker and are annotated with @Serializable in the app. */
interface Destination

/** Inject into feature ViewModels; no NavController or Activity reference is needed. */
interface Navigator {
    fun navigate(destination: Destination)

    fun back()

    fun replace(destination: Destination)

    fun resetTo(destination: Destination)

    /** Rebuilds root + target as one request, so latest-wins cannot drop half the operation. */
    fun openDeepLink(root: Destination, destination: Destination)
}

sealed interface NavigationRequest {
    data class OpenDeepLink(val root: Destination, val destination: Destination) : NavigationRequest

    data class Navigate(val destination: Destination) : NavigationRequest

    data object Back : NavigationRequest

    data class Replace(val destination: Destination) : NavigationRequest

    data class ResetTo(val destination: Destination) : NavigationRequest
}
