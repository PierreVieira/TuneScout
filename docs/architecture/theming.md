# Theming

The app renders in a light or a dark palette, and on Android 12+ it can take its colors from the
wallpaper instead. The user picks between the three modes in the theme sheet
(`feature/theme_selection`); the choice is a `Theme` (`LIGHT`, `DARK`, `SYSTEM`, default `SYSTEM`)
stored in the Preferences DataStore.

## Reading a color

Every color a composable draws comes from `TuneScoutColors`, an accessor object whose properties
are `@Composable` getters over `LocalTuneScoutColorPalette` — the same shape as `TuneScoutIcons`.

```kotlin
Text(
    text = song.title,
    color = TuneScoutColors.textPrimary,
)
```

Never write a `Color(0xFF...)` in a feature or in `ui/component`. A new color is a new field on
`TuneScoutColorPalette`, given a value in **both** `darkColorPalette` and `lightColorPalette`.

The tokens are named for the role they play, not for the value they happen to have in one palette
— `surfaceSubtle`, not `white10`. A token whose name only makes sense in the dark palette is a
token that will be wrong in the light one.

### Outside a composable

A `@Composable` getter cannot be read from a `drawBehind`/`drawWithCache` lambda, a `remember`
block or a plain function. Read it in composable scope and close over the value:

```kotlin
// Correct
val trackColor = TuneScoutColors.surfaceSubtle
Box(modifier = Modifier.drawBehind { drawRect(color = trackColor) })

// Wrong — TuneScoutColors is not readable inside the draw lambda
Box(modifier = Modifier.drawBehind { drawRect(color = TuneScoutColors.surfaceSubtle) })
```

`TuneScoutBrandColors` is the exception that needs no composable scope: it holds the two splash
colors, which are fixed. The splash continues the system splash window, whose background is
declared in `res/values/colors.xml` and drawn before any preference can be read, so it cannot
follow the theme and does not try to.

## How the palette is resolved

`TuneScoutTheme(theme, isDynamicColorEnabled, content)` does three things:

1. resolves `theme` to a `Boolean` with `Theme.isDark()` (`SYSTEM` asks `isSystemInDarkTheme()`);
2. builds the palette — from the platform's dynamic `ColorScheme` when dynamic color is on and the
   device is on API 31+, otherwise from the static light/dark palette;
3. provides it through `LocalTuneScoutColorPalette` and hands Material 3 a matching `ColorScheme`,
   so the Material components (`Switch`, `HorizontalDivider`, ...) agree with the palette.

`colorPalette(isDark, isDynamicColorEnabled)` returns the palette a theme *would* render with,
regardless of the one in force. The preview cards in the theme sheet are drawn with it.

## The system bars follow the app's theme

`MainActivity` calls `enableEdgeToEdge` from a `LaunchedEffect(isDark)`, passing a style for **both**
bars. The styles come from `SystemBars.of(isDark)` in `ui/theme`, which describes each bar as plain
data (`SystemBarSpec`) so the scrims can be checked in a test. `isDark` is `Theme.isDark()`, the same
value `TuneScoutTheme` resolves `Theme.SYSTEM` with, so the bars never disagree with the palette. Neither may be left to `SystemBarStyle.auto`: `auto` reads the platform's night mode, so an app
set to `Theme.DARK` on a phone whose system is light would keep the light bar — dark icons over a
white scrim under a black app. The navigation bar used to be the default `auto` and showed exactly
that.

`SystemBarStyle.dark` also tells `enableEdgeToEdge` to stop enforcing navigation bar contrast, which
is what leaves the bar transparent over the dark palette. The light style keeps the scrims, because
API 26 has no `windowLightNavigationBar` flag and the bar's icons would otherwise be white on white.
The status bar passes transparent scrims instead: its contrast is never enforced.

`auto` is not only a dark mode check, either. It also drops the scrims and turns on the platform's
navigation bar contrast from API 29, so using it for `Theme.SYSTEM` would still change the navigation
bar on 3-button navigation. That is why dark mode is resolved in composition rather than left to
`auto`, and why the bars are not in `MainUiState`: the ViewModel cannot see the device's night mode.

A sheet or a dialog opens a window of its own, and the system bars take their icons from it while it
is in front. `ModalBottomSheet` picks them once, when its window is created, so `BottomSheetScene`
picks them again whenever the overlay's background switches between light and dark — otherwise a
theme switched from the theme sheet leaves dark icons on a dark bar until the sheet closes.

## Skeletons

Placeholders use `TuneScoutColors.skeleton`, not `surfaceSubtle`. The two look alike in the light
palette but are different roles: `surfaceSubtle` fills a real surface that sits under content, while
a skeleton has to read as a block on an empty screen. They diverge in the dark palette, where
`surfaceSubtle` at 10% white is fine behind a search field and invisible as a standalone block on
black.

`compose-shimmer`'s sweep is a `DstIn` mask, so it can only take light away: the block is brightest
where the band is and darkest at the band's edges. Its stock edge alpha of 0.25 would put the dark
skeleton at 5% white — a placeholder the user cannot see. `Shimmer.kt` therefore overrides the
theme, holding the edges at 0.55 and shortening the pause between sweeps. Raising the base colour
alone would not fix it: the animation would still fade the block to nothing twice a second.

## The dynamic color explanation

The (i) next to the dynamic colors toggle opens `DynamicColorInfoRoute` — a dialog, not a second
sheet, so the theme sheet it was opened from stays on screen behind it. It repeats the toggle, so
the user can act on the explanation without going back first; both switches read the same
preference, so they never disagree.

It lives in `feature/theme_selection` rather than a module of its own: nothing outside that feature
opens it, which is the line the module split is drawn on (see
[module-structure.md](module-structure.md)).

## The splash holds the first frame

`MainViewModel` exposes `MainUiState.Loading` until the stored theme arrives, and `MainActivity`
passes that to `setKeepOnScreenCondition`. The system splash therefore stays up over the DataStore
read, and the first frame the app composes is already in the chosen theme — there is no flash of
the wrong one. The flow is started `SharingStarted.Eagerly` for that reason: the splash condition
polls `uiState.value`, so nothing else is subscribed yet to move it off `Loading`.

## Screenshots

The generators in `tools/screenshots` pin `Theme.DARK`. They run under Robolectric, which reports
a light system theme, so the default `Theme.SYSTEM` would flip every README image.
