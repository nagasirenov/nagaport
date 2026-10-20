#!/usr/bin/env bash
set -euo pipefail
D=$(cd "$(dirname "$0")" && pwd)
export PYTHONDONTWRITEBYTECODE=1
python3 "$D/source.py"
python3 "$D/engine.py"
python3 "$D/runtime.py"
python3 "$D/attribute.py"
