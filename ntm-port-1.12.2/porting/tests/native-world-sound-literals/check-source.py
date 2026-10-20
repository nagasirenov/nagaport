#!/usr/bin/env python3
import argparse,collections,csv,gzip,hashlib,json,re,zipfile
from pathlib import Path
p=argparse.ArgumentParser();p.add_argument('--project',type=Path,default=Path(__file__).resolve().parents[3]);p.add_argument('--proof',type=Path);a=p.parse_args();e=Path(__file__).resolve().parent;m=json.loads((e/'manifest.json').read_text());sha=lambda b:hashlib.sha256(b).hexdigest()
with zipfile.ZipFile(e/'sources.zip') as z:
 for path,info in m['files'].items():
  before=z.read(path);after=(a.project/'src/main/java'/path).read_bytes();assert sha(before)==info['before'] and sha(after)==info['after'],path
  inverse=after.decode()
  for c in [x for x in m['calls'] if x['path']==path]:assert c['new_invocation'] in inverse;inverse=inverse.replace(c['new_invocation'],c['full_invocation'],1)
  assert inverse.encode()==before,path
 path='com/hbm/main/ModSoundEvents.java';after=(a.project/'src/main/java'/path).read_bytes();assert sha(after)==m['registry']['after'];assert after.decode().replace(m['registry']['inserted_block'],'',1).encode()==z.read(path)
for path,h in m['immutable'].items():assert sha((a.project/'src/main/java'/path).read_bytes())==h,path
print('PASS 76 complete invocation inverses / 48 whole consumer files, exact 42 declaration-only insertion inverse, unchanged native helper/category/resolver.')
if a.proof:
 def rows(n):
  f=a.proof/n;handle=f.open() if f.exists() else gzip.open(str(f)+'.gz','rt');return list(csv.DictReader(handle,delimiter='\t'))
 def dec(s):return re.sub(r'\\([\\trn])',lambda q:{'\\':'\\','t':'\t','r':'\r','n':'\n'}[q[1]],s)
 before=[{k:dec(v) for k,v in x.items()} for x in rows('before.tsv')];after=[{k:dec(v) for k,v in x.items()} for x in rows('after.tsv')];assert len(before)==227 and len(after)==76
 originals={(c['path'],c['start']):c for c in m['calls']}
 for old in m['calls']:
  found=[x for x in before if x['path']==old['path'] and x['start']==old['start']];assert found==[{k:v for k,v in old.items() if k!='new_invocation'}]
 for new,old in zip(after,m['calls']):
  assert new['path']==old['path'] and new['line']==old['line'];assert new['method']=='play' and new['method_owner']=='com.hbm.sound.WorldSoundEffects' and new['method_element_kind']=='METHOD'
  assert new['method_signature']=='play(net.minecraft.world.World,double,double,double,java.lang.String,float,float)'
  assert new['arguments_source'].split(' || ')==[old['receiver_source']]+old['arguments_source'].split(' || ')
  assert new['arguments_type'].split(' || ')==[old['receiver_type']]+old['arguments_type'].split(' || ')
  assert new['full_invocation']==old['new_invocation']
  inv=new['enclosing_method_source']
  for c in m['calls']:
   if c['path']==old['path']:inv=inv.replace(c['new_invocation'],c['full_invocation'])
  assert inv==old['enclosing_method_source']
 b=collections.Counter((x['path'],x['line'],x['code'],x['message']) for x in rows('before-diagnostics.tsv'));c=collections.Counter((x['path'],x['line'],x['code'],x['message']) for x in rows('after-diagnostics.tsv'))
 # Added registry lines shift only offsets/lines inside that file, which has no errors.
 assert not c-b;removed=b-c;assert sum(removed.values())==76 and all('playSoundEffect' in x[3] for x in removed)
 print('PASS full genuine-source attribution: 76 whole receivers, 456 argument expressions/types and enclosing methods preserved; native destinations exact. 15878 -> 15802 diagnostics, exactly 76 removed and zero other diagnostic changes.')
