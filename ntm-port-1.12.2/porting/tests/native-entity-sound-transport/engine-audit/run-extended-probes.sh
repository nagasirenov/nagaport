#!/usr/bin/env bash
set -euo pipefail
D=$(cd "$(dirname "$0")" && pwd)
P=/workspace/shared/world-sound-engine-evidence
CP=$(cat /tmp/nagaport-ae2-native-classpath.txt)
J=/workspace/shared/nagaport-toolchain/java8/bin
for TEST in NativeCustomSoundProbe PlayerSoundCoordinateProbe; do
    "$J/javac" -source 8 -target 8 -cp "$P:$P/production-classes:$CP" -d "$D" "$D/$TEST.java"
    (cd "$D"; "$J/java" -Djava.awt.headless=true -cp "$D:$P:$P/production-classes:$CP" ForgeEventNativeLauncher "$TEST" /tmp/nagaport-legacy-client.jar)
done
