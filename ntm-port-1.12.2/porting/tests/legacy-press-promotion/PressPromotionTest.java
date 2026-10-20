import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.machine.MachinePress;
import com.hbm.blocks.machine.MachineEPress;
import com.hbm.tileentity.LegacyPressMetadataUpgrade;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.tileentity.machine.TileEntityMachinePress;
import com.hbm.tileentity.machine.TileEntityMachineEPress;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.*;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.*;
import net.minecraft.world.*;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;

public class PressPromotionTest {
 static int checks;static void ck(boolean b,String s){checks++;if(!b)throw new AssertionError(s);}
 interface Force {void promote(World w,BlockPos p);}
 public static class P extends MachinePress implements Force {
  int breaks;P(){super(Material.IRON);}public void promote(World w,BlockPos p){super.fixLegacyMonoblock(w,p.getX(),p.getY(),p.getZ());}
  public void breakBlock(World w,int x,int y,int z,Block b,int m){breaks++;super.breakBlock(w,x,y,z,b,m);}
 }
 public static class E extends MachineEPress implements Force {
  int breaks;E(){super(Material.IRON);}public void promote(World w,BlockPos p){super.fixLegacyMonoblock(w,p.getX(),p.getY(),p.getZ());}
  public void breakBlock(World w,int x,int y,int z,Block b,int m){breaks++;super.breakBlock(w,x,y,z,b,m);}
 }
 public static class W extends WorldWriteTest.NativeWorld {
  int removals,lastFlags;Runnable notification;List<String> order=new ArrayList<>();
  W(){super(false);}
  public boolean isAreaLoaded(BlockPos a,BlockPos b){return true;}
  public void removeTileEntity(BlockPos p){removals++;order.add("remove");super.removeTileEntity(p);}
  public boolean setBlockState(BlockPos p,IBlockState s,int flags){lastFlags=flags;return super.setBlockState(p,s,flags);}
  public void notifyBlockUpdate(BlockPos p,IBlockState old,IBlockState now,int flags){order.add("client");super.notifyBlockUpdate(p,old,now,flags);}
  public void notifyNeighborsRespectDebug(BlockPos p,Block b,boolean observers){ck(observers,"flags3 includes native observer notification");order.add("neighbor-observer");if(notification!=null){Runnable r=notification;notification=null;r.run();}}
 }
 static class F {
  final W w;final BlockDummyable block;final BlockPos pos=new BlockPos(5,64,5);final TileEntityMachineBase tile;final ItemStack[] identities;
  F(boolean electric,int meta){w=new W();block=electric?new E():new P();tile=electric?new TileEntityMachineEPress():new TileEntityMachinePress();Chunk c=new Chunk(w,0,0);w.chunks.put(ChunkPos.asLong(0,0),c);c.markLoaded(true);c.setTerrainPopulated(true);c.setLightPopulated(true);ExtendedBlockStorage st=new ExtendedBlockStorage(64,true);st.set(5,0,5,block.getStateFromMeta(meta));c.getBlockStorageArray()[4]=st;w.setTileEntity(pos,tile);identities=new ItemStack[tile.slots.length];
   for(int i=0;i<identities.length;i++){ItemStack stack=new ItemStack(Items.DIAMOND,7+i);NBTTagCompound tag=new NBTTagCompound();tag.setInteger("fixture",100+i);stack.setTagCompound(tag);tile.slots[i]=stack;identities[i]=stack;}
   tile.setCustomName("legacy contents");tile.muffled=true;
   if(electric){TileEntityMachineEPress t=(TileEntityMachineEPress)tile;t.power=43210;t.press=123;t.renderPress=12.5;t.lastPress=11.5;}
   else{TileEntityMachinePress t=(TileEntityMachinePress)tile;t.speed=222;t.burnTime=333;t.press=123;t.renderPress=12.5;t.lastPress=11.5;}
  }
  IBlockState state(int meta){return block.getStateFromMeta(meta);}
  void retain(int target){ck(w.getBlockState(pos)==state(target),"stored actual owner/state");ck(w.getTileEntity(pos)==tile&&!tile.isInvalid(),"same valid real machine-base tile identity");ck(w.removals==0,"no removal");ck(tile.getBlockMetadata()==(target&15),"native tile cache is canonical; original raw-cache difference is explicit");ck(tile.getName().equals("legacy contents")&&tile.muffled,"inherited name and muffled retained");for(int i=0;i<identities.length;i++)ck(tile.slots[i]==identities[i]&&tile.slots[i].getCount()==7+i&&tile.slots[i].getTagCompound().getInteger("fixture")==100+i,"real machine-base inventory and NBT aliases retained");
   if(tile instanceof TileEntityMachinePress){TileEntityMachinePress t=(TileEntityMachinePress)tile;ck(t.speed==222&&t.burnTime==333&&t.press==123&&t.renderPress==12.5&&t.lastPress==11.5,"press persisted and transient fields retained");ck(((P)block).breaks==0,"no break/drop");}
   else{TileEntityMachineEPress t=(TileEntityMachineEPress)tile;ck(t.power==43210&&t.press==123&&t.renderPress==12.5&&t.lastPress==11.5,"electric persisted and transient fields retained");ck(((E)block).breaks==0,"no break/drop");}
  }
 }
 static void callbacks(){
  for(boolean electric:new boolean[]{false,true})for(boolean tick:new boolean[]{false,true})for(int meta=0;meta<16;meta++){
   F f=new F(electric,meta);if(tick)f.block.updateTick(f.w,f.pos,f.state(meta),new Random(1));else f.block.neighborChanged(f.state(meta),f.w,f.pos,Blocks.AIR,f.pos.north());
   f.retain(meta<12?(meta+10)&15:meta);if(meta<12)ck(f.w.lastFlags==3,"original flags retained");
  }
  for(boolean electric:new boolean[]{false,true})for(int meta=0;meta<16;meta++){
   F f=new F(electric,meta);((Force)f.block).promote(f.w,f.pos);f.retain((meta+10)&15);
   ck(f.tile.shouldRefresh(f.w,f.pos,f.state(meta),f.state(meta+10)),"one-shot consumed before caller returns");
  }
  System.out.println("PASS full production Press/EPress blocks, complete base neighbor/tick paths: all16 inputs; callback0..11 promotions,12..15 unchanged cores; direct scoped promotions all16");
 }
 static void normalWrites(){
  for(boolean electric:new boolean[]{false,true})for(int meta=0;meta<16;meta++)for(int delta:new int[]{1,10}){
   F f=new F(electric,meta);int next=(meta+delta)&15;ck(f.w.setBlockState(f.pos,f.state(next),3),"ordinary full write succeeds");ck(f.tile.isInvalid()&&f.w.removals==1&&f.w.getTileEntity(f.pos)!=f.tile,"ordinary full write still refreshes/replaces old tile");
  }
  for(boolean electric:new boolean[]{false,true}){
   F f=new F(electric,2);((Force)f.block).promote(f.w,f.pos);f.retain(12);ck(f.w.setBlockState(f.pos,f.state(13),3),"subsequent full write succeeds");ck(f.tile.isInvalid()&&f.w.getTileEntity(f.pos)!=f.tile,"no persistent retention after promotion");
  }
  for(boolean electric:new boolean[]{false,true}){F f=new F(electric,2);Force foreign=electric?new E():new P();foreign.promote(f.w,f.pos);ck(f.w.getBlockState(f.pos)==f.state(12),"foreign callback receiver does not replace stored block identity");ck(f.tile.isInvalid()&&f.w.getTileEntity(f.pos)!=f.tile,"foreign callback owner cannot arm retention");}
  System.out.println("PASS ordinary same-block full writes (including exact +10), and wrong callback owners, preserve native refresh behavior");
 }
 static void reentrantNotification(){
  for(boolean electric:new boolean[]{false,true}){
   F f=new F(electric,2);f.w.notification=()->{ck(f.w.getTileEntity(f.pos)==f.tile&&!f.tile.isInvalid(),"original tile present at first observer seam");ck(f.tile.shouldRefresh(f.w,f.pos,f.state(2),f.state(12)),"guard consumed before notification");ck(f.w.setBlockState(f.pos,f.state(13),3),"reentrant ordinary full write");};
   ((Force)f.block).promote(f.w,f.pos);ck(f.tile.isInvalid()&&f.w.removals==1&&f.w.getTileEntity(f.pos)!=f.tile,"reentrant notification not retained");ck(f.w.order.indexOf("client")<f.w.order.indexOf("neighbor-observer"),"actual native client update before neighbor/observer seam");
  }
  System.out.println("PASS one-shot consumption precedes native notification/reentrant full write");
 }
 static void guardControls()throws Exception{
  F f=new F(false,2);LegacyPressMetadataUpgrade g=new LegacyPressMetadataUpgrade();IBlockState before=f.state(2),after=f.state(12);
  g.run(f.tile,f.block,f.w,f.pos,()->{ck(g.consume(f.tile,f.w,f.pos,before,after),"exact scope accepted");ck(!g.consume(f.tile,f.w,f.pos,before,after),"cannot consume twice");});
  for(int which=0;which<6;which++){
   final int n=which;F other=new F(false,2);if(n==0){other.tile.setWorld(f.w);other.tile.setPos(f.pos);}g.run(f.tile,f.block,f.w,f.pos,()->{
    TileEntity t=n==0?other.tile:f.tile;World w=n==1?other.w:f.w;BlockPos p=n==2?f.pos.east():f.pos;IBlockState b=n==3?f.state(3):before,a=n==4?f.state(13):after;
    if(n==5){AtomicBoolean got=new AtomicBoolean(true);Thread thread=new Thread(()->got.set(g.consume(t,w,p,b,a)));thread.start();try{thread.join();}catch(InterruptedException ex){throw new RuntimeException(ex);}ck(!got.get(),"wrong thread rejected");}else ck(!g.consume(t,w,p,b,a),"wrong tile/world/pos/state rejected "+n);
    ck(!g.consume(f.tile,f.w,f.pos,before,after),"mismatch consumes expectation "+n);
   });
  }
  g.run(f.tile,f.block,f.w,f.pos,()->{});ck(!g.consume(f.tile,f.w,f.pos,before,after),"no-op action cleans up");
  try{g.run(f.tile,f.block,f.w,f.pos,()->{throw new IllegalStateException("fixture");});throw new AssertionError();}catch(IllegalStateException expected){}ck(!g.consume(f.tile,f.w,f.pos,before,after),"exception cleans up");
  g.run(f.tile,f.block,f.w,f.pos,()->ck(!f.w.setBlockState(f.pos,before,3),"actual same-state no-op"));ck(!g.consume(f.tile,f.w,f.pos,before,after),"native no-op cleanup");
  g.run(f.tile,f.block,f.w,f.pos,()->ck(!f.w.setBlockState(f.pos.up(256),after,3),"actual bounds rejection"));ck(!g.consume(f.tile,f.w,f.pos,before,after),"native rejected-boundary cleanup");
  ((TileEntityMachinePress)f.tile).withLegacyPromotion(f.block,f.w,f.pos,()->{f.w.getWorldInfo().setTerrainType(WorldType.DEBUG_ALL_BLOCK_STATES);try{ck(!f.w.setBlockState(f.pos,after,3),"actual debug-world rejection after arming");}finally{f.w.getWorldInfo().setTerrainType(WorldType.DEFAULT);}});ck(f.w.getTileEntity(f.pos)==f.tile&&f.w.getBlockState(f.pos)==before,"debug rejected scoped promotion");ck(f.tile.shouldRefresh(f.w,f.pos,before,after),"production field clears after debug rejection");
  g.run(f.tile,f.block,f.w,f.pos,()->g.run(f.tile,f.block,f.w,f.pos,()->ck(!g.consume(f.tile,f.w,f.pos,before,after),"nested pre-refresh scope revokes outer expectation")));ck(!g.consume(f.tile,f.w,f.pos,before,after),"nested cleanup");
  for(int which=0;which<5;which++){
   F q=new F(false,2),o=new F(false,2);final int n=which;TileEntity t=n==0?o.tile:q.tile;World w=n==1?o.w:q.w;BlockPos pos=n==2?q.pos.east():q.pos;Block owner=n==3?o.block:q.block;if(n==4)q.tile.invalidate();
   g.run(t,owner,w,pos,()->ck(!g.consume(t,w,pos,q.state(2),q.state(12)),"invalid arm context rejected "+n));
  }
  F moved=new F(false,2);g.run(moved.tile,moved.block,moved.w,moved.pos,()->{moved.tile.setPos(moved.pos.east());ck(!g.consume(moved.tile,moved.w,moved.pos,moved.state(2),moved.state(12)),"tile moved after arming rejected");});
  F invalid=new F(false,2);g.run(invalid.tile,invalid.block,invalid.w,invalid.pos,()->{invalid.tile.invalidate();ck(!g.consume(invalid.tile,invalid.w,invalid.pos,invalid.state(2),invalid.state(12)),"tile invalidated after arming rejected");});
  System.out.println("PASS expected-context negatives, thread/nesting, exceptions, real no-op/bounds/debug cleanup");
 }
 public static void main(String[] args)throws Exception{Bootstrap.register();callbacks();normalWrites();reentrantNotification();guardControls();System.out.println("PASS "+checks+" native scoped-promotion checks; complete block and inventory bases; exact tile lifecycle/field projections, not complete machine processing");}
}
