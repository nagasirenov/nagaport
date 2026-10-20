#!/usr/bin/env bash
set -euo pipefail
D=$(cd "$(dirname "$0")" && pwd)
J=${JAVA8_HOME:-/workspace/shared/nagaport-toolchain/java8}
CP_FILE=${NATIVE_CP_FILE:-/tmp/nagaport-ae2-native-classpath.txt}
OLD=${LEGACY_CLIENT_JAR:-/tmp/nagaport-legacy-client.jar}
O=$(mktemp -d /tmp/native-bounds.XXXXXX)
trap 'rm -rf "$O"' EXIT
python3 "$D/source.py"
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
p=pathlib.Path(sys.argv[1]);assert hashlib.sha256(p.read_bytes()).hexdigest()=='a4fc2284657544e0f4bcc964f927c2fda3e3a205178ed1d5d58883aaf9780cce'
z=zipfile.ZipFile(p)
for n in ['azt.class','azw.class','azu.class']:(pathlib.Path(sys.argv[2])/n).write_bytes(z.read(n))
PY
CP=$(cat "$CP_FILE")
python3 "$D/generate_probe.py" "$O"
"$J/bin/javac" -cp "$O:$CP" -d "$O" "$O/BoundProbe.java"
"$J/bin/java" -cp "$O:$CP" BoundProbe
"$J/bin/javap" -c -s -classpath "$O:$CP" BoundProbe > "$O/probe.javap"
grep -q 'AxisAlignedBB."<init>":(DDDDDD)V' "$O/probe.javap"
grep -q 'azt.a:(DDDDDD)Lazt;' "$O/probe.javap"
"$J/bin/javac" -cp "$J/lib/tools.jar" -d "$O" "$D/BoundScanner.java"
cp "$D/Controls.java.in" "$O/Controls.java"
printf '%s\n' "$O/Controls.java" > "$O/inputs"
"$J/bin/java" -cp "$O:$J/lib/tools.jar" BoundScanner "$O/inputs" "$CP_FILE" > "$O/controls.jsonl"
python3 - "$O/controls.jsonl" <<'PY'
import json,sys
r=[json.loads(l) for l in open(sys.argv[1])];assert len(r)==18;assert all(x['selected']==(i<4) for i,x in enumerate(r));print('PASS 4 positive / 14 negative exact-owner, type and arithmetic attribution controls')
PY
