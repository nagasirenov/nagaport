import java.util.*;
import net.minecraft.block.*;
import net.minecraft.block.material.*;
import net.minecraft.block.state.*;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.init.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;
import com.hbm.world.WorldBlockState;
import com.hbm.blocks.machine.BlockBeamBase;
import com.hbm.blocks.generic.BlockHangingVine;

/** Exact owner-method projections and genuine native engine/full-class controls. */
public final class PlacementNativeTest {
 static int checks; static boolean stayResult; static Block stayOwner;static World stayWorld;static BlockPos stayPos;static int stayCalls;
 static final List<String> trace=new ArrayList<>();
 static void check(boolean b,String m){checks++;if(!b)throw new AssertionError(m+" "+trace);}
 static boolean stay(Block b,World w,int x,int y,int z){trace.add("stay");stayCalls++;stayOwner=b;stayWorld=w;stayPos=new BlockPos(x,y,z);return stayResult;}
 static final class W extends World {
  final Map<BlockPos,IBlockState> states=new HashMap<>();int reads;int height=256;boolean worldSolid;boolean poison;
  W(){super(new net.minecraft.world.storage.SaveHandlerMP(),new net.minecraft.world.storage.WorldInfo(new NBTTagCompound()),new WorldProviderSurface(),new net.minecraft.profiler.Profiler(),false);}
  protected net.minecraft.world.chunk.IChunkProvider createChunkProvider(){return null;}
  protected boolean isChunkLoaded(int x,int z,boolean e){throw new AssertionError("unexpected chunk read");}
  public IBlockState getBlockState(BlockPos p){if(poison)throw new AssertionError("unexpected world read");reads++;trace.add("read:"+p.getX()+","+p.getY()+","+p.getZ());return states.getOrDefault(p,Blocks.AIR.getDefaultState());}
  public int getHeight(){trace.add("height");return height;}
  public boolean isSideSolid(BlockPos p,EnumFacing f,boolean fallback){trace.add("world-side:"+f);return worldSolid;}
  public boolean isAirBlock(BlockPos p){trace.add("world-air");IBlockState s=getBlockState(p);return s.getBlock().isAir(s,this,p);}
  void put(BlockPos p,Block b){states.put(p,b.getDefaultState());}
  void reset(){states.clear();reads=0;worldSolid=false;poison=false;trace.clear();stayCalls=0;stayResult=false;}
 }
 static class Probe extends Block {
  static final PropertyInteger META=PropertyInteger.create("meta",0,15);
  boolean solid,replaceable,leaves,opaque,air;IBlockState supplied;BlockPos position;EnumFacing side;Runnable onSide,onLeaves;
  Probe(){super(Material.ROCK);}
  protected BlockStateContainer createBlockState(){return new BlockStateContainer(this,META);}
  public IBlockState getStateFromMeta(int m){return getDefaultState().withProperty(META,m&15);}
  public int getMetaFromState(IBlockState s){return s.getValue(META);}
  public boolean isSideSolid(IBlockState s,IBlockAccess w,BlockPos p,EnumFacing f){trace.add("block-side:"+f);supplied=s;position=p;side=f;if(onSide!=null)onSide.run();return solid;}
  public boolean isReplaceable(IBlockAccess w,BlockPos p){trace.add("replace");position=p;return replaceable;}
  public boolean isLeaves(IBlockState s,IBlockAccess w,BlockPos p){trace.add("leaves");supplied=s;position=p;if(onLeaves!=null)onLeaves.run();return leaves;}
  public boolean isAir(IBlockState s,IBlockAccess w,BlockPos p){trace.add("air");supplied=s;position=p;return air;}
  public boolean isOpaqueCube(IBlockState s){trace.add("opaque");supplied=s;return opaque;}
 }
 static class LayerFallout extends PlacementOwners.BlockFallout {
  static final PropertyInteger META=PropertyInteger.create("meta",0,15);
  protected BlockStateContainer createBlockState(){return new BlockStateContainer(this,META);}
  public IBlockState getStateFromMeta(int m){return getDefaultState().withProperty(META,m&15);}
  public int getMetaFromState(IBlockState s){return s.getValue(META);}
 }
 static class LayerFlesh extends PlacementOwners.MeltedFlesh {
  static final PropertyInteger META=PropertyInteger.create("meta",0,15);
  protected BlockStateContainer createBlockState(){return new BlockStateContainer(this,META);}
  public IBlockState getStateFromMeta(int m){return getDefaultState().withProperty(META,m&15);}
  public int getMetaFromState(IBlockState s){return s.getValue(META);}
 }
 static class Beam extends BlockBeamBase {public TileEntity createNewTileEntity(World w,int m){throw new AssertionError("unused factory");}}
 static World argWorld(W w){trace.add("arg-world");return w;}
 static Block argBlock(Block b){trace.add("arg-block");return b;}
 static int arg(char c,int v){trace.add("arg-"+c);return v;}
 static int fail(){trace.add("arg-X");throw new IllegalStateException();}
 static boolean place(Block b,W w,BlockPos p){return b.canPlaceBlockAt(w,p);}
 public static void main(String[] args)throws Exception{
  Bootstrap.register();W w=new W();BlockPos p=new BlockPos(13,64,-9);Probe target=new Probe(),below=new Probe();IBlockState support=below.getStateFromMeta(11);
  // Invoke the genuine signed original engine predicate, not a copied fence test.
  java.net.URLClassLoader old=new java.net.URLClassLoader(new java.net.URL[]{java.nio.file.Paths.get(args[0]).toUri().toURL()},PlacementNativeTest.class.getClassLoader());
  Class<?> originalBlock=old.loadClass("aji"),originalBlocks=old.loadClass("ajn"),originalFence=old.loadClass("akz");originalBlock.getMethod("p").invoke(null);
  Object oak=originalBlocks.getField("aJ").get(null),nether=originalBlocks.getField("bk").get(null);java.lang.reflect.Method fencePredicate=originalFence.getMethod("a",originalBlock);int oldFences=0;
  for(java.lang.reflect.Field field:originalBlocks.getFields())if(java.lang.reflect.Modifier.isStatic(field.getModifiers())&&originalBlock.isAssignableFrom(field.getType())){Object b=field.get(null);boolean result=(Boolean)fencePredicate.invoke(null,b);check(result==(b==oak||b==nether),"actual original fence predicate "+field.getName());if(result)oldFences++;}check(oldFences==2,"exactly two original fence identities");old.close();
  // Native base targets the world occupant, not the prospective placing block.
  Block base=new Block(Material.ROCK){};for(boolean replace:new boolean[]{false,true}){w.reset();target.replaceable=replace;w.put(p,target);check(place(base,w,p)==replace,"genuine native target replaceability");check(trace.equals(Arrays.asList("read:13,64,-9","replace")),"base hook trace");}
  Block defaultAir=new Block(Material.AIR){};for(Block occupant:new Block[]{defaultAir,Blocks.STONE}){w.reset();w.put(p,occupant);check(place(base,w,p)==(occupant==defaultAir),"actual native default material replaceability");check(w.reads==2,"native default replacement deliberately rereads target state, unlike old engine default");}
  w.reset();w.put(p,Blocks.AIR);check(place(base,w,p)&&w.reads==1,"actual native BlockAir override avoids default reread");
  // All seven stay-delegating owner methods are exact current production slices.
  Block[] stayOwners={new PlacementOwners.BlockVinylVine(),new PlacementOwners.BlockWaterPlant(),new PlacementOwners.BlockDeadPlant(),new PlacementOwners.BlockMush(),new PlacementOwners.BlockRubberPlant(),new PlacementOwners.BlockTallPlant(),new PlacementOwners.BlockTallPlantWater()};
  for(Block b:stayOwners)for(boolean replace:new boolean[]{false,true})for(boolean stay:new boolean[]{false,true})for(boolean above:new boolean[]{false,true}){
   w.reset();target.replaceable=replace;w.put(p,target);stayResult=stay;boolean water=b instanceof PlacementOwners.BlockWaterPlant||b instanceof PlacementOwners.BlockTallPlantWater;boolean tall=b instanceof PlacementOwners.BlockTallPlant;
   w.put(p.up(),water?(above?Blocks.WATER:Blocks.STONE):(above?Blocks.AIR:Blocks.STONE));
   boolean expected=replace&&stay&&(!(water||tall)||above);check(place(b,w,p)==expected,"exact delegate projection "+b.getClass().getSimpleName());check(stayCalls==(replace?1:0),"stay short circuit");if(replace)check(stayOwner==b&&stayWorld==w&&stayPos.equals(p),"own virtual stay and exact coordinates");if(!replace||!stay)check(w.reads==1,"later above query short-circuited");
  }
  PlacementOwners.BlockKelp kelp=new PlacementOwners.BlockKelp();PlacementOwners.ModBlocks.laythe_silt=new Block(Material.SAND){};
  for(Block b:new Block[]{kelp,PlacementOwners.ModBlocks.laythe_silt,Blocks.SAND,Blocks.AIR}){w.reset();w.put(p.down(),b);check(place(kelp,w,p)==(b==kelp||b==PlacementOwners.ModBlocks.laythe_silt),"Kelp exact two identities");check(w.reads==1,"Kelp one read");}
  PlacementOwners.BlockOrbitalStation station=new PlacementOwners.BlockOrbitalStation();PlacementOwners.ModBlocks.orbital_station=station;
  w.reset();w.poison=true;check(!place(station,w,p),"main orbital port forbids before world read");check(!station.canPlaceBlockAt(null,p),"main orbital port retains null-world early exit");
  PlacementOwners.BlockOrbitalStationLauncher launcher=new PlacementOwners.BlockOrbitalStationLauncher();w.reset();target.replaceable=true;w.put(p,target);check(place(launcher,w,p),"launcher inherits non-main identity native base path");
  PlacementOwners.DigammaFlame flame=new PlacementOwners.DigammaFlame();PlacementOwners.Landmine mine=new PlacementOwners.Landmine();
  for(boolean solid:new boolean[]{false,true}){w.reset();below.solid=solid;w.states.put(p.down(),support);check(place(flame,w,p)==solid,"Digamma original Forge support hook");check(below.supplied==support&&below.side==EnumFacing.UP&&below.position.equals(p.down()),"exact support state/context");check(!trace.contains("world-side:up"),"not the distinct world-side loaded-chunk fallback");}
  below.solid=false;for(Block fence:new Block[]{Blocks.OAK_FENCE,Blocks.NETHER_BRICK_FENCE,Blocks.SPRUCE_FENCE,Blocks.BIRCH_FENCE,Blocks.OAK_FENCE_GATE,Blocks.AIR}){w.reset();w.put(p.down(),fence);check(place(mine,w,p)==(fence==Blocks.OAK_FENCE||fence==Blocks.NETHER_BRICK_FENCE),"Landmine exact original fence identities");check(w.reads==2,"Landmine fallback repeats its original read");}
  w.reset();below.solid=true;w.states.put(p.down(),support);check(place(mine,w,p)&&w.reads==1,"Landmine solid short circuit");
  w.reset();below.solid=false;below.onSide=()->w.put(p.down(),Blocks.OAK_FENCE);w.states.put(p.down(),support);check(place(mine,w,p)&&w.reads==2,"Landmine fallback observes original intervening support callback mutation");below.onSide=null;
  PlacementOwners.BlockStalagmite stalag=new PlacementOwners.BlockStalagmite(),stalact=new PlacementOwners.BlockStalagmite(),other=new PlacementOwners.BlockStalagmite();PlacementOwners.ModBlocks.stalagmite=stalag;PlacementOwners.ModBlocks.stalactite=stalact;
  for(boolean solid:new boolean[]{false,true}){w.reset();below.solid=solid;w.states.put(p.down(),support);check(place(stalag,w,p)==solid,"stalagmite block-UP hook");w.reset();w.worldSolid=solid;check(place(stalact,w,p)==solid&&trace.equals(Arrays.asList("world-side:down")),"stalactite retains distinct original world-DOWN hook");}
  w.reset();w.poison=true;check(place(other,w,p),"other stalagmite identity unconditional true");
  // Door + original NTM gate descendant retain height, support, lower, upper order.
  for(Block door:new Block[]{new PlacementOwners.BlockModDoor(),new PlacementOwners.BlockFenceGate()})for(int height:new int[]{64,65,66,256})for(boolean solid:new boolean[]{false,true})for(boolean lower:new boolean[]{false,true})for(boolean upper:new boolean[]{false,true}){
   w.reset();w.height=height;below.solid=solid;target.replaceable=lower;Probe upperBlock=new Probe();upperBlock.replaceable=upper;w.states.put(p.down(),support);w.put(p,target);w.put(p.up(),upperBlock);trace.clear();
   check(place(door,w,p)==(64<height-1&&solid&&lower&&upper),"door/gate exact condition chain");check(trace.get(0).equals("height"),"door height first");if(64>=height-1)check(w.reads==0,"door upper-limit short circuit");
  }
  // Layer predicates retain original distinct leaves/air and complete nibble rules.
  PlacementOwners.BlockFallout fallout=new PlacementOwners.BlockFallout();PlacementOwners.MeltedFlesh flesh=new PlacementOwners.MeltedFlesh();
  for(Block owner:new Block[]{fallout,flesh})for(Block ice:new Block[]{Blocks.ICE,Blocks.PACKED_ICE}){w.reset();w.put(p.down(),ice);check(!place(owner,w,p)&&w.reads==1,"layer ice exclusion");}
  for(boolean leaves:new boolean[]{false,true})for(boolean air:new boolean[]{false,true})for(boolean opaque:new boolean[]{false,true}){
   w.reset();below.leaves=leaves;below.air=air;below.opaque=opaque;w.states.put(p.down(),support);check(place(fallout,w,p)==(leaves&&!air||opaque),"Fallout leaves excludes air then opaque");w.reset();w.states.put(p.down(),support);check(place(flesh,w,p)==(leaves||opaque),"Flesh leaves intentionally includes air");
  }
  // Native leaves/air/opacity must use the initially captured state, even if a hook mutates the world.
  for(Block owner:new Block[]{fallout,flesh}){w.reset();below.leaves=false;below.opaque=true;below.onLeaves=()->w.put(p.down(),Blocks.AIR);w.states.put(p.down(),support);check(place(owner,w,p),"captured non-default support survives leaf callback mutation");check(below.supplied==support&&below.position.equals(p.down())&&w.reads==1,"leaves and opacity receive exact original state without re-read");below.onLeaves=null;}
  w.reset();below.leaves=true;below.air=true;below.opaque=true;below.onLeaves=()->w.put(p.down(),Blocks.STONE);w.states.put(p.down(),support);check(place(fallout,w,p),"Fallout air then opaque uses original receiver after mutation");check(below.supplied==support&&w.reads==1&&trace.equals(Arrays.asList("read:13,63,-9","leaves","air","opaque")),"Fallout capture and callback order");below.onLeaves=null;
  for(Block owner:new Block[]{new LayerFallout(),new LayerFlesh()})for(int meta=0;meta<16;meta++){w.reset();w.states.put(p.down(),owner.getStateFromMeta(meta));check(place(owner,w,p)==((meta&7)==7),"exact original low-three-bit stacking rule");}
  Beam beam=new Beam();w.reset();w.put(p.down(),beam);check(!place(fallout,w,p),"actual full Beam leaves+air excluded from Fallout");check(place(flesh,w,p),"actual full Beam leaves accepted by Flesh");
  // Real complete existing native owner/state classes, not projections.
  BlockHangingVine vine=new BlockHangingVine(Material.PLANTS);for(boolean solid:new boolean[]{false,true}){w.reset();w.worldSolid=solid;check(place(vine,w,p)==solid,"actual full NTM HangingVine support");}w.reset();w.put(p.up(),vine);check(place(vine,w,p),"actual full NTM HangingVine same owner");
  w.reset();target.replaceable=true;w.put(p,target);trace.clear();check(argBlock(base).canPlaceBlockAt(argWorld(w),new BlockPos(arg('x',13),arg('y',64),arg('z',-9))),"native call argument wrapper");check(trace.subList(0,5).equals(Arrays.asList("arg-block","arg-world","arg-x","arg-y","arg-z")),"receiver/world/XYZ order");
  trace.clear();try{argBlock(null).canPlaceBlockAt(argWorld(w),new BlockPos(arg('x',13),arg('y',64),arg('z',-9)));throw new AssertionError();}catch(NullPointerException expected){check(trace.equals(Arrays.asList("arg-block","arg-world","arg-x","arg-y","arg-z")),"null receiver still evaluates arguments");}
  trace.clear();try{argBlock(base).canPlaceBlockAt(argWorld(w),new BlockPos(fail(),arg('y',64),arg('z',-9)));throw new AssertionError();}catch(IllegalStateException expected){check(trace.equals(Arrays.asList("arg-block","arg-world","arg-X")),"argument throw order");}
  trace.clear();check(!(false&&argBlock(base).canPlaceBlockAt(argWorld(w),new BlockPos(arg('x',0),0,0)))&&trace.isEmpty(),"outer short circuit");
  System.out.println("PASS "+checks+" assertions; exact 15 owner-method projections and two inherited descendants, actual native World/Block/BlockVine hooks, full actual NTM Beam/HangingVine/PlantBlockState; scripted collaborator fixtures, not complete migrated owner classes");
 }
}
