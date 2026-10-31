# testing

Shared test fakes and assertions, consumed using testImplementation.

Artifact: `com.stillswarm.base:foundation-testing:0.1.0-SNAPSHOT`.

This module provides local test doubles for foundation consumers.

`InMemoryPreferenceStore` provides isolated preferences for repository and ViewModel tests, including atomic edits and updates. Other testing utilities remain scaffolded.

`FakeRemoteConfigSource` simulates remote snapshots, delays, and exceptions and records the fetch count. Configure it before starting a config session.
