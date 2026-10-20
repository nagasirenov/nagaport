import java.util.*;
import com.hbm.blocks.*;
import com.hbm.blocks.generic.BlockFallingTint;
import com.hbm.entity.item.EntityFallingBlockNT;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.*;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.util.math.*;

public class NativeFallingTest {
 static int checks;
 static void check(boolean b,String m){checks++;if(!b)throw new AssertionError(m);}
 static class World extends net.minecraft.world.World {
  Map<BlockPos,IBlockState> states=new HashMap<>(); List<Entity> spawned=new ArrayList<>(),targets=new ArrayList<>(); boolean area=true, rejectPlacement; int scheduled; net.minecraft.tileentity.TileEntity tile;
  World(){super(new net.minecraft.world.storage.SaveHandlerMP(),new net.minecraft.world.storage.WorldInfo(new NBTTagCompound()),new net.minecraft.world.WorldProviderSurface(),new net.minecraft.profiler.Profiler(),false);}
  protected net.minecraft.world.chunk.IChunkProvider createChunkProvider(){return null;}
  protected boolean isChunkLoaded(int x,int z,boolean empty){return true;}
  public boolean isAreaLoaded(BlockPos a,BlockPos b){return area;}
  public BlockPos getSpawnPoint(){return BlockPos.ORIGIN;}
  public IBlockState getBlockState(BlockPos p){return states.getOrDefault(p,Blocks.AIR.getDefaultState());}
  public boolean setBlockState(BlockPos p,IBlockState s,int flags){if(rejectPlacement)return false;states.put(p.toImmutable(),s);return true;}
  public boolean setBlockToAir(BlockPos p){states.remove(p);return true;}
  public boolean spawnEntity(Entity e){spawned.add(e);return true;}
  public void scheduleUpdate(BlockPos p,Block b,int delay){check(delay==2,"two tick schedule");scheduled++;}
  public List<AxisAlignedBB> getCollisionBoxes(Entity e,AxisAlignedBB box){List<AxisAlignedBB> list=new ArrayList<>();for(Map.Entry<BlockPos,IBlockState> entry:states.entrySet()){AxisAlignedBB bb=entry.getValue().getCollisionBoundingBox(this,entry.getKey());if(bb!=null){bb=bb.offset(entry.getKey());if(bb.intersects(box))list.add(bb);}}return list;}
  public List<Entity> getEntitiesWithinAABBExcludingEntity(Entity e,AxisAlignedBB box){return targets;}
  public boolean isMaterialInBB(AxisAlignedBB bb,Material material){return false;}
  public boolean isFlammableWithin(AxisAlignedBB bb){return false;}
  public boolean isRainingAt(BlockPos p){return false;}
  public net.minecraft.tileentity.TileEntity getTileEntity(BlockPos p){return tile;}
  public void markChunkDirty(BlockPos p,net.minecraft.tileentity.TileEntity t){}
 }
 static class Falling extends EntityFallingBlockNT {
  Falling(World w,Block b,int meta){super(w,.5,5.5,.5,b,meta);}
  Falling(World w){super(w);}
  NBTTagCompound save(){NBTTagCompound n=new NBTTagCompound();writeEntityToNBT(n);return n;}
  void load(NBTTagCompound n){readEntityFromNBT(n);}
 }
 static class Target extends Entity {
  float damage; DamageSource source;
  Target(World w){super(w);}protected void entityInit(){}protected void writeEntityToNBT(NBTTagCompound n){}protected void readEntityFromNBT(NBTTagCompound n){}
  public boolean attackEntityFrom(DamageSource s,float d){source=s;damage=d;return true;}
 }
 static class CountingBlock extends BlockFallingNT {int lands; @Override public void onLand(net.minecraft.world.World w,int x,int y,int z,int m){lands++;check(m==9,"native onLand argument");}}
 static class Spot extends BlockFallingNT implements ISpotlight {public int getBeamLength(){return 5;}}
 static void register(Block b,String name){b.setRegistryName("hbm",name);net.minecraftforge.fml.common.registry.ForgeRegistries.BLOCKS.register(b);}
 static void lifecycle() {
  World w=new World();CountingBlock owner=new CountingBlock();register(owner,"falling_probe");BlockPos source=new BlockPos(0,5,0);w.states.put(source,owner.getStateFromMeta(9));w.states.put(BlockPos.ORIGIN,Blocks.STONE.getDefaultState());
  owner.onBlockAdded(w,source,owner.getDefaultState());owner.neighborChanged(owner.getDefaultState(),w,source,Blocks.STONE,source.down());check(w.scheduled==2,"native schedule callbacks");
  owner.updateTick(w,source,owner.getStateFromMeta(9),new Random(1));check(w.spawned.size()==1,"loaded spawn");EntityFallingBlockNT falling=(EntityFallingBlockNT)w.spawned.get(0);check(falling.getMeta()==9,"metadata sync");
  falling.onUpdate();check(w.getBlockState(source).getBlock()==Blocks.AIR&&!falling.isDead,"source removed once");check(Math.abs(falling.motionY-(-.04*.98))<1e-10,"gravity and drag");
  for(int i=0;i<80&&!falling.isDead;i++)falling.onUpdate();check(falling.isDead,"actual native collision reaches ground");check(w.getBlockState(new BlockPos(0,1,0)).getBlock()==owner,"native landing placement");check(w.getBlockState(new BlockPos(0,1,0)).getValue(BlockFallingNT.META)==9,"landing metadata");check(owner.lands==1,"onLand called once");
  w=new World();w.area=false;w.states.put(source,owner.getStateFromMeta(9));w.states.put(BlockPos.ORIGIN,Blocks.STONE.getDefaultState());owner.updateTick(w,source,owner.getStateFromMeta(9),new Random());check(w.spawned.isEmpty()&&w.getBlockState(new BlockPos(0,1,0)).getBlock()==owner,"unloaded instant path");check(w.getBlockState(new BlockPos(0,1,0)).getValue(BlockFallingNT.META)==0,"original instant metadata reset");
  w=new World();Falling missing=new Falling(w,Blocks.SAND,0);missing.onUpdate();check(missing.isDead&&w.spawned.isEmpty(),"missing source does not duplicate");
  w=new World();Spot spot=new Spot();register(spot,"spot_probe");Falling light=new Falling(w,spot,0);light.onUpdate();check(light.isDead&&light.fallingTicks==0,"ISpotlight exempt");
  for(Block b:new Block[]{Blocks.AIR,Blocks.FIRE,Blocks.WATER,Blocks.LAVA}){w.states.put(source,b.getDefaultState());check(BlockFallingNT.canFallThrough(w,0,5,0),"passable material");}w.states.put(source,Blocks.STONE.getDefaultState());check(!BlockFallingNT.canFallThrough(w,0,5,0),"solid support");
 }
 static class TileBlock extends BlockFallingNT implements ITileEntityProvider { public net.minecraft.tileentity.TileEntity createNewTileEntity(net.minecraft.world.World w,int meta){return new DataTile();} }
 static class DataTile extends net.minecraft.tileentity.TileEntity { int custom; public NBTTagCompound writeToNBT(NBTTagCompound n){n.setInteger("x",getPos().getX());n.setInteger("y",getPos().getY());n.setInteger("z",getPos().getZ());n.setInteger("custom",custom);return n;}public void readFromNBT(NBTTagCompound n){setPos(new BlockPos(n.getInteger("x"),n.getInteger("y"),n.getInteger("z")));custom=n.getInteger("custom");} public void markDirty(){} }
 static void landingEdges() {
  World pistonWorld=new World();pistonWorld.states.put(BlockPos.ORIGIN,Blocks.STONE.getDefaultState());pistonWorld.states.put(new BlockPos(0,5,0),Blocks.SAND.getDefaultState());pistonWorld.states.put(new BlockPos(0,1,0),Blocks.PISTON_EXTENSION.getDefaultState());Falling pistonFall=new Falling(pistonWorld,Blocks.SAND,0);for(int i=0;i<80&&!pistonFall.isDead;i++)pistonFall.onUpdate();check(!pistonFall.isDead&&pistonWorld.spawned.isEmpty(),"original moving piston landing exemption");

  World w=new World();w.states.put(BlockPos.ORIGIN,Blocks.STONE.getDefaultState());w.states.put(new BlockPos(0,5,0),Blocks.SAND.getDefaultState());Falling f=new Falling(w,Blocks.SAND,0);f.onUpdate();w.rejectPlacement=true;for(int i=0;i<80&&!f.isDead;i++)f.onUpdate();check(f.isDead&&w.spawned.size()==1,"failed landing drops exactly once");
  TileBlock owner=new TileBlock();register(owner,"tile_probe");w=new World();w.states.put(BlockPos.ORIGIN,Blocks.STONE.getDefaultState());w.states.put(new BlockPos(0,5,0),owner.getDefaultState());DataTile tile=new DataTile();tile.setPos(new BlockPos(0,1,0));w.tile=tile;f=new Falling(w,owner,0);f.tileNBT=new NBTTagCompound();f.tileNBT.setInteger("x",55);f.tileNBT.setInteger("y",55);f.tileNBT.setInteger("z",55);f.tileNBT.setInteger("custom",42);for(int i=0;i<80&&!f.isDead;i++)f.onUpdate();check(tile.custom==42&&tile.getPos().equals(new BlockPos(0,1,0)),"tile merge excludes xyz and retains destination");
  w=new World();Falling anvil=new Falling(w,Blocks.ANVIL,0);Target target=new Target(w);w.targets.add(target);anvil.func_145806_a(true);anvil.fall(100F,1);check(anvil.getMeta()==4&&target.source==DamageSource.ANVIL,"vanilla anvil degradation updates native sync");anvil.fall(100F,1);check(anvil.getMeta()==8,"second anvil damage stage");anvil.fall(100F,1);try{java.lang.reflect.Field field=EntityFallingBlockNT.class.getDeclaredField("destroyOnLand");field.setAccessible(true);check(field.getBoolean(anvil),"third anvil damage destroys");}catch(Exception e){throw new RuntimeException(e);}
 }
 static void constructorQuirks() { World w=new World();Falling spawned=new Falling(w,Blocks.SAND,0),loaded=new Falling(w);check(spawned.width==.6F&&spawned.height==1.8F,"original spawn constructor dimensions");check(loaded.width==.98F&&loaded.height==.98F,"original load constructor dimensions"); }
 static void damageAndNBT() {
  World w=new World();Falling f=new Falling(w,Blocks.SAND,0);Target target=new Target(w);w.targets.add(target);f.fall(8.3F,1);check(target.damage==0,"damage disabled default");f.func_145806_a(true);f.fall(8.3F,1);check(target.damage==16&&target.source==DamageSource.FALLING_BLOCK,"original ceil/floor damage");f.fall(100F,1);check(target.damage==40,"damage cap");
  f.fallingTicks=590;f.canDrop=false;f.tileNBT=new NBTTagCompound();f.tileNBT.setString("custom","persist");NBTTagCompound n=f.save();Falling restored=new Falling(w);restored.load(n);check(restored.getBlock()==Blocks.SAND&&restored.getMeta()==0&&restored.fallingTicks==(590&255)&&!restored.canDrop,"native synced NBT roundtrip and original unsigned-byte time");check(restored.tileNBT.getString("custom").equals("persist"),"tile NBT retained");
  n.setByte("Time",(byte)230);restored.load(n);check(restored.fallingTicks==230,"legacy byte time accepted");n.setInteger("TileID",0);restored.load(n);check(restored.getBlock()==Blocks.SAND,"air NBT fallback sync");
  Falling client=new Falling(w);client.getBlock();client.getMeta();client.getDataManager().setEntryValues(f.getDataManager().getAll());check(client.getBlock()==Blocks.SAND,"no stale initial watcher cache");
  try {net.minecraft.network.play.server.SPacketEntityMetadata packet=new net.minecraft.network.play.server.SPacketEntityMetadata(f.getEntityId(),f.getDataManager(),true);net.minecraft.network.PacketBuffer bytes=new net.minecraft.network.PacketBuffer(io.netty.buffer.Unpooled.buffer());packet.writePacketData(bytes);net.minecraft.network.play.server.SPacketEntityMetadata received=new net.minecraft.network.play.server.SPacketEntityMetadata();received.readPacketData(bytes);Falling remote=new Falling(w);remote.getDataManager().setEntryValues(received.getDataManagerEntries());check(remote.getBlock()==Blocks.SAND&&remote.getMeta()==0,"actual metadata packet wire roundtrip");bytes.release();}catch(Exception ex){throw new RuntimeException(ex);}
  w=new World();f=new Falling(w,Blocks.SAND,0);f.fallingTicks=600;f.onUpdate();check(f.isDead&&w.spawned.size()==1&&w.spawned.get(0) instanceof EntityItem,"timeout item drop");check(((EntityItem)w.spawned.get(0)).getItem().getCount()==1,"single dropped item");
  w=new World();f=new Falling(w,Blocks.SAND,0);f.fallingTicks=600;f.canDrop=false;f.onUpdate();check(f.isDead&&w.spawned.isEmpty(),"drop disabled");
 }
 static void tint() {
  BlockFallingTint b=new BlockFallingTint(Material.SAND);for(int m=0;m<16;m++){check(b.getMetaFromState(b.getStateFromMeta(m))==m,"all native metadata");check(BlockFallingTint.color(m)==java.awt.Color.HSBtoRGB(0F,0F,1F-m/15F),"original tint formula");}
  for(int x:new int[]{0,-9,10000,Integer.MAX_VALUE})for(int y:new int[]{0,7,256})for(int z:new int[]{-17,0,500}){long l=(long)(x*3129871)^(long)y*116129781L^(long)z;l=l*l*42317861L+l*11L;check(BlockFallingTint.textureIndex(new BlockPos(x,y,z))==(int)(l>>16&3L),"original overflow-aware positional texture");}
 }
 public static void main(String[] args){Bootstrap.register();constructorQuirks();lifecycle();landingEdges();damageAndNBT();tint();System.out.println("PASS native falling "+checks+" assertions; native World collision/move, source removal, metadata, instant branch, damage, timeout drops, NBT/data manager, spotlight exemption, tint");}
}
