#!/usr/bin/env python3
"""Apply only attributed AST accesses; emit exact reversible fragments, no whole-file merge."""
from pathlib import Path
import json,hashlib,collections
from inventory import ROOT,eligible
HERE=Path(__file__).resolve().parent
BASE='077492a6c31c910f34697783f1aa0cfabd8ff88e'
def sha(s):return hashlib.sha256(s.encode()).hexdigest()
def index(s,n):
    if n<0:return n
    return len(s.encode('utf-16-le')[:2*n].decode('utf-16-le'))
def apply(src,edits):
    last=len(src)+1
    for e in sorted(edits,key=lambda e:e['start'],reverse=True):
        assert e['end']<=last and src[e['start']:e['end']]==e['before']
        src=src[:e['start']]+e['after']+src[e['end']:];last=e['start']
    return src
rows=[json.loads(s) for s in (HERE/'attributed-accesses.jsonl').read_text().splitlines()]
byfile=collections.defaultdict(list)
for r in rows: byfile[r['path']].append(r)
manifest={'baseline':BASE,'method':'javac native receiver + native field symbol attribution','files':[],'rejected':[]}
for absolute,entries in sorted(byfile.items()):
    p=Path(absolute);assert eligible(p);src=p.read_bytes().decode('utf-8');rel=p.relative_to(ROOT).as_posix()
    for r in entries:
        r['path']=rel
        for k in ('start','end','receiverStart','receiverEnd','assignmentStart','assignmentEnd','rhsStart','rhsEnd'):r[k]=index(src,r[k])
    manifest['rejected'].extend(r for r in entries if r['kind']=='reject')
    writes=[r for r in entries if r['kind']=='write'];reads=[r for r in entries if r['kind']=='read'];edits=[]
    for w in writes:
        start,end=w['assignmentStart'],w['assignmentEnd'];rs,re=w['rhsStart'],w['rhsEnd'];rhs=src[rs:re];children=[]
        for r in reads:
            if rs<=r['start'] and r['end']<=re:
                a,b=r['end']-len('stackTagCompound'),r['end'];assert src[a:b]=='stackTagCompound'
                children.append({'start':a-rs,'end':b-rs,'before':'stackTagCompound','after':'getTagCompound()'})
        rhs=apply(rhs,children)
        # No selected write is nested in a discarded-value write: nested assignment values are rejected.
        assert not any(w is not x and start<=x['assignmentStart']<end for x in writes)
        receiver=src[w['receiverStart']:w['receiverEnd']]
        edits.append({'start':start,'end':end,'before':src[start:end],'after':receiver+'.setTagCompound('+rhs+')','proof':[w]+[r for r in reads if rs<=r['start'] and r['end']<=re]})
    for r in reads:
        if any(w['assignmentStart']<=r['start'] and r['end']<=w['assignmentEnd'] for w in writes):continue
        a,b=r['end']-len('stackTagCompound'),r['end'];assert src[a:b]=='stackTagCompound'
        edits.append({'start':a,'end':b,'before':src[a:b],'after':'getTagCompound()','proof':[r]})
    if not edits:continue
    edits.sort(key=lambda e:e['start']);result=apply(src,edits)
    for e in edits:
        line_start=src.rfind('\n',0,e['start'])+1
        line_end=src.find('\n',e['end']);line_end=len(src) if line_end<0 else line_end
        e['line_before']=src[line_start:line_end]
        e['line']=src.count('\n',0,e['start'])+1
    manifest['files'].append({'path':rel,'before_sha256':sha(src),'after_sha256':sha(result),'edits':edits})
    p.write_bytes(result.encode('utf-8'))
manifest['summary']={'files':len(manifest['files']),'reads':len([r for r in rows if r['kind']=='read']),'writes':len([r for r in rows if r['kind']=='write']),'rejected':len(manifest['rejected']),'fragments':sum(len(f['edits']) for f in manifest['files'])}
(HERE/'manifest.json').write_text(json.dumps(manifest,indent=2)+'\n')
print(json.dumps(manifest['summary']))
