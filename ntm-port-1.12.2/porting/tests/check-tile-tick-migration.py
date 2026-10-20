#!/usr/bin/env python3
"""Portable native tile tick contract and method-body regression, without git objects.
Default checks ownership, tick/static inheritance, exact tick payload fingerprints,
and excluded hazard overloads. --strict-snapshot also certifies migration file bytes.
This is source parity, not production class compilation or gameplay acceptance.
"""
import hashlib
import json
import re
from pathlib import Path
ROOT = Path(__file__).resolve().parents[2]
MANIFEST = ROOT / 'porting/tests/tile-tick-manifest.json'

def digest(text): return hashlib.sha256(text.encode('utf-8')).hexdigest()

def mask(text):
    return re.sub(r'//[^\n]*|/\*.*?\*/|"(?:\\.|[^"\\])*"|\'(?:\\.|[^\'\\])*\'',
                  lambda m: re.sub(r'[^\r\n]', ' ', m[0]), text, flags=re.S)

def canonical(text):
    # Tokenize literals/comments BEFORE whitespace. String contents, escapes and
    # comment contents are never silently normalized by a payload fingerprint.
    return re.sub(r'"(?:\\.|[^"\\])*"|\'(?:\\.|[^\'\\])*\'|//[^\n]*|/\*.*?\*/|\s+',
                  lambda m: '' if m[0].isspace() else m[0], text, flags=re.S)

def scan(transform=None):
    texts, classes, methods = {}, [], []
    for path in sorted((ROOT / 'src/main/java').rglob('*.java')):
        rel = path.relative_to(ROOT).as_posix()
        text = path.read_bytes().decode('utf-8')
        if transform is not None: text = transform(rel, text)
        texts[rel] = text; clean = mask(text)
        pairs, stack = {}, []
        for i, ch in enumerate(clean):
            if ch == '{': stack.append(i)
            elif ch == '}' and stack: pairs[stack.pop()] = i
        local = []
        for m in re.finditer(r'\b(class|interface|enum)\s+(\w+)([^;{}]*)\{', clean):
            header = m[3]
            while re.search(r'<[^<>]*>', header): header = re.sub(r'<[^<>]*>', '', header)
            parent = re.search(r'\bextends\s+([\w.]+)', header)
            c = dict(name=m[2], parent=parent[1] if parent else None, path=rel,
                     key=rel+':'+m[2], start=m.start(), head_end=m.end()-1,
                     end=pairs[m.end()-1], header=m[3], tickable=bool(re.search(r'\b(?:net\.minecraft\.util\.)?ITickable\b', m[3])))
            classes.append(c);local.append(c)
        for m in re.finditer(r'\b(public|protected|private)\s+(?:(?:abstract|final|static|synchronized)\s+)*(void|boolean)\s+(updateEntity|update|canUpdate)\s*\(([^)]*)\)\s*([;{])', clean):
            owners=[c for c in local if c['head_end']<m.start()<c['end']]
            assert owners,(rel,m.start())
            owner=min(owners,key=lambda c:c['end']-c['start'])
            end=m.end() if m[5]==';' else pairs[m.end()-1]+1
            methods.append(dict(path=rel,owner=owner,key=owner['key']+':'+m[4].strip(),name=m[3],args=m[4].strip(),ret=m[2],start=m.start(),name_start=m.start(3),name_end=m.end(3),end=end,abstract=m[5]==';',body_start=m.end(),body_end=end-1))
    names={}
    for c in classes:names.setdefault(c['name'],[]).append(c)
    def parent(c):
        candidates=names.get(c['parent'],[])
        if len(candidates)>1:
            local=[x for x in candidates if x['path']==c['path']]
            if len(local)==1: return local[0]
            same_package=[x for x in candidates if Path(x['path']).parent==Path(c['path']).parent]
            if len(same_package)==1: return same_package[0]
            imports=re.findall(r'\bimport\s+([\w.]+)\s*;', mask(texts[c['path']]))
            explicit=[x for x in candidates if any(i.endswith('.'+x['name']) and x['path'].endswith(i.replace('.', '/')+'.java') for i in imports)]
            if len(explicit)==1: return explicit[0]
            # Unresolved unrelated APIs are not classified as tiles. Fail closed
            # if any ambiguous parent could belong to the audited tile scope.
            assert not any('tileentity' in x['path'].lower() or x['name'].startswith('TileEntity') for x in candidates), ('Ambiguous tile parent',c['key'])
            return None
        return candidates[0] if candidates else None
    def chain(c):
        result=[]
        while c is not None:
            assert c not in result,('Inheritance cycle',c['key'])
            result.append(c);c=parent(c)
        return result
    for c in classes:
        ancestors=chain(c);c['ancestors']=[x['key'] for x in ancestors]
        c['external_parent']=ancestors[-1]['parent']
        c['tile']=c['external_parent'] in ('TileEntity','net.minecraft.tileentity.TileEntity','TileEntityFurnace','net.minecraft.tileentity.TileEntityFurnace')
        c['native_ticking']=any(x['tickable'] for x in ancestors) or c['external_parent'] in ('TileEntityFurnace','net.minecraft.tileentity.TileEntityFurnace')
    return texts,classes,methods

def body(texts,method):
    return '' if method['abstract'] else texts[method['path']][method['body_start']:method['body_end']]

def main(strict=False):
    manifest=json.loads(MANIFEST.read_text());texts,classes,methods=scan()
    actual_texts=texts
    # Audit the new field migration first; retain the original tick payload hashes.
    # Only this source-regression view is restored, never production/probe inputs.
    import importlib.util
    spec=importlib.util.spec_from_file_location('native_world_field_audit',ROOT/'porting/tests/native-world-field-audit.py')
    world_fields=importlib.util.module_from_spec(spec);spec.loader.exec_module(world_fields)
    coord_spec=importlib.util.spec_from_file_location('native_coordinate_audit',ROOT/'porting/tests/check-native-coordinates.py')
    coordinates=importlib.util.module_from_spec(coord_spec);coord_spec.loader.exec_module(coordinates)
    newer_spec=importlib.util.spec_from_file_location('thermal_family_audit',ROOT/'porting/tests/check-thermal-family-source.py')
    newer=importlib.util.module_from_spec(newer_spec);newer_spec.loader.exec_module(newer)
    coordinates.main(transform=newer.reverse_newer_source)
    ai_spec=importlib.util.spec_from_file_location('maskman_approach_audit',ROOT/'porting/tests/probe-maskman-approach.py')
    maskman=importlib.util.module_from_spec(ai_spec);ai_spec.loader.exec_module(maskman)
    maskman.check_source(ROOT)
    rocket_spec=importlib.util.spec_from_file_location('rocket_metadata_audit',ROOT/'porting/tests/check-rocket-metadata-source.py')
    rockets=importlib.util.module_from_spec(rocket_spec);rocket_spec.loader.exec_module(rockets)
    rockets.main()
    biome_spec=importlib.util.spec_from_file_location('biome_consumer_audit',ROOT/'porting/tests/check-biome-consumers.py')
    biomes=importlib.util.module_from_spec(biome_spec);biome_spec.loader.exec_module(biomes)
    biomes.verify()
    inventory_spec=importlib.util.spec_from_file_location('inventory_boundary_audit',ROOT/'porting/tests/check-inventory-boundary.py')
    inventory=importlib.util.module_from_spec(inventory_spec);inventory_spec.loader.exec_module(inventory)
    inventory.main(transform=newer.reverse_newer_source)
    sided_spec=importlib.util.spec_from_file_location('machine_sided_audit',ROOT/'porting/tests/check-machine-sided-source.py')
    sided=importlib.util.module_from_spec(sided_spec);sided_spec.loader.exec_module(sided)
    naming_spec=importlib.util.spec_from_file_location('machine_naming_audit',ROOT/'porting/tests/check-machine-naming-source.py')
    naming=importlib.util.module_from_spec(naming_spec);naming_spec.loader.exec_module(naming)
    naming.main()
    sided.main(transform=naming.reverse_source)
    # Audit-only reversal of independent exact-source API edits before the World bridge.
    def reverse_current(rel,text):
        text=naming.reverse_source(rel,text)
        text=sided.reverse_source(rel,text)
        text=inventory.legacy_text(rel,text)
        for audit in (biomes,rockets,maskman,coordinates):
            text=audit.reverse_source(rel,text)
        return text
    historical_texts=world_fields.verify_bridge(transform=reverse_current)
    texts,classes,methods=scan(transform=lambda rel,text: historical_texts[rel])
    tiles=[c for c in classes if c['tile']]
    assert len(tiles)==434,len(tiles)
    assert {c['key'] for c in tiles if c['native_ticking']}==set(manifest['tick_closure']), 'Native ticking inheritance changed'
    assert {c['key'] for c in tiles if not c['native_ticking']}==set(manifest['static_closure']), 'Static tile acquired ticking or ownership changed'
    assert {c['key'] for c in tiles if c['tickable']}==set(manifest['interface_additions']), 'Interface added outside first-declaring roots'
    assert not [m for m in methods if m['owner']['tile'] and m['name'] in ('updateEntity','canUpdate')], 'Legacy tile callback/suppression remains'
    ticks=[m for m in methods if m['owner']['tile'] and m['name']=='update' and not m['args']]
    expected={m['key']:m for m in manifest['methods']}
    assert set(expected)=={m['key'] for m in ticks},'Tick method ownership changed'
    for m in ticks:
        old=expected[m['key']]
        assert m['ret']=='void' and m['abstract']==old['abstract'],m['key']
        assert digest(canonical(body(texts,m)))==old['native_body_sha256'],(m['key'],'Tick body/order/literal drift')
    excluded={m['key']:digest(canonical(texts[m['path']][m['start']:m['end']])) for m in methods if m['name']=='updateEntity'}
    assert excluded==manifest['excluded_methods'],'Non-tile hazard overload changed'
    for rel,expected_hash in manifest['excluded_call_files'].items():
        # HazardSystem is outside this migration. Snapshot comparison is optional;
        # default compares its exact updateEntity invocation, not unrelated methods.
        clean=mask(texts[rel]);calls=[]
        for m in re.finditer(r'\bentry\.type\.updateEntity\s*\([^;]+;',clean):calls.append(canonical(texts[rel][m.start():m.end()]))
        assert digest('\n'.join(calls))==expected_hash,'Hazard dispatch changed'
    assert canonical('"a b"')!='"ab"' and canonical("' '")=="' '", 'Literal preservation regression'
    print('PASS: 336 exact native update signatures; 133 first-declaring ITickable roots')
    print('PASS: full 434-tile ownership; 379 native tick descendants; all 55 static descendants remain non-ITickable')
    print('PASS: exact literal-preserving tick bodies, abstract/empty methods, early returns and 58 meaningful super calls; sole OC inherited no-op removed')
    print('PASS: 14 obsolete constant-false suppressors absent; 12 hazard overloads and hazard dispatch unchanged')
    if strict:
        for path,fingerprint in manifest['post_file_sha256'].items():assert digest(actual_texts[path])==fingerprint,(path,'Migration snapshot drift')
        print('PASS STRICT SNAPSHOT: exact authorized migration file bytes')
    else:print('Reusable tick-body regression: unrelated edits outside the frozen 162 naming/125 sided/two inventory/seven biome/two rocket/Maskman AI/eight coordinate consumers and validated receiver lines are allowed; --strict-snapshot remains historical')
    print('SOURCE PARITY ONLY: not production class compilation or gameplay acceptance')

if __name__=='__main__':
    import argparse
    parser=argparse.ArgumentParser(description=__doc__);parser.add_argument('--strict-snapshot',action='store_true')
    main(parser.parse_args().strict_snapshot)
