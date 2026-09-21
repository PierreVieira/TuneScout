#!/bin/bash

# Regenerates the screenshots the README shows and writes them to docs/screenshots/.
# Renders every screen under Robolectric through :tools:screenshots — no device, no emulator.
# See docs/screenshots.md.

set -e

# Get the directory where this script is located
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Find project root by looking for gradlew or build.gradle.kts
# Start from script directory and go up until we find it
CURRENT_DIR="$SCRIPT_DIR"
PROJECT_ROOT=""

while [ "$CURRENT_DIR" != "/" ]; do
    if [ -f "$CURRENT_DIR/gradlew" ] || [ -f "$CURRENT_DIR/build.gradle.kts" ]; then
        PROJECT_ROOT="$CURRENT_DIR"
        break
    fi
    CURRENT_DIR="$(dirname "$CURRENT_DIR")"
done

if [ -z "$PROJECT_ROOT" ]; then
    echo "❌ Could not find project root (looking for gradlew or build.gradle.kts)"
    exit 1
fi

cd "$PROJECT_ROOT"

if [ ! -f "./gradlew" ]; then
    echo "❌ gradlew not found in project root: $PROJECT_ROOT"
    exit 1
fi

echo "📸 Regenerating the README screenshots..."
echo "📁 Working directory: $PROJECT_ROOT"
echo ""

chmod +x ./gradlew
./gradlew updateReadmeScreenshots "$@"

echo ""
echo "✅ docs/screenshots/ is up to date. Review the diff before committing:"
echo "  git status docs/screenshots"
echo ""
echo "ℹ️  widget_*.png, lock_screen*.png and notification.png are manual captures and are never regenerated."
echo "✨ screenshots finished!"
