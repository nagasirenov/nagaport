#!/usr/bin/env bash
set -euo pipefail
HERE=$(cd "$(dirname "$0")" && pwd)
export PYTHONDONTWRITEBYTECODE=1
python3 "$HERE/source.py"
python3 "$HERE/closure.py"
python3 "$HERE/engine.py"
python3 "$HERE/init_audit.py"
python3 "$HERE/run.py"
