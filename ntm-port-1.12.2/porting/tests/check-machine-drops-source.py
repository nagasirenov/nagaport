#!/usr/bin/env python3
"""Fail-closed whole-file reversal for 35 reviewed drop/serialization paths.
Compose reverse_source before earlier source audits. No Git or /tmp dependency.
"""
import hashlib
import json
from pathlib import Path
ROOT = Path(__file__).resolve().parents[2]
MANIFEST = json.loads((ROOT / 'porting/tests/machine-drops-source-manifest.json').read_text(encoding='utf-8'))
FILES = {entry['path']: entry for entry in MANIFEST['files']}

def sha(text):
    return hashlib.sha256(text.encode('utf-8')).hexdigest()

def reverse_source(relative, text):
    entry = FILES.get(relative)
    if entry is None:
        return text
    assert sha(text) == entry['after_sha256'], (relative, 'drop migrated whole-file drift')
    lines = text.splitlines(keepends=True)
    for edit in reversed(entry['edits']):
        start, end = edit['after_start'], edit['after_end']
        assert ''.join(lines[start:end]) == edit['new'], (relative, 'drop slice drift')
        lines[start:end] = edit['old'].splitlines(keepends=True)
    before = ''.join(lines)
    assert sha(before) == entry['before_sha256'], (relative, 'drop baseline whole-file drift')
    return before

def main():
    assert len(FILES) == 35, '35 exact audited drop files'
    rejected = 0
    for relative, entry in FILES.items():
        text = (ROOT / relative).read_bytes().decode('utf-8')
        before = reverse_source(relative, text)
        for drift in (text + '\n', before, text.replace('.isEmpty()', '.isEmpty() || true', 1)):
            try:
                reverse_source(relative, drift)
            except AssertionError:
                rejected += 1
            else:
                raise AssertionError((relative, 'drift not rejected'))
    print('PASS: 35 drop whole-file reversals and %d drift rejections; baseline snapshots persisted, no Git dependency.' % rejected)

if __name__ == '__main__':
    main()
