#!/bin/bash

# Runs one shard of the instrumented tests, with any Gradle arguments after the shard:
#   ./scripts/instrumented_shard.sh connected 0 3 --continue --max-workers=1
# The instrumented-tests workflow runs every shard on its own emulator. Modules are discovered by
# their src/androidTest directory, so a new module joins a shard without anyone editing the
# workflow.
#
# Shard 0 is the application's. Its end-to-end flows go through the Test Orchestrator, which clears
# the app's data and starts a new process for every test, so :app alone takes about as long on the
# device as every library module together, and no layout can make the job finish sooner than :app
# alone takes. The libraries are dealt round-robin over the other shards. With a single shard,
# everything runs in it.
#
#   assemble   builds everything the shard installs, so it can run before the emulator exists
#   connected  runs the shard's tests on the connected device
#
# The script hands Gradle the tasks itself rather than printing them: a shard with no module is an
# error, and a command substitution around a printed list would hide it from ./gradlew, which then
# runs `help` and passes without a test. Add -m to see the tasks without running them.

set -euo pipefail

usage() {
    echo "Usage: $0 <assemble|connected> <shard-index> <shard-count> [gradle arguments]" >&2
    exit 1
}

[ "$#" -ge 3 ] || usage
case "$1" in assemble | connected) ;; *) usage ;; esac
for NUMBER in "$2" "$3"; do
    case "$NUMBER" in *[!0-9]* | "") usage ;; esac
done
[ "$2" -lt "$3" ] || usage

MODE="$1"
SHARD_INDEX="$2"
SHARD_COUNT="$3"
shift 3

# The working directory stays the caller's, so a relative path among the Gradle arguments means
# what it would with ./gradlew. .claude/worktrees holds other checkouts of this repository, each
# with the same modules. A fixed locale, so a shard holds the same modules on the runner and on a
# developer's machine.
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
ALL_MODULE_DIRS=$(find "$PROJECT_ROOT" -type d -path '*/src/androidTest' -not -path '*/build/*' \
    -not -path "$PROJECT_ROOT/.claude/*" \
    | sed -E "s#^$PROJECT_ROOT/##; s#/src/androidTest\$##" \
    | LC_ALL=C sort)

APPLICATION_DIRS=""
LIBRARY_DIRS=""
while IFS= read -r MODULE_DIR; do
    if grep -q "android.application" "$PROJECT_ROOT/$MODULE_DIR/build.gradle.kts"; then
        APPLICATION_DIRS+="$MODULE_DIR"$'\n'
    else
        LIBRARY_DIRS+="$MODULE_DIR"$'\n'
    fi
done <<< "$ALL_MODULE_DIRS"

if [ "$SHARD_COUNT" -eq 1 ]; then
    MODULE_DIRS="$APPLICATION_DIRS$LIBRARY_DIRS"
elif [ "$SHARD_INDEX" -eq 0 ]; then
    MODULE_DIRS="$APPLICATION_DIRS"
else
    MODULE_DIRS=$(awk -v shard="$SHARD_INDEX" -v count="$SHARD_COUNT" \
        'NF && (NR - 1) % (count - 1) == shard - 1' <<< "$LIBRARY_DIRS")
fi

TASKS=""
for MODULE_DIR in $MODULE_DIRS; do
    MODULE=":${MODULE_DIR//\//:}"
    if [ "$MODE" = "connected" ]; then
        TASKS="$TASKS $MODULE:connectedDebugAndroidTest"
        continue
    fi
    TASKS="$TASKS $MODULE:assembleDebugAndroidTest"
    # A library's test APK carries the library itself. An application's tests run against the
    # app APK, which the test APK does not build.
    if grep -q "android.application" "$PROJECT_ROOT/$MODULE_DIR/build.gradle.kts"; then
        TASKS="$TASKS $MODULE:assembleDebug"
    fi
done

if [ -z "$TASKS" ]; then
    echo "Shard $SHARD_INDEX of $SHARD_COUNT has no module with src/androidTest" >&2
    exit 1
fi

# shellcheck disable=SC2086 # the tasks are meant to be split
exec "$PROJECT_ROOT/gradlew" $TASKS "$@"
