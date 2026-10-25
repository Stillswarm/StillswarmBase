package com.stillswarm.base.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.espresso.Espresso.pressBack
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.stillswarm.base.ui.components.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ComponentInteractionTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun buttonsBlockDisabledAndLoadingTapsAndKeepWidth() {
        var calls = 0
        val state = mutableStateOf(ButtonState.Enabled)
        compose.setContent {
            StillswarmTheme {
                SuperButton(
                    text = "Save",
                    onClick = { calls++ },
                    state = state.value,
                    loadingText = "Saving changes",
                )
            }
        }
        val width = compose.onNodeWithText("Save").fetchSemanticsNode().boundsInRoot.width
        compose.onNodeWithText("Save").performClick()
        compose.runOnIdle {
            assertEquals(1, calls)
            state.value = ButtonState.Disabled
        }
        compose.onNodeWithText("Save").assertIsNotEnabled().performTouchInput { click() }
        compose.runOnIdle {
            assertEquals(1, calls)
            state.value = ButtonState.Loading
        }
        compose.onNodeWithText("Saving changes").assertIsNotEnabled().performTouchInput { click() }
        compose.runOnIdle { assertEquals(1, calls) }
        val loadingWidth =
            compose.onNodeWithText("Saving changes").fetchSemanticsNode().boundsInRoot.width
        assertEquals(width, loadingWidth, .1f)
    }

    @Test
    fun inlineLoadingDoesNotInvokeCallback() {
        var calls = 0
        compose.setContent {
            StillswarmTheme {
                InlineButton(
                    text = "Save",
                    onClick = { calls++ },
                    state = ButtonState.Loading,
                    loadingText = "Saving",
                )
            }
        }
        compose.onNodeWithText("Saving").assertIsNotEnabled().performTouchInput { click() }
        compose.runOnIdle { assertEquals(0, calls) }
    }

    @Test
    fun dialogCancelAndBackNeverConfirmDeletion() {
        var deleted = 0
        var cancelled = 0
        compose.setContent {
            StillswarmTheme {
                AppDialog(
                    title = "Delete entry?",
                    confirmText = "Delete entry",
                    onConfirm = { deleted++ },
                    onDismissRequest = { cancelled++ },
                    destructive = true,
                    content = { Text(text = "Permanent deletion") },
                )
            }
        }
        compose.onNodeWithText("Cancel").performClick()
        compose.runOnIdle {
            assertEquals(0, deleted)
            assertEquals(1, cancelled)
        }
        pressBack()
        compose.runOnIdle {
            assertEquals(0, deleted)
            assertEquals(2, cancelled)
        }
        compose.onNodeWithText("Delete entry").performClick()
        compose.runOnIdle { assertEquals(1, deleted) }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Test
    fun bottomSheetAcceptsArbitraryContentAndDismissesOnBack() {
        var dismissed = false
        val name = mutableStateOf("")
        val checked = mutableStateOf(false)
        compose.setContent {
            StillswarmTheme {
                AppBottomSheet(
                    onDismissRequest = { dismissed = true },
                    title = "New entry",
                    content = {
                        Column {
                            OutlinedTextField(
                                value = name.value,
                                onValueChange = { name.value = it },
                                label = { Text(text = "Name") },
                            )
                            Switch(
                                checked = checked.value,
                                onCheckedChange = { checked.value = it },
                            )
                            Text(text = "Custom footer")
                        }
                    },
                )
            }
        }
        compose.onNodeWithText("Name").performTextInput("Hello")
        compose.onNode(isToggleable()).performClick()
        compose.onNodeWithText("Custom footer").assertIsDisplayed()
        compose.onNodeWithContentDescription("Dismiss notification").assertDoesNotExist()
        compose.runOnIdle {
            assertEquals("Hello", name.value)
            assertTrue(checked.value)
        }
        closeSoftKeyboard()
        pressBack()
        compose.waitUntil(5000) { dismissed }
    }

    @Test
    fun snackbarCloseReturnsDismissedWithoutUndoAction() {
        val host = SnackbarHostState()
        var result: SnackbarResult? = null
        compose.setContent {
            StillswarmTheme {
                AppSnackbarHost(hostState = host)
                LaunchedEffect(Unit) {
                    result = host.showAppSnackbar("Saved", SnackbarDuration.Indefinite)
                }
            }
        }
        compose.onNodeWithText("Saved").assertIsDisplayed()
        compose.onNodeWithText("Undo").assertDoesNotExist()
        compose.onNodeWithContentDescription("Dismiss notification").performClick()
        compose.runOnIdle { assertEquals(SnackbarResult.Dismissed, result) }
    }

    @Test
    fun overflowSelectionCallsOnlySelectedAction() {
        var export = 0
        var settings = 0
        compose.setContent {
            StillswarmTheme {
                AppTopBar(
                    title = "Entries",
                    action =
                        AppTopBarAction.Overflow(
                            contentDescription = "More options",
                            items =
                                listOf(
                                    AppMenuItem(text = "Export", onClick = { export++ }),
                                    AppMenuItem(text = "Settings", onClick = { settings++ }),
                                ),
                            icon = { Text(text = "⋮") },
                        ),
                )
            }
        }
        compose.onNodeWithContentDescription("More options").performClick()
        compose.onNodeWithText("Export").performClick()
        compose.runOnIdle {
            assertEquals(1, export)
            assertEquals(0, settings)
        }
        compose.onNodeWithText("Settings").assertDoesNotExist()
    }
}
