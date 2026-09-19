#!/bin/bash

# Prints the Gradle tasks of one shard of the instrumented tests, for example:
#   ./gradlew $(./scripts/instrumented_shard.sh connected 0 2)
# The instrumented-tests workflow runs every shard on its own emulator. Modules are discovered by
# their src/androidTest directory and dealt out round-robin, so a new module joins a shard without
# anyone editing the workflow.
#
#   assemble   builds everything the shard installs, so it can run before the emulator exists
#   connected  runs the shard's tests on the connected device

set -euo pipefail

if [ "$#" -ne 3 ] || { [ "$1" != "assemble" ] && [ "$1" != "connected" ]; }; then
    echo "Usage: $0 <assemble|connected> <shard-index> <shard-count>" >&2
    exit 1
fi

MODE="$1"
SHARD_INDEX="$2"
SHARD_COUNT="$3"

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$PROJECT_ROOT"

MODULE_DIRS=$(find . -type d -path '*/src/androidTest' -not -path '*/build/*' \
    | sed -E 's#^\./##; s#/src/androidTest$##' \
    | sort \
    | awk -v shard="$SHARD_INDEX" -v count="$SHARD_COUNT" '(NR - 1) % count == shard')

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
    if grep -q "android.application" "$MODULE_DIR/build.gradle.kts"; then
        TASKS="$TASKS $MODULE:assembleDebug"
    fi
done

echo "${TASKS# }"
