#!/usr/bin/env python3
"""Recovery ONLY of the 44 frozen expressions. Exact retained before/after hashes are authoritative.
No native attribution, native execution or old proof-log regeneration occurs here.
"""
import argparse,collections,hashlib,json,re,zipfile
from pathlib import Path
H=Path(__file__).resolve().parent;M=H.parents[2]
BASE='d25dcb9f0908d76b1e9eca96b75e58f6397de355'
P='src/main/java/com/hbm/world/dungeon/'
KNOWN={
 'Antenna.java':('4cc0b5a733e13410778d3b56fa50fa410f0550ae049fc5e7d92cd96cfd4898f7','a4dcd3b096c7ceaf75cd40b8c0351c35bddf4470dcc9c8336bf653ee03afc662',7),
 'Ruin001.java':('67f6c4a379bb162811427b96e269d678dbfc4ec47fc8eb17a9a11d89a91de1b2','ccf5c1cd82b3e1b8956a3ec268aecf4b46c6a4ab6b379b2a292092d311e11042',3),
 'Ruin002.java':('6fbc06a7fc97a2b4b2dbda14cef3e799719fb184a71231f585303fb8a80cf5f8','fd3924236805bbee2360dc2bb1a4e7a6dd7a14275243a38f63bf7bcc2ff7cd5b',14),
 'Spaceship.java':('27a68e34f5a42bffd06617f7812859af971e08bd498d289212b76e2c0d0e356b','488d05d121dd7eb469b4048edd149ef30e727cc5d416f1c319b9183c2590ac53',20),
}
def sha(b):return hashlib.sha256(b).hexdigest()
p=argparse.ArgumentParser();p.add_argument('--apply',action='store_true');a=p.parse_args()
source={}
if a.apply:source={P+n:(M/P/n).read_bytes() for n in KNOWN}
else:
 with zipfile.ZipFile(H/'sources.zip')as z:source={n:z.read(n)for n in z.namelist()}
rows=[];counts=collections.Counter();files={}
for n,(before,after,count)in KNOWN.items():
 f=P+n;b=source[f];assert sha(b)==before,('before',f);s=b.decode();found=[]
 for match in re.finditer(r'\bworld\.setBlock\(([^;\n]+)\);',s):
  args=[x.strip()for x in match[1].split(',')]
  if len(args)!=6 or args[3]not in ['ModBlocks.red_cable','ModBlocks.red_wire_coated','ModBlocks.deco_steel']or args[4:]!=['0','3']:continue
  assert n!='Antenna.java' or args[3]=='ModBlocks.deco_steel'
  for axis,arg in zip('xyz',args[:3]):assert re.fullmatch(axis+r'(?:\s*[+-]\s*-?\d+)?',arg),arg
  old=match[0];new='com.hbm.world.generator.DungeonToolbox.placeUnorientedStructureBlock(world, '+', '.join(args[:4])+');'
  row={'file':f,'start':match.start(),'end':match.end(),'receiver':'world','args':args,'block':args[3][10:],'old':old,'new':new,'recovery_basis':'Exact retained whole-file before/after hashes; current native attribution pending.'}
  found.append(row);counts[args[3]]+=1
 assert len(found)==count,(n,len(found));out=s
 for r in reversed(found):out=out[:r['start']]+r['new']+out[r['end']:]
 assert sha(out.encode())==after,('after',f)
 inverse=out
 for r in reversed(found):assert inverse.count(r['new'])==1;inverse=inverse.replace(r['new'],r['old'],1)
 assert inverse.encode()==b,'exact source inverse'
 if a.apply:(M/f).write_bytes(out.encode())
 else:assert (M/f).read_bytes()==out.encode(),'current production bytes'
 rows.extend(found);files[f]={'before':before,'after':after,'placements':count}
assert dict(counts)=={'ModBlocks.deco_steel':7,'ModBlocks.red_cable':27,'ModBlocks.red_wire_coated':10}
manifest={'recovery_baseline':BASE,'recovery_tree':'b51f8971c483858304306df2f0cbbafade512a77','historical_baseline':'0aeda80b','status':'SOURCE RECOVERY VERIFIED; CURRENT NATIVE EXECUTION NOT RUN','placements':44,'counts':dict(counts),'files':files}
if a.apply:
 with zipfile.ZipFile(H/'sources.zip','w',zipfile.ZIP_DEFLATED)as z:
  for f,b in sorted(source.items()):z.writestr(f,b)
 (H/'selection.json').write_text(json.dumps(rows,indent=2)+'\n');(H/'manifest.json').write_text(json.dumps(manifest,indent=2)+'\n');(H/'source-hashes.json').write_text(json.dumps({f:sha(b)for f,b in sorted(source.items())},indent=2)+'\n')
else:assert rows==json.loads((H/'selection.json').read_text());assert manifest==json.loads((H/'manifest.json').read_text())
print('CURRENT RECOVERY PASS:44 selected replacements, four exact source inverses and all eight retained before/after SHA256 hashes. Native tests NOT run in replacement workspace.')
