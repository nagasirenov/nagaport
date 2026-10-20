#!/usr/bin/env python3
from pathlib import Path
import argparse,ast,json,hashlib,subprocess,tempfile,os,zipfile,re
p=argparse.ArgumentParser();p.add_argument('--module',type=Path,default=Path(__file__).resolve().parents[3]);p.add_argument('--java-home',type=Path,default=Path('/workspace/shared/nagaport-toolchain/java8'));p.add_argument('--classpath-file',type=Path,default=Path('/tmp/nagaport-ae2-native-classpath.txt'));p.add_argument('--mutants',action='store_true');p.add_argument('--compose-colored',action='store_true');p.add_argument('--compose-replaceability',action='store_true');p.add_argument('--replaceability-evidence',type=Path);p.add_argument('--legacy-client',type=Path,default=Path('/tmp/nagaport-legacy-client.jar'));a=p.parse_args();here=Path(__file__).resolve().parent;src=a.module/'src/main/java';cp=a.classpath_file.read_text().strip()
import audit
audit.verify(a.module)
for path, want in json.loads((here/'source-inputs.json').read_text()).items():
 q = here / path.split('porting/tests/legacy-press-promotion/',1)[1] if path.startswith('porting/tests/legacy-press-promotion/') else a.module/path
 assert audit.sha(q.read_bytes())==want, path
runtime=json.loads((here/'runtime-inputs.json').read_text())
assert audit.sha(a.legacy_client.read_bytes())==runtime['legacy_client_sha256']
assert cp.split(os.pathsep)==runtime['supplied_classpath']
cp=os.pathsep.join(runtime['native_classpath'])
for path,want in runtime['native_jars'].items():
 assert audit.sha(Path(path).read_bytes())==want,path
subprocess.run([os.sys.executable,str(here/'cache-audit/scan.py'),'--module',str(a.module)]+(['--compose-colored'] if a.compose_colored else [])+(['--compose-replaceability'] if a.compose_replaceability else [])+(['--replaceability-evidence',str(a.replaceability_evidence)] if a.replaceability_evidence else []),check=True)
print('PASS pinned complete compilation/projection inputs and supplied native/original engine jars',flush=True)
def method(text,signature):
 start=text.index(signature);i=text.index('{',start)+1;depth=1
 while depth:depth+=(text[i]=='{')-(text[i]=='}');i+=1
 return text[start:i]
# Reuse the explicit surrounding collaborator definitions from the existing whole-base suite.
tree=ast.parse((a.module/'porting/tests/probe-machine-core.py').read_text());fixtures={}
for n in tree.body:
 if isinstance(n,ast.Assign) and any(isinstance(t,ast.Name) and t.id=='collaborators' for t in n.targets):fixtures=ast.literal_eval(n.value)
assert fixtures
fixtures.update({'com/hbm/main/MainRegistry.java':'package com.hbm.main; public class MainRegistry {public static Object instance;}', 'com/hbm/util/CompatExternal.java':'package com.hbm.util; public class CompatExternal {public static net.minecraft.tileentity.TileEntity getCoreFromPos(net.minecraft.world.World w,int x,int y,int z){return w.getTileEntity(new net.minecraft.util.math.BlockPos(x,y,z));}}','com/hbm/tileentity/TileEntityProxyCombo.java':'package com.hbm.tileentity; public class TileEntityProxyCombo extends net.minecraft.tileentity.TileEntity {public TileEntityProxyCombo(boolean a,boolean b,boolean c){}}','com/hbm/inventory/RecipesCommon.java':'package com.hbm.inventory; public class RecipesCommon {public static class ComparableStack {public ComparableStack(net.minecraft.item.ItemStack s){}}}','com/hbm/inventory/UpgradeManagerNT.java':'package com.hbm.inventory; public class UpgradeManagerNT {public UpgradeManagerNT(){}}'})
packet=(src/'com/hbm/packet/toclient/BufPacket.java').read_text();body=packet.split('public class BufPacket extends PrecompiledPacket {',1)[1].split('\n\tpublic static class Handler',1)[0]
fixtures['com/hbm/packet/toclient/BufPacket.java']='package com.hbm.packet.toclient; import com.hbm.packet.threading.PrecompiledPacket; import com.hbm.tileentity.IBufPacketReceiver; import io.netty.buffer.ByteBuf; public class BufPacket extends PrecompiledPacket {'+body+'\n}'
for suffix in ['Press','EPress']:
 name='TileEntityMachine'+suffix;text=(src/('com/hbm/tileentity/machine/'+name+'.java')).read_text();start=text.index('{',text.index('public class '+name))+1;end=text.index('\n\tpublic '+name+'()')
 fields=text[start:end]
 fixtures['com/hbm/tileentity/machine/'+name+'.java']='package com.hbm.tileentity.machine; import net.minecraft.world.World; import net.minecraft.item.ItemStack; import com.hbm.inventory.UpgradeManagerNT; public class '+name+' extends com.hbm.tileentity.TileEntityMachineBase {'+fields+'\n'+method(text,'public '+name+'()')+'\n'+method(text,'public String getDefaultName()')+'\npublic void update(){throw new AssertionError("Processing is outside the exact lifecycle projection");}\n}'
production=['com/hbm/blocks/BlockDummyable.java','com/hbm/blocks/machine/MachinePress.java','com/hbm/blocks/machine/MachineEPress.java','com/hbm/handler/MultiblockHandlerXR.java','com/hbm/interfaces/ICopiable.java','com/hbm/blocks/ICustomBlockHighlight.java','com/hbm/handler/ThreeInts.java','com/hbm/util/Either.java','com/hbm/tileentity/IPersistentNBT.java','com/hbm/world/gen/nbt/INBTBlockTransformable.java','com/hbm/lib/ForgeDirection.java','com/hbm/world/WorldBlockState.java','com/hbm/world/WorldAirRemoval.java','com/hbm/tileentity/LegacyPressMetadataUpgrade.java','com/hbm/tileentity/TileEntityMachineBase.java','com/hbm/tileentity/TileEntityLoadedBase.java','com/hbm/tileentity/IBufPacketReceiver.java','com/hbm/sound/AudioWrapper.java','com/hbm/packet/threading/ThreadedPacket.java','com/hbm/packet/threading/PrecompiledPacket.java','api/hbm/block/IToolable.java']
with tempfile.TemporaryDirectory(prefix='press-promotion-') as tmp:
 t=Path(tmp);out=t/'classes';out.mkdir();files=[]
 for name,body in fixtures.items():
  f=t/name;f.parent.mkdir(parents=True,exist_ok=True);f.write_text(body);files.append(f)
 oldtest=(a.module/'porting/tests/rbmk-metadata-assessment/MetadataLifecycleProbe.java').read_text()
 oldmethod=method(oldtest,'static void originalMetadata(').replace('MetadataLifecycleProbe.class','OriginalPressMetadataDomain.class').replace('meta=2;meta<=5','meta=0;meta<16').replace('==meta+10,"original raw canonical nibble stored"','==((meta+10)&15),"original stored low nibble"').replace('check(read.invoke(w,5,64,5)==old,"original metadata-only path retains tile identity");','check(read.invoke(w,5,64,5)==old,"original metadata-only path retains tile identity"); check((Integer)tile.getMethod("p").invoke(old)==meta+10,"original tile cache preserves unmasked argument");')
 domain=t/'OriginalPressMetadataDomain.java';domain.write_text('import java.io.*;import java.lang.reflect.*;import java.net.*;import java.util.*;public class OriginalPressMetadataDomain {static int checks;static void check(boolean b,String m){checks++;if(!b)throw new AssertionError(m);}' + oldmethod + ' public static void main(String[] args)throws Exception{originalMetadata(new File(args[0]));System.out.println("PASS "+checks+" original all-nibble storage/tile identity/raw-cache checks; raw cache differs from native for old>=6");}}');files.append(domain)
 files += [src/x for x in production]+[here/'PressPromotionTest.java',a.module/'porting/tests/world-block-write-native/WorldWriteTest.java',a.module/'porting/tests/rbmk-metadata-assessment/MetadataLifecycleProbe.java']
 javac=[str(a.java_home/'bin/javac'),'-encoding','UTF-8','-cp',cp,'-d',str(out)]
 subprocess.run(javac+list(map(str,files)),check=True)
 command=[str(a.java_home/'bin/java'),'-cp',str(out)+os.pathsep+cp,'PressPromotionTest']
 subprocess.run(command[:-1]+['MetadataLifecycleProbe',str(a.legacy_client)],cwd=t,check=True)
 subprocess.run(command,cwd=t,check=True)
 subprocess.run(command[:-1]+['OriginalPressMetadataDomain',str(a.legacy_client)],cwd=t,check=True)
 if a.mutants:
  helper=src/'com/hbm/tileentity/LegacyPressMetadataUpgrade.java';original=helper.read_text()
  mutants=[('disable-retention',original.replace('return attempt != null','return false && attempt != null')),('always-retain',original.replace('attempt != null && attempt.thread == Thread.currentThread()','true || attempt != null && attempt.thread == Thread.currentThread()')),('persistent-guard',original.replace('        expected = null;\n        return attempt','        return attempt')),('no-finally-cleanup',original.replace('            expected = null;\n        }\n    }','        }\n    }')),('wrong-delta',original.replace('metadata + 10','metadata + 11')),('ignore-world',original.replace('attempt.world == world && ','').replace('tile.getWorld() == world && ','')),('ignore-pos',original.replace('attempt.pos.equals(pos)','true').replace('tile.getPos().equals(pos)','true')),('ignore-tile',original.replace('attempt.tile == tile','true')),('ignore-before',original.replace('attempt.before == before','true')),('ignore-after',original.replace('attempt.after == after','true')),('ignore-thread',original.replace('attempt.thread == Thread.currentThread()','true'))]
  mutant=t/'LegacyPressMetadataUpgrade.java'
  for name,code in mutants:
   assert code!=original,name;mutant.write_text(code);subprocess.run(javac+['-cp',str(out)+os.pathsep+cp,str(mutant)],check=True)
   result=subprocess.run(command,cwd=t,stdout=subprocess.PIPE,stderr=subprocess.STDOUT,text=True)
   assert result.returncode!=0,('survived',name,result.stdout)
   print('PASS rejected runtime mutant '+name,flush=True)
