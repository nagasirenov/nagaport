import java.io.File;
import java.lang.reflect.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import com.hbm.blocks.BlockDummyable;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;

/** Counterexample probe, not a replacement implementation of either press or RBMK. */
public class MetadataLifecycleProbe {
 static int checks;
 static void check(boolean x,String m){checks++;if(!x)throw new AssertionError(m);}
 public static class Marker extends TileEntity {
  int payload; ItemStack stack=ItemStack.EMPTY;
 }
 public static class Owner extends BlockDummyable {
  int breaks;
  Owner(){super(Material.IRON);}
  public TileEntity createNewTileEntity(World w,int meta){return meta>=12?new Marker():null;}
  public int[] getDimensions(){return new int[]{2,0,0,0,0,0};}
  public int getOffset(){return 0;}
  protected boolean isLegacyMonoblock(World w,int x,int y,int z){return w.getTileEntity(new BlockPos(x,y,z)) instanceof Marker;}
  public void breakBlock(World w,int x,int y,int z,Block b,int m){breaks++;super.breakBlock(w,x,y,z,b,m);}
 }
 public static class W extends WorldWriteTest.NativeWorld {
  int removed;
  W(){super(false);}
  public boolean isAreaLoaded(BlockPos from,BlockPos to){return true;}
  public void removeTileEntity(BlockPos pos){removed++;super.removeTileEntity(pos);}
  public void notifyNeighborsRespectDebug(BlockPos p,Block b,boolean observers){}
 }
 static void nativeUpgrade() {
  for(boolean tick:new boolean[]{false,true})for(int meta=2;meta<=5;meta++) {
   W w=new W(); Owner b=new Owner(); BlockPos pos=new BlockPos(5,64,5);
   Chunk c=new Chunk(w,0,0);w.chunks.put(ChunkPos.asLong(0,0),c);c.markLoaded(true);
   ExtendedBlockStorage st=new ExtendedBlockStorage(64,true);st.set(5,0,5,b.getStateFromMeta(meta));c.getBlockStorageArray()[4]=st;
   Marker old=new Marker();old.payload=12345;old.stack=new ItemStack(net.minecraft.init.Items.DIAMOND,37);
   w.setTileEntity(pos,old);
   check(w.getTileEntity(pos)==old,"seeded legacy tile identity");
   check(old.shouldRefresh(w,pos,b.getStateFromMeta(meta),b.getStateFromMeta(meta+10)),"native default mod tile refreshes same-block metadata transition");
   if(tick)b.updateTick(w,pos,w.getBlockState(pos),new Random(1));
   else b.neighborChanged(w.getBlockState(pos),w,pos,Blocks.AIR,pos.north());
   check(w.getBlockState(pos).getBlock()==b&&b.getMetaFromState(w.getBlockState(pos))==meta+10,"whole production base orphan callback performs legacy promotion");
   TileEntity after=w.getTileEntity(pos);
   check(after instanceof Marker&&after!=old,"actual native World/Chunk replaces marker tile");
   check(old.isInvalid()&&w.removed==1,"actual native World removal invalidates old tile once");
   check(((Marker)after).payload==0&&((Marker)after).stack.isEmpty(),"replacement loses marker payload and 37-item fixture inventory");
   check(b.breaks==0,"same-block state update does not call block removal/drop hook");
   System.out.println("OBSERVED native whole BlockDummyable "+(tick?"tick":"neighbor")+" callback: "+meta+" -> "+(meta+10)+", old tile invalidated, identity changed, marker payload 12345 -> 0, marker stack 37 -> 0, break callbacks=0");
  }
 }
 static void originalMetadata(File jar)throws Exception {
  try(URLClassLoader l=new URLClassLoader(new URL[]{jar.toURI().toURL()},MetadataLifecycleProbe.class.getClassLoader())) {
   Class.forName("kl",true,l).getMethod("b").invoke(null);
   Class<?> world=Class.forName("ahb",true,l),client=Class.forName("bjf",true,l),chunk=Class.forName("apx",true,l),storage=Class.forName("apz",true,l),provider=Class.forName("apu",true,l),block=Class.forName("aji",true,l),dimension=Class.forName("aqo",true,l),tile=Class.forName("aor",true,l),furnace=Class.forName("apg",true,l),stack=Class.forName("add",true,l);
   Object w=WorldWriteTest.U.allocateInstance(client);Object dim=dimension.getMethod("a",int.class).invoke(null,0);
   WorldWriteTest.field(world,"t").set(w,dim);WorldWriteTest.field(world,"E").setBoolean(w,true);WorldWriteTest.field(world,"C").set(w,Class.forName("qi",true,l).newInstance());
   for(String name:new String[]{"u","g","a","b","e","f"})WorldWriteTest.field(world,name).set(w,new ArrayList<>());
   final Object[] current={null};Object proxy=java.lang.reflect.Proxy.newProxyInstance(l,new Class[]{provider},(p,m,a)->{if(m.getReturnType()==chunk)return current[0];if(m.getReturnType()==boolean.class)return false;throw new AssertionError(m.toString());});WorldWriteTest.field(world,"v").set(w,proxy);
   Object furnaceBlock=block.getMethod("e",int.class).invoke(null,61),stone=block.getMethod("e",int.class).invoke(null,1);
   Method write=world.getMethod("a",int.class,int.class,int.class,int.class,int.class),read=world.getMethod("o",int.class,int.class,int.class),metaRead=world.getMethod("e",int.class,int.class,int.class);
   for(boolean remote:new boolean[]{false,true})for(int meta=2;meta<=5;meta++) {
    WorldWriteTest.field(world,"E").setBoolean(w,remote);
    Object c=chunk.getConstructor(world,int.class,int.class).newInstance(w,0,0);current[0]=c;
    Object st=storage.getConstructor(int.class,boolean.class).newInstance(64,true);storage.getMethod("a",int.class,int.class,int.class,block).invoke(st,5,0,5,furnaceBlock);storage.getMethod("a",int.class,int.class,int.class,int.class).invoke(st,5,0,5,meta);Array.set(WorldWriteTest.field(chunk,"u").get(c),4,st);
    Object old=furnace.newInstance();furnace.getField("a").setInt(old,2205);
    Object item=stack.getConstructor(block,int.class,int.class).newInstance(stone,37,0);furnace.getMethod("a",int.class,stack).invoke(old,0,item);
    chunk.getMethod("a",int.class,int.class,int.class,tile).invoke(c,5,64,5,old);
    check(read.invoke(w,5,64,5)==old,"original seeded actual furnace tile");
    check((Boolean)write.invoke(w,5,64,5,meta+10,3),"actual original World metadata change succeeds");
    check((Integer)metaRead.invoke(w,5,64,5)==meta+10,"original raw canonical nibble stored");
    check(read.invoke(w,5,64,5)==old,"original metadata-only path retains tile identity");
    check(!(Boolean)tile.getMethod("r").invoke(old),"original retained tile not invalidated");
    check(furnace.getField("a").getInt(old)==2205&&furnace.getMethod("a",int.class).invoke(old,0)==item,"original fuel counter and stack identity retained");
    System.out.println("OBSERVED original World/Chunk metadata-only remote="+remote+": "+meta+" -> "+(meta+10)+", actual furnace tile identity, fuel counter 2205 and stack identity retained");
   }
  }
 }
 public static void main(String[] args)throws Exception {
  net.minecraft.init.Bootstrap.register();nativeUpgrade();originalMetadata(new File(args[0]));
  System.out.println("PASS "+checks+" counterexample checks. Native marker fixture is not a production press/RBMK tile. Original engine is vanilla 1.7 bytecode; Forge1614 patch is separately audited, not executed.");
 }
}
