#!/usr/bin/env bash
set -euo pipefail
HERE=$(cd "$(dirname "$0")" && pwd)
ROOT=$(cd "$HERE/../../.." && pwd)
: "${JAVA_HOME:?Java 8 required}"
: "${MINECRAFT_CLASSPATH:?Actual mapped Forge 1.12.2 classpath required}"
OUT=$(mktemp -d)
trap 'rm -rf "$OUT"' EXIT
python3 "$HERE/verify_and_extract.py" "$OUT/ExtractedMachineLoops.java"
"$JAVA_HOME/bin/javac" -source 8 -target 8 -sourcepath '' -cp "$MINECRAFT_CLASSPATH" -d "$OUT" "$ROOT/src/main/java/com/hbm/util/NullableInventoryStackNbt.java" "$OUT/ExtractedMachineLoops.java" "$HERE/NativeMachineLoopsTest.java"
"$JAVA_HOME/bin/java" -cp "$OUT:$MINECRAFT_CLASSPATH" NativeMachineLoopsTest
