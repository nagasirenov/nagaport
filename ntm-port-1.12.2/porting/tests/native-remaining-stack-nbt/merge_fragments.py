#!/usr/bin/env python3
"""Merge owned fragments only, preserving unrelated edits. Default is dry run.
Usage: python3 merge_fragments.py /path/to/ntm-port-1.12.2 [--apply]
A changed owned expression is a hard conflict, never a whole-file overwrite.
"""
import difflib,hashlib,json,subprocess,sys
from pathlib import Path
HERE=Path(__file__).resolve().parent
manifest=json.loads((HERE/'manifest.json').read_text())
root=Path(sys.argv[1]).resolve();repo=Path(subprocess.check_output(['git','-C',str(root),'rev-parse','--show-toplevel'],text=True).strip())
updates=[]
for f in manifest['files']:
 path=root/f['path']; repo_path=path.relative_to(repo).as_posix()
 old=subprocess.check_output(['git','-C',str(repo),'show',manifest['baseline']+':'+repo_path]).decode('utf-8')
 assert hashlib.sha256(old.encode()).hexdigest()==f['before_sha256'], 'baseline mismatch: '+repo_path
 current=path.read_bytes().decode('utf-8')
 if hashlib.sha256(current.encode()).hexdigest()==f['after_sha256']:
  continue
 blocks=[]
 old_lines=old.splitlines(keepends=True);new_lines=current.splitlines(keepends=True)
 ao=[0];bo=[0]
 for line in old_lines:ao.append(ao[-1]+len(line))
 for line in new_lines:bo.append(bo[-1]+len(line))
 for tag,a1,a2,b1,b2 in difflib.SequenceMatcher(None,old_lines,new_lines,autojunk=False).get_opcodes():
  if tag=='equal':blocks.append(difflib.Match(ao[a1],bo[b1],ao[a2]-ao[a1]))
  elif tag=='replace':
   for block in difflib.SequenceMatcher(None,old[ao[a1]:ao[a2]],current[bo[b1]:bo[b2]],autojunk=False).get_matching_blocks():
    blocks.append(difflib.Match(ao[a1]+block.a,bo[b1]+block.b,block.size))
 edits=[]
 for e in f['edits']:
  matches=[b for b in blocks if b.a<=e['start'] and e['end']<=b.a+b.size]
  assert len(matches)==1, 'Concurrent change in owned fragment: '+f['path']+':'+str(e['line'])+' '+e['before']
  b=matches[0]; start=b.b+e['start']-b.a;end=start+e['end']-e['start']
  assert current[start:end]==e['before']
  edits.append((start,end,e['after']))
 for start,end,new in sorted(edits,reverse=True):current=current[:start]+new+current[end:]
 updates.append((path,current))
if '--apply' in sys.argv:
 for path,current in updates:path.write_bytes(current.encode('utf-8'))
print(('APPLIED' if '--apply' in sys.argv else 'DRY RUN PASS')+': '+str(len(updates))+' files; only exact owned fragments changed')
