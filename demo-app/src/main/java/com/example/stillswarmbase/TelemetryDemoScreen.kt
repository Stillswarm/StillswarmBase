package com.example.stillswarmbase

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.stillswarm.base.shell.StillswarmAppShell
import com.stillswarm.base.telemetry.*
import com.stillswarm.base.ui.Dimens
import com.stillswarm.base.ui.StillswarmBaseTheme
import com.stillswarm.base.ui.components.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

internal data class TelemetryDemoState(
    val analytics: Boolean,
    val crashes: Boolean,
    val message: String = "",
)

internal class TelemetryDemoViewModel(private val telemetry: Telemetry) : ViewModel() {
    private val mutableState =
        MutableStateFlow(
            TelemetryDemoState(
                analytics = telemetry.analyticsEnabled,
                crashes = telemetry.crashReportingEnabled,
            )
        )
    val state = mutableState.asStateFlow()

    fun entered() {
        telemetry.screen("telemetry_demo")
        telemetry.track(
            TelemetryEvent(
                name = "component_impression",
                parameters = mapOf("component" to TelemetryValue.Text("telemetry_demo")),
            )
        )
    }

    fun click() {
        telemetry.track(
            TelemetryEvent(
                name = "button_click",
                parameters =
                    mapOf(
                        "screen" to TelemetryValue.Text("telemetry_demo"),
                        "button" to TelemetryValue.Text("sample_event"),
                        "sample" to TelemetryValue.Flag(true),
                    ),
            )
        )
        refresh("Sample event submitted; server delivery is asynchronous.")
    }

    fun report() {
        telemetry.breadcrumb("Demo non-fatal button tapped")
        telemetry.recordException(IllegalStateException("Stillswarm demo non-fatal report"))
        refresh("Sample non-fatal report submitted.")
    }

    fun toggleAnalytics() {
        telemetry.setAnalyticsEnabled(!telemetry.analyticsEnabled)
        refresh()
    }

    fun toggleCrashes() {
        telemetry.setCrashReportingEnabled(!telemetry.crashReportingEnabled)
        refresh()
    }

    private fun refresh(message: String = "") {
        mutableState.value =
            TelemetryDemoState(
                analytics = telemetry.analyticsEnabled,
                crashes = telemetry.crashReportingEnabled,
                message = message,
            )
    }
}

@Composable
internal fun TelemetryDemoScreen(onBack: () -> Unit) {
    val telemetry = (LocalContext.current.applicationContext as DemoApplication).telemetry
    val model = viewModel(initializer = { TelemetryDemoViewModel(telemetry) })
    val state by model.state.collectAsStateWithLifecycle()
    // One impression per composition entry; changing controls does not emit it again.
    LaunchedEffect(key1 = model, block = { model.entered() })
    StillswarmBaseTheme {
        StillswarmAppShell(
            topBar = {
                AppTopBar(
                    title = "Telemetry demo",
                    navigation =
                        AppBarIcon(
                            contentDescription = "Back",
                            onClick = onBack,
                            icon = {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                    contentDescription = null,
                                )
                            },
                        ),
                )
            },
            content = { padding ->
                Column(
                    modifier =
                        Modifier.padding(padding)
                            .verticalScroll(rememberScrollState())
                            .padding(Dimens.medium),
                    verticalArrangement = Arrangement.spacedBy(Dimens.medium),
                    content = {
                        Text(text = "Analytics: ${if (state.analytics) "enabled" else "disabled"}")
                        InlineButton(
                            text = "Toggle analytics",
                            onClick = model::toggleAnalytics,
                        )
                        SuperButton(
                            text = "Send sample event",
                            onClick = model::click,
                            state =
                                if (state.analytics) ButtonState.Enabled else ButtonState.Disabled,
                        )
                        Text(
                            text =
                                "Crash reporting: ${if (state.crashes) "enabled" else "disabled"}"
                        )
                        InlineButton(
                            text = "Toggle crash reporting",
                            onClick = model::toggleCrashes,
                        )
                        SuperButton(
                            text = "Report sample non-fatal",
                            onClick = model::report,
                            state = if (state.crashes) ButtonState.Enabled else ButtonState.Disabled,
                        )
                        if (state.message.isNotEmpty()) Text(text = state.message)
                        Text(
                            text =
                                "Firebase handles buffering and uploads. These switches last for this demo process; a new process enables collection again."
                        )
                    },
                )
            },
        )
    }
}
