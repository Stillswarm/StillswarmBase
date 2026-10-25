package com.stillswarm.base.storage

import androidx.datastore.preferences.core.*

class PreferenceKey<T : Any>
private constructor(internal val nativeKey: Preferences.Key<T>, val defaultValue: T) {
    val name: String
        get() = nativeKey.name

    init {
        require(name.isNotBlank()) { "Preference names cannot be blank" }
    }

    companion object {
        fun boolean(name: String, defaultValue: Boolean = false) =
            PreferenceKey(booleanPreferencesKey(name), defaultValue)

        fun int(name: String, defaultValue: Int = 0) =
            PreferenceKey(intPreferencesKey(name), defaultValue)

        fun long(name: String, defaultValue: Long = 0L) =
            PreferenceKey(longPreferencesKey(name), defaultValue)

        fun float(name: String, defaultValue: Float = 0f) =
            PreferenceKey(floatPreferencesKey(name), defaultValue)

        fun double(name: String, defaultValue: Double = 0.0) =
            PreferenceKey(doublePreferencesKey(name), defaultValue)

        fun string(name: String, defaultValue: String = "") =
            PreferenceKey(stringPreferencesKey(name), defaultValue)

        fun stringSet(name: String, defaultValue: Set<String> = emptySet()) =
            PreferenceKey(
                stringSetPreferencesKey(name),
                java.util.Collections.unmodifiableSet(defaultValue.toSet()),
            )
    }
}
