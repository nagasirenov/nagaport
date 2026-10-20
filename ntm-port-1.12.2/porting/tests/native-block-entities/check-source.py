#!/usr/bin/env python3
"""Exact frozen CP49-relative boundary; this is not runtime proof."""
from pathlib import Path
import hashlib,json,argparse
p=argparse.ArgumentParser();p.add_argument('--source-root',type=Path,required=True);a=p.parse_args();r=a.source_root.resolve();t=Path(__file__).resolve().parent
sha=lambda b:hashlib.sha256(b).hexdigest()
m=json.loads((t/'source-map.json').read_text());assert m['baseline']=='caf22cf027f33f9d0d0be366454c2f54fdba7915'
for path,e in m['files'].items():
 raw=(r/path).read_bytes();text=raw.decode()
 if e.get('ownership')=='exact_factory_fragments':
  # Shared file: certify only our three factory substitutions. Unrelated native
  # migrations are the coordinator's aggregate source-guard responsibility.
  assert path=='src/main/java/com/hbm/main/ClientProxy.java' and len(e['fragments'])==3
  restored=text
  for edit in e['fragments']:
   assert restored.count(edit['new'])==1 and restored.count(edit['old'])==0,path
   restored=restored.replace(edit['new'],edit['old'])
  for edit in reversed(e['fragments']):
   assert restored.count(edit['old'])==1 and restored.count(edit['new'])==0,path
   restored=restored.replace(edit['old'],edit['new'])
  assert restored==text,path
  continue
 assert sha(raw)==e['after_sha256'],path
 for edit in reversed(e['edits']):
  off=edit['after_offset'];assert text[off:off+len(edit['new'])]==edit['new'];text=text[:off]+edit['old']+text[off+len(edit['new']):]
 assert sha(text.encode())==e['before_sha256'],path
 for edit in reversed(e['edits']):
  off=edit['before_offset'];assert text[off:off+len(edit['old'])]==edit['old'];text=text[:off]+edit['new']+text[off+len(edit['old']):]
 assert text.encode()==raw,path
for path,h in m['new_files'].items():assert sha((r/path).read_bytes())==h,path
for path,h in json.loads((t/'original-sha256.json').read_text()).items():
 assert sha((r.parent/'ntm-original-pristine'/path).read_bytes())==h,path
 if path.startswith('src/main/resources/') or path.endswith('/ModelRubble.java'):
  assert (r/path).read_bytes()==(r.parent/'ntm-original-pristine'/path).read_bytes(),path
# Source audit of original synchronization ownership, not an inference from field numbers.
for name in ['EntityTNTPrimedBase','EntityRubble','EntityBlockSpider']:
 paths=list((r/'src/main/java/com/hbm/entity').rglob(name+'.java'));assert len(paths)==1
 assert 'dataWatcher' not in paths[0].read_text(),name
spider=(r/'src/main/java/com/hbm/entity/mob/EntityBlockSpider.java').read_text();assert 'writeEntityToNBT' not in spider and 'readEntityFromNBT' not in spider
assert not any('makeBlock(' in f.read_text() for f in (r/'src/main/java').rglob('*.java') if f.name!='EntityBlockSpider.java' and 'OreDictManager' not in f.name)
print('PASS exact CP49 source boundary: %d existing paths (ClientProxy factory fragments only), %d new paths; pinned original owners and unchanged geometry/texture assets; no entity-mechanics compilation claim.'%(len(m['files']),len(m['new_files'])))
