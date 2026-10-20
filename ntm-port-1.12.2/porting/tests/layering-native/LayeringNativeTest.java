import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import com.hbm.blocks.generic.BlockLayering;
import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.machine.ZirnoxDestroyed;
import com.hbm.blocks.machine.rbmk.RBMKDebris;


import com.mojang.authlib.GameProfile;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Bootstrap;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.profiler.Profiler;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldProviderSurface;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.storage.SaveHandlerMP;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.fml.common.DummyModContainer;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModMetadata;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

/** Real native owner and World/Chunk fixture; only NTM registry/type dependencies are stand-ins. */
public class LayeringNativeTest {
 static int checks;
 static final BlockPos POS = new BlockPos(8,64,8);
 static void check(boolean ok,String label) { checks++; if(!ok) throw new AssertionError(label); }
 static final class NativeWorld extends World {
  final Map<Long,Chunk> chunks=new HashMap<>();
  final List<String> events=new ArrayList<>();
  int flags=-1,writes;BlockPos lastWrite;IBlockState written;
  NativeWorld(boolean remote){super(new SaveHandlerMP(),new WorldInfo(new NBTTagCompound()),new WorldProviderSurface(),new Profiler(),remote);provider.setWorld(this);chunkProvider=createChunkProvider();for(int x=-1;x<=1;x++)for(int z=-1;z<=1;z++){Chunk c=new Chunk(this,x,z);c.markLoaded(true);chunks.put(key(x,z),c);}}
  static long key(int x,int z){return ((long)x<<32)^(z&0xffffffffL);}
  protected IChunkProvider createChunkProvider(){return new IChunkProvider(){public Chunk getLoadedChunk(int x,int z){return chunks.get(key(x,z));}public Chunk provideChunk(int x,int z){return getLoadedChunk(x,z);}public boolean tick(){return false;}public String makeString(){return "layering native proof";}public boolean isChunkGeneratedAt(int x,int z){return getLoadedChunk(x,z)!=null;}};}
  protected boolean isChunkLoaded(int x,int z,boolean empty){return chunks.containsKey(key(x,z));}
  public boolean checkLight(BlockPos p){return true;}
  public boolean setBlockState(BlockPos p,IBlockState s,int f){flags=f;writes++;lastWrite=p.toImmutable();written=s;events.add(s.getBlock()==Blocks.AIR?"air":"state");return super.setBlockState(p,s,f);}
  void put(BlockPos p,IBlockState s){getChunk(p).setBlockState(p,s);}
  void reset(){flags=-1;writes=0;lastWrite=null;written=null;events.clear();}
 }

 static final class Player extends EntityPlayer {
  int stats, exhaustions;
  public void addStat(net.minecraft.stats.StatBase stat) { stats++; ((NativeWorld)world).events.add("stat"); super.addStat(stat); }
  public void addExhaustion(float amount) { exhaustions++; ((NativeWorld)world).events.add("exhaustion"); super.addExhaustion(amount); }
  Player(World world){super(world,new GameProfile(new UUID(11,19),"layering-native-proof"));}
  public boolean isSpectator(){return false;}
  public boolean isCreative(){return false;}
 }
 static BlockLayering[] layers;
 static void setup() {
  Bootstrap.register();
  ModMetadata md=new ModMetadata();md.modId="hbm";md.name="Layering proof";md.version="probe";
  DummyModContainer container=new DummyModContainer(md);Loader.instance().setupTestHarness(container);Loader.instance().setActiveModContainer(container);
  layers=new BlockLayering[]{new BlockLayering(Material.SNOW),new BlockLayering(Material.SAND),new BlockLayering(Material.LEAVES),new BlockLayering(Material.GROUND)};
  ModBlocks.foam_layer=layers[0];ModBlocks.sand_boron_layer=layers[1];ModBlocks.leaves_layer=layers[2];ModBlocks.oil_spill=layers[3];
  String[] names={"foam_layer","sand_boron_layer","leaves_layer","oil_spill"};
  for(int i=0;i<4;i++){layers[i].setRegistryName("hbm",names[i]);ForgeRegistries.BLOCKS.register(layers[i]);}
 }
 public static void main(String[] args) throws Exception {
  setup();NativeWorld w=new NativeWorld(false);Player player=new Player(w);OriginalLayerOracle oracle=new OriginalLayerOracle();Random rng=new Random(719);
  for(int k=0;k<4;k++) for(int m=0;m<16;m++) {
   BlockLayering b=layers[k];Block dispatch=b;IBlockState s=b.getStateFromMeta(m);
   check(b.getBlockState().getValidStates().size()==16,"16 distinct states");
   check(b.getMetaFromState(s)==m,"nibble roundtrip");
   check(s.getBlock()==b,"owner identity");
   check(Block.getStateById(Block.getStateId(s))==s,"native serialized block/meta ID roundtrip");
   w.put(POS,s);check(w.getBlockState(POS)==s,"real Chunk state roundtrip");
   oracle.bounds(m);AxisAlignedBB box=s.getBoundingBox(w,POS);
   check(box.minX==0&&box.minY==0&&box.minZ==0&&box.maxX==1&&box.maxZ==1&&box.maxY==oracle.height,"original selection bounds");
   AxisAlignedBB collision=s.getCollisionBoundingBox(w,POS);
   check(collision.maxY==oracle.collision(m,0),"original collision height");
   List<AxisAlignedBB> hits=new ArrayList<>();
   s.addCollisionBoxToList(w,POS,new AxisAlignedBB(7,63,7,10,66,10),hits,null,false);
   check(hits.size()==1&&hits.get(0).maxY==oracle.collision(m,64),"native state collision dispatch and world offset");
   for(int fortune:new int[]{0,1,9}) {
    check(dispatch.quantityDropped(s,fortune,rng)==oracle.quantity(m,fortune,rng),"original quantity formula native dispatch");
    check(dispatch.getItemDropped(s,rng,fortune)==Items.AIR,"original no normal drop");
    net.minecraft.util.NonNullList<ItemStack> drops=net.minecraft.util.NonNullList.create();dispatch.getDrops(drops,w,POS,s,fortune);check(drops.isEmpty(),"real native drop callback produces no stacks");
   }
   check(!s.isOpaqueCube()&&!s.isFullCube(),"partial nonopaque state");
   for(EnumFacing face:EnumFacing.values()){check(!s.isSideSolid(w,POS,face),"non-normal layer has no solid attachment face");check(s.getBlockFaceShape(w,POS,face)==BlockFaceShape.UNDEFINED,"native face shape stays non-solid");}
   check(dispatch.isReplaceable(w,POS)==(k!=1||oracle.replace(m,Material.SAND.isReplaceable())),"variant replacement");
   w.put(POS.up(),Blocks.STONE.getDefaultState());check(s.shouldSideBeRendered(w,POS,EnumFacing.UP),"top always visible");
   w.put(POS.east(),Blocks.STONE.getDefaultState());check(!s.shouldSideBeRendered(w,POS,EnumFacing.EAST),"opaque side hidden");
   w.put(POS.east(),Blocks.AIR.getDefaultState());check(s.shouldSideBeRendered(w,POS,EnumFacing.EAST),"air side visible");
   w.reset();dispatch.updateTick(w,POS,s,rng);check(w.writes==0,"disabled melting remains disabled");
   w.put(POS.down(),Blocks.STONE.getDefaultState());check(dispatch.canPlaceBlockAt(w,POS),"opaque support accepted");
   w.reset();s.neighborChanged(w,POS,Blocks.STONE,POS.down());check(w.writes==0,"valid neighbor support retained");
   w.put(POS.down(),Blocks.AIR.getDefaultState());w.reset();s.neighborChanged(w,POS,Blocks.AIR,POS.down());
   check(w.writes==1&&w.flags==3&&w.getBlockState(POS).getBlock()==Blocks.AIR,"native state neighbor dispatch removes unsupported layer through real World/Chunk");
   w.put(POS,s);w.reset();int oldStats=player.stats,oldExhaustions=player.exhaustions;
   dispatch.harvestBlock(w,player,POS,s,null,ItemStack.EMPTY);
   check(player.stats==oldStats+1&&player.exhaustions==oldExhaustions+1,"native superclass harvest invoked exactly once");
   check(w.writes==1&&w.flags==3&&w.getBlockState(POS).getBlock()==Blocks.AIR,"native harvest final removal");
   check(w.loadedEntityList.isEmpty(),"harvest spawned no items");
   check(w.events.equals(Arrays.asList("stat","exhaustion","air")),"native superclass effects before final air removal");
   check(!dispatch.canSilkHarvest(w,POS,s,player),"non-full original layer not silk-harvestable");
   for(Block support:new Block[]{Blocks.ICE,Blocks.PACKED_ICE,Blocks.GLASS,Blocks.AIR}) {w.put(POS.down(),support.getDefaultState());check(!dispatch.canPlaceBlockAt(w,POS),"rejected support "+support);}
   w.put(POS.down(),Blocks.LEAVES.getDefaultState());check(dispatch.canPlaceBlockAt(w,POS),"leaves support accepted");
   w.put(POS.down(),s);check(dispatch.canPlaceBlockAt(w,POS)==(k==2||(m&7)==7),"same-owner full layer support including high bit");
   w.put(POS.down(),layers[(k+1)%4].getStateFromMeta(7));check(dispatch.canPlaceBlockAt(w,POS)==((k+1)%4==2),"other layer owner only supports through leaves material");
  }
  for(BlockLayering b:layers)for(int raw:new int[]{-17,-1,16,31,255})check(b.getMetaFromState(b.getStateFromMeta(raw))==(raw&15),"stored nibble normalization");
  NativeWorld client=new NativeWorld(true);Player clientPlayer=new Player(client);
  for(BlockLayering b:layers){IBlockState s=b.getStateFromMeta(15);client.put(POS,Blocks.STONE.getDefaultState());client.reset();b.harvestBlock(client,clientPlayer,POS,s,null,ItemStack.EMPTY);check(client.writes==1&&client.getBlockState(POS).getBlock()==Blocks.AIR,"client harvest preserves unconditional original final removal and stale-position semantics");}
  // A replaceable non-exempt material exposes the raw >= 7 distinction hidden
  // by the sand instance's nonreplaceable material.
  BlockLayering ordinary=new BlockLayering(Material.SNOW);
  for(int m=0;m<16;m++){w.put(POS,ordinary.getStateFromMeta(m));check(ordinary.isReplaceable(w,POS)==oracle.replace(m,true),"unmasked replacement cutoff "+m);}
  // Decoder is the actual native state owner, not the queried layer block.
  w.put(POS,Blocks.SNOW_LAYER.getStateFromMeta(7));check(!ordinary.isReplaceable(w,POS),"foreign owner decode");
  w.put(POS,Blocks.STONE.getDefaultState());check(ordinary.isReplaceable(w,POS),"foreign state without layer property");
  for(Block special:new Block[]{new RBMKDebris(),new ZirnoxDestroyed()}){w.put(POS.down(),special.getDefaultState());check(layers[0].canPlaceBlockAt(w,POS),"special support type");}
  for(BlockPos p:new BlockPos[]{new BlockPos(8,0,8),new BlockPos(30000000,64,8),new BlockPos(-30000001,64,8)})check(!layers[0].canPlaceBlockAt(w,p),"original World boundary rejects support without chunk access");
  System.out.println("PASS "+checks+" native owner/state/Chunk/geometry/drop/support/neighbor/replacement assertions");
 }
}
