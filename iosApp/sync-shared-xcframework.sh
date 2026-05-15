#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

./gradlew :shared:assembleSharedReleaseXCFramework

echo "XCFramework is available at:"
echo "  shared/build/XCFrameworks/release/Shared.xcframework"
