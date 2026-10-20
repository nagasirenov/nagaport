import java.lang.reflect.*;
import java.util.*;
import com.hbm.world.TileEntityBlockUpdate;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.chunk.IRenderChunkFactory;
import net.minecraft.init.Bootstrap;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.*;
import net.minecraft.pathfinding.PathWorldListener;
import net.minecraft.profiler.Profiler;
import net.minecraft.server.management.*;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.*;
import net.minecraft.world.*;
import net.minecraft.world.chunk.*;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraft.world.storage.*;
import it.unimi.dsi.fastutil.longs.*;

/** Real native World/listener/Chunk/map/entry/packet bodies; no platform replacements. */
public class NotificationTest {
 static final sun.misc.Unsafe U; static int checks; static StringBuilder trace=new StringBuilder();
 static {try {Field f=sun.misc.Unsafe.class.getDeclaredField("theUnsafe"); f.setAccessible(true); U=(sun.misc.Unsafe)f.get(null);}catch(Exception e){throw new RuntimeException(e);}}
 static Field field(Class<?> c,String n)throws Exception {Field f=c.getDeclaredField(n);f.setAccessible(true);return f;}
 static void check(boolean b,String m){checks++;if(!b)throw new AssertionError(m);}
 static class TestWorld extends World {
  Map<Long,Chunk> chunks=new HashMap<>(); int requests,mutations,neighbors,light;
  TestWorld(boolean remote){super(new SaveHandlerMP(),new WorldInfo(new NBTTagCompound()),new WorldProviderSurface(),new Profiler(),remote);provider.setWorld(this);chunkProvider=createChunkProvider();}
  protected IChunkProvider createChunkProvider(){return new IChunkProvider(){
   public Chunk getLoadedChunk(int x,int z){return chunks.get(ChunkPos.asLong(x,z));}
   public Chunk provideChunk(int x,int z){requests++;Chunk c=getLoadedChunk(x,z);if(c==null)throw new AssertionError("unloaded chunk generation attempted");return c;}
   public boolean tick(){return false;}public String makeString(){return "existing chunks only";}public boolean isChunkGeneratedAt(int x,int z){return getLoadedChunk(x,z)!=null;}
  };}
  protected boolean isChunkLoaded(int x,int z,boolean empty){return chunks.containsKey(ChunkPos.asLong(x,z));}
  public boolean setBlockState(BlockPos p,IBlockState s,int flags){mutations++;throw new AssertionError("block mutation");}
  public void notifyNeighborsOfStateChange(BlockPos p,Block b,boolean observer){neighbors++;throw new AssertionError("neighbor notification");}
  public boolean checkLight(BlockPos p){light++;throw new AssertionError("light mutation");}
 }
 static class MetaBlock extends SelectedOwnerState {
  int collisionCalls;
  public TileEntity createNewTileEntity(World w,int m){return new TestTile();}
  public AxisAlignedBB getCollisionBoundingBox(IBlockState s,IBlockAccess w,BlockPos p){collisionCalls++;return super.getCollisionBoundingBox(s,w,p);}
 }
 static class TestTile extends TileEntity {
  Block paint;int dirty,packetRequests;
  public void markDirty(){dirty++;trace.append('d');}
  public SPacketUpdateTileEntity getUpdatePacket(){packetRequests++;NBTTagCompound n=new NBTTagCompound();n.setInteger("paint",paint==null?0:1);return new SPacketUpdateTileEntity(getPos(),0,n);}
 }
 static Chunk seed(TestWorld w,BlockPos p,IBlockState s,TestTile tile){
  Chunk c=new Chunk(w,p.getX()>>4,p.getZ()>>4);ExtendedBlockStorage e=new ExtendedBlockStorage(p.getY()&~15,true);e.set(p.getX()&15,p.getY()&15,p.getZ()&15,s);c.getBlockStorageArray()[p.getY()>>4]=e;w.chunks.put(ChunkPos.asLong(c.x,c.z),c);
  tile.setWorld(w);tile.setPos(p);c.getTileEntityMap().put(p,tile);return c;
 }
 static class Frustum extends ViewFrustum {
  int calls;int[] last;boolean immediate;
  Frustum(){super(null,0,null,null);}
  public void markBlocksForUpdate(int a,int b,int c,int d,int e,int f,boolean now){calls++;last=new int[]{a,b,c,d,e,f};immediate=now;}
 }
 static void nativeFlow()throws Exception {
  MetaBlock block=new MetaBlock();
  for(boolean remote:new boolean[]{false,true})for(int meta=0;meta<16;meta++)for(int x:new int[]{-17,-1,0,15,16,29999999})for(int y:new int[]{0,64,255}){
   TestWorld w=new TestWorld(remote);BlockPos pos=new BlockPos(x,y,-19);IBlockState state=block.getStateFromMeta(meta);TestTile tile=new TestTile();tile.paint=block;Chunk chunk=seed(w,pos,state,tile);
   RenderGlobal renderer=(RenderGlobal)U.allocateInstance(RenderGlobal.class);Frustum frustum=(Frustum)U.allocateInstance(Frustum.class);field(RenderGlobal.class,"viewFrustum").set(renderer,frustum);w.addEventListener(renderer);
   final int[] calls={0};IWorldEventListener listener=(IWorldEventListener)Proxy.newProxyInstance(NotificationTest.class.getClassLoader(),new Class[]{IWorldEventListener.class},(obj,m,args)->{
    check(m.getName().equals("notifyBlockUpdate"),"no other callback");calls[0]++;trace.append('n');check(args[0]==w&&args[1].equals(pos),"same world coordinates");check(args[2]==state&&args[3]==state,"exact stored immutable state twice");check((Integer)args[4]==2,"notification flag 2");check(tile.paint==null&&tile.dirty==0,"paint cleared before notify and dirty after");return null;
   });w.addEventListener(listener);
   // This is the unchanged selected three-statement branch, generated from production by prove.py.
   com.hbm.world.WorldBlockState.read(w,pos.getX(),pos.getY(),pos.getZ());check(w.getTileEntity(pos)==tile,"normal tooling pre-read and real tile lookup");int requests=w.requests;int collision=block.collisionCalls;trace.setLength(0);SelectedBranch.apply(w,pos.getX(),pos.getY(),pos.getZ(),tile);
   check(trace.toString().equals("nd"),"one synchronous notify before original dirty");check(calls[0]==1&&tile.paint==null&&tile.dirty==1,"one notification and exact tile effect");check(w.requests==requests+1,"one existing-chunk state read, no generation");check(chunk.getBlockState(pos)==state&&block.getMetaFromState(state)==meta,"stored metadata unchanged");check(w.mutations==0&&w.neighbors==0&&w.light==0,"no world mutation neighbors lighting");check(block.collisionCalls==collision+2,"native path listener calls inherited collision twice");check(frustum.calls==1&&!frustum.immediate,"one ordinary render invalidation");check(Arrays.equals(frustum.last,new int[]{x-1,y-1,-20,x+1,y+1,-18}),"same one-block render margin");
  }
 }
 static class Nav extends net.minecraft.pathfinding.PathNavigateGround {
  Nav(){super(null,null);}
  public boolean canUpdatePathOnTimeout(){throw new AssertionError("unchanged full-cube state must not inspect or update navigation");}
 }
 static class Entry extends PlayerChunkMapEntry {
  List<Packet<?>> packets;
  Entry(){super(null,0,0);}
  public void sendPacket(Packet<?> packet){packets.add(packet);}
 }
 static void serverFlow()throws Exception {
  MetaBlock block=new MetaBlock();TestWorld source=new TestWorld(false);BlockPos p=new BlockPos(-17,80,19);IBlockState state=block.getStateFromMeta(11);TestTile tile=new TestTile();Chunk c=seed(source,p,state,tile);
  WorldServer server=(WorldServer)U.allocateInstance(WorldServer.class);field(World.class,"chunkProvider").set(server,source.createChunkProvider());field(World.class,"addedTileEntityList").set(server,new ArrayList<>());field(World.class,"eventListeners").set(server,new ArrayList<IWorldEventListener>(Arrays.asList(new PathWorldListener())));
  PathWorldListener path=(PathWorldListener)((List)field(World.class,"eventListeners").get(server)).get(0);((List)field(PathWorldListener.class,"navigations").get(path)).add(U.allocateInstance(Nav.class));
  PlayerChunkMap map=(PlayerChunkMap)U.allocateInstance(PlayerChunkMap.class);field(PlayerChunkMap.class,"world").set(map,server);Long2ObjectOpenHashMap<PlayerChunkMapEntry> entries=new Long2ObjectOpenHashMap<>();field(PlayerChunkMap.class,"entryMap").set(map,entries);Set<PlayerChunkMapEntry> dirty=new HashSet<>();field(PlayerChunkMap.class,"dirtyEntries").set(map,dirty);field(WorldServer.class,"playerChunkMap").set(server,map);
  Entry entry=(Entry)U.allocateInstance(Entry.class);entry.packets=new ArrayList<>();field(PlayerChunkMapEntry.class,"playerChunkMap").set(entry,map);field(PlayerChunkMapEntry.class,"pos").set(entry,new ChunkPos(c.x,c.z));field(PlayerChunkMapEntry.class,"chunk").set(entry,c);field(PlayerChunkMapEntry.class,"changedBlocks").set(entry,new short[64]);field(PlayerChunkMapEntry.class,"sentToPlayers").setBoolean(entry,true);
  Method index=PlayerChunkMap.class.getDeclaredMethod("getIndex",int.class,int.class);index.setAccessible(true);entries.put((Long)index.invoke(null,c.x,c.z),entry);server.addEventListener(new ServerWorldEventHandler(null,server));
  TileEntityBlockUpdate.notifyAt(server,p.getX(),p.getY(),p.getZ());TileEntityBlockUpdate.notifyAt(server,p.getX(),p.getY(),p.getZ());
  check(dirty.size()==1&&dirty.contains(entry),"actual player map queues watched entry once");check(field(PlayerChunkMapEntry.class,"changes").getInt(entry)==1,"actual entry coalesces duplicate position");check(field(PlayerChunkMapEntry.class,"changedSectionFilter").getInt(entry)==1<<5,"actual section bit retained");check(entry.packets.isEmpty()&&tile.packetRequests==0,"notification queues, does not send early");
  entry.update();check(entry.packets.size()==2,"actual one block plus tile packet");SPacketBlockChange change=(SPacketBlockChange)entry.packets.get(0);check(change.getBlockPosition().equals(p)&&change.getBlockState()==state,"actual packet uses current full state");SPacketUpdateTileEntity packet=(SPacketUpdateTileEntity)entry.packets.get(1);check(packet.getPos().equals(p)&&packet.getNbtCompound().getInteger("paint")==0&&tile.packetRequests==1,"actual tile packet uses changed paint");check(field(PlayerChunkMapEntry.class,"changes").getInt(entry)==0,"actual queue flushed");
  // Unwatched positions must not create map entries or packets. The block itself is still loaded.
  entries.clear();TileEntityBlockUpdate.notifyAt(server,p.getX(),p.getY(),p.getZ());check(entries.isEmpty()&&entry.packets.size()==2,"unwatched no entry generation or packets");
 }
 static class Observed extends TestWorld {
  IBlockState state;Observed(){super(true);state=net.minecraft.init.Blocks.STONE.getDefaultState();}
  public IBlockState getBlockState(BlockPos p){trace.append('s');check(p.equals(new BlockPos(1,64,3)),"coordinates exact");return state;}
  public void notifyBlockUpdate(BlockPos p,IBlockState a,IBlockState b,int f){trace.append('n');check(a==state&&b==state&&f==2,"virtual update exact states flags");}
 }
 static World selected;static World recv(){trace.append('r');return selected;}static int arg(int a){trace.append(a);return a==2?64:a;}
 static void evaluation(){selected=new Observed();trace.setLength(0);TileEntityBlockUpdate.notifyAt(recv(),arg(1),arg(2),arg(3));check(trace.toString().equals("r123sn"),"receiver and coordinate evaluation then state then virtual notify");selected=null;trace.setLength(0);try{TileEntityBlockUpdate.notifyAt(recv(),arg(1),arg(2),arg(3));throw new AssertionError();}catch(NullPointerException expected){check(trace.toString().equals("r123"),"null receiver after all coordinates");}trace.setLength(0);try{TileEntityBlockUpdate.notifyAt(recv(),arg(1),(new int[0])[arg(2)],arg(3));throw new AssertionError();}catch(ArrayIndexOutOfBoundsException expected){check(trace.toString().equals("r12"),"coordinate failure before null");}}
 public static void main(String[] a)throws Exception{Bootstrap.register();nativeFlow();serverFlow();evaluation();System.out.println("PASS: "+checks+" native state/listener/render/real packet queue/evaluation assertions");}
}
