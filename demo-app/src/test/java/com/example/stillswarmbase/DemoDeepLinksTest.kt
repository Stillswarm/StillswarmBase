package com.example.stillswarmbase

import com.stillswarm.base.navigation.DeepLinkResult
import org.junit.Assert.*
import org.junit.Test

class DemoDeepLinksTest {
    @Test
    fun resolvesEachDemoRoute() {
        assertEquals(
            DeepLinkResult.Resolved(Gallery),
            demoDeepLinkResolver.resolve("stillswarm-demo://app/gallery"),
        )
        assertEquals(
            DeepLinkResult.Resolved(RoomDemo),
            demoDeepLinkResolver.resolve("stillswarm-demo://app/room"),
        )
        assertEquals(
            DeepLinkResult.Resolved(NavigationDetails("sample-entry")),
            demoDeepLinkResolver.resolve("stillswarm-demo://app/entry/sample-entry"),
        )
    }

    @Test
    fun rejectsInvalidIdsAndUnexpectedQueryParameters() {
        listOf("", "..", "a/b", "%2F", "%252F", "a?admin=true", "a".repeat(65)).forEach {
            assertTrue(
                demoDeepLinkResolver.resolve("stillswarm-demo://app/entry/$it")
                    is DeepLinkResult.Invalid
            )
        }
        assertEquals(
            DeepLinkResult.Unsupported,
            demoDeepLinkResolver.resolve("stillswarm-demo://app/unknown"),
        )
    }
}
