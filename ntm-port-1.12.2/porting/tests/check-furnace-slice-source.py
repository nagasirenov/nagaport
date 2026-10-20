#!/usr/bin/env python3
"""Current furnace layer before EMPTY/callback/naming/sided historical reversals.
Nine prior-source files are exact reversible migrations; the dedicated native
model handler/assets have separate immutable hashes. No Git objects required.
"""
import importlib.util
from pathlib import Path
import subprocess
import sys
ROOT = Path(__file__).resolve().parents[2]
_thermal_spec = importlib.util.spec_from_file_location('thermal_family_audit', ROOT / 'porting/tests/check-thermal-family-source.py')
_thermal = importlib.util.module_from_spec(_thermal_spec)
_thermal_spec.loader.exec_module(_thermal)
NAMES = ['check-electric-furnace-core-source.py', 'check-furnace-block-source.py', 'check-furnace-gui-source.py']
GROUPS = []
FILES = {}
for index, name in enumerate(NAMES):
    spec = importlib.util.spec_from_file_location('furnace_slice_' + str(index), ROOT / 'porting/tests' / name)
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    GROUPS.append(module)
    for relative in module.FILES:
        assert relative not in FILES, ('overlapping furnace source groups', relative)
        FILES[relative] = module

def reverse_source(relative, text):
    text = _thermal.reverse_source(relative, text)
    module = FILES.get(relative)
    return text if module is None else module.reverse_source(relative, text)

def main():
    _thermal.main()
    assert len(FILES) == 9
    for module in GROUPS:
        if module.__file__.endswith('check-furnace-gui-source.py'):
            module.main(source_transform=_thermal.reverse_source)
        else:
            module.main()
    subprocess.run([sys.executable, str(ROOT / 'porting/tests/probe-electric-furnace-models.py'), '--source-only'], check=True)
    print('PASS nine exact furnace source reversals plus native model source/resource hashes; unresolved debris-face parity remains documented')

if __name__ == '__main__':
    main()
