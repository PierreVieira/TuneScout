## Dependency Injection (Koin)

Every Gradle module has exactly one Koin module, in a single file under `di/`:

```kotlin
// di/PlayerModule.kt
val playerModule: Module = module {
    singleOf(::SongsRepositoryImpl).bind<SongsRepository>()
    factoryOf(::ObserveSongUseCase).bind<ObserveSong>()
    factoryOf(::AddToRecentlyPlayedUseCase)
    factoryOf(::PlayerUseCases)
    viewModelOf(::PlayerViewModel)
}
```

- `singleOf(::Impl).bind<Interface>()` — one instance for the whole app (repositories, the `Navigator`,
  the database, the HTTP client)
- `factoryOf(::UseCase)` — a new instance per injection (use cases, mappers)
- `viewModelOf(::ViewModel)` — a ViewModel scoped to its navigation entry; route arguments arrive through
  `parametersOf(route)` at the call site (see [navigation.md](navigation.md))

**Register the new module** in `app/src/main/kotlin/com/pierre/tunescout/di/AppModules.kt`:

```kotlin
val appModules: List<Module> = listOf(
    navigationModule,
    // ... existing modules
    playerModule,
)
```

`TuneScoutApplication` starts Koin with `appModules`; nothing else calls `startKoin` or `loadKoinModules`.

## Verifying the graph

`app/src/test/.../di/AppModulesTest.kt` merges `appModules` into a single module and runs `verify()`:
it walks each definition's constructor by reflection and fails when a parameter has no matching
definition. It is what guards the type-based resolution of `singleOf`/`factoryOf`/`viewModelOf`,
which would otherwise only break at runtime.

Two practical consequences when declaring a new dependency:

- **Declare a collaborator rather than building it inside the lambda.** `single<HttpClientEngine> { OkHttp.create() }`
  and `single<SongDao> { get<TuneScoutDatabase>().songDao() }` exist so that whoever consumes them is
  verifiable. An object created inside a lambda is invisible to the graph.
- **Parameters arriving through `parametersOf`** (the ViewModel routes) must be declared in the test's
  `injections`, as `definition<AlbumViewModel>(AlbumRoute::class)`.
