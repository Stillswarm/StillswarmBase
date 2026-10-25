package com.example.stillswarmbase.room

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.stillswarm.base.shell.StillswarmAppShell
import com.stillswarm.base.ui.Dimens
import com.stillswarm.base.ui.StillswarmBaseTheme
import com.stillswarm.base.ui.components.*
import java.util.UUID
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

internal class RoomDemoViewModel(private val repository: DemoNoteRepository) : ViewModel() {
    private val mutableError = MutableStateFlow<String?>(null)
    val error = mutableError.asStateFlow()
    private val mutableBusy = MutableStateFlow(false)
    val busy = mutableBusy.asStateFlow()
    val notes =
        repository
            .observeNotes()
            .catch { failure ->
                if (failure is CancellationException) throw failure
                mutableError.value = "Unable to load notes. Reopen this screen to retry."
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList(),
            )

    fun add(text: String) = write {
        if (text.isNotBlank())
            repository.add(DemoNote(id = UUID.randomUUID().toString(), text = text.trim()))
    }

    fun update(note: DemoNote, text: String) = write {
        if (text.isNotBlank()) repository.update(note.copy(text = text.trim()))
    }

    fun delete(note: DemoNote) = write { repository.delete(note) }

    private fun write(action: suspend () -> Unit) {
        if (mutableBusy.value) return
        mutableBusy.value = true
        viewModelScope.launch {
            try {
                action()
                mutableError.value = null
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                mutableError.value = "Unable to save changes. Please try again."
            } finally {
                mutableBusy.value = false
            }
        }
    }
}

@Composable
internal fun RoomDemoScreen(onBack: () -> Unit) {
    val context = LocalContext.current.applicationContext
    val model =
        viewModel(
            initializer = {
                RoomDemoViewModel(DemoNoteRepository(DemoDatabase.getInstance(context).notes()))
            }
        )
    val notes by model.notes.collectAsStateWithLifecycle()
    val busy by model.busy.collectAsStateWithLifecycle()
    val error by model.error.collectAsStateWithLifecycle()
    var text by rememberSaveable { mutableStateOf("") }
    var pendingDelete by remember { mutableStateOf<DemoNote?>(null) }
    val actionState = if (busy) ButtonState.Disabled else ButtonState.Enabled
    StillswarmBaseTheme {
        StillswarmAppShell(
            topBar = {
                AppTopBar(
                    title = "Room demo",
                    navigation = AppBarIcon(
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
                LazyColumn(
                    modifier = Modifier.padding(padding).padding(Dimens.medium),
                    verticalArrangement = Arrangement.spacedBy(Dimens.small),
                    content = {
                        item {
                            Text(text = "Notes are stored on this device and survive app restarts.")
                        }
                        item {
                            OutlinedTextField(
                                value = text,
                                onValueChange = { text = it },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text(text = "Note text") },
                                singleLine = true,
                            )
                        }
                        item {
                            SuperButton(
                                text = "Add note",
                                onClick = { model.add(text) },
                                state =
                                    if (busy || text.isBlank()) ButtonState.Disabled
                                    else ButtonState.Enabled,
                            )
                        }
                        error?.let { message -> item { Text(text = message) } }
                        items(
                            items = notes,
                            key = { it.id },
                            itemContent = { note ->
                                VLCard(
                                    modifier = Modifier.fillMaxWidth(),
                                    content = {
                                        Text(text = note.text)
                                        InlineButton(
                                            text = "Update with entered text",
                                            onClick = { model.update(note, text) },
                                            state =
                                                if (text.isBlank()) ButtonState.Disabled
                                                else actionState,
                                        )
                                        InlineButton(
                                            text = "Delete note",
                                            onClick = { pendingDelete = note },
                                            state = actionState,
                                        )
                                    },
                                )
                            },
                        )
                    },
                )
            },
        )
        pendingDelete?.let { note ->
            AppDialog(
                title = "Delete note?",
                confirmText = "Delete",
                destructive = true,
                onConfirm = {
                    model.delete(note)
                    pendingDelete = null
                },
                onDismissRequest = { pendingDelete = null },
                content = { Text(text = "This note will be permanently deleted.") },
            )
        }
    }
}
