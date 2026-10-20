#!/usr/bin/env bash
set -euo pipefail
if [[ $# != 5 ]]; then echo "Usage: $0 <project-root> <native-classpath-file> <java8-home> <legacy-client-jar> <fresh-work-dir>" >&2; exit 2; fi
D=$(cd "$(dirname "$0")" && pwd)
PROJECT=$(cd "$1" && pwd); CP=$(cat "$2"); JAVA=$(cd "$3" && pwd); LEGACY=$(realpath "$4")
OUT=$(realpath -m "$5")
case "$OUT/" in "$D/"*) echo 'Work directory must be outside preserved evidence.' >&2; exit 2;; esac
if [[ -e "$OUT" && -n $(find "$OUT" -mindepth 1 -maxdepth 1 -print -quit) ]]; then echo 'Work directory must be fresh and empty.' >&2; exit 2; fi
mkdir -p "$OUT/classes" "$OUT/legacy-classes" "$OUT/generated" "$OUT/empty-sourcepath" "$OUT/tmp"
python3 "$D/generate-fixtures.py" "$OUT/generated"
sources=();for f in com/hbm/main/ModSoundEvents.java com/hbm/lib/RefStrings.java com/hbm/sound/WorldSoundEffects.java com/hbm/sound/WorldSoundCategories.java com/hbm/sound/SoundCategoryResolver.java;do sources+=("$PROJECT/src/main/java/$f");done
sha256sum "${sources[@]}" "$LEGACY" "$D/generate-fixtures.py" "$D/NumericWideningProbe.java" "$D/ForgeEventNativeLauncher.java" "$OUT/generated/LegacyNumericFixture.java" "$OUT/generated/NativeNumericFixture.java" "$D/run-probes.sh" "$D/verify-bytecode.py" "$D/check-compile-negatives.py" "$2" "$PROJECT/src/main/resources/assets/hbm/sounds.json" > "$OUT/tested-inputs.sha256"
"$JAVA/bin/javac" -encoding UTF-8 -source 8 -target 8 -sourcepath "$OUT/empty-sourcepath" -cp "$LEGACY:$CP" -d "$OUT/legacy-classes" "$OUT/generated/LegacyNumericFixture.java" > "$OUT/legacy-compile.log" 2>&1
"$JAVA/bin/javac" -encoding UTF-8 -source 8 -target 8 -sourcepath "$OUT/empty-sourcepath" -cp "$CP" -d "$OUT/classes" "${sources[@]}" "$OUT/generated/NativeNumericFixture.java" "$D/NumericWideningProbe.java" "$D/ForgeEventNativeLauncher.java" > "$OUT/native-compile.log" 2>&1
"$JAVA/bin/javap" -c -p -classpath "$OUT/legacy-classes:$LEGACY" LegacyNumericFixture > "$OUT/legacy-wrapper.javap"
"$JAVA/bin/javap" -c -p -classpath "$OUT/classes:$CP" NativeNumericFixture > "$OUT/native-wrapper.javap"
(cd "$OUT" && "$JAVA/bin/java" -Djava.io.tmpdir="$OUT/tmp" -Djava.awt.headless=true -cp "$OUT/classes:$CP" ForgeEventNativeLauncher NumericWideningProbe "$LEGACY" "$OUT/legacy-classes") 2>&1 | tee "$OUT/probe.log"
python3 "$D/verify-bytecode.py" "$OUT" | tee "$OUT/bytecode-verification.log"
python3 "$D/check-compile-negatives.py" "$JAVA" "$CP" "$LEGACY" "$OUT" | tee "$OUT/negative-compile-verification.log"
REG="$PROJECT/porting/tests/native-world-sound"
regressions=("$REG/WorldSoundRegistryCategoryTest.java" "$REG/NativeWorldSoundCallbackProbe.java" "$REG/PacketParityProbe.java" "$REG/ReceiverRangeTest.java")
sha256sum "${regressions[@]}" "$REG/source-manifest.json" >> "$OUT/tested-inputs.sha256"
"$JAVA/bin/javac" -encoding UTF-8 -source 8 -target 8 -sourcepath "$OUT/empty-sourcepath" -cp "$OUT/classes:$CP:$JAVA/lib/tools.jar" -d "$OUT/classes" "${regressions[@]}" > "$OUT/regression-compile.log" 2>&1
(cd "$OUT" && "$JAVA/bin/java" -Djava.io.tmpdir="$OUT/tmp" -cp "$OUT/classes:$CP:$JAVA/lib/tools.jar" ReceiverRangeTest "$OUT/classes:$CP:$JAVA/lib/tools.jar") 2>&1 | tee "$OUT/receiver-regression.log"
(cd "$OUT" && "$JAVA/bin/java" -Djava.io.tmpdir="$OUT/tmp" -Djava.awt.headless=true -cp "$OUT/classes:$CP" ForgeEventNativeLauncher WorldSoundRegistryCategoryTest "$PROJECT/src/main/resources" "$REG/source-manifest.json") 2>&1 | tee "$OUT/registry-category-regression.log"
(cd "$OUT" && "$JAVA/bin/java" -Djava.io.tmpdir="$OUT/tmp" -Djava.awt.headless=true -cp "$OUT/classes:$CP" ForgeEventNativeLauncher NativeWorldSoundCallbackProbe "$PROJECT/src/main/resources") 2>&1 | tee "$OUT/callback-regression.log"
(cd "$OUT" && "$JAVA/bin/java" -Djava.io.tmpdir="$OUT/tmp" -Djava.awt.headless=true -cp "$OUT/classes:$CP" ForgeEventNativeLauncher PacketParityProbe "$LEGACY") 2>&1 | tee "$OUT/packet-regression.log"
printf 'Fresh runtime outputs: %s\n' "$OUT"
