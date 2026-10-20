#!/usr/bin/env bash
set -euo pipefail
ROOT=$(git rev-parse --show-toplevel)
HERE="$ROOT/ntm-port-1.12.2/porting/tests/native-inventory-lifecycle"
: "${JAVA_HOME:?Set JAVA_HOME to the existing Java 8 toolchain}"
: "${NATIVE_CLASSPATH_FILE:?Set NATIVE_CLASSPATH_FILE to the cached native classpath file}"
OUT=${1:-$(mktemp -d)}
mkdir -p "$OUT/classes"
python3 "$HERE/verify.py" "$ROOT" "$OUT" | tee "$OUT/source-proof.log"
CP=$(cat "$NATIVE_CLASSPATH_FILE")
SRC="$ROOT/ntm-port-1.12.2/src/main/java"
"$JAVA_HOME/bin/javac" -source 8 -target 8 -cp "$CP" -d "$OUT/classes" "$SRC/com/hbm/lib/RefStrings.java" "$SRC/com/hbm/main/ModSoundEvents.java" "$SRC/com/hbm/sound/WorldSoundEffects.java" "$OUT/LifecycleFixtures.java" "$HERE/LifecycleNativeTest.java" 2>&1 | tee "$OUT/compile.log"
"$JAVA_HOME/bin/java" -cp "$OUT/classes:$CP" LifecycleNativeTest 2>&1 | tee "$OUT/native-test.log"
"$JAVA_HOME/bin/javap" -classpath "$CP" net.minecraft.inventory.IInventory net.minecraft.world.IWorldNameable > "$OUT/native-contract.txt"
