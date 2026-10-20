"""Verify independent CP85 read/write/foreach patches compose without losing original bytes."""
from pathlib import Path
import json,hashlib,collections,importlib.util
R=Path(__file__).resolve().parents[2];T=R/'porting/tests'
def load(name,path):
 spec=importlib.util.spec_from_file_location(name,path);m=importlib.util.module_from_spec(spec);spec.loader.exec_module(m);return m
read=load('read_inverse',T/'player-inventory-reads/check-source.py');write=load('write_inverse',T/'player-inventory-writes/check-source.py')
rm=json.loads((T/'player-inventory-reads/source-manifest.json').read_text());wm=json.loads((T/'player-inventory-writes/source-manifest.json').read_text());lm=json.loads((T/'player-inventory-loops/source-manifest.json').read_text());loops={f['path']:f for f in lm['files']}
assert rm['baseline']==wm['baseline_commit']==lm['base']=='abe006a0845275e8be52417967ac8ebf6f283a52'
files=set(rm['files'])|set(wm['files'])|set(loops)
for relative in sorted(files):
 text=(R/relative).read_bytes().decode();before=[]
 for manifest in (rm,wm):
  if relative in manifest['files']:before.append(manifest['files'][relative]['before_sha256'])
 text=read.reverse_source(relative,text,strict=False);text=write.reverse_source(relative,text,strict=False)
 if relative in loops:
  row=loops[relative];before.append(row['before']);counts=collections.Counter(e['new'] for e in row['edits'])
  for new,count in counts.items():assert text.count(new)==count,(relative,new);text=text.replace(new,'')
 assert len(set(before))==1,(relative,'conflicting worker baseline')
 assert hashlib.sha256(text.encode()).hexdigest()==before[0],(relative,'combined inverse drift')
helper=rm['helper'];assert hashlib.sha256((R/helper['file']).read_bytes()).hexdigest()==helper['sha256']
print('PASS',len(files),'combined exact CP85 inventory source inverses plus pinned new helper; no unrelated byte overwritten')
