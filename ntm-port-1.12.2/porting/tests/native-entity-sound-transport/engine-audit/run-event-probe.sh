#!/usr/bin/env bash
set -euo pipefail
D=$(cd "$(dirname "$0")" && pwd)
CP=$(cat /tmp/nagaport-ae2-native-classpath.txt)
JAVA=/workspace/shared/nagaport-toolchain/java8/bin
"$JAVA/javac" -source 8 -target 8 -cp "$CP" -d "$D" "$D/NativeEntitySoundEventProbe.java" > "$D/native-entity-event-compile.log" 2>&1
cd "$D"
"$JAVA/java" -Djava.awt.headless=true -cp "$D:/workspace/shared/world-sound-engine-evidence:$CP" ForgeEventNativeLauncher NativeEntitySoundEventProbe 2>&1 | tee "$D/native-entity-event-probe.log"
