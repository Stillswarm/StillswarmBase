package com.stillswarm.base.testing

import com.stillswarm.base.config.RemoteConfigSource
import kotlinx.coroutines.delay

/** Configure before calling start; no Firebase or network needed. */
class FakeRemoteConfigSource(
    var values: Map<String, String> = emptyMap(),
    var delayMillis: Long = 0,
    var failure: Exception? = null,
) : RemoteConfigSource {
    var fetchCount: Int = 0
        private set

    override suspend fun fetch(): Map<String, String> {
        fetchCount++
        delay(delayMillis)
        failure?.let { throw it }
        return values.toMap()
    }
}
