package com.stillswarm.base.storage

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

// One delegate per file per process; never create another DataStore for this filename.
private val Context.foundationPreferences by preferencesDataStore(name = "stillswarm_preferences")

/**
 * Uses application context and a process-wide DataStore instance. This is single-process storage.
 */
fun defaultPreferenceStore(context: Context): PreferenceStore =
    DataStorePreferenceStore(context.applicationContext.foundationPreferences)

/** Inject an app-owned DataStore when custom filenames, migrations, or scopes are needed. */
class DataStorePreferenceStore(private val dataStore: DataStore<Preferences>) : PreferenceStore {
    override fun <T : Any> observe(key: PreferenceKey<T>) =
        dataStore.data.map { it[key.nativeKey] ?: key.defaultValue }.distinctUntilChanged()

    override suspend fun <T : Any> get(key: PreferenceKey<T>): T = observe(key).first()

    override suspend fun <T : Any> set(key: PreferenceKey<T>, value: T) {
        edit { this[key] = value }
    }

    override suspend fun <T : Any> remove(key: PreferenceKey<T>) {
        edit { remove(key) }
    }

    override suspend fun <T : Any> update(key: PreferenceKey<T>, transform: (T) -> T): T {
        val result = transact { this[key] = transform(this[key]) }
        return result[key.nativeKey] ?: key.defaultValue
    }

    override suspend fun edit(block: PreferenceEditor.() -> Unit) {
        transact(block)
    }

    private suspend fun transact(block: PreferenceEditor.() -> Unit): Preferences =
        dataStore.updateData { current ->
            val mutable = current.toMutablePreferences()
            val editor = Editor(mutable)
            try {
                editor.block()
                mutable.toPreferences()
            } finally {
                editor.active = false
            }
        }

    private class Editor(private val preferences: MutablePreferences) : PreferenceEditor {
        var active = true

        override fun <T : Any> get(key: PreferenceKey<T>): T {
            check(active) { "Preference editor is only valid inside edit" }
            return preferences[key.nativeKey] ?: key.defaultValue
        }

        override fun <T : Any> set(key: PreferenceKey<T>, value: T) {
            check(active) { "Preference editor is only valid inside edit" }
            preferences[key.nativeKey] = value
        }

        override fun <T : Any> remove(key: PreferenceKey<T>) {
            check(active) { "Preference editor is only valid inside edit" }
            preferences.remove(key.nativeKey)
        }
    }
}
