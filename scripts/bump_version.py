#!/usr/bin/env python3
"""Bumps the app's version in app/build.gradle.kts, or reads it.

Usage: scripts/bump_version.py {none,patch,minor,major}

The versionName follows semantic versioning, and the versionCode goes up by one on every bump,
since Android installs an update only when it is higher than the one on the device. `none` leaves
both as they are. Either way the resulting version is printed as `version=` and `version_code=`
lines, which the release workflow appends to its step outputs. See docs/ci.md#release.
"""

import re
import sys
from pathlib import Path

BUILD_FILE = Path(__file__).resolve().parent.parent / "app" / "build.gradle.kts"
VERSION_CODE = re.compile(r'^(\s*versionCode = )(\d+)$', re.MULTILINE)
VERSION_NAME = re.compile(r'^(\s*versionName = ")(\d+)\.(\d+)\.(\d+)(")$', re.MULTILINE)
BUMPS = ("none", "patch", "minor", "major")


def main() -> int:
    if len(sys.argv) != 2 or sys.argv[1] not in BUMPS:
        print(f"usage: {sys.argv[0]} {{{','.join(BUMPS)}}}", file=sys.stderr)
        return 2
    bump = sys.argv[1]

    text = BUILD_FILE.read_text()
    code_match = VERSION_CODE.search(text)
    name_match = VERSION_NAME.search(text)
    if code_match is None or name_match is None:
        print(f"{BUILD_FILE}: versionCode or versionName not found", file=sys.stderr)
        return 1

    code = int(code_match.group(2))
    major, minor, patch = (int(name_match.group(i)) for i in (2, 3, 4))
    if bump == "major":
        major, minor, patch = major + 1, 0, 0
    elif bump == "minor":
        minor, patch = minor + 1, 0
    elif bump == "patch":
        patch += 1
    if bump != "none":
        code += 1
        text = VERSION_CODE.sub(rf"\g<1>{code}", text, count=1)
        text = VERSION_NAME.sub(rf"\g<1>{major}.{minor}.{patch}\g<5>", text, count=1)
        BUILD_FILE.write_text(text)

    print(f"version={major}.{minor}.{patch}")
    print(f"version_code={code}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
