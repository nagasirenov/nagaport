#!/usr/bin/env bash
set -euo pipefail
H=porting/tests/native-translation-subtype-chains
J=${JAVA_HOME:-/workspace/shared/nagaport-toolchain/java8}
CP=$(cat /tmp/nagaport-ae2-native-classpath.txt)
T=$(mktemp -d /tmp/armor-names-proof.XXXXXX)
trap 'rm -rf "$T"' EXIT
"$J/bin/javac" -cp "$J/lib/tools.jar" -d "$T" "$H/SubtypeScanner.java" "$H/SubtypeDestinationScanner.java" porting/tests/native-translation-name-chains/ReturnProof.java
"$J/bin/javac" -cp "$CP" -d "$T" "$H/ArmorNameOracle.java" porting/tests/native-translation-names/NameOracle.java porting/tests/native-translation-name-chains/HierarchyOracle.java 2> "$H/native-compile.log"
for probe in ArmorNameOracle NameOracle HierarchyOracle; do
 "$J/bin/java" -cp "$T:$CP" "$probe" /tmp/nagaport-legacy-client.jar
done
"$J/bin/java" -cp "$T:$J/lib/tools.jar" ReturnProof src/main/java/com/hbm/items/armor/ArmorFSB.java
find "$PWD/src/main/java" -name '*.java' > "$T/sources.txt"
"$J/bin/java" -Xmx1800m -cp "$T:$J/lib/tools.jar" SubtypeScanner "$T/sources.txt" /tmp/nagaport-ae2-native-classpath.txt > "$H/after.tsv" 2> "$H/after.log"
"$J/bin/java" -Xmx1800m -cp "$T:$J/lib/tools.jar" SubtypeDestinationScanner "$T/sources.txt" /tmp/nagaport-ae2-native-classpath.txt > "$H/independent-after.tsv" 2> "$H/independent-after.log"
cp "$H/Controls.java.in" "$T/Controls.java"
echo "$T/Controls.java" >> "$T/sources.txt"
"$J/bin/java" -Xmx1800m -cp "$T:$J/lib/tools.jar" SubtypeScanner "$T/sources.txt" /tmp/nagaport-ae2-native-classpath.txt > "$H/controls.tsv" 2> "$H/controls.log"
python3 "$H/verify.py"
