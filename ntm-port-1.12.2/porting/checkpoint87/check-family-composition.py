"""Compose independently tested invocation edits back to sealed CP86 whole files."""
from pathlib import Path
import importlib.util,json,collections,hashlib,zipfile
H=Path(__file__).resolve().parent;R=H.parents[1];T=R/'porting/tests'
def load(name,path):
 s=importlib.util.spec_from_file_location(name,path);m=importlib.util.module_from_spec(s);s.loader.exec_module(m);return m
boundary=load('cp87',T/'check-native-equipment-destruction-name-source.py');boundary.verify()
reads=load('reads',T/'equipment-reads-native/check_source.py')
writes=load('writes',T/'equipment-writes-native/check-source.py')
destroy=json.loads((T/'native-destroy-block/manifest.json').read_text())
paths=set(reads.DATA['files'])|set(writes.DATA['files'])|set(destroy['files'])
for rel in sorted(paths):
 text=(R/rel).read_bytes().decode()
 if rel in reads.DATA['files']:text=reads.inverse(rel,text,strict=False)
 text=writes.reverse_source(rel,text,strict=False)
 if rel in destroy['files']:
  for (new,old),n in collections.Counter((e['new'],e['old']) for e in destroy['files'][rel]).items():
   assert text.count(new)==n,(rel,new)
   text=text.replace(new,old)
 assert hashlib.sha256(text.encode()).hexdigest()==boundary.FILES[rel]['before_sha256'],rel
items=json.loads((T/'native-item-stack-key-hooks/manifest.json').read_text())
for row in items['production']:
 assert hashlib.sha256((R/row['path']).read_bytes()).hexdigest()==row.get('after_sha256',row.get('unchanged_sha256')),row['path']
print('PASS independent read/write/destruction inverses compose to %d exact CP86 files; item hooks and collaborators match tested hashes'%len(paths))
