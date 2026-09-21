# Accessibility

What the app does so it works with TalkBack, Switch Access, a large font and low vision — and the
conventions a new screen follows to keep it that way. Where a rule has a shared piece of code behind
it, use that piece instead of repeating the semantics by hand.

## Colour

Body text needs **4.5:1** against what it is drawn on, and an icon the user can act on needs
**3:1** (WCAG 1.4.3 and 1.4.11). The tokens closest to the line have a job each:

| Token | Use it for | Never for |
| --- | --- | --- |
| `textSecondary` | subtitles, descriptions, section labels | — |
| `textTertiary` | icons that do something but should not pull the eye: a row's action, the drag handle, an unselected tab | — |
| `elementMuted` | a control that is **disabled** | anything the user can tap |
| `elementSubtle` | dividers, tracks, disabled transport buttons | an icon that is the only thing saying a control is there |

`TuneScoutPalettesContrastTest` measures each text token against every surface it is drawn on, in
both palettes: `textSecondary`, `textTertiary` and the `accent` of a playing song's title on the
background, a sheet and a subtle surface over the background; `error` on a sheet, where it labels a
dialog's confirm button; `textPlaceholder` inside a field, on either. A new value that fails it is
too light, however good it looks on one of them. Under dynamic colour `textSecondary` is
`onSurfaceVariant`, which Material builds for text; `outline` is built for 3:1 and only feeds
`elementMuted`.

**Colour is never the only signal.** A state drawn with a tint or a fade also gets a shape and a
word: a song that cannot play is faded, carries the offline glyph and says "Unavailable offline"; a
mode that is on (`PlaybackModeIcon`) gets a dot under its icon, because repeating the queue and not
repeating draw the same glyph.

## Touch targets

Anything tappable is **48dp** in both directions. Shrink the icon, not the container:

```kotlin
// Correct — a 20dp glyph in a 48dp target
IconButton(onClick = onClick, modifier = Modifier.size(48.dp)) {
    Icon(imageVector = icon, contentDescription = label, modifier = Modifier.size(20.dp))
}

// Wrong — the target is the size of the glyph's frame
IconButton(onClick = onClick, modifier = Modifier.size(36.dp)) { ... }
```

When the target is larger than the control should look, draw the visible part inset from it, the way
`LibraryViewModeToggle` draws its track and indicator inside two 48dp segments.

## Heights follow the font

A row that holds text uses `heightIn(min = …)`, never `height(…)`: at a 200% font scale the text is
taller than the row was designed for, and a fixed height clips it.

A screen that fills the window and does not scroll has to decide what gives way. The player sizes
its artwork against the height its details need *at the font scale in force*, shrinks the cover down
to a floor, and scrolls past that, so the controls are never the part that is cut. Its controls
(`PlaybackControlsComponent`) need 360dp for one row; under that the play button and the gaps
shrink, and on a narrower window still the modes wrap under the transport — a target is never
squeezed to make a row fit.

## Motion

"Remove animations" sets the system's animator scale to zero, and Compose already shortens every
finite animation to nothing at that scale. What keeps going is motion with no end, so each of those
checks `rememberReduceMotion()` (`:ui:utils`) and falls back to a still frame (WCAG 2.2.2):

| Animation | With motion reduced |
| --- | --- |
| `Modifier.loopingMarquee()` | not applied; the text ends in an ellipsis, so callers set `overflow` |
| `NowPlayingBarsIcon` | the three bars at the heights of their first frame |
| `Modifier.shimmer()` | a still placeholder block |
| `VoicePulseIcon` | no idle breathing; the halos still follow the voice, which is feedback |

A new endless animation makes the same check.

## What a screen reader is told

- **Where the user is.** A full screen marks its root with `Modifier.screenPane(title)`
  (`:ui:utils`). Screens change with a fade, so nothing else announces the change. Sheets and
  dialogs are windows of their own and need nothing.
- **Headings.** Every title — the top bar's, a section's, a sheet's, an empty state's — carries
  `semantics { heading() }`, so the screen can be skimmed by heading. `TopBar`, `OptionsSheet`,
  `PromptCard` and `StateMessage` already do.
- **State belongs to the role, not to the label.** A control keeps one name and says its state
  through semantics, so the name does not change under the user's finger:
  - two states → `toggleable(role = Role.Switch)` (`ShuffleButton`, the dynamic colour rows). A
    `Switch` beside a label takes `onCheckedChange = null` and the **row** is the toggleable, or
    TalkBack reads "Switch, off" with no idea what it switches;
  - one of several → `selectable(role = Role.RadioButton)` inside a `selectableGroup()` (the theme
    cards, the library's list/grid toggle);
  - more than two states → a button with a `stateDescription` (repeat: "Off", "Whole queue",
    "This song").
- **What a tap does.** A clickable row passes `onClickLabel`, so TalkBack says "double tap to play"
  instead of "to activate". `SongRow` defaults to "Play".
- **Outcomes the user did not navigate to.** A result that appears on its own is a live region:
  `StateMessage(isAnnounced = true)` for a search that found nothing or failed, `NoticeBar` for the
  offline notice, an assertive one for a voice search that failed. A message a screen simply opens on
  is not announced — focus reaches it anyway. The voice search transcript is deliberately **not** a
  live region: the microphone is open, and TalkBack reading each word back would be heard as the
  next one.
- **Numbers the way they are said.** The seek bar's state is "12 seconds of 30 seconds" rather than
  the slider's "40 percent", and the clocks under it read "12 seconds played" rather than "0:12".
- **A field keeps its name.** `SearchField` is named after its placeholder through
  `contentDescription`, because the placeholder leaves with the first character typed. The
  placeholder and the leading icon are silent so the name is not read three times.
- **A gesture always has another way.** Swiping a song offers the same actions as
  `CustomAccessibilityAction`s (`SongSwipeActionsBox`), a queue row offers "Move up" and "Move
  down" — its drag handle is silent, since a drag is not something a screen reader can do — and
  removing a song from a playlist is an entry in its options sheet.
- **Nothing is said twice, and nothing empty is said at all.** A playlist's four-cover grid lets
  only its first tile speak, and `TopBar` draws no text node for a title that is still loading.
- **Bars do not hide under a screen reader.** `hidesBarsOnScroll()` does nothing while touch
  exploration is on (`rememberIsTouchExplorationEnabled()`): TalkBack scrolls the list to follow its
  focus, which would collapse the top bar and the navigation under a user who never swiped.

## Outside the app's own screens

- **Media controls.** The session offers like, shuffle and repeat as labelled `CommandButton`s
  (`MediaButtonSpecFactory`), so the notification, a car, a watch or a headset can switch them. The
  icon shows the state a mode is in and the label says what pressing does.
- **Widgets.** Glance has no disabled state to announce, so a skip the queue cannot honour loses its
  click and is described as unavailable. The cover beside the title is silent, a shortcut tile says
  "Play <song>" rather than a bare title, the buttons are 48dp targets and the two widgets have
  different names in the picker.

## Checking a change

Three checks run on every pull request, so most of the above cannot quietly regress:

- **The screenshot tests run the Accessibility Test Framework on every capture** — the checks
  behind Accessibility Scanner: touch target size, contrast, missing labels. An error fails the
  test. See [Screenshot tests](../testing/screenshot-tests.md#accessibility-checks).
- **The main screens are captured at a 2x font scale**, the largest the system setting reaches, so a
  clipped row is a diff in the pull request.
- **Android lint** runs with its accessibility checks as errors. See
  [Code quality](../code-quality.md#android-lint).

The instrumented screen tests assert on semantics — `assertIsOn()`, `hasStateDescription(…)` —
rather than on a label that happens to contain the state. For anything a test cannot say, run the
screen with TalkBack on and at the largest font size in the system settings.
