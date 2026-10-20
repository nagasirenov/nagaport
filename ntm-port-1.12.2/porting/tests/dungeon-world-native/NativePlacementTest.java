import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.init.Bootstrap;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldProviderSurface;
import net.minecraft.world.WorldType;
import net.minecraft.world.storage.SaveHandlerMP;
import net.minecraft.world.storage.WorldInfo;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.profiler.Profiler;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntityChest;
import java.io.*;
import java.util.*;

/** Real native platform types only. Stream extraction is not whole-generator execution. */
public final class NativePlacementTest {
 static int checks;
 static void check(boolean b,String label){checks++;if(!b)throw new AssertionError(label);}
 public static final class RecordingWorld extends World {
  final PrintWriter out;String stream;int origin;int accepted;
  RecordingWorld(PrintWriter o){super(new SaveHandlerMP(),new WorldInfo(new NBTTagCompound()),new WorldProviderSurface(),new Profiler(),false);out=o;}
  protected IChunkProvider createChunkProvider(){return null;}
  protected boolean isChunkLoaded(int x,int z,boolean allowEmpty){return false;}
  public boolean setBlockState(BlockPos pos,IBlockState state,int flags){
   check(isValid(pos),"legacy bounds guard before native mutation");
   out.println(stream+"\t"+origin+"\t"+pos.getX()+"\t"+pos.getY()+"\t"+pos.getZ()+"\t"+Block.getIdFromBlock(state.getBlock())+"\t"+state.getBlock().getMetaFromState(state)+"\t"+flags);
   accepted++;return (accepted&1)==0; // both success/failure remain ignored as in the audited source
  }
 }
 static final class ChunkWorld extends World {
  final Map<Long,Chunk> chunks=new HashMap<>();int notifications,neighbors;
  ChunkWorld(){super(new SaveHandlerMP(),new WorldInfo(new NBTTagCompound()),new WorldProviderSurface(),new Profiler(),false);provider.setWorld(this);chunkProvider=createChunkProvider();for(int x=-1;x<=1;x++)for(int z=-1;z<=1;z++){Chunk c=new Chunk(this,x,z);c.markLoaded(true);c.setTerrainPopulated(true);c.setLightPopulated(true);c.onTick(false);chunks.put(key(x,z),c);}}
  static long key(int x,int z){return ((long)x<<32)^(z&0xffffffffL);}
  protected IChunkProvider createChunkProvider(){return new IChunkProvider(){public Chunk getLoadedChunk(int x,int z){return chunks.get(key(x,z));}public Chunk provideChunk(int x,int z){return getLoadedChunk(x,z);}public boolean tick(){return false;}public String makeString(){return "native placement proof";}public boolean isChunkGeneratedAt(int x,int z){return getLoadedChunk(x,z)!=null;}};}
  protected boolean isChunkLoaded(int x,int z,boolean allowEmpty){return chunks.containsKey(key(x,z));}
  public boolean checkLight(BlockPos pos){return true;}
  public void notifyBlockUpdate(BlockPos pos,IBlockState oldState,IBlockState state,int flags){notifications++;}
  public void notifyNeighborsRespectDebug(BlockPos pos,Block block,boolean observers){neighbors++;}
 }
 static void chunks() {
  ChunkWorld w=new ChunkWorld();BlockPos p=new BlockPos(1,64,1);
  check(!w.setBlockState(new BlockPos(1,-1,1),Blocks.STONE.getDefaultState(),3),"native negative Y rejection");
  check(!w.setBlockState(new BlockPos(1,256,1),Blocks.STONE.getDefaultState(),3),"native high Y rejection");
  check(w.setBlockState(p,Blocks.STONEBRICK.getStateFromMeta(3),3),"native chunk accepted stonebrick");
  check(w.getBlockState(p)==Blocks.STONEBRICK.getStateFromMeta(3),"native chunk stores exact state");
  check(!w.setBlockState(p,Blocks.STONEBRICK.getStateFromMeta(3),3),"same-state false return");
  check(w.notifications==1&&w.neighbors==1,"flag 3 client plus neighbor notification: "+w.notifications+"/"+w.neighbors);
  check(w.setBlockState(p,Blocks.STONEBRICK.getStateFromMeta(0),2),"flag 2 state change");
  check(w.notifications==2&&w.neighbors==1,"flag 2 no neighbor notification");
  check(w.setBlockState(p,Blocks.CHEST.getStateFromMeta(5),3),"native chest placement");
  check(w.getTileEntity(p) instanceof TileEntityChest,"native chest tile exists");
  check(w.getBlockState(p).getValue(net.minecraft.block.BlockChest.FACING)==net.minecraft.util.EnumFacing.EAST,"chest metadata 5 orientation");
  check(w.setBlockState(p,Blocks.AIR.getStateFromMeta(0),3),"native air replacement");
  check(w.getTileEntity(p)==null,"native air replacement removes tile");
  for(int[] b:new int[][]{{-30000000,0,-30000000},{29999999,255,29999999}})check(w.isValid(new BlockPos(b[0],b[1],b[2])),"inclusive lower/exclusive upper world limits");
  for(int[] b:new int[][]{{-30000001,64,0},{30000000,64,0},{0,64,-30000001},{0,64,30000000},{0,-1,0},{0,256,0}})check(!w.isValid(new BlockPos(b[0],b[1],b[2])),"legacy rejected bound");
 }
 public static void main(String[] args)throws Exception {
  Bootstrap.register();
  try(PrintWriter out=new PrintWriter(new BufferedWriter(new FileWriter(args[0])))) {
   RecordingWorld w=new RecordingWorld(out);
   int[][] origins={{0,64,0},{-17,200,-31},{29999990,0,-29999990},{-30000000,255,29999999},{Integer.MAX_VALUE,64,Integer.MIN_VALUE},{0,-256,0},{0,256,0},{29999999,64,29999999},{-30000001,64,-30000001}};
   for(int i=0;i<origins.length;i++){w.origin=i;GeneratedPlacements.run(w,origins[i][0],origins[i][1],origins[i][2]);}
   System.out.println("PASS actual native World subclass received "+w.accepted+" ordered state/flag placements over "+origins.length+" ordinary, boundary, invalid-height and integer-overflow origins");
  }
  chunks();System.out.println("PASS "+checks+" checks, including real native World.setBlockState/chunks, notifications, same-state false return, chest creation/orientation and tile removal");
 }
}
