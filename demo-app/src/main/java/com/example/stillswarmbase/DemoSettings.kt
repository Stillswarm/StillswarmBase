package com.example.stillswarmbase

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.stillswarm.base.storage.PreferenceKey
import com.stillswarm.base.storage.PreferenceStore
import com.stillswarm.base.storage.defaultPreferenceStore
import com.stillswarm.base.ui.StillswarmTheme
import com.stillswarm.base.ui.components.InlineButton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal data class DemoSettings(val dark: Boolean, val branded: Boolean)

internal class DemoSettingsRepository(private val store: PreferenceStore) {
    private val dark = PreferenceKey.boolean("demo.dark")
    private val branded = PreferenceKey.boolean("demo.branded")

    suspend fun load() = DemoSettings(dark = store.get(dark), branded = store.get(branded))

    suspend fun setDark(value: Boolean) = store.set(dark, value)

    suspend fun setBranded(value: Boolean) = store.set(branded, value)
}

internal class DemoSettingsViewModel(private val repository: DemoSettingsRepository) : ViewModel() {
    private val mutableSettings = MutableStateFlow<DemoSettings?>(null)
    val settings = mutableSettings.asStateFlow()
    private val mutableError = MutableStateFlow<String?>(null)
    val error = mutableError.asStateFlow()

    init {
        load()
    }

    fun load() = perform { mutableSettings.value = repository.load() }

    fun setDark(value: Boolean) = perform { repository.setDark(value) }

    fun setBranded(value: Boolean) = perform { repository.setBranded(value) }

    private fun perform(action: suspend () -> Unit) {
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
}

@Composable
internal fun PersistentComponentDemo(onOpenNavigationDemo: () -> Unit, onOpenRoomDemo: () -> Unit) {
    val context = LocalContext.current.applicationContext
    val model =
        viewModel(
            initializer = {
                DemoSettingsViewModel(DemoSettingsRepository(defaultPreferenceStore(context)))
            }
        )
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
                    initialBranded = current.branded,
                    onDarkChanged = model::setDark,
                    onBrandedChanged = model::setBranded,
                )
            }
        }
    }
}
