#!/usr/bin/env python3
"""Portable complete-family reversal and exact native loop resource/input boundary."""
from pathlib import Path
import hashlib,json
ROOT=Path(__file__).resolve().parents[2];TESTS=ROOT/'porting/tests'
sha=lambda b:hashlib.sha256(b).hexdigest()
def audit(overrides=None,extra_stored=None):
 overrides=overrides or {};read=lambda p:overrides[p] if p in overrides else (ROOT/p).read_bytes()
 scope=json.loads((TESTS/'native-loop-sound-source-manifest.json').read_text());resources=json.loads((TESTS/'native-loop-sound-resources.json').read_text())
 for e in scope['files']:
  data=read(e['path']);assert sha(data)==e['after_sha256'],e['path'];s=data.decode()
  for x in reversed(e['replacements']):
   o=x['after_offset'];assert s[o:o+len(x['after'])]==x['after'];s=s[:o]+x['before']+s[o+len(x['after']):]
  assert sha(s.encode())==e['before_sha256'],e['path']
 for path,want in scope['new_sources'].items():assert sha(read(path))==want,path
 shared=read(scope['integration']['path']).decode()
 assert shared.count(scope['integration']['listener'])==1,'category reload listener absent/duplicated'
 for body in scope['integration']['original_audio_callbacks']:assert shared.count(body)==1,'original ClientProxy audio callback changed'
 j=resources['sounds_json'];data=read(j['path']);assert sha(data)==j['after_sha256'];s=data.decode()
 for x in reversed(j['replacements']):assert s.count(x['after'])==1;s=s.replace(x['after'],x['before'])
 assert sha(s.encode())==j['before_sha256']
 original=ROOT.parent/'ntm-original-pristine';original_json=json.loads((original/j['path']).read_text());current=json.loads(data)
 for key in resources['events']:
  expected=json.loads(json.dumps(original_json[key]));expected['sounds']=['hbm:'+v if isinstance(v,str) else dict(v,name='hbm:'+v['name']) for v in expected['sounds']];assert expected==current[key],key
 stored={f.relative_to(ROOT).as_posix() for f in (ROOT/'src/main/resources/assets/hbm/sounds').rglob('*') if f.is_file()};stored.update(extra_stored or [])
 for e in resources['renames']:assert e['before'] not in stored and e['after'] in stored,e['after']
 for e in resources['samples']:
  data=read(e['path']);assert sha(data)==e['sha256'];assert data==(original/e['original']).read_bytes(),e['path']
 for e in resources['call_sites']:assert e['expression'] in read(e['path']).decode(),e['path']
 assert len(scope['files'])==10 and len(scope['new_sources'])==2 and len(resources['events'])==69 and len(resources['samples'])==68 and len(resources['renames'])==43
 return scope,resources
if __name__=='__main__':
 scope,resources=audit();mutations=0
 paths=[e['path'] for e in scope['files']]+list(scope['new_sources'])+[resources['sounds_json']['path']]+[e['path'] for e in resources['samples']]
 for path in paths:
  try:audit({path:(ROOT/path).read_bytes()+b' '})
  except AssertionError:mutations+=1
  else:raise AssertionError('Accepted source/resource drift: '+path)
 for before in [e['before'] for e in resources['renames']]:
  try:audit(extra_stored=[before])
  except AssertionError:mutations+=1
  else:raise AssertionError('Accepted legacy case duplicate: '+before)
 integration=scope['integration'];path=integration['path'];data=(ROOT/path).read_text()
 for needle in [integration['listener']]+integration['original_audio_callbacks']:
  try:audit({path:data.replace(needle,needle+' /* changed */' if needle==integration['listener'] else needle.replace('audio','changed_audio'),1).encode()} if needle!=integration['listener'] else {path:data.replace(needle,'',1).encode()})
  except AssertionError:mutations+=1
  else:raise AssertionError('Accepted shared integration drift')
 print('PASS complete 10-source reversal, 2 native helper pins, 3 original audio callbacks/reload listener, 69 selected original events, 68 byte-identical samples, 43 portable renames; '+str(mutations)+' drift mutations rejected')
