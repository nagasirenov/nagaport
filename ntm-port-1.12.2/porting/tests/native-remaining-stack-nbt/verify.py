#!/usr/bin/env python3
"""Exact inverse against CP51 and finite ownership. This is structural, not runtime evidence."""
from pathlib import Path
import collections,difflib,hashlib,json,subprocess
from inventory import ROOT,eligible
HERE=Path(__file__).resolve().parent
manifest=json.loads((HERE/'manifest.json').read_text()); count=0
for file in manifest['files']:
 p=ROOT/file['path'];assert eligible(p)
 live=p.read_bytes().decode('utf-8')
 baseline=subprocess.check_output(['git','-C',str(ROOT),'show',manifest['baseline']+':ntm-port-1.12.2/'+file['path']]).decode('utf-8')
 assert hashlib.sha256(baseline.encode()).hexdigest()==file['before_sha256']
 src=baseline
 for edit in reversed(file['edits']):
  assert src[edit['start']:edit['end']]==edit['before']
  src=src[:edit['start']]+edit['after']+src[edit['end']:]
 assert hashlib.sha256(src.encode()).hexdigest()==file['after_sha256']
 blocks=[];al=src.splitlines(keepends=True);bl=live.splitlines(keepends=True);ao=[0];bo=[0]
 for line in al:ao.append(ao[-1]+len(line))
 for line in bl:bo.append(bo[-1]+len(line))
 for tag,a1,a2,b1,b2 in difflib.SequenceMatcher(None,al,bl,autojunk=False).get_opcodes():
  if tag=='equal':blocks.append(difflib.Match(ao[a1],bo[b1],ao[a2]-ao[a1]))
  elif tag=='replace':
   blocks.extend(difflib.Match(ao[a1]+z.a,bo[b1]+z.b,z.size) for z in difflib.SequenceMatcher(None,src[ao[a1]:ao[a2]],live[bo[b1]:bo[b2]],autojunk=False).get_matching_blocks())
 shift=0;positions=[]
 for edit in file['edits']:
  a=edit['start']+shift;b=a+len(edit['after'])
  assert src[a:b]==edit['after'],(file['path'],edit['line'])
  match=[x for x in blocks if x.a<=a and b<=x.a+x.size]
  assert len(match)==1, ('live migrated fragment changed',file['path'],edit['line'])
  offset=match[0].b+a-match[0].a
  assert live[offset:offset+b-a]==edit['after']
  positions.append((a,b,edit['before']))
  shift+=len(edit['after'])-len(edit['before'])
  for proof in edit['proof']:
   assert proof['receiverType']=='net.minecraft.item.ItemStack' and proof['fieldOwner']=='net.minecraft.item.ItemStack'
   assert proof['kind'] in ('read','write');count+=1
 for a,b,old in reversed(positions):src=src[:a]+old+src[b:]
 assert hashlib.sha256(src.encode()).hexdigest()==file['before_sha256']
 assert src==baseline, file['path']
 assert not any('stackSize' in e['before'] and 'stackSize' not in e['after'] for e in file['edits'])
assert count==manifest['summary']['reads']+manifest['summary']['writes']
print('PASS exact inverse: %d attributed accesses, %d fragments, %d files; manifest owns no unrelated source edits; all live migrated fragments verified' % (count,manifest['summary']['fragments'],len(manifest['files'])))
controls=collections.defaultdict(list)
for line in (HERE/'control-accesses.jsonl').read_text().splitlines():
 row=json.loads(line);controls[row['line']].append(row['kind'])
for n,line in enumerate((HERE/'controls/ReceiverControls.java').read_text().splitlines(),1):
 if '// EXPECT:' in line:
  value=line.split('// EXPECT:')[-1]; expected=[] if value=='none' else value.split(',')
  assert sorted(controls[n])==sorted(expected),(n,controls[n],expected)
print('PASS attribution controls: accepted native read/write/array/method/field receiver; rejected unrelated/shadowed type and used assignment; comments/strings untouched')
