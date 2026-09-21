# Features

Everything the app does, screen by screen. The [README](../README.md) has the short version and the
screenshots.

- **Search** the iTunes Search API as you type, with debounce and paginated results.
- **Search by voice** from the microphone beside the field: a sheet listens, shows the words as
  they are understood with a halo that follows the voice, and searches as soon as you stop
  speaking. The microphone permission is asked first, a refusal is explained with a shortcut to
  the settings, and a device with no speech recognition never sees the button.
- **Play** a preview. A song tapped in search or in recently played plays on its own; a track
  tapped inside an album plays the album from there.
- **Queue** songs and whole albums by hand, either right after the current song ("Play next") or
  at the end of what you queued ("Add to queue"). What you add plays before the rest of the album
  and survives starting something else, the way Spotify's queue does. The queue screen reorders by
  drag, removes by tap, and jumps to any song. It opens as a sheet from the player or the mini
  player.
- **Shuffle and repeat**: shuffle plays the rest of the album in a random order, always behind the
  songs you queued by hand, and the queue screen lists that order. Turning it off puts the album
  back in order from the song playing. Repeat steps through off, the whole queue and the current
  song. Both are the player's own modes, so the notification and the lock screen agree with them.
- **Pick up where you left off**: closing the app keeps the queue, the song and its position, and
  reopening restores all three, paused, from the local database — shuffled or not, and with the
  repeat mode it had.
- **Two tabs**: Home, which is search and recently played, and Your Library. The bar at the bottom
  becomes a navigation rail as soon as the window has width to spare, so a phone turned sideways
  gives the list its height back. On a window wider still — a tablet, or most phones on their side —
  an album opens beside the tabs instead of over them, and the next one picked replaces it there.
- **Library** of your own: liked songs, the playlists you create and the albums you like, as a list
  or a grid, with the choice remembered on the device. Chips narrow it to playlists or albums, and
  its own search screen keeps the items you opened under "Recent searches" and lets you drop them
  one by one.
- **Like a song** or **add it to a playlist** from the same options sheet every list opens —
  including every track of an album. Adding to a playlist can create one on the spot.
- **Like an album** from the heart in its top bar; queueing it moved into the overflow beside it,
  because liking is a state the bar should show and queueing is not.
- **Play an album or a playlist** from the round accent button under its header, with shuffle
  beside it, the way Spotify lays them out. On an album the button starts it from the top — from
  any song with shuffle on — and turns into pause while that album plays. A playlist or your liked
  songs are played now, ahead of the queue, shuffled first while shuffle is on.
- **Reorder an album or a playlist** in place: "Reorder songs" in a track's options sheet or in the
  album's or playlist's own, or a long press on a row, which drags it at once. Each row shows a
  handle while the mode lasts, a check ends it, and every move is kept on the device — an album
  keeps its order through every refresh, and plays in it. A screen reader gets "Move up" and "Move
  down" on each row instead of the drag. The liked songs keep the order they were liked in.
- **Recently played** is the first tab. It is stored locally, so it works offline and survives
  restarts. Playing a song records it once, wherever playback was started from.
- **Offline**, the app is still the app: a preview that played once plays again from the media
  cache, search answers from the songs already on the device, artwork comes off disk, and a line
  above the list says where the rows come from. When the connection returns, the search runs again
  by itself. Songs the player cannot reach offline are faded and marked with an offline glyph, and
  tapping one says why instead of failing silently.
- **Player** with artwork, timeline, elapsed and remaining time, play/pause, previous, next,
  shuffle, repeat and the queue. Dragging the timeline seeks on release without pausing.
- **Mini player** above every screen while something is loaded, with its own play/pause and a tap
  to reopen the player. The artwork flies between the two.
- **Album** screen reached from the song options sheet. Fetched once through the lookup endpoint
  and cached, so it opens offline afterwards — and is not fetched again for an hour. A refresh that
  fails keeps the cached tracks on screen and says it could not update them.
- **Theme** picked from a sheet on the songs screen: light, dark, or whatever the system says.
  On Android 12+ the palette can follow the wallpaper instead, explained by a dialog behind the
  (i) next to the toggle. The choice is stored on the device with DataStore, and the splash holds
  until it is read, so the first frame is already in the chosen theme.
- **Media controls** in the notification shade and on the lock screen, backed by a media session,
  with like, shuffle and repeat buttons beside the transport controls wherever the surface has room
  for them — a car or a watch included.
- **Two home screen widgets**, built with Glance and resizable:
  - *Now playing* (4×1): the cover, title and artist, previous / play-pause / next, and the
    timeline with elapsed and total time.
  - *Now playing with shortcuts* (4×2): the same controls over a row of the five songs played
    last. Tapping a cover plays it, with the five as the queue.

  Tapping either widget opens the player on the current song, or the app when nothing has played.
  The buttons work from a cold start — they wait for the saved queue to be restored — and previous
  and next are dimmed when there is no song that way. With nothing to play, the widget says so.
- Loading, empty, error, offline and rate-limited states on every screen — the device's own
  connectivity decides which one, not the shape of the last failure; pull to refresh on search
  results; English, Brazilian Portuguese, Spanish and French.
- **Accessible** with TalkBack and Switch Access: every screen and heading is named, toggles say
  their state through their role, a search announces how it ended, the timeline is read as time
  rather than as a percentage and every swipe has an action that does the same. Targets are 48dp,
  text holds 4.5:1 in both palettes, the player still fits at a 200% font, and endless animations
  stop when the system is told to remove them. The conventions, and the checks that keep them, are
  in [Accessibility](architecture/accessibility.md).
