package com.example.stillswarmbase

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.stillswarm.base.storage.PreferenceKey
import com.stillswarm.base.storage.PreferenceStore
import com.stillswarm.base.ui.StillswarmTheme
import com.stillswarm.base.ui.components.InlineButton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.combine

internal data class DemoSettings(val dark: Boolean, val branded: Boolean)

internal class DemoSettingsRepository(private val store: PreferenceStore) {
    private val dark = PreferenceKey.boolean("demo.dark")
    private val branded = PreferenceKey.boolean("demo.branded")

    fun observe() = combine(store.observe(dark), store.observe(branded)) { dark, branded ->
        DemoSettings(dark = dark, branded = branded)
    }

    suspend fun setDark(value: Boolean) = store.set(dark, value)

    suspend fun setBranded(value: Boolean) = store.set(branded, value)
}

internal class DemoSettingsViewModel(private val repository: DemoSettingsRepository) : ViewModel() {
    private val mutableSettings = MutableStateFlow<DemoSettings?>(null)
    val settings = mutableSettings.asStateFlow()
    private val mutableError = MutableStateFlow<String?>(null)
    val error = mutableError.asStateFlow()

    private var observer: Job? = null

    init {
        load()
    }

    fun load() {
        observer?.cancel()
        observer = perform {
            repository.observe().collect {
                mutableSettings.value = it
                mutableError.value = null
            }
        }
    }

    fun setDark(value: Boolean) = perform { repository.setDark(value) }

    fun setBranded(value: Boolean) = perform { repository.setBranded(value) }

    private fun perform(action: suspend () -> Unit): Job =
        viewModelScope.launch {
            try {
                action()
                mutableError.value = null
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                mutableError.value = "Settings could not be saved or loaded. Please try again."
            }
        }
}

@Composable
internal fun PersistentComponentDemo(
    onOpenNavigationDemo: () -> Unit,
    onOpenRoomDemo: () -> Unit,
    onOpenConfigDemo: () -> Unit,
    onOpenTelemetryDemo: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenAbout: () -> Unit,
    model: DemoSettingsViewModel,
) {
    val settings by model.settings.collectAsStateWithLifecycle()
    val error by model.error.collectAsStateWithLifecycle()
    val current = settings
    StillswarmTheme {
        Column {
            error?.let { Text(text = it) }
            if (current == null) {
                Text(text = "Loading settings…")
                if (error != null) InlineButton(
                    text = "Retry",
                    onClick = model::load,
                )
            } else {
                ComponentDemo(
                    initialDark = current.dark,
                    onOpenNavigationDemo = onOpenNavigationDemo,
                    onOpenRoomDemo = onOpenRoomDemo,
                    onOpenConfigDemo = onOpenConfigDemo,
                    onOpenTelemetryDemo = onOpenTelemetryDemo,
                    onOpenSettings = onOpenSettings,
                    onOpenAbout = onOpenAbout,
                    initialBranded = current.branded,
                    onDarkChanged = model::setDark,
                    onBrandedChanged = model::setBranded,
                )
            }
        }
    }
}
