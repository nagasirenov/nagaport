#!/usr/bin/env python3
"""Fail-closed two-file native inventory migration and historical audit reversal.
The full old source is retained so historical audits need neither Git nor stubs.
Reversal is audit-only and requires the exact reviewed native production bytes.
"""
import hashlib
import json
from pathlib import Path
import re
ROOT = Path(__file__).resolve().parents[2]
MANIFEST = Path(__file__).with_name('inventory-boundary-manifest.json')
def digest(text):
    return hashlib.sha256(text.encode('utf-8')).hexdigest()
def records():
    return json.loads(MANIFEST.read_text(encoding='utf-8'))['files']
def legacy_text(relative, text):
    """Undo only this reviewed migration before an older audit's own reversal."""
    entry = records().get(relative)
    if entry is None:
        return text
    assert digest(text) == entry['native_sha256'], (relative, 'Native inventory source drift')
    assert digest(entry['before_source']) == entry['before_sha256'], (relative, 'Corrupt historical inventory source')
    return entry['before_source']
def restore_texts(texts):
    return {relative: legacy_text(relative, text) for relative, text in texts.items()}
def main(transform=None):
    data = records()
    base = 'src/main/java/com/hbm/tileentity/TileEntityInventoryBase.java'
    capsule = 'src/main/java/com/hbm/tileentity/machine/storage/TileEntitySoyuzCapsule.java'
    assert set(data) == {base, capsule}
    for relative, record in data.items():
        text = (ROOT / relative).read_bytes().decode('utf-8')
        if transform is not None: text = transform(relative, text)
        legacy_text(relative, text)
        if relative == capsule:
            assert text.replace('protected String getDefaultName() {', 'public String getName() {') == record['before_source']
    subclasses = []
    for path in (ROOT / 'src/main/java').rglob('*.java'):
        if re.search(r'\bextends\s+TileEntityInventoryBase\b', path.read_text(encoding='utf-8')):
            subclasses.append(path.relative_to(ROOT).as_posix())
    assert subclasses == [capsule], ('Re-audit new inventory descendants', subclasses)
    gui = (ROOT / 'src/main/java/com/hbm/inventory/gui/GUISoyuzCapsule.java').read_text()
    container = (ROOT / 'src/main/java/com/hbm/inventory/container/ContainerSoyuzCapsule.java').read_text()
    assert 'this.diFurnace.hasCustomInventoryName()' in gui and 'this.diFurnace.getInventoryName()' in gui
    assert 'diFurnace.isUseableByPlayer(player)' in container
    text = (ROOT / base).read_text()
    assert 'world.getTileEntity(pos) != this' in text
    assert 'player.getDistanceSq(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) <= 128' in text
    assert text.count('markDirty();') == 1
    print('PASS: exact reviewed two-file inventory source; sole descendant/name hook; actual legacy wrapper callers; distance 128; audit-only reversal')
if __name__ == '__main__':
    main()
