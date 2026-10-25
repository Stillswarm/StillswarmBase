package com.stillswarm.base.storage

import kotlinx.coroutines.flow.Flow

interface PreferenceStore {
    fun <T : Any> observe(key: PreferenceKey<T>): Flow<T>

    suspend fun <T : Any> get(key: PreferenceKey<T>): T

    suspend fun <T : Any> set(key: PreferenceKey<T>, value: T)

    suspend fun <T : Any> remove(key: PreferenceKey<T>)

    suspend fun <T : Any> update(key: PreferenceKey<T>, transform: (T) -> T): T

    /**
     * Atomic transaction. Keep the block short; do not retain its editor or nest store operations.
     */
    suspend fun edit(block: PreferenceEditor.() -> Unit)
}

interface PreferenceEditor {
    operator fun <T : Any> get(key: PreferenceKey<T>): T

    operator fun <T : Any> set(key: PreferenceKey<T>, value: T)

    fun <T : Any> remove(key: PreferenceKey<T>)
}
