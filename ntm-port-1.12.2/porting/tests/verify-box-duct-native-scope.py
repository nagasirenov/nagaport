#!/usr/bin/env python3
"""Twelve frozen owned files plus exact shared fragments; unrelated approved edits remain allowed.

This is source-scope evidence, not behavioral/runtime certification. Before/after
shared hashes identify the immutable CP48-to-box baseline audit, not an ongoing
whole-file lock. Shared substitutions and their inverses operate on bytes only.
"""
import argparse,hashlib,json
from pathlib import Path
HERE=Path(__file__).resolve().parent

def reverse_shared(data,entry,label):
    """Validate and reverse only box-owned fragments, preserving all other bytes."""
    result=data
    for fragment in entry['fragments']:
        before=fragment['before'].encode('utf-8');after=fragment['after'].encode('utf-8')
        assert result.count(after)==1,'Box-owned shared fragment missing/duplicated: '+label
        assert result.count(before)==0,'Obsolete box-owned fragment still present: '+label
        result=result.replace(after,before,1)
    replay=result
    for fragment in entry['fragments']:
        before=fragment['before'].encode('utf-8');after=fragment['after'].encode('utf-8')
        assert replay.count(before)==1,'Ambiguous reverse-owned fragment: '+label
        replay=replay.replace(before,after,1)
    assert replay==data,'Shared fragment reverse/forward changed unrelated bytes: '+label
    return result

def verify(module,self_test=False):
    manifest=json.loads((HERE/'box-duct-native-source-manifest.json').read_text())
    shared=json.loads((HERE/'box-duct-native-shared-fragments.json').read_text())
    assert manifest['baseline']==shared['baseline']
    expected={rel for rel,entry in manifest['files'].items() if entry['shared_fragment']}
    assert expected==set(shared['files']),'Shared ownership manifest drift'
    owned=0;fragments=0;mutations=0
    for rel,entry in manifest['files'].items():
        data=(module/rel).read_bytes()
        if not entry['shared_fragment']:
            assert hashlib.sha256(data).hexdigest()==entry['sha256'],'Frozen owned box-duct production drift: '+rel
            owned+=1;continue
        boundary=shared['files'][rel]
        assert boundary['baseline_before_sha256']==entry['before_sha256'] and boundary['baseline_after_sha256']==entry['sha256']
        reversed_data=reverse_shared(data,boundary,rel);fragments+=len(boundary['fragments'])
        # Exact full-baseline equality is additionally checked when this file has
        # no unrelated changes. Integrated CT/other approved fragments are not erased.
        if hashlib.sha256(data).hexdigest()==entry['sha256']:
            assert hashlib.sha256(reversed_data).hexdigest()==entry['before_sha256'],'Pinned baseline reverse mismatch: '+rel
        if self_test:
            marker=b'\n// source-audit-only unrelated append\n'
            assert reverse_shared(data+marker,boundary,rel)==reversed_data+marker
            for fragment in boundary['fragments']:
                after=fragment['after'].encode('utf-8')
                for bad in [data.replace(after,b'!'+after[1:],1),data+after]:
                    rejected=False
                    try:reverse_shared(bad,boundary,rel)
                    except AssertionError:rejected=True
                    assert rejected,'Owned fragment mutation escaped guard: '+rel
                    mutations+=1
    print('PASS',owned,'full-hashed owned files,',len(shared['files']),'shared files,',fragments,'exact byte-reversible fragments;',mutations,'mutation rejections; unrelated shared edits preserved. Baseline',manifest['baseline'])

if __name__=='__main__':
    p=argparse.ArgumentParser();p.add_argument('--module',type=Path,required=True);p.add_argument('--self-test',action='store_true');a=p.parse_args();verify(a.module,a.self_test)
