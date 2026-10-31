# App shell

Artifact: `com.stillswarm.base:foundation-app-shell:0.1.0-SNAPSHOT`.

`StillswarmAppShell` provides the Material scaffold and shared snackbar host.
`NavigationHandler` binds one Activity-scoped `NavigationCoordinator` to an
app-owned `NavHostController`, executing requests only while RESUMED.

Install the handler once beside your `NavHost`, outside individual destinations.
The app registers typed routes and chooses its start destination and transitions.
The handler does not construct the graph, own business state, or process intents.
Navigation Compose is exposed as an API dependency for apps building their graphs.
See the navigation module README and demo app for the complete wiring.

Deep-link requests rebuild root + target synchronously in the root handler. URI parsing and intent dispatch live in navigation; the app owns allowed routes and Activity wiring.
