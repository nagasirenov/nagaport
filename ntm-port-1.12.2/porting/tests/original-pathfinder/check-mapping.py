#!/usr/bin/env python3
"""Finite CP48 inverse, not a source rewrite or behavior proof."""
from pathlib import Path
import argparse,hashlib,json
HERE=Path(__file__).resolve().parent
MANIFEST=json.loads((HERE/'source-manifest.json').read_text())
def digest(text):return hashlib.sha256(text.encode('utf-8')).hexdigest()
def reverse_source(rel,text):
    rel=Path(rel).as_posix();entry=MANIFEST['existing'].get(rel)
    if entry is None:
        if rel in MANIFEST['new']:assert digest(text)==MANIFEST['new'][rel],('Changed new pathfinder',rel)
        return text
    assert digest(text)==entry['after_sha256'],('Changed frozen path source',rel)
    before=text
    for edit in reversed(entry['edits']):
        offset=edit['after_offset'];assert before[offset:offset+len(edit['new'])]==edit['new']
        before=before[:offset]+edit['old']+before[offset+len(edit['new']):]
    assert digest(before)==entry['before_sha256'],('Wrong CP48 inverse',rel)
    after=before
    for edit in reversed(entry['edits']):
        offset=edit['before_offset'];assert after[offset:offset+len(edit['old'])]==edit['old']
        after=after[:offset]+edit['new']+after[offset+len(edit['old']):]
    assert after==text,('Wrong pathfinder roundtrip',rel)
    return before

def check(module):
    rejects=0
    for rel in [*MANIFEST['existing'],*MANIFEST['new']]:
        text=(module/rel).read_bytes().decode();reverse_source(rel,text)
        for changed in [text+'\n',text.replace('\n','\r\n'),text.replace('package ','package /* drift */ ',1)]:
            assert changed!=text
            try:reverse_source(rel,changed)
            except AssertionError:rejects+=1
            else:raise AssertionError('Accepted source drift '+rel)
    print('PASS three exact CP48 inverses, one pinned native helper, %d rejected drift mutations'%rejects,flush=True)
if __name__=='__main__':
    p=argparse.ArgumentParser();p.add_argument('--module',type=Path,default=HERE.parents[2]);a=p.parse_args();check(a.module)
