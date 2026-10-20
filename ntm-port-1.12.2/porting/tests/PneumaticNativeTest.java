import java.util.*;
import com.hbm.blocks.network.*;
import com.hbm.render.block.RenderPneumoTube;
import com.hbm.render.block.nativeblock.*;
import com.hbm.main.PneumaticModelHandler;
import com.hbm.tileentity.network.TileEntityPneumoTube;
import com.hbm.lib.*;
import net.minecraft.block.state.*;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.init.Bootstrap;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.client.model.*;
import net.minecraftforge.common.property.IExtendedBlockState;

public final class PneumaticNativeTest {
 static int checks,cases;static void check(boolean b,String why){checks++;if(!b)throw new AssertionError(why);}
 static class Sprite extends TextureAtlasSprite {Sprite(String s,int i){super(s);setIconWidth(16);setIconHeight(16);initSprite(256,256,16*i,16,false);}}
 static TextureAtlasSprite[] materials=new TextureAtlasSprite[5];
 static List<String> expected(int mask,int air,int in,int out,boolean item){List<String> result=new ArrayList<>();for(LegacyPneumaticOracle.Face f:LegacyPneumaticOracle.faces(mask,air,in,out,item)){
  double[] b=f.bounds.clone();int axis=f.side<2?1:f.side<4?2:0;boolean positive=(f.side&1)!=0;b[positive?axis:axis+3]=b[positive?axis+3:axis];result.add(f.side+":"+f.texture+":"+Arrays.toString(b));
 }Collections.sort(result);return result;}
 static List<String> actual(List<BakedQuad> quads){List<String> result=new ArrayList<>();for(BakedQuad q:quads){double[] b={2,2,2,-1,-1,-1};int[] data=q.getVertexData();int stride=q.getFormat().getIntegerSize();for(int v=0;v<4;v++)for(int ax=0;ax<3;ax++){double value=Float.intBitsToFloat(data[v*stride+ax]);b[ax]=Math.min(b[ax],value);b[ax+3]=Math.max(b[ax+3],value);}result.add(q.getFace().getIndex()+":"+q.getSprite().getIconName()+":"+Arrays.toString(b));check(q.getTintIndex()==-1,"no tint injection");}Collections.sort(result);return result;}
 static void geometry(){
  for(int mask=0;mask<64;mask++)for(int in=0;in<7;in++)for(int out=0;out<7;out++){
   if(in==out&&in!=6)continue;
   // Tube adjacency and collars can overlap after a neighbor change; retain original rendering.
   int air=in==6?0:63&~PneumaticModelState.bit(in)&~PneumaticModelState.bit(out)&~mask;
   PneumaticModelState snapshot=new PneumaticModelState(mask,air,in,out);
   List<BakedQuad> got=RenderPneumoTube.bake(snapshot,false,materials);
   check(actual(got).equals(expected(mask,air,in,out,false)),"pristine topology/collars "+mask+"/"+air+"/"+in+"/"+out);cases++;
  }
  check(actual(RenderPneumoTube.bake(PneumaticModelState.EMPTY,true,materials)).equals(expected(0,0,6,6,true)),"pristine inventory bounds/materials");
 }
 static class Access implements IBlockAccess {
  final Map<BlockPos,TileEntity> tiles=new HashMap<>();final PneumoTube owner;Access(PneumoTube owner){this.owner=owner;}
  public TileEntity getTileEntity(BlockPos p){return tiles.get(p);}public int getCombinedLight(BlockPos p,int n){return 0;}
  public IBlockState getBlockState(BlockPos p){return owner.getDefaultState();}public boolean isAirBlock(BlockPos p){return false;}
  public Biome getBiome(BlockPos p){return net.minecraft.init.Biomes.PLAINS;}public int getStrongPower(BlockPos p,EnumFacing s){return 0;}
  public WorldType getWorldType(){return WorldType.DEFAULT;}public boolean isSideSolid(BlockPos p,EnumFacing s,boolean d){return false;}
 }
 static void owner(){
  PneumoTube owner=new PneumoTube();BlockPos pos=new BlockPos(8,64,8);Access access=new Access(owner);
  check(owner.createNewTileEntity(null,0) instanceof TileEntityPneumoTube,"actual tile factory");
  check(owner.getRenderType(owner.getDefaultState())==EnumBlockRenderType.MODEL,"native render entry");
  check(!owner.isFullCube(owner.getDefaultState())&&!owner.isOpaqueCube(owner.getDefaultState()),"non-full nonopaque");
  TileEntityPneumoTube tile=new TileEntityPneumoTube();access.tiles.put(pos,tile);
  PneumaticBakedModel model=new PneumaticBakedModel(false,materials);
  for(int mask=0;mask<64;mask++){
   for(EnumFacing face:EnumFacing.values()){access.tiles.remove(pos.offset(face));if((mask&PneumaticModelState.bit(face.getIndex()))!=0)access.tiles.put(pos.offset(face),new TileEntityPneumoTube());}
   IBlockState extended=owner.getExtendedState(owner.getDefaultState(),access,pos);
   PneumaticModelState snapshot=((IExtendedBlockState)extended).getValue(PneumaticModelState.MODEL);
   check(snapshot.tubes==mask,"actual neighbor snapshot "+mask);
   check(actual(model.getQuads(extended,null,0)).equals(expected(mask,0,6,6,false)),"owner extended-state to baked-model path");
   check(owner.getBoundingBox(owner.getDefaultState(),access,pos).equals(snapshot.bounds()),"native selection bounds");
  }
  access.tiles.clear();access.tiles.put(pos,tile);tile.insertionDir=ForgeDirection.UP;tile.ejectionDir=ForgeDirection.EAST;
  Library.AIR_PORTS.add(pos.up());Library.AIR_PORTS.add(pos.east());Library.AIR_PORTS.add(pos.north());
  PneumaticModelState snapshot=owner.snapshot(access,pos);check(snapshot.air==1,"compressor excludes insertion and ejection sides");
  access.tiles.put(pos.north(),new TileEntityPneumoTube());check(owner.snapshot(access,pos).air==0,"tube neighbor excludes gas connector");
  tile.insertionDir=ForgeDirection.UNKNOWN;check(owner.snapshot(access,pos).air==0,"endpoint-only does not subscribe to air");Library.AIR_PORTS.clear();
  check(model.getQuads(null,EnumFacing.UP,0).isEmpty(),"general quads only");
 }
 static final class NativeWorld extends World {
  final Map<BlockPos,TileEntity> tiles=new HashMap<>();final PneumoTube owner;int updates;
  NativeWorld(PneumoTube owner,boolean remote){super(new net.minecraft.world.storage.SaveHandlerMP(),new net.minecraft.world.storage.WorldInfo(new net.minecraft.nbt.NBTTagCompound()),new WorldProviderSurface(),new net.minecraft.profiler.Profiler(),remote);this.owner=owner;provider.setWorld(this);}
  protected net.minecraft.world.chunk.IChunkProvider createChunkProvider(){return null;}
  protected boolean isChunkLoaded(int x,int z,boolean allowEmpty){return true;}
  @Override public TileEntity getTileEntity(BlockPos p){return tiles.get(p);}
  @Override public IBlockState getBlockState(BlockPos p){return owner.getDefaultState();}
  @Override public void notifyBlockUpdate(BlockPos p,IBlockState before,IBlockState after,int flags){check(flags==3,"screw sync flags");updates++;}
 }
 static final class Player extends net.minecraft.entity.player.EntityPlayer {
  boolean sneaking;
  Player(World world){super(world,new com.mojang.authlib.GameProfile(new UUID(1,2),"pneumatic-probe"));}
  public boolean isSpectator(){return false;}public boolean isCreative(){return false;}public boolean isSneaking(){return sneaking;}
 }
 static void collisionsAndTool(){
  PneumoTube owner=new PneumoTube();NativeWorld world=new NativeWorld(owner,false);BlockPos pos=new BlockPos(8,64,8);TileEntityPneumoTube tile=new TileEntityPneumoTube();world.tiles.put(pos,tile);
  AxisAlignedBB query=new AxisAlignedBB(7,63,7,10,66,10);
  for(int mask=0;mask<64;mask++){
   for(EnumFacing face:EnumFacing.values()){world.tiles.remove(pos.offset(face));if((mask&PneumaticModelState.bit(face.getIndex()))!=0)world.tiles.put(pos.offset(face),new TileEntityPneumoTube());}
   List<AxisAlignedBB> boxes=new ArrayList<>();owner.addCollisionBoxToList(owner.getDefaultState(),world,pos,query,boxes,null,false);
   check(boxes.size()==1+Integer.bitCount(mask),"actual native collision arm count");
   check(boxes.get(0).equals(new AxisAlignedBB(8.3125,64.3125,8.3125,8.6875,64.6875,8.6875)),"translated collision core");
   for(EnumFacing face:EnumFacing.values())if((mask&PneumaticModelState.bit(face.getIndex()))!=0)check(boxes.contains(PneumaticModelState.arm(face,.3125,.6875,.6875,1).offset(pos)),"native collision exact arm");
  }
  world.tiles.clear();world.tiles.put(pos,tile);tile.insertionDir=ForgeDirection.UP;tile.ejectionDir=ForgeDirection.EAST;
  List<AxisAlignedBB> boxes=new ArrayList<>();owner.addCollisionBoxToList(owner.getDefaultState(),world,pos,query,boxes,null,false);check(boxes.size()==1,"original collar-only collision omits arms");
  check(owner.getBoundingBox(owner.getDefaultState(),world,pos).maxX==1&&owner.getBoundingBox(owner.getDefaultState(),world,pos).maxY==1,"collar-only selection still extends");
  boxes.clear();owner.addCollisionBoxToList(owner.getDefaultState(),world,pos,new AxisAlignedBB(8,64,8,8.1,64.1,8.1),boxes,null,false);check(boxes.isEmpty(),"intersection filtering");
  Player player=new Player(world);tile.insertionDir=ForgeDirection.UNKNOWN;tile.ejectionDir=ForgeDirection.NORTH;
  world.tiles.put(pos.down(),new TileEntityPneumoTube());world.tiles.put(pos.up(),new net.minecraft.tileentity.TileEntityChest());
  check(owner.onScrew(world,player,8,64,8,0,0,0,0,api.hbm.block.IToolable.ToolType.SCREWDRIVER),"screw accepted");
  check(tile.insertionDir==ForgeDirection.UP&&tile.ejectionDir==ForgeDirection.NORTH,"screw skips tube and selects inventory");check(world.updates==1,"screw notifies native world");
  world.tiles.remove(pos.up());check(owner.onScrew(world,player,8,64,8,0,0,0,0,api.hbm.block.IToolable.ToolType.SCREWDRIVER),"second screw");check(tile.insertionDir==ForgeDirection.UNKNOWN,"unknown disables inlet");
  player.sneaking=true;world.tiles.put(pos.east(),new net.minecraft.tileentity.TileEntityChest());owner.onScrew(world,player,8,64,8,0,0,0,0,api.hbm.block.IToolable.ToolType.SCREWDRIVER);check(tile.ejectionDir==ForgeDirection.EAST,"sneak rotates outlet only");
  check(!owner.onScrew(world,player,8,64,8,0,0,0,0,api.hbm.block.IToolable.ToolType.WRENCH),"wrong tool rejected");
  NativeWorld remote=new NativeWorld(owner,true);check(owner.onScrew(remote,player,8,64,8,0,0,0,0,api.hbm.block.IToolable.ToolType.SCREWDRIVER)&&remote.updates==0,"client screw acknowledges without mutation");
 }
 static void loader(){
  PneumaticModelHandler.Loader loader=new PneumaticModelHandler.Loader();
  for(String path:new String[]{"native_pneumatic/world","models/block/native_pneumatic/world","native_pneumatic/item"}){
   ResourceLocation location=new ResourceLocation("hbm",path);check(loader.accepts(location),"native normalized route");IModel unbaked=loader.loadModel(location);check(unbaked.getTextures().size()==5,"all original atlas textures declared");
   IBakedModel baked=unbaked.bake(unbaked.getDefaultState(),DefaultVertexFormats.ITEM,id->{for(TextureAtlasSprite s:materials)if(id.toString().equals(s.getIconName()))return s;throw new AssertionError(id);});
   check(baked.getParticleTexture()==materials[0],"body particle material");check(baked.getQuads(null,null,0).size()==6,"loader bake actual route");
   TextureAtlasSprite fresh=new Sprite("replacement",0);IBakedModel next=unbaked.bake(unbaked.getDefaultState(),DefaultVertexFormats.ITEM,id->fresh);check(next.getParticleTexture()==fresh,"reload rebake does not retain old atlas");
  }
  check(!loader.accepts(new ResourceLocation("other","native_pneumatic/world")),"foreign namespace rejected");
  check(!loader.accepts(new ResourceLocation("hbm","native_pneumatic/garbage")),"invalid model rejected");
 }
 static Object get(Object target,Class<?> type,String name)throws Exception{java.lang.reflect.Field field=type.getDeclaredField(name);field.setAccessible(true);return field.get(target);}
 static void set(Object target,Class<?> type,String name,Object value)throws Exception{java.lang.reflect.Field field=type.getDeclaredField(name);field.setAccessible(true);field.set(target,value);}
 static void registry()throws Exception{
  net.minecraftforge.fml.common.Mod.EventBusSubscriber ann=PneumaticModelHandler.class.getAnnotation(net.minecraftforge.fml.common.Mod.EventBusSubscriber.class);
  check(ann!=null&&ann.modid().equals("hbm")&&Arrays.equals(ann.value(),new net.minecraftforge.fml.relauncher.Side[]{net.minecraftforge.fml.relauncher.Side.CLIENT}),"client-only native registry subscription");
  PneumoTube owner=new PneumoTube();owner.setRegistryName("hbm","tile.pneumatic_tube");net.minecraftforge.fml.common.registry.ForgeRegistries.BLOCKS.register(owner);
  net.minecraft.item.ItemBlock item=new net.minecraft.item.ItemBlock(owner);item.setRegistryName(owner.getRegistryName());net.minecraftforge.fml.common.registry.ForgeRegistries.ITEMS.register(item);
  PneumoTube foreign=new PneumoTube();foreign.setRegistryName("foreign","tile.pneumatic_tube");net.minecraftforge.fml.common.registry.ForgeRegistries.BLOCKS.register(foreign);
  PneumoTube subclass=new PneumoTube(){};subclass.setRegistryName("hbm","external_pneumatic_subclass");net.minecraftforge.fml.common.registry.ForgeRegistries.BLOCKS.register(subclass);
  PneumoTube unspecified=new PneumoTube();unspecified.setRegistryName("hbm","unrelated_tube");net.minecraftforge.fml.common.registry.ForgeRegistries.BLOCKS.register(unspecified);
  net.minecraft.client.resources.SimpleReloadableResourceManager resources=new net.minecraft.client.resources.SimpleReloadableResourceManager(new net.minecraft.client.resources.data.MetadataSerializer());
  sun.misc.Unsafe unsafe=(sun.misc.Unsafe)get(null,sun.misc.Unsafe.class,"theUnsafe");net.minecraft.client.Minecraft mc=(net.minecraft.client.Minecraft)unsafe.allocateInstance(net.minecraft.client.Minecraft.class);set(null,net.minecraft.client.Minecraft.class,"instance",mc);set(mc,net.minecraft.client.Minecraft.class,"resourceManager",resources);
  net.minecraft.launchwrapper.Launch.blackboard=new HashMap<>();net.minecraft.launchwrapper.Launch.blackboard.put("fml.deobfuscatedEnvironment",Boolean.TRUE);
  net.minecraft.client.renderer.texture.TextureMap atlas=new net.minecraft.client.renderer.texture.TextureMap("textures");net.minecraft.client.renderer.block.model.ModelManager manager=new net.minecraft.client.renderer.block.model.ModelManager(atlas);
  new ModelLoader(resources,atlas,new net.minecraft.client.renderer.BlockModelShapes(manager));
  PneumaticModelHandler.registerModels(new net.minecraftforge.client.event.ModelRegistryEvent());
  Map<?,?> custom=(Map<?,?>)get(null,ModelLoader.class,"customModels");check(custom.size()==16,"native sixteen metadata item mappings");
  Map<?,?> states=(Map<?,?>)get(null,ModelLoader.class,"customStateMappers");net.minecraft.client.renderer.block.statemap.IStateMapper mapper=(net.minecraft.client.renderer.block.statemap.IStateMapper)states.get(owner.delegate);check(mapper!=null,"native custom world mapper installed");check(!states.containsKey(foreign.delegate)&&!states.containsKey(subclass.delegate)&&!states.containsKey(unspecified.delegate),"foreign, subclass and unspecified owners untouched without requiring ItemBlocks");
  Map<IBlockState,ModelResourceLocation> routes=mapper.putStateModelLocations(owner);check(routes.size()==1,"single exposed tube listed state");
  ModelResourceLocation route=routes.values().iterator().next();check(route.toString().equals("hbm:native_pneumatic/world#normal"),"world registry route exact");
  IModel loaded=ModelLoaderRegistry.getModel(route);check(loaded!=ModelLoaderRegistry.getMissingModel(),"actual native registry dispatches custom world loader");
  IModel before=loaded;ModelLoaderRegistry.clearModelCache(resources);IModel after=ModelLoaderRegistry.getModel(route);check(before!=after,"resource reload clears native unbaked route");
  PneumaticModelHandler.registerModels(new net.minecraftforge.client.event.ModelRegistryEvent());check(((Map<?,?>)get(null,ModelLoader.class,"customModels")).size()==16,"repeated registry event stable");
 }
 static void uv(){
  // Independent analytic checks for original straight axes and RenderBlocksNT's asymmetric N/E patch.
  for(int mask:new int[]{0,3,12,48})for(BakedQuad quad:RenderPneumoTube.bake(new PneumaticModelState(mask,0,6,6),false,materials)){
   int[] data=quad.getVertexData();int stride=quad.getFormat().getIntegerSize();int offset=quad.getFormat().getUvOffsetById(0)/4;
   for(int v=0;v<4;v++){
    double x=Float.intBitsToFloat(data[v*stride]),y=Float.intBitsToFloat(data[v*stride+1]),z=Float.intBitsToFloat(data[v*stride+2]);double u,w;
    switch(quad.getFace()){
    case DOWN:case UP:u=mask==3?1-z:x;w=mask==3?x:z;break;
    case NORTH:u=mask==12?1-y:1-x;w=mask==12?1-x:1-y;break;
    case SOUTH:u=mask==12?y:x;w=mask==12?x:1-y;break;
    case WEST:u=mask==12?y:z;w=mask==12?z:1-y;break;
    case EAST:u=mask==12?1-y:1-z;w=mask==12?1-z:1-y;break;
    default:throw new AssertionError();
    }
    check(Math.abs(Float.intBitsToFloat(data[v*stride+offset])-quad.getSprite().getInterpolatedU(u*16))<1E-6,"original straight/body U");
    check(Math.abs(Float.intBitsToFloat(data[v*stride+offset+1])-quad.getSprite().getInterpolatedV(w*16))<1E-6,"original straight/body V");
   }
  }
  for(EnumFacing side:new EnumFacing[]{EnumFacing.NORTH,EnumFacing.EAST}){
   BakedQuad q=PneumaticCuboidGeometry.face(materials[0],false,side,0,.125,.25,.0625,.625,.75,.8125,0);int[] data=q.getVertexData();int stride=q.getFormat().getIntegerSize(),uv=q.getFormat().getUvOffsetById(0)/4;
   for(int v=0;v<4;v++){double coord=Float.intBitsToFloat(data[v*stride+(side==EnumFacing.NORTH?0:2)]);check(Math.abs(Float.intBitsToFloat(data[v*stride+uv])-materials[0].getInterpolatedU((1-coord)*16))<1E-6,"RenderBlocksNT global mirror, not partial-bound reversal");}
  }
 }
 public static void main(String[] args)throws Exception{Bootstrap.register();for(int i=0;i<5;i++)materials[i]=new Sprite("hbm:blocks/pneumatic_tube"+new String[]{"","_in","_out","_connector","_straight"}[i],i);geometry();owner();collisionsAndTool();loader();uv();registry();System.out.println("PASS "+checks+" native assertions; "+cases+" verbatim-pristine topology/port cases; owner neighbor/extended-state and loader bake invoked. Mechanical collaborators are explicit doubles.");}
}
