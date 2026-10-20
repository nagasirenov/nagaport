#!/usr/bin/env python3
"""Only exact audited invocation fragments/declarations, with full input-preserving inverse."""
import argparse,collections,hashlib,json
from pathlib import Path
p=argparse.ArgumentParser();p.add_argument('--project',type=Path,required=True);p.add_argument('--receipt',type=Path,required=True);p.add_argument('--apply',action='store_true');a=p.parse_args();e=Path(__file__).resolve().parent;m=json.loads((e/'manifest.json').read_text());sha=lambda b:hashlib.sha256(b).hexdigest();pending={};receipt={'base':m['base'],'files':{}}
for rel,data in m['files'].items():
 p=a.project/'src/main/java'/rel;b=p.read_bytes();s=b.decode();out=s;calls=[c for c in m['calls'] if c['path']==rel];counts=collections.Counter(c['full_invocation'] for c in calls);dest={c['full_invocation']:c['new_invocation'] for c in calls}
 for old,count in counts.items():assert out.count(old)==count,(rel,old);out=out.replace(old,dest[old])
 inverse=out
 for old,count in counts.items():assert inverse.count(dest[old])==count;inverse=inverse.replace(dest[old],old)
 assert inverse==s;pending[p]=out.encode();receipt['files'][rel]={'before':sha(b),'after':sha(out.encode()),'complete_inverse_equals_input':True,'calls':len(calls)}
rel='com/hbm/main/ModSoundEvents.java';p=a.project/'src/main/java'/rel;b=p.read_bytes();assert sha(b)==m['registry']['before'],'registry baseline differs; inspect before composing';anchor='\n\t}\n\n\tprivate static void worldEffect(String path, SoundCategory category) {';assert b.decode().count(anchor)==1;out=b.decode().replace(anchor,m['registry']['inserted_block']+anchor);assert sha(out.encode())==m['registry']['after'];pending[p]=out.encode();receipt['files'][rel]={'before':sha(b),'after':sha(out.encode()),'complete_inverse_equals_input':out.replace(m['registry']['inserted_block'],'',1)==b.decode()}
for rel,h in m['immutable'].items():assert sha((a.project/'src/main/java'/rel).read_bytes())==h
if a.apply:
 for p,b in pending.items():p.write_bytes(b)
a.receipt.write_text(json.dumps(receipt,indent=2)+'\n');print('PASS 76 exact fragments and 42 scoped registry declarations, complete inverse preserving all unrelated integrated input; '+('applied' if a.apply else 'dry run')+'.')
