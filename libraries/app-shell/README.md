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

## Shared pages

`SettingsScreen` and `AboutScreen` provide scrollable pages with a top-bar Back
button. Both enforce `StillswarmBaseTheme`, preserving the selected light/dark
mode while ignoring app colour overrides. Titles and Back descriptions can be
localized by callers; default English labels are Android resources.

Settings accepts a `LazyListScope` so apps can add sections and arbitrary content.
Use `SettingsSection`, `SettingsActionRow`, and `SettingsSwitchRow` for consistent
rows. The whole switch row is one accessible toggle target. Values, enabled
states, persistence, navigation, and errors belong to the app's ViewModel.

```kotlin
SettingsScreen(
    onBack = navigator::back,
    content = {
        item {
            SettingsSection(
                title = "Appearance",
                content = {
                    SettingsSwitchRow(
                        title = "Dark theme",
                        checked = state.dark,
                        onCheckedChange = model::setDark,
                    )
                },
            )
        }
    },
)
```

`AboutScreen` takes app name, a formatted version label, description, and Back
callback. Its optional icon, list of `AboutAction`s, and composable footer are
omitted when absent. Apps supply real support/privacy/store actions; the library
does not open links or infer destinations. No extra storage or Firebase dependency
is required. In the demo gallery, open the top-right menu → Settings or About.
