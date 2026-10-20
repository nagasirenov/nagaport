#!/usr/bin/env bash
set -euo pipefail
if [[ $# != 4 ]]; then echo "Usage: $0 <project-root> <classpath-file> <java8-home> <fresh-work-dir>" >&2; exit 2; fi
D=$(cd "$(dirname "$0")" && pwd); MODULE=$(cd "$1" && pwd); CP="$2"; J="$3/bin"
mkdir -p "$4"; WORK=$(cd "$4" && pwd)
if find "$WORK" -mindepth 1 -maxdepth 1 -print -quit | grep -q .; then echo "Work must be fresh" >&2; exit 2; fi
BASE=5e6ba284ef32b46ad5f81cd1e1570c08b53cb24f
REPO=$(git -C "$MODULE" rev-parse --show-toplevel)
git -C "$REPO" archive --format=zip "$BASE" ntm-port-1.12.2/src/main/java > "$WORK/source.zip"
mkdir -p "$WORK/classes"
"$J/javac" -cp "$3/lib/tools.jar" -d "$WORK/classes" "$D/WorldSoundWideningAttribution.java"
"$J/java" -Xmx4g -cp "$WORK/classes:$3/lib/tools.jar" WorldSoundWideningAttribution "$WORK/source.zip" "$CP" - "$WORK/before-diagnostics.tsv" - > "$WORK/before.tsv" 2> "$WORK/before.log"
"$J/java" -Xmx4g -cp "$WORK/classes:$3/lib/tools.jar" WorldSoundWideningAttribution "$WORK/source.zip" "$CP" "$D/sites.tsv" "$WORK/after-diagnostics.tsv" "$MODULE/src/main/java" > "$WORK/after.tsv" 2> "$WORK/after.log"
python3 "$D/check-source.py" --project "$MODULE" --proof "$WORK"
