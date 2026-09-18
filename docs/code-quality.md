# Code quality

The project uses ktlint for formatting and style, extended with the custom rules in
[`tools/ktlint-custom-rules`](../tools/ktlint-custom-rules) (ruleset id `tunescout-style`). What those
rules enforce, and the conventions behind them, is documented in [Code style](architecture/code-style.md).

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
