#!/usr/bin/env bash
set -euo pipefail
HERE=$(cd "$(dirname "$0")" && pwd)
ROOT=$(cd "$HERE/../../.." && pwd)
: "${JAVA_HOME:?Set Java 8 JAVA_HOME}"
: "${MINECRAFT_CLASSPATH:?Set actual mapped Forge 1.12.2 classpath}"
OUT=$(mktemp -d)
trap 'rm -rf "$OUT"' EXIT
printf '%s\n' "$MINECRAFT_CLASSPATH" > "$OUT/classpath.txt"
"$JAVA_HOME/bin/javac" -cp "$JAVA_HOME/lib/tools.jar" -d "$OUT" "$HERE/StackNbtAccessScanner.java"
printf '%s\n' "$HERE/controls/ReceiverControls.java" > "$OUT/controls.txt"
"$JAVA_HOME/bin/java" -cp "$OUT:$JAVA_HOME/lib/tools.jar" StackNbtAccessScanner "$OUT/controls.txt" "$OUT/classpath.txt" > "$HERE/control-accesses.jsonl" 2> "$HERE/control-attribution.log"
python3 "$HERE/verify.py"
"$JAVA_HOME/bin/javac" -source 8 -target 8 -encoding UTF-8 -sourcepath '' -cp "$MINECRAFT_CLASSPATH" -d "$OUT" \
  "$ROOT/src/main/java/cofh/api/energy/IEnergyContainerItem.java" \
  "$ROOT/src/main/java/cofh/api/energy/ItemEnergyContainer.java" "$ROOT/src/main/java/com/hbm/items/ISatChip.java" "$HERE/NativeStackNbtTest.java"
"$JAVA_HOME/bin/java" -cp "$OUT:$MINECRAFT_CLASSPATH" NativeStackNbtTest
"$JAVA_HOME/bin/javap" -classpath "$MINECRAFT_CLASSPATH" -c net.minecraft.item.ItemStack > "$OUT/ItemStack.bytecode.txt"
python3 - "$OUT/ItemStack.bytecode.txt" <<'PY'
import re,sys
text=open(sys.argv[1]).read()
for signature,instructions in [('public net.minecraft.nbt.NBTTagCompound getTagCompound();',['aload_0','getfield','areturn']),('public void setTagCompound(net.minecraft.nbt.NBTTagCompound);',['aload_0','aload_1','putfield','return'])]:
 section=text.split(signature,1)[1].split('\n\n',1)[0]
 actual=re.findall(r'^\s*\d+: (\w+)',section,re.M)
 assert actual==instructions,(signature,actual)
 assert 'stackTagCompound:Lnet/minecraft/nbt/NBTTagCompound;' in section
print('PASS native getter/setter bytecode: exact raw getfield/putfield, no copy or normalization')
PY
