#!/usr/bin/env python3
"""Exact local Git layer check; source.py provides portable no-Git inverses."""
import hashlib,json,subprocess
import source
H=source.H;R=source.R;m=json.loads((H/'manifest.json').read_text());n=0
lines=subprocess.check_output(['git','ls-tree','-r',m['prerequisite_layer_tree'],'--','src/main/java'],cwd=R,text=True).splitlines()
for line in lines:
 mode,kind,rest=line.split(' ',2);oid,rel=rest.split('\t')
 if not rel.endswith('.java'):continue
 b=(R/rel).read_bytes()
 if rel in m['files']:b=source.inverse(b,m['files'][rel])
 assert hashlib.sha1(b'blob '+str(len(b)).encode()+b'\0'+b).hexdigest()==oid,rel
 assert source.sha(b)==m['before_all_java_sha256'][rel];n+=1
assert n==4062
for rel,r in m['files'].items():
 b=subprocess.check_output(['git','show',m['baseline']+':ntm-original-pristine/'+rel],cwd=R)
 for e in r['edits']:assert source.sha(b)==e['original']['source_sha256']
print('PASS all 4062 reconstructed sources equal CP64 plus exact approved constructor prerequisite; all 32 original-provenance files equal pristine Git bytes')
