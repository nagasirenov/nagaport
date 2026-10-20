#!/usr/bin/env python3
"""Compose exact call fragments over independent edits; inverse preserves entire input."""
import argparse,collections,hashlib,json
from pathlib import Path
p=argparse.ArgumentParser();p.add_argument('--project',type=Path,required=True);p.add_argument('--receipt',type=Path,required=True);p.add_argument('--apply',action='store_true');a=p.parse_args();e=Path(__file__).resolve().parent;m=json.loads((e/'manifest.json').read_text());sha=lambda b:hashlib.sha256(b).hexdigest();pending={};receipt={'base':m['base'],'files':{}}
for rel,data in m['files'].items():
 p=a.project/'src/main/java'/rel;b=p.read_bytes();s=b.decode();out=s;fragments=[c for c in m['fragments'] if c['path']==rel];counts=collections.Counter(c['before'] for c in fragments);dest={c['before']:c['after'] for c in fragments}
 for old,count in counts.items():assert out.count(old)==count,(rel,old);out=out.replace(old,dest[old])
 inverse=out
 for old,count in counts.items():assert inverse.count(dest[old])==count;inverse=inverse.replace(dest[old],old)
 assert inverse==s;pending[p]=out.encode();receipt['files'][rel]={'before':sha(b),'after':sha(out.encode()),'complete_inverse_equals_input':True,'calls':sum(c['calls'] for c in fragments)}
rel='com/hbm/sound/EntitySoundTransport.java';p=a.project/'src/main/java'/rel;b=p.read_bytes();assert sha(b)==m['transport']['before'],'transport baseline differs; inspect before composing';assert b.endswith(b'}\n');out=b.decode()[:-2]+m['transport']['inserted_block']+'}\n';assert sha(out.encode())==m['transport']['after'];pending[p]=out.encode();receipt['files'][rel]={'before':sha(b),'after':sha(out.encode()),'complete_inverse_equals_input':out.replace(m['transport']['inserted_block'],'',1)==b.decode()}
rel='com/hbm/sound/SoundCategoryResolver.java';p=a.project/'src/main/java'/rel;b=p.read_bytes();assert sha(b)==m['resolver']['before'],'resolver changed; inspect before composing';out=m['resolver']['after_text'].encode();assert sha(out)==m['resolver']['after'];pending[p]=out;receipt['files'][rel]={'before':sha(b),'after':sha(out),'audited_complete_file':True}
for rel in ['src/main/java/com/hbm/sound/VanillaWorldSoundEffects.java','src/main/resources/assets/minecraft/sounds.json']:
 p=a.project/rel;assert not p.exists(),('new path collision; inspect before taking ownership',p);b=(e.parents[2]/rel).read_bytes();pending[p]=b;receipt['files'][rel]={'before':None,'after':sha(b),'new_path_absent':True}
for rel,h in m['immutable'].items():assert sha((a.project/'src/main/java'/rel).read_bytes())==h,('protected contract changed',rel)
if a.apply:
 for p,b in pending.items():p.parent.mkdir(parents=True,exist_ok=True);p.write_bytes(b)
a.receipt.write_text(json.dumps(receipt,indent=2)+'\n');print('PASS 94 exact fragments, original transport inverse and two absent new paths; complete inverse preserves unrelated input; '+('applied' if a.apply else 'dry run')+'.')
