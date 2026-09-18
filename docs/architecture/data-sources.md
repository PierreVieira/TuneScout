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

`core/database` owns the `RoomDatabase`, its DAOs and entities. DAOs expose `Flow<T>` for reactive
queries and `suspend fun` for writes; repositories map with `.map(mapper::map)`.

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

Search results are not cached in Room; only the songs the user opens are persisted by the screens that
show them.
