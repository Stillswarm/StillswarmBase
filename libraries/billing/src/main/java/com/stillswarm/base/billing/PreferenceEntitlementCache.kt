package com.stillswarm.base.billing

import com.stillswarm.base.storage.PreferenceKey
import com.stillswarm.base.storage.PreferenceStore

/** Use a separate namespace for each store/environment. This is an offline cache, not proof. */
class PreferenceEntitlementCache(private val storage: PreferenceStore, namespace: String) :
    EntitlementCache {
    init {
        require(namespace.isNotBlank())
    }

    private val key = PreferenceKey.stringSet("billing.$namespace.lifetime")

    override suspend fun read() = storage.get(key)

    override suspend fun write(productIds: Set<String>) = storage.set(key, productIds)
}
