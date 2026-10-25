# Foundation UI

Neutral light/dark palettes, offline Manrope headings and Inter body text, and shared spacing. Dynamic wallpaper colours are disabled with no automatic opt-in.

## App theme

```kotlin
StillswarmTheme(
    lightColorScheme = StillswarmColors.Light.copy(primary = myPrimary, onPrimary = myOnPrimary),
    darkColorScheme = StillswarmColors.Dark.copy(primary = myDarkPrimary, onPrimary = myDarkOnPrimary),
    // Optional font-family overrides; defaults are bundled Manrope and Inter.
    typography = stillswarmTypography(),
) { AppContent() }
```

Override matching container/on-colour roles together where needed to preserve contrast. `darkTheme` defaults to system appearance but can be supplied by app settings.

## Shared screens

Wrap each shared Settings/About/Privacy destination in `StillswarmBaseTheme { ... }`. This restores base colours and fonts while inheriting the parent Stillswarm light/dark preference. Place a Surface or Scaffold inside to paint the background. No shared Settings/About screen exists yet.

## Type and spacing

Use `MaterialTheme.typography.titleLarge` for screen headings (24/32sp), `titleMedium` for sections (18/26sp), `bodyLarge` for body/input text (16/24sp), `bodyMedium` for supporting text (14/20sp), and `labelLarge` for buttons (14/20sp). Heading styles use Manrope 600; body styles use Inter 400 and labels Inter 500. Sp sizes respect system font scaling.

`Dimens.xxsmall`, `xsmall`, `small`, `medium`, `large`, `xlarge`, and `xxlarge` map to 4, 8, 12, 16, 20, 24, and 28dp. Values are read from prefixed Android dimension resources in `res/values/dimens.xml`. Touch targets, strokes, progress indicators, and dialog widths use purpose-named resources outside that spacing scale. Use onSurfaceVariant for secondary text and outlineVariant for subtle dividers.

Fonts are static instances for API 24 compatibility; provenance is in FONT_SOURCES.md and licences ship in assets/font-licenses. Unsupported scripts use Android fallback.


## Components

All components are in `com.stillswarm.base.ui.components` and use the enclosing theme.

- `SuperButton` and `InlineButton`: `ButtonState.Enabled`, `Disabled`, `Loading`. Loading blocks taps, shows progress, and reserves both label widths. Supply localized `loadingText` as needed. Use `Modifier.fillMaxWidth()` for a full-width primary action.
- `AppCard`: vertical/horizontal layout, optional leading/trailing slots, arbitrary `ColumnScope` content. `VLCard` and `HLCard` are convenience wrappers. Null `onClick` means a non-clickable surface. Avoid nesting interactive actions inside an already clickable card unless intentional.
- `AppTopBar`: title, search, or both. Optional `AppBarIcon` navigation; exactly one `AppTopBarAction.Direct` or `.Overflow`. Callers own the query and business callbacks. Caller icon slots should draw decorative 24dp icons with null content descriptions; the surrounding control supplies the label.
- `AppFab`: icon-only or extended using `label`. Provide a localized accessible description.
- `AppSnackbar` / `AppSnackbarHost`: message with a dismiss X, no action button. Use `SnackbarHostState.showAppSnackbar(message)` to get queued messages and accessibility-aware duration. Dismiss returns `SnackbarResult.Dismissed`.
- `AppBottomSheet`: arbitrary `content: @Composable () -> Unit`, optional title, native handle/outside-tap/swipe/back dismissal, no close icon. Remove it from composition in `onDismissRequest`. Content owns scrolling (e.g. use a bounded LazyColumn for long lists). Sheet state is configurable; standard Material sheet APIs require experimental opt-in at present.
- `AppDialog`: arbitrary body, centered 36dp icon, compact 12dp-horizontal/8dp-vertical action padding with Material touch targets. Up to two actions. `destructive=true` changes the confirm button to error colours; it does not supply an icon. The optional icon slot is omitted entirely when null. The demo explicitly supplies `Icons.Rounded.Warning` for deletion. Dialog bodies do not carry an internal scroll state. Only `onConfirm` performs the operation; Cancel/back/outside tap invoke `onDismissRequest`. The caller owns visibility, operation state, and failure handling. Show a success snackbar only after a successful operation. Configure `DialogProperties` if a workflow needs to prevent outside/back dismissal.

`StillswarmAppShell` accepts `topBar`, `floatingActionButton` and a `SnackbarHostState`, and uses the dismiss-only host. The demo includes a form bottom sheet (not radio buttons), delete confirmation, all button states, card layouts, and top-bar modes in its overflow menu.

Device interaction tests live in `src/androidTest`: disabled/loading taps, stable button width, sheet slots/back, dialog cancellation, snackbar dismissal, and overflow dispatch.


## Overrides and icons

Components expose `shape` and nullable `border` parameters with base-style defaults; cards keep their subtle default outline, other surfaces default to no border. Set `border = null` to remove a card outline. `AppDialog` also exposes `actionShape`. Components without a Material border argument apply the supplied border through their modifier.

The demo depends on `androidx.compose.material:material-icons-extended` and uses Rounded icons. The foundation uses the smaller core icon dependency for Close and Search. No custom component icon drawables remain. New app modules can use the same extended dependency via `libs.androidx.compose.material.icons.extended`.

Dialog actions retain FlowRow: when larger text or longer labels no longer fit side by side, actions wrap instead of clipping. Each action keeps its accessible touch target despite the compact visible padding.

Call-site conventions are recorded in the root AGENTS.md and .cursor/rules/kotlin-conventions.mdc.
