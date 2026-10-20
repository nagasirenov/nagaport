#!/usr/bin/env bash
set -euo pipefail
project_root=$(cd "$(dirname "$0")/../.." && pwd)
test_output=$(mktemp -d)
trap 'rm -rf "$test_output"' EXIT
if [[ -n "${JAVA_HOME:-}" ]]; then
    java_bin="$JAVA_HOME/bin/java"
    javac_bin="$JAVA_HOME/bin/javac"
else
    java_bin=java
    javac_bin=javac
fi
"$javac_bin" -source 8 -target 8 -d "$test_output" \
    "$project_root/src/main/java/com/hbm/lib/LegacyWeightedLoot.java" \
    "$project_root/porting/tests/LegacyWeightedLootTest.java"
"$java_bin" -cp "$test_output" LegacyWeightedLootTest
