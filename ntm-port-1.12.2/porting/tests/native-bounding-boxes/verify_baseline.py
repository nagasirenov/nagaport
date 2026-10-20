#!/usr/bin/env python3
"""Optional exact Git-baseline proof; portable no-Git inverse guard is source.py."""
import hashlib,json,subprocess
import source
H=source.HERE;R=source.ROOT;m=json.loads((H/'manifest.json').read_text())
lines=subprocess.check_output(['git','ls-tree','-r',m['baseline'],'--','src/main/java'],cwd=R,text=True).splitlines();matched=0
for line in lines:
 mode,kind,rest=line.split(' ',2);oid,rel=rest.split('\t');assert kind=='blob'
 if not rel.endswith('.java'):continue
 b=(R/rel).read_bytes()
 if rel in m['files']:b=source.verify(b,m['files'][rel]).encode()
 assert hashlib.sha1(b'blob '+str(len(b)).encode()+b'\0'+b).hexdigest()==oid,rel
 assert hashlib.sha256(b).hexdigest()==m['before_all_java_sha256'][rel],rel
 matched+=1
assert matched==4062
print('PASS all 4062 reconstructed production Java bytes equal exact Git CP63 '+m['baseline'])
