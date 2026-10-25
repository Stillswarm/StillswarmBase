package com.example.stillswarmbase.room

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import java.util.UUID
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/** Exercises Room-generated code inheriting the published foundation BaseDao. */
class BaseDaoTest {
    private lateinit var database: DemoDatabase
    private lateinit var dao: DemoNoteDao

    @Before
    fun open() {
        database =
            Room.inMemoryDatabaseBuilder(
                    context = ApplicationProvider.getApplicationContext<Context>(),
                    klass = DemoDatabase::class.java,
                )
                .build()
        dao = database.notes()
    }

    @After
    fun close() {
        database.close()
    }

    @Test
    fun singleEntityCrudAndMissingRows() = runBlocking {
        val note = DemoNote(id = "one", text = "original")
        assertTrue(dao.insert(note) > 0)
        assertEquals(note, dao.getById(note.id))
        assertEquals(1, dao.update(note.copy(text = "edited")))
        assertEquals("edited", dao.getById(note.id)?.text)
        assertEquals(0, dao.update(DemoNote(id = "missing", text = "missing")))
        assertEquals(1, dao.delete(note))
        assertEquals(0, dao.delete(note))
        assertNull(dao.getById(note.id))
    }

    @Test
    fun duplicateInsertAbortsWithoutOverwriting() = runBlocking {
        val original = DemoNote(id = "one", text = "original")
        dao.insert(original)
        try {
            dao.insert(original.copy(text = "replacement"))
            fail("Duplicate insert must fail")
        } catch (_: SQLiteConstraintException) {}
        assertEquals(original, dao.getById(original.id))
    }

    @Test
    fun batchInsertRollsBackOnConflict() = runBlocking {
        dao.insert(DemoNote(id = "existing", text = "original"))
        try {
            dao.insertAll(
                listOf(
                    DemoNote(id = "new", text = "must roll back"),
                    DemoNote(id = "existing", text = "conflict"),
                )
            )
            fail("Batch must fail atomically")
        } catch (_: SQLiteConstraintException) {}
        assertNull(dao.getById("new"))
        assertEquals("original", dao.getById("existing")?.text)
    }

    @Test
    fun batchUpdatesDeletesAndUpsertsPreserveExpectedRows() = runBlocking {
        val first = DemoNote(id = "one", text = "Alpha")
        val second = DemoNote(id = "two", text = "Beta")
        assertEquals(2, dao.insertAll(listOf(first, second)).size)
        assertEquals(2, dao.updateAll(listOf(first.copy(text = "A"), second.copy(text = "B"))))
        dao.upsert(first.copy(text = "updated"))
        val third = DemoNote(id = "three", text = "Gamma")
        dao.upsertAll(listOf(second.copy(text = "upserted"), third))
        assertEquals(3, dao.getAll().size)
        assertEquals("updated", dao.getById("one")?.text)
        assertEquals("upserted", dao.getById("two")?.text)
        assertEquals(2, dao.deleteAll(listOf(first, third)))
        assertEquals(listOf("two"), dao.getAll().map { it.id })
        assertEquals(0, dao.deleteAll(emptyList()))
    }

    @Test
    fun flowsObserveInsertUpdateAndDelete() = runBlocking {
        withTimeout(5_000) {
            assertNull(dao.observeById("one").first())
            val inserted =
                async(start = CoroutineStart.UNDISPATCHED) {
                    dao.observeAll().first { it.isNotEmpty() }
                }
            val note = DemoNote(id = "one", text = "original")
            dao.insert(note)
            assertEquals(listOf(note), inserted.await())
            val updated =
                async(start = CoroutineStart.UNDISPATCHED) {
                    dao.observeById("one").first { it?.text == "edited" }
                }
            dao.update(note.copy(text = "edited"))
            assertEquals("edited", updated.await()?.text)
            dao.delete(note)
            assertTrue(dao.observeAll().first().isEmpty())
        }
    }

    @Test
    fun fileDatabasePersistsAfterReopening() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val name = "room-test-${UUID.randomUUID()}.db"
        fun openFile() =
            Room.databaseBuilder(
                context = context,
                klass = DemoDatabase::class.java,
                name = name,
            )
                .build()
        try {
            val first = openFile()
            try {
                first.notes().insert(DemoNote(id = "saved", text = "persisted"))
            } finally {
                first.close()
            }
            val reopened = openFile()
            try {
                assertEquals("persisted", reopened.notes().getById("saved")?.text)
            } finally {
                reopened.close()
            }
        } finally {
            context.deleteDatabase(name)
        }
    }
}
