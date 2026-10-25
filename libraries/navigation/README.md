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
is required. Deep links, notifications, auth gates, navigation results, and multiple
back stacks are outside this implementation.
