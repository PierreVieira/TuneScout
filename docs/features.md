# Features

Everything the app does, screen by screen. The [README](../README.md) has the short version and the
screenshots.

- **Search** the iTunes Search API as you type, with debounce and paginated results.
- **Play** a preview. A song tapped in search or in recently played plays on its own; a track
  tapped inside an album plays the album from there.
- **Queue** songs and whole albums by hand, either right after the current song ("Play next") or
  at the end of what you queued ("Add to queue"). What you add plays before the rest of the album
  and survives starting something else, the way Spotify's queue does. The queue screen reorders by
  drag, removes by tap, and jumps to any song. It opens as a sheet from the player or the mini
  player.
- **Pick up where you left off**: closing the app keeps the queue, the song and its position, and
  reopening restores all three, paused, from the local database.
- **Two tabs**: Home, which is search and recently played, and Your Library. The bar at the bottom
  becomes a navigation rail as soon as the window has width to spare, so a phone turned sideways
  gives the list its height back.
- **Library** of your own: liked songs, the playlists you create and the albums you like, as a list
  or a grid, with the choice remembered on the device. Chips narrow it to playlists or albums, and
  its own search screen keeps the items you opened under "Recent searches" and lets you drop them
  one by one.
- **Like a song** or **add it to a playlist** from the same options sheet every list opens —
  including every track of an album. Adding to a playlist can create one on the spot.
- **Like an album** from the heart in its top bar; queueing it moved into the overflow beside it,
  because liking is a state the bar should show and queueing is not.
- **Recently played** is the first tab. It is stored locally, so it works offline and survives
  restarts. Playing a song records it once, wherever playback was started from.
- **Offline**, the app is still the app: a preview that played once plays again from the media
  cache, search answers from the songs already on the device, artwork comes off disk, and a line
  above the list says where the rows come from. When the connection returns, the search runs again
  by itself. Songs the player cannot reach offline are faded, and tapping one says why instead of
  failing silently.
- **Player** with artwork, timeline, elapsed and remaining time, play/pause, previous, next,
  repeat and the queue. Dragging the timeline seeks on release without pausing.
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
  with a like button beside the transport controls.
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
  results; English and Brazilian Portuguese; content descriptions on every control.
