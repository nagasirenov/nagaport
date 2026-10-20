#!/usr/bin/env bash
set -euo pipefail
D=$(cd "$(dirname "$0")" && pwd)
J=${JAVA8_HOME:-/workspace/shared/nagaport-toolchain/java8}
CP_FILE=${NATIVE_CP_FILE:-/tmp/nagaport-ae2-native-classpath.txt}
OLD=${LEGACY_CLIENT_JAR:-/tmp/nagaport-legacy-client.jar}
O=$(mktemp -d /tmp/native-box-factories.XXXXXX)
trap 'rm -rf "$O"' EXIT
python3 - "$OLD" "$O" <<'PY'
import sys,zipfile,pathlib,hashlib
p=pathlib.Path(sys.argv[1]);assert hashlib.sha256(p.read_bytes()).hexdigest()=='a4fc2284657544e0f4bcc964f927c2fda3e3a205178ed1d5d58883aaf9780cce'
# Actual unmodified original classes extracted to avoid signed default-package collision.
z=zipfile.ZipFile(p)
for n in ['azt.class','azw.class','azu.class']:(pathlib.Path(sys.argv[2])/n).write_bytes(z.read(n))
PY
CP=$(cat "$CP_FILE")
"$J/bin/javac" -cp "$O:$CP" -d "$O" "$D/BoxFactoryProbe.java"
"$J/bin/java" -cp "$O:$CP" BoxFactoryProbe
"$J/bin/javap" -c -s -classpath "$O:$CP" BoxFactoryProbe > "$O/probe.javap"
grep -q 'AxisAlignedBB."<init>":(DDDDDD)V' "$O/probe.javap"
python3 "$D/apply-and-verify.py"
"$J/bin/javac" -cp "$J/lib/tools.jar" -d "$O" "$D/BoxFactoryScanner.java"
printf '%s\n' "$D/Positive.java" "$D/Negative.java" "$D/Shadow.java" "$D/Instance.java" > "$O/fixtures.txt"
"$J/bin/java" -cp "$O:$J/lib/tools.jar" BoxFactoryScanner "$O/fixtures.txt" "$CP_FILE" > "$O/fixtures.tsv"
test "$(wc -l < "$O/fixtures.tsv")" = 2
! grep -v '/Positive.java' "$O/fixtures.tsv"
echo 'PASS 2 positive and 6 negative ownership/argument fixtures; actual native constructor descriptor (DDDDDD)V'
