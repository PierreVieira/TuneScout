# Code quality

The project uses ktlint for formatting and style, extended with the custom rules in
[`tools/ktlint_custom_rules`](../tools/ktlint_custom_rules) (ruleset id `tunescout-style`). What those
rules enforce, and the conventions behind them, is documented in [Code style](architecture/code-style.md).
Not every convention there is a rule: some need type resolution, which ktlint does not have, and stay
review conventions.

## Running it

CI runs the ktlint CLI directly, and `scripts/ktlint.sh` runs exactly the same check locally:

```bash
./scripts/ktlint.sh            # check (what CI runs)
```

```bash
./scripts/ktlint.sh --format   # autocorrect what can be autocorrected
```

The script downloads the ktlint CLI once (cached in `~/.cache/ktlint`, version read from
`gradle/libs.versions.toml`) and rebuilds the custom ruleset jar only when its sources change.

`./gradlew ktlintCheck` still works, but it is much slower: it drags KSP and Kotlin compilation into
the task graph to check files that are already on disk.

## In CI

The `static-analysis` workflow runs the same script on every pull request — and on every push to
`main` — that touches a `.kt` or `.kts` file, the version catalog, the ktlint script or
`.editorconfig`.

## Module graph

The dependencies between Gradle modules are checked by
[modules-graph-assert](https://github.com/jraska/modules-graph-assert), configured in the
`moduleGraphAssert` block of the root `build.gradle.kts`. It only looks at production source sets
(the `api` and `implementation` configurations, so test-only dependencies such as `:core:testing` are
ignored) and enforces the layering: `:app` is the composition root and the only module allowed to
depend on `:feature:*`. Features never depend on each other, `:core:*` never depends on a feature, and
nothing depends on `:app`. Features talk through routes in `:core:navigation` and shared state in
`:core:*`.

```bash
./gradlew assertModuleGraph                  # run every check (what CI runs)
```

The height of the graph (its longest dependency chain) is not enforced, since part of it is the
intentional composition root. Keep an eye on it instead, as a deeper graph compiles less in
parallel:

```bash
./gradlew generateModulesGraphStatistics     # module count, edge count, height, longest path
```

```bash
./gradlew generateModulesGraphvizText -Pmodules.graph.of.module=:feature:player
```

A new dependency that breaks a rule means the code is in the wrong module, not that the rule needs
an exception: move the shared piece down to a `:core:*` module instead.

The `module-graph` workflow runs `assertModuleGraph` on every pull request — and on every push to
`main` — that touches a Gradle build script, `build-logic`, the version catalog or the Gradle wrapper:
the only files that can change the module graph. It is a separate workflow so that Kotlin-only changes
skip the full Gradle configuration it needs.

## Test coverage

[Kover](https://github.com/Kotlin/kotlinx-kover) measures the line coverage of the JVM unit tests and
two rules gate it, both at **80%**:

- **The merged report.** Every measured module is merged into one report at the root and the total
  has to stay at or above the bar.
- **Each new file.** The total is an average, so a new file with no tests barely moves it.
  `verifyNewFilesCoverage` lists the `.kt` files added since the base branch
  (`git diff --diff-filter=A`) and holds each one to the same bar on its own. Files that are not in
  the report are skipped: the ones the filters below exclude, and the ones with nothing to execute
  (interfaces, models without logic).

```bash
./gradlew :koverVerifyCi :verifyNewFilesCoverage   # both rules (what CI runs)
```

```bash
./gradlew :koverHtmlReportCi                       # build/reports/kover/htmlCi/index.html
```

`verifyNewFilesCoverage` compares against `origin/main`; pass `-Pcoverage.baseRef=<ref>` for another
base. It reads the working tree, so a new file only needs `git add` to be seen, not a commit.

The colon matters: without it Gradle also runs the task of the same name in every module. `Ci` is a
Kover variant that holds `debug` for Android modules and `jvm` for the plain Kotlin ones, so the report
runs `testDebugUnitTest` once instead of once per Android build variant.

### What is measured

Only what a JVM unit test can reach. Composables, and the end-to-end flows, are covered on a device by
the `androidTest` suites, which do not feed this report. Left out:

| Excluded | How | Why |
| --- | --- | --- |
| `@Composable` and `@Preview` functions | annotation filter | covered by the instrumented tests |
| `*.presentation.content`, `*.presentation.component` | package filter | composable-only packages; the annotation filter leaves their top-level `Dp` constants behind |
| `*.presentation.navigation`, `*.di` | package filter | navigation entries and Koin modules: wiring, no behaviour of its own |
| `*_Impl`, `$$serializer`, `ComposableSingletons` | class filter | generated by Room, kotlinx.serialization and the Compose compiler |
| `:ui:*`, `:core:testing`, `:tools:*` | Kover is not applied (`configureCoverage` in `build-logic`) | composables and theme constants, test helpers, build tooling |

The filters and both rules live in the root `build.gradle.kts`; the modules only apply the plugin,
through their convention plugin. A new module is measured as soon as it uses one. Excluding more code
is a change to that table and to the filters, reviewed like any other: the default for new code is to
be measured and tested.

Branch coverage is reported but not gated: `@Serializable` classes carry generated branches
(`write$Self`, the synthetic constructor) that no test of ours should chase.

### In CI

The `unit-tests` job of the `build-and-test` workflow runs both rules next to the tests and uploads
the HTML report as the `coverage-report` artifact. On a pull request it first fetches the tip of the
base branch, which the depth-1 checkout does not bring, so the new-files rule has something to diff
against.
