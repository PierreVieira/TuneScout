## Data Sources

### Remote (iTunes Search API over Ktor)

The network layer is the `ITunesApi` interface in `core/network`, implemented with a Ktor client
(OkHttp engine, `ContentNegotiation` with kotlinx.serialization JSON). Only the interface is `public`:
the DTOs (`SongDto`, `SearchResponseDto`, ...) and the Ktor implementation are `internal` to
`core/network`, and every response is mapped once, at the module boundary, to the `core/model` types
(`Song`, `Album`). Nothing outside `core/network` sees a DTO or a Ktor type, so the HTTP stack can be
swapped without touching a feature.

```kotlin
interface ITunesApi {
    suspend fun searchSongs(term: String, limit: Int): List<Song>
    suspend fun fetchAlbum(albumId: Long): Album
}
```

Suspend calls that can fail are wrapped at the repository boundary with `suspendRunCatching` and
returned as `Result<T>` — see [coroutine-error-handling.md](coroutine-error-handling.md).

#### DTOs

A `*Dto` mirrors the wire format and nothing else:

```kotlin
@Serializable
internal data class ResultDto(
    @SerialName("trackId") val trackId: Long?,
    @SerialName("trackName") val trackName: String?,
    @SerialName("artworkUrl100") val artworkUrl100: String?,
)
```

- **Every field declares its JSON key with `@SerialName`**, even when it equals the property name.
  Renaming the Kotlin property can then never change the contract, and the key is greppable.
- **No default values.** What the server may leave out is a nullable type, and the client's `Json` is
  configured with `explicitNulls = false` (in `HttpClientFactory`), which reads an absent key as `null`.
  A default would hide which fields the API really guarantees; the mapper decides what a missing value
  means (`toSongOrNull()` drops a result with no `trackId`, and falls back to `0` for `trackNumber`).

Both are enforced by the custom ktlint rule `tunescout-style:dto-serial-name`, on every class whose name
ends in `Dto`.

#### Artwork sizes

The [search results](https://developer.apple.com/library/archive/documentation/AudioVideo/Conceptual/iTuneSearchAPI/UnderstandingSearchResults.html)
only carry `artworkUrl60` and `artworkUrl100`, both far too small for the album header or the player —
stretched to 264dp, the 100×100 thumbnail is visibly pixelated. The size is a segment of the file name
(`.../source/100x100bb.jpg`), and Apple's image host renders whatever size that segment asks for, so
`core/model` wraps the url the API returned in an `Artwork` value class that rewrites that segment:

| Property | Pixels | Used by |
|---|---|---|
| `thumbnailUrl` | 200×200 | song rows (search results, recently played, album track list) |
| `mediumUrl` | 600×600 | album header, playback notification and lock-screen controls |
| `largeUrl` | 1000×1000 | the player screen artwork |

`artworkUrl100` stays the source url — it is what the mappers store in Room and what `sourceUrl`
returns — and a url whose file name carries no size is passed through untouched.

### Local (Room 3)

`core/database/api` declares the local data source interfaces a repository injects;
`core/database/impl` owns the `RoomDatabase`, its DAOs and entities. DAOs expose `Flow<T>` for
reactive queries and `suspend fun` for writes; repositories map with `.map(mapper::map)`.

Entities are normalized:

- No JSON blobs. A song's fields are columns; a relationship is a second table.
- Every relationship declares a `@ForeignKey` and an `@Index` on the referencing column.
- Ordering is an explicit column (`position`, `playedAt`), never the insertion order or the primary key.

```kotlin
@Entity(
    tableName = "recently_played",
    foreignKeys = [
        ForeignKey(
            entity = SongEntity::class,
            parentColumns = ["id"],
            childColumns = ["songId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("songId")],
)
data class RecentlyPlayedEntity(
    @PrimaryKey val songId: Long,
    val playedAt: Long,
)
```

### Repositories (offline-first)

Repositories live in the feature that owns the screen (`feature/<name>/data/repository/`), except the
recently-played repository, which is shared and lives in core. They combine the two sources:

- **Cache then network.** Observe Room first so the screen renders immediately, then refresh from
  `ITunesApi` and write the result into Room; the observed `Flow` emits the update.
- **Recently played comes from Room only.** The list is local state (`RecentlyPlayedEntity`), never
  refetched.
- **A refresh that fails does not clear the screen.** The cached rows stay, and the state carries a
  flag (`AlbumUiState.Loaded.isStale`) the screen turns into one line above them.
- **A cached album is not refreshed while it is fresh.** `AlbumLocalDataSource.isFresherThan` reads
  the row's `cachedAt`, and `refreshAlbum` returns early within the window `albumModule` configures
  (one hour). An album's track list does not change, so the call would return the rows already on
  screen.

```kotlin
class AlbumRepositoryImpl(
    private val api: ITunesApi,
    private val dao: AlbumDao,
    private val mapper: AlbumMapper,
) : AlbumRepository {
    override fun observeAlbum(albumId: Long): Flow<Album?> =
        dao.observeAlbumWithSongs(albumId).map { it?.let(mapper::map) }

    override suspend fun refreshAlbum(albumId: Long): Result<Unit> = suspendRunCatching {
        dao.upsertAlbumWithSongs(mapper.toEntities(api.fetchAlbum(albumId)))
    }
}
```

### Search (Paging 3)

Search results are served through a `PagingSource<Int, Song>` exposed as a `Flow<PagingData<Song>>` and
collected with `collectAsLazyPagingItems()`.

The iTunes Search API ignores `offset` and caps `limit` at 200, so the paging key is not a page number
or an offset but the **growing `limit`**: page *n* requests `limit = pageSize * n`, drops the
`pageSize * (n - 1)` items already delivered and returns the tail. `nextKey` is `null` once the response
is shorter than the requested `limit` or the cap is reached.

```kotlin
class SearchSongsPagingSource(
    private val api: ITunesApi,
    private val term: String,
) : PagingSource<Int, Song>() {
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Song> {
        val limit = params.key ?: params.loadSize
        return suspendRunCatching { api.searchSongs(term, limit.coerceAtMost(MAX_LIMIT)) }
            .fold(
                onSuccess = { songs ->
                    val alreadyDelivered = limit - params.loadSize
                    LoadResult.Page(
                        data = songs.drop(alreadyDelivered),
                        prevKey = null,
                        nextKey = (limit + params.loadSize).takeIf { songs.size == limit && limit < MAX_LIMIT },
                    )
                },
                onFailure = { LoadResult.Error(it) },
            )
    }

    override fun getRefreshKey(state: PagingState<Int, Song>): Int? = null
}
```

#### When the API cannot be reached

Every result the search delivers is written to `songs`, so `SearchSongsPagingSource` can answer the
**first page** from Room when the call fails with `RemoteException.Unavailable`
(`SongLocalDataSource.findByTerm` matches title, artist and album, most recently cached first). The
fallback ends the list there — the cache is a single page — and a throttled or unexpected response is
still reported as an error, because the catalog is reachable and the user should know why it refused.

The screen restarts the search when `NetworkMonitor` reports the connection is back, so those cached
rows are replaced by the catalog's own without a pull to refresh.

#### What the cache keeps

`songs` would otherwise grow with every query ever typed, so each save trims it to the most recently
cached `MAX_CACHED_SONGS` (500) **orphans** — rows no other table points at. A song in the history, in
a playlist, liked, in the saved queue, or belonging to a cached album is never a candidate: those are
exactly what the app can still show with no connection.

### Connectivity

`NetworkMonitor` (`core/network`) exposes `observeIsOnline(): Flow<Boolean>` over
`ConnectivityManager.registerDefaultNetworkCallback`, with the state at subscription read from the
active network's `NET_CAPABILITY_VALIDATED`. A screen uses it to say where its rows come from, to pick
the right wording for a failure, and to retry by itself — never to decide whether to make a call.

### What is cached where

| Cache | Where | Size | Why |
|---|---|---|---|
| Songs, albums, the history, the library | Room (`tunescout.db`) | 500 orphan songs | The screens open with no connection |
| Previews | `SimpleCache` in `cacheDir/media_cache` | 128 MB, least recently used | A song played once plays again offline |
| Artwork | Coil's disk cache in `cacheDir/image_cache` | 2% of the free space, never under 64 MB | The lists look the same offline |
| API responses | Ktor `HttpCache` over `FileStorage` in `cacheDir/http_cache` | unbounded, honours `Cache-Control` | A repeated search survives a restart |
