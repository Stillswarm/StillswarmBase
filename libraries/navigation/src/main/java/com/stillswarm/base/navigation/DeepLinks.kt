package com.stillswarm.base.navigation

import java.net.URI
import java.net.URISyntaxException

sealed interface DeepLinkResult {
    data class Resolved(val destination: Destination) : DeepLinkResult

    data object Unsupported : DeepLinkResult

    data class Invalid(val reason: String) : DeepLinkResult
}

fun interface DeepLinkResolver {
    fun resolve(link: String): DeepLinkResult
}

/** Exact origin allowlist, without wildcard or suffix matching. Port -1 means no explicit port. */
data class DeepLinkOrigin(val scheme: String, val host: String, val port: Int = -1) {
    init {
        require(scheme.isNotBlank() && host.isNotBlank())
        require(port == -1 || port in 1..65535)
    }
}

/** Validates the envelope; app code validates paths, query parameters, IDs, and access rules. */
class ValidatingDeepLinkResolver(
    origins: Set<DeepLinkOrigin>,
    private val resolveRoute: (URI) -> DeepLinkResult,
) : DeepLinkResolver {
    private val origins = origins.toSet()

    init {
        require(origins.isNotEmpty())
    }

    override fun resolve(link: String): DeepLinkResult {
        val uri =
            try {
                URI(link)
            } catch (_: URISyntaxException) {
                return DeepLinkResult.Invalid("Malformed URI")
            }
        if (!uri.isAbsolute || uri.isOpaque || uri.host == null) {
            return DeepLinkResult.Invalid("Expected an absolute hierarchical URI with a host")
        }
        if (uri.rawUserInfo != null || uri.rawFragment != null) {
            return DeepLinkResult.Invalid("User info and fragments are not supported")
        }
        if (
            origins.none {
                it.scheme.equals(uri.scheme, ignoreCase = true) &&
                    it.host.equals(uri.host, ignoreCase = true) &&
                    it.port == uri.port
            }
        )
            return DeepLinkResult.Unsupported
        return resolveRoute(uri)
    }
}
