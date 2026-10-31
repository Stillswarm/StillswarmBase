# testing

Shared test fakes and assertions, consumed using testImplementation.

Artifact: `com.stillswarm.base:foundation-testing:0.1.1`.

This module provides local test doubles for foundation consumers.

`InMemoryPreferenceStore` provides isolated preferences for repository and ViewModel tests, including atomic edits and updates. Other testing utilities remain scaffolded.

`FakeRemoteConfigSource` simulates remote snapshots, delays, and exceptions and records the fetch count. Configure it before starting a config session.

`RecordingTelemetry` records events, screen views, breadcrumbs, and handled exceptions, with independent collection switches and isolated state per instance.

`FakeBilling` simulates lifetime purchase success, cancellation, pending payment,
failure, offline refresh, pending completion, and refund. It uses the same
`BillingSession` reconciliation as the Play adapter, with isolated in-memory state.
Use it only in tests or explicitly labelled demo screens; never inject it into a
production feature gate. Its price is labelled simulated.
