# Firebase Remote Config adapter

Artifact: `com.stillswarm.base:foundation-firebase-config:0.1.0-SNAPSHOT`.

`FirebaseRemoteConfigSource` implements core `RemoteConfigSource`. Inject a configured
FirebaseRemoteConfig instance, then supply this adapter to a process-scoped ConfigSession.
The consuming APP applies the Google Services plugin and supplies google-services.json;
the library contains no project configuration or credentials. Firebase initializes via
its manifest provider before Application.onCreate.

The adapter calls fetch(minimumFetchIntervalSeconds), awaits activation, and returns
only VALUE_SOURCE_REMOTE values as strings. SDK defaults/static fallbacks are excluded:
ConfigKey defaults and core overrides remain authoritative. Firebase errors/throttling
propagate to core, which retains its accepted cached values. No real-time listener,
connectivity observer, or adapter retry loop is installed.

The default fetch interval is 12 hours. The demo uses zero in debug for manual testing
and 12 hours in release. Zero does not bypass Firebase's server throttling. Core calls
the adapter once per process and applies its own 5-second deadline in the demo.
Cancellation stops coroutine consumption, but Firebase Tasks can continue in the
background. A late result cannot update core's current session; SDK caches can still
change. Always read app config through ConfigSession, never directly from Firebase.
Only this adapter should own fetch/activate for its FirebaseRemoteConfig instance.

## Demo setup

Place your downloaded `google-services.json` in `demo-app/` before building the demo.
This file is intentionally gitignored; obtain it from Firebase Console > Project
settings > Your apps for the Android package `com.example.stillswarmbase`.
The current local copy connects to `stillswarm-demo`. On a fresh checkout, supply the
file yourself; CI must provision it before the Gradle build. Never substitute a
service-account key. The foundation libraries and starter-template do not need it.
No browser/CLI Google login is needed on the build machine once this file is present.
No remote project settings are modified by this code.

In Firebase Console > Remote Config, publish client parameters:

- welcome_text: String, e.g. "Hello from Firebase" (nonblank, at most 80 characters)
- suggestions_enabled: Boolean, e.g. true
- daily_limit: Number, e.g. 10 (integer in 1–100)

Then run demo-app > Try config. Clear local overrides to expose remote values, and
force-stop/relaunch for a new fetch. Ordinary screen navigation or rotation does not
fetch again. Missing remote keys use declared defaults. Invalid known values reject
the entire snapshot, preserving the previous accepted one. Applied can also mean a
successful empty snapshot when the console has no published parameters.

The live demo uses the demo_firebase storage namespace, separate from the old simulated
demo namespace. Old fake values and local overrides are intentionally not migrated.
Instrumentation uses a custom runner with a deterministic source and demo_test namespace;
it neither depends on published parameters nor seeds fake values into the live cache.

These are custom client parameters, not secrets. Analytics-based targeting, Analytics
collection, App Check, telemetry, and backend administration are outside this adapter.

For an optional read-only backend smoke check, run the demo connected test task with
`-Pandroid.testInstrumentationRunnerArguments.class=com.example.stillswarmbase.FirebaseConnectionTest`
and `-Pandroid.testInstrumentationRunnerArguments.liveFirebase=true`. It fetches live
values with a diagnostic 20-second timeout and reports only the parameter count; it
does not publish parameters or change the app's 5-second session timeout.

Debug demo startup logs use tag `DemoConfig` and report elapsed time, fetch status, and the welcome value source without logging parameter values. The UI stays responsive while the five-second fetch budget runs.
