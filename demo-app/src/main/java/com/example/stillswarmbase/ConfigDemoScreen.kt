package com.example.stillswarmbase

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.stillswarm.base.config.ConfigSession
import com.stillswarm.base.shell.StillswarmAppShell
import com.stillswarm.base.ui.Dimens
import com.stillswarm.base.ui.StillswarmBaseTheme
import com.stillswarm.base.ui.components.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal class ConfigDemoViewModel(private val config: ConfigSession) : ViewModel() {
    val state = config.state
    private val mutableError = MutableStateFlow<String?>(null)
    val error = mutableError.asStateFlow()
    private val mutableBusy = MutableStateFlow(false)
    val busy = mutableBusy.asStateFlow()

    fun toggleSuggestions() = write {
        config.setOverride(DemoConfigKeys.suggestions, !config[DemoConfigKeys.suggestions])
    }

    fun saveLimit(text: String) = write {
        val value = text.toIntOrNull()
        require(value != null && value in 1..100) { "Enter a limit from 1 to 100." }
        config.setOverride(DemoConfigKeys.limit, value)
    }

    fun clearOverrides() = write { config.clearOverrides() }

    private fun write(block: suspend () -> Unit) {
        if (mutableBusy.value) return
        mutableBusy.value = true
        viewModelScope.launch {
            try {
                block()
                mutableError.value = null
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: IllegalArgumentException) {
                mutableError.value = "Enter a limit from 1 to 100."
            } catch (_: Exception) {
                mutableError.value = "Could not save the override. Please try again."
            } finally {
                mutableBusy.value = false
            }
        }
    }
}

@Composable
internal fun ConfigDemoScreen(onBack: () -> Unit) {
    val config = (LocalContext.current.applicationContext as DemoApplication).config
    val model = viewModel(initializer = { ConfigDemoViewModel(config) })
    val state by model.state.collectAsStateWithLifecycle()
    val error by model.error.collectAsStateWithLifecycle()
    val busy by model.busy.collectAsStateWithLifecycle()
    var limit by rememberSaveable { mutableStateOf("") }
    val buttonState = if (busy) ButtonState.Loading else ButtonState.Enabled
    StillswarmBaseTheme {
        StillswarmAppShell(
            topBar = {
                AppTopBar(
                    title = "Config demo",
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
                        Text(text = "Fetch: ${state.fetchStatus}")
                        Text(text = "Local settings loaded: ${state.localLoaded}")
                        Text(
                            text =
                                "Welcome: ${state.snapshot[DemoConfigKeys.welcome]} (${state.snapshot.source(DemoConfigKeys.welcome)})"
                        )
                        Text(
                            text =
                                "Suggestions: ${state.snapshot[DemoConfigKeys.suggestions]} (${state.snapshot.source(DemoConfigKeys.suggestions)})"
                        )
                        Text(
                            text =
                                "Daily limit: ${state.snapshot[DemoConfigKeys.limit]} (${state.snapshot.source(DemoConfigKeys.limit)})"
                        )
                        SuperButton(
                            text = "Toggle suggestions override",
                            onClick = model::toggleSuggestions,
                            state = buttonState,
                        )
                        OutlinedTextField(
                            value = limit,
                            onValueChange = { limit = it },
                            label = { Text(text = "Daily limit (1–100)") },
                            singleLine = true,
                        )
                        SuperButton(
                            text = "Save limit override",
                            onClick = { model.saveLimit(limit) },
                            state = buttonState,
                        )
                        InlineButton(
                            text = "Clear local overrides",
                            onClick = model::clearOverrides,
                            state = buttonState,
                        )
                        error?.let { Text(text = it) }
                        if (state.issues.isNotEmpty())
                            Text(text = "Storage issues: ${state.issues}")
                        Text(
                            text =
                                "Firebase is checked once per process. Overrides apply immediately and survive relaunch; clear them to use remote values again."
                        )
                    },
                )
            },
        )
    }
}
