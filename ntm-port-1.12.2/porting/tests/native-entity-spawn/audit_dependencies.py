#!/usr/bin/env python3
"""Pin and inspect supplied original binary/Forge patch evidence, without fetching dependencies."""
import argparse,hashlib,io,json,zipfile
from pathlib import Path
H=Path(__file__).resolve().parent
a=argparse.ArgumentParser();a.add_argument('--legacy-client',type=Path,required=True);a.add_argument('--legacy-forge',type=Path,required=True);a.add_argument('--legacy-mappings',type=Path,required=True);a.add_argument('--classpath-file',type=Path,required=True);a=a.parse_args()
m=json.loads((H/'dependency-evidence.json').read_text())
for name,p in [('legacy_client',a.legacy_client),('legacy_forge',a.legacy_forge),('legacy_mappings',a.legacy_mappings)]:assert hashlib.sha256(p.read_bytes()).hexdigest()==m[name],name
with zipfile.ZipFile(a.legacy_forge)as jar:
 with zipfile.ZipFile(io.BytesIO(jar.read('forgepatches.zip')))as z:
  world=z.read('net/minecraft/world/World.java.patch').decode();entity=z.read('net/minecraft/entity/Entity.java.patch').decode();item=z.read('net/minecraft/entity/item/EntityItem.java.patch').decode();server=z.read('net/minecraft/world/WorldServer.java.patch').decode();client=z.read('net/minecraft/client/multiplayer/WorldClient.java.patch').decode()
  assert 'MinecraftForge.EVENT_BUS.post(new EntityJoinWorldEvent(p_72838_1_, this)) && !flag' in world
  assert 'p_72838_1_ == null' in world and 'restoringBlockSnapshots' in world
  assert 'new EntityEvent.EntityConstructing(this)' in entity and 'props.init(this, p_i1582_1_)' in entity
  assert 'getEntityLifespan(p_i1710_8_, p_i1710_1_)' in item
  assert 'func_72838_d' not in server and 'func_72838_d' not in client
with zipfile.ZipFile(a.legacy_mappings)as z:
 s=z.read('joined.srg').decode();assert 'MD: ahb/d (Lsa;)Z net/minecraft/world/World/func_72838_d (Lnet/minecraft/entity/Entity;)Z' in s
found={}
for p in a.classpath_file.read_text().strip().split(':'):
 if not p.endswith('.jar')or not Path(p).exists():continue
 with zipfile.ZipFile(p)as z:
  for name,digest in m['native_classes'].items():
   if name in z.namelist():assert hashlib.sha256(z.read(name)).hexdigest()==digest,name;found[name]=p
assert set(found)==set(m['native_classes'])
print('PASS pinned original client/1614 patches/mappings and native2768 World/WorldServer/WorldClient/Entity/EntityItem classes; event/snapshot/constructor-hook additions verified')
