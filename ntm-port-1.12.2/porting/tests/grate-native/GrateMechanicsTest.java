import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import com.hbm.blocks.generic.BlockGrate;
import com.hbm.main.MainRegistry;
import com.hbm.util.i18n.ITranslate;
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

/** Complete native owner, actual native World/Chunk and actual native entity proof.
 * The independent oracle executes verbatim original owner method bodies in a
 * narrowly named recording environment; it is not a substitute Minecraft API.
 * Only MainRegistry's translation-service holder is a controlled NTM collaborator.
 */
public final class GrateMechanicsTest {
 static final BlockPos POS=new BlockPos(8,64,8);
 static final AxisAlignedBB ALL=new AxisAlignedBB(7,62,7,10,68,10);
 static final AxisAlignedBB MISS=new AxisAlignedBB(-10,-10,-10,-9,-9,-9);
 static final double[] FULL={0,0,0,1,1,1};
 static long checks;
 static RecordingGrate[] grates;
 static Support support;
 static void check(boolean ok,String message){checks++;if(!ok)throw new AssertionError(message);}
 static void exact(double got,double want,String message){check(Double.doubleToLongBits(got)==Double.doubleToLongBits(want),message+": "+got+" != "+want);}
 static double[] values(AxisAlignedBB b){return new double[]{b.minX,b.minY,b.minZ,b.maxX,b.maxY,b.maxZ};}
 static void box(AxisAlignedBB got,double[] want,String message){check(Arrays.equals(values(got),want),message+": "+Arrays.toString(values(got))+" != "+Arrays.toString(want));}
 static AxisAlignedBB shape(double[] b){return b==null?null:new AxisAlignedBB(b[0],b[1],b[2],b[3],b[4],b[5]);}
 static double[] lower(double y){return new double[]{0,0,0,1,y,1};}
 static double[] upper(double y){return new double[]{0,y,0,1,1,1};}

 static final class NativeWorld extends World {
  final Map<Long,Chunk> chunks=new HashMap<>();
  final List<String> events=new ArrayList<>();
  int flags=-1,writes;BlockPos lastWrite;IBlockState written;
  NativeWorld(boolean remote){super(new SaveHandlerMP(),new WorldInfo(new NBTTagCompound()),new WorldProviderSurface(),new Profiler(),remote);provider.setWorld(this);chunkProvider=createChunkProvider();for(int x=-1;x<=1;x++)for(int z=-1;z<=1;z++){Chunk c=new Chunk(this,x,z);c.markLoaded(true);chunks.put(key(x,z),c);}}
  static long key(int x,int z){return ((long)x<<32)^(z&0xffffffffL);}
  protected IChunkProvider createChunkProvider(){return new IChunkProvider(){public Chunk getLoadedChunk(int x,int z){return chunks.get(key(x,z));}public Chunk provideChunk(int x,int z){return getLoadedChunk(x,z);}public boolean tick(){return false;}public String makeString(){return "grate native proof";}public boolean isChunkGeneratedAt(int x,int z){return getLoadedChunk(x,z)!=null;}};}
  protected boolean isChunkLoaded(int x,int z,boolean empty){return chunks.containsKey(key(x,z));}
  public boolean checkLight(BlockPos p){return true;}
  public boolean setBlockState(BlockPos p,IBlockState s,int f){flags=f;writes++;lastWrite=p.toImmutable();written=s;events.add(s.getBlock()==Blocks.AIR?"air":"state");return super.setBlockState(p,s,f);}
  void put(BlockPos p,IBlockState s){getChunk(p).setBlockState(p,s);}
  void reset(){flags=-1;writes=0;lastWrite=null;written=null;events.clear();}
 }
 static final class Player extends EntityPlayer {
  Player(World world){super(world,new GameProfile(new UUID(11,19),"grate-native-proof"));}
  public boolean isSpectator(){return false;}
  public boolean isCreative(){return false;}
 }
 static final class RecordingGrate extends BlockGrate {
  int drops,dropMeta=-1,fortune=-1;float chance;BlockPos dropPos;IBlockState dropped;
  RecordingGrate(boolean wide){super(Material.IRON,wide);}
  @Override public void dropBlockAsItemWithChance(World world,BlockPos pos,IBlockState state,float chance,int fortune){drops++;dropMeta=getMetaFromState(state);this.fortune=fortune;this.chance=chance;dropPos=pos.toImmutable();dropped=state;((NativeWorld)world).events.add("drop");}
  void reset(){drops=0;dropMeta=fortune=-1;dropPos=null;dropped=null;chance=0;}
 }
 static final class Support extends Block {
  final Map<BlockPos,AxisAlignedBB> shapes=new HashMap<>();
  final List<BlockPos> airQueries=new ArrayList<>(),collisionQueries=new ArrayList<>();
  final List<IBlockState> airStates=new ArrayList<>();
  boolean air;BlockPos airOnlyAt;
  Support(){super(Material.IRON);}
  @Override public AxisAlignedBB getCollisionBoundingBox(IBlockState state,IBlockAccess world,BlockPos pos){collisionQueries.add(pos.toImmutable());return shapes.get(pos);}
  @Override public boolean isAir(IBlockState state,IBlockAccess world,BlockPos pos){airQueries.add(pos.toImmutable());airStates.add(state);return airOnlyAt==null?air:airOnlyAt.equals(pos);}
  void configure(double[] below,double[] above,boolean isAir){shapes.clear();shapes.put(POS.down(),shape(below));shapes.put(POS.up(),shape(above));air=isAir;airOnlyAt=null;resetQueries();}
  void resetQueries(){airQueries.clear();collisionQueries.clear();airStates.clear();}
 }
 @SuppressWarnings("unchecked")
 static void setup()throws Exception{
  Bootstrap.register();ModMetadata md=new ModMetadata();md.modId="hbm";md.name="Grate native mechanics proof";md.version="probe";DummyModContainer container=new DummyModContainer(md);Loader.instance().setupTestHarness(container);Loader.instance().setActiveModContainer(container);
  grates=new RecordingGrate[]{new RecordingGrate(false),new RecordingGrate(true)};
  Field mapField=Item.class.getDeclaredField("BLOCK_TO_ITEM");mapField.setAccessible(true);Map<Block,Item> items=(Map<Block,Item>)mapField.get(null);
  for(int i=0;i<2;i++){RecordingGrate b=grates[i];b.setTranslationKey(i==0?"steel_grate":"steel_grate_wide");b.setRegistryName("hbm",i==0?"grate_mechanics_narrow":"grate_mechanics_wide");ForgeRegistries.BLOCKS.register(b);ItemBlock item=new ItemBlock(b);item.setRegistryName(b.getRegistryName());ForgeRegistries.ITEMS.register(item);items.put(b,item);}
  support=new Support();support.setRegistryName("hbm","grate_mechanics_support");ForgeRegistries.BLOCKS.register(support);
 }
 static void prepare(NativeWorld w,RecordingGrate b,int meta,double[] below,double[] above,boolean air){
  support.configure(below,above,air);w.put(POS.down(),support.getDefaultState());w.put(POS.up(),support.getDefaultState());w.put(POS,b.getStateFromMeta(meta));w.reset();b.reset();support.resetQueries();
 }
 static void boundsAndSupport(){
  NativeWorld world=new NativeWorld(false);
  for(int kind=0;kind<2;kind++)for(int meta=0;meta<16;meta++){
   RecordingGrate b=grates[kind];IBlockState state=b.getStateFromMeta(meta);String label="wide="+(kind==1)+" meta="+meta;
   check(b.getMetaFromState(state)==meta,"metadata roundtrip "+label);check(b.isWide()==(kind==1),"owner identity "+label);
   exact(b.getY(meta),OriginalGrateMechanicsOracle.owner(kind==1).getY(meta),"original getY "+label);
   double[] expected=OriginalGrateMechanicsOracle.bounds(kind==1,meta);
   box(b.getBoundingBox(state,world,POS),expected,"original exact float selection "+label);box(b.getCollisionBoundingBox(state,world,POS),expected,"original exact float collision "+label);
   check(!state.isOpaqueCube()&&!state.isFullCube()&&!b.hasTileEntity(state),"nonopaque/nonfull/no tile "+label);check(state.getRenderType()==EnumBlockRenderType.MODEL&&b.getRenderLayer()==BlockRenderLayer.SOLID,"native MODEL/SOLID "+label);
   check(b.damageDropped(state)==0&&b.getItemDropped(state,new Random(1),17)==Item.getItemFromBlock(b),"normal own item metadata-zero drop "+label);check(b.getSoundType(state,world,POS,null)==SoundType.STONE,"original default stone sound "+label);
   world.put(POS,state);check(world.getBlockState(POS).equals(state)&&world.getTileEntity(POS)==null,"actual chunk metadata/no tile "+label);
   OriginalGrateMechanicsOracle.World original=OriginalGrateMechanicsOracle.world(meta,false,null,null,false);
   for(EnumFacing face:EnumFacing.values()){
    boolean want=OriginalGrateMechanicsOracle.owner(kind==1).isSideSolid(original,8,64,8,OriginalGrateMechanicsOracle.ForgeDirection.values()[face.getIndex()]);
    check(b.isSideSolid(state,world,POS,face)==want,"original side support "+face+" "+label);
    check(state.isSideSolid(world,POS,face)==want,"native state delegates side support "+label);
    check(b.getBlockFaceShape(world,state,POS,face)==(want?BlockFaceShape.SOLID:BlockFaceShape.UNDEFINED),"native face shape "+label);
   }
  }
  exact(grates[0].getY(9),-.125D,"metadata nine is negative, not 1.125");
  exact(grates[1].getBoundingBox(grates[1].getStateFromMeta(0),world,POS).maxY,(double)(.125F-.001F),"wide epsilon retains float rounding, not double subtraction");
  check(grates[1].getBoundingBox(grates[1].getStateFromMeta(0),world,POS).maxY!=.125D-.001D,"test distinguishes float epsilon rounding");
  System.out.println("PASS native grate all 32 states: exact float bounds, getY(9), actual chunk/no-tile, own item drops and every side/face support.");
 }
 static void placement(){
  NativeWorld world=new NativeWorld(false);Player player=new Player(world);List<Float> hits=new ArrayList<>();
  hits.add(0F);hits.add(1F);for(int i=1;i<8;i++){float at=i*.125F;hits.add(Math.nextDown(at));hits.add(at);hits.add(Math.nextUp(at));}
  for(RecordingGrate b:grates)for(EnumFacing side:EnumFacing.values())for(float hitY:hits)for(int metadata=0;metadata<16;metadata++)for(EnumHand hand:EnumHand.values()){
   IBlockState got=b.getStateForPlacement(world,POS,side,.2F,hitY,.8F,metadata,player,hand);
   int want=OriginalGrateMechanicsOracle.placement(side.getIndex(),hitY,metadata);
   check(b.getMetaFromState(got)==want,"original side/hitY placement: "+side+" hitY="+hitY+" inputMeta="+metadata+" hand="+hand);
  }
  double[][] lowerCases={null,FULL,lower(.75),lower(.95-1e-7),lower(.95),lower(.95+1e-7),lower(1.25)};
  double[][] upperCases={null,FULL,upper(.25),upper(.05-1e-7),upper(.05),upper(.05+1e-7),upper(-.25)};
  for(RecordingGrate b:grates)for(int meta=0;meta<16;meta++)for(boolean sneak:new boolean[]{false,true})for(boolean air:new boolean[]{false,true})for(int n=0;n<lowerCases.length;n++){
   double[] lower=lowerCases[n],upper=upperCases[n];prepare(world,b,meta,lower,upper,air);player.setSneaking(sneak);
   b.onBlockPlacedBy(world,POS,b.getStateFromMeta(meta),player,new ItemStack(Item.getItemFromBlock(b)));
   OriginalGrateMechanicsOracle.World want=OriginalGrateMechanicsOracle.placed(b.isWide(),meta,sneak,lower,upper,air);
   check(b.getMetaFromState(world.getBlockState(POS))==want.meta&&world.writes==want.writes,"original sneak resulting metadata/writes meta="+meta+" case="+n+" air="+air+" sneak="+sneak);
   check(world.flags==want.flags,"original sneak flags");if(world.writes>0)check(POS.equals(world.lastWrite)&&world.written.getBlock()==b,"only owner state changed");
   OriginalGrateMechanicsOracle.Block queried=meta==0?want.below:want.above;
   check(support.airQueries.size()==queried.airCalls,"original air query count");
   if(queried.airCalls>0){check(support.airQueries.get(0).equals(new BlockPos(queried.airX,queried.airY,queried.airZ)),"exact original isAir queried position");check(support.airStates.get(0).getBlock()==support,"isAir invoked on support owner state");}
  }
  // The original below-support query deliberately passes the ABOVE position.
  for(RecordingGrate b:grates){
   prepare(world,b,0,null,FULL,false);world.put(POS.up(),Blocks.STONE.getDefaultState());support.airOnlyAt=POS.up();player.setSneaking(true);b.onBlockPlacedBy(world,POS,b.getStateFromMeta(0),player,ItemStack.EMPTY);check(b.getMetaFromState(world.getBlockState(POS))==0&&support.airQueries.equals(Arrays.asList(POS.up())),"below owner reports air only at ABOVE, so no conversion despite actual above stone");
   prepare(world,b,0,null,FULL,false);world.put(POS.up(),Blocks.AIR.getDefaultState());support.airOnlyAt=POS.down();b.onBlockPlacedBy(world,POS,b.getStateFromMeta(0),player,ItemStack.EMPTY);check(b.getMetaFromState(world.getBlockState(POS))==9,"below owner reports air only at BELOW, original ABOVE query permits conversion despite actual above air");
   // Method reads current world metadata rather than a stale placement argument.
   prepare(world,b,0,null,FULL,false);b.onBlockPlacedBy(world,POS,b.getStateFromMeta(7),player,ItemStack.EMPTY);check(b.getMetaFromState(world.getBlockState(POS))==9,"sneak placement reads actual world metadata");
  }
  System.out.println("PASS original placement oracle: six sides, every eighth-height boundary, both hands/input metadata, sneak null/air/threshold branches and odd below-owner ABOVE isAir query.");
 }
 static void neighborRemoval(){
  double[][] lowers={null,FULL,lower(.75),lower(.95-1e-7),lower(.95),lower(.95+1e-7),lower(1.25)};
  double[][] uppers={null,FULL,upper(.25),upper(.05-1e-7),upper(.05),upper(.05+1e-7),upper(-.25)};
  for(boolean remote:new boolean[]{false,true}){
   NativeWorld world=new NativeWorld(remote);
   for(RecordingGrate b:grates)for(int meta=0;meta<16;meta++)for(int n=0;n<lowers.length;n++){
    prepare(world,b,meta,lowers[n],uppers[n],false);IBlockState incoming=b.getStateFromMeta(meta==0?7:0);
    b.neighborChanged(incoming,world,POS,support,POS.down());
    OriginalGrateMechanicsOracle.World want=OriginalGrateMechanicsOracle.neighbor(b.isWide(),meta,remote,lowers[n],uppers[n]);
    check((world.getBlockState(POS).getBlock()==Blocks.AIR)==want.removed,"original neighbor removed meta="+meta+" remote="+remote+" case="+n);
    check(world.writes==want.writes&&b.drops==want.drops,"original neighbor write/drop count");check(b.dropMeta==want.dropMeta&&b.fortune==want.fortune,"original drop metadata-zero/fortune-zero");
    check(support.airQueries.isEmpty(),"neighbor collision check does not test isAir");
    check(support.collisionQueries.size()==(!remote&&(meta==8||meta==9)?1:0),"client/ordinary metadata skips collision query");
    if(want.removed){check(b.chance==1F&&POS.equals(b.dropPos)&&b.dropped.getBlock()==b,"actual final native dropBlockAsItem dispatch to chance override");check(world.events.equals(Arrays.asList("drop","air")),"drop occurs before native block-to-air update");check(world.flags==3&&POS.equals(world.lastWrite),"actual native setBlockToAir flags and position");}
    else check(world.getBlockState(POS).equals(b.getStateFromMeta(meta)),"neighbor preserves existing state");
   }
  }
  System.out.println("PASS actual server/client neighbor callbacks: every metadata, null and .05 threshold cases, stale-argument protection, native final drop dispatch meta=0/fortune=0 and drop-before-air ordering.");
 }
 static Entity entity(NativeWorld world,int kind,double y){
  Entity e=kind==0?null:kind==1?new EntityItem(world,8.25,y,8.75,new ItemStack(Items.STICK)):kind==2?new EntityXPOrb(world,8.25,y,8.75,3):new Player(world);
  if(e!=null){e.setPosition(8.25,y,8.75);e.motionX=.75;e.motionY=.5;e.motionZ=-.625;}
  return e;
 }
 static void collisions(){
  NativeWorld world=new NativeWorld(false);
  for(RecordingGrate b:grates)for(int meta=0;meta<16;meta++)for(int kind=0;kind<4;kind++)for(boolean actual:new boolean[]{false,true}){
   double threshold=64+meta*.125D+.375D;
   for(double y:new double[]{threshold-1,Math.nextDown(threshold),threshold,Math.nextUp(threshold),threshold+1})for(AxisAlignedBB query:new AxisAlignedBB[]{ALL,MISS}){
    prepare(world,b,meta,FULL,FULL,false);Entity e=entity(world,kind,y);AxisAlignedBB before=e==null?null:e.getEntityBoundingBox();List<AxisAlignedBB> got=new ArrayList<>();
    b.addCollisionBoxToList(b.getStateFromMeta(meta),world,POS,query,got,e,actual);
    OriginalGrateMechanicsOracle.Collision want=OriginalGrateMechanicsOracle.collision(b.isWide(),meta,kind,y,values(query));
    check(got.size()==want.boxes.size(),"original collision box count wide="+b.isWide()+" meta="+meta+" kind="+kind+" y="+y);
    for(int j=0;j<got.size();j++)box(got.get(j),want.boxes.get(j).bounds(),"original world-space collision bounds");
    if(e!=null){exact(e.posX,want.entity.posX,"entity posX");exact(e.posY,want.entity.posY,"entity posY");exact(e.posZ,want.entity.posZ,"entity posZ");exact(e.motionX,want.entity.motionX,"entity motionX");exact(e.motionY,want.entity.motionY,"entity motionY");exact(e.motionZ,want.entity.motionZ,"entity motionZ");
     boolean moved=b.isWide()&&(kind==1||kind==2)&&y<threshold;AxisAlignedBB expected=moved?before.offset(0,-.125,0):before;box(e.getEntityBoundingBox(),values(expected),"native setPosition updates actual entity AABB");
    }
    check(world.writes==0,"collision entity handling does not alter block state");
   }
  }
  // Strong regression: metadata nine uses 9*.125, not negative getY(9).
  RecordingGrate wide=grates[1];prepare(world,wide,9,FULL,FULL,false);Entity item=entity(world,1,65);List<AxisAlignedBB> out=new ArrayList<>();wide.addCollisionBoxToList(wide.getStateFromMeta(0),world,POS,MISS,out,item,false);exact(item.posY,64.875,"meta-nine world metadata threshold, not getY or stale passed state");check(out.isEmpty(),"wide eligible entity passes through even outside query bounds");
  // Query touching boundaries is not intersecting, as in the native base method.
  for(RecordingGrate b:grates)for(int m=0;m<16;m++){
   prepare(world,b,m,FULL,FULL,false);AxisAlignedBB own=b.getCollisionBoundingBox(b.getStateFromMeta(m),world,POS).offset(POS);
   for(AxisAlignedBB q:new AxisAlignedBB[]{new AxisAlignedBB(own.maxX,own.minY,own.minZ,own.maxX+1,own.maxY,own.maxZ),new AxisAlignedBB(own.minX,own.maxY,own.minZ,own.maxX,own.maxY+1,own.maxZ),new AxisAlignedBB(own.minX,own.minY,own.maxZ,own.maxX,own.maxY,own.maxZ+1)}){List<AxisAlignedBB> boxes=new ArrayList<>();b.addCollisionBoxToList(b.getStateFromMeta(m),world,POS,q,boxes,null,false);check(boxes.isEmpty(),"normal native collision strict touching boundary");}
  }
  System.out.println("PASS actual native EntityItem/EntityXPOrb/Player collisions, all metadata and strict height thresholds, negative getY(9) distinction, motion/position/AABB updates and ordinary query intersections.");
 }
 static final class Translator implements ITranslate {
  String key;Object[] args;int calls;
  public String resolveKey(String key,Object... args){this.key=key;this.args=args;calls++;return "resolved:"+key;}
  public String[] resolveKeyArray(String key,Object... args){throw new AssertionError("unexpected array translation");}
  public List<String> autoBreakWithParagraphs(Object font,String text,int width){throw new AssertionError("unexpected line wrapping");}
  public List<String> autoBreak(Object font,String text,int width){throw new AssertionError("unexpected line wrapping");}
 }
 static void tooltip(){
  ITranslate prior=MainRegistry.proxy.translator;Translator translator=new Translator();MainRegistry.proxy.translator=translator;
  try{for(RecordingGrate b:grates)for(boolean extended:new boolean[]{false,true})for(int meta=0;meta<16;meta++){
   List<String> got=new ArrayList<>();got.add("existing");int before=translator.calls;b.addInformation(new ItemStack(Item.getItemFromBlock(b),1,meta),null,got,extended);String key=b.getTranslationKey()+".desc";
   check(got.equals(Arrays.asList("existing","resolved:"+key)),"tooltip preserves earlier lines and appends translated desc");check(translator.calls==before+1&&key.equals(translator.key)&&translator.args.length==0,"actual production I18nUtil dispatches exact .desc key without arguments");
   OriginalGrateMechanicsOracle.Owner owner=OriginalGrateMechanicsOracle.owner(b.isWide());owner.name=b.getTranslationKey();List<String> want=new ArrayList<>();want.add("existing");owner.addInformation(new OriginalGrateMechanicsOracle.ItemStack(),null,want,extended);check(got.equals(want),"verbatim original tooltip output");
  }}finally{MainRegistry.proxy.translator=prior;}
  System.out.println("PASS tooltip through actual production ITooltipProvider/I18nUtil, exact narrow/wide description keys, input-line preservation and original callback oracle.");
 }
 public static void main(String[] args)throws Exception{
  setup();boundsAndSupport();placement();neighborRemoval();collisions();tooltip();
  System.out.println("PASS grate mechanics: "+checks+" assertions. Actual native Minecraft/Forge owner, World/Chunk and entities; separately hash-pinned original callback recorder. Sole NTM collaborator: MainRegistry translation-service holder. Not full game/mod acceptance.");
 }
}
