#!/usr/bin/env python3
"""Fail-closed exact UTF-8 byte delta reversal; not semantic acceptance."""
import argparse
import hashlib
import json
from pathlib import Path

MANIFEST = json.loads(Path(__file__).with_name('machine-consumer-source-manifest.json').read_bytes().decode('utf-8'))
FILES = {entry['path']: entry for entry in MANIFEST['files']}

def sha(text):
    return hashlib.sha256(text.encode('utf-8')).hexdigest()

def reverse_source(relative, text):
    relative = str(relative).replace('\\', '/')
    entry = FILES[relative]
    assert sha(text) == entry['after_sha256'], 'Unexpected source drift: ' + relative
    lines = text.splitlines(True)
    for hunk in reversed(entry['hunks']):
        start, new = hunk['after_start'], hunk['after']
        assert lines[start:start + len(new)] == new, 'Hunk mismatch: ' + relative
        lines[start:start + len(new)] = hunk['before']
    original = ''.join(lines)
    assert sha(original) == entry['before_sha256'], 'Reversal mismatch: ' + relative
    return original

def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--root', type=Path, default=Path(__file__).resolve().parents[2])
    args = parser.parse_args()
    for relative in FILES:
        reverse_source(relative, (args.root / relative).read_bytes().decode('utf-8'))
    print('PASS: exact UTF-8 byte reverse reconstruction of ' + str(len(FILES)) + ' consumer files; no unexplained delta.')

if __name__ == '__main__':
    main()
