package com.stillswarm.base.testing

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import com.stillswarm.base.storage.DataStorePreferenceStore
import com.stillswarm.base.storage.PreferenceStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Isolated per instance. Uses the production preference semantics without files or Android context.
 * Test substitute for the real preferences store.
*/
class InMemoryPreferenceStore : PreferenceStore by DataStorePreferenceStore(MemoryDataStore())

private class MemoryDataStore : DataStore<Preferences> {
    private val state = MutableStateFlow(emptyPreferences())
    private val mutex = Mutex()
    override val data: Flow<Preferences> = state

    override suspend fun updateData(transform: suspend (Preferences) -> Preferences): Preferences =
        mutex.withLock { transform(state.value).toPreferences().also { state.value = it } }
}
