package com.stillswarm.base.navigation

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

/**
 * Own one instance per root navigation host, in the Activity's ViewModelStore. Keeps only the
 * latest undelivered request. Requests are not persisted across process death. Delivery is best
 * effort: cancellation between receipt and execution can lose a request.
 */
class NavigationCoordinator : ViewModel(), Navigator {
    private val channel = Channel<NavigationRequest>(capacity = Channel.CONFLATED)

    val requests: Flow<NavigationRequest> = channel.receiveAsFlow()

    override fun navigate(destination: Destination) = send(NavigationRequest.Navigate(destination))

    override fun back() = send(NavigationRequest.Back)

    override fun replace(destination: Destination) = send(NavigationRequest.Replace(destination))

    override fun resetTo(destination: Destination) = send(NavigationRequest.ResetTo(destination))

    override fun openDeepLink(root: Destination, destination: Destination) =
        send(NavigationRequest.OpenDeepLink(root = root, destination = destination))

    private fun send(request: NavigationRequest) {
        channel.trySend(request)
    }

    override fun onCleared() {
        channel.cancel()
    }
}
