package com.example.stillswarmbase.room

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import com.stillswarm.base.storage.room.BaseDao
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "demo_notes") data class DemoNote(@PrimaryKey val id: String, val text: String)

@Dao
interface DemoNoteDao : BaseDao<DemoNote> {
    @Query("SELECT * FROM demo_notes WHERE id = :id") suspend fun getById(id: String): DemoNote?

    @Query("SELECT * FROM demo_notes ORDER BY text, id") suspend fun getAll(): List<DemoNote>

    @Query("SELECT * FROM demo_notes WHERE id = :id") fun observeById(id: String): Flow<DemoNote?>

    @Query("SELECT * FROM demo_notes ORDER BY text, id") fun observeAll(): Flow<List<DemoNote>>
}

@Database(
    entities = [DemoNote::class],
    version = 1,
    exportSchema = true,
)
abstract class DemoDatabase : RoomDatabase() {
    abstract fun notes(): DemoNoteDao

    companion object {
        @Volatile private var instance: DemoDatabase? = null

        fun getInstance(context: Context): DemoDatabase =
            instance
                ?: synchronized(this) {
                    instance
                        ?: Room.databaseBuilder(
                                context = context.applicationContext,
                                klass = DemoDatabase::class.java,
                                name = "room-demo.db",
                            )
                            .build()
                            .also { instance = it }
                }
    }
}

/**
 * App-owned repository; the foundation does not define business operations or a universal CRUD API.
 */
internal class DemoNoteRepository(private val dao: DemoNoteDao) {
    fun observeNotes() = dao.observeAll()

    suspend fun add(note: DemoNote) {
        dao.insert(note)
    }

    suspend fun update(note: DemoNote) {
        dao.update(note)
    }

    suspend fun delete(note: DemoNote) {
        dao.delete(note)
    }
}
