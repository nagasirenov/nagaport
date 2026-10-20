import java.io.*;
import java.net.*;
import java.lang.reflect.*;
import java.util.*;
import net.minecraft.world.*;
import net.minecraft.world.chunk.*;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraft.world.storage.WorldInfo;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.profiler.Profiler;
import net.minecraft.util.math.ChunkPos;
public final class OwnedHeightProbe {
 static int checks;static void check(boolean v,String m){checks++;if(!v)throw new AssertionError(m);}
 static Field f(Class<?> c,String n)throws Exception{return HeightNativeTest.field(c,n);}
 static WorldServer world(Class<? extends WorldServer> c)throws Exception {
  WorldServer w=(WorldServer)HeightNativeTest.U.allocateInstance(c);f(World.class,"worldBorder").set(w,new net.minecraft.world.border.WorldBorder());f(World.class,"worldInfo").set(w,new WorldInfo(new NBTTagCompound()));f(World.class,"provider").set(w,new WorldProviderSurface());w.provider.setWorld(w);f(World.class,"profiler").set(w,new Profiler());return w;
 }
 static class ForeignWorld extends WorldServer {ForeignWorld(){super(null,null,null,0,null);} @Override public int getHeight(int x,int z){throw new AssertionError("foreign dispatch unexpectedly bypassed/used");}}
 static class ForeignProvider extends ChunkProviderServer {ForeignProvider(WorldServer w){super(w,null,null);}}
 static class ForeignChunk extends Chunk {int calls;ForeignChunk(World w){super(w,0,0);}@Override public int getHeightValue(int x,int z){calls++;return 37+x*2+z;}}
 static void nativeCase()throws Exception{
  for(Class<? extends WorldServer> cl:Arrays.asList(WorldServer.class,WorldServerMulti.class)){
   WorldServer w=world(cl);check(OwnedHeightCandidate.assess(w,30000000,0).getAsInt()==64,"bounds precede absent provider access");ChunkProviderServer cp=new ChunkProviderServer(w,null,null);f(World.class,"chunkProvider").set(w,cp);Set<Long> queue=(Set<Long>)f(ChunkProviderServer.class,"droppedChunks").get(cp);
   for(int sea:new int[]{0,63,117,Integer.MAX_VALUE}){w.setSeaLevel(sea);
    for(int x:HeightNativeTest.H)for(int z:HeightNativeTest.H)for(int mode=0;mode<3;mode++){
     cp.loadedChunks.clear();queue.clear();long key=ChunkPos.asLong(x>>4,z>>4);Chunk c=mode==2?new EmptyChunk(w,x>>4,z>>4):new Chunk(w,x>>4,z>>4);
     for(int xx=0;xx<16;xx++)for(int zz=0;zz<16;zz++)c.getHeightMap()[(zz<<4)|xx]=HeightNativeTest.height(xx,zz);
     if(mode!=0)cp.loadedChunks.put(key,c);queue.add(key);c.unloadQueued=true;
     int expected=!HeightNativeTest.valid(x,z)?64:mode==1?HeightNativeTest.height(x,z):0;
     check(OwnedHeightCandidate.assess(w,x,z).getAsInt()==expected,"owned height parity");check(c.unloadQueued&&queue.size()==1&&queue.contains(key),"both actual native queue representations unchanged");check(cp.loadedChunks.size()==(mode==0?0:1),"no loaded-map mutations or creation");
    }
   }
   cp.loadedChunks.clear();queue.clear();Chunk c=new Chunk(w,1024,2048);cp.loadedChunks.put(ChunkPos.asLong(1024,2048),c);cp.queueUnload(c);check(c.unloadQueued&&!queue.isEmpty(),"actual native queueUnload setup");OwnedHeightCandidate.assess(w,16384,32768);check(c.unloadQueued&&!queue.isEmpty(),"candidate preserves actual queued state");w.getHeight(16384,32768);check(!c.unloadQueued,"native getHeight counterexample cancels flag");
   cp.loadedChunks.clear();cp.loadedChunks.put(0L,null);check(!OwnedHeightCandidate.assess(w,0,0).isPresent(),"present-null is explicitly uncertified");
   cp.loadedChunks.clear();ForeignChunk fc=new ForeignChunk(w);cp.loadedChunks.put(0L,fc);check(OwnedHeightCandidate.assess(w,2,3).getAsInt()==44&&fc.calls==1,"actual chunk virtual height owner dispatch retained");
   cp.loadedChunks.clear();cp.loadedChunks.defaultReturnValue(fc);check(OwnedHeightCandidate.assess(w,2,3).getAsInt()==0&&fc.calls==1,"absent key remains absent even with altered map default");cp.loadedChunks.defaultReturnValue(null);
   f(World.class,"chunkProvider").set(w,new ForeignProvider(w));check(!OwnedHeightCandidate.assess(w,0,0).isPresent(),"provider subclass excluded");
   f(World.class,"chunkProvider").set(w,new ChunkProviderServer(world(WorldServer.class),null,null));check(!OwnedHeightCandidate.assess(w,0,0).isPresent(),"mismatched provider owner excluded");
  }
  WorldServer foreign=world(ForeignWorld.class);f(World.class,"chunkProvider").set(foreign,new ChunkProviderServer(foreign,null,null));check(!OwnedHeightCandidate.assess(foreign,0,0).isPresent(),"world subclass excluded before dispatch");check(!OwnedHeightCandidate.assess(HeightNativeTest.world(true),0,0).isPresent(),"client excluded from server experiment");
 }
 static void originalActualQueue(File jar)throws Exception{
  try(URLClassLoader l=new URLClassLoader(new URL[]{jar.toURI().toURL()},OwnedHeightProbe.class.getClassLoader())){
   Class.forName("kl",true,l).getMethod("b").invoke(null);Class<?> wc=Class.forName("ahb",true,l),sc=Class.forName("mt",true,l),pc=Class.forName("ms",true,l),cc=Class.forName("apx",true,l);Object w=HeightNativeTest.U.allocateInstance(sc);
   // End provider does not consult spawn coordinates when queueing a chunk.
   f(wc,"t").set(w,Class.forName("aqr",true,l).newInstance());Object cp=pc.getConstructor(sc,Class.forName("aqc",true,l),Class.forName("apu",true,l)).newInstance(w,null,null);f(wc,"v").set(w,cp);Object map=f(pc,"g").get(cp);long key=ChunkPos.asLong(1024,2048);Object c=cc.getConstructor(wc,int.class,int.class).newInstance(w,1024,2048);((int[])f(cc,"f").get(c))[0]=77;map.getClass().getMethod("a",long.class,Object.class).invoke(map,key,c);
   pc.getMethod("b",int.class,int.class).invoke(cp,1024,2048);Set<?> queue=(Set<?>)f(pc,"c").get(cp);check(queue.contains(key),"actual original queueUnload setup");check(((Integer)wc.getMethod("f",int.class,int.class).invoke(w,16384,32768))==77,"actual original queued column height");check(queue.contains(key),"actual original queued state retained");
   map.getClass().getMethod("d",long.class).invoke(map,key);check(((Integer)wc.getMethod("f",int.class,int.class).invoke(w,16384,32768))==0,"original missing column returns zero without generator/load path");check(((Integer)map.getClass().getMethod("a").invoke(map))==0,"original absent lookup does not populate map");
   map.getClass().getMethod("a",long.class,Object.class).invoke(map,key,null);
   // Original containsKey succeeds and provideChunk enters its load path.
   // With this actual provider's null generator it installs its genuine EmptyChunk.
   check(((Integer)wc.getMethod("f",int.class,int.class).invoke(w,16384,32768))==0,"actual original present-null fallback height");
   check(map.getClass().getMethod("a",long.class).invoke(map,key)!=null&&!queue.contains(key),"original present-null loads EmptyChunk and removes queued entry");
  }
 }
 public static void main(String[] a)throws Exception{net.minecraft.init.Bootstrap.register();nativeCase();HeightNativeTest.original(new File(a[0]),false);originalActualQueue(new File(a[0]));System.out.println("PASS: "+checks+" owned-server experiment assertions plus "+HeightNativeTest.checks+" reused real original server assertions; no production activation");}
}
