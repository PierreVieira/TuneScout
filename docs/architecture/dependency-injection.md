## Dependency Injection (Koin)

Every Gradle module has exactly one Koin module, in a single file under `di/`:

```kotlin
// di/PlayerModule.kt
val playerModule: Module = module {
    singleOf(::SongsRepositoryImpl) { bind<SongsRepository>() }
    factoryOf(::ObserveSongUseCase) { bind<ObserveSong>() }
    factoryOf(::AddToRecentlyPlayedUseCase)
    factoryOf(::PlayerUseCases)
    viewModelOf(::PlayerViewModel)
}
```

- `singleOf(::Impl) { bind<Interface>() }` — one instance for the whole app (repositories, the `Navigator`,
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
