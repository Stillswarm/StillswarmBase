# Stillswarm Base

Reusable Android library modules with a demo and a buildable starter. Open this directory in Android Studio and sync Gradle. Run `demo-app` or `starter-template`.

## Current scope

All library modules have Android build files, namespaces, manifests, resources, consumer rules, main/test/androidTest source sets, and Maven publications. Shared theme, buttons, cards, top bar, FAB, dismiss-only snackbar, arbitrary-content bottom sheet, confirmation dialog, and shell are implemented. Typed navigation, a latest-pending-request coordinator, and a lifecycle-aware UI handler are also implemented. The demo exercises component interactions and ViewModel-driven navigation. Typed Preferences DataStore storage and an in-memory testing implementation are also available. Configuration, telemetry, billing, and Firebase adapters remain scaffolds. Deep links and notifications are deferred. No Firebase project or credentials are required to build. No artifacts have been uploaded remotely.

The existing `app` scaffold was moved to `demo-app`, preserving its application ID, resources, and tests. The missing launcher activity was supplied.

## Build

Use the Android Studio bundled JDK and the installed SDK matching the existing project (compile/target 37, min 24). AGP provides built-in Kotlin support; do not add the legacy Kotlin Android plugin.

```sh
./gradlew assembleDebug
./gradlew testDebugUnitTest
./gradlew publishAllPublicationsToVerificationRepository
```

The last command publishes libraries locally to `build/maven-repository` for verification. `publishToMavenLocal` is also available. Remote GitHub Packages credentials/repository configuration are deliberately not supplied until the hosting repository is chosen.

## Modules

- `libraries:ui`: Compose theme and shared visual components.
- `libraries:navigation`: typed destination contracts, Navigator, and the conflated Channel coordinator.
- `libraries:app-shell`: root scaffold, snackbar host, and lifecycle-aware Navigation Compose handler; depends on UI and navigation.
- `libraries:config`: configuration contracts and session policies.
- `libraries:telemetry`: vendor-independent analytics/crash contracts.
- `libraries:storage-room`: optional Room BaseDao; app-owned database and query example in demo-app.
- `libraries:storage`: typed local preferences, atomic updates, and DataStore integration.
- `libraries:billing`: purchase and entitlement infrastructure.
- `integrations:firebase-config`: optional adapter depending on config.
- `integrations:firebase-telemetry`: optional adapter depending on telemetry.
- `testing`: test-only utilities depending on config, telemetry, and billing.
- `build-logic`: included Gradle build supplying shared Android library and publication defaults.

Artifacts use `com.stillswarm.base:foundation-<module-name>:0.1.0-SNAPSHOT`. Group and version are configurable in `gradle.properties`. Kotlin files can live under `src/main/java`; the package-info files keep empty package/source-set directories visible and versioned without fake implementations.
