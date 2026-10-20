#!/usr/bin/env python3
"""Exact reviewed two-file snapshot and reversibility evidence, not gameplay proof."""
from pathlib import Path
import hashlib
import json

ROOT = Path(__file__).resolve().parents[2]
MANIFEST = json.loads((ROOT / 'porting/tests/container-clicks-manifest.json').read_text(encoding='utf-8'))
FILES = MANIFEST['files']


def sha(text):
    return hashlib.sha256(text.encode('utf-8')).hexdigest()


def reverse_source(relative, text):
    """Restore frozen baseline only after exact whole-file native hash verification."""
    entry = FILES.get(relative)
    if entry is None:
        return text
    assert sha(text) == entry['native_sha256'], (relative, 'container native whole-file drift')
    before = entry['before_source']
    assert sha(before) == entry['before_sha256'], (relative, 'container original snapshot corrupt')
    return before


def main():
    for name in FILES:
        text = (ROOT / name).read_bytes().decode('utf-8')
        reverse_source(name, text)
        try:
            reverse_source(name, text + '\n')
        except AssertionError:
            pass
        else:
            raise AssertionError((name, 'whole-file drift accepted'))
        for forbidden in ['.stackSize', 'onPickupFromSlot', 'return null;', 'putStack(null)', 'setItemStack(null)']:
            assert forbidden not in text, (name, forbidden)
    nt = (ROOT / 'src/main/java/com/hbm/inventory/container/ContainerNT.java').read_text(encoding='utf-8')
    for kind in ['PICKUP', 'QUICK_MOVE', 'SWAP', 'CLONE', 'THROW', 'QUICK_CRAFT', 'PICKUP_ALL']:
        assert 'clickType == ClickType.' + kind in nt, kind
    assert 'ordinal()' not in nt
    assert 'if(this.dragMode == 0 || this.dragMode == 1)' in nt
    assert 'this.dragMode == 2' not in nt
    assert 'isValidDragMode(' not in nt
    print('PASS exact container source reversals, drift rejections, native boundaries and seven explicit ClickTypes')


if __name__ == '__main__':
    main()
