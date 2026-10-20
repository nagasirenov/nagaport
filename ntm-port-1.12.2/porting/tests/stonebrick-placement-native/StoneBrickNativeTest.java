import java.util.*;import java.io.*;import java.net.*;import java.lang.reflect.*;
import net.minecraft.block.state.IBlockState;import net.minecraft.util.math.BlockPos;import net.minecraft.util.*;import net.minecraft.world.*;import net.minecraft.world.chunk.*;import net.minecraft.world.storage.*;import net.minecraft.nbt.*;import net.minecraft.profiler.Profiler;import net.minecraft.creativetab.CreativeTabs;import net.minecraft.entity.Entity;import net.minecraft.entity.item.EntityItem;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.*;
import net.minecraft.init.Bootstrap;
import net.minecraftforge.fml.common.*;
import net.minecraftforge.fml.common.registry.ForgeRegistries;



public class StoneBrickNativeTest extends NativeProof {
static final class NativeWorld extends World {
  final Map<Long,Chunk> chunks=new HashMap<>();final List<ItemStack> spawned=new ArrayList<>();int notifications,lastFlags;
  NativeWorld(){super(new SaveHandlerMP(),new WorldInfo(new NBTTagCompound()),new WorldProviderSurface(),new Profiler(),false);provider.setWorld(this);chunkProvider=createChunkProvider();for(int x=-1;x<=1;x++)for(int z=-1;z<=1;z++){Chunk c=new Chunk(this,x,z);c.markLoaded(true);chunks.put(key(x,z),c);}}
  static long key(int x,int z){return ((long)x<<32)^(z&0xffffffffL);}protected IChunkProvider createChunkProvider(){return new IChunkProvider(){public Chunk getLoadedChunk(int x,int z){return chunks.get(key(x,z));}public Chunk provideChunk(int x,int z){return getLoadedChunk(x,z);}public boolean tick(){return false;}public String makeString(){return "real preloaded chunks";}public boolean isChunkGeneratedAt(int x,int z){return getLoadedChunk(x,z)!=null;}};}protected boolean isChunkLoaded(int x,int z,boolean empty){return chunks.containsKey(key(x,z));}public boolean checkLight(BlockPos p){return true;}
  @Override public void notifyBlockUpdate(BlockPos p,IBlockState old,IBlockState next,int flags){lastFlags=flags;notifications++;super.notifyBlockUpdate(p,old,next,flags);}
  @Override public boolean spawnEntity(Entity e){if(e instanceof EntityItem){spawned.add(((EntityItem)e).getItem().copy());return true;}return super.spawnEntity(e);}
 }
 public static void main(String[] args)throws Exception{Bootstrap.register();FMLCommonHandler.instance().beginLoading(net.minecraftforge.fml.server.FMLServerHandler.instance());ModMetadata md=new ModMetadata();md.modId="hbm";md.name="Ore native callback probe";md.version="1";DummyModContainer c=new DummyModContainer(md);Loader.instance().setupTestHarness(c);Loader.instance().setActiveModContainer(c);

runOwner(args[0]);System.out.println("PASS "+checks+" real original/native stone-brick owner/drop/state/save checks");}
 static void runOwner(String legacy)throws Exception {
  Block b=net.minecraft.init.Blocks.STONEBRICK;check(b.getClass()==net.minecraft.block.BlockStoneBrick.class,"genuine immutable singleton owner");int mods=net.minecraft.init.Blocks.class.getField("STONEBRICK").getModifiers();check(Modifier.isStatic(mods)&&Modifier.isFinal(mods),"native immutable owner field");check(b.getBlockState().getValidStates().size()==4,"only four genuine native metadata states");NativeWorld w=new NativeWorld();
  try(URLClassLoader loader=new URLClassLoader(new URL[]{new File(legacy).toURI().toURL()},StoneBrickNativeTest.class.getClassLoader())){
   Class<?> ob=Class.forName("aji",true,loader),oi=Class.forName("adb",true,loader);ob.getMethod("p").invoke(null);oi.getMethod("l").invoke(null);Object original=ob.getMethod("b",String.class).invoke(null,"stonebrick");check(original.getClass().getName().equals("anw"),"actual original owner");check((Integer)ob.getMethod("b",ob).invoke(null,original)==Block.getIdFromBlock(b),"same original/native registry ID");
   Method damage=ob.getMethod("a",int.class),item=ob.getMethod("a",int.class,Random.class,int.class),quantity=ob.getMethod("a",int.class,Random.class),itemId=oi.getMethod("b",oi);
   for(int m=0;m<4;m++){IBlockState state=b.getStateFromMeta(m);check(b.getMetaFromState(state)==m&&Block.getStateById(Block.getStateId(state))==state,"native metadata/state IDs");check(b.damageDropped(state)==(Integer)damage.invoke(original,m),"actual old/native variant drop damage");for(int fortune:new int[]{0,1,3})for(int seed=0;seed<12;seed++){Random oldRand=new Random(seed),newRand=new Random(seed);check((Integer)itemId.invoke(null,item.invoke(original,m,oldRand,fortune))==Item.getIdFromItem(b.getItemDropped(state,newRand,fortune))&&oldRand.nextLong()==newRand.nextLong(),"actual old/native item identity and RNG");oldRand=new Random(seed);newRand=new Random(seed);check((Integer)quantity.invoke(original,fortune,oldRand)==b.quantityDroppedWithBonus(fortune,newRand)&&oldRand.nextLong()==newRand.nextLong(),"actual old/native quantity and RNG");}
    BlockPos p=new BlockPos(8,96+m,8);check(w.setBlockState(p,state,3)&&w.getBlockState(p)==state,"actual World/Chunk placement");check(!b.hasTileEntity(state)&&b.createTileEntity(w,state)==null&&w.getTileEntity(p)==null,"no tile lifecycle");NonNullList<ItemStack> drops=NonNullList.create();b.getDrops(drops,w,p,state,3);check(drops.size()==1&&drops.get(0).getItem()==Item.getItemFromBlock(b)&&drops.get(0).getItemDamage()==m,"actual native mining drop");
   }
  }
  net.minecraft.world.chunk.storage.AnvilChunkLoader loader=new net.minecraft.world.chunk.storage.AnvilChunkLoader(new File("chunk-storage"),new net.minecraft.util.datafix.DataFixer(1343));Method write=loader.getClass().getDeclaredMethod("writeChunkToNBT",Chunk.class,World.class,NBTTagCompound.class),read=loader.getClass().getDeclaredMethod("readChunkFromNBT",World.class,NBTTagCompound.class);write.setAccessible(true);read.setAccessible(true);NBTTagCompound saved=new NBTTagCompound();write.invoke(loader,w.getChunk(0,0),w,saved);ByteArrayOutputStream bytes=new ByteArrayOutputStream();CompressedStreamTools.writeCompressed(saved,bytes);NBTTagCompound restored=CompressedStreamTools.readCompressed(new ByteArrayInputStream(bytes.toByteArray()));NativeWorld fresh=new NativeWorld();Chunk loaded=(Chunk)read.invoke(loader,fresh,restored);fresh.chunks.put(NativeWorld.key(0,0),loaded);loaded.markLoaded(true);
  for(int m=0;m<4;m++){BlockPos p=new BlockPos(8,96+m,8);check(fresh.getBlockState(p)==b.getStateFromMeta(m)&&fresh.getTileEntity(p)==null,"compressed native Anvil state/tile roundtrip");fresh.spawned.clear();check(fresh.destroyBlock(p,true)&&fresh.isAirBlock(p)&&fresh.spawned.size()==1&&fresh.spawned.get(0).getItemDamage()==m,"actual native destruction/drop preserves variant");}
 }
}
