#!/usr/bin/env bash
set -euo pipefail
if [[ $# != 6 ]]; then echo "Usage: $0 <overlay-project-root> <baseline-project-root> <native-classpath-file> <java8-home> <legacy-client-jar> <fresh-work-dir>" >&2; exit 2; fi
D=$(cd "$(dirname "$0")" && pwd)
OVERLAY="$1/src/main/java"
BASE="$2/src/main/java"
CP=$(cat "$3")
J="$4/bin"
LEGACY="$5"
mkdir -p "$6"
WORK=$(cd "$6" && pwd)
if [[ "$WORK" == "$D" || "$WORK" == "$D/"* ]]; then echo "Work directory must be outside sealed evidence." >&2; exit 2; fi
if find "$WORK" -mindepth 1 -maxdepth 1 -print -quit | grep -q .; then echo "Work directory must be fresh and empty." >&2; exit 2; fi
OUT="$WORK/classes"
mkdir -p "$OUT"
choose_source() { if [[ -f "$OVERLAY/$1" ]]; then printf '%s\n' "$OVERLAY/$1"; else printf '%s\n' "$BASE/$1"; fi; }
PACKET=$(choose_source com/hbm/packet/toclient/LegacyEntitySoundPacket.java)
TRANSPORT=$(choose_source com/hbm/sound/EntitySoundTransport.java)
RESOLVER=$(choose_source com/hbm/sound/SoundCategoryResolver.java)
NETWORK=$(choose_source com/hbm/main/NetworkHandler.java)
THREADED=$(choose_source com/hbm/packet/threading/ThreadedPacket.java)
CONTROL=$(choose_source com/hbm/packet/toclient/ExplosionKnockbackPacket.java)
sha256sum "$PACKET" "$TRANSPORT" "$RESOLVER" "$NETWORK" "$THREADED" "$CONTROL" "$D/NativeEntityTransportProbe.java" "$D/NativeCustomCodecProbe.java" "$D/ScopedCodecOwnershipProbe.java" "$D/fixtures/com/hbm/handler/threading/PacketThreading.java" "$D/ForgeEventNativeLauncher.java" > "$WORK/tested-inputs.sha256"
"$J/javac" -encoding UTF-8 -source 8 -target 8 -cp "$CP" -d "$OUT" "$PACKET" "$TRANSPORT" "$RESOLVER" "$D/NativeEntityTransportProbe.java" "$D/ForgeEventNativeLauncher.java" > "$WORK/compile.log" 2>&1
(cd "$WORK"; "$J/java" -Xmx320m -Djava.awt.headless=true -cp "$OUT:$CP" ForgeEventNativeLauncher NativeEntityTransportProbe "$LEGACY") 2>&1 | tee "$WORK/probe.log"
"$J/javac" -encoding UTF-8 -source 8 -target 8 -cp "$OUT:$CP" -d "$OUT" "$NETWORK" "$THREADED" "$CONTROL" "$D/fixtures/com/hbm/handler/threading/PacketThreading.java" "$D/NativeCustomCodecProbe.java" "$D/ScopedCodecOwnershipProbe.java" > "$WORK/custom-codec-compile.log" 2>&1
(cd "$WORK"; "$J/java" -Xmx320m -Djava.awt.headless=true -cp "$OUT:$CP" ForgeEventNativeLauncher NativeCustomCodecProbe) 2>&1 | tee "$WORK/custom-codec-probe.log"
(cd "$WORK"; "$J/java" -Xmx320m -Djava.awt.headless=true -cp "$OUT:$CP" ForgeEventNativeLauncher ScopedCodecOwnershipProbe) > "$WORK/scoped-ownership-probe.log" 2>&1
# The ownership probe deliberately causes decoder/handler exceptions; the full
# expected native diagnostics stay in its log, while this displays the result.
tail -1 "$WORK/scoped-ownership-probe.log"
printf 'Fresh proof output: %s\n' "$WORK"
