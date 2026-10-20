#!/usr/bin/env python3
"""Newest eleven-file thermal/recipe/codec layer; reverse before furnace18/EMPTY.
Exact file whitelists and immutable prior hashes, never a global null/API shim.
"""
import importlib.util
from pathlib import Path
import subprocess
import sys
ROOT = Path(__file__).resolve().parents[2]
_core_spec = importlib.util.spec_from_file_location('native_core_lifecycle_audit', ROOT / 'porting/tests/check-native-core-lifecycle-source.py')
_core = importlib.util.module_from_spec(_core_spec)
_core_spec.loader.exec_module(_core)
_compat_spec = importlib.util.spec_from_file_location('native_render_compat_audit', ROOT / 'porting/tests/check-native-render-compat-source.py')
_compat = importlib.util.module_from_spec(_compat_spec)
_compat_spec.loader.exec_module(_compat)
_roots_spec = importlib.util.spec_from_file_location('native_integration_roots_audit', ROOT / 'porting/tests/check-native-integration-roots-source.py')
_roots = importlib.util.module_from_spec(_roots_spec)
_roots_spec.loader.exec_module(_roots)
_bulk_spec = importlib.util.spec_from_file_location('bulk_tile_field_audit', ROOT / 'porting/tests/bulk-tile-field-migration.py')
_bulk = importlib.util.module_from_spec(_bulk_spec)
_bulk_spec.loader.exec_module(_bulk)
NAMES = ['thermal-furnace-family-migration.py', 'thermal-gui-migration.py', 'check-recipe-key-source.py', 'check-buffer-util-native-source.py']
FILES = {}
for index, name in enumerate(NAMES):
    spec = importlib.util.spec_from_file_location('thermal_family_' + str(index), ROOT / 'porting/tests' / name)
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    paths = module.FILES if hasattr(module, 'FILES') else module.WHITELIST
    for relative in paths:
        assert relative not in FILES, ('overlapping thermal source groups', relative)
        FILES[relative] = module

def reverse_compat_source(relative, text):
    """Reverse CP23 then CP22 without altering production sources."""
    return _compat.reverse_source(relative, _core.reverse_source(relative, text))

def reverse_integration_source(relative, text):
    """Reverse CP23 then CP22 then CP21 exactly once, retaining pinned introduced sources."""
    return _roots.reverse_source(relative, reverse_compat_source(relative, text))

def reverse_newer_source(relative, text):
    """CP23 -> CP22 -> CP21 -> CP20 bulk fields; only the historical source view changes."""
    return _bulk.reverse_source(relative, reverse_integration_source(relative, text))

def reverse_source(relative, text):
    text = reverse_newer_source(relative, text)
    module = FILES.get(relative)
    return text if module is None else module.reverse_source(relative, text)

def main():
    _core.verify()
    _compat.verify(source_transform=_core.reverse_source)
    _roots.verify(source_transform=reverse_compat_source)
    _bulk.verify(transform=reverse_integration_source)
    assert len(FILES) == 11
    for name in NAMES:
        if name == 'thermal-furnace-family-migration.py':
            next(module for module in FILES.values() if module.__file__.endswith(name)).main(source_transform=reverse_newer_source)
        else:
            subprocess.run([sys.executable, str(ROOT / 'porting/tests' / name)], check=True)
    print('PASS eleven exact thermal-family/recipe-key/codec source reversals before historical layers')

if __name__ == '__main__':
    main()
