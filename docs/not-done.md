# Not done, on purpose or for lack of time

What the app leaves out, and why. Each line was checked against the code; the reasoning behind the
larger ones is in [Decisions and trade-offs](decisions.md).

## Design

- The design uses the Articulat CF typeface, which is commercial; the app ships the system
  sans-serif with the same sizes and weights.
- The splash window Android draws before the app's first frame only accepts a flat colour, so it
  shows the note over a dark teal close to the gradient's average; the designed gradient only
  appears as that splash leaves, fading in before it dissolves into the home screen. The splash is
  always dark, whatever the theme: its window is declared in resources, before the preference can
  be read.
- The light palette is derived from the Figma dark one rather than designed: the file only
  specifies dark.
- A wide window opens an album beside the tabs, and nothing else: a playlist, the liked songs and
  the player still cover them, and the player does not put its queue beside it yet.

## Search and playback

- The 200-item cap of the API is the end of every search; there is no "load more" beyond it.
- The queue is built from a playlist, or the liked songs, as they were when it started: a song added
  to them, or taken out, afterwards does not reach a queue already playing, and a renamed playlist
  keeps its old name in the queue until it is played again.
- Repeating the whole queue starts it over in the same shuffled order instead of shuffling again.
- The notification and the lock screen show the shuffle and repeat modes the player is in, but have
  no buttons of their own to change them.

## Library

- A playlist holds a song once: adding it again leaves it where it already is.
- The songs in a playlist cannot be reordered.
- A liked album is the album screen, not a screen of its own: the library row opens the same album
  it would from search.

## Widgets

- The widgets follow the system's night mode, not the theme picked in the app, and they do not use
  dynamic colour: a widget is drawn by the launcher, which only knows a day and a night colour.
- The widget picker shows a preview rendered from the real widget only on Android 15+. Below that it
  shows a static XML layout drawn with the same palette, with placeholder text instead of your song.

## Your data

- Playlists, liked songs, liked albums and the theme stay on the device. There is no account, so
  there is nothing to sync them to.
- Backup is off, both to the cloud and between devices, so reinstalling the app or moving to a new
  phone starts the library from empty.
