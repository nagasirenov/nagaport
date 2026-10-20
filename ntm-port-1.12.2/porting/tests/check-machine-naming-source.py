#!/usr/bin/env python3
"""Whole-file, fail-closed naming reversal, composed BEFORE sided reversal.
No Git objects, baseline regeneration, whole-machine compilation or gameplay claim.
"""
import hashlib
import importlib.util
import json
from pathlib import Path
import re
ROOT = Path(__file__).resolve().parents[2]
# Exact callback reversal is the newest layer; keep historical naming hashes frozen.
_player_spec=importlib.util.spec_from_file_location('machine_player_audit',ROOT/'porting/tests/check-machine-player-source.py')
_player=importlib.util.module_from_spec(_player_spec);_player_spec.loader.exec_module(_player)
MANIFEST = json.loads((ROOT / 'porting/tests/machine-naming-source-manifest.json').read_text(encoding='utf-8'))
FILES = {entry['path']: entry for entry in MANIFEST['files']}
def sha(text): return hashlib.sha256(text.encode('utf-8')).hexdigest()
def require(condition, detail):
    if not condition: raise AssertionError(detail)
def reverse_source(relative, text):
    text = _player.reverse_source(relative, text)
    entry = FILES.get(relative)
    if entry is None: return text
    require(sha(text) == entry['migrated_sha256'], (relative, 'naming migrated whole-file drift'))
    for edit in reversed(entry['edits']):
        offset = edit['offset']
        require(text[offset:offset+len(edit['new'])] == edit['new'], (relative, edit['kind'], 'naming edit mismatch'))
        text = text[:offset] + edit['old'] + text[offset+len(edit['new']):]
    require(sha(text) == entry['baseline_sha256'], (relative, 'naming baseline whole-file drift'))
    return text

def method(text, name):
    match = re.search(r'public\s+[\w.]+\s+'+name+r'\s*\([^)]*\)\s*\{', text)
    require(match is not None, name)
    start=match.start(); end=text.index('{',start)+1; depth=1
    while depth:
        depth+=(text[end]=='{')-(text[end]=='}'); end+=1
    return text[start:end]

def main():
    _player.main()
    require(len(FILES) == 162, '162 naming files')
    require(len(set(MANIFEST['descendant_default_paths'])) == 161, '161 descendant defaults')
    for relative in FILES:
        text = (ROOT / relative).read_bytes().decode('utf-8')
        old = reverse_source(relative, text)
        try: reverse_source(relative, text+'\n')
        except AssertionError: pass
        else: raise AssertionError((relative, 'drift accepted'))
        # Compare naming-era bodies, after reversing later EMPTY/callback changes.
        text = _player.reverse_source(relative, text)
        if relative in MANIFEST['descendant_default_paths']:
            require(text.count('public String getDefaultName()') == 1, (relative, 'owned hook'))
        if relative.endswith(('TileEntityMachineBase.java', 'TileEntityMachineBattery.java', 'TileEntityDiFurnaceRTG.java')):
            for callback in ('readFromNBT', 'writeToNBT', 'setCustomName'):
                require(method(text,callback)==method(old,callback), (relative, callback, 'ownership/body drift'))
            field='name' if relative.endswith('TileEntityDiFurnaceRTG.java') else 'customName'
            require('private String '+field+';' in text, (relative, 'independent name state'))
            if field=='name':
                require('getString("name")' not in method(text,'readFromNBT'), 'RTG does not read its private name')
                require('setString("name"' not in method(text,'writeToNBT'), 'RTG does not write its private name')
            else:
                require('customName = nbt.getString("name");' in method(text,'readFromNBT'), 'original name NBT read')
                require('nbt.setString("name", customName);' in method(text,'writeToNBT'), 'original name NBT write')
        # Each original NBT method and its field references are outside the exact allowed edits.
        for edit in FILES[relative]['edits']:
            require(not any(word in edit['old']+edit['new'] for word in ('readFromNBT', 'writeToNBT', 'slots[', 'setTag', 'setString')), (relative, 'out-of-scope edit'))
    spec = importlib.util.spec_from_file_location('naming_closure', ROOT/'porting/tests/check-tile-tick-migration.py')
    scan = importlib.util.module_from_spec(spec); spec.loader.exec_module(scan)
    texts, classes, _ = scan.scan()
    base = 'src/main/java/com/hbm/tileentity/TileEntityMachineBase.java:TileEntityMachineBase'
    descendants = [c for c in classes if base in c['ancestors'] and c['key'] != base]
    require(len(descendants) == 182, ('descendant closure drift', len(descendants)))
    owned = []
    for c in descendants:
        # Restrict to directly owned declarations (nested classes cannot steal a method).
        text = scan.mask(texts[c['path']])
        for m in re.finditer(r'public\s+String\s+getDefaultName\s*\(\s*\)', text):
            owners = [o for o in classes if o['path']==c['path'] and o['head_end'] < m.start() < o['end']]
            if min(owners,key=lambda o:o['end']-o['start'])['key']==c['key']: owned.append(c['path'])
    require(sorted(owned)==sorted(MANIFEST['descendant_default_paths']), 'exact owned default hook census')
    print('PASS: 182 descendants; 161 owned defaults; 162 whole-file naming reversals and drift rejections; NBT/storage/lifecycle unchanged. Not full-machine compilation.')
if __name__ == '__main__': main()
