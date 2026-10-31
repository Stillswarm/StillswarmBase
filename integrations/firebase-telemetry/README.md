# Firebase telemetry

Artifact: `com.stillswarm.base:foundation-firebase-telemetry:0.1.3`.
FirebaseTelemetry implements Analytics + CrashReporter with Firebase Analytics and
Crashlytics. Both collection flags default to false in its constructor; apps opt in.

The APP supplies google-services.json, applies Google Services and Crashlytics Gradle
plugins, and creates one telemetry instance using FirebaseAnalytics.getInstance(context)
and FirebaseCrashlytics.getInstance(). The Crashlytics plugin handles build IDs and
release mapping integration. Libraries do not contain project configuration.

Firebase SDK versions are aligned through BoM 34.7.0 for this project's Kotlin 2.2
compiler. BoM 35.1.0's Analytics artifacts have Kotlin 2.4 metadata and cannot be
consumed by the current compiler; do not upgrade that BoM independently of Kotlin.

Event parameters map to Bundle strings, longs, and doubles. Boolean flags become
1L/0L. screen(name) logs Firebase's screen_view event with screen_name. Disable
SDK automatic screen reporting when manually tracking Compose screens, as the demo
manifest does. No user ID, advertising identity, or user-property API is added here.

The SDK owns offline event storage, upload scheduling, session events, and fatal-crash
capture. A successful adapter call does not mean the backend received it. Disabled
adapter calls are dropped. Crashlytics collection disabled means automatic upload is
disabled; the SDK can still store crash reports locally and send them after re-enabling.
Changing a collection flag is not a purge of already-collected data.

SDK exceptions are caught so telemetry does not break app actions. An optional
onFailure callback supports local diagnostics and must not recurse into telemetry.
Schema validation remains a caller error and is not silently ignored.

## Demo

Open demo-app > Try telemetry. Entering sends a screen view and component_impression;
buttons submit button_click and a deliberately handled sample exception. Nothing
intentionally crashes the app. The two collection switches last for the demo process;
normal demo startup explicitly enables both again. Production apps should persist
and apply their own policy rather than copying that demo shortcut.

Manifest defaults disable collection before app setup. SDK collection preferences
can persist and override manifest defaults on later launches. Instrumentation replaces
the adapter with RecordingTelemetry and explicitly disables Firebase collection.
No live report delivery is asserted by the automated tests.

To inspect live Analytics events, enable Google Analytics for this Firebase project
if it is not already enabled, then open Analytics > DebugView. On your test device:

```sh
adb shell setprop debug.firebase.analytics.app com.example.stillswarmbase
```

Launch the normal demo (not instrumentation) and send a sample event. Disable debug
mode afterwards with `adb shell setprop debug.firebase.analytics.app .none.`.
For crash reports, open Crashlytics in the Firebase console and complete any setup
prompt. Submit a sample non-fatal report and relaunch; reporting is asynchronous.
A separate intentional fatal-crash check can be done later when explicitly requested.
