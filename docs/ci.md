# Continuous integration

Every check runs on GitHub Actions, on each pull request and on each push to `main`. There is no
delivery step: no workflow signs, publishes or distributes the app. The debug APK of every run is
uploaded as an artifact, and that is as far as it goes.

The workflows live in [`.github/workflows`](../.github/workflows), and the two composite actions
they share in [`.github/actions`](../.github/actions).

## The workflows

| Workflow | What it runs | When |
|---|---|---|
| `build-and-test` | `unit-tests`: the JVM tests and the coverage rules. `build`: `:app:assembleDebug` and `:app:assembleRelease`. | Any change outside Markdown, `docs/` and `.claude/` |
| `instrumented-tests` | Compose screen tests and end-to-end flows on an API 35 emulator, split into two shards | Same as `build-and-test` |
| `screenshot-tests` | `verify` compares every screen with its committed reference; `record` renders new ones | Same as `build-and-test`, and when a label is added |
| `static-analysis` | `ktlint`: the project's custom rules, through `scripts/ktlint.sh`. `lint`: Android lint over every module, `:app:lintDebug` | `.kt`/`.kts` files, resources and manifests, `.editorconfig`, the version catalog, the ktlint script or action |
| `module-graph` | `assertModuleGraph` | Build scripts, `build-logic`, the version catalog or the Gradle wrapper |
| `translations` | `scripts/check_translations.py`: every string in `values` has a `values-pt-rBR`, a `values-es`, a `values-fr`, a `values-de` and a `values-it` twin | A `strings.xml` or the script |
| `cleanup-pr-caches` | Cancels a closed pull request's pending runs and deletes its Gradle caches | A pull request is closed |

Each check can be run locally with the same command:

```bash
./scripts/ktlint.sh
```

```bash
./gradlew :app:lintDebug
```

```bash
./gradlew testDebugUnitTest test :koverVerifyCi :verifyNewFilesCoverage
```

```bash
./gradlew connectedDebugAndroidTest
```

```bash
./gradlew assertModuleGraph
```

```bash
python3 scripts/check_translations.py
```

Screenshots are the exception: they are verified and recorded only on CI, because the same screen
renders a few pixels differently on macOS.

### build-and-test

`unit-tests` runs the JVM tests of every module and both coverage rules in one Gradle call with
`--continue`, so a coverage failure doesn't hide a test failure. `verifyNewFilesCoverage` compares
the pull request against the tip of its base branch, so the job fetches that branch first; the
depth-1 checkout does not include it. `:tools:screenshot_tests` is skipped here because
`screenshot-tests` renders the same screens and compares them. The coverage report is always
uploaded as `coverage-report`, and the test reports as `unit-test-reports` when something fails.
The rules themselves are in [Code quality](code-quality.md#test-coverage).

`build` assembles `:app` only. The release variant is the only one R8 runs on, so a missing keep
rule fails here instead of on a device. The root `assemble*` tasks would also build an AAR per
library module that nothing uses. The debug APK is uploaded as `tunescout-debug`.

### instrumented-tests

The job compiles the shard's APKs before it boots the emulator. Gradle then gets every core, and a
compilation error fails the job before it pays for the boot. Tests run one module at a time
(`--max-workers=1`) on a single emulator, so the log shows which module is stuck.

[`scripts/instrumented_shard.sh`](../scripts/instrumented_shard.sh) finds every module with a
`src/androidTest` directory and deals them round-robin between the shards. A new module joins a
shard without anyone editing the workflow. To add a shard, raise `SHARD_COUNT` and add an index to
`matrix.shard`; the two must stay equal. The test step has its own 25-minute timeout, well under the
job's 45, so a run that hangs still uploads `instrumented-test-reports-shard-<n>`.

The AVD is not cached. Measured, it saved about 20 seconds, and its 1.3 GB entry pushed the Gradle
caches out of the budget.

`:tools:baselineprofile` is not in any shard: its tests are in `src/main`, as a `com.android.test` module's
are, and they are the macrobenchmarks, which run by hand on a device nothing else is using. See
[Performance](performance.md#reproducing).

### screenshot-tests

`verify` uploads `screenshot-differences` when it fails: the reference, the render and the diff
side by side. When a screen changed on purpose, label the pull request **`record-screenshots`**.
`record` then renders the references on Linux, commits them to the branch as `github-actions[bot]`
and removes the label, so the next push verifies again. The whole flow is in
[Screenshot tests](testing/screenshot-tests.md).

## Concurrency

A new push to a pull request cancels that pull request's previous run in every workflow.

On `main` it depends on the workflow. `build-and-test`, `instrumented-tests` and `screenshot-tests`
put each commit in its own group, so every run on `main` finishes. A cancelled run saves no Gradle
cache. When merges came in quickly, `main`'s cache fell several commits behind, and the next pull
requests recompiled from scratch. The lighter workflows (`static-analysis`, `module-graph`,
`translations`) group by ref and do cancel on `main`, since they save nothing worth keeping.

## Gradle cache

[`.github/actions/gradle`](../.github/actions/gradle/action.yml) sets up JDK 21 and
`gradle/actions/setup-gradle`. All Gradle jobs share it, and it decides who may write to the
repository's 10 GB cache budget:

- **`main`** always writes. Every pull request restores from those entries.
- **`GRADLE_CACHE_WRITE_ON_PULL_REQUESTS=true`** lets a job write on a pull request too, scoped to
  that pull request's ref, so the next push compiles only what changed. Only the jobs on the
  critical path set it: `build` and the two `instrumented-tests` shards. `unit-tests` stays
  read-only, since even a cold run of it finishes before those.
- **`GRADLE_CACHE_READ_ONLY=true`** never writes, even on `main`. It is for jobs that barely use
  Gradle (`ktlint`, `module-graph`), and for `lint`, which compiles what `build` compiles and reads
  that job's entry rather than saving the same outputs twice. Their small entries would take up the budget. Also,
  setup-gradle restores the most recent entry of any job, so a job with no entry of its own could
  pick up one of theirs instead of a useful one.

On a pull request, every job except the read-only ones restores only its own cache entry. It
doesn't fall back to whatever entry is newest. By default, from the second push on, the newest
entry would belong to another job of the same pull request: a read-only job restored a writer's
entry and compiled everything that writer never builds (`unit-tests` took 4m09s instead of 52s).
A job that never runs on `main` has no entry of its own. It uses another job's entry by setting
`GRADLE_BUILD_ACTION_CACHE_KEY_JOB` to that job's id: `record` uses the `verify` entry, since both
compile and render the same things. On `main`, jobs still fall back to the newest entry, so a new
job's first run starts from another job's cache instead of from nothing.

Pull request entries are never cleaned up while the pull request is open, because each push would
save a new entry of hundreds of MB. When it closes, `cleanup-pr-caches` cancels its runs that are
still going and waits for them, since a cancelled job still saves its cache in the post step. Then
it deletes the cache entries of every closed pull request. That includes older ones whose runs
finished after that wait.

The `GRADLE_ENCRYPTION_KEY` secret encrypts the configuration cache. Without it the configuration
cache is dropped between runs and every job configures the build from scratch.

The ktlint action caches its own things: the CLI, keyed by version, and the custom ruleset jar,
keyed by its sources. It sets up Gradle only when the jar has to be rebuilt.

## Adding a workflow

- Scope it with `paths` or `paths-ignore`, so a docs-only change doesn't run a build.
- Use the `concurrency` block of the workflows above: per-commit groups on `main` if it writes a
  Gradle cache, per-ref otherwise.
- Set up Gradle through `./.github/actions/gradle`, pass `secrets.GRADLE_ENCRYPTION_KEY`, and pick
  one of the two cache switches above if the job is not a default reader.
- Keep `permissions` at `contents: read` unless the job has to push or edit the pull request.
- Wrap anything beyond a single Gradle call in a script under `scripts/`, so it runs the same way
  locally.
