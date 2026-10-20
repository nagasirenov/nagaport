#!/usr/bin/env python3
"""Exact CP22 static-item/material, optional JEI and native AE2 source guard.

Reverse this layer BEFORE CP21 roots -> bulk fields -> thermal -> furnace ->
EMPTY/callback/naming/sided history. All existing-file UTF-8 bytes and introduced
files are pinned; no Git objects, pristine checkout, production rewrites,
compilation exclusions or stubs are needed when running this guard.
Introduced files remain present and validated in historical scans: they have no
earlier source to reconstruct and are never replaced by empty source/shims.
This certifies source identity/reversibility, not compilation or gameplay.
"""
import hashlib
import json
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
MANIFEST_PATH = Path(__file__).with_name('native-render-compat-source-manifest.json')
BASELINE = '290af844793ced6c20380adfaa83aab764ae6197'
# Pin the reviewed specification as well as each input/output source byte stream.
MANIFEST_SHA256 = '50978279f3f8f67599e1db921c9a8b2ae418c14226da61a7a1cfe8d3bb01001a'


def require(ok, detail):
    if not ok:
        raise AssertionError(detail)


def digest(text):
    return hashlib.sha256(text.encode('utf-8')).hexdigest()


raw_manifest = MANIFEST_PATH.read_bytes()
require(hashlib.sha256(raw_manifest).hexdigest() == MANIFEST_SHA256,
        'Native render/compat source manifest drift')
MANIFEST = json.loads(raw_manifest.decode('utf-8'))
require(MANIFEST['baseline'] == BASELINE, 'Native render/compat baseline changed')
FILES = MANIFEST['files']
NEW_FILES = MANIFEST['new_files']
require(len(FILES) == 113 and len(NEW_FILES) == 13, 'Native render/compat file census drift')
require(not set(FILES) & set(NEW_FILES), 'Native render/compat file groups overlap')
require(all(p.startswith('src/main/java/') and p.endswith('.java') and '..' not in Path(p).parts
            for p in (*FILES, *NEW_FILES)), 'Native render/compat path escaped Java source')


def reverse_source(relative, text):
    """Return exact CP21 bytes; unknown paths pass through, new files stay pinned."""
    entry = FILES.get(relative)
    if entry is None:
        if relative in NEW_FILES:
            require(digest(text) == NEW_FILES[relative], (relative, 'introduced native source drift'))
        return text
    require(digest(text) == entry['after_sha256'], (relative, 'native render/compat whole-file drift'))
    for edit in reversed(entry['edits']):
        offset = edit['after_offset']
        new = edit['new']
        require(text[offset:offset + len(new)] == new, (relative, offset, 'native render/compat edit drift'))
        text = text[:offset] + edit['old'] + text[offset + len(new):]
    require(digest(text) == entry['before_sha256'], (relative, 'native render/compat baseline drift'))
    return text


def _forward_source(relative, text):
    entry = FILES[relative]
    require(digest(text) == entry['before_sha256'], (relative, 'native render/compat forward baseline drift'))
    for edit in reversed(entry['edits']):
        offset = edit['before_offset']
        old = edit['old']
        require(text[offset:offset + len(old)] == old, (relative, offset, 'native render/compat old edit drift'))
        text = text[:offset] + edit['new'] + text[offset + len(old):]
    require(digest(text) == entry['after_sha256'], (relative, 'native render/compat forward output drift'))
    return text


def verify(root=ROOT, verbose=True, source_transform=None):
    """Read-only verification, exact round trips and selected-file drift rejection."""
    root = Path(root)
    rejected = 0
    crlf_files = 0
    for relative in sorted(set(FILES) | set(NEW_FILES)):
        current = (root / relative).read_bytes().decode('utf-8')
        if source_transform is not None:
            current = source_transform(relative, current)
        before = reverse_source(relative, current)
        if relative in FILES:
            require(_forward_source(relative, before).encode('utf-8') == current.encode('utf-8'),
                    (relative, 'native render/compat byte-exact round trip failed'))
        else:
            require(before == current, (relative, 'introduced source was excluded from audit'))
        crlf_files += '\r\n' in current
        mutations = (current + '\n', current[:-1], current.replace('\r\n', '\n')
                     if '\r\n' in current else current.replace('\n', '\r\n'))
        for mutant in mutations:
            require(mutant != current, (relative, 'ineffective drift mutation'))
            try:
                reverse_source(relative, mutant)
            except AssertionError:
                rejected += 1
            else:
                raise AssertionError((relative, 'native render/compat drift accepted'))
    require(reverse_source('unrelated.java', '\ufeffuntouched\r\n') == '\ufeffuntouched\r\n',
            'Unrelated source was normalized')
    if verbose:
        print('PASS native render/compat: 113 byte-exact existing-source inverses/round trips; '
              '13 pinned introduced files retained in historical scans; %d drift mutations rejected; '
              '%d CRLF inputs preserved.' % (rejected, crlf_files))
        print('SOURCE PARITY ONLY: no Git/pristine checkout, production writes, exclusions or stubs; '
              'not compilation/gameplay acceptance.')
    return MANIFEST


if __name__ == '__main__':
    require(not sys.flags.optimize, 'Run without -O so all composed historical assertions remain enabled')
    import importlib.util
    spec = importlib.util.spec_from_file_location('native_core_lifecycle', ROOT / 'porting/tests/check-native-core-lifecycle-source.py')
    newer = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(newer)
    newer.verify()
    verify(source_transform=newer.reverse_source)
