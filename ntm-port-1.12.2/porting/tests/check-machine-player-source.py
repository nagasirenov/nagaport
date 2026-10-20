#!/usr/bin/env python3
"""Exact callback-only source closure. Reverse before naming, sided and older audits.
Whole-file fingerprints and per-edit slices reject unrelated changes. No Git required.
This is source parity, not full machines, gameplay, resource reload or audible sound.
"""
import hashlib
import importlib.util
import json
from pathlib import Path
import re
ROOT = Path(__file__).resolve().parents[2]
# Reverse newest EMPTY layer first; historical callback fingerprints remain frozen.
_empty_spec=importlib.util.spec_from_file_location('machine_empty_audit',ROOT/'porting/tests/check-machine-empty-boundary.py')
_empty=importlib.util.module_from_spec(_empty_spec);_empty_spec.loader.exec_module(_empty)
MANIFEST = json.loads((ROOT/'porting/tests/machine-player-source-manifest.json').read_text(encoding='utf-8'))
FILES = {entry['path']:entry for entry in MANIFEST['files']}
def require(ok, detail):
    if not ok: raise AssertionError(detail)
def sha(text): return hashlib.sha256(text.encode('utf-8')).hexdigest()
def reverse_source(relative, text):
    text=_empty.reverse_source(relative,text)
    entry=FILES.get(relative)
    if entry is None: return text
    require(sha(text)==entry['migrated_sha256'], (relative,'player callbacks migrated whole-file drift'))
    for edit in reversed(entry['edits']):
        at=edit['offset'];new=edit['new']
        require(text[at:at+len(new)]==new,(relative,edit['kind'],'callback edit mismatch'))
        text=text[:at]+edit['old']+text[at+len(new):]
    require(sha(text)==entry['baseline_sha256'], (relative,'player callbacks baseline whole-file drift'))
    return text

def main():
    require(len(FILES)==147,'147 exact callback source files')
    for p in FILES:
        text=(ROOT/p).read_bytes().decode('utf-8')
        reverse_source(p,text)
        try: reverse_source(p,text+'\n')
        except AssertionError: pass
        else: raise AssertionError((p,'drift not rejected'))
    spec=importlib.util.spec_from_file_location('player_closure',ROOT/'porting/tests/check-tile-tick-migration.py')
    scan=importlib.util.module_from_spec(spec);spec.loader.exec_module(scan)
    texts,classes,_=scan.scan()
    base='src/main/java/com/hbm/tileentity/TileEntityMachineBase.java:TileEntityMachineBase'
    closure=[c for c in classes if base in c['ancestors']]
    require(len(closure)==183,'root + 182 descendants')
    declarations={name:[] for name in ('isUsableByPlayer','openInventory','closeInventory')}
    selfcalls=[]
    for c in closure:
        clean=scan.mask(texts[c['path']])
        for m in re.finditer(r'\b(isUseableByPlayer|isUsableByPlayer|openInventory|closeInventory)\s*\(([^)]*)\)',clean):
            owners=[o for o in classes if o['path']==c['path'] and o['head_end']<m.start()<o['end']]
            if min(owners,key=lambda o:o['end']-o['start'])['key']!=c['key']:continue
            name,args=m.groups()
            require(name!='isUseableByPlayer',(c['key'],'legacy callback'))
            if re.search(r'public\s+(boolean|void)\s*$',clean[:m.start()]):
                require(args.strip()=='EntityPlayer player',(c['key'],name,'native argument'))
                declarations[name].append(c['path'])
            elif name=='isUsableByPlayer':
                require(args.strip()=='player',(c['key'],'self call argument'))
                selfcalls.append(c['path'])
            else:
                raise AssertionError((c['key'],'unexpected callback call',name,args))
    expected_use=[p for p in MANIFEST['usability_owners'] if not p.endswith('TileEntityCrateBase.java')]
    require(sorted(declarations['isUsableByPlayer'])==sorted(expected_use),'seven usability owners')
    expected_pair=[p for p in MANIFEST['open_close_owners'] if p not in MANIFEST['independent_owners']]
    for name in ('openInventory','closeInventory'):
        require(sorted(declarations[name])==sorted(expected_pair),(name,'five root/descendant owners'))
    require(len(selfcalls)==35 and sorted(selfcalls)==sorted(MANIFEST['self_calls']),'exact 35 descendant self-call mapping')
    machine_types={c['name'] for c in closure}
    calls=[]
    for p,text in texts.items():
        if '/inventory/container/' not in p:continue
        clean=scan.mask(text)
        for m in re.finditer(r'\b(\w+)\.(isUseableByPlayer|isUsableByPlayer)\s*\(([^)]*)\)',clean):
            receiver=m[1]
            types=re.findall(r'\b(\w+)\s+'+re.escape(receiver)+r'\s*(?:;|=|,|\))',clean)
            # A receiver must have one consistent static type for a checked call.
            relevant=set(types)&machine_types
            if relevant:
                require(len(set(types))==1,(p,receiver,'ambiguous receiver',types))
                require(m[2]=='isUsableByPlayer' and m[3].strip()=='player',(p,'typed callback name/argument'))
                calls.append(p)
    require(len(calls)==101 and sorted(calls)==sorted(MANIFEST['container_calls']),'exact 101 machine-typed container calls')
    for p in MANIFEST['independent_container_calls']:
        text=texts[p]
        require(text.count('.isUsableByPlayer(player)')==1 and 'isUseableByPlayer' not in text,(p,'independent usability edge'))
    generic=texts['src/main/java/com/hbm/inventory/container/ContainerBase.java']
    require(re.search(r'\bIInventory\s+\w+\s*;',generic),'ContainerBase IInventory dispatch')
    mass=[c for c in classes if c['name']=='TileEntityMassStorage']
    require(len(mass)==1 and any(k.endswith(':TileEntityCrateBase') for k in mass[0]['ancestors']),'MassStorage inherits CrateBase policy')
    for p in MANIFEST['paired_containers']:
        text=texts[p]
        require(text.count('.openInventory(invPlayer.player)')==1,(p,'constructor player'))
        arg=re.search(r'onContainerClosed\(EntityPlayer (\w+)\)',text)[1]
        require(text.count('.closeInventory('+arg+')')==1,(p,'close player'))
        require(text.index('super.onContainerClosed('+arg+')')<text.index('.closeInventory('+arg+')'),(p,'super-close ordering'))
    for p in MANIFEST['independent_owners']:
        text=texts[p]
        require('isUseableByPlayer' not in text,(p,'legacy independent usability'))
        for name in ('openInventory','closeInventory'):
            require('public void '+name+'(EntityPlayer player)' in text,(p,name,'independent native declaration'))
            require('.'+name+'()' not in text,(p,name,'old independent delegate'))
    print('PASS: 147 whole-file callback reversals/drift rejections; 183-class census; seven machine usability policies, five callback pairs, 101 typed + two independent callers, 35 self-calls and five paired containers. Not full machines/gameplay/audio.')
if __name__=='__main__':main()
