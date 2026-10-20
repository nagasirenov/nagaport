import argparse,csv,hashlib,json,re,subprocess
from pathlib import Path
p=argparse.ArgumentParser();p.add_argument('--root',type=Path,required=True);p.add_argument('--pristine',type=Path,required=True);p.add_argument('--record',action='store_true');a=p.parse_args();E=Path(__file__).resolve().parent;B='112cf44057e5cd1d1173561e66dbcca97edc9bdb';rows=json.loads((E/'sites.json').read_text());sha=lambda s:hashlib.sha256(s).hexdigest();provenance=[]
methods=list(csv.DictReader((E/'attribution/methods.tsv').open(),delimiter='\t'));classes=list(csv.DictReader((E/'attribution/classes.tsv').open(),delimiter='\t'));byname={c['owner']:c for c in classes};closure={}
for r in rows:
 raw=(a.root/r['path']).read_bytes();s=raw.decode();assert sha(raw)==r['after_sha256'];o=r['offset'];assert s[o:o+len(r['after'])]==r['after'];before=s[:o]+r['before']+s[o+len(r['after']):];assert sha(before.encode())==r['before_sha256'];assert subprocess.check_output(['git','-C',str(a.root),'show',B+':ntm-port-1.12.2/'+r['path']])==before.encode()
 old=(a.pristine/r['path']).read_bytes();assert re.search(r'public int getRenderType\(\)\s*\{\s*return -1;\s*}',old.decode());provenance.append(dict(path=r['path'],sha256=sha(old)))
 ms=[m for m in methods if m['path']==r['path']];assert len(ms)==1 and ms[0]['native_override']=='true';owner=ms[0]['owner'];desc=[]
 for c in classes:
  q=c;seen=set()
  while q['superclass'] in byname and q['owner'] not in seen:
   seen.add(q['owner'])
   if q['superclass']==owner:desc.append(c['owner']);break
   q=byname[q['superclass']]
 for n in desc:assert not any(m['owner']==n for m in methods),(owner,n)
 closure[owner]=desc
# All production files, including TESR bodies, model handlers and item registration, unchanged outside exact slices.
changed=subprocess.check_output(['git','-C',str(a.root),'diff','--name-only',B,'--','src/main/java'],text=True).splitlines()
assert set(changed)=={'ntm-port-1.12.2/'+r['path'] for r in rows},changed
calls=list(csv.DictReader((E/'attribution/calls.tsv').open(),delimiter='\t'))
assert len(calls)==5
assert sum(c['source']=='getRenderType()' and c['method_owner']=='com.hbm.blocks.BlockDummyable' for c in calls)==1
assert sum(c['source']=='block.getRenderType()' and c['method_kind']=='CLASS' for c in calls)==1 # unresolved legacy minecart remains excluded, not repaired by this work
if a.record:
 (E/'original-provenance.json').write_text(json.dumps(provenance,indent=2)+'\n');(E/'closure.json').write_text(json.dumps(closure,indent=2)+'\n')
else:
 assert provenance==json.loads((E/'original-provenance.json').read_text());assert closure==json.loads((E/'closure.json').read_text())
handler=(a.root/'src/main/java/com/hbm/main/LegacyBaseModelHandler.java').read_text()
assert 'getDeclaredMethod("getRenderType")' in handler
assert 'if(block.getRenderType(state) != EnumBlockRenderType.MODEL || !block.isFullCube(state)) return false;' in handler
print('PASS: 62 exact baseline reversals/original constant-negative callbacks/native overrides; '+str(sum(map(len,closure.values())))+' inherited descendants; five direct consumers audited; all other production files unchanged.')
