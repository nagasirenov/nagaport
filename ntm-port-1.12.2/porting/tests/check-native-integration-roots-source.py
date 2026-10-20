#!/usr/bin/env python3
"""Exact CP21 native JEI/Angel integration source guard and audit-only inverse.

Reverse this layer BEFORE bulk fields -> thermal -> furnace -> EMPTY history.
All existing-file UTF-8 bytes and introduced files are pinned; no Git objects,
pristine checkout, production rewrites, compilation exclusions or stubs required.
Introduced files stay present and validated in historical scans: they have no
earlier source to reconstruct, and are never replaced by empty source/shims.
This certifies source identity/reversibility, not compilation or gameplay.
"""
import hashlib
import json
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
MANIFEST_PATH = Path(__file__).with_name('native-integration-roots-source-manifest.json')
BASELINE = '5f720e0f82340aa29f2613ff71fc706689d7c3aa'
# Pin the reviewed specification as well as each input/output source byte stream.
MANIFEST_SHA256 = 'bb4954cc96e7c1a2288f550512d78f403d49eed0e84a9b036ba705ab98f36eef'


def require(ok, detail):
    if not ok:
        raise AssertionError(detail)


def digest(text):
    return hashlib.sha256(text.encode('utf-8')).hexdigest()


raw_manifest = MANIFEST_PATH.read_bytes()
require(hashlib.sha256(raw_manifest).hexdigest() == MANIFEST_SHA256,
        'Native integration source manifest drift')
MANIFEST = json.loads(raw_manifest.decode('utf-8'))
require(MANIFEST['baseline'] == BASELINE, 'Native integration baseline changed')
FILES = MANIFEST['files']
NEW_FILES = MANIFEST['new_files']
require(len(FILES) == 85 and len(NEW_FILES) == 13, 'Native integration file census drift')
require(not set(FILES) & set(NEW_FILES), 'Native integration file groups overlap')
require(all(p.startswith('src/main/java/') and p.endswith('.java') and '..' not in Path(p).parts
            for p in (*FILES, *NEW_FILES)), 'Native integration path escaped Java source')


def reverse_source(relative, text):
    """Return exact CP20 bytes; unknown paths pass through, new files stay pinned."""
    entry = FILES.get(relative)
    if entry is None:
        if relative in NEW_FILES:
            require(digest(text) == NEW_FILES[relative], (relative, 'introduced native source drift'))
        return text
    require(digest(text) == entry['after_sha256'], (relative, 'native integration whole-file drift'))
    for edit in reversed(entry['edits']):
        offset = edit['after_offset']
        new = edit['new']
        require(text[offset:offset + len(new)] == new, (relative, offset, 'native integration edit drift'))
        text = text[:offset] + edit['old'] + text[offset + len(new):]
    require(digest(text) == entry['before_sha256'], (relative, 'native integration baseline drift'))
    return text


def _forward_source(relative, text):
    entry = FILES[relative]
    require(digest(text) == entry['before_sha256'], (relative, 'native integration forward baseline drift'))
    for edit in reversed(entry['edits']):
        offset = edit['before_offset']
        old = edit['old']
        require(text[offset:offset + len(old)] == old, (relative, offset, 'native integration old edit drift'))
        text = text[:offset] + edit['new'] + text[offset + len(old):]
    require(digest(text) == entry['after_sha256'], (relative, 'native integration forward output drift'))
    return text


def verify(root=ROOT, verbose=True, source_transform=None):
    """Verify CP21 bytes, optionally after a newer exact audit-only inverse."""
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
                    (relative, 'native integration byte-exact round trip failed'))
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
                raise AssertionError((relative, 'native integration drift accepted'))
    require(reverse_source('unrelated.java', '\ufeffuntouched\r\n') == '\ufeffuntouched\r\n',
            'Unrelated source was normalized')
    if verbose:
        print('PASS native integration roots: 85 byte-exact existing-source inverses/round trips; '
              '13 pinned introduced files retained in historical scans; %d drift mutations rejected; '
              '%d CRLF inputs preserved.' % (rejected, crlf_files))
        print('SOURCE PARITY ONLY: no Git/pristine checkout, production writes, exclusions or stubs; '
              'not compilation/gameplay acceptance.')
    return MANIFEST


if __name__ == '__main__':
    require(not sys.flags.optimize, 'Run without -O so all composed historical assertions remain enabled')
    import importlib.util
    spec = importlib.util.spec_from_file_location('native_history_composition', ROOT / 'porting/tests/check-thermal-family-source.py')
    newer = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(newer)
    newer._core.verify()
    newer._compat.verify(source_transform=newer._core.reverse_source)
    verify(source_transform=newer.reverse_compat_source)
