# Core config

Artifact: `com.stillswarm.base:foundation-config:0.1.0-SNAPSHOT`.
Depends on preferences storage; no Firebase, Room, connectivity listener, or network SDK.

## App setup

Declare typed `ConfigKey` instances once and register them in a `ConfigSession`.
Supported types are Boolean, String, Int, Long, and finite Double. Defaults and
local edits are validated. String/numeric keys can supply a pure validator.
Duplicate names and use of an unregistered key instance fail immediately.

```kotlin
val limit = ConfigKey.int(
    name = "daily_limit",
    defaultValue = 5,
    validate = { it in 1..100 },
)
val config = ConfigSession(
    keys = listOf(limit),
    storage = preferenceStore,
    remoteSource = remoteSource, // optional; defaults/cache/overrides work without it
    fetchTimeoutMillis = 2_000,
    namespace = "app",
)
```

Create ONE session per process and namespace in the app's dependency container.
Launch `config.start()` once from an application-owned coroutine scope at cold start.
Concurrent/repeated calls are idempotent and wait for the first start to finish;
they never trigger another fetch. Cancellation is propagated and terminal for that
session. Do not scope start to a screen or Activity recreation.

Defaults are readable immediately. Persisted values are loaded before the remote
attempt. Observe `state.localLoaded` if a decision must wait for persisted overrides;
do not interpret the initial default as the final startup value. UI need not block
while the remote fetch runs. `observe(key)` emits distinct effective values; `get`
(operator `config[key]`) reads synchronously. Read related keys from the SAME
`state.value.snapshot` for a consistent view. `snapshot.source(key)` reports Default,
Remote, or LocalOverride.

## Precedence and activation

Local override > last accepted remote snapshot > declared default.

`RemoteConfigSource.fetch()` returns a complete `Map<String, String>` snapshot.
The provider must cooperate with coroutine cancellation. The timeout bounds this
fetch, not disk I/O during local initialization or snapshot persistence. Core config
adds no retries, refresh timers, or connection monitoring. Late timed-out results
are never applied. A new process/session is required to fetch again.

Unknown remote keys are ignored. Any invalid KNOWN value rejects the whole response,
preserving the last accepted snapshot. An accepted response REPLACES the cached map:
omitted keys return to defaults (unless overridden), and an empty response clears
remote values. Each accepted snapshot is saved as one JSON preference value and
only then activated. A failed cache write preserves the old values for this session.

Explicit local edits apply during the session after durable persistence:

```kotlin
config.setOverride(limit, 20)
config.removeOverride(limit)
config.clearOverrides() // only this namespace; remote cache and other settings remain
```

Edits serialize with snapshot activation; an override made while fetching wins.
Invalid local values throw without changing storage. Failed local writes propagate
without publishing unsaved values. Removing an override reveals the currently
accepted remote/default value, without fetching again.

## Failures and schema evolution

`state.fetchStatus` distinguishes NotStarted, Disabled, Fetching, Applied, TimedOut,
Failed, Rejected, and Cancelled. `state.issues` accumulates storage diagnostics for
this session. Read failures fall back to defaults; unreadable overrides cannot be
overwritten by a local edit until a subsequent read succeeds. Invalid stored JSON
falls back to defaults. Stored unknown keys and individual values that no longer
pass the current schema are ignored. Invalid known stored values are reported.

Persistence uses `config.<namespace>.remote` and `config.<namespace>.overrides`.
Namespaces isolate apps/features within one preferences file. Renaming a key is a
new key; changing type/default/validation is an app schema decision. No generic
migration engine or server-side targeting is included. Config values are not secrets
or authorization checks. Cross-key business constraints belong in the provider/app.

## Demo and tests

Run demo-app and select **Try config**. The app starts one process-scoped session
with the Firebase Remote Config adapter, showing values, sources, fetch status, and
storage issues. Toggle suggestions, save a limit in 1–100, or clear overrides.
Overrides persist across relaunches. Force-stop/relaunch to start another remote
fetch; navigating away or rotating the Activity does not refresh it.

`:testing` supplies `FakeRemoteConfigSource` with configurable values, delay, failure,
and a fetch count. Core tests exercise precedence, snapshot replacement/rejection,
timeouts, idempotent concurrent start, cancellation, override races, storage errors,
observation, schema validation, and namespace isolation. The storage module separately
tests real on-disk persistence. The optional Firebase adapter is implemented in `:integrations:firebase-config`; core remains provider-independent.
