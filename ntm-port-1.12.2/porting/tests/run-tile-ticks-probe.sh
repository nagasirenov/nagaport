#!/usr/bin/env bash
set -euo pipefail
root=$(cd "$(dirname "$0")/../.." && pwd)
: "${MINECRAFT_CLASSPATH:?Set the real mapped Forge 1.12.2 compile/runtime classpath}"
out=$(mktemp -d)
trap 'rm -rf "$out"' EXIT
java_bin=${JAVA_HOME:+$JAVA_HOME/bin/}java
javac_bin=${JAVA_HOME:+$JAVA_HOME/bin/}javac
python3 "$root/porting/tests/check-tile-tick-migration.py"
"$javac_bin" -source 8 -target 8 -encoding UTF-8 -cp "$MINECRAFT_CLASSPATH" -d "$out" "$root/porting/tests/TileTickSchedulerTest.java"
(cd "$out" && "$java_bin" -cp "$out:$MINECRAFT_CLASSPATH" TileTickSchedulerTest)
