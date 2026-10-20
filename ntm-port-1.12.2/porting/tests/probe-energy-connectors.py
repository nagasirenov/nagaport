#!/usr/bin/env python3
"""Whole four energy interfaces + real Minecraft; controlled NTM nodes/packets, extracted positional collaborators. NOT topology/gameplay proof."""
import argparse, hashlib, json, os, re, subprocess, tempfile
from pathlib import Path
ROOT=Path(__file__).resolve().parents[2]
def read(p): return (ROOT/'src/main/java'/p).read_text(encoding='utf-8')
def method(s,sig):
 start=s.index(sig);end=s.index('{',start)+1;depth=1
 while depth: depth+=(s[end]=='{')-(s[end]=='}');end+=1
 return s[start:end]
def parity():
 m=json.loads(Path(__file__).with_name('energy-connector-manifest.json').read_text(encoding='utf-8'))
 for p,f in m['files'].items():
  s=(ROOT/p).read_text(encoding='utf-8')
  for r in reversed(f['replacements']):
   assert s.count(r['new'])==r['count'],p+' replacement count'
   s=s.replace(r['new'],r['old'])
  assert hashlib.sha256(s.encode('utf-8')).hexdigest()==f['before_sha256'],p+' full-file parity'
 print('PASS: four full files exactly reversible to baseline hashes without Git/pristine dependency',flush=True)
 return list(m['files'])
p=argparse.ArgumentParser(description=__doc__);p.add_argument('--classpath');p.add_argument('--java-home',type=Path);p.add_argument('--parity-only',action='store_true');a=p.parse_args();files=parity()
if a.parity_only:raise SystemExit()
if not a.classpath or not a.java_home:p.error('--classpath and --java-home required')
with tempfile.TemporaryDirectory(prefix='hbm-energy-native-') as d:
 out=Path(d); sources=[ROOT/f for f in files]
 def emit(path,body):
  dest=out/path;dest.parent.mkdir(parents=True,exist_ok=True);dest.write_text(body,encoding='utf-8');sources.append(dest)
 # Exact extraction, not a newly invented alternate positional API. Unported unrelated overloads excluded.
 bp=read('com/hbm/util/fauxpointtwelve/BlockPos.java')
 emit('com/hbm/util/fauxpointtwelve/BlockPos.java','package com.hbm.util.fauxpointtwelve; public class BlockPos { private int x,y,z;\n'+'\n'.join(method(bp,s) for s in ['public BlockPos(int','public int getX(','public int getY(','public int getZ('])+'}')
 dp=read('com/hbm/util/fauxpointtwelve/DirPos.java')
 emit('com/hbm/util/fauxpointtwelve/DirPos.java','package com.hbm.util.fauxpointtwelve; import com.hbm.lib.ForgeDirection; public class DirPos extends BlockPos { protected ForgeDirection dir;\n'+'\n'.join(method(dp,s) for s in ['public DirPos(int','public ForgeDirection getDir('])+'}')
 lib=read('com/hbm/lib/Library.java');constants=re.findall(r'public static final ForgeDirection (?:POS|NEG)_[XYZ] = ForgeDirection.\w+;',lib);assert len(constants)==6
 emit('com/hbm/lib/Library.java','package com.hbm.lib; public class Library {'+'\n'.join(constants)+'}')
 ec=read('com/hbm/util/CompatEnergyControl.java');constants=re.findall(r'public static final String L_(?:ENERGY|CAPACITY)_HE\s*=\s*"[^"]+";',ec);assert len(constants)==2
 emit('com/hbm/util/CompatEnergyControl.java','package com.hbm.util; public class CompatEnergyControl {'+'\n'.join(constants)+'}')
 emit('com/hbm/util/Compat.java','package com.hbm.util; import net.minecraft.world.World; import net.minecraft.tileentity.TileEntity; public class Compat {'+method(read('com/hbm/util/Compat.java'),'public static TileEntity getTileStandard(')+'}')
 # Explicit observation controls only. These replace NTM, never Minecraft/Forge APIs.
 emit('api/hbm/energymk2/Nodespace.java','''package api.hbm.energymk2;
import com.hbm.util.fauxpointtwelve.*;import net.minecraft.world.World;
public class Nodespace { public static PowerNode selected; public static int lookups; public static World lastWorld; public static int x,y,z;
public static PowerNode getNode(World w,int a,int b,int c){lookups++;lastWorld=w;x=a;y=b;z=c;return selected;}
public static class Net {public int providers,receivers,removed; public Object last; public void addProvider(IEnergyProviderMK2 p){providers++;last=p;} public void addReceiver(IEnergyReceiverMK2 r){receivers++;last=r;} public void removeReceiver(IEnergyReceiverMK2 r){removed++;last=r;}}
public static class PowerNode { public Net net; public BlockPos[] positions; public DirPos[] connections;public PowerNode(BlockPos... p){positions=p;}public PowerNode setConnections(DirPos... c){connections=c;return this;}}
}''')
 emit('com/hbm/packet/toclient/AuxParticlePacketNT.java','package com.hbm.packet.toclient; import net.minecraft.nbt.NBTTagCompound; public class AuxParticlePacketNT {public AuxParticlePacketNT(NBTTagCompound n,double x,double y,double z){throw new AssertionError("debug branch unexpectedly active");}}')
 emit('com/hbm/handler/threading/PacketThreading.java','package com.hbm.handler.threading; import com.hbm.packet.toclient.AuxParticlePacketNT; import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint; public class PacketThreading {public static void createAllAroundThreadedPacket(AuxParticlePacketNT p,TargetPoint t){throw new AssertionError("debug branch unexpectedly active");}}')
 sources += [ROOT/'src/main/java'/s for s in ['api/hbm/energymk2/IEnergyConnectorMK2.java','api/hbm/tile/ILoadedTile.java','com/hbm/lib/ForgeDirection.java','com/hbm/util/Tuple.java','com/hbm/interfaces/Spaghetti.java','com/hbm/interfaces/NotableComments.java']]
 sources.append(ROOT/'porting/tests/EnergyConnectorNativeTest.java')
 subprocess.run([str(a.java_home/'bin'/('javac.exe' if os.name=='nt' else 'javac')),'-encoding','UTF-8','-source','8','-target','8','-cp',a.classpath,'-d',str(out)]+list(map(str,sources)),check=True)
 subprocess.run([str(a.java_home/'bin'/('java.exe' if os.name=='nt' else 'java')),'-cp',str(out)+os.pathsep+a.classpath,'EnergyConnectorNativeTest'],check=True)
print('PASS: native whole-interface fixture; real ILoadedTile cache and extracted Compat lookup; controlled NTM nodes/packets, NOT runtime topology/gameplay',flush=True)
