package com.stillswarm.base.navigation

import org.junit.Assert.*
import org.junit.Test

class DeepLinkResolverTest {
    private data object Target : Destination

    private var routeCalls = 0
    private val resolver =
        ValidatingDeepLinkResolver(
            origins = setOf(DeepLinkOrigin(
                scheme = "https",
                host = "links.example.com",
            )),
            resolveRoute = { uri ->
                routeCalls++
                if (uri.path == "/target") DeepLinkResult.Resolved(Target)
                else DeepLinkResult.Unsupported
            },
        )

    @Test
    fun acceptsExactOriginAndResolvesTypedDestination() {
        assertEquals(
            DeepLinkResult.Resolved(Target),
            resolver.resolve("https://links.example.com/target"),
        )
        assertEquals(
            DeepLinkResult.Resolved(Target),
            resolver.resolve("HTTPS://LINKS.EXAMPLE.COM/target"),
        )
    }

    @Test
    fun unknownOriginsNeverReachAppResolver() {
        listOf(
                "http://links.example.com/target",
                "https://links.example.com.evil.test/target",
                "https://evil.test/target",
                "https://links.example.com:8443/target",
            )
            .forEach { assertEquals(DeepLinkResult.Unsupported, resolver.resolve(it)) }
        assertEquals(0, routeCalls)
    }

    @Test
    fun malformedAndAmbiguousLinksAreRejected() {
        listOf(
                "not a uri",
                "/target",
                "javascript:alert(1)",
                "https://user@links.example.com/target",
                "https://links.example.com/target#fragment",
                "https://links.example.com/%ZZ",
            )
            .forEach { assertTrue(resolver.resolve(it) is DeepLinkResult.Invalid) }
        assertEquals(0, routeCalls)
    }

    @Test
    fun unknownPathsAreUnsupported() {
        assertEquals(
            DeepLinkResult.Unsupported,
            resolver.resolve("https://links.example.com/unknown"),
        )
    }
}
