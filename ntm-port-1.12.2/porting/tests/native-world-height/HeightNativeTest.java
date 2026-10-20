import java.lang.reflect.*;
import java.net.*;
import java.io.*;
import java.util.*;
import net.minecraft.world.*;
import net.minecraft.world.storage.WorldInfo;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.profiler.Profiler;
import net.minecraft.world.chunk.*;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraft.client.multiplayer.*;
import net.minecraft.util.math.ChunkPos;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import com.hbm.world.WorldHeight;
public class HeightNativeTest {
 static final sun.misc.Unsafe U;static int checks;
 static {try{Field f=sun.misc.Unsafe.class.getDeclaredField("theUnsafe");f.setAccessible(true);U=(sun.misc.Unsafe)f.get(null);}catch(Exception e){throw new RuntimeException(e);}}
 static Field field(Class<?> c,String n)throws Exception{Field f=c.getDeclaredField(n);f.setAccessible(true);return f;}
 static void check(boolean v,String message){checks++;if(!v)throw new AssertionError(message);}
 static boolean valid(int x,int z){return x>=-30000000&&x<30000000&&z>=-30000000&&z<30000000;}
 static int height(int x,int z){return ((x&15)*19+(z&15)*7)%257;}
 static World world(boolean client)throws Exception{
  World w=(World)U.allocateInstance(client?WorldClient.class:WorldServer.class);
  field(World.class,"worldInfo").set(w,new WorldInfo(new NBTTagCompound()));field(World.class,"provider").set(w,new WorldProviderSurface());w.provider.setWorld(w);field(World.class,"profiler").set(w,new Profiler());return w;
 }
 static final int[] H={Integer.MIN_VALUE,-30000001,-30000000,-17,-16,-1,0,1,15,16,29999999,30000000,Integer.MAX_VALUE};
 static void nativeHeight(boolean client)throws Exception{
  World w=world(client);Object cp=client?new ChunkProviderClient(w):new ChunkProviderServer((WorldServer)w,null,null);field(World.class,"chunkProvider").set(w,cp);
  if(client)field(WorldClient.class,"clientChunkProvider").set(w,cp);
  Long2ObjectMap<Chunk> map=client?(Long2ObjectMap<Chunk>)field(ChunkProviderClient.class,"loadedChunks").get(cp):((ChunkProviderServer)cp).loadedChunks;
  for(int sea:new int[]{0,63,91,255,Integer.MAX_VALUE}) {w.setSeaLevel(sea);
   for(int x:H)for(int z:H)for(int mode=0;mode<3;mode++){
    map.clear();Chunk c=mode==2?new EmptyChunk(w,x>>4,z>>4):new Chunk(w,x>>4,z>>4);
    int[] hm=c.getHeightMap();for(int xx=0;xx<16;xx++)for(int zz=0;zz<16;zz++)hm[(zz<<4)|xx]=height(xx,zz);
    if(mode!=0)map.put(ChunkPos.asLong(x>>4,z>>4),c);c.unloadQueued=true;
    int expected=!valid(x,z)?64:mode==1?height(x,z):0;
    check(WorldHeight.read(w,x,z)==expected,"native height/loaded/empty/domain");
    check(map.size()==(mode==0?0:1),"height never loads absent chunk");
    check(c.unloadQueued==(client||!valid(x,z)||mode==0),"native server clears unloadQueued; client retains it");
   }
   check(w.getHeight(30000000,0)==sea+1,"actual native outside sentinel depends on sea level");
   check(WorldHeight.read(w,30000000,0)==64,"original sentinel independent of sea level");
  }
 }
 static void original(File jar,boolean client)throws Exception{
  try(URLClassLoader l=new URLClassLoader(new URL[]{jar.toURI().toURL()},HeightNativeTest.class.getClassLoader())){
   Class.forName("kl",true,l).getMethod("b").invoke(null);Class<?> wc=Class.forName("ahb",true,l),cc=Class.forName("apx",true,l),ec=Class.forName("apw",true,l),pc=Class.forName(client?"bjd":"ms",true,l),sc=Class.forName("mt",true,l);
   Object w=U.allocateInstance(Class.forName(client?"bjf":"mt",true,l));Object cp=client?pc.getConstructor(wc).newInstance(w):pc.getConstructor(sc,Class.forName("aqc",true,l),Class.forName("apu",true,l)).newInstance(w,null,null);
   field(wc,"v").set(w,cp);Object map=field(pc,client?"c":"g").get(cp);Class<?> mapc=map.getClass();Method put=mapc.getMethod("a",long.class,Object.class),remove=mapc.getMethod("d",long.class),size=mapc.getMethod("a"),get=wc.getMethod("f",int.class,int.class);
   Set<Object> unload=client?null:(Set<Object>)field(pc,"c").get(cp);
   for(int x:H)for(int z:H)for(int mode=0;mode<3;mode++){
    long key=ChunkPos.asLong(x>>4,z>>4);Object c=(mode==2?ec:cc).getConstructor(wc,int.class,int.class).newInstance(w,x>>4,z>>4);int[] hm=(int[])field(cc,"f").get(c);for(int xx=0;xx<16;xx++)for(int zz=0;zz<16;zz++)hm[(zz<<4)|xx]=height(xx,zz);
    if(mode!=0)put.invoke(map,key,c);if(unload!=null)unload.add(key);
    int expected=!valid(x,z)?64:mode==1?height(x,z):0;
    check(((Integer)get.invoke(w,x,z))==expected,"actual original World/provider/Chunk height");check(((Integer)size.invoke(map))==(mode==0?0:1),"original never loads absent chunk");if(unload!=null)check(unload.contains(key),"original pending-unload entry remains after height query");
    remove.invoke(map,key);if(unload!=null)unload.clear();
   }
  }
 }
 static StringBuilder trace=new StringBuilder();static World selected;
 static World receiver(){trace.append('r');return selected;}static int x(){trace.append('x');return Integer.MAX_VALUE;}static int z(){trace.append('z');return 0;}
 static void evaluation()throws Exception{
  selected=world(true);trace.setLength(0);check(WorldHeight.read(receiver(),x(),z())==64,"eval result");check(trace.toString().equals("rxz"),"receiver then x then z");selected=null;
  trace.setLength(0);try{WorldHeight.read(receiver(),x(),z());throw new AssertionError();}catch(NullPointerException e){check(trace.toString().equals("rxz"),"null after arguments even outside bounds");}
  trace.setLength(0);try{WorldHeight.read(receiver(),x(),(new int[0])[z()]);throw new AssertionError();}catch(ArrayIndexOutOfBoundsException e){check(trace.toString().equals("rxz"),"argument exception precedes null");}
 }
 public static void main(String[] a)throws Exception{net.minecraft.init.Bootstrap.register();nativeHeight(true);nativeHeight(false);original(new File(a[0]),true);original(new File(a[0]),false);evaluation();System.out.println("PASS: "+checks+" actual original/native client/server World/provider/Chunk height, bounds, sea level, empty/unloaded and evaluation assertions");}
}
