#!/usr/bin/env python3
"""Complete native foundry tiles and shared node dispatcher; explicit NTM boundaries only."""
import argparse,pathlib,subprocess,tempfile,os
p=argparse.ArgumentParser();p.add_argument('--classpath',required=True);p.add_argument('--java-home',type=pathlib.Path,required=True);a=p.parse_args()
r=pathlib.Path(__file__).resolve().parents[2];s=r/'src/main/java';t=r/'porting/tests'
collaborators={
'com/hbm/blocks/ModBlocks.java':'''package com.hbm.blocks;public class ModBlocks {public static net.minecraft.block.Block foundry_channel,slag;}''',
'com/hbm/items/ModItems.java':'''package com.hbm.items;public class ModItems {public static net.minecraft.item.Item mold;}''',
'com/hbm/inventory/OreDictManager.java':'''package com.hbm.inventory;public class OreDictManager {public static class DictFrame {public final String[] mats;public DictFrame(String...names){mats=names;}}}''',
'com/hbm/inventory/material/Mats.java':'''package com.hbm.inventory.material;public class Mats {
 public static java.util.Map<Integer,NTMMaterial> matById=new java.util.HashMap<>();public static java.util.Map<String,NTMMaterial> matByName=new java.util.HashMap<>();public static java.util.List<NTMMaterial> orderedList=new java.util.ArrayList<>();public static java.util.Map<String,MaterialShapes> prefixByName=new java.util.HashMap<>();
 public static class MaterialStack {public final NTMMaterial material;public int amount;public MaterialStack(NTMMaterial material,int amount){this.material=material;this.amount=amount;}public MaterialStack copy(){return new MaterialStack(material,amount);}}
}''',
'com/hbm/items/machine/ItemMold.java':'''package com.hbm.items.machine;public class ItemMold extends net.minecraft.item.Item {
 public final java.util.Map<Integer,Mold> molds=new java.util.HashMap<>();public ItemMold(){setHasSubtypes(true);}public Mold getMold(net.minecraft.item.ItemStack stack){return molds.get(stack.getItemDamage());}
 public static class Mold {public final int size,cost;public final com.hbm.inventory.material.NTMMaterial material;public net.minecraft.item.ItemStack output;public Mold(int size,int cost,com.hbm.inventory.material.NTMMaterial mat,net.minecraft.item.ItemStack out){this.size=size;this.cost=cost;this.material=mat;this.output=out;}public int getCost(){return cost;}public net.minecraft.item.ItemStack getOutput(com.hbm.inventory.material.NTMMaterial mat){return mat==material?output:null;}}
}''',
'com/hbm/util/Compat.java':'''package com.hbm.util;public class Compat {public static final String MOD_GT6="gregtech";public static boolean isModLoaded(String mod){return false;}public static net.minecraft.tileentity.TileEntity getTileStandard(net.minecraft.world.World world,int x,int y,int z){return world.getTileEntity(new net.minecraft.util.math.BlockPos(x,y,z));}}''',
'com/hbm/lib/Library.java':'''package com.hbm.lib;public class Library {public static final ForgeDirection POS_X=ForgeDirection.EAST,NEG_X=ForgeDirection.WEST,POS_Z=ForgeDirection.SOUTH,NEG_Z=ForgeDirection.NORTH;}''',
'com/hbm/interfaces/ICopiable.java':'''package com.hbm.interfaces;public interface ICopiable {net.minecraft.nbt.NBTTagCompound getSettings(net.minecraft.world.World world,int x,int y,int z);void pasteSettings(net.minecraft.nbt.NBTTagCompound nbt,int index,net.minecraft.world.World world,net.minecraft.entity.player.EntityPlayer player,int x,int y,int z);default String[] infoForDisplay(net.minecraft.world.World world,int x,int y,int z){return null;}}''',
'com/hbm/blocks/machine/FoundryChannel.java':'''package com.hbm.blocks.machine;public class FoundryChannel {public static class FoundryNode extends com.hbm.uninos.GenNode<com.hbm.uninos.networkproviders.FoundryNetwork> {public com.hbm.inventory.material.NTMMaterial type;public FoundryNode(com.hbm.uninos.INetworkProvider<com.hbm.uninos.networkproviders.FoundryNetwork> provider,com.hbm.util.fauxpointtwelve.BlockPos...positions){super(provider,positions);}@Override public FoundryNode setConnections(com.hbm.util.fauxpointtwelve.DirPos...connections){super.setConnections(connections);return this;}}}''',
'com/hbm/blocks/generic/BlockDynamicSlag.java':'''package com.hbm.blocks.generic;public class BlockDynamicSlag {public static class TileEntitySlag extends net.minecraft.tileentity.TileEntity {public com.hbm.inventory.material.NTMMaterial mat;public int amount;public static int maxAmount=72*9*16;}}''',
'com/hbm/packet/toclient/AuxParticlePacketNT.java':'''package com.hbm.packet.toclient;public class AuxParticlePacketNT {public final net.minecraft.nbt.NBTTagCompound data;public final double x,y,z;public AuxParticlePacketNT(net.minecraft.nbt.NBTTagCompound n,double a,double b,double c){data=n.copy();x=a;y=b;z=c;}}''',
'com/hbm/packet/toclient/BufPacket.java':'''package com.hbm.packet.toclient;public class BufPacket {public BufPacket(int x,int y,int z,com.hbm.tileentity.IBufPacketReceiver receiver){}public io.netty.buffer.ByteBuf getCompiledBuffer(){throw new AssertionError("Foundry must not invent a BufPacket path");}}''',
'com/hbm/handler/threading/PacketThreading.java':'''package com.hbm.handler.threading;public class PacketThreading {public static final java.util.List<Object> packets=new java.util.ArrayList<>();public static final java.util.List<net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint> points=new java.util.ArrayList<>();public static void createAllAroundThreadedPacket(Object packet,net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint point){packets.add(packet);points.add(point);}}'''
}
paths=['com/hbm/tileentity/machine/TileEntityFoundry'+name+'.java' for name in ['Base','Channel','Tank','Outlet','Slagtap','CastingBase','Basin','Mold']]
paths+=['com/hbm/tileentity/machine/IRenderFoundry.java','com/hbm/tileentity/TileEntityLoadedBase.java','com/hbm/tileentity/IBufPacketReceiver.java','api/hbm/tile/ILoadedTile.java','api/hbm/block/ICrucibleAcceptor.java','com/hbm/sound/AudioWrapper.java','com/hbm/lib/ForgeDirection.java','com/hbm/util/CrucibleUtil.java','com/hbm/util/Vec3NT.java','com/hbm/util/Tuple.java','com/hbm/interfaces/Spaghetti.java','com/hbm/inventory/material/NTMMaterial.java','com/hbm/inventory/material/MaterialShapes.java']
paths+=['com/hbm/util/fauxpointtwelve/'+name+'.java' for name in ['BlockPos','DirPos','Rotation']]
paths+=['com/hbm/uninos/'+name+'.java' for name in ['GenNode','NodeNet','INetworkProvider','UniNodespace']]+['com/hbm/uninos/networkproviders/'+name+'.java' for name in ['FoundryNetwork','FoundryNetworkProvider']]
with tempfile.TemporaryDirectory(prefix='foundry-native-') as d:
 out=pathlib.Path(d);files=[s/path for path in paths]+[t/'NativeFoundryTilesTest.java',t/'ForgeEventNativeLauncher.java']
 for path,body in collaborators.items():
  target=out/'fixtures'/path;target.parent.mkdir(parents=True,exist_ok=True);target.write_text(body);files.append(target)
 subprocess.run([str(a.java_home/'bin/javac'),'-encoding','UTF-8','-source','8','-target','8','-sourcepath',str(out/'fixtures'),'-cp',a.classpath,'-d',d]+list(map(str,files)),check=True)
 print('PASS eight complete native foundry tiles, actual loaded tile/NBT/raytrace/material foundations and complete UniNodespace/GenNode/NodeNet/FoundryNetwork dispatcher compile',flush=True)
 subprocess.run([str(a.java_home/'bin/java'),'-cp',d+os.pathsep+a.classpath,'ForgeEventNativeLauncher','NativeFoundryTilesTest'],cwd=d,check=True)
