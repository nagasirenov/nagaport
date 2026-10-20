import java.io.*;
import java.lang.reflect.*;
import java.nio.*;
import java.util.*;
import com.hbm.blocks.network.*;
import com.hbm.inventory.fluid.*;
import com.hbm.lib.ForgeDirection;
import com.hbm.main.BoxDuctModelHandler;
import com.hbm.render.block.RenderBoxDuct;
import com.hbm.render.block.nativeblock.*;
import com.hbm.render.block.nativeblock.BoxDuctMaterials.Kind;
import com.hbm.render.tileentity.*;
import com.hbm.tileentity.network.*;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.client.renderer.tileentity.*;
import net.minecraft.client.renderer.vertex.*;
import net.minecraft.client.resources.*;
import net.minecraft.init.*;
import net.minecraft.item.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.profiler.Profiler;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;
import net.minecraft.world.chunk.*;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.storage.*;
import net.minecraftforge.client.model.*;
import net.minecraftforge.client.model.pipeline.*;
import net.minecraftforge.common.property.*;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

/** Genuine native registry/state/model/lighter/dispatcher buffers. No actual GL/game launch. */
public final class BoxDuctModelNativeTest {
 static long checks;static final BlockPos POS=new BlockPos(8,64,8);
 static final String[] NAMES={"tile.fluid_duct_box","tile.fluid_duct_exhaust","tile.red_cable_box"};
 static Block[] OWNERS;
 static final Map<String,TextureAtlasSprite> SPRITES=new LinkedHashMap<>();static sun.misc.Unsafe unsafe;
 static File resources;static SimpleReloadableResourceManager manager;
 static void check(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
 static Field field(Class<?> type,String name)throws Exception{Field f=type.getDeclaredField(name);f.setAccessible(true);return f;}
 static Object get(Object target,Class<?> type,String name)throws Exception{return field(type,name).get(target);}
 static void set(Object target,Class<?> type,String name,Object value)throws Exception{field(type,name).set(target,value);}
 interface Action{void run()throws Exception;}
 static void rejects(Action action,String reason)throws Exception{boolean bad=false;try{action.run();}catch(Exception expected){bad=true;}check(bad,reason);}
 static final class Sprite extends TextureAtlasSprite {Sprite(String name,int index){super(name);setIconWidth(16);setIconHeight(16);initSprite(2048,256,(index%100)*16,32+16*(index/100),false);}}
 static TextureAtlasSprite sprite(String name){return SPRITES.computeIfAbsent(name,k->new Sprite(k,SPRITES.size()));}
 static BoxDuctMaterials materials(int family){return new BoxDuctMaterials(Kind.values()[family],r->sprite(r.toString()));}
 static IBlockState state(int family,int meta,int mask,int color){IExtendedBlockState s=(IExtendedBlockState)OWNERS[family].getStateFromMeta(meta);s=s.withProperty(ConnectionState.CONNECTIONS,mask);return family==2?s:s.withProperty(FluidConnectionState.FLUID_COLOR,color);}
 static int[] metas(int family){return family==0?new int[]{0,1,2,3,4,5,6,7,8,9,10,11,12,13,14}:family==1?new int[]{0,3,6,9,12}:new int[]{0,1,2,3,4};}
 static final class Endpoint extends TileEntity implements api.hbm.energymk2.IEnergyConnectorMK2,api.hbm.fluidmk2.IFluidConnectorMK2 { }
 static class NativeWorld extends World {
  final Map<Long,Chunk> chunks=new HashMap<>();int mask;final Endpoint endpoint=new Endpoint();
  NativeWorld(){super(new SaveHandlerMP(),new WorldInfo(new NBTTagCompound()),new WorldProviderSurface(),new Profiler(),false);provider.setWorld(this);chunkProvider=createChunkProvider();for(int x=-1;x<=1;x++)for(int z=-1;z<=1;z++){Chunk c=new Chunk(this,x,z);c.markLoaded(true);chunks.put(key(x,z),c);}}
  static long key(int x,int z){return ((long)x<<32)^(z&0xffffffffL);}
  protected IChunkProvider createChunkProvider(){return new IChunkProvider(){public Chunk getLoadedChunk(int x,int z){return chunks.get(key(x,z));}public Chunk provideChunk(int x,int z){return getLoadedChunk(x,z);}public boolean tick(){return false;}public String makeString(){return "box duct native probe";}public boolean isChunkGeneratedAt(int x,int z){return getLoadedChunk(x,z)!=null;}};}
  protected boolean isChunkLoaded(int x,int z,boolean allowEmpty){return chunks.containsKey(key(x,z));}
  public boolean checkLight(BlockPos p){return true;}
  public int getCombinedLight(BlockPos p,int emission){int x=p.getX()-POS.getX(),y=p.getY()-POS.getY(),z=p.getZ()-POS.getZ();return Math.floorMod(11+3*x+5*y+7*z,16)*16<<16|Math.floorMod(7+5*x+3*y+z,16)*16;}
  public TileEntity getTileEntity(BlockPos p){for(EnumFacing side:EnumFacing.values())if(p.equals(POS.offset(side))&&(mask&new int[]{4,8,1,2,16,32}[side.getIndex()])!=0)return endpoint;return super.getTileEntity(p);}
  public boolean spawnEntity(net.minecraft.entity.Entity e){return true;}
 }
 static void setup()throws Exception {
  Bootstrap.register();net.minecraft.launchwrapper.Launch.blackboard.put("fml.deobfuscatedEnvironment",Boolean.TRUE);OWNERS=new Block[]{new FluidDuctBox(Material.IRON),new FluidDuctBoxExhaust(Material.IRON),new PowerCableBox(Material.IRON)};net.minecraftforge.fml.common.ModMetadata md=new net.minecraftforge.fml.common.ModMetadata();md.modId="hbm";md.name="Box duct native test";md.version="test";net.minecraftforge.fml.common.DummyModContainer container=new net.minecraftforge.fml.common.DummyModContainer(md);net.minecraftforge.fml.common.Loader.instance().setupTestHarness(container);net.minecraftforge.fml.common.Loader.instance().setActiveModContainer(container);
  unsafe=(sun.misc.Unsafe)get(null,sun.misc.Unsafe.class,"theUnsafe");Minecraft mc=(Minecraft)unsafe.allocateInstance(Minecraft.class);set(null,Minecraft.class,"instance",mc);manager=new SimpleReloadableResourceManager(new net.minecraft.client.resources.data.MetadataSerializer());set(mc,Minecraft.class,"resourceManager",manager);
  Map<Block,Item> map=(Map<Block,Item>)get(null,Item.class,"BLOCK_TO_ITEM");
  for(int n=0;n<3;n++){Block b=OWNERS[n];b.setRegistryName("hbm",NAMES[n]);ForgeRegistries.BLOCKS.register(b);ItemBlock item=new ItemBlock(b);item.setHasSubtypes(true);item.setRegistryName(b.getRegistryName());ForgeRegistries.ITEMS.register(item);map.put(b,item);}
  BoxDuctModelHandler.registerModels(new net.minecraftforge.client.event.ModelRegistryEvent());
 }
 static void modelProperties()throws Exception {
  for(int family=0;family<3;family++)for(int metadata:metas(family)) {
   BoxDuctBakedModel model=new BoxDuctBakedModel(materials(family),metadata,false),item=new BoxDuctBakedModel(materials(family),metadata,true);
   check(model.isAmbientOcclusion()&&model.isGui3d()&&!model.isBuiltInRenderer(),"original standard cubes retain AO and inventory3d");
   List<BakedQuad> inventory=item.getQuads(null,null,0);check(inventory.size()==6,"one original item cuboid");
   for(BakedQuad quad:inventory){check(!quad.hasTintIndex()&&quad.shouldApplyDiffuseLighting(),"original item directional normals");for(int v=0;v<4;v++)check(quad.getVertexData()[v*7+3]==-1,"inventory untinted neutral color");}
   for(int mask=0;mask<64;mask++)for(int color:new int[]{0xffffff,0x000000,0x317bdf}) {
    IBlockState state=state(family,metadata,mask,color);List<BakedQuad> quads=model.getQuads(state,null,0);check(quads==model.getQuads(state,null,21),"immutable cached identity independent random seed");
    for(EnumFacing side:EnumFacing.values())check(model.getQuads(state,side,0).isEmpty(),"original all six faces unculled");
    int rgba=family==2?-1:0xff000000|(color&255)<<16|(color&0xff00)|(color>>>16&255);
    for(BakedQuad quad:quads){check(quad.shouldApplyDiffuseLighting()&&!quad.hasTintIndex(),"world standard cube lighting and already-resolved color");for(int v=0;v<4;v++)check(quad.getVertexData()[v*7+3]==rgba,"exact original final RGB channels");}
    rejects(()->quads.clear(),"model lists immutable");
   }
   check(((Map<?,?>)get(model,BoxDuctBakedModel.class,"cache")).size()<=128,"bounded color and connectivity cache");
   for(ItemCameraTransforms.TransformType type:ItemCameraTransforms.TransformType.values())check(item.handlePerspective(type).getLeft()==item,"native item perspective keeps identity");
   javax.vecmath.Matrix4f fixed=item.handlePerspective(ItemCameraTransforms.TransformType.FIXED).getRight();fixed.setZero();check(item.handlePerspective(ItemCameraTransforms.TransformType.FIXED).getRight().m33==1,"fixed transform returned defensively");
  }
  System.out.println("PASS native model all valid metadata/masks/RGB, immutable bounded cache, complete six item contexts and fixed transform isolation");
 }
 static final class Capture extends BoxDuctBreakingRenderer<TileEntity> {BufferBuilder buffer;TextureMap texture;int draws;@Override protected void draw(BufferBuilder b,TextureMap atlas){buffer=b;texture=atlas;draws++;}}
 static BufferBuilder ordinary(BlockModelRenderer renderer,NativeWorld world,IBlockState state,List<BakedQuad> quads,boolean ambient){
  BufferBuilder b=new BufferBuilder(65536);b.begin(7,DefaultVertexFormats.BLOCK);b.setTranslation(1.25-POS.getX(),2.5-POS.getY(),3.75-POS.getZ());IBakedModel model=new AtlasObjBakedModel(quads,quads.get(0).getSprite());if(ambient)renderer.renderModelSmooth(world,model,state,POS,b,true,0);else renderer.renderModelFlat(world,model,state,POS,b,true,0);b.finishDrawing();return b;
 }
 static void breaking()throws Exception {
  Minecraft mc=Minecraft.getMinecraft();net.minecraft.client.settings.GameSettings settings=(net.minecraft.client.settings.GameSettings)unsafe.allocateInstance(net.minecraft.client.settings.GameSettings.class);mc.gameSettings=settings;
  TextureMap atlas=new TextureMap("textures"){@Override public TextureAtlasSprite getAtlasSprite(String n){throw new AssertionError("Original box duct never uses destroy texture: "+n);}};set(mc,Minecraft.class,"textureMapBlocks",atlas);
  final IBakedModel[] current={null};BlockModelShapes shapes=new BlockModelShapes(new ModelManager(atlas)){@Override public IBakedModel getModelForState(IBlockState s){return current[0];}};
  BlockRendererDispatcher renderer=(BlockRendererDispatcher)unsafe.allocateInstance(BlockRendererDispatcher.class);set(renderer,BlockRendererDispatcher.class,"blockModelShapes",shapes);set(mc,Minecraft.class,"blockRenderDispatcher",renderer);
  TileEntityRendererDispatcher dispatcher=TileEntityRendererDispatcher.instance;
  boolean previous=net.minecraftforge.common.ForgeModContainer.forgeLightPipelineEnabled;
  try {for(int family=0;family<3;family++) {
   Block owner=OWNERS[family];NativeWorld world=new NativeWorld();world.setBlockState(POS,owner.getDefaultState(),2);TileEntity tile=world.getTileEntity(POS);check(owner.getDefaultState().hasCustomBreakingProgress()&&tile.canRenderBreaking(),"native RenderGlobal custom path+standard skip");check(dispatcher.getRenderer(tile) instanceof BoxDuctBreakingRenderer,"actual loader bound existing tile class");
   Capture capture=new Capture();TileEntitySpecialRenderer<?> saved=dispatcher.renderers.put(tile.getClass(),capture);
   try {
    dispatcher.render(tile,1.25,2.5,3.75,.5F,-1,1);dispatcher.render(tile,1.25,2.5,3.75,.5F,10,1);check(capture.draws==0,"ordinary/invalid stage no model or GL work");
    for(int metadata:metas(family)) {
     world.setBlockState(POS,owner.getStateFromMeta(metadata),2);tile=world.getTileEntity(POS);if(tile instanceof TileEntityPipeBaseNT)((TileEntityPipeBaseNT)tile).type=new FluidType(7,0x1949a1,"Test");
     current[0]=new BoxDuctBakedModel(materials(family),metadata,false);
     for(int mask:new int[]{0,1,3,5,10,16,32,48,21,63}) {
      world.mask=mask;IBlockState state=world.getBlockState(POS),extended=owner.getExtendedState(state,world,POS);check(((IExtendedBlockState)extended).getValue(ConnectionState.CONNECTIONS)==mask,"real owner endpoint mask");List<BakedQuad> quads=current[0].getQuads(extended,null,0);
      for(boolean forge:new boolean[]{false,true})for(boolean ambient:new boolean[]{false,true}) {
       net.minecraftforge.common.ForgeModContainer.forgeLightPipelineEnabled=forge;settings.ambientOcclusion=ambient?2:0;BlockModelRenderer lighter=new ForgeBlockModelRenderer(new BlockColors());set(renderer,BlockRendererDispatcher.class,"blockModelRenderer",lighter);BufferBuilder expected=ordinary(lighter,world,state,quads,ambient);ByteBuffer e=expected.getByteBuffer();
       for(int stage:new int[]{0,5,9}) {
        dispatcher.render(tile,1.25,2.5,3.75,.5F,stage,1);BufferBuilder got=capture.buffer;ByteBuffer g=got.getByteBuffer();check(capture.texture==atlas&&got.getVertexCount()==expected.getVertexCount(),"actual dispatcher emits original icon cube count");check(got.getVertexFormat().getColorOffset()<0&&got.getVertexFormat().getSize()==24,"old noColor upload");
        for(int v=0;v<got.getVertexCount();v++){for(int c=0;c<3;c++)check(g.getInt(v*24+c*4)==e.getInt(v*28+c*4),"ordinary/damage exact native camera-space positions");check(g.getInt(v*24+12)==e.getInt(v*28+16)&&g.getInt(v*24+16)==e.getInt(v*28+20),"original icon UV retained; never crack");check(g.getInt(v*24+20)==e.getInt(v*28+24),"native flat/smooth packed lighting preserved through color removal");}
       }
      }
     }
    }
    int count=capture.draws;for(Block unowned:new Block[]{new FluidDuctBox(Material.IRON).setRegistryName("foreign","tile.fluid_duct_box"),new FluidDuctBox(Material.IRON){}.setRegistryName("hbm","tile.fluid_duct_box"),new PowerCableBox(Material.IRON).setRegistryName("hbm","other_cable"),Blocks.STONE}) {
     world.setBlockState(POS,unowned.getDefaultState(),2);check(!unowned.getDefaultState().hasCustomBreakingProgress()&&!tile.canRenderBreaking()&&!capture.owns(tile,world.getBlockState(POS)),"unowned or subclass not claimed");dispatcher.render(tile,1,2,3,.5F,4,1);check(capture.draws==count,"unowned render no-op");
    }
    check(!capture.owns(new TileEntity(){},owner.getDefaultState()),"unrelated tile class not claimed");
   }finally{dispatcher.renderers.put(tile.getClass(),saved);}
  }} finally{net.minecraftforge.common.ForgeModContainer.forgeLightPipelineEnabled=previous;}
  System.out.println("PASS actual native damage dispatcher, three exact owner/tile pairs, valid metadata x10 shape masks x2 AO x2 light pipelines x3 stages; original materials/UVs and native light retained, noColor output and non-owned no-op");
 }
 static void lightingGate()throws Exception {
  NativeWorld world=new NativeWorld();world.setBlockState(POS,OWNERS[0].getStateFromMeta(0),2);IBlockState state=world.getBlockState(POS);BoxDuctBakedModel model=new BoxDuctBakedModel(materials(0),0,false);List<BakedQuad> quads=model.getQuads(OWNERS[0].getExtendedState(state,world,POS),null,0);
  boolean previous=net.minecraftforge.common.ForgeModContainer.forgeLightPipelineEnabled;
  try {for(boolean forge:new boolean[]{false,true}) {
   net.minecraftforge.common.ForgeModContainer.forgeLightPipelineEnabled=forge;BufferBuilder buffer=ordinary(new ForgeBlockModelRenderer(new BlockColors()),world,state,quads,false);int mismatch=0;float[] shades={.5F,1F,.8F,.8F,.6F,.6F};
   for(int q=0;q<quads.size();q++){int expected=(int)(255F*shades[quads.get(q).getFace().getIndex()]);for(int v=0;v<4;v++){int rgb=buffer.getByteBuffer().getInt((q*4+v)*28+12);if((rgb&255)!=expected||(rgb>>>8&255)!=expected||(rgb>>>16&255)!=expected)mismatch++;}}
   System.out.println("OPEN original flat diffuse-byte quantization: native Forge pipeline="+forge+" differs at "+mismatch+"/"+buffer.getVertexCount()+" vertices from original RenderBlocks setColorOpaque_F truncation. This is not an approved visual loss; full old/native AO solver equality remains unproven.");
   check(forge?mismatch>0:mismatch==0,"explicit original/native flat shade gate remains visible");
  }}finally{net.minecraftforge.common.ForgeModContainer.forgeLightPipelineEnabled=previous;}
 }
 static void registry(File jar)throws Exception {
  ICustomModelLoader loader=(ICustomModelLoader)get(null,BoxDuctModelHandler.class,"LOADER");Map<?,?> routes=(Map<?,?>)get(null,ModelLoader.class,"customModels");check(routes.size()==48,"exact three item owners x16 persisted nibble routes");
  for(IResourcePack pack:new IResourcePack[]{new FolderResourcePack(resources),new FileResourcePack(jar)}) {
   manager.reloadResources(Collections.singletonList(pack));loader.onResourceManagerReload(manager);
   for(int family=0;family<3;family++) {
    com.google.gson.JsonObject json;try(Reader reader=new InputStreamReader(pack.getInputStream(new ResourceLocation("hbm","blockstates/"+NAMES[family]+".json")),java.nio.charset.StandardCharsets.UTF_8)){json=new com.google.gson.JsonParser().parse(reader).getAsJsonObject().getAsJsonObject("variants");}
    for(int metadata=0;metadata<16;metadata++) {
     String world=json.getAsJsonObject("metadata="+metadata).get("model").getAsString();ResourceLocation loc=new ResourceLocation(world);check(loader.accepts(loc)&&loader.accepts(new ResourceLocation("hbm","models/block/"+loc.getPath())),"actual directory/JAR blockstate route");IModel raw=loader.loadModel(loc);check(new HashSet<>(raw.getTextures()).equals(new HashSet<>(BoxDuctMaterials.textures(Kind.values()[family]))),"all exact original textures declared");for(ResourceLocation tex:raw.getTextures())try(InputStream in=pack.getInputStream(new ResourceLocation(tex.getNamespace(),"textures/"+tex.getPath()+".png"))){check(javax.imageio.ImageIO.read(in)!=null,"actual original sprite decodes");}
     IBakedModel baked=raw.bake(raw.getDefaultState(),DefaultVertexFormats.BLOCK,r->sprite(r.toString()));check(baked instanceof BoxDuctBakedModel,"whole native model baked");Item item=Item.getItemFromBlock(OWNERS[family]);ModelResourceLocation itemRoute=(ModelResourceLocation)routes.get(org.apache.commons.lang3.tuple.Pair.of(item.delegate,metadata));check(itemRoute!=null&&itemRoute.getPath().equals(loc.getPath().replace("/world","/item")),"deferred actual ItemBlock route");
    }
   }
  }
  TextureMap atlas=new TextureMap("textures");ModelManager initial=new ModelManager(atlas);BlockModelShapes initialShapes=new BlockModelShapes(initial);new ModelLoader(manager,atlas,initialShapes);ModelLoader.onRegisterAllBlocks(initialShapes);
  net.minecraft.util.registry.RegistrySimple<ModelResourceLocation,IBakedModel> baked=new net.minecraft.util.registry.RegistrySimple<>();Map<IBlockState,ModelResourceLocation> stateRoutes=new HashMap<>();ModelLoaderRegistry.clearModelCache(manager);
  for(int family=0;family<3;family++) {
   Block block=OWNERS[family];Map<IBlockState,ModelResourceLocation> states=initialShapes.getBlockStateMapper().getVariants(block);check(states.size()==16,"native state mapper sees16 metadata");
   for(int metadata=0;metadata<16;metadata++) {
    IBlockState state=block.getStateFromMeta(metadata);ModelResourceLocation loc=states.get(state);check(loc.getVariant().equals("metadata="+metadata),"native property serialization");IModel descriptor=ModelLoaderRegistry.getModel(loc);check(descriptor!=ModelLoaderRegistry.getMissingModel(),"actual native blockstate JSON loader resolution");baked.putObject(loc,descriptor.bake(descriptor.getDefaultState(),DefaultVertexFormats.BLOCK,r->sprite(r.toString())));stateRoutes.put(state,loc);
    Item item=Item.getItemFromBlock(block);ModelResourceLocation itemId=(ModelResourceLocation)routes.get(org.apache.commons.lang3.tuple.Pair.of(item.delegate,metadata));IModel itemModel=ModelLoaderRegistry.getModel(itemId);check(itemModel!=ModelLoaderRegistry.getMissingModel(),"actual native item loader resolution");baked.putObject(itemId,itemModel.bake(itemModel.getDefaultState(),DefaultVertexFormats.ITEM,r->sprite(r.toString())));
   }
  }
  ModelManager mm=(ModelManager)unsafe.allocateInstance(ModelManager.class);set(mm,ModelManager.class,"modelRegistry",baked);set(mm,ModelManager.class,"defaultModel",baked.getObject(stateRoutes.values().iterator().next()));ItemModelMesher mesher=new ItemModelMesher(mm);ModelLoader.onRegisterItems(mesher);mesher.rebuildCache();for(Block block:OWNERS)for(int m=0;m<16;m++){Item item=Item.getItemFromBlock(block);check(mesher.getItemModel(new ItemStack(item,1,m))==baked.getObject((ModelResourceLocation)routes.get(org.apache.commons.lang3.tuple.Pair.of(item.delegate,m))),"native mesher exact metadata identity");}
  BlockModelShapes shapes=new BlockModelShapes(mm);ModelLoader.onRegisterAllBlocks(shapes);shapes.reloadModels();for(Map.Entry<IBlockState,ModelResourceLocation> entry:stateRoutes.entrySet())check(shapes.getModelForState(entry.getKey())==baked.getObject(entry.getValue()),"native shape binding exact state identity");
  ResourceLocation reloaded=new ResourceLocation("hbm","native_boxduct/tile.fluid_duct_box/2/world");IModel old=ModelLoaderRegistry.getModel(reloaded);manager.reloadResources(Collections.singletonList(new FolderResourcePack(resources)));ModelLoaderRegistry.clearModelCache(manager);IModel fresh=ModelLoaderRegistry.getModel(reloaded);check(old!=fresh,"reload publishes fresh model descriptor and cache");
  for(String bad:new String[]{"foreign:native_boxduct/tile.fluid_duct_box/0/world","hbm:native_boxduct/tile.other/0/world","hbm:native_boxduct/tile.fluid_duct_box/16/world","hbm:native_boxduct/tile.fluid_duct_box/0/other","hbm:native_boxduct/tile.red_cable_box/0/item/extra"}){ResourceLocation loc=new ResourceLocation(bad);check(!loader.accepts(loc),"unowned/malformed route rejected");rejects(()->loader.loadModel(loc),"unowned load rejected");}
  System.out.println("PASS exact48 state/item routes, genuine native ModelLoaderRegistry/ItemModelMesher/BlockModelShapes, directory+JAR textures and reload replacement");
 }
 public static void main(String[] args)throws Exception{resources=new File(args[0]);setup();modelProperties();breaking();lightingGate();registry(new File(args[1]));System.out.println("PASS box-duct native model/dispatcher "+checks+" assertions; CPU buffers, no GPU or whole network-tile mechanics claim");}
}
