#!/usr/bin/env bash
set -euo pipefail
HERE=$(cd "$(dirname "$0")" && pwd)
ROOT=$(cd "$HERE/../../.." && pwd)
: "${JAVA_HOME:?Java 8 required}"
: "${MINECRAFT_CLASSPATH:?Mapped Forge 1.12.2 classpath required}"
: "${LEGACY_CLIENT_JAR:?Actual 1.7.10 client jar required}"
OUT=$(mktemp -d)
trap 'rm -rf "$OUT"' EXIT
python3 "$HERE/verify.py"
mkdir -p "$OUT/native" "$OUT/legacy"
"$JAVA_HOME/bin/javac" -source 8 -target 8 -sourcepath '' -cp "$MINECRAFT_CLASSPATH" -d "$OUT/native" "$ROOT/src/main/java/com/hbm/util/NullableInventoryStackNbt.java" "$HERE/NativeInventoryNbtTest.java"
"$JAVA_HOME/bin/java" -cp "$OUT/native:$MINECRAFT_CLASSPATH" NativeInventoryNbtTest
# The client is signed in the default package. Strip only metadata into a
# disposable copy so the default-package oracle can run; all class bytes stay exact.
python3 - "$LEGACY_CLIENT_JAR" "$OUT/legacy-unsigned.jar" <<'PY'
import zipfile,sys,hashlib
with zipfile.ZipFile(sys.argv[1]) as src,zipfile.ZipFile(sys.argv[2],'w') as dst:
    for entry in src.infolist():
        if not entry.filename.upper().startswith('META-INF/'):
            dst.writestr(entry,src.read(entry.filename))
with zipfile.ZipFile(sys.argv[1]) as src,zipfile.ZipFile(sys.argv[2]) as dst:
    for name in dst.namelist(): assert src.read(name)==dst.read(name)
print('PASS original client class/resource bytes unchanged in disposable signature-free oracle copy')
PY
"$JAVA_HOME/bin/javac" -source 8 -target 8 -sourcepath '' -cp "$OUT/legacy-unsigned.jar:$MINECRAFT_CLASSPATH" -d "$OUT/legacy" "$HERE/LegacyInventoryNbtTest.java"
"$JAVA_HOME/bin/java" -cp "$OUT/legacy:$OUT/legacy-unsigned.jar:$MINECRAFT_CLASSPATH" LegacyInventoryNbtTest
