#!/usr/bin/env python3
"""Exact CP23 thermal block, entity capability and native particle source guard.

Reverse this layer BEFORE CP22 then CP21 roots -> bulk fields -> thermal -> furnace ->
EMPTY/callback/naming/sided history. All existing-file UTF-8 bytes and introduced
files are pinned; no Git objects, pristine checkout, production rewrites,
compilation exclusions or stubs are needed when running this guard.
Introduced files remain present and validated in historical scans: they have no
earlier source to reconstruct and are never replaced by empty source/shims.
This certifies source identity/reversibility, not compilation or gameplay.
"""
import hashlib
import importlib.util
import json
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
_particle_spec = importlib.util.spec_from_file_location('native_owned_particles_source', ROOT / 'porting/tests/check-native-owned-particles-source.py')
_particles = importlib.util.module_from_spec(_particle_spec)
_particle_spec.loader.exec_module(_particles)
MANIFEST_PATH = Path(__file__).with_name('native-core-lifecycle-source-manifest.json')
BASELINE = 'f711b25320cdaaffba8594c565cfc6c87aea3257'
# Pin the reviewed specification as well as each input/output source byte stream.
MANIFEST_SHA256 = '5502739b84250490dcb88662667c980b3cf81bede5a2280ed3855a60f867d7ff'


def require(ok, detail):
    if not ok:
        raise AssertionError(detail)


def digest(text):
    return hashlib.sha256(text.encode('utf-8')).hexdigest()


raw_manifest = MANIFEST_PATH.read_bytes()
require(hashlib.sha256(raw_manifest).hexdigest() == MANIFEST_SHA256,
        'Native core/lifecycle source manifest drift')
MANIFEST = json.loads(raw_manifest.decode('utf-8'))
require(MANIFEST['baseline'] == BASELINE, 'Native core/lifecycle baseline changed')
FILES = MANIFEST['files']
NEW_FILES = MANIFEST['new_files']
RETIRED_FILES = MANIFEST['retired_files']
require(set(RETIRED_FILES) == {'src/main/java/com/hbm/render/block/RenderDiFurnaceExtension.java'}, 'Retired source scope drift')
require(len(FILES) == 20 and len(NEW_FILES) == 9, 'Native core/lifecycle file census drift')
require(not set(FILES) & set(NEW_FILES), 'Native core/lifecycle file groups overlap')
require(all(p.startswith('src/main/java/') and p.endswith('.java') and '..' not in Path(p).parts
            for p in (*FILES, *NEW_FILES)), 'Native core/lifecycle path escaped Java source')


def reverse_source(relative, text):
    """Return exact CP22 bytes; unknown paths pass through, new files stay pinned."""
    text = _particles.reverse_source(relative, text)
    entry = FILES.get(relative)
    if entry is None:
        if relative in NEW_FILES:
            require(digest(text) == NEW_FILES[relative], (relative, 'introduced native source drift'))
        return text
    require(digest(text) == entry['after_sha256'], (relative, 'native core/lifecycle whole-file drift'))
    for edit in reversed(entry['edits']):
        offset = edit['after_offset']
        new = edit['new']
        require(text[offset:offset + len(new)] == new, (relative, offset, 'native core/lifecycle edit drift'))
        text = text[:offset] + edit['old'] + text[offset + len(new):]
    require(digest(text) == entry['before_sha256'], (relative, 'native core/lifecycle baseline drift'))
    return text


def _forward_source(relative, text):
    entry = FILES[relative]
    require(digest(text) == entry['before_sha256'], (relative, 'native core/lifecycle forward baseline drift'))
    for edit in reversed(entry['edits']):
        offset = edit['before_offset']
        old = edit['old']
        require(text[offset:offset + len(old)] == old, (relative, offset, 'native core/lifecycle old edit drift'))
        text = text[:offset] + edit['new'] + text[offset + len(old):]
    require(digest(text) == entry['after_sha256'], (relative, 'native core/lifecycle forward output drift'))
    return text


def verify(root=ROOT, verbose=True):
    """Read-only verification, exact round trips and selected-file drift rejection."""
    root = Path(root)
    _particles.verify(root, verbose)
    # This old renderer was genuinely replaced, not excluded from compilation.
    # Keep exact retired source evidence without restoring/writing a fake class.
    for relative, record in RETIRED_FILES.items():
        require(not (root / relative).exists(), (relative, 'retired legacy renderer reintroduced'))
        require(digest(record['before_text']) == record['before_sha256'], (relative, 'retired source evidence drift'))
        for successor, expected in record['successors'].items():
            require(digest((root / successor).read_bytes().decode('utf-8')) == expected, (successor, 'native replacement drift'))
    rejected = 0
    crlf_files = 0
    for relative in sorted(set(FILES) | set(NEW_FILES)):
        current = (root / relative).read_bytes().decode('utf-8')
        before = reverse_source(relative, current)
        if relative in FILES:
            require(_forward_source(relative, before).encode('utf-8') == _particles.reverse_source(relative, current).encode('utf-8'),
                    (relative, 'native core/lifecycle byte-exact round trip failed'))
        else:
            require(before == _particles.reverse_source(relative, current), (relative, 'introduced source was excluded from audit'))
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
                raise AssertionError((relative, 'native core/lifecycle drift accepted'))
    require(reverse_source('unrelated.java', '\ufeffuntouched\r\n') == '\ufeffuntouched\r\n',
            'Unrelated source was normalized')
    if verbose:
        print('PASS native core/lifecycle: 20 byte-exact existing-source inverses/round trips; '
              '9 pinned introduced files retained in historical scans; %d drift mutations rejected; '
              '%d CRLF inputs preserved.' % (rejected, crlf_files))
        print('PASS one explicitly retired legacy renderer; exact original evidence and two native successors pinned.')
        print('SOURCE PARITY ONLY: no Git/pristine checkout, production writes, exclusions or stubs; '
              'not compilation/gameplay acceptance.')
    return MANIFEST


if __name__ == '__main__':
    require(not sys.flags.optimize, 'Run without -O so all composed historical assertions remain enabled')
    verify()
