## Use Cases

### Pattern A — `fun interface` (preferred for injectable abstractions)

```kotlin
// domain/usecase/ObserveRecentlyPlayed.kt
fun interface ObserveRecentlyPlayed {
    operator fun invoke(): Flow<List<Song>>
}

// domain/usecase/impl/ObserveRecentlyPlayedUseCase.kt
class ObserveRecentlyPlayedUseCase(
    private val repository: RecentlyPlayedRepository,
) : ObserveRecentlyPlayed {
    override fun invoke(): Flow<List<Song>> = repository.observeRecentlyPlayed()
}
```

Koin binding:
```kotlin
factoryOf(::ObserveRecentlyPlayedUseCase).bind<ObserveRecentlyPlayed>()
```

### Pattern B — concrete class (for complex use cases that don't need an abstraction)

```kotlin
class AddToRecentlyPlayedUseCase(
    private val repository: RecentlyPlayedRepository,
) {
    suspend operator fun invoke(songId: Long) {
        repository.addToRecentlyPlayed(songId)
    }
}
```

### Use case aggregation (when a ViewModel needs many use cases)

```kotlin
// domain/usecase/PlayerUseCases.kt
data class PlayerUseCases(
    val observeSong: ObserveSong,
    val addToRecentlyPlayed: AddToRecentlyPlayedUseCase,
    val anotherUseCase: AnotherUseCase,
)
```

Koin:
```kotlin
factoryOf(::PlayerUseCases)
viewModelOf(::PlayerViewModel)
```

## Repository Pattern

```kotlin
// domain/repository/SongsRepository.kt
interface SongsRepository {
    fun observeSong(songId: Long): Flow<Song>
    suspend fun refreshSong(songId: Long): Result<Unit>
}

// data/repository/SongsRepositoryImpl.kt
class SongsRepositoryImpl(
    private val dao: SongDao,
    private val mapper: SongMapper,
) : SongsRepository {
    override fun observeSong(songId: Long): Flow<Song> =
        dao.observeSong(songId).map(mapper::map)

    override suspend fun refreshSong(songId: Long): Result<Unit> = suspendRunCatching {
        dao.upsert(mapper.toEntity(remote.fetchSong(songId)))
    }
}
```
