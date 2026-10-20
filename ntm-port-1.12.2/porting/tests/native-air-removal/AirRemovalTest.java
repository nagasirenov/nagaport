import java.lang.reflect.*;
import java.net.*;
import java.io.*;
import java.util.*;
import com.hbm.world.WorldAirRemoval;
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
import net.minecraft.world.gen.ChunkProviderServer;

public class AirRemovalTest {
 static final sun.misc.Unsafe U; static int checks;
 static {try {Field f=sun.misc.Unsafe.class.getDeclaredField("theUnsafe");f.setAccessible(true);U=(sun.misc.Unsafe)f.get(null);}catch(Exception e){throw new RuntimeException(e);}}
 static void check(boolean b,String m){checks++;if(!b)throw new AssertionError(m);}
 static Field field(Class<?> c,String n)throws Exception{Field f=c.getDeclaredField(n);f.setAccessible(true);return f;}
 static boolean valid(int x,int y,int z){return x>=-30000000&&x<30000000&&z>=-30000000&&z<30000000&&y>=0&&y<256;}
 static WorldServer world()throws Exception{
  WorldServer w=(WorldServer)U.allocateInstance(WorldServer.class);field(World.class,"worldInfo").set(w,new WorldInfo(new NBTTagCompound()));field(World.class,"provider").set(w,new WorldProviderSurface());w.provider.setWorld(w);field(World.class,"profiler").set(w,new Profiler());return w;
 }

 static class NativeWorld extends World {
  final Map<Long,Chunk> chunks=new HashMap<>(); int requests;
  NativeWorld(){super(new SaveHandlerMP(),new WorldInfo(new NBTTagCompound()),new WorldProviderSurface(),new Profiler(),false);provider.setWorld(this);chunkProvider=createChunkProvider();}
  protected IChunkProvider createChunkProvider(){return new IChunkProvider(){
   public Chunk getLoadedChunk(int x,int z){return chunks.get(ChunkPos.asLong(x,z));}
   public Chunk provideChunk(int x,int z){requests++;long key=ChunkPos.asLong(x,z);if(!chunks.containsKey(key))chunks.put(key,new Chunk(NativeWorld.this,x,z));return chunks.get(key);}
   public boolean tick(){return false;} public String makeString(){return "test loaded chunks";}
   public boolean isChunkGeneratedAt(int x,int z){return getLoadedChunk(x,z)!=null;}
  };}
  protected boolean isChunkLoaded(int x,int z,boolean allow){return chunks.containsKey(ChunkPos.asLong(x,z));}
  public boolean checkLight(BlockPos p){return true;}
 }
 static void nativeRemoval()throws Exception{
  NativeWorld w=new NativeWorld();
  int[] h={Integer.MIN_VALUE,-30000001,-30000000,-17,-1,0,16,29999999,30000000,Integer.MAX_VALUE};
  for(int x:h)for(int z:h)for(int y:new int[]{-1,0,64,255,256}){
   Chunk c=new Chunk(w,x>>4,z>>4);w.chunks.clear();w.chunks.put(ChunkPos.asLong(x>>4,z>>4),c);
   if(y>=0&&y<256){ExtendedBlockStorage st=new ExtendedBlockStorage(y&~15,true);st.set(x&15,y&15,z&15,Blocks.STONE.getDefaultState());c.getBlockStorageArray()[y>>4]=st;}
   w.requests=0;boolean result=WorldAirRemoval.remove(w,x,y,z);
   check(result==valid(x,y,z),"native changed return");check((w.requests>0)==valid(x,y,z),"native prechunk bounds");
   if(valid(x,y,z)){check(c.getBlockState(new BlockPos(x,y,z)).getBlock()==Blocks.AIR,"actual native Chunk removed");check(!WorldAirRemoval.remove(w,x,y,z),"native repeated air returns false");}
  }
 }
 static void originalRemoval(File jar)throws Exception{
  try(URLClassLoader l=new URLClassLoader(new URL[]{jar.toURI().toURL()},AirRemovalTest.class.getClassLoader())){
   Class.forName("kl",true,l).getMethod("b").invoke(null);
   Class<?> world=Class.forName("ahb",true,l),client=Class.forName("bjf",true,l),chunk=Class.forName("apx",true,l),storage=Class.forName("apz",true,l),provider=Class.forName("apu",true,l),block=Class.forName("aji",true,l),dimension=Class.forName("aqo",true,l);
   Object w=U.allocateInstance(client),air=Class.forName("ajn",true,l).getField("a").get(null);
   Object dim=dimension.getMethod("a",int.class).invoke(null,0);field(world,"t").set(w,dim);field(world,"E").setBoolean(w,true);field(world,"C").set(w,Class.forName("qi",true,l).newInstance());
   final Object[] current={null};final int[] calls={0};
   Object proxy=java.lang.reflect.Proxy.newProxyInstance(l,new Class[]{provider},(p,m,a)->{if(m.getReturnType()==chunk){calls[0]++;return current[0];}if(m.getReturnType()==boolean.class)return false;throw new AssertionError(m.toString());});field(world,"v").set(w,proxy);
   Method remove=world.getMethod("f",int.class,int.class,int.class),read=world.getMethod("a",int.class,int.class,int.class);
   Object stone=block.getMethod("e",int.class).invoke(null,1);
   int[] h={Integer.MIN_VALUE,-30000001,-30000000,-17,-1,0,16,29999999,30000000,Integer.MAX_VALUE};
   for(int x:h)for(int z:h)for(int y:new int[]{-1,0,64,255,256}){
    Object c=chunk.getConstructor(world,int.class,int.class).newInstance(w,x>>4,z>>4);current[0]=c;
    if(y>=0&&y<256){Object st=storage.getConstructor(int.class,boolean.class).newInstance(y&~15,true);storage.getMethod("a",int.class,int.class,int.class,block).invoke(st,x&15,y&15,z&15,stone);Array.set(field(chunk,"u").get(c),y>>4,st);}
    calls[0]=0;check((Boolean)remove.invoke(w,x,y,z)==valid(x,y,z),"genuine old changed return");check((calls[0]>0)==valid(x,y,z),"genuine old prechunk bounds");
    if(valid(x,y,z)){check(read.invoke(w,x,y,z)==air,"genuine old chunk removed");check(!(Boolean)remove.invoke(w,x,y,z),"genuine old repeated air false");}
   }
  }
 }
 static StringBuilder trace=new StringBuilder();static World selected;static boolean returned;
 static World receiver(){trace.append('r');return selected;}static int arg(int n){trace.append(n);return n==2?64:n;}
 static class Observed extends NativeWorld {
  public boolean setBlockToAir(BlockPos p){trace.append('a');check(p.equals(new BlockPos(1,64,3)),"coordinate order");return returned;}
 }
 static void order(){
  selected=new Observed();for(boolean b:new boolean[]{true,false}){returned=b;trace.setLength(0);check(WorldAirRemoval.remove(receiver(),arg(1),arg(2),arg(3))==b&&trace.toString().equals("r123a"),"native virtual dispatch and return");}
  trace.setLength(0);check(!WorldAirRemoval.remove(selected,0,256,0)&&trace.length()==0,"invalid coordinates no native dispatch");
  selected=null;trace.setLength(0);try{WorldAirRemoval.remove(receiver(),arg(1),arg(2),arg(3));throw new AssertionError();}catch(NullPointerException expected){check(trace.toString().equals("r123"),"null after args");}
  trace.setLength(0);try{WorldAirRemoval.remove(receiver(),arg(1),(new int[0])[arg(2)],arg(3));throw new AssertionError();}catch(ArrayIndexOutOfBoundsException expected){check(trace.toString().equals("r12"),"argument failure precedes null");}
  try{WorldAirRemoval.remove(null,Integer.MIN_VALUE,-1,Integer.MAX_VALUE);throw new AssertionError();}catch(NullPointerException expected){check(true,"invalid null throws");}
 }
 public static void main(String[] a)throws Exception{Bootstrap.register();nativeRemoval();originalRemoval(new File(a[0]));order();System.out.println("PASS: "+checks+" real original/native World/Chunk removal, return, bounds and evaluation assertions");}
}
