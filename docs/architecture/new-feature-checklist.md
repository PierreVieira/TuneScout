## Checklist for a New Feature

- [ ] Create a new Gradle module under `feature/` with the standard layer structure, applying the `tunescout.android.feature` convention plugin
      — name the directory `snake_case` and the Kotlin package without the separator, see [Module names](module-structure.md#module-names)
- [ ] Add the new Gradle module to `settings.gradle.kts`
- [ ] Define `UiState`, `UiEvent`, `UiAction` in `presentation/model/`
- [ ] Define repository interface in `domain/repository/`
- [ ] Implement repository in `data/repository/`
- [ ] Define use cases in `domain/usecase/` (use `fun interface` + `impl/` pattern)
- [ ] Group use cases in a `FeatureUseCases` data class if there are more than 2
- [ ] Implement `FeatureViewModel` extending `ViewModel`, with `StateFlow<UiState>`, `SharedFlow<UiAction>` and a single `onEvent`
- [ ] Create `FeatureContent` composable with Loading/Loaded split
- [ ] Define a `@Serializable` route implementing `NavKey` in `core/navigation/.../route/` (one per file)
- [ ] Create `EntryProviderScope<NavKey>.feature()` extension (Root composable)
- [ ] Register the entry in `TuneScoutNavDisplay`'s `entryProvider` (`app`)
- [ ] Create a Koin module in `di/` and register it in `AppModules.kt` (`appModules`)
- [ ] Write unit tests for the ViewModel, repository and mappers (Given/When/Then) — see [docs/testing/README.md](../testing/README.md)
