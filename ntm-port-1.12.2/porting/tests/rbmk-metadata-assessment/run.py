#!/usr/bin/env python3
from pathlib import Path
import argparse,json,hashlib,subprocess,tempfile,os
p=argparse.ArgumentParser();p.add_argument('--module',type=Path,default=Path(__file__).resolve().parents[3]);p.add_argument('--java-home',type=Path,default=Path('/workspace/shared/nagaport-toolchain/java8'));p.add_argument('--classpath-file',type=Path,default=Path('/tmp/nagaport-ae2-native-classpath.txt'));p.add_argument('--legacy-client',type=Path,default=Path('/tmp/nagaport-legacy-client.jar'));a=p.parse_args();here=Path(__file__).resolve().parent
for name,want in json.loads((here/'source-input-hashes.json').read_text()).items():
 q=Path(name) if name.startswith('/') else a.module/name
 assert hashlib.sha256(q.read_bytes()).hexdigest()==want, str(q)
# Finite source opt-in closure; do not silently expand the affected-owner claim.
import re
owners=[]
for f in (a.module/'src/main/java').rglob('*.java'):
 if re.search(r'(?:protected|public)\s+boolean\s+isLegacyMonoblock\s*\(',f.read_text()): owners.append(str(f.relative_to(a.module)))
assert sorted(owners)==sorted(['src/main/java/com/hbm/blocks/BlockDummyable.java','src/main/java/com/hbm/blocks/machine/MachinePress.java','src/main/java/com/hbm/blocks/machine/MachineEPress.java'])
for f in ['TileEntityMachinePress.java','TileEntityMachineEPress.java']:
 assert 'shouldRefresh(' not in (a.module/'src/main/java/com/hbm/tileentity/machine'/f).read_text()
for f in ['TileEntityMachineBase.java','TileEntityLoadedBase.java']:
 assert 'shouldRefresh(' not in (a.module/'src/main/java/com/hbm/tileentity'/f).read_text()
cp=a.classpath_file.read_text().strip()
production=['com/hbm/blocks/BlockDummyable.java','com/hbm/handler/MultiblockHandlerXR.java','com/hbm/interfaces/ICopiable.java','com/hbm/blocks/ICustomBlockHighlight.java','com/hbm/handler/ThreeInts.java','com/hbm/util/Either.java','com/hbm/tileentity/IPersistentNBT.java','com/hbm/world/gen/nbt/INBTBlockTransformable.java','com/hbm/lib/ForgeDirection.java','com/hbm/world/WorldBlockState.java']
fixtures={'com.hbm.main.MainRegistry':'public class MainRegistry {public static Object instance;}','com.hbm.util.CompatExternal':'public class CompatExternal {public static net.minecraft.tileentity.TileEntity getCoreFromPos(net.minecraft.world.World w,int x,int y,int z){return w.getTileEntity(new net.minecraft.util.math.BlockPos(x,y,z));}}'}
with tempfile.TemporaryDirectory(prefix='metadata-lifecycle-') as tmp:
 t=Path(tmp);out=t/'classes';out.mkdir()
 files=[a.module/'src/main/java'/x for x in production]+[here/'MetadataLifecycleProbe.java',a.module/'porting/tests/world-block-write-native/WorldWriteTest.java']
 for name,body in fixtures.items():
  f=t/(name.replace('.','/')+'.java');f.parent.mkdir(parents=True,exist_ok=True);f.write_text('package '+name.rsplit('.',1)[0]+';'+body);files.append(f)
 subprocess.run([str(a.java_home/'bin/javac'),'-encoding','UTF-8','-cp',cp,'-d',str(out)]+list(map(str,files)),check=True)
 subprocess.run([str(a.java_home/'bin/java'),'-cp',str(out)+os.pathsep+cp,'MetadataLifecycleProbe',str(a.legacy_client)],cwd=t,check=True)
