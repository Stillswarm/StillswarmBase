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

`Dimens.dp8`, `dp12`, `dp16`, and `dp24` are spacing tokens, not touch-target limits. Use onSurfaceVariant for secondary text and outlineVariant for subtle dividers.

Fonts are static instances for API 24 compatibility; provenance is in FONT_SOURCES.md and licences ship in assets/font-licenses. Unsupported scripts use Android fallback.
