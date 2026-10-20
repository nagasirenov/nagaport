#!/usr/bin/env python3
"""Compile all four complete RBMK debris owners against cached native Forge 1.12.2.
Only NTM registries, packet transport/client effect dispatch and contamination
sink are controlled collaborators. No net.minecraft/net.minecraftforge facades.
The three complete gameplay bodies are pinned to pristine modulo native APIs.
"""
import argparse, hashlib, os, subprocess, sys, tempfile
from pathlib import Path
p=argparse.ArgumentParser();p.add_argument('--java-home',type=Path,required=True);p.add_argument('--classpath',required=True)
a=p.parse_args();root=Path(__file__).resolve().parents[2];src=root/'src/main/java';old=root.parent/'ntm-original-pristine/src/main/java';tests=root/'porting/tests'
hashes={'RBMKDebris':'26dfadebbdd947cb185ef40ed47189168f9f24bab026f8ead9626ebb819a3900','RBMKDebrisBurning':'57c5d35f44c65c709868ae13dac85b08e87d4d0cdfe801d4bbb72c0876a7c924','RBMKDebrisRadiating':'4c6710b976b983299ea6f96c0e40b11d35222698dcdb796db0c1b0e5b41165c4','RBMKDebrisDigamma':'8df07d76f65be384bb764b0bc52ada3f9b7347a0a236265730da95f587ef5cf8'}
for name,digest in hashes.items():
 pristine=(old/('com/hbm/blocks/machine/rbmk/'+name+'.java')).read_bytes();assert hashlib.sha256(pristine).hexdigest()==digest
 if name=='RBMKDebris':continue
 s=pristine.decode().replace('cpw.mods.fml.common.network.NetworkRegistry.TargetPoint','net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint').replace('net.minecraftforge.common.util.ForgeDirection','com.hbm.lib.ForgeDirection').replace('net.minecraft.util.AxisAlignedBB','net.minecraft.util.math.AxisAlignedBB').replace('net.minecraft.util.Vec3;','net.minecraft.util.math.Vec3d;')
 s=s.replace('import net.minecraft.block.Block;','import net.minecraft.block.Block;\nimport net.minecraft.block.state.IBlockState;\nimport net.minecraft.util.math.BlockPos;')
 s=s.replace('public void updateTick(World world, int x, int y, int z, Random rand) {','public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {'+('' if name=='RBMKDebrisDigamma' else '\n\t\tint x = pos.getX(), y = pos.getY(), z = pos.getZ();'))
 s=s.replace('public void onBlockAdded(World world, int x, int y, int z) {\n\t\tsuper.onBlockAdded(world, x, y, z);','@Override\n\tpublic void onBlockAdded(World world, BlockPos pos, IBlockState state) {\n\t\tsuper.onBlockAdded(world, pos, state);'+('' if name=='RBMKDebrisDigamma' else '\n\t\tint x = pos.getX(), y = pos.getY(), z = pos.getZ();'))
 replacements={
 'world.provider.dimensionId':'world.provider.getDimension()',
 'world.getBlock(x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ)':'world.getBlockState(pos.add(dir.offsetX, dir.offsetY, dir.offsetZ)).getBlock()',
 'world.getBlockMetadata(x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ)':'block.getMetaFromState(world.getBlockState(pos.add(dir.offsetX, dir.offsetY, dir.offsetZ)))',
 'world.setBlock(x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ, ModBlocks.gas_meltdown)':'world.setBlockState(pos.add(dir.offsetX, dir.offsetY, dir.offsetZ), ModBlocks.gas_meltdown.getDefaultState())',
 'world.setBlock(x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ, this)':'world.setBlockState(pos.add(dir.offsetX, dir.offsetY, dir.offsetZ), getDefaultState())',
 'world.setBlock(x, y, z, ModBlocks.pribris_burning)':'world.setBlockState(pos, ModBlocks.pribris_burning.getDefaultState())',
 'world.setBlock(x, y, z, ModBlocks.pribris)':'world.setBlockState(pos, ModBlocks.pribris.getDefaultState())',
 'world.scheduleBlockUpdate(x, y, z, this,':'world.scheduleUpdate(pos, this,',
 'world.getBlockMetadata(x, y, z)':'getMetaFromState(world.getBlockState(pos))',
 'world.setBlockMetadataWithNotify(x, y, z, meta + 1, 2)':'world.setBlockState(pos, getStateFromMeta(meta + 1), 2)',
 'Blocks.air':'Blocks.AIR',
 'AxisAlignedBB.getBoundingBox(':'new AxisAlignedBB(',
 '.expand(range, range, range)':'.grow(range, range, range)',
 'Vec3 vec = Vec3.createVectorHelper(':'Vec3d vec = new Vec3d(',
 'vec.lengthVector()':'vec.length()',
 'vec.xCoord':'vec.x','vec.yCoord':'vec.y','vec.zCoord':'vec.z',
 'world.getBlock(ix, iy, iz)':'world.getBlockState(new BlockPos(ix, iy, iz)).getBlock()',
 'DamageSource.inFire':'DamageSource.IN_FIRE',
 'p.getHeldItem() != null':'!p.getHeldItemMainhand().isEmpty()',
 'p.getHeldItem()':'p.getHeldItemMainhand()'}
 for before,after in replacements.items():s=s.replace(before,after)
 if name!='RBMKDebrisDigamma':
  s=s.replace('import net.minecraft.init.Blocks;','import net.minecraft.init.Blocks;\nimport net.minecraft.init.SoundEvents;\nimport net.minecraft.util.SoundCategory;')
  s=s.replace('world.playSoundEffect(x + 0.5F, y + 0.5, z + 0.5, "fire.fire",','world.playSound(null, x + 0.5F, y + 0.5, z + 0.5, SoundEvents.BLOCK_FIRE_AMBIENT, SoundCategory.BLOCKS,')
 assert s==(src/('com/hbm/blocks/machine/rbmk/'+name+'.java')).read_text(), name+' changed beyond exact native API substitutions'
print('PASS pristine gameplay body parity: all three full subclasses, original branch/constants/random order/ray formula/payload bodies',flush=True)
collaborators={
'com/hbm/blocks/ModBlocks.java':'''package com.hbm.blocks;public class ModBlocks {public static net.minecraft.block.Block gas_meltdown,foam_layer,block_foam,sand_boron_layer,sand_mix,pribris,pribris_burning,corium_block,block_corium;}''',
'com/hbm/blocks/generic/BlockNTMSand.java':'''package com.hbm.blocks.generic;public class BlockNTMSand {public enum EnumSandType {BORON,LEAD,URANIUM,POLONIUM,QUARTZ}}''',
'com/hbm/items/ModItems.java':'''package com.hbm.items;public class ModItems {public static net.minecraft.item.Item marshmallow;}''',
'com/hbm/main/MainRegistry.java':'''package com.hbm.main;public class MainRegistry {public static final Proxy proxy=new Proxy();public static class Proxy {public final java.util.List<net.minecraft.nbt.NBTTagCompound> effects=new java.util.ArrayList<>();public void effectNT(net.minecraft.nbt.NBTTagCompound data){effects.add(data);}}}''',
'com/hbm/packet/toclient/AuxParticlePacketNT.java':'''package com.hbm.packet.toclient;public class AuxParticlePacketNT {public final net.minecraft.nbt.NBTTagCompound data;public final double x,y,z;public AuxParticlePacketNT(net.minecraft.nbt.NBTTagCompound n,double x,double y,double z){this.data=n;this.x=x;this.y=y;this.z=z;}}''',
'com/hbm/handler/threading/PacketThreading.java':'''package com.hbm.handler.threading;public class PacketThreading {public static final java.util.List<com.hbm.packet.toclient.AuxParticlePacketNT> packets=new java.util.ArrayList<>();public static final java.util.List<net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint> targets=new java.util.ArrayList<>();public static void createAllAroundThreadedPacket(com.hbm.packet.toclient.AuxParticlePacketNT p,net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint t){packets.add(p);targets.add(t);}}''',
'com/hbm/util/ContaminationUtil.java':'''package com.hbm.util;public class ContaminationUtil {public enum HazardType {RADIATION}public enum ContaminationType {CREATIVE}public static final java.util.List<net.minecraft.entity.EntityLivingBase> entities=new java.util.ArrayList<>();public static final java.util.List<Float> rads=new java.util.ArrayList<>();public static void contaminate(net.minecraft.entity.EntityLivingBase e,HazardType h,ContaminationType c,float rad){if(h!=HazardType.RADIATION||c!=ContaminationType.CREATIVE)throw new AssertionError();entities.add(e);rads.add(rad);}}'''}
production=['com/hbm/blocks/machine/rbmk/'+name+'.java' for name in hashes]+['com/hbm/lib/ForgeDirection.java']
exe='.exe' if os.name=='nt' else ''
with tempfile.TemporaryDirectory(prefix='rbmk-debris-native-') as tmp:
 out=Path(tmp);files=[]
 for rel,source in collaborators.items():
  assert rel.startswith('com/hbm/'), 'No native facades allowed'
  f=out/rel;f.parent.mkdir(parents=True,exist_ok=True);f.write_text(source);files.append(str(f))
 for rel in production:
  f=src/rel;print('PRODUCTION',rel,hashlib.sha256(f.read_bytes()).hexdigest(),flush=True);files.append(str(f))
 f=out/'RBMKDebrisNativeTest.java';f.write_text((tests/'rbmk-debris-native-test.java.in').read_text());files.append(str(f))
 compile=subprocess.run([str(a.java_home/'bin'/('javac'+exe)),'-encoding','UTF-8','-source','8','-target','8','-cp',a.classpath,'-d',tmp]+files)
 if compile.returncode:sys.exit(compile.returncode)
 result=subprocess.run([str(a.java_home/'bin'/('java'+exe)),'-Xmx512m','-Djava.awt.headless=true','-cp',os.pathsep.join([tmp,a.classpath]),'RBMKDebrisNativeTest'],cwd=tmp)
 sys.exit(result.returncode)
