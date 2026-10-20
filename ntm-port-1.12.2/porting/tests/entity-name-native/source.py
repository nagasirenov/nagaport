#!/usr/bin/env python3
"""Sealed two receiver migrations and health-status override, with exact inverse."""
from pathlib import Path
import hashlib,json,subprocess
H=Path(__file__).resolve().parent;ROOT=H.parents[3];MANIFEST=json.loads((H/'manifest.json').read_text());FILES={f['path']:f for f in MANIFEST['files']}
def sha(s):return hashlib.sha256(s.encode()).hexdigest()
def forward_source(relative,text):
 f=FILES[relative];assert sha(text)==f['before_sha256'],relative+' baseline differs'
 for e in reversed(f['edits']):
  assert text[e['start']:e['end']]==e['before'];text=text[:e['start']]+e['after']+text[e['end']:]
 assert sha(text)==f['after_sha256'];return text
def reverse_source(relative,text):
 if relative not in FILES:return text
 f=FILES[relative];assert sha(text)==f['after_sha256'],relative+' migrated source differs';offset=0;positions=[]
 for e in f['edits']:
  a=e['start']+offset;positions.append((a,a+len(e['after']),e));offset+=len(e['after'])-len(e['before'])
 for a,b,e in reversed(positions):
  assert text[a:b]==e['after'];text=text[:a]+e['before']+text[b:]
 assert sha(text)==f['before_sha256'];return text
def verify():
 rejected=0
 for relative,f in FILES.items():
  current=(ROOT/relative).read_bytes().decode();old=subprocess.check_output(['git','-C',str(ROOT),'show',MANIFEST['baseline']+':'+relative]).decode()
  assert reverse_source(relative,current)==old and forward_source(relative,old)==current
  for e in f['edits']:
   for bad in [e['after'].replace('getName','getDisplayName'),e['after'].replace('getName','getCommandSenderName'),e['after']+' ']:
    mutation=current.replace(e['after'],bad,1)
    try:reverse_source(relative,mutation)
    except AssertionError:rejected+=1
    else:raise AssertionError('accepted selector mutation')
  try:reverse_source(relative,current+'\n')
  except AssertionError:rejected+=1
  else:raise AssertionError('accepted unrelated mutation')
 for f in MANIFEST['audit_files']:assert sha((ROOT/f['path']).read_bytes().decode())==f['sha256'],f['path']+' audited excluded/closure source changed'
 # Exact inventory: neither excluded calls nor declarations can silently enter the patch.
 found=[]
 for p in sorted((ROOT/'ntm-port-1.12.2/src/main/java').rglob('*.java')):
  s=p.read_bytes().decode()
  if 'getCommandSenderName' in s:found.extend((str(p.relative_to(ROOT)),i+1,l.strip()) for i,l in enumerate(s.splitlines()) if 'getCommandSenderName' in l and not l.lstrip().startswith('//'))
 assert len(found)==34,found # 32 unselected calls plus both cart declarations.
 assert rejected==12
 print('PASS exact three-file inverse; 2 guarded/registered player selectors + EntityDummy override; 12 drift controls rejected; 32 excluded calls and both cart declarations unchanged.')
if __name__=='__main__':verify()
