package com.example.stillswarmbase

import android.app.Application
import android.content.Context
import android.os.Bundle
import androidx.test.runner.AndroidJUnitRunner
import com.stillswarm.base.config.RemoteConfigSource

/** UI tests exercise config behavior without depending on the live Firebase template or network. */
class DemoTestRunner : AndroidJUnitRunner() {
    override fun onCreate(arguments: Bundle) {
        if (arguments.getString("liveFirebase") != "true") {
            val excluded = listOfNotNull(
                arguments.getString("notAnnotation")?.takeIf { it.isNotBlank() },
                LiveFirebaseCheck::class.java.name,
            ).joinToString(",")
            arguments.putString("notAnnotation", excluded)
        }
        super.onCreate(arguments)
    }

    override fun newApplication(
        cl: ClassLoader?,
        className: String?,
        context: Context?,
    ): Application = super.newApplication(cl, TestDemoApplication::class.java.name, context)
}

class TestDemoApplication : DemoApplication() {
    override val configNamespace: String = "demo_test"

    override fun createRemoteSource() = RemoteConfigSource {
        mapOf(
            "welcome_text" to "Hello from test config",
            "suggestions_enabled" to "true",
            "daily_limit" to "10",
        )
    }
}
