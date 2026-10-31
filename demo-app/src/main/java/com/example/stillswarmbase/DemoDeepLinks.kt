package com.example.stillswarmbase

import com.stillswarm.base.navigation.DeepLinkOrigin
import com.stillswarm.base.navigation.DeepLinkResult
import com.stillswarm.base.navigation.ValidatingDeepLinkResolver

internal val demoDeepLinkResolver =
    ValidatingDeepLinkResolver(
        origins = setOf(DeepLinkOrigin(
                scheme = "stillswarm-demo",
                host = "app",
            )),
        resolveRoute = { uri ->
            val path = uri.rawPath.orEmpty()
            when {
                uri.rawQuery != null -> DeepLinkResult.Invalid("Query parameters are not supported")
                path == "/gallery" -> DeepLinkResult.Resolved(Gallery)
                path == "/room" -> DeepLinkResult.Resolved(RoomDemo)
                path.startsWith("/entry/") -> {
                    val id = path.removePrefix("/entry/")
                    if (id.matches(Regex("[A-Za-z0-9_-]{1,64}"))) {
                        DeepLinkResult.Resolved(NavigationDetails(entryId = id))
                    } else DeepLinkResult.Invalid("Invalid entry ID")
                }
                else -> DeepLinkResult.Unsupported
            }
        },
    )
