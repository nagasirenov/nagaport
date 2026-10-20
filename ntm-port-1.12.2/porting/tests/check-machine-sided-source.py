#!/usr/bin/env python3
"""Fail-closed, whole-file native sided migration proof. No Git or network required.

Apply reverse_source before older coordinate/world/tick frozen-source checks.
Only ISidedInventory signatures, their explicit legacy-side locals, and the 36
reviewed native call expressions are reversible edits. Other content is frozen.
This is source parity, not complete machine compilation or gameplay acceptance.
"""
import collections
import hashlib
import json
from pathlib import Path
import re

ROOT = Path(__file__).resolve().parents[2]
MANIFEST = json.loads((ROOT / 'porting/tests/machine-sided-source-manifest.json').read_text(encoding='utf-8'))
FILES = {entry['path']: entry for entry in MANIFEST['files']}
EXPECTED = {'getAccessibleSlotsFromSide': 118, 'canInsertItem': 46, 'canExtractItem': 109}


def sha(text):
    return hashlib.sha256(text.encode('utf-8')).hexdigest()


def require(condition, detail):
    if not condition:
        raise AssertionError(detail)


def reverse_source(relative, text):
    """Return exact pre-migration source only when the entire migrated file matches."""
    entry = FILES.get(relative)
    if entry is None:
        return text
    require(sha(text) == entry['migrated_sha256'], (relative, 'sided migrated whole-file drift'))
    for edit in reversed(entry['edits']):
        offset = edit['offset']
        require(text[offset:offset + len(edit['new'])] == edit['new'],
                (relative, 'sided edit mismatch', edit['kind'], offset))
        text = text[:offset] + edit['old'] + text[offset + len(edit['new']):]
    require(sha(text) == entry['baseline_sha256'], (relative, 'sided baseline whole-file drift'))
    return text


def mask(text):
    return re.sub(r'//[^\r\n]*|/\*[\s\S]*?\*/|"(?:\\.|[^"\\])*"|\'(?:\\.|[^\'\\])*\'',
                  lambda m: ''.join(c if c in '\r\n' else ' ' for c in m.group()), text)


def main(transform=None):
    require(len(FILES) == 125, '125 frozen source files')
    require(dict(collections.Counter(x['method'] for x in MANIFEST['declarations'])) == EXPECTED,
            '273 typed ordinary declaration census')
    require(len({x['path'] for x in MANIFEST['declarations']}) == 120, '120 declaration files')
    require(len(MANIFEST['calls']) == 36, '36 explicit native call expressions')
    require(len({x['path'] for x in MANIFEST['calls']}) == 14, '14 caller files')
    require('src/main/java/com/hbm/tileentity/TileEntityInventoryBase.java' not in FILES, 'InventoryBase frozen')
    require('src/main/java/com/hbm/util/ItemStackUtil.java' not in FILES, 'independent ItemStackUtil owner')
    reversed_files = 0
    for relative in FILES:
        text = (ROOT / relative).read_bytes().decode('utf-8')
        if transform is not None: text = transform(relative, text)
        reverse_source(relative, text)
        reversed_files += 1
        try:
            reverse_source(relative, text + '\n')
        except AssertionError:
            pass
        else:
            raise AssertionError((relative, 'drift not rejected'))
    # Typed census: retain coordinate overloads; no legacy ordinary native signature remains.
    old = []
    coordinate = collections.Counter()
    for path in (ROOT / 'src/main/java').rglob('*.java'):
        source = mask(path.read_bytes().decode('utf-8'))
        for match in re.finditer(r'\bpublic\s+(?:(?:final|abstract|default)\s+)*(?:int\s*\[\s*\]|boolean)\s+(getAccessibleSlotsFromSide|canInsertItem|canExtractItem)\s*\(([^)]*)\)', source):
            name, args = match.groups()
            parameters = args.split(',')
            ordinary = 1 if name == 'getAccessibleSlotsFromSide' else 3
            if len(parameters) == ordinary and re.fullmatch(r'\s*int\s+\w+\s*', parameters[-1]):
                old.append((path.relative_to(ROOT).as_posix(), name))
            if len(parameters) == ordinary + 3:
                coordinate[name] += 1
    require(not old, ('legacy ordinary sided declarations remain', old))
    require(dict(coordinate) == {'getAccessibleSlotsFromSide': 7, 'canInsertItem': 5, 'canExtractItem': 7},
            ('coordinate-domain declarations drift', dict(coordinate)))
    print('PASS: 273 native sided declaration mappings; 36 explicit call mappings; 125 whole-file reversals and 125 drift rejections; 19 coordinate-domain declarations retained. Not full machines/gameplay.')


if __name__ == '__main__':
    import importlib.util
    spec = importlib.util.spec_from_file_location('machine_naming_audit', ROOT/'porting/tests/check-machine-naming-source.py')
    naming = importlib.util.module_from_spec(spec); spec.loader.exec_module(naming)
    naming.main()
    main(transform=naming.reverse_source)
