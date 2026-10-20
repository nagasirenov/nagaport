#!/usr/bin/env python3
"""Portable, fail-closed source parity check; hashes certify source, not gameplay.
Manifest contains pre-migration body and outside-method fingerprints, independent
of git objects or a pristine sibling checkout. Only return plumbing is normalized.
"""
import hashlib
import json
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
MANIFEST = ROOT / 'porting/tests/tile-nbt-manifest.json'

def digest(value):
    return hashlib.sha256(value.encode('utf-8')).hexdigest()

def mask(text):
    return re.sub(r'//[^\n]*|/\*.*?\*/|"(?:\\.|[^"\\])*"|\'(?:\\.|[^\'\\])*\'',
                  lambda m: re.sub(r'[^\n]', ' ', m[0]), text, flags=re.S)

def scan(transform=None):
    classes, methods, texts = [], [], {}
    for path in sorted((ROOT / 'src/main/java').rglob('*.java')):
        rel = path.relative_to(ROOT).as_posix()
        text = path.read_bytes().decode('utf-8')
        if transform is not None: text = transform(rel, text)
        texts[rel] = text
        clean = mask(text)
        pairs, stack = {}, []
        for i, char in enumerate(clean):
            if char == '{': stack.append(i)
            elif char == '}' and stack: pairs[stack.pop()] = i
        local = []
        for match in re.finditer(r'\b(class|interface|enum)\s+(\w+)([^;{}]*)\{', clean):
            parent = re.search(r'\bextends\s+([\w.]+)', match[3])
            entry = dict(name=match[2], parent=parent[1] if parent else None,
                         path=rel, start=match.end()-1, end=pairs[match.end()-1])
            local.append(entry); classes.append(entry)
        for match in re.finditer(r'\b(public|protected|private)\s+(?:static\s+)?(\w+)\s+writeToNBT\s*\(([^)]*)\)\s*\{', clean):
            owners = [c for c in local if c['start'] < match.start() < c['end']]
            assert owners, (rel, match.start())
            owner = min(owners, key=lambda c: c['end']-c['start'])
            methods.append(dict(path=rel, owner=owner, ret=match[2], args=match[3].strip(),
                                start=match.start(), ret_start=match.start(2), ret_end=match.end(2),
                                body_start=match.end(), body_end=pairs[match.end()-1]))
    names = {}
    for c in classes: names.setdefault(c['name'], []).append(c)
    def is_tile(c, seen=()):
        if c['name'] in seen: return False
        if c['parent'] in ('TileEntity', 'net.minecraft.tileentity.TileEntity', 'TileEntityFurnace'):
            return True
        candidates = names.get(c['parent'], [])
        if len(candidates) > 1:
            assert not any(is_tile(p, seen+(c['name'],)) for p in candidates), 'Ambiguous tile parent'
        return any(is_tile(p, seen+(c['name'],)) for p in candidates)
    for method in methods:
        method['tile'] = is_tile(method['owner'])
        method['key'] = method['path'] + ':' + method['owner']['name'] + ':' + method['args']
    return texts, methods, sum(is_tile(c) for c in classes)

def canonical_body(body):
    # Match literals/comments before whitespace so NBT keys and string payloads
    # retain every byte, including embedded spaces and escaped quote characters.
    tokens = r'"(?:\\.|[^"\\])*"|\'(?:\\.|[^\'\\])*\'|//[^\n]*|/\*.*?\*/|\s+'
    return re.sub(tokens, lambda m: '' if m[0].isspace() else m[0], body, flags=re.S)

def main(strict=False):
    manifest = json.loads(MANIFEST.read_text())
    actual_texts, _, _ = scan()
    import importlib.util
    spec=importlib.util.spec_from_file_location('inventory_boundary_audit',ROOT/'porting/tests/check-inventory-boundary.py')
    inventory=importlib.util.module_from_spec(spec);spec.loader.exec_module(inventory)
    newer_spec=importlib.util.spec_from_file_location('thermal_family_audit',ROOT/'porting/tests/check-thermal-family-source.py')
    newer=importlib.util.module_from_spec(newer_spec);newer_spec.loader.exec_module(newer)
    inventory.main(transform=newer.reverse_newer_source)
    sided_spec=importlib.util.spec_from_file_location('machine_sided_audit',ROOT/'porting/tests/check-machine-sided-source.py')
    sided=importlib.util.module_from_spec(sided_spec);sided_spec.loader.exec_module(sided)
    naming_spec=importlib.util.spec_from_file_location('machine_naming_audit',ROOT/'porting/tests/check-machine-naming-source.py')
    naming=importlib.util.module_from_spec(naming_spec);naming_spec.loader.exec_module(naming)
    naming.main()
    sided.main(transform=naming.reverse_source)
    texts, methods, count = scan(transform=lambda rel,text: inventory.legacy_text(rel,sided.reverse_source(rel,naming.reverse_source(rel,text))))
    tiles = [m for m in methods if m['tile']]
    assert count == 434 and len(tiles) == 297, (count, len(tiles))
    expected = {m['key']: m for m in manifest['methods']}
    assert set(expected) == {m['key'] for m in tiles}, 'Ownership/method set changed'
    for method in tiles:
        old = expected[method['key']]
        assert method['ret'] == 'NBTTagCompound', method['key']
        body = texts[method['path']][method['body_start']:method['body_end']]
        parameter = method['args'].split()[-1]
        terminal = re.search(r'\breturn\s+'+re.escape(parameter)+r'\s*;\s*$', body)
        assert terminal, (method['key'], 'Missing terminal return')
        # Preserve all original text except whitespace and specified return changes.
        body = body[:terminal.start()]
        if old['early_returns']:
            assert old['early_returns'] == 1 and method['owner']['name'] == 'TileEntityCustomMachine'
            body, replaced = re.subn(r'\breturn\s+'+re.escape(parameter)+r'\s*;', 'return;', body)
            assert replaced == 1
        assert digest(canonical_body(body)) == old['body_sha256'], (method['key'], 'Payload changed')
    print('PASS: 434 named TileEntity descendants; 297 exact native return signatures in 297 files')
    print('PASS: portable pre-change method payload fingerprints and sole early return')
    if strict:
        # Snapshot audit only: future unrelated edits should use the default check.
        for path, fingerprint in manifest['post_file_sha256'].items():
            assert digest(actual_texts[path]) == fingerprint, (path, 'Outside authorized patch or byte drift')
        excluded = {m['key']: digest(texts[m['path']][m['start']:m['body_end']+1])
                    for m in methods if not m['tile'] and m['ret'] == 'void'}
        assert excluded == manifest['excluded_void_methods'], 'Unrelated void serializer changed'
        print('PASS STRICT SNAPSHOT: exact authorized file bytes; 37 unrelated serializers untouched; read/ticking/null semantics unchanged')
    else:
        print('Reusable method regression: 162 naming/125 sided and two inventory files are exact-snapshot validated then reversed for historical payload audit; unrelated file edits are not checked; use --strict-snapshot for migration audit')
    print('SOURCE PARITY ONLY: not production class compilation or machine gameplay acceptance')

if __name__ == '__main__':
    import argparse
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--strict-snapshot', action='store_true', help='Also require exact migration file bytes and untouched excluded serializers')
    main(parser.parse_args().strict_snapshot)
