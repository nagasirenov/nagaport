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
import net.minecraft.world.gen.ChunkProviderServer;

public class WorldReadNativeTest {
 static final sun.misc.Unsafe U; static int checks;
 static {try {Field f=sun.misc.Unsafe.class.getDeclaredField("theUnsafe");f.setAccessible(true);U=(sun.misc.Unsafe)f.get(null);}catch(Exception e){throw new RuntimeException(e);}}
 static void check(boolean b,String m){checks++;if(!b)throw new AssertionError(m);}
 static Field field(Class<?> c,String n)throws Exception{Field f=c.getDeclaredField(n);f.setAccessible(true);return f;}
 static boolean valid(int x,int y,int z){return x>=-30000000&&x<30000000&&z>=-30000000&&z<30000000&&y>=0&&y<256;}
 static WorldServer world()throws Exception{
  WorldServer w=(WorldServer)U.allocateInstance(WorldServer.class);field(World.class,"worldInfo").set(w,new WorldInfo(new NBTTagCompound()));field(World.class,"provider").set(w,new WorldProviderSurface());w.provider.setWorld(w);field(World.class,"profiler").set(w,new Profiler());return w;
 }
 static void nativeReads()throws Exception{
  WorldServer w=world();ChunkProviderServer cp=new ChunkProviderServer(w,null,null);field(World.class,"chunkProvider").set(w,cp);
  int[] horizontal={Integer.MIN_VALUE,-30000001,-30000000,-17,-16,-1,0,15,16,29999999,30000000,Integer.MAX_VALUE};
  for(int x:horizontal)for(int z:horizontal)for(int y:new int[]{Integer.MIN_VALUE,-1,0,64,255,256,Integer.MAX_VALUE}){
   Chunk c=new Chunk(w,x>>4,z>>4);cp.loadedChunks.put(ChunkPos.asLong(x>>4,z>>4),c);c.unloadQueued=true;
   IBlockState state=Blocks.WOOL.getStateFromMeta(13);
   if(y>=0&&y<256){ExtendedBlockStorage storage=new ExtendedBlockStorage(y&~15,true);storage.set(x&15,y&15,z&15,state);c.getBlockStorageArray()[y>>4]=storage;}
   IBlockState got=WorldBlockState.read(w,x,y,z);
   check(got==(valid(x,y,z)?state:Blocks.AIR.getDefaultState()),"native bounds/state identity");
   check(WorldBlockState.metadata(got)==(valid(x,y,z)?13:0),"native owner metadata");
   check(c.unloadQueued!=valid(x,y,z),"out-of-domain never touches chunk; valid provider side effect retained");
  }
  Chunk c=new Chunk(w,-1,-2);cp.loadedChunks.put(ChunkPos.asLong(-1,-2),c);ExtendedBlockStorage storage=new ExtendedBlockStorage(64,true);c.getBlockStorageArray()[4]=storage;
  for(Block block:new Block[]{Blocks.WOOL,Blocks.STAINED_GLASS,Blocks.LOG,Blocks.STONE,Blocks.OAK_STAIRS})for(int meta=0;meta<16;meta++){
   IBlockState state=block.getStateFromMeta(meta);storage.set(15,0,15,state);IBlockState got=WorldBlockState.read(w,-1,64,-17);
   check(got==state,"stored native owner state");check(WorldBlockState.metadata(got)==block.getMetaFromState(state),"actual owner not caller metadata");
  }
 }
 static void originalReads(File jar)throws Exception{
  try(URLClassLoader l=new URLClassLoader(new URL[]{jar.toURI().toURL()},WorldReadNativeTest.class.getClassLoader())){
   Class<?> bootstrap=Class.forName("kl",true,l);bootstrap.getMethod("b").invoke(null);
   Class<?> world=Class.forName("ahb",true,l),client=Class.forName("bjf",true,l),chunk=Class.forName("apx",true,l),storage=Class.forName("apz",true,l),provider=Class.forName("apu",true,l),block=Class.forName("aji",true,l);
   Object w=U.allocateInstance(client),air=Class.forName("ajn",true,l).getField("a").get(null);
   final Object[] current={null};final int[] calls={0};
   Object proxy=java.lang.reflect.Proxy.newProxyInstance(l,new Class[]{provider},(p,m,a)->{if(m.getReturnType()==chunk){calls[0]++;return current[0];}throw new AssertionError(m.toString());});field(world,"v").set(w,proxy);
   Method getBlock=world.getMethod("a",int.class,int.class,int.class),getMeta=world.getMethod("e",int.class,int.class,int.class);
   // Actual original World and Chunk bytecode, only the chunk provider is an observation seam.
   Object stone=block.getMethod("e",int.class).invoke(null,1);
   int[] h={Integer.MIN_VALUE,-30000001,-30000000,-17,-1,0,16,29999999,30000000,Integer.MAX_VALUE};
   for(int x:h)for(int z:h)for(int y:new int[]{-1,0,64,255,256}){
    Object c=chunk.getConstructor(world,int.class,int.class).newInstance(w,x>>4,z>>4);current[0]=c;
    if(y>=0&&y<256){Object s=storage.getConstructor(int.class,boolean.class).newInstance(y&~15,true);storage.getMethod("a",int.class,int.class,int.class,block).invoke(s,x&15,y&15,z&15,stone);storage.getMethod("a",int.class,int.class,int.class,int.class).invoke(s,x&15,y&15,z&15,7);Array.set(field(chunk,"u").get(c),y>>4,s);}
    calls[0]=0;check(getBlock.invoke(w,x,y,z)==(valid(x,y,z)?stone:air),"actual original block bounds");check(((Integer)getMeta.invoke(w,x,y,z))==(valid(x,y,z)?7:0),"actual original raw metadata bounds");check(calls[0]==(valid(x,y,z)?2:0),"original provider calls");
   }
   Object wool=block.getMethod("e",int.class).invoke(null,35);
   for(int meta=0;meta<16;meta++){
    Object c=chunk.getConstructor(world,int.class,int.class).newInstance(w,0,0);current[0]=c;
    Object section=storage.getConstructor(int.class,boolean.class).newInstance(64,true);
    storage.getMethod("a",int.class,int.class,int.class,block).invoke(section,0,0,0,wool);
    storage.getMethod("a",int.class,int.class,int.class,int.class).invoke(section,0,0,0,meta);Array.set(field(chunk,"u").get(c),4,section);
    int original=(Integer)getMeta.invoke(w,0,64,0);
    check(original==WorldBlockState.metadata(Blocks.WOOL.getStateFromMeta(meta)),"original/native sixteen-color metadata wire equality");
    check(getBlock.invoke(w,0,64,0)==wool,"actual original owner identity for metadata wire");
   }
  }
 }
 static StringBuilder trace=new StringBuilder();static World selected;
 static World receiver(){trace.append('r');return selected;}static int arg(int n){trace.append(n);return n==2?64:n;}
 static class ObservedWorld extends World{
  ObservedWorld(){super(new SaveHandlerMP(),new WorldInfo(new NBTTagCompound()),new WorldProviderSurface(),new Profiler(),false);}protected IChunkProvider createChunkProvider(){return null;}protected boolean isChunkLoaded(int x,int z,boolean b){return false;}
  public IBlockState getBlockState(BlockPos p){trace.append('s');return Blocks.WOOL.getStateFromMeta(9);}
 }
 static void order(){
  selected=new ObservedWorld();trace.setLength(0);check(WorldBlockState.metadata(WorldBlockState.read(receiver(),arg(1),arg(2),arg(3)))==9&&trace.toString().equals("r123s"),"receiver/arguments/native dispatch order");
  trace.setLength(0);check(WorldBlockState.read(selected,0,256,0)==Blocks.AIR.getDefaultState()&&trace.length()==0,"Y rejection before virtual dispatch");
  selected=null;trace.setLength(0);try{WorldBlockState.read(receiver(),arg(1),arg(2),arg(3));throw new AssertionError();}catch(NullPointerException expected){check(trace.toString().equals("r123"),"null after arguments");}
  trace.setLength(0);try{WorldBlockState.read(receiver(),arg(1),(new int[0])[arg(2)],arg(3));throw new AssertionError();}catch(ArrayIndexOutOfBoundsException expected){check(trace.toString().equals("r12"),"argument exception precedes null and third argument");}
  try{WorldBlockState.read(null,Integer.MIN_VALUE,-1,Integer.MAX_VALUE);throw new AssertionError();}catch(NullPointerException expected){check(true,"null checked even outside bounds");}
 }
 public static void main(String[] a)throws Exception{Bootstrap.register();nativeReads();originalReads(new File(a[0]));order();System.out.println("PASS: "+checks+" actual original/native World/Chunk bounds, metadata, null and evaluation assertions");}
}
