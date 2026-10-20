#!/usr/bin/env bash
set -euo pipefail
if [[ $# != 5 ]]; then echo "Usage: $0 <project-root> <native-classpath-file> <java8-home> <legacy-client-jar> <fresh-work-dir>" >&2; exit 2; fi
D=$(cd "$(dirname "$0")" && pwd); S="$1/src/main/java"; CP=$(cat "$2"); J="$3/bin"; LEGACY="$4"
mkdir -p "$5"; WORK=$(cd "$5" && pwd)
if find "$WORK" -mindepth 1 -maxdepth 1 -print -quit | grep -q .; then echo "Work must be empty" >&2; exit 2; fi
OUT="$WORK/classes"; mkdir -p "$OUT"
SOURCES=("$S/com/hbm/sound/VanillaWorldSoundEffects.java" "$S/com/hbm/sound/EntitySoundTransport.java" "$S/com/hbm/sound/SoundCategoryResolver.java" "$S/com/hbm/packet/toclient/LegacyEntitySoundPacket.java" "$S/com/hbm/main/NetworkHandler.java" "$S/com/hbm/packet/threading/ThreadedPacket.java" "$D/fixtures/com/hbm/packet/PacketDispatcher.java" "$D/fixtures/com/hbm/handler/threading/PacketThreading.java" "$D/NativeEntityTransportProbe.java" "$D/NativeCustomCodecProbe.java" "$D/NativeVanillaSoundProbe.java" "$D/RepresentativeMethods.java" "$D/ForgeEventNativeLauncher.java")
sha256sum "${SOURCES[@]}" > "$WORK/tested-inputs.sha256"
"$J/javac" -encoding UTF-8 -source 8 -target 8 -cp "$CP" -d "$OUT" "${SOURCES[@]}" > "$WORK/compile.log" 2>&1
(cd "$WORK"; "$J/java" -Xmx320m -Djava.awt.headless=true -cp "$OUT:$CP" ForgeEventNativeLauncher NativeVanillaSoundProbe "$D/../manifest.json" "$D/../provenance/legacy-sounds.json" "$D/../provenance/native-sounds.json" "$LEGACY" "$1/src/main/resources") 2>&1 | tee "$WORK/probe.log"
