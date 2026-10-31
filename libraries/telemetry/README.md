# Telemetry contracts

Artifact: `com.stillswarm.base:foundation-telemetry:0.1.0-SNAPSHOT`.
No Firebase, network client, database, worker, or connectivity observer is required.

Inject `Analytics` into ViewModels that track behavior and `CrashReporter` into code
that reports handled failures. `Telemetry` combines both for app wiring. `NoOpTelemetry`
is available for previews or apps without collection; `RecordingTelemetry` in :testing
records calls without a backend.

`Analytics.track(TelemetryEvent)` sends an explicit event. Parameters are typed Text,
Integer (Long), Decimal (finite Double), or Flag (Boolean). Events copy the input map.
Names must start with an ASCII letter, contain only letters/digits/underscores, and
have at most 40 characters. firebase_, google_, and ga_ prefixes are reserved.
At most 25 parameters are allowed; text values have at most 100 characters. Invalid
schemas throw immediately, so define stable names in app code. Provider-specific
reserved event names still apply. Use `screen(name)` rather than a custom screen_view.

Track a click in its callback or ViewModel action. Track an impression when content
actually becomes visible, not inside the composable body. The demo uses LaunchedEffect
for one impression per screen composition entry. Recomposition does not repeat it;
returning to the screen or recreating its composition can record another view.
Lazy lists need their own visibility policy; this module does not auto-track components.

`CrashReporter.breadcrumb` adds context to reports; `recordException` records a handled
(non-fatal) error. Uncaught-crash capture is provided by the backend SDK, not a custom
exception handler. Do not report ordinary cancellation as an error. Avoid free-form
user content, tokens, URIs with private arguments, or personal data in parameters,
breadcrumbs, or exception messages; this module does not automatically redact them.

Analytics and crash collection can be enabled independently. The app owns preferences,
consent decisions, and their persistence; telemetry is not implicitly controlled by
remote config. Calls made while disabled are dropped, not stored for later replay.
SDK method calls are asynchronous submissions, not delivery acknowledgements. There
is no foundation upload queue, retry loop, or forced flush API.
