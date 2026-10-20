import java.lang.reflect.*;
import java.io.*;
import java.net.*;
import java.util.*;
import com.hbm.world.WorldBlockDestruction;
import com.hbm.tileentity.machine.TileEntityDummy;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.*;
import net.minecraft.init.*;
import net.minecraft.profiler.Profiler;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.*;
import net.minecraft.world.*;
import net.minecraft.world.storage.*;
import net.minecraft.world.chunk.*;
import net.minecraft.world.chunk.storage.*;

/** Genuine native World/Chunk mutation; only startup, provider and lighting seams. */
public class DestroyBlockTest {
 static final sun.misc.Unsafe U;static int checks;
 static {try{Field f=sun.misc.Unsafe.class.getDeclaredField("theUnsafe");f.setAccessible(true);U=(sun.misc.Unsafe)f.get(null);}catch(Exception e){throw new RuntimeException(e);}}
 static Field field(Class<?> c,String n)throws Exception{Field f=c.getDeclaredField(n);f.setAccessible(true);return f;}
 static void check(boolean b,String m){checks++;if(!b)throw new AssertionError(m);}
 static boolean valid(int x,int y,int z){return x>=-30000000&&x<30000000&&z>=-30000000&&z<30000000&&y>=0&&y<256;}
 static class NativeWorld extends World {
  final Map<Long,Chunk> chunks=new HashMap<>();int requests;
  NativeWorld(boolean remote){super(new SaveHandlerMP(),new WorldInfo(new NBTTagCompound()),new WorldProviderSurface(),new Profiler(),remote);provider.setWorld(this);chunkProvider=createChunkProvider();}
  protected IChunkProvider createChunkProvider(){return new IChunkProvider(){
   public Chunk getLoadedChunk(int x,int z){return chunks.get(ChunkPos.asLong(x,z));}
   public Chunk provideChunk(int x,int z){requests++;long k=ChunkPos.asLong(x,z);if(!chunks.containsKey(k))chunks.put(k,new Chunk(NativeWorld.this,x,z));return chunks.get(k);}
   public boolean tick(){return false;}public String makeString(){return "test chunks";}public boolean isChunkGeneratedAt(int x,int z){return getLoadedChunk(x,z)!=null;}
  };}
  protected boolean isChunkLoaded(int x,int z,boolean allow){return chunks.containsKey(ChunkPos.asLong(x,z));}
  public boolean checkLight(BlockPos p){return true;}
 }
 static Chunk place(NativeWorld w,int x,int y,int z,IBlockState state){
  Chunk c=new Chunk(w,x>>4,z>>4);w.chunks.clear();w.chunks.put(ChunkPos.asLong(x>>4,z>>4),c);
  if(y>=0&&y<256){ExtendedBlockStorage st=new ExtendedBlockStorage(y&~15,true);st.set(x&15,y&15,z&15,state);c.getBlockStorageArray()[y>>4]=st;}
  w.requests=0;return c;
 }
 static void domain(){
  NativeWorld w=new NativeWorld(true);int[] h={Integer.MIN_VALUE,-30000001,-30000000,-17,-1,0,16,29999999,30000000,Integer.MAX_VALUE};
  for(boolean drop:new boolean[]{false,true})for(int x:h)for(int z:h)for(int y:new int[]{-1,0,64,255,256}){
   Chunk c=place(w,x,y,z,Blocks.STONE.getDefaultState());boolean result=WorldBlockDestruction.destroy(w,x,y,z,drop);
   check(result==valid(x,y,z),"native actual changed return");check((w.requests>0)==valid(x,y,z),"native prechunk domain");
   if(valid(x,y,z)){check(c.getBlockState(new BlockPos(x,y,z)).getBlock()==Blocks.AIR,"native actual removed");check(!WorldBlockDestruction.destroy(w,x,y,z,drop),"native repeated air false");}
   else if(y>=0&&y<256)check(c.getBlockState(new BlockPos(x,y,z)).getBlock()==Blocks.STONE,"out of domain unchanged");
  }
 }
 static StringBuilder trace=new StringBuilder();static boolean sabotage;static int wantedMeta;static RecordingBlock recording;
 static class RecordingBlock extends Block {
  static final PropertyInteger META=PropertyInteger.create("metadata",0,15);
  RecordingBlock(){super(Material.ROCK);setDefaultState(blockState.getBaseState().withProperty(META,0));}
  protected net.minecraft.block.state.BlockStateContainer createBlockState(){return new net.minecraft.block.state.BlockStateContainer(this,META);}
  public IBlockState getStateFromMeta(int m){return getDefaultState().withProperty(META,m&15);}
  public int getMetaFromState(IBlockState s){return s.getValue(META);}
  public void dropBlockAsItemWithChance(World w,BlockPos p,IBlockState s,float chance,int fortune){
   trace.append('d');check(s.getBlock()==this&&getMetaFromState(s)==wantedMeta,"original native state delivered to drop");check(chance==1F&&fortune==0,"native chance one fortune zero");
   check(w.getBlockState(p).getBlock()==(sabotage?Blocks.AIR:this),"native event precedes drop and drop precedes own removal");
  }
 }
 static void callbacks()throws Exception{
  recording=new RecordingBlock();net.minecraftforge.fml.common.registry.ForgeRegistries.BLOCKS.register(recording.setRegistryName("minecraft","destruction_probe"));
  NativeWorld w=new NativeWorld(false);BlockPos p=new BlockPos(-17,64,19);
  IWorldEventListener listener=(IWorldEventListener)java.lang.reflect.Proxy.newProxyInstance(DestroyBlockTest.class.getClassLoader(),new Class[]{IWorldEventListener.class},(proxy,m,args)->{
   if(m.getName().equals("playEvent")){trace.append('e');check((Integer)args[1]==2001&&args[2].equals(p),"native effect coordinates");check((Integer)args[3]==Block.getIdFromBlock(recording)+(wantedMeta<<12),"native metadata event payload");check(w.getBlockState(p).getBlock()==recording,"event before removal");if(sabotage)check(w.setBlockToAir(p),"event callback preempts removal");}return null;
  });w.addEventListener(listener);
  for(boolean preempt:new boolean[]{false,true})for(boolean drop:new boolean[]{false,true})for(int meta=0;meta<16;meta++){
   wantedMeta=meta;sabotage=preempt;place(w,p.getX(),p.getY(),p.getZ(),recording.getStateFromMeta(meta));trace.setLength(0);
   boolean result=WorldBlockDestruction.destroy(w,p.getX(),p.getY(),p.getZ(),drop);check(result==!preempt,"native actual final mutation return");check(trace.toString().equals(drop?"ed":"e"),"native event/drop order "+trace);check(w.getBlockState(p).getBlock()==Blocks.AIR,"native final removal");
  }
  place(w,p.getX(),p.getY(),p.getZ(),Blocks.AIR.getDefaultState());trace.setLength(0);check(!WorldBlockDestruction.destroy(w,p.getX(),p.getY(),p.getZ(),true)&&trace.length()==0,"air emits no event/drop");
 }
 static World selected;static boolean returned,dropValue;
 static World receiver(){trace.append('r');return selected;}static int arg(int n){trace.append(n);return n==2?64:n;}static boolean drop(){trace.append('d');return dropValue;}
 static class Observed extends NativeWorld{
  Observed(){super(false);}public boolean destroyBlock(BlockPos p,boolean d){trace.append('a');check(p.equals(new BlockPos(1,64,3)),"coordinate order");check(d==dropValue,"exact drop flag");return returned;}
 }
 static void evaluation(){
  selected=new Observed();for(boolean b:new boolean[]{true,false})for(boolean d:new boolean[]{true,false}){returned=b;dropValue=d;trace.setLength(0);check(WorldBlockDestruction.destroy(receiver(),arg(1),arg(2),arg(3),drop())==b&&trace.toString().equals("r123da"),"virtual destroy dispatch boolean return");}
  trace.setLength(0);check(!WorldBlockDestruction.destroy(selected,0,256,0,true)&&trace.length()==0,"invalid no native dispatch");
  selected=null;trace.setLength(0);try{WorldBlockDestruction.destroy(receiver(),arg(1),arg(2),arg(3),drop());throw new AssertionError();}catch(NullPointerException expected){check(trace.toString().equals("r123d"),"null after drop evaluation");}
  trace.setLength(0);try{WorldBlockDestruction.destroy(receiver(),arg(1),(new int[0])[arg(2)],arg(3),drop());throw new AssertionError();}catch(ArrayIndexOutOfBoundsException expected){check(trace.toString().equals("r12"),"argument failure before null");}
  trace.setLength(0);try{WorldBlockDestruction.destroy(receiver(),arg(1),arg(2),arg(3),((boolean[])null)[0]);throw new AssertionError();}catch(NullPointerException expected){check(trace.toString().equals("r123"),"drop failure before receiver null");}
  try{WorldBlockDestruction.destroy(null,Integer.MIN_VALUE,-1,Integer.MAX_VALUE,false);throw new AssertionError();}catch(NullPointerException expected){check(true,"invalid null throws");}
 }

 static void ordinaryDrops(){
  for(boolean drop:new boolean[]{false,true}){
   NativeWorld w=new NativeWorld(false);BlockPos p=new BlockPos(1,64,3);place(w,1,64,3,Blocks.STONE.getDefaultState());
   check(WorldBlockDestruction.destroy(w,1,64,3,drop),"ordinary stone removed");
   check(w.loadedEntityList.size()==(drop?1:0),"real native stone item spawn count");
   if(drop){net.minecraft.entity.item.EntityItem item=(net.minecraft.entity.item.EntityItem)w.loadedEntityList.get(0);check(item.getItem().getItem()==net.minecraft.item.Item.getItemFromBlock(Blocks.COBBLESTONE)&&item.getItem().getCount()==1,"real native cobblestone formula");}
  }
 }
 static void repositoryAir(){
  com.hbm.blocks.machine.BlockBeamBase beam=new com.hbm.blocks.machine.BlockBeamBase(){public net.minecraft.tileentity.TileEntity createNewTileEntity(World w,int meta){return null;}};
  net.minecraftforge.fml.common.registry.ForgeRegistries.BLOCKS.register(beam.setRegistryName("minecraft","destruction_beam_probe"));
  NativeWorld w=new NativeWorld(false);place(w,1,64,3,beam.getStateFromMeta(7));
  check(beam.getDefaultState().getMaterial()==Material.AIR,"actual NTM beam material AIR");check(!WorldBlockDestruction.destroy(w,1,64,3,true),"actual NTM beam native air stays");check(w.getBlockState(new BlockPos(1,64,3)).getBlock()==beam,"actual NTM beam not removed");
  Block foreign=new Block(Material.ROCK){public boolean isAir(IBlockState s,IBlockAccess w,BlockPos p){return true;}};
  net.minecraftforge.fml.common.registry.ForgeRegistries.BLOCKS.register(foreign.setRegistryName("minecraft","destruction_foreign_probe"));place(w,1,64,3,foreign.getDefaultState());
  check(!WorldBlockDestruction.destroy(w,1,64,3,true),"foreign non-AIR isAir override follows native engine (not original parity)");
 }
 static void production(){
  NativeWorld w=new NativeWorld(false);BlockPos p=new BlockPos(1,64,3);TileEntityDummy te=new TileEntityDummy();te.setWorld(w);te.setPos(p);te.targetX=2;te.targetY=65;te.targetZ=3;
  place(w,1,64,3,Blocks.STONE.getDefaultState());te.update();check(w.getBlockState(p).getBlock()==Blocks.AIR,"whole production TileEntityDummy update destroys invalid dummy");
  NativeWorld client=new NativeWorld(true);te.setWorld(client);place(client,1,64,3,Blocks.STONE.getDefaultState());te.update();check(client.getBlockState(p).getBlock()==Blocks.STONE,"whole production TileEntityDummy client guard");
 }
 public static void main(String[] args)throws Exception{Bootstrap.register();domain();callbacks();evaluation();ordinaryDrops();repositoryAir();production();System.out.println("PASS: "+checks+" genuine native World/Chunk domain, callback order, metadata, return, evaluation and complete production TileEntityDummy assertions");}
}
