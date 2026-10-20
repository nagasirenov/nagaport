#!/usr/bin/env bash
set -euo pipefail
D=$(cd "$(dirname "$0")" && pwd)
R=$(cd "$D/../../.." && pwd)
J=${JAVA8_HOME:-/workspace/shared/nagaport-toolchain/java8}
CP_FILE=${NATIVE_CP_FILE:-/tmp/nagaport-ae2-native-classpath.txt}
OLD=${LEGACY_CLIENT_JAR:-/tmp/nagaport-legacy-client.jar}
O=$(mktemp -d /tmp/native-finite-aabb.XXXXXX)
trap 'rm -rf "$O"' EXIT
python3 "$D/source.py"
python3 "$D/engine.py"
python3 - "$OLD" "$O" "$D/binary-inputs.json" "$CP_FILE" <<'PY'
import sys,zipfile,pathlib,hashlib,json
rec=json.loads(pathlib.Path(sys.argv[3]).read_text())
for entry in pathlib.Path(sys.argv[4]).read_text().strip().split(':'):
 if pathlib.Path(entry).is_file() and zipfile.is_zipfile(entry):
  with zipfile.ZipFile(entry) as native:
   name=rec['native']['class']
   if name in native.namelist():
    assert hashlib.sha256(native.read(name)).hexdigest()==rec['native']['class_sha256']
    break
else:raise AssertionError('native AxisAlignedBB class not found')
p=pathlib.Path(sys.argv[1]);assert hashlib.sha256(p.read_bytes()).hexdigest()==rec['original_jar_sha256']
with zipfile.ZipFile(p) as z:
 for name,h in rec['original_classes_sha256'].items():
  data=z.read(name);assert hashlib.sha256(data).hexdigest()==h;(pathlib.Path(sys.argv[2])/name).write_bytes(data)
PY
CP=$(cat "$CP_FILE")
python3 "$D/generate_probe.py" "$O"
"$J/bin/javac" -cp "$O:$CP" -d "$O" "$R/src/main/java/com/hbm/lib/ForgeDirection.java" "$O/FiniteAabbProbe.java"
"$J/bin/java" -cp "$O:$CP" FiniteAabbProbe
"$J/bin/javap" -c -s -classpath "$O:$CP" FiniteAabbProbe > "$O/probe.javap"
grep -q 'AxisAlignedBB."<init>":(DDDDDD)V' "$O/probe.javap"
grep -q 'azt.a:(DDDDDD)Lazt;' "$O/probe.javap"
"$J/bin/javac" -cp "$J/lib/tools.jar" -d "$O" "$D/FiniteAabbScanner.java"
cp "$D/Controls.java.in" "$O/Controls.java"
printf '%s\n' "$O/Controls.java" > "$O/inputs"
"$J/bin/java" -cp "$O:$J/lib/tools.jar" FiniteAabbScanner "$O/inputs" "$CP_FILE" > "$O/controls.jsonl" 2> "$O/controls.log"
python3 - "$O/controls.jsonl" <<'PY'
import json,sys
r=[json.loads(l) for l in open(sys.argv[1])];assert len(r)==15,len(r);assert all(x['selected']==(i<2) for i,x in enumerate(r));print('PASS 2 positive / 13 negative native-owner, local-symbol scope, all-write finiteness, integer overflow and arithmetic attribution controls')
PY
