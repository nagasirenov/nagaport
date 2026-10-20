#!/usr/bin/env python3
"""Compile the complete four production editor blocks, including every nested tile/GUI.
Minecraft/Forge, chunks, NBT, ByteBuf, inventories and GUI classes are never replaced.
Explicit NTM collaborators below bound unrelated registries, structure execution,
LoadedBase networking, packet transport, live overlay rendering and loot catalogues.
No owner methods are copied, extracted, or substituted.
"""
import argparse, hashlib, os, subprocess, tempfile, zipfile
from pathlib import Path
p=argparse.ArgumentParser();p.add_argument('--java-home',type=Path,required=True);p.add_argument('--classpath',required=True);a=p.parse_args()
root=Path(__file__).resolve().parents[2];src=root/'src/main/java';tests=root/'porting/tests'
collaborators={
'com/hbm/blocks/ModBlocks.java':'''package com.hbm.blocks; import net.minecraft.block.Block; import net.minecraft.item.*;
public final class ModBlocks { public static Block wand_air,wand_logic,logic_block,deco_loot,wand_structure,wand_jigsaw,wand_tandem,wand_loot;
 public static Block getBlockFromStack(ItemStack s){return s==null||s.isEmpty()||!(s.getItem() instanceof ItemBlock)?null:((ItemBlock)s.getItem()).getBlock();}
 public static boolean isStructureBlock(Block b,boolean extra){if(b==null)return false;if(b==wand_air)return extra;return b==wand_structure||b==wand_jigsaw||b==wand_logic||b==wand_tandem||b==wand_loot;}
}''',
'com/hbm/items/ModItems.java':'package com.hbm.items; public final class ModItems { public static net.minecraft.item.Item wand_s; }',
'com/hbm/main/MainRegistry.java':'package com.hbm.main; public final class MainRegistry { public static MainRegistry instance=new MainRegistry(); public static final org.apache.logging.log4j.Logger logger=org.apache.logging.log4j.LogManager.getLogger("editor-native-probe"); }',
'com/hbm/tileentity/TileEntityLoadedBase.java':'''package com.hbm.tileentity; import io.netty.buffer.ByteBuf; import net.minecraft.nbt.NBTTagCompound;
public class TileEntityLoadedBase extends net.minecraft.tileentity.TileEntity {
 public boolean muffled=false,isLoaded=true; public int syncs,lastRange,dirtyCalls;
 public void serialize(ByteBuf b){b.writeBoolean(muffled);} public void deserialize(ByteBuf b){muffled=b.readBoolean();}
 public void networkPackNT(int range){syncs++;lastRange=range;}
 @Override public void markDirty(){dirtyCalls++;super.markDirty();}
 @Override public NBTTagCompound writeToNBT(NBTTagCompound n){super.writeToNBT(n);n.setBoolean("muffled",muffled);return n;}
 @Override public void readFromNBT(NBTTagCompound n){super.readFromNBT(n);muffled=n.getBoolean("muffled");}
}''',
'com/hbm/interfaces/ICopiable.java':'''package com.hbm.interfaces; import net.minecraft.world.World;import net.minecraft.entity.player.EntityPlayer;import net.minecraft.nbt.NBTTagCompound;
public interface ICopiable { NBTTagCompound getSettings(World w,int x,int y,int z); void pasteSettings(NBTTagCompound n,int index,World w,EntityPlayer p,int x,int y,int z); }''',
'com/hbm/inventory/RecipesCommon.java':'''package com.hbm.inventory; public class RecipesCommon { public static class ComparableStack { public ComparableStack(net.minecraft.item.ItemStack s){} } }''',
'com/hbm/util/i18n/I18nUtil.java':'package com.hbm.util.i18n; public final class I18nUtil { public static String resolveKey(String s,Object... args){return s;} public static String[] resolveKeyArray(String s){return new String[]{s};} }',
'com/hbm/config/StructureConfig.java':'package com.hbm.config; public final class StructureConfig { public static boolean debugStructures; public static double lootAmountFactor=1.0; }',
'com/hbm/packet/toserver/NBTControlPacket.java':'''package com.hbm.packet.toserver; public class NBTControlPacket implements net.minecraftforge.fml.common.network.simpleimpl.IMessage {
 public final net.minecraft.nbt.NBTTagCompound nbt; public final int x,y,z;
 public NBTControlPacket(net.minecraft.nbt.NBTTagCompound n,int x,int y,int z){nbt=n;this.x=x;this.y=y;this.z=z;}
 public void toBytes(io.netty.buffer.ByteBuf b){} public void fromBytes(io.netty.buffer.ByteBuf b){}
}''',
'com/hbm/packet/PacketDispatcher.java':'package com.hbm.packet; public final class PacketDispatcher { public static final com.hbm.main.NetworkHandler wrapper=new com.hbm.main.NetworkHandler("hbm"); }',
'com/hbm/main/NetworkHandler.java':'package com.hbm.main; public class NetworkHandler { public static com.hbm.packet.toserver.NBTControlPacket last;public NetworkHandler(String name){}public void sendToServer(net.minecraftforge.fml.common.network.simpleimpl.IMessage message){last=(com.hbm.packet.toserver.NBTControlPacket)message;} }',
'com/hbm/blocks/generic/LogicBlock.java':'''package com.hbm.blocks.generic; import net.minecraft.block.*;import net.minecraft.block.material.Material;import net.minecraft.world.World;import net.minecraft.tileentity.TileEntity;
public class LogicBlock extends BlockContainer { public LogicBlock(){super(Material.IRON);} public TileEntity createNewTileEntity(World w,int meta){return new TileEntityLogicBlock();}
 public static class TileEntityLogicBlock extends TileEntity { public String actionID,conditionID,interactionID;public com.hbm.lib.ForgeDirection direction;public Block disguise;public int disguiseMeta; }
}''',
'com/hbm/blocks/generic/BlockLoot.java':'''package com.hbm.blocks.generic; import net.minecraft.block.*;import net.minecraft.block.material.Material;import net.minecraft.world.World;import net.minecraft.tileentity.TileEntity;
public class BlockLoot extends BlockContainer { public BlockLoot(){super(Material.IRON);}public TileEntity createNewTileEntity(World w,int meta){return new TileEntityLoot();} public static class TileEntityLoot extends TileEntity{} }
''',
'com/hbm/itempool/ItemPool.java':'''package com.hbm.itempool; import java.util.*; import com.hbm.lib.WeightedRandomChestContent;
public class ItemPool { public static HashMap<String,ItemPool> pools=new LinkedHashMap<>();public static final List<String> calls=new ArrayList<>();public WeightedRandomChestContent[] entries;
 public static WeightedRandomChestContent[] getPool(String name){calls.add(name);ItemPool p=pools.get(name);return p==null?new WeightedRandomChestContent[0]:p.entries;}
}''',
'com/hbm/util/LootGenerator.java':'''package com.hbm.util; import net.minecraft.world.World;
public final class LootGenerator { public static final String LOOT_BOOKLET="LOOT_BOOKLET";public static int applies;public static String last;
 public static String[] getLootNames(){return new String[]{LOOT_BOOKLET,"OTHER"};}public static void applyLoot(World w,int x,int y,int z,String name){applies++;last=name;}
}''',
'com/hbm/world/gen/nbt/SpawnCondition.java':'''package com.hbm.world.gen.nbt;import java.util.*;
public class SpawnCondition {public final String name;public Map<String,JigsawPool> pools=new HashMap<>();public SpawnCondition(String name){this.name=name;}
 public JigsawPool getPool(String name){JigsawPool p=pools.get(name);return p==null?null:p.clone();}
}''',
'com/hbm/world/gen/nbt/NBTStructure.java':'''package com.hbm.world.gen.nbt; import java.util.*;import net.minecraft.world.World;import com.hbm.lib.ForgeDirection;import com.hbm.handler.ThreeInts;
public class NBTStructure {
 public static final Map<String,SpawnCondition> registry=new HashMap<>();public static final List<String> events=new ArrayList<>();public List<JigsawConnection> connections;public ForgeDirection requestedDir;public String requestedTarget;
 public static SpawnCondition getStructure(String n){return registry.get(n);}public List<JigsawConnection> getConnectionPool(ForgeDirection dir,String target){requestedDir=dir;requestedTarget=target;return connections;}
 public int rotateX(int x,int z,int base){events.add("rotateX:"+x+":"+z+":"+base);return 100+x;}public int rotateZ(int x,int z,int base){events.add("rotateZ:"+x+":"+z+":"+base);return 200+z;}
 public void build(World w,JigsawPiece piece,int x,int y,int z,int base,String name){events.add("build:"+x+":"+y+":"+z+":"+base+":"+name);}
 public static class JigsawConnection {public final ThreeInts pos;public final ForgeDirection dir;private JigsawConnection(ThreeInts pos,ForgeDirection dir,String pool,String target,boolean roll,int selection,int placement){this.pos=pos;this.dir=dir;}}
}'''
}
for owner,method,values in [('Actions','getActionNames','FODDER_WAVE,A2,A3'),('Conditions','getConditionNames','PLAYER_CUBE_5,C2,C3'),('Interactions','getInteractionNames','I1,I2,I3')]:
 collaborators['com/hbm/world/gen/util/LogicBlock'+owner+'.java']='package com.hbm.world.gen.util;public class LogicBlock'+owner+' {public static java.util.List<String> '+method+'(){return java.util.Arrays.asList('+','.join('"'+x+'"' for x in values.split(','))+');}}'
production=['com/hbm/blocks/generic/BlockWand'+n+'.java' for n in ['Jigsaw','Tandem','Logic','Loot']]
production += ['com/hbm/blocks/IBlockSideRotation.java','com/hbm/blocks/SideRotationModelState.java','com/hbm/blocks/ILookOverlay.java','com/hbm/blocks/ITooltipProvider.java','api/hbm/block/IToolable.java','com/hbm/interfaces/IControlReceiver.java','com/hbm/interfaces/IBomb.java','com/hbm/tileentity/IGUIProvider.java','com/hbm/lib/RefStrings.java','com/hbm/lib/ForgeDirection.java','com/hbm/lib/WeightedRandomChestContent.java','com/hbm/lib/LegacyWeightedLoot.java','com/hbm/util/BufferUtil.java','com/hbm/handler/ThreeInts.java','com/hbm/util/Tuple.java','com/hbm/interfaces/Spaghetti.java','com/hbm/world/gen/nbt/INBTBlockTransformable.java','com/hbm/world/gen/nbt/INBTTileEntityTransformable.java','com/hbm/world/gen/nbt/JigsawPiece.java','com/hbm/world/gen/nbt/JigsawPool.java']
with tempfile.TemporaryDirectory(prefix='editor-cube-native-') as tmp:
 out=Path(tmp);files=[]
 for rel,body in collaborators.items():
  assert rel.startswith('com/hbm/'),rel
  f=out/rel;f.parent.mkdir(parents=True,exist_ok=True);f.write_text(body);files.append(str(f));print('BOUNDED COLLABORATOR',rel,flush=True)
 for rel in production:
  f=src/rel;files.append(str(f));print('COMPLETE PRODUCTION',rel,hashlib.sha256(f.read_bytes()).hexdigest(),flush=True)
 f=out/'EditorCubeNativeTest.java';f.write_text((tests/'editor-cube-native-test.java.in').read_text());files.append(str(f))
 subprocess.run([str(a.java_home/'bin/javac'),'-encoding','UTF-8','-source','8','-target','8','-sourcepath',str(out),'-cp',a.classpath,'-d',tmp]+files,check=True)
 natives=out/'natives';natives.mkdir()
 for jar in a.classpath.split(os.pathsep):
  if 'natives-linux' in jar:
   with zipfile.ZipFile(jar) as z:
    for name in z.namelist():
     if name.endswith('.so'): (natives/Path(name).name).write_bytes(z.read(name))
 subprocess.run([str(a.java_home/'bin/java'),'-Djava.awt.headless=true','-Djava.library.path='+str(natives),'-cp',tmp+os.pathsep+a.classpath,'EditorCubeNativeTest'],cwd=tmp,check=True)
