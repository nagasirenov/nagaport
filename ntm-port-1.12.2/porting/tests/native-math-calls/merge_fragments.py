#!/usr/bin/env python3
"""Apply audited UTF16 fragments onto a separately changed CP52 descendant, rejecting overlapping edits. Dry-run by default."""
import pathlib,argparse,subprocess,difflib
import check_source as guard
def matching_blocks(old,current):
 if old==current:return [difflib.Match(0,0,len(old))]
 left=old.splitlines(True);right=current.splitlines(True);lo=[0];ro=[0]
 for line in left:lo.append(lo[-1]+len(line))
 for line in right:ro.append(ro[-1]+len(line))
 result=[]
 for tag,i,j,k,l in difflib.SequenceMatcher(None,left,right,autojunk=False).get_opcodes():
  if tag=='equal':result.append(difflib.Match(lo[i],ro[k],lo[j]-lo[i]))
  elif tag=='replace':
   for b in difflib.SequenceMatcher(None,old[lo[i]:lo[j]],current[ro[k]:ro[l]],autojunk=False).get_matching_blocks():
    if b.size:result.append(difflib.Match(lo[i]+b.a,ro[k]+b.b,b.size))
 return result
p=argparse.ArgumentParser();p.add_argument('target',type=pathlib.Path);p.add_argument('--apply',action='store_true');a=p.parse_args();changes=[]
for rel,e in guard.data['files'].items():
 base=subprocess.check_output(['git','-C',str(a.target),'show',guard.data['base']+':ntm-port-1.12.2/'+rel]);assert guard.digest(base)==e['before_sha256']
 path=a.target/'ntm-port-1.12.2'/rel;raw=path.read_bytes();old=base.decode('utf8');current=raw.decode('utf8');blocks=matching_blocks(old,current);edits=[]
 for edit in e['edits']:
  start=len(old.encode('utf-16-le')[:edit['start']*2].decode('utf-16-le'));end=len(old.encode('utf-16-le')[:edit['end']*2].decode('utf-16-le'))
  choices={(b.b+start-b.a,b.b+end-b.a) for b in blocks if b.a<=start and end<=b.a+b.size}
  assert len(choices)==1,('overlap or ambiguous insertion',rel,edit)
  s,t=choices.pop();assert current[s:t]==edit['before'];edits.append((s,t,edit['after']))
 result=current
 for s,t,new in sorted(edits,reverse=True):result=result[:s]+new+result[t:]
 changes.append((path,raw,result.encode('utf8')))
if a.apply:
 for path,before,after in changes:assert path.read_bytes()==before,'concurrent modification'
 for path,before,after in changes:path.write_bytes(after)
print('PASS',len(changes),'files: fragments are nonoverlapping with existing changes;', 'applied' if a.apply else 'dry run only')
