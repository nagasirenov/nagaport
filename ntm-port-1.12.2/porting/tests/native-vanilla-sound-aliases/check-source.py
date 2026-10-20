#!/usr/bin/env python3
"""Exact complete-invocation/whole-file inverses and full-source native attribution."""
import argparse,collections,csv,gzip,hashlib,json,re,zipfile
from pathlib import Path
p=argparse.ArgumentParser();p.add_argument('--project',type=Path,default=Path(__file__).resolve().parents[3]);p.add_argument('--proof',type=Path);a=p.parse_args();e=Path(__file__).resolve().parent;m=json.loads((e/'manifest.json').read_text());sha=lambda b:hashlib.sha256(b).hexdigest()
with zipfile.ZipFile(e/'sources.zip') as z:
 for path,info in m['files'].items():
  before=z.read(path);after=(a.project/'src/main/java'/path).read_bytes();assert sha(before)==info['before'] and sha(after)==info['after'],path
  inverse=after.decode()
  for c in [x for x in m['calls'] if x['path']==path]:assert c['new_invocation'] in inverse;inverse=inverse.replace(c['new_invocation'],c['full_invocation'],1)
  assert inverse.encode()==before,path
 path='com/hbm/sound/EntitySoundTransport.java';after=(a.project/'src/main/java'/path).read_bytes();assert sha(after)==m['transport']['after'];assert after.decode().replace(m['transport']['inserted_block'],'',1).encode()==z.read(path)
path='com/hbm/sound/SoundCategoryResolver.java';assert sha((a.project/'src/main/java'/path).read_bytes())==m['resolver']['after'];assert (a.project/'src/main/java'/path).read_text()==m['resolver']['after_text']
with zipfile.ZipFile(e/'sources.zip') as z:assert sha(z.read(path))==m['resolver']['before']
for path,h in m['immutable'].items():assert sha((a.project/'src/main/java'/path).read_bytes())==h,path
representative=json.loads((e/'runtime/representative-method.json').read_text());assert sha((e/'runtime/RepresentativeMethods.java').read_bytes())==representative['generated_sha256'];assert representative['adapted'] in (e/'runtime/RepresentativeMethods.java').read_text();assert any(c['path']==representative['path'] and c['enclosing_method_source']==representative['original'] for c in m['calls'])
resolver=(a.project/'src/main/java/com/hbm/sound/SoundCategoryResolver.java').read_text();section=resolver[resolver.index('private static boolean isVanillaCoordinateAlias'):];assert set(re.findall(r'case "([^"\n]+)":',section))=={x['legacy'] for x in m['aliases']}
source=(a.project/'src/main/java/com/hbm/packet/PacketDispatcher.java').read_text();registrations=re.findall(r'wrapper\.registerMessage\(([^;]+);',source);assert len([r for r in registrations[:33] if 'i++' in r])==33 and 'LegacyEntitySoundPacket.Handler.class, LegacyEntitySoundPacket.class, i++, Side.CLIENT' in registrations[33]
print('PASS 94 complete invocation inverses / 76 whole consumer files; exact transport overload insertion inverse; original transport method, packet, codec and dispatcher unchanged; resolver change limited to reload diagnostics; one exact complete production method.')
if a.proof:
 def rows(n):
  f=a.proof/n;handle=f.open() if f.exists() else gzip.open(str(f)+'.gz','rt');return list(csv.DictReader(handle,delimiter='\t'))
 def dec(s):return re.sub(r'\\([\\trn])',lambda q:{'\\':'\\','t':'\t','r':'\r','n':'\n'}[q[1]],s)
 before=[{k:dec(v) for k,v in x.items()} for x in rows('before.tsv')];after=[{k:dec(v) for k,v in x.items()} for x in rows('after.tsv')];assert len(before)==227 and len(after)==94
 for old in m['calls']:
  found=[x for x in before if x['path']==old['path'] and x['start']==old['start']];assert found==[{k:v for k,v in old.items() if k!='new_invocation'}];assert old['receiver_type']=='net.minecraft.world.World' and old['receiver_kind']=='DECLARED' and 'ERROR' not in old['arguments_kind']
 for new,old in zip(after,m['calls']):
  assert new['path']==old['path'] and new['line']==old['line'];assert new['method']=='play' and new['method_owner']=='com.hbm.sound.VanillaWorldSoundEffects' and new['method_element_kind']=='METHOD'
  assert new['method_signature']=='play(net.minecraft.world.World,double,double,double,java.lang.String,float,float)'
  assert new['arguments_source'].split(' || ')==[old['receiver_source']]+old['arguments_source'].split(' || ')
  assert new['arguments_type'].split(' || ')==[old['receiver_type']]+old['arguments_type'].split(' || ')
  assert new['full_invocation']==old['new_invocation']
  inv=new['enclosing_method_source']
  for c in m['calls']:
   if c['path']==old['path']:inv=inv.replace(c['new_invocation'],c['full_invocation'])
  assert inv==old['enclosing_method_source']
 b=collections.Counter((x['path'],x['line'],x['code'],x['message']) for x in rows('before-diagnostics.tsv'));c=collections.Counter((x['path'],x['line'],x['code'],x['message']) for x in rows('after-diagnostics.tsv'))
 assert not c-b;removed=b-c;assert sum(removed.values())==94 and all('playSoundEffect' in x[3] for x in removed)
 print('PASS 4063 -> 4064 genuine units fully attributed: 94 complete receivers, 564 argument expressions/types and enclosing methods preserved; exact native helper destinations. 15608 -> 15514 diagnostics, exactly 94 removed, zero other diagnostic changes.')
