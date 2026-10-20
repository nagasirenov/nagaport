#!/usr/bin/env bash
set -euo pipefail
HERE=$(cd "$(dirname "$0")" && pwd)
ROOT=$(cd "$HERE/../../.." && pwd)
: "${JAVA_HOME:?Java 8 required}"
: "${MINECRAFT_CLASSPATH:?Genuine mapped Forge 1.12.2 classpath required}"
OUT=$(mktemp -d)
trap 'rm -rf "$OUT"' EXIT
python3 "$HERE/verify_and_extract.py" "$OUT/ExtractedEmbeddedStorage.java"
"$JAVA_HOME/bin/javac" -source 8 -target 8 -sourcepath '' -cp "$MINECRAFT_CLASSPATH" -d "$OUT" "$ROOT/src/main/java/com/hbm/util/NullableInventoryStackNbt.java" "$OUT/ExtractedEmbeddedStorage.java" "$HERE/NativeEmbeddedStorageTest.java" "$HERE/../bomb-inventory-nbt-native/NativeInventoryNbtTest.java"
"$JAVA_HOME/bin/javap" -classpath "$OUT:$MINECRAFT_CLASSPATH" -c 'ExtractedEmbeddedStorage$Armor' > "$OUT/armor.bytecode"
grep -F 'net/minecraft/nbt/NBTTagCompound.isEmpty:()Z' "$OUT/armor.bytecode"
echo "PASS cleanup call attributed to genuine native NBTTagCompound.isEmpty, not ItemStack emptiness"
"$JAVA_HOME/bin/java" -cp "$OUT:$MINECRAFT_CLASSPATH" NativeEmbeddedStorageTest
"$JAVA_HOME/bin/java" -cp "$OUT:$MINECRAFT_CLASSPATH" NativeInventoryNbtTest
