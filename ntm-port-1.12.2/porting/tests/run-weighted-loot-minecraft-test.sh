#!/usr/bin/env bash
set -euo pipefail
project_root=$(cd "$(dirname "$0")/../.." && pwd)
: "${MINECRAFT_CLASSPATH:?Set MINECRAFT_CLASSPATH to the real Forge 1.12.2 mapped compile/runtime classpath}"
test_output=$(mktemp -d)
trap 'rm -rf "$test_output"' EXIT
if [[ -n "${JAVA_HOME:-}" ]]; then
    java_bin="$JAVA_HOME/bin/java"
    javac_bin="$JAVA_HOME/bin/javac"
else
    java_bin=java
    javac_bin=javac
fi
"$javac_bin" -source 8 -target 8 -cp "$MINECRAFT_CLASSPATH" -d "$test_output" \
    "$project_root/src/main/java/com/hbm/lib/LegacyWeightedLoot.java" \
    "$project_root/src/main/java/com/hbm/lib/WeightedRandomChestContent.java" \
    "$project_root/src/main/java/com/hbm/handler/VanillaLootHandler.java" \
    "$project_root/porting/tests/WeightedLootMinecraftTest.java"
echo 'PASS: target javac of production loot helpers and native API harness'
"$java_bin" -cp "$test_output:$MINECRAFT_CLASSPATH" WeightedLootMinecraftTest
