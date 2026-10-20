#!/usr/bin/env python3
"""CP56 exact inverses, unchanged argument expressions, bundled gates and negative controls."""
from pathlib import Path
import hashlib,json,re,subprocess,csv
ROOT=Path(__file__).resolve().parents[3];HERE=Path(__file__).resolve().parent
sha=lambda b:hashlib.sha256(b).hexdigest()
M=json.loads((HERE/'source-manifest.json').read_text())
def audit(overrides=None):
 overrides=overrides or {};read=lambda rel:overrides.get(rel,(ROOT/rel).read_bytes())
 for rel,want in M['receiver_attribution_pins'].items():assert sha((HERE/rel).read_bytes())==want,rel
 evidence=json.loads((HERE/'receiver-attribution/receiver-evidence.json').read_text())
 before={(e['path'],e['line']):e for e in evidence['calls']}
 with (HERE/'receiver-attribution/corrected.tsv').open() as f:after={(e['path'],int(e['line'])):e for e in csv.DictReader(f,delimiter='\t')}
 assert len(before)==len(after)==96
 count=0;names=set()
 for f in M['files']:
  data=read(f['path']);assert sha(data)==f['after_sha256'],f['path'];lines=data.decode().splitlines(keepends=True)
  for c in f['changes']:
   assert lines[c['line']-1]==c['after'],f['path'];old=c['before'];new=c['after']
   evidence=before[(f['path'],c['line'])];destination=after[(f['path'],c['line'])]
   assert evidence['receiver_kind']=='DECLARED' and evidence['receiver_type']=='net.minecraft.world.World'
   assert destination['method_element_kind']=='METHOD' and destination['method_owner']=='com.hbm.sound.WorldSoundEffects'
   assert destination['method_signature']=='play(net.minecraft.world.World,double,double,double,java.lang.String,float,float)'
   assert destination['arguments_type'].split(' || ')[0]=='net.minecraft.world.World'
   assert destination['arguments_source'].split(' || ')==[evidence['receiver']]+evidence['arguments']
   assert destination['full_invocation']==evidence['expected_after_invocation']
   assert evidence['expected_after_invocation'] in new and evidence['before_invocation'] in old
   receiver=c['javac_receiver'];original=c['javac_original_invocation']
   assert receiver==evidence['receiver'] and original==evidence['before_invocation']
   assert original in old and old.count(original)==1,'original javac invocation absent/ambiguous'
   prefix=receiver+'.playSoundEffect('
   assert original.startswith(prefix),'receiver is not the COMPLETE javac method-select expression'
   assert len(c['javac_original_arguments'])==6
   expected=old.replace(prefix,'com.hbm.sound.WorldSoundEffects.play('+receiver+', ',1)
   assert expected==new,'full receiver/arguments/order altered'
   assert c['name'][4:] in M['names'];names.add(c['name'][4:]);lines[c['line']-1]=old;count+=1
  assert sha(''.join(lines).encode())==f['before_sha256'],'CP56 inverse failed'
 assert count==M['call_count']==96 and len(M['files'])==67 and names==set(M['names']) and len(names)==45
 sounds=json.loads((ROOT/'src/main/resources/assets/hbm/sounds.json').read_text())
 mod=read('src/main/java/com/hbm/main/ModSoundEvents.java').decode()
 declared=re.findall(r'worldEffect\("([^"]+)", SoundCategory\.([A-Z]+)\);',mod);assert len(declared)==45 and len(set(declared))==45
 categories={'block':'BLOCKS','player':'PLAYERS','hostile':'HOSTILE','record':'RECORDS','ambient':'AMBIENT','neutral':'NEUTRAL','master':'MASTER'}
 for name,category in declared:assert sounds[name]==M['names'][name] and category==categories[sounds[name]['category']]
 existing=set(re.findall(r'public static final SoundEvent [^=]+= create\("([^"]+)"\)',mod));assert len(existing)==17 and not existing.intersection(names)
 assert len(M['excluded'])==25 and all(e['name'][4:] not in names for e in M['excluded'])
 for rel,want in M['production_pins'].items():assert sha(read(rel))==want,rel
 # Earlier category/reload behavior is deliberately untouched, not rewritten for this port.
 for rel,want in M['unchanged_pins'].items():assert sha(read(rel))==want,rel
 return count
if __name__=='__main__':
 count=audit();mutations=0
 for f in M['files']:
  data=(ROOT/f['path']).read_bytes();c=f['changes'][0];bad=data.replace(c['after'].encode(),c['before'].encode(),1)
  try:audit({f['path']:bad})
  except AssertionError:mutations+=1
  else:raise AssertionError('accepted reverted consumer')
 for f in M['files']:
  for c in f['changes']:
   if 'failed_candidate_after' in c:
    data=(ROOT/f['path']).read_bytes();bad=data.replace(c['after'].encode(),c['failed_candidate_after'].encode(),1)
    try:audit({f['path']:bad})
    except AssertionError:mutations+=1
    else:raise AssertionError('accepted reported partial-receiver regression')
 for rel in M['production_pins']:
  try:audit({rel:(ROOT/rel).read_bytes()+b' '})
  except AssertionError:mutations+=1
  else:raise AssertionError('accepted helper drift')
 print('PASS96 exact call inverses across67 files; receiver/coordinates/name/volume/pitch order unchanged;45 authoritative names/categories;25 explicit exclusions; '+str(mutations)+' negative controls')
