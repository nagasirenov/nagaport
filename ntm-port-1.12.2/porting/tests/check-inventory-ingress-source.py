#!/usr/bin/env python3
"""Fail-closed reversal of 35 independent inventory setters and 34 removals; no Git objects required.
Compose reverse_source before callback -> names -> sided historical reversal.
Does not migrate or approve any other inventory code.
"""
import hashlib
import json
from pathlib import Path
ROOT = Path(__file__).resolve().parents[2]
MANIFEST = json.loads((ROOT / 'porting/tests/inventory-ingress-source-manifest.json').read_text(encoding='utf-8'))
FILES = {entry['path']: entry for entry in MANIFEST['files']}

def require(ok, detail):
    if not ok:
        raise AssertionError(detail)

def sha(text):
    return hashlib.sha256(text.encode('utf-8')).hexdigest()

def reverse_source(relative, text):
    entry = FILES.get(relative)
    if entry is None:
        return text
    require(sha(text) == entry['after_sha256'], (relative, 'ingress migrated whole-file drift'))
    before = text
    for edit in reversed(entry['edits']):
        require(before.count(edit['new']) == 1, (relative, edit['kind'], 'ingress method slice drift'))
        before = before.replace(edit['new'], edit['old'], 1)
    require(sha(before) == entry['before_sha256'], (relative, 'ingress baseline whole-file drift'))
    return before

def main():
    require(len(FILES) == 35, '35 exact ingress files')
    for relative, entry in FILES.items():
        text = (ROOT / relative).read_bytes().decode('utf-8')
        reverse_source(relative, text)
        for drift in (text + '\n', text.replace(entry['new'], entry['old'], 1), text.replace('.isEmpty()', '.isEmpty() || true', 1)):
            try:
                reverse_source(relative, drift)
            except AssertionError:
                pass
            else:
                raise AssertionError((relative, 'drift not rejected'))
    clears = [edit for entry in FILES.values() for edit in entry['edits'] if edit['kind'] == 'decrement-clear']
    require(len(clears) == 4, 'four exact portable-inventory native clear calls')
    for edit in clears:
        require(edit['old'].replace('setInventorySlotContents(slot, null);', 'setInventorySlotContents(slot, ItemStack.EMPTY);') == edit['new'], 'clear-only decrement source change')
    print('PASS: four exact native decrement clear calls; 35 setter/removal/clear-only whole-file reversals and 105 drift rejections; no getter/storage contract migration claim.')

if __name__ == '__main__':
    main()
