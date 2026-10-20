#!/usr/bin/env python3
"""Exact invocation fragment bridge; --strict also pins complete baseline/postimage files.
Uses byte-preserved UTF-8. Does not need original Git objects. Deliberately permits
unrelated changes in fragment mode so concurrent separately-audited migrations compose.
"""
import argparse,collections,hashlib,json,re
from pathlib import Path
HERE=Path(__file__).resolve().parent
ROOT=HERE.parents[2]
def manifest():return json.loads((HERE/'manifest.json').read_text())
def sha(t):return hashlib.sha256(t.encode()).hexdigest()
def forms(text,e,source):
    options=[e]+([e['composed']] if 'composed' in e else [])
    found=[o for o in options if len(list(re.finditer(re.escape(o[source]),text)))==e[source+'_total']]
    assert len(found)==1, ('Invocation drift or ambiguous composition',e[source])
    return found[0]
def transform(text,record,reverse=False):
    source,target=('after','before') if reverse else ('before','after')
    ranges=[]
    for e in record['edits']:
        selected=forms(text,e,source);old,new=selected[source],selected[target]
        matches=list(re.finditer(re.escape(old),text))
        assert len(matches)==e[source+'_total'], ('Invocation fragment count/drift',old,len(matches))
        hit=matches[e[source+'_ordinal']];ranges.append((hit.start(),hit.end(),new))
    assert len(set((a,b) for a,b,_ in ranges))==len(ranges)
    for a,b,new in sorted(ranges,reverse=True):text=text[:a]+new+text[b:]

    return text

def restore(rel,text):
    record=manifest()['files'].get(rel)
    return transform(text,record,True) if record else text

def verify(strict=False):
    m=manifest();n=0;neg=0
    for rel,r in m['files'].items():
        text=(ROOT/rel).read_bytes().decode();old=transform(text,r,True);assert transform(old,r)==text
        if strict:assert sha(old)==r['before_sha256'] and sha(text)==r['after_sha256'],rel
        # Every changed full invocation detects coordinate/receiver drift.
        for e in r['edits']:
            selected=forms(text,e,'after')['after'];bad=text.replace(selected,selected.replace('getTileEntity','getTileEntityBROKEN'),1)
            try:transform(bad,r,True)
            except AssertionError:neg+=1
            else:raise AssertionError(('Negative control accepted',rel))
        n+=len(r['edits'])
    print('PASS: %d exact reversible World calls / %d files; %d fragment drift rejections%s'%(n,len(m['files']),neg,'; whole-file byte parity' if strict else ''))
if __name__=='__main__':
    p=argparse.ArgumentParser();p.add_argument('--strict',action='store_true');a=p.parse_args();verify(a.strict)
