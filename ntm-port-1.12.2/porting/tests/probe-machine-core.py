#!/usr/bin/env python3
"""Compile COMPLETE production MachineBase + LoadedBase, with explicit NTM-only collaborators.
Minecraft/Forge/Netty are real classpath APIs. No production source substitutions.
Atmosphere routing, DirPos, packet dispatch and packet receiver collaborator are controlled;
this is not production network threading, dimension implementation or server acceptance.
"""
import argparse, os, re, subprocess, tempfile
from pathlib import Path
p=argparse.ArgumentParser();p.add_argument('--java-home',type=Path,required=True);p.add_argument('--classpath',required=True);a=p.parse_args()
root=Path(__file__).resolve().parents[2];src=root/'src/main/java';exe='.exe' if os.name=='nt' else ''
if not (a.java_home/'bin'/('javac'+exe)).is_file() and (a.java_home.parent/'bin'/('javac'+exe)).is_file(): a.java_home=a.java_home.parent
collaborators={
'api/hbm/tile/ILoadedTile.java': 'package api.hbm.tile; public interface ILoadedTile { boolean isLoaded(); }',
'com/hbm/dim/orbit/WorldProviderOrbit.java': 'package com.hbm.dim.orbit; public class WorldProviderOrbit extends net.minecraft.world.WorldProviderSurface {}',
'com/hbm/inventory/fluid/Fluids.java': 'package com.hbm.inventory.fluid; public class Fluids { public static final Object EARTHAIR=new Object(), OXYGEN=new Object(); }',
'com/hbm/dim/trait/CBT_Atmosphere.java': 'package com.hbm.dim.trait; public class CBT_Atmosphere { public boolean breathable; public boolean hasFluid(Object type,double fraction) { return breathable; } }',
'com/hbm/dim/CelestialBody.java': 'package com.hbm.dim; public class CelestialBody { public static com.hbm.dim.trait.CBT_Atmosphere atmosphere; public static <T> T getTrait(net.minecraft.world.World w,Class<T> c){return c.cast(atmosphere);} }',
'com/hbm/handler/atmosphere/AtmosphereBlob.java': 'package com.hbm.handler.atmosphere; public class AtmosphereBlob { public boolean breathable; public int consumed; public boolean hasFluid(Object type,double fraction){return breathable;} public void consume(int n){consumed+=n;} }',
'com/hbm/handler/atmosphere/ChunkAtmosphereManager.java': 'package com.hbm.handler.atmosphere; public class ChunkAtmosphereManager { public static final ChunkAtmosphereManager proxy=new ChunkAtmosphereManager(); public java.util.List<AtmosphereBlob> blobs=new java.util.ArrayList<>(); public java.util.List<AtmosphereBlob> getBlobs(net.minecraft.world.World w,int x,int y,int z){return blobs;} }',
'com/hbm/util/fauxpointtwelve/DirPos.java': 'package com.hbm.util.fauxpointtwelve; public class DirPos extends net.minecraft.util.math.BlockPos { private final com.hbm.lib.ForgeDirection dir; public DirPos(int x,int y,int z,com.hbm.lib.ForgeDirection d){super(x,y,z);dir=d;} public com.hbm.lib.ForgeDirection getDir(){return dir;} }',
'com/hbm/handler/threading/PacketThreading.java': 'package com.hbm.handler.threading; public class PacketThreading { public static int calls; public static com.hbm.packet.toclient.BufPacket packet; public static net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint target; public static void createAllAroundThreadedPacket(com.hbm.packet.toclient.BufPacket p,net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint t){calls++; packet=p; target=t;} }',
}
# Exact production packet fields/constructors/fromBytes/toBytes; exclude unrelated client Handler.
packet=(src/'com/hbm/packet/toclient/BufPacket.java').read_text(encoding='utf-8')
body=packet.split('public class BufPacket extends PrecompiledPacket {',1)[1].split('\n\tpublic static class Handler',1)[0]
collaborators['com/hbm/packet/toclient/BufPacket.java']='package com.hbm.packet.toclient; import com.hbm.packet.threading.PrecompiledPacket; import com.hbm.tileentity.IBufPacketReceiver; import io.netty.buffer.ByteBuf; public class BufPacket extends PrecompiledPacket {'+body+'\n}'
production=['com/hbm/tileentity/TileEntityMachineBase.java','com/hbm/tileentity/TileEntityLoadedBase.java','com/hbm/tileentity/IBufPacketReceiver.java','com/hbm/lib/ForgeDirection.java','com/hbm/sound/AudioWrapper.java','com/hbm/packet/threading/ThreadedPacket.java','com/hbm/packet/threading/PrecompiledPacket.java']
def method(text,name):
 m=re.search(r'(?:public|private) (?:static )?(?:ItemStack|void) '+name+r'\([^)]*\)\s*\{',text);assert m,name
 i=m.end();d=1
 while d: d+=(text[i]=='{')-(text[i]=='}');i+=1
 return text[m.start():i]
proxy_fixtures=[]
for name,path,extra in [
 ('Proxy','com/hbm/tileentity/TileEntityProxyInventory.java','ISidedInventory core; ISidedInventory getBase(){return core;}'),
 ('Combo','com/hbm/tileentity/TileEntityProxyCombo.java','boolean inventory=true; Object core; Object getCoreObject(){return core;}'),
 ('PWR','com/hbm/blocks/machine/BlockPWR.java','int metadata=1; Object block=new Object(); TileEntityPWRController core; public int getBlockMetadata(){return metadata;} TileEntityPWRController getCore(){return core;}')]:
 text=(src/path).read_text(encoding='utf-8')
 proxy_fixtures.append('static class '+name+' extends Inventory {'+extra+'\n'+'\n'.join(method(text,n) for n in ['nativeStack','getStackInSlot','decrStackSize','removeStackFromSlot','setInventorySlotContents'])+'\n}')
template=(root/'porting/tests/MachineCoreNativeTest.java').read_text(encoding='utf-8').replace('// @PROXY_FIXTURES@','\n'.join(proxy_fixtures))
with tempfile.TemporaryDirectory(prefix='hbm-machine-core-') as tmp:
 out=Path(tmp);files=[]
 for relative,text in collaborators.items():
  target=out/relative;target.parent.mkdir(parents=True,exist_ok=True);target.write_text(text,encoding='utf-8');files.append(str(target))
 test=out/'MachineCoreNativeTest.java';test.write_text(template,encoding='utf-8')
 files += [str(src/f) for f in production]+[str(test)]
 subprocess.run([str(a.java_home/'bin'/('javac'+exe)),'-encoding','UTF-8','-cp',a.classpath,'-d',tmp]+files,check=True)
 subprocess.run([str(a.java_home/'bin'/('java'+exe)),'-cp',os.pathsep.join([tmp,a.classpath]),'MachineCoreNativeTest'],cwd=tmp,check=True)
