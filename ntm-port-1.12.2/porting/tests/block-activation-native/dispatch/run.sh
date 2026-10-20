#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"
python3 prove_native_dispatch.py | tee proof.log
