import java.lang.reflect.*;
import java.net.*;
import java.io.*;
import java.util.*;
import com.hbm.world.WorldBlockState;
import net.minecraft.block.*;
import net.minecraft.block.state.*;
import net.minecraft.init.*;
import net.minecraft.profiler.Profiler;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.*;
import net.minecraft.world.*;
import net.minecraft.world.storage.*;
import net.minecraft.world.chunk.*;
import net.minecraft.world.chunk.storage.*;

public class WorldWriteTest {
 static final sun.misc.Unsafe U; static int checks;
 static {try {Field f=sun.misc.Unsafe.class.getDeclaredField("theUnsafe");f.setAccessible(true);U=(sun.misc.Unsafe)f.get(null);}catch(Exception e){throw new RuntimeException(e);}}
 static void check(boolean b,String m){checks++;if(!b)throw new AssertionError(m);}
 static Field field(Class<?> c,String n)throws Exception{Field f=c.getDeclaredField(n);f.setAccessible(true);return f;}
 static boolean valid(int x,int y,int z){return x>=-30000000&&x<30000000&&z>=-30000000&&z<30000000&&y>=0&&y<256;}
 static Block[] blocks; static int[] metadata={0,0,0,0,1,0,0,0};
 static class NativeWorld extends World {
  final Map<Long,Chunk> chunks=new HashMap<>(); int requests; StringBuilder events=new StringBuilder();
  NativeWorld(boolean remote){super(new SaveHandlerMP(),new WorldInfo(new NBTTagCompound()),new WorldProviderSurface(),new Profiler(),remote);provider.setWorld(this);chunkProvider=createChunkProvider();}
  protected IChunkProvider createChunkProvider(){return new IChunkProvider(){
   public Chunk getLoadedChunk(int x,int z){return chunks.get(ChunkPos.asLong(x,z));}
   public Chunk provideChunk(int x,int z){requests++;long key=ChunkPos.asLong(x,z);if(!chunks.containsKey(key))chunks.put(key,new Chunk(NativeWorld.this,x,z));return chunks.get(key);}
   public boolean tick(){return false;} public String makeString(){return "bounded test chunks";}
   public boolean isChunkGeneratedAt(int x,int z){return getLoadedChunk(x,z)!=null;}
  };}
  protected boolean isChunkLoaded(int x,int z,boolean allow){return chunks.containsKey(ChunkPos.asLong(x,z));}
  public boolean checkLight(BlockPos p){return true;}
  public void notifyBlockUpdate(BlockPos p,IBlockState before,IBlockState after,int flags){events.append('u');}
  public void notifyNeighborsRespectDebug(BlockPos p,Block block,boolean observers){events.append('n');check(observers,"native bit-one observer notification");check(block==Blocks.STONE,"notify old owner");}
  public void updateComparatorOutputLevel(BlockPos p,Block block){events.append('c');}
  public void updateObservingBlocksAt(BlockPos p,Block block){events.append('o');}
 }
 static void nativeStorage(){
  NativeWorld w=new NativeWorld(true);
  int[] h={Integer.MIN_VALUE,-30000001,-30000000,-17,-1,0,16,29999999,30000000,Integer.MAX_VALUE};
  for(int bi=0;bi<blocks.length;bi++)for(int x:h)for(int z:h)for(int y:new int[]{-1,0,64,255,256}){
   Block block=blocks[bi];int meta=metadata[bi];IBlockState target=block.getStateFromMeta(meta);
   Chunk c=new Chunk(w,x>>4,z>>4);w.chunks.clear();w.chunks.put(ChunkPos.asLong(x>>4,z>>4),c);
   if(y>=0&&y<256){ExtendedBlockStorage st=new ExtendedBlockStorage(y&~15,true);st.set(x&15,y&15,z&15,Blocks.STONE.getDefaultState());c.getBlockStorageArray()[y>>4]=st;}
   w.requests=0;boolean result=WorldBlockState.write(w,x,y,z,target,3);
   check(result==valid(x,y,z),"native changed return "+block);check((w.requests>0)==valid(x,y,z),"native prechunk bounds");
   if(valid(x,y,z)){IBlockState actual=c.getBlockState(new BlockPos(x,y,z));check(actual==target,"actual native Chunk state identity");check(block.getMetaFromState(actual)==meta,"actual native metadata retained");check(!WorldBlockState.write(w,x,y,z,target,3),"native unchanged return");}
  }
 }
 static void originalStorage(File jar)throws Exception{
  try(URLClassLoader l=new URLClassLoader(new URL[]{jar.toURI().toURL()},WorldWriteTest.class.getClassLoader())){
   Class.forName("kl",true,l).getMethod("b").invoke(null);
   Class<?> world=Class.forName("ahb",true,l),client=Class.forName("bjf",true,l),chunk=Class.forName("apx",true,l),storage=Class.forName("apz",true,l),provider=Class.forName("apu",true,l),block=Class.forName("aji",true,l),dimension=Class.forName("aqo",true,l);
   Object w=U.allocateInstance(client);Object dim=dimension.getMethod("a",int.class).invoke(null,0);field(world,"t").set(w,dim);field(world,"E").setBoolean(w,true);field(world,"C").set(w,Class.forName("qi",true,l).newInstance());field(world,"u").set(w,new ArrayList<>());
   final Object[] current={null};final int[] calls={0};
   Object proxy=java.lang.reflect.Proxy.newProxyInstance(l,new Class[]{provider},(p,m,a)->{if(m.getReturnType()==chunk){calls[0]++;return current[0];}if(m.getReturnType()==boolean.class)return false;throw new AssertionError(m.toString());});field(world,"v").set(w,proxy);
   Method write=world.getMethod("d",int.class,int.class,int.class,block,int.class,int.class),shortWrite=world.getMethod("b",int.class,int.class,int.class,block),read=world.getMethod("a",int.class,int.class,int.class),metaRead=world.getMethod("e",int.class,int.class,int.class);
   Object stone=block.getMethod("e",int.class).invoke(null,1);
   int[] h={Integer.MIN_VALUE,-30000001,-30000000,-17,-1,0,16,29999999,30000000,Integer.MAX_VALUE};
   for(int bi=0;bi<blocks.length;bi++)for(int x:h)for(int z:h)for(int y:new int[]{-1,0,64,255,256}){
    int id=Block.getIdFromBlock(blocks[bi]),meta=metadata[bi];Object target=block.getMethod("e",int.class).invoke(null,id);
    Object c=chunk.getConstructor(world,int.class,int.class).newInstance(w,x>>4,z>>4);current[0]=c;
    if(y>=0&&y<256){Object st=storage.getConstructor(int.class,boolean.class).newInstance(y&~15,true);storage.getMethod("a",int.class,int.class,int.class,block).invoke(st,x&15,y&15,z&15,stone);Array.set(field(chunk,"u").get(c),y>>4,st);}
    calls[0]=0;check((Boolean)write.invoke(w,x,y,z,target,meta,3)==valid(x,y,z),"old changed return");check((calls[0]>0)==valid(x,y,z),"old prechunk bounds");
    if(valid(x,y,z)){check(read.invoke(w,x,y,z)==target,"old stored block owner");check((Integer)metaRead.invoke(w,x,y,z)==meta,"old stored raw metadata");check(!(Boolean)write.invoke(w,x,y,z,target,meta,3),"old unchanged return");if(meta==0)check(!(Boolean)shortWrite.invoke(w,x,y,z,target),"old short overload means metadata zero");}
   }
   Object c=chunk.getConstructor(world,int.class,int.class).newInstance(w,0,0);current[0]=c;
   Object dirt=block.getMethod("e",int.class).invoke(null,3);
   for(int raw:new int[]{-1,0,1,15,16,17,Integer.MIN_VALUE,Integer.MAX_VALUE}){
    write.invoke(w,0,64,0,dirt,raw,3);check((Integer)metaRead.invoke(w,0,64,0)==(raw&15),"original stored metadata is low four bits, not arbitrary native state");
   }
  }
 }
 static void conversions(){
  for(int i=0;i<blocks.length;i++){Block b=blocks[i];int meta=metadata[i];IBlockState expected=b.getStateFromMeta(meta);check(expected.getBlock()==b&&b.getMetaFromState(expected)==meta,"exact representable vanilla metadata");
   // The conversion resolves to the same immutable, precomputed state every time.
   for(int n=0;n<100;n++)check(b.getStateFromMeta(meta)==expected,"total stable singleton conversion");
  }
  Block[] excluded={Blocks.TORCH,Blocks.CHEST,Blocks.NETHER_WART,Blocks.DIAMOND_BLOCK,Blocks.DIAMOND_BLOCK,Blocks.LIT_REDSTONE_LAMP};int[] meta={0,0,5,5,8,5};
  for(int i=0;i<meta.length;i++){boolean loss=false;try{loss=excluded[i].getMetaFromState(excluded[i].getStateFromMeta(meta[i]))!=meta[i];}catch(IllegalArgumentException expected){loss=true;}check(loss,"excluded raw metadata is not natively representable");}
 }
 static StringBuilder trace=new StringBuilder();static World selected;static boolean returned;
 static World receiver(){trace.append('r');return selected;}static int arg(int n){trace.append(n);return n==2?64:n;}
 static IBlockState state(){trace.append('s');return Blocks.DIRT.getStateFromMeta(1);}static int flags(){trace.append('f');return 2;}
 static class Observed extends NativeWorld {
  Observed(){super(true);}public boolean setBlockState(BlockPos p,IBlockState s,int flags){trace.append('w');check(p.equals(new BlockPos(1,64,3)),"coordinate order");check(s==Blocks.DIRT.getStateFromMeta(1)&&flags==2,"exact supplied state and flags");return returned;}
 }
 static void order(){
  selected=new Observed();for(boolean b:new boolean[]{true,false}){returned=b;trace.setLength(0);check(WorldBlockState.write(receiver(),arg(1),arg(2),arg(3),state(),flags())==b&&trace.toString().equals("r123sfw"),"native virtual dispatch and return");}
  trace.setLength(0);check(!WorldBlockState.write(selected,0,256,0,Blocks.FIRE.getStateFromMeta(0),3)&&trace.length()==0,"invalid coordinates no native dispatch");
  selected=null;trace.setLength(0);try{WorldBlockState.write(receiver(),arg(1),arg(2),arg(3),state(),flags());throw new AssertionError();}catch(NullPointerException expected){check(trace.toString().equals("r123sf"),"null after args");}
  trace.setLength(0);try{WorldBlockState.write(receiver(),arg(1),(new int[0])[arg(2)],arg(3),state(),flags());throw new AssertionError();}catch(ArrayIndexOutOfBoundsException expected){check(trace.toString().equals("r12"),"argument failure precedes null");}
  try{WorldBlockState.write(null,Integer.MIN_VALUE,-1,Integer.MAX_VALUE,Blocks.AIR.getStateFromMeta(0),3);throw new AssertionError();}catch(NullPointerException expected){check(true,"invalid null throws");}
  NativeWorld w=new NativeWorld(false);w.getWorldInfo().setTerrainType(WorldType.DEBUG_ALL_BLOCK_STATES);check(!WorldBlockState.write(w,0,64,0,Blocks.DIRT.getStateFromMeta(1),3)&&w.requests==0,"native debug-world rejection retained before chunk access");
  for(boolean remote:new boolean[]{false,true})for(int f=0;f<8;f++){w=new NativeWorld(remote);w.markAndNotifyBlock(new BlockPos(0,64,0),null,Blocks.STONE.getDefaultState(),Blocks.DIRT.getStateFromMeta(1),f);String expected=((f&2)!=0&&(!remote||(f&4)==0)?"u":"")+(remote?"":((f&1)!=0?"n":"o"));check(w.events.toString().equals(expected),"native update/neighbor/observer ordering and flags "+remote+"/"+f);}
 }
 public static void main(String[] a)throws Exception{Bootstrap.register();blocks=new Block[]{Blocks.AIR,Blocks.FIRE,Blocks.GRASS,Blocks.DIRT,Blocks.DIRT,Blocks.MOSSY_COBBLESTONE,Blocks.FLOWING_LAVA,Blocks.LAVA};conversions();nativeStorage();originalStorage(new File(a[0]));order();System.out.println("PASS: "+checks+" original/native World/Chunk state writes, metadata, returns, bounds, dispatch, native flags/debug-world and evaluation assertions");}
}
