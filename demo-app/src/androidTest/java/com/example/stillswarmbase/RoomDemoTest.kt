package com.example.stillswarmbase

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.example.stillswarmbase.room.DemoDatabase
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.util.UUID

class RoomDemoTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Test
    fun galleryOpensRoomDemoAndSavesANote() {
        val text = "Room test ${UUID.randomUUID()}"
        val dao = DemoDatabase.getInstance(compose.activity).notes()
        try {
            compose.waitUntil(timeoutMillis = 5_000) {
                compose.onAllNodesWithText(text = "Try Room storage").fetchSemanticsNodes().isNotEmpty()
            }
            compose.onNodeWithText(text = "Try Room storage").performScrollTo().performClick()
            compose.onNodeWithText(text = "Room demo").assertIsDisplayed()
            compose.onNodeWithText(text = "Add note").assertIsNotEnabled()
            compose.onNode(hasSetTextAction()).performTextInput(text)
            compose.onNodeWithText(text = "Add note").performClick()
            compose.waitUntil(timeoutMillis = 5_000) {
                runBlocking { dao.getAll().any { it.text == text } }
            }
            assertTrue(runBlocking { dao.getAll().any { it.text == text } })
        } finally {
            runBlocking { dao.deleteAll(dao.getAll().filter { it.text == text }) }
        }
    }
}
