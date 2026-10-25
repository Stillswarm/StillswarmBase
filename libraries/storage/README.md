# Preferences storage

Artifact: `com.stillswarm.base:foundation-storage:0.1.0-SNAPSHOT`.

This module implements shared settings with Preferences DataStore, not the legacy
Android SharedPreferences API. All operations are local and asynchronous.

## Setup

Create `defaultPreferenceStore(applicationContext)` once in your app's dependency
container and inject `PreferenceStore` into app repositories. The factory uses an
application-context delegate that owns one DataStore per process for
`stillswarm_preferences.preferences_pb`. Repeated factory calls share that delegate.
Never create a second DataStore for the same file. This default is single-process.

For custom files, migrations, or scopes, create one app-owned Preferences DataStore
and pass it to `DataStorePreferenceStore`. Existing SharedPreferences files are not
migrated automatically; configure a migration on the app-owned DataStore if needed.

## API

Declare keys once, with unique namespaced names and stable types:

```kotlin
val sortOrder = PreferenceKey.string("journal.sort_order", "newest")
val launchCount = PreferenceKey.int("journal.launch_count")

store.observe(sortOrder) // Flow<String>; distinct values
store.get(sortOrder) // suspend, one-shot read
store.set(sortOrder, "oldest")
store.remove(sortOrder) // subsequent reads use the declared default
store.update(launchCount) { it + 1 } // atomic; returns committed value
store.edit {
    this[sortOrder] = "newest"
    this[launchCount] = 0
}
```

Supported types: Boolean, Int, Long, Float, Double, String, Set<String>.
A missing key uses its declared default without writing it to disk. Reusing a name
for a different type is invalid; type changes require migration. Defaults belong
to keys, so reuse the same declarations across all consumers.

Writes return after DataStore commits. Read/write/corruption errors propagate;
they are never silently replaced with defaults. Cancellation propagates too.
Transactions serialize concurrent updates, and failed blocks commit nothing.
Keep edit/update blocks short and free of side effects; do not call the store from
inside them. Editors expire when their transaction finishes and cannot be retained.

## Consumers and testing

ViewModels use app repositories, which use this API. The demo persists its theme
and app-colour toggles across launches and reports persistence failures.
`InMemoryPreferenceStore` in `:testing` provides isolated, atomic, in-memory storage
using the same preference adapter. Real DataStore unit tests cover disk persistence,
concurrency, rollback, observation, and failure propagation.

Future `:config` can depend on this module to persist overrides and accepted remote
snapshots. Validation, precedence, fetch timing, and session activation remain in
`:config`; that integration is not implemented yet. Use one transaction (or one
serialized value) per config snapshot, not independent writes for each key.

Room, credentials/encryption, attachments, backup/export/import, and blanket data
clearing are outside this implementation. Android backup policy remains app-owned.
