package com.stillswarm.base.navigation

import android.content.Intent

/**
 * Dispatch once on a fresh Activity launch, and once per onNewIntent delivery. Do not dispatch the
 * original launch intent again when restoring saved Activity state. Non-VIEW intents are ignored.
 * No arbitrary extras are interpreted as destinations.
 */
class DeepLinkHandler(
    private val resolver: DeepLinkResolver,
    private val navigator: Navigator,
    private val root: Destination,
) {
    /** Null means this is not a link intent. Invalid/unsupported links never change navigation. */
    fun handle(intent: Intent): DeepLinkResult? {
        if (intent.action != Intent.ACTION_VIEW) return null
        val result =
            intent.dataString?.let(resolver::resolve) ?: DeepLinkResult.Invalid("Missing link URI")
        if (result is DeepLinkResult.Resolved) {
            navigator.openDeepLink(root = root, destination = result.destination)
        }
        return result
    }
}
