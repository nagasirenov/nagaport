#!/usr/bin/env bash
set -euo pipefail
HERE="$(cd "$(dirname "$0")" && pwd)"
REPO="${REPO:-/workspace/shared/nagaport}"
JAVA_HOME="${JAVA_HOME:-/workspace/shared/nagaport-toolchain/java8}"
SCRATCH="${SCRATCH:-/tmp/player-sound-attribution}"
BASE=e72ec3d2ae8db6653ebaad529a574ee32c99260b
mkdir -p "$SCRATCH/classes" "$SCRATCH/empty-sourcepath"
git -C "$REPO" archive --format=zip -o "$SCRATCH/cp59-sources.zip" "$BASE" ntm-port-1.12.2/src/main/java
"$JAVA_HOME/bin/javac" -cp "$JAVA_HOME/lib/tools.jar" -d "$SCRATCH/classes" "$HERE/PlayerSoundAttribution.java"
"$JAVA_HOME/bin/java" -Xmx5g -cp "$SCRATCH/classes:$JAVA_HOME/lib/tools.jar" PlayerSoundAttribution "$SCRATCH/cp59-sources.zip" "$HERE/native-classpath.txt" "$HERE/diagnostic-sites.tsv" "$HERE/before-diagnostics.tsv" - > "$HERE/before.tsv" 2> "$HERE/before.log"
CP="$(cat "$HERE/native-classpath.txt")"
"$JAVA_HOME/bin/javac" -cp "$CP" -d "$SCRATCH/classes" "$HERE/ResourceNames.java"
python3 - "$HERE" "$JAVA_HOME/bin/java" "$SCRATCH/classes:$CP" <<'PY'
import csv,json,pathlib,subprocess,sys
p=pathlib.Path(sys.argv[1]);sounds=json.loads((p/'sounds.json').read_text());samples=set()
for row in csv.DictReader((p/'before.tsv').open(),delimiter='\t'):
    name=row['string_literals'].split(' || ')[1]
    if name.startswith('hbm:') and name!='hbm:item.battery' and name[4:] in sounds:
        samples.update(s if isinstance(s,str) else s['name'] for s in sounds[name[4:]]['sounds'])
(p/'native-resource-names.tsv').write_text(subprocess.check_output([sys.argv[2],'-cp',sys.argv[3],'ResourceNames']+sorted(samples),text=True))
PY
python3 "$HERE/select_candidates.py" > "$HERE/verification.log"
cat "$HERE/before.log" "$HERE/verification.log"
