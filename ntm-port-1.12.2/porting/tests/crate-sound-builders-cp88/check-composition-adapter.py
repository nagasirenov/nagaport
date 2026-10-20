#!/usr/bin/env python3
"""Family reconstruction controls; composed acceptance requires the real pinned aggregate."""
import importlib.util, subprocess, sys
from pathlib import Path
sys.dont_write_bytecode = True
HERE = Path(__file__).resolve().parent
spec = importlib.util.spec_from_file_location('crate_guard', HERE/'check-source.py')
guard = importlib.util.module_from_spec(spec)
spec.loader.exec_module(guard)
old, current = guard.inputs()
old = {path: old[path] for path in guard.FAMILY}
current = {path: current[path] for path in guard.FAMILY}
assert guard.reconstruct_family(old) == current
assert guard.validate(old, current) == 3
print('PASS exact complete three-file family reconstruction equals actual tested sources')
for path in guard.FAMILY:
    changed = dict(current)
    changed[path] += b'\n'
    try: guard.validate(old, changed)
    except AssertionError: print('PASS rejected whole-file family drift:', path)
    else: raise AssertionError('Composed family validation accepted drift: ' + path)
if (HERE.parent/guard.COMPOSED_GUARD).is_file() and (HERE.parent/guard.COMPOSED_MANIFEST).is_file():
    guard.verify_composed()
else:
    result = subprocess.run([sys.executable, str(HERE/'check-source.py'), '--composed-source'], capture_output=True, text=True)
    assert result.returncode and 'Required pinned CP88 aggregate guard/manifest is absent' in result.stderr, result.stderr
    print('PASS composed mode fails closed while the required real CP88 aggregate is unavailable; no composed native acceptance claimed')
