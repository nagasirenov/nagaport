#!/usr/bin/env bash
set -euo pipefail
D=$(cd "$(dirname "$0")" && pwd)
J=${JAVA8_HOME:-/workspace/shared/nagaport-toolchain/java8}
CP_FILE=${NATIVE_CP_FILE:-/tmp/nagaport-ae2-native-classpath.txt}
OLD=${LEGACY_CLIENT_JAR:-/tmp/nagaport-legacy-client.jar}
O=$(mktemp -d /tmp/native-symmetric-growth.XXXXXX)
trap 'rm -rf "$O"' EXIT
export PYTHONDONTWRITEBYTECODE=1
python3 "$D/source.py"
python3 - "$OLD" "$O" "$D/binary-inputs.json" "$CP_FILE" <<'PY'
import sys,zipfile,pathlib,hashlib,json
rec=json.loads(pathlib.Path(sys.argv[3]).read_text())
for entry in pathlib.Path(sys.argv[4]).read_text().strip().split(':'):
 if pathlib.Path(entry).is_file() and zipfile.is_zipfile(entry):
  with zipfile.ZipFile(entry) as native:
   name=rec['native']['class']
   if name in native.namelist():
    assert hashlib.sha256(native.read(name)).hexdigest()==rec['native']['class_sha256'];break
else:raise AssertionError('native AxisAlignedBB class not found')
p=pathlib.Path(sys.argv[1]);assert hashlib.sha256(p.read_bytes()).hexdigest()==rec['original_jar_sha256'];z=zipfile.ZipFile(p)
for n,h in rec['original_classes_sha256'].items():
 b=z.read(n);assert hashlib.sha256(b).hexdigest()==h;(pathlib.Path(sys.argv[2])/n).write_bytes(b)
PY
CP=$(cat "$CP_FILE")
python3 "$D/generate_probe.py" "$O"
"$J/bin/javac" -cp "$O:$CP" -d "$O" "$O/GrowthProbe.java" "$D/NativeClosure.java"
"$J/bin/java" -cp "$O:$CP" GrowthProbe
"$J/bin/java" -cp "$O:$CP" NativeClosure "$CP_FILE"
"$J/bin/javap" -c -s -classpath "$O:$CP" GrowthProbe > "$O/probe.javap"
grep -q 'AxisAlignedBB.grow:(DDD)' "$O/probe.javap"
grep -q 'azt.b:(DDD)Lazt;' "$O/probe.javap"
"$J/bin/javac" -cp "$J/lib/tools.jar" -d "$O" "$D/GrowthScanner.java"
for N in Controls OverrideControl; do
 cp "$D/$N.java.in" "$O/$N.java"
 printf '%s\n' "$O/$N.java" > "$O/inputs"
 "$J/bin/java" -cp "$O:$J/lib/tools.jar" GrowthScanner "$O/inputs" "$CP_FILE" > "$O/$N.jsonl"
done
python3 - "$O" <<'PY'
import json,sys,pathlib
D=pathlib.Path(sys.argv[1]);r=[json.loads(l) for l in (D/'Controls.jsonl').read_text().splitlines()];assert len(r)==19;assert all((x['candidate'] and x['name']=='expand')==(i<4) for i,x in enumerate(r));r=[json.loads(l) for l in (D/'OverrideControl.jsonl').read_text().splitlines()];assert len(r)==2 and not any(x['candidate'] for x in r);print('PASS 4 positive / 15 negative compiler controls plus 2 reachable source-subclass override controls')
PY
