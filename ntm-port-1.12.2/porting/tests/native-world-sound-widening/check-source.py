#!/usr/bin/env python3
"""Exact complete-AST inverses for the finite16-call int-coordinate extension."""
import argparse, hashlib, json, pathlib
HERE=pathlib.Path(__file__).resolve().parent
p=argparse.ArgumentParser();p.add_argument('--root',type=pathlib.Path,default=HERE.parents[3]);a=p.parse_args()
M=json.loads((HERE/'source-manifest.json').read_text());B=json.loads((HERE/'attribution/before-selected-manifest.json').read_text());A=json.loads((HERE/'attribution/after-verification.json').read_text());sha=lambda b:hashlib.sha256(b).hexdigest()
def audit(overrides=None):
    overrides=overrides or {}
    for path,want in M['evidence_pins'].items():assert sha((HERE/path).read_bytes())==want,('evidence',path)
    for path,want in M['unchanged_production'].items():assert sha((a.root/path).read_bytes())==want,('unchanged production',path)
    before={(c['path'],c['line']):c for c in B['calls']}
    assert len(before)==len(A['calls'])==16 and len(A['files'])==14
    for rel,file in A['files'].items():
        path='ntm-port-1.12.2/src/main/java/'+rel
        data=overrides.get(path,(a.root/path).read_bytes());assert sha(data)==file['after_sha256'],('current source',rel)
        utf=data.decode('utf-8').encode('utf-16-le')
        for c in sorted((c for c in A['calls'] if c['path']==rel),key=lambda c:c['after_source_start'],reverse=True):
            old=before[(rel,c['line'])]
            assert old['receiver_type']=='net.minecraft.world.World' and old['receiver_kind']=='DECLARED'
            assert c['after_method_owner']=='com.hbm.sound.WorldSoundEffects'
            assert c['after_method_signature']=='play(net.minecraft.world.World,double,double,double,java.lang.String,float,float)'
            assert c['after_argument_expressions']==[old['receiver_source']]+old['arguments_source']
            assert c['after_argument_types']==['net.minecraft.world.World']+old['arguments_type']
            assert old['arguments_type'] in [['int','int','int','java.lang.String','float','float'],['double','int','double','java.lang.String','float','float']]
            lo,hi=c['after_source_start']*2,c['after_source_end']*2
            assert utf[lo:hi].decode('utf-16-le')==c['after_full_invocation']==old['destination_invocation']
            utf=utf[:lo]+old['full_invocation'].encode('utf-16-le')+utf[hi:]
        assert sha(utf.decode('utf-16-le').encode('utf-8'))==file['before_sha256']==B['files'][rel]['sha256'],('exact full-file inverse',rel)
    return 16
audit();negative=0
for c in A['calls']:
    path='ntm-port-1.12.2/src/main/java/'+c['path'];data=(a.root/path).read_bytes();utf=data.decode().encode('utf-16-le');lo,hi=c['after_source_start']*2,c['after_source_end']*2
    bad=(utf[:lo]+c['original_full_invocation'].encode('utf-16-le')+utf[hi:]).decode('utf-16-le').encode()
    try:audit({path:bad})
    except AssertionError:negative+=1
    else:raise AssertionError('accepted reverted selected call')
print('PASS16 exact full-AST source inverses across14 files; six original argument expressions/types and full receiver preserved; frozen helper/registry/category/transport; '+str(negative)+' negative controls. Historical96 whole-file guard has separate baseline applicability.')
