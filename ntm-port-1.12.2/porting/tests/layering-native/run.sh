#!/usr/bin/env bash
set -euo pipefail
ROOT=$(git rev-parse --show-toplevel)
HERE="$ROOT/ntm-port-1.12.2/porting/tests/layering-native"
: "${JAVA_HOME:?Use the existing Java 8 toolchain}"
: "${NATIVE_CLASSPATH_FILE:?Use the existing native classpath file}"
OUT=${1:-$(mktemp -d)}
mkdir -p "$OUT/classes" "$OUT/src/com/hbm/blocks/machine/rbmk" "$OUT/src/com/hbm/blocks/machine"
python3 "$HERE/verify.py" "$ROOT" "$OUT" | tee "$OUT/source-proof.log"
cat > "$OUT/src/com/hbm/blocks/ModBlocks.java" <<'JAVA'
package com.hbm.blocks;
public final class ModBlocks { public static net.minecraft.block.Block foam_layer, sand_boron_layer, leaves_layer, oil_spill; }
JAVA
cat > "$OUT/src/com/hbm/blocks/machine/ZirnoxDestroyed.java" <<'JAVA'
package com.hbm.blocks.machine;
public class ZirnoxDestroyed extends net.minecraft.block.Block { public ZirnoxDestroyed() { super(net.minecraft.block.material.Material.IRON); } }
JAVA
cat > "$OUT/src/com/hbm/blocks/machine/rbmk/RBMKDebris.java" <<'JAVA'
package com.hbm.blocks.machine.rbmk;
public class RBMKDebris extends net.minecraft.block.Block { public RBMKDebris() { super(net.minecraft.block.material.Material.IRON); } }
JAVA
CP=$(cat "$NATIVE_CLASSPATH_FILE")
SRC="$ROOT/ntm-port-1.12.2/src/main/java"
"$JAVA_HOME/bin/javac" -source 8 -target 8 -cp "$CP" -d "$OUT/classes" $(find "$OUT/src" -name '*.java') "$SRC/com/hbm/blocks/generic/BlockLayering.java" "$SRC/com/hbm/world/WorldBlockState.java" "$SRC/com/hbm/world/WorldAirRemoval.java" "$HERE/LayeringNativeTest.java" 2>&1 | tee "$OUT/compile.log"
"$JAVA_HOME/bin/java" -cp "$OUT/classes:$CP" LayeringNativeTest 2>&1 | tee "$OUT/native-test.log"
"$JAVA_HOME/bin/javap" -classpath "$OUT/classes:$CP" -c com.hbm.blocks.generic.BlockLayering > "$OUT/native-owner.javap.txt"
