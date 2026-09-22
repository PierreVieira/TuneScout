# Continuous integration

Every check runs on GitHub Actions, on each pull request and on each push to `main`. The debug APK
of every run is uploaded as an artifact. Delivery is one workflow, run by hand: [`release`](#release)
signs the release APK and publishes it on a GitHub release. Nothing publishes to a store.

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
| `release` | Bumps the version, builds `:app:assembleRelease` signed with the release key, publishes a GitHub release with the APK, and opens the bump pull request | By hand, from `main` |

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

The emulator and its system image are installed in a step of their own, which retries up to three
times, before the emulator runner starts. Google's repository sometimes serves a corrupt archive
("Error on ZipFile unknown archive"), and the runner fails the job on the first one. When the
runner's turn comes, both packages are already up to date and it downloads nothing. If you change
`api-level`, `target` or `arch`, update the system image in that step to match.

`:tools:baseline_profile` is not in any shard: its tests are in `src/main`, as a `com.android.test` module's
are, and they are the macrobenchmarks, which run by hand on a device nothing else is using. See
[Performance](performance.md#reproducing).

### screenshot-tests

`verify` uploads `screenshot-differences` when it fails: the reference, the render and the diff
side by side. When a screen changed on purpose, label the pull request **`record-screenshots`**.
`record` then renders the references on Linux, commits them to the branch as `github-actions[bot]`
and removes the label, so the next push verifies again. The whole flow is in
[Screenshot tests](testing/screenshot-tests.md).

### release

The only workflow that runs by hand, and only from `main`: **Actions → release → Run workflow**, or

```bash
gh workflow run release -f bump=patch
```

`bump` is `patch`, `minor` or `major`, the semantic versioning parts, or `none`. The version lives
in `app/build.gradle.kts`, and [`scripts/bump_version.py`](../scripts/bump_version.py) is what
edits it: a bump sets the `versionName` and adds one to the `versionCode`, since Android only
installs an update whose code is higher than the one on the device. `none` releases the version
`main` already has, which is what the first release and a rebuild of an unreleased version want.
Both print the version they end on, so the script is the same check locally:

```bash
python3 scripts/bump_version.py none
```

The run, in order:

1. Refuses a version that is already released, as a tag `v<version>`. After a bump, that means the
   previous bump pull request is not merged, so `main` still has the old version and the bump lands
   on the same number. Merge it and run again.
2. Builds `:app:assembleRelease` with the bumped version, signed with the release key. A build
   failure stops here, with nothing pushed.
3. On a bump, commits the two version lines as `github-actions[bot]` on
   `chore/bump-version-<version>` and pushes the branch.
4. Creates the release `v<version>`, tagged on that commit, or on `main`'s head for `none`, with
   notes generated from the pull requests since the previous tag. It carries two files: the APK,
   `tunescout-<version>.apk`, and the R8 `mapping.txt` of that build, which is the only way to read
   a stack trace from it.
5. On a bump, opens the pull request `chore: bump the version to <version>` against `main`,
   assigned to whoever ran the workflow. It changes only the two version lines, on the commit the
   run just built and released. The pull request is opened with the workflow's own token, so no
   check runs on it. Squash-merge it before the next release: the tag stays on the branch's commit,
   which is what the APK was built from, and `main` gets the same two lines.

If step 5 fails, the branch and the release exist and the pull request is the only thing missing;
open it by hand with `gh pr create --head chore/bump-version-<version>`. The step needs the
repository setting **Settings → Actions → General → Allow GitHub Actions to create and approve pull
requests**, which is off by default:

```bash
gh api -X PUT repos/PierreVieira/TuneScout/actions/permissions/workflow -F can_approve_pull_request_reviews=true
```

#### The signing key

The release key is not in the repository. `app/build.gradle.kts` reads it from four environment
variables and the workflow fills them from secrets of the same names, apart from the keystore
itself, which is stored base64-encoded as `RELEASE_KEYSTORE_BASE64` and decoded to a file the
`RELEASE_KEYSTORE` variable points at. Without them, locally and in the `build` job, the release
APK is signed with the debug key instead: it installs, but a device with a published release on it
refuses it as an update, so the workflow checks the four secrets before it does anything else.

To create the key once, and keep the keystore and its passwords somewhere safe, since every future
release has to be signed with the same key to install over the previous one:

```bash
keytool -genkeypair -keystore tunescout-release.jks -alias tunescout -keyalg RSA -keysize 4096 -validity 10000
```

```bash
gh secret set RELEASE_KEYSTORE_BASE64 --body "$(base64 -i tunescout-release.jks)"
```

```bash
gh secret set RELEASE_KEYSTORE_PASSWORD
```

```bash
gh secret set RELEASE_KEY_ALIAS --body tunescout
```

```bash
gh secret set RELEASE_KEY_PASSWORD
```

The same variables sign a local build the way the workflow does:

```bash
RELEASE_KEYSTORE=/path/to/tunescout-release.jks RELEASE_KEYSTORE_PASSWORD=… RELEASE_KEY_ALIAS=tunescout RELEASE_KEY_PASSWORD=… ./gradlew :app:assembleRelease
```

## Concurrency

A new push to a pull request cancels that pull request's previous run in every workflow.

On `main` it depends on the workflow. `build-and-test`, `instrumented-tests` and `screenshot-tests`
put each commit in its own group, so every run on `main` finishes. A cancelled run saves no Gradle
cache. When merges came in quickly, `main`'s cache fell several commits behind, and the next pull
requests recompiled from scratch. The lighter workflows (`static-analysis`, `module-graph`,
`translations`) group by ref and do cancel on `main`, since they save nothing worth keeping.

`release` has a single group and never cancels: two runs at once would compute the same version
from the same `main`, so the second waits for the first and then stops on the tag it created.

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
  Gradle (`ktlint`, `module-graph`), and for `lint` and `release`, which compile what `build`
  compiles and read that job's entry rather than saving the same outputs twice. Their small entries would take up the budget. Also,
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
