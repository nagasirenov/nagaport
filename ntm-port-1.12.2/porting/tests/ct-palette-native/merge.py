#!/usr/bin/env python3
"""Merge ONLY identical selected lines into a later tree; refuse any changed call interior."""
import argparse,collections,difflib,hashlib,json,zipfile
from pathlib import Path
H=Path(__file__).resolve().parent
p=argparse.ArgumentParser();p.add_argument('--module',type=Path,required=True);p.add_argument('--apply',action='store_true');p.add_argument('--receipt',type=Path);a=p.parse_args()
rows=json.loads((H/'selection.json').read_text());groups=collections.defaultdict(list)
for r in rows:groups[r['file']].append(r)
outputs={};receipt=[]
with zipfile.ZipFile(H/'sources.zip')as z:
 for f,rs in groups.items():
  before=z.read(f).decode();current=(a.module/f).read_bytes().decode();base_lines=before.splitlines(True);now_lines=current.splitlines(True);ops=difflib.SequenceMatcher(None,base_lines,now_lines,autojunk=False).get_opcodes();starts=[0]
  for l in now_lines:starts.append(starts[-1]+len(l))
  edits=[]
  for r in rs:
   assert '\n'not in r['old'];line=before[:r['start']].count('\n');col=r['start']-(before.rfind('\n',0,r['start'])+1);op=next(o for o in ops if o[1]<=line<o[2]);assert op[0]=='equal','Changed placement line requires explicit composition: '+f+':'+str(line+1)
   new_line=op[3]+line-op[1];lo=starts[new_line]+col;hi=lo+len(r['old']);assert current[lo:hi]==r['old'];edits.append((lo,hi,r['old'],r['new']));receipt.append({'file':f,'start':lo,'end':hi,'old':r['old'],'new':r['new']})
  out=current
  for lo,hi,old,new in sorted(edits,reverse=True):out=out[:lo]+new+out[hi:]
  # Exact inverse also proves all preceding independent edits survived.
  offset=0;inverse=[]
  for lo,hi,old,new in sorted(edits):inverse.append((lo+offset,lo+offset+len(new),old));offset+=len(new)-(hi-lo)
  back=out
  for lo,hi,old in reversed(inverse):back=back[:lo]+old+back[hi:]
  assert back==current;outputs[f]=out.encode()
# Only the existing helper's descriptive comment changes; no behavior or initialization changes.
helper_path='src/main/java/com/hbm/world/generator/DungeonToolbox.java'
current=(a.module/helper_path).read_text();comment=json.loads((H/'helper-comment.json').read_text())
assert current.count(comment['old'])==1 and 'placeUnorientedStructureBlock' in current
out=current.replace(comment['old'],comment['new'],1);assert out.replace(comment['new'],comment['old'],1)==current
outputs[helper_path]=out.encode();receipt.append({'file':helper_path,'old':comment['old'],'new':comment['new'],'before_sha256':hashlib.sha256(current.encode()).hexdigest(),'after_sha256':hashlib.sha256(out.encode()).hexdigest()})
if a.apply:
 assert a.receipt,'--receipt required with --apply'
 for f,b in outputs.items():(a.module/f).write_bytes(b)
 a.receipt.parent.mkdir(parents=True,exist_ok=True);a.receipt.write_text(json.dumps(receipt,indent=2)+'\n')
print('PASS',len(rows),'placement fragments across',len(outputs),'files; identical selected lines and exact inverse preserve all independent edits. '+('APPLIED'if a.apply else 'CHECK ONLY'))
