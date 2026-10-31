package com.example.stillswarmbase

import com.google.firebase.FirebaseApp
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.stillswarm.base.firebase.config.FirebaseRemoteConfigSource
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assume.assumeTrue
import org.junit.Test

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class LiveFirebaseCheck

/** Optional backend smoke check; excluded from ordinary offline UI test runs. */
@LiveFirebaseCheck
class FirebaseConnectionTest {
    @Test
    fun fetchesFromConfiguredProject() = runBlocking {
        assumeTrue(InstrumentationRegistry.getArguments().getString("liveFirebase") == "true")
        assertEquals("stillswarm-demo", FirebaseApp.getInstance().options.projectId)
        val source = FirebaseRemoteConfigSource(
            remoteConfig = FirebaseRemoteConfig.getInstance(),
            minimumFetchIntervalSeconds = 0,
        )
        // This diagnostic timeout does not change the app's 5-second cold-start budget.
        val started = android.os.SystemClock.elapsedRealtime()
        val values = withTimeout(20_000) { source.fetch() }
        val elapsed = android.os.SystemClock.elapsedRealtime() - started
        android.util.Log.i("FirebaseSmokeTest", "Backend fetch succeeded in ${elapsed}ms: ${values.size} remote parameters; welcome_text present=${"welcome_text" in values}")
        Unit
    }
}
