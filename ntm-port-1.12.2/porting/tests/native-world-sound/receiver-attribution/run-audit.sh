#!/usr/bin/env bash
set -euo pipefail
if [[ $# != 5 ]]; then echo "Usage: $0 <CP57-project-root> <failed-candidate-project-root> <corrected-project-root> <native-classpath-file> <java8-home>" >&2; exit 2; fi
HERE="$(cd "$(dirname "$0")" && pwd)"
BEFORE="$1"; CURRENT="$2"; CORRECTED="$3"; CLASSPATH="$4"; JAVA_HOME="$5"
mkdir -p "$HERE/classes"
"$JAVA_HOME/bin/javac" -cp "$JAVA_HOME/lib/tools.jar" -d "$HERE/classes" "$HERE/ReceiverAudit.java" "$HERE/ReceiverAuditSpans.java"
for mode in before current corrected; do
  case "$mode" in before) ROOT="$BEFORE";; current) ROOT="$CURRENT";; corrected) ROOT="$CORRECTED";; esac
  "$JAVA_HOME/bin/java" -Xmx5g -cp "$HERE/classes:$JAVA_HOME/lib/tools.jar" ReceiverAudit "$ROOT" "$CLASSPATH" "$HERE/sites.tsv" "$HERE/$mode-diagnostics.tsv" > "$HERE/$mode.tsv" 2> "$HERE/$mode.log"
  "$JAVA_HOME/bin/java" -Xmx512m -cp "$HERE/classes:$JAVA_HOME/lib/tools.jar" ReceiverAuditSpans "$ROOT" "$CLASSPATH" "$HERE/sites.tsv" "$HERE/$mode-spans-diagnostics.tsv" > "$HERE/$mode-spans.tsv" 2> "$HERE/$mode-spans.log"
done
python3 "$HERE/verify-evidence.py" "$BEFORE" "$CURRENT" "$CORRECTED"
