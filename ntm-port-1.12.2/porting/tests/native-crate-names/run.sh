#!/usr/bin/env bash
set -euo pipefail
ROOT=$(git rev-parse --show-toplevel)
HERE="$ROOT/ntm-port-1.12.2/porting/tests/native-crate-names"
: "${JAVA_HOME:?Use the existing Java 8 toolchain}"
: "${NATIVE_CLASSPATH_FILE:?Use the existing native classpath file}"
OUT=${1:-$(mktemp -d)}
mkdir -p "$OUT/classes"
python3 "$HERE/verify.py" "$ROOT" "$OUT" | tee "$OUT/source-proof.log"
CP=$(cat "$NATIVE_CLASSPATH_FILE")
"$JAVA_HOME/bin/javac" -source 8 -target 8 -cp "$CP" -d "$OUT/classes" "$OUT/CrateNameFixtures.java" "$HERE/CrateNamesNativeTest.java" 2>&1 | tee "$OUT/compile.log"
"$JAVA_HOME/bin/java" -cp "$OUT/classes:$CP" CrateNamesNativeTest 2>&1 | tee "$OUT/native-test.log"
"$JAVA_HOME/bin/javap" -classpath "$CP" -c net.minecraft.client.gui.inventory.GuiChest net.minecraft.inventory.InventoryBasic net.minecraft.network.play.server.SPacketOpenWindow > "$OUT/native-title-contract.txt"
