# Navigation

Artifact: `com.stillswarm.base:foundation-navigation:0.1.0-SNAPSHOT`.

`Navigator` is the interface injected into feature ViewModels. `NavigationCoordinator`
is its Activity-scoped ViewModel implementation. It owns a conflated Channel:
only the latest undelivered request is retained, and receiving consumes it.
There is no event acknowledgement, replay cache, or global broadcast bus.

- `navigate(destination)` opens a destination with single-top behavior.
- `back()` pops one screen; at the root it does nothing. System Back remains platform-owned.
- `replace(destination)` replaces the current screen in one request.
- `resetTo(destination)` clears the graph's screens and installs a new root.

Apps own their routes: implement `Destination`, annotate each concrete route with
Kotlin serialization's `@Serializable`, and register it with `composable<Route>`.
Pass small identifiers, not domain objects. Apply the Kotlin serialization Gradle
plugin in modules declaring routes. The marker cannot enforce serialization at
compile time; missing serializers or unregistered routes are programming errors.

Create one coordinator outside the NavHost, using the Activity's ViewModelStore,
and inject that same instance as `Navigator` into screen-scoped ViewModels.
Do not create one coordinator per screen. Install exactly one `NavigationHandler`
from app-shell beside the NavHost. Screens receive callbacks; ViewModels depend
on `Navigator`, never NavController or Activity.

The app-shell handler collects with ordinary `collect` while the root lifecycle
is RESUMED. Navigation Compose owns the back stack, saved-state restoration, and
system Back. Commands are synchronous on the UI thread; pending requests survive
rotation through the coordinator but are not persisted across process death.
Delivery is best effort: cancellation during receipt can lose a request. Multiple
requests emitted before consumption replace each other, so use atomic replace/reset
commands rather than sequences of back + navigate. Stale background work should
not navigate. Clearing the owning ViewModelStore discards pending requests.

See `demo-app/NavigationDemo.kt` for typed routes, ViewModel injection, graph
registration, argument decoding, and UI binding. No dependency injection framework
is required. Notification creation, auth gates, navigation results, and multiple back stacks remain outside this implementation.


## Deep links

`ValidatingDeepLinkResolver` parses a URI and checks an exact scheme/host/port
allowlist before calling app-owned route mapping. Mapping returns `Resolved`,
`Unsupported`, or `Invalid`. Relative/opaque/malformed URIs, user info, and fragments
are rejected. Apps validate the path, query parameters, IDs, and access permissions.
The foundation never constructs a destination from arbitrary intent extras.

`DeepLinkHandler` accepts ACTION_VIEW intents and dispatches successful resolutions
through `Navigator.openDeepLink(root, destination)`. Non-link intents are ignored;
invalid/unsupported links return a result without changing navigation. Apps choose
how to report the failure. Resolution does not authorize access to stored records.

The shell executes root + destination as a single command. It rebuilds the stack
for both cold and warm links; Back returns to the supplied root. Linking to the root
itself creates just that screen. Repeated links do not accumulate duplicate entries.
This policy intentionally replaces the previous flow and its screen state.

### Activity wiring

- Share one Activity-scoped coordinator between the handler and NavHost.
- Call the handler in onCreate only when savedInstanceState is null.
- Override onNewIntent, call super and setIntent, then handle that new delivery.
- Use singleTop for the demonstrated single-Activity app; other task architectures
  must choose their own launch flags/policy.
- Do not also register these links with NavHost deepLinks/handleDeepLink; this router
  owns dispatch, avoiding two handlers for the same intent.

Skipping the original intent on saved-state restoration prevents replay after
rotation or Back. It is not permanent URL deduplication: tapping the same URL
again is a new request. The retained Channel preserves the latest pending request
across configuration changes. Pending requests remain best-effort and are not
persisted across process death; the NavHost restores already-executed navigation.

### Demo

Supported routes:
- `stillswarm-demo://app/gallery`
- `stillswarm-demo://app/room`
- `stillswarm-demo://app/entry/sample-entry`

Entry IDs allow 1–64 ASCII letters, digits, underscores, and hyphens. Unknown query
parameters and encoded separators are rejected. The entry destination is a sample
screen displaying the ID; it does not resolve a real database record.

With demo-app installed:

```sh
adb shell am start -W -a android.intent.action.VIEW -d 'stillswarm-demo://app/entry/sample-entry' com.example.stillswarmbase
```

For a cold-start check, force-stop the demo first; for a warm-start check, leave it
open. Custom schemes require no server but do not establish domain ownership.
Production HTTPS App Links need an app-owned domain, a matching manifest filter,
and a hosted Digital Asset Links association. No domain or verification is configured
in this demo. Notification PendingIntent creation is deferred; a future ACTION_VIEW
notification intent carrying the same URI can enter this pipeline.
