package com.stillswarm.base.storage.room

import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Update
import androidx.room.Upsert

/**
 * Common writes for app-owned entities. Extend from a concrete @Dao interface. Inserts/updates
 * abort on conflicts; callers must explicitly choose upsert when intended. Reads stay in concrete
 * DAOs because table names, keys, filters, and ordering are app-specific. Each list operation is
 * transactional. Failures propagate; no implicit retries or data deletion.
 */
interface BaseDao<T : Any> {
    /** Returns SQLite rowid, not necessarily the entity's primary key. */
    @Insert(onConflict = OnConflictStrategy.ABORT) suspend fun insert(entity: T): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAll(entities: List<T>): List<Long>

    /** Matches by primary key; returns zero if the row does not exist. */
    @Update(onConflict = OnConflictStrategy.ABORT) suspend fun update(entity: T): Int

    @Update(onConflict = OnConflictStrategy.ABORT) suspend fun updateAll(entities: List<T>): Int

    @Delete suspend fun delete(entity: T): Int

    /** Deletes only the supplied entities, never clears the entire table. */
    @Delete suspend fun deleteAll(entities: List<T>): Int

    /** Inserts missing primary keys and updates existing ones, without REPLACE semantics. */
    @Upsert suspend fun upsert(entity: T)

    @Upsert suspend fun upsertAll(entities: List<T>)
}
