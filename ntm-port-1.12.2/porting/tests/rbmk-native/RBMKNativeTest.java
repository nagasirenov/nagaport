import java.io.*;
import java.lang.reflect.*;
import java.nio.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;
import com.hbm.blocks.machine.rbmk.*;
import com.hbm.blocks.machine.rbmk.RBMKRenderState.*;
import com.hbm.main.RBMKModelHandler;
import com.hbm.render.block.nativeblock.*;
import com.hbm.render.mesh.*;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.client.renderer.vertex.*;
import net.minecraft.client.resources.*;
import net.minecraft.init.*;
import net.minecraft.item.*;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;
import net.minecraftforge.client.model.*;
import net.minecraftforge.client.model.pipeline.*;
import net.minecraftforge.common.property.*;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

/** Real native models/loader/state/BufferBuilder; reactor owners are explicit recording fixtures. */
public final class RBMKNativeTest {
 static long checks,vertices,standardVertices,objVertices;
 static final BlockPos POS=new BlockPos(8,64,8);
 static final Map<String,TextureAtlasSprite> sprites=new LinkedHashMap<>();
 static final Map<String,RBMKOwnerFixture> owners=new LinkedHashMap<>();
 static final Map<String,RBMKBakedModel> worldModels=new HashMap<>(),itemModels=new HashMap<>();
 static File resources;
 static sun.misc.Unsafe unsafe;
 static void check(boolean b,String label){checks++;if(!b)throw new AssertionError(label);}
 static Field field(Class<?> c,String n)throws Exception{Field f=c.getDeclaredField(n);f.setAccessible(true);return f;}
 interface Throwing {void run()throws Exception;}
 static void rejects(Throwing r,String label)throws Exception{boolean thrown=false;try{r.run();}catch(Exception e){thrown=true;}check(thrown,label);}
 static final class Sprite extends TextureAtlasSprite {
  Sprite(String name){super(name);setIconWidth(16);setIconHeight(16);int index=sprites.size();initSprite(4096,256,16*(index%128),32+16*(index/128),false);}
 }
 static TextureAtlasSprite sprite(String name){String canonical=name.equals("override")?"minecraft:blocks/destroy_stage_5":name.replace("hbm:rbmk/","hbm:blocks/rbmk/");return sprites.computeIfAbsent(canonical,Sprite::new);}
 static ObjMesh mesh(String name)throws Exception{File f=new File(resources,"assets/hbm/models/rbmk/"+name+".obj");try(Reader r=new InputStreamReader(new FileInputStream(f),StandardCharsets.UTF_8)){return ObjMeshParser.parse(r,name,name.equals("rbmk_element"));}}
 static void setup()throws Exception {
  Bootstrap.register();net.minecraftforge.fml.common.ModMetadata md=new net.minecraftforge.fml.common.ModMetadata();md.modId="hbm";md.name="RBMK native proof";md.version="test";net.minecraftforge.fml.common.DummyModContainer container=new net.minecraftforge.fml.common.DummyModContainer(md);net.minecraftforge.fml.common.Loader.instance().setupTestHarness(container);net.minecraftforge.fml.common.Loader.instance().setActiveModContainer(container);
  unsafe=(sun.misc.Unsafe)field(sun.misc.Unsafe.class,"theUnsafe").get(null);
  for(Profile p:RBMKRenderState.profiles()){
   RBMKOwnerFixture b=(RBMKOwnerFixture)Class.forName(p.ownerClass).newInstance();b.setRegistryName("hbm",p.name);ForgeRegistries.BLOCKS.register(b);owners.put(p.name,b);
   RBMKMaterials materials=new RBMKMaterials(p,r->sprite(r.toString()));
   worldModels.put(p.name,new RBMKBakedModel(materials,mesh("rbmk_element"),null,null,false));
   itemModels.put(p.name,new RBMKBakedModel(materials,mesh("rbmk_element"),mesh("rbmk_element_rods"),mesh("rbmk_rods"),true));
   for(ResourceLocation texture:RBMKMaterials.textures(p))check(new File(resources,"assets/"+texture.getNamespace()+"/textures/"+texture.getPath()+".png").isFile(),"actual original texture exists "+texture);
  }
 }
 static class LightingWorld implements IBlockAccess {
  final RBMKOwnerFixture block;final int meta,mask;final Integer core;
  final Map<BlockPos,IBlockState> overrides=new HashMap<>();
  LightingWorld(RBMKOwnerFixture b,int meta,Integer core,int mask){block=b;this.meta=meta;this.core=core;this.mask=mask;}
  public IBlockState getBlockState(BlockPos p){
   if(overrides.containsKey(p))return overrides.get(p);
   if(p.equals(POS.down(8)))return block.getStateFromMeta(core==null?0:core);
   for(EnumFacing side:EnumFacing.values())if(p.equals(POS.offset(side))||p.equals(POS.up().offset(side)))return (mask&1<<side.getIndex())!=0?Blocks.AIR.getDefaultState():Blocks.STONE.getDefaultState();
   return p.getX()<POS.getX()&&p.getZ()>=POS.getZ()?Blocks.STONE.getDefaultState():Blocks.AIR.getDefaultState();
  }
  public int getCombinedLight(BlockPos p,int emission){int x=p.getX()-POS.getX(),y=p.getY()-POS.getY(),z=p.getZ()-POS.getZ();return Math.floorMod(11+3*x+5*y+7*z,16)*16<<16|Math.floorMod(7+5*x+3*y+z,16)*16;}
  public TileEntity getTileEntity(BlockPos p){return null;}
  public boolean isAirBlock(BlockPos p){return getBlockState(p).getBlock()==Blocks.AIR;}
  public net.minecraft.world.biome.Biome getBiome(BlockPos p){return Biomes.PLAINS;}
  public int getStrongPower(BlockPos p,EnumFacing side){return 0;}
  public WorldType getWorldType(){return WorldType.DEFAULT;}
  public boolean isSideSolid(BlockPos p,EnumFacing side,boolean fallback){return getBlockState(p).isOpaqueCube();}
 }
 static IBlockState extended(RBMKOwnerFixture block,int meta,Integer core,int mask){block.core=core;IBlockState state=block.getStateFromMeta(meta);return block.getExtendedState(state,new LightingWorld(block,meta,core,mask),POS);}
 static boolean match(BakedQuad quad,OriginalRBMKOracle.Face face){
  if(quad.getSprite()!=sprite(face.texture))return false;
  int[] data=quad.getVertexData();int stride=quad.getFormat().getIntegerSize();int uv=quad.getFormat().getUvOffsetById(0)/4;
  for(int shift=0;shift<4;shift++){
   boolean same=true;
   for(int v=0;v<4;v++){
    OriginalRBMKOracle.VertexRecord e=face.vertices[(v+shift)%4];double[] expected=e.xyzuv();
    for(int c=0;c<5;c++){int k=c<3?c:uv+c-3;float got=Float.intBitsToFloat(data[v*stride+k]);if(Math.abs(got-expected[c])>0.0000003){same=false;break;}}
    if(!same)break;
   }
   if(same)return true;
  }
  return false;
 }
 static void geometry(List<BakedQuad> actual,OriginalRBMKOracle.Layout expected,String context,boolean color){
  check(actual.size()==expected.faces.size(),context+" count "+actual.size()+" != "+expected.faces.size());
  List<OriginalRBMKOracle.Face> remaining=new ArrayList<>(expected.faces);
  for(BakedQuad q:actual){
   OriginalRBMKOracle.Face found=null;
   for(OriginalRBMKOracle.Face f:remaining)if(match(q,f)){found=f;break;}
   check(found!=null,context+" original geometry/material/winding match "+q.getSprite().getIconName());remaining.remove(found);
   int[] data=q.getVertexData();int stride=q.getFormat().getIntegerSize();
   for(int v=0;v<4;v++){
    vertices++;if(found.standard)standardVertices++;else objVertices++;
    if(color&&!found.standard&&found.vertices[v].hasColor){int want=found.vertices[v].color;int got=data[v*stride+q.getFormat().getColorOffset()/4];check((got&255)==(want>>>16&255)&&(got>>>8&255)==(want>>>8&255)&&(got>>>16&255)==(want&255),context+" original explicit RGB bytes");}
   }
  }
  check(remaining.isEmpty(),context+" complete original primitive multiset");
 }
 static void originalGeometry(File legacy)throws Exception {
  try(OriginalRBMKOracle.LegacyFaces faces=new OriginalRBMKOracle.LegacyFaces(legacy)){
   int[] masks={0,1,2,4,8,16,32,63,21,42};
   for(Profile p:RBMKRenderState.profiles()){
    String name=p.name.substring(5);RBMKOwnerFixture block=owners.get(p.name);RBMKBakedModel item=itemModels.get(p.name),world=worldModels.get(p.name);
    check(item.isAmbientOcclusion()&&item.isGui3d()&&!item.isBuiltInRenderer(),"native model flags");
    OriginalRBMKOracle.Layout original=OriginalRBMKOracle.layout(name,0,true,null,false,63,faces,RBMKNativeTest::sprite);
    geometry(item.getQuads(null,null,0),original,name+" item",true);
    for(int meta=0;meta<16;meta++)for(Integer core:new Integer[]{null,12,15,13})for(int mask:masks){
     IBlockState state=extended(block,meta,core,mask);Snapshot snap=RBMKRenderState.snapshot(state);
     check(snap.metadata==meta&&snap.pos.equals(POS)&&snap.state==((IExtendedBlockState)state).getClean(),"actual immutable extended-state hook");
     OriginalRBMKOracle.Layout want=OriginalRBMKOracle.layout(name,meta,false,core,false,mask,faces,RBMKNativeTest::sprite);
     geometry(world.getQuads(state,null,0),want,name+" meta"+meta+" core"+core+" mask"+mask,false);
     for(EnumFacing side:EnumFacing.values())check(world.getQuads(state,side,0).isEmpty(),"snapshot already owns culling");
     if(mask==63&&meta==6){OriginalRBMKOracle.Layout damage=OriginalRBMKOracle.layout(name,meta,false,core,true,mask,faces,RBMKNativeTest::sprite);geometry(world.getBreakingQuads(state,sprite("override")),damage,name+" callable original damage",false);}
    }
    for(int meta=0;meta<16;meta++)check(item.getQuads(null,null,meta).size()==original.faces.size(),"item metadata does not alter original four segments");
    check(block.getRenderLayer()==BlockRenderLayer.CUTOUT_MIPPED&&block.getRenderType(block.getDefaultState())==EnumBlockRenderType.MODEL,"native extracted layer/render hooks");
   }
  }
 }
 static BufferBuilder render(List<BakedQuad> quads,Snapshot snapshot,boolean smooth)throws Exception{
  BufferBuilder out=new BufferBuilder(65536);out.begin(7,DefaultVertexFormats.BLOCK);
  VertexBufferConsumer consumer=new VertexBufferConsumer(out);consumer.setOffset(POS);
  VertexLighterFlat lighter=smooth?new VertexLighterSmoothAo(new BlockColors()):new VertexLighterFlat(new BlockColors());lighter.setParent(consumer);lighter.setWorld(snapshot.world);lighter.setState(snapshot.state);lighter.setBlockPos(POS);lighter.updateBlockInfo();
  Object info=field(VertexLighterFlat.class,"blockInfo").get(lighter);
  for(BakedQuad q:quads){q.pipe(lighter);check(field(QuadGatheringTransformer.class,"parent").get(lighter)==consumer,"quad restores native lighter parent");check(((BlockInfo)info).getWorld()==snapshot.world&&((BlockInfo)info).getState()==snapshot.state&&((BlockInfo)info).getBlockPos().equals(POS),"quad restores native lighter context");}
  out.finishDrawing();return out;
 }
 static void lightAndDamage()throws Exception {
  for(Profile p:RBMKRenderState.profiles()){
   RBMKOwnerFixture block=owners.get(p.name);RBMKBakedModel model=worldModels.get(p.name);
   IBlockState state=extended(block,6,13,63);Snapshot snapshot=RBMKRenderState.snapshot(state);List<BakedQuad> quads=model.getQuads(state,null,0);
   for(boolean smooth:new boolean[]{false,true}){
    BufferBuilder output=render(quads,snapshot,smooth);check(output.getVertexCount()==quads.size()*4,"native real BufferBuilder count");ByteBuffer bytes=output.getByteBuffer();
    for(int i=0;i<quads.size();i++)if(quads.get(i).getSprite().getIconName().endsWith("_inner")||p.family==Family.ROD&&quads.get(i).getSprite().getIconName().endsWith("_top")&&i<quads.size()-6){
     for(int v=0;v<4;v++){check(bytes.getInt((i*4+v)*28+24)==snapshot.packedLight,"OBJ exact block-position light even with native smooth AO");check(bytes.getInt((i*4+v)*28+12)==quads.get(i).getVertexData()[v*7+3],"OBJ exact original shade, no AO multiplication");}
    }
   }
   IBakedModel damage=net.minecraftforge.client.ForgeHooksClient.getDamageModel(model,sprite("override"),state,snapshot.world,POS);
   List<BakedQuad> actual=damage.getQuads(state,null,0),want=model.getBreakingQuads(state,sprite("override"));int materialDiff=0;
   check(actual.size()==want.size(),"default damage preserves primitive count");
   for(int i=0;i<actual.size();i++)if(actual.get(i).getSprite()!=want.get(i).getSprite())materialDiff++;
   check(p.family==Family.ROD?materialDiff>0:materialDiff==0,"explicit native default-damage material gate");
   for(boolean smooth:new boolean[]{false,true}){
    BufferBuilder a=render(actual,snapshot,smooth),w=render(want,snapshot,smooth);int lightDiff=0,colorDiff=0;
    for(int v=0;v<a.getVertexCount();v++){if(a.getByteBuffer().getInt(v*28+24)!=w.getByteBuffer().getInt(v*28+24))lightDiff++;if(a.getByteBuffer().getInt(v*28+12)!=w.getByteBuffer().getInt(v*28+12))colorDiff++;}
    System.out.println("OPEN DEFAULT DAMAGE "+p.name+" smooth="+smooth+" materialQuads="+materialDiff+" lightVertices="+lightDiff+" colorVertices="+colorDiff+" / "+a.getVertexCount()+"; callable original damage is NOT a registered dispatcher");
   }
  }
 }
 static void failureRestoration()throws Exception {
  RBMKOwnerFixture block=owners.get("tile.rbmk_rod");IBlockState state=extended(block,6,13,63);Snapshot snapshot=RBMKRenderState.snapshot(state);
  List<BakedQuad> all=worldModels.get("tile.rbmk_rod").getQuads(state,null,0);
  List<BakedQuad> chosen=Arrays.asList(all.get(4),all.get(all.size()-1));
  for(BakedQuad quad:chosen)for(boolean smooth:new boolean[]{false,true})for(boolean fail:new boolean[]{false,true}){
   BufferBuilder out=new BufferBuilder(256);out.begin(7,DefaultVertexFormats.BLOCK);
   IVertexConsumer parent=new VertexTransformer(new VertexBufferConsumer(out)) {
    @Override public void put(int element,float...values){if(fail&&element==0)throw new IllegalStateException("intentional RBMK sink failure");super.put(element,values);}
   };
   VertexLighterFlat lighter=smooth?new VertexLighterSmoothAo(new BlockColors()):new VertexLighterFlat(new BlockColors());
   IBlockState previousState=Blocks.DIRT.getDefaultState();BlockPos previousPos=POS.add(3,2,-1);IBlockAccess previousWorld=new LightingWorld(block,0,null,21);
   lighter.setParent(parent);lighter.setWorld(previousWorld);lighter.setState(previousState);lighter.setBlockPos(previousPos);lighter.updateBlockInfo();
   boolean thrown=false;try{quad.pipe(lighter);}catch(IllegalStateException e){check(e.getMessage().equals("intentional RBMK sink failure"),"original sink error preserved");thrown=true;}
   check(thrown==fail,"only requested failure occurs");BlockInfo info=(BlockInfo)field(VertexLighterFlat.class,"blockInfo").get(lighter);
   check(field(QuadGatheringTransformer.class,"parent").get(lighter)==parent&&info.getWorld()==previousWorld&&info.getState()==previousState&&info.getBlockPos().equals(previousPos),"exact parent/context restored on success and failure");
  }
  System.out.println("PASS native Flat/SmoothAo shifted cube and fixed OBJ context restored on output and intentional sink failure");
 }
 static void registry(File jar)throws Exception {
  SimpleReloadableResourceManager manager=new SimpleReloadableResourceManager(new net.minecraft.client.resources.data.MetadataSerializer());
  net.minecraft.client.Minecraft mc=(net.minecraft.client.Minecraft)unsafe.allocateInstance(net.minecraft.client.Minecraft.class);field(net.minecraft.client.Minecraft.class,"instance").set(null,mc);field(net.minecraft.client.Minecraft.class,"resourceManager").set(mc,manager);
  if(net.minecraft.launchwrapper.Launch.blackboard==null)net.minecraft.launchwrapper.Launch.blackboard=new HashMap<>();net.minecraft.launchwrapper.Launch.blackboard.put("fml.deobfuscatedEnvironment",Boolean.TRUE);
  rejects(()->RBMKModelHandler.registerModels(new net.minecraftforge.client.event.ModelRegistryEvent()),"missing real ItemBlock registration rejected");
  Map<Block,Item> map=(Map<Block,Item>)field(Item.class,"BLOCK_TO_ITEM").get(null);
  for(RBMKOwnerFixture b:owners.values()){ItemBlock item=new ItemBlock(b);item.setRegistryName(b.getRegistryName());ForgeRegistries.ITEMS.register(item);map.put(b,item);}
  RBMKModelHandler.registerModels(new net.minecraftforge.client.event.ModelRegistryEvent());
  ICustomModelLoader loader=(ICustomModelLoader)field(RBMKModelHandler.class,"LOADER").get(null);
  for(IResourcePack pack:new IResourcePack[]{new FolderResourcePack(resources),new FileResourcePack(jar)}){
   manager.reloadResources(Collections.singletonList(pack));loader.onResourceManagerReload(manager);
   for(Profile p:RBMKRenderState.profiles())for(boolean item:new boolean[]{false,true}){
    ResourceLocation route=new ResourceLocation("hbm","native_rbmk/"+p.name+"/"+(item?"item":"world"));check(loader.accepts(route),"exact loader route");check(loader.accepts(new ResourceLocation("hbm","models/block/"+route.getPath())),"native normalized route");
    IModel unbaked=loader.loadModel(route);check(new HashSet<>(unbaked.getTextures()).equals(new HashSet<>(RBMKMaterials.textures(p))),"exact texture dependency set");IBakedModel baked=unbaked.bake(unbaked.getDefaultState(),DefaultVertexFormats.BLOCK,r->sprite(r.toString()));
    IBlockState state=item?null:extended(owners.get(p.name),6,15,63);check(baked.getQuads(state,null,0).size()==(item?itemModels:worldModels).get(p.name).getQuads(state,null,0).size(),"actual reload model geometry");
   }
  }
  // Exercise actual Forge state mapper, global model-loader cache, native item mesher and block shapes.
  TextureMap atlas=new TextureMap("textures");ModelManager initial=new ModelManager(atlas);BlockModelShapes initialShapes=new BlockModelShapes(initial);new ModelLoader(manager,atlas,initialShapes);
  net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(RBMKModelHandler.class);net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.ModelRegistryEvent());net.minecraftforge.common.MinecraftForge.EVENT_BUS.unregister(RBMKModelHandler.class);
  ModelLoaderRegistry.clearModelCache(manager);
  Map<?,?> custom=(Map<?,?>)field(ModelLoader.class,"customModels").get(null);check(custom.size()==18*16,"only eighteen owners times sixteen item metadata registered");
  ModelLoader.onRegisterAllBlocks(initialShapes);net.minecraft.util.registry.RegistrySimple<ModelResourceLocation,IBakedModel> bakedRegistry=new net.minecraft.util.registry.RegistrySimple<>();Map<IBlockState,ModelResourceLocation> stateRoutes=new HashMap<>();
  for(Profile p:RBMKRenderState.profiles()){
   RBMKOwnerFixture block=owners.get(p.name);Map<IBlockState,ModelResourceLocation> variants=initialShapes.getBlockStateMapper().getVariants(block);check(variants.size()==16,"sixteen real listed states mapped");
   ModelResourceLocation worldId=variants.get(block.getStateFromMeta(0));check(worldId.getPath().equals("native_rbmk/"+p.name+"/world")&&worldId.getVariant().equals("normal"),"direct custom state-map route");
   IModel descriptor=ModelLoaderRegistry.getModel(worldId);check(descriptor!=ModelLoaderRegistry.getMissingModel(),"actual ModelLoaderRegistry world resolution");IBakedModel baked=descriptor.bake(descriptor.getDefaultState(),DefaultVertexFormats.BLOCK,r->sprite(r.toString()));bakedRegistry.putObject(worldId,baked);
   for(int m=0;m<16;m++){
    IBlockState listed=block.getStateFromMeta(m);check(variants.get(listed).equals(worldId),"listed metadata remains dynamic, exact same world route");stateRoutes.put(listed,worldId);
    Item item=Item.getItemFromBlock(block);ModelResourceLocation itemId=(ModelResourceLocation)custom.get(org.apache.commons.lang3.tuple.Pair.of(item.delegate,m));check(itemId!=null&&itemId.getPath().equals("native_rbmk/"+p.name+"/item")&&itemId.getVariant().equals("inventory"),"all exact deferred item routes");
    if(bakedRegistry.getObject(itemId)==null){IModel itemDescriptor=ModelLoaderRegistry.getModel(itemId);check(itemDescriptor!=ModelLoaderRegistry.getMissingModel(),"actual ModelLoaderRegistry item resolution");bakedRegistry.putObject(itemId,itemDescriptor.bake(itemDescriptor.getDefaultState(),DefaultVertexFormats.ITEM,r->sprite(r.toString())));}
   }
  }
  ModelManager modelManager=(ModelManager)unsafe.allocateInstance(ModelManager.class);field(ModelManager.class,"modelRegistry").set(modelManager,bakedRegistry);field(ModelManager.class,"defaultModel").set(modelManager,bakedRegistry.getObject(stateRoutes.values().iterator().next()));
  ItemModelMesher mesher=new ItemModelMesher(modelManager);ModelLoader.onRegisterItems(mesher);mesher.rebuildCache();for(RBMKOwnerFixture block:owners.values())for(int m=0;m<16;m++){Item item=Item.getItemFromBlock(block);ModelResourceLocation id=(ModelResourceLocation)custom.get(org.apache.commons.lang3.tuple.Pair.of(item.delegate,m));check(mesher.getItemModel(new ItemStack(item,1,m))==bakedRegistry.getObject(id),"real native item model mesher identity");}
  BlockModelShapes shapes=new BlockModelShapes(modelManager);ModelLoader.onRegisterAllBlocks(shapes);shapes.reloadModels();for(Map.Entry<IBlockState,ModelResourceLocation> route:stateRoutes.entrySet())check(shapes.getModelForState(route.getKey())==bakedRegistry.getObject(route.getValue()),"real native block model shapes identity");
  ResourceLocation reloadRoute=new ResourceLocation("hbm","native_rbmk/tile.rbmk_rod/item");IModel oldDescriptor=ModelLoaderRegistry.getModel(reloadRoute);Object oldMesh=((Map<?,?>)field(loader.getClass(),"meshes").get(loader)).get("rbmk_element");manager.reloadResources(Collections.singletonList(new FileResourcePack(jar)));ModelLoaderRegistry.clearModelCache(manager);IModel newDescriptor=ModelLoaderRegistry.getModel(reloadRoute);Object newMesh=((Map<?,?>)field(loader.getClass(),"meshes").get(loader)).get("rbmk_element");check(newDescriptor!=oldDescriptor&&newMesh!=oldMesh,"resource reload replaces descriptor and mesh publication");
  check(((Map<?,?>)field(ModelLoader.class,"customModels").get(null)).size()==18*16,"reload does not broaden registered owners");
  System.out.println("PASS actual custom mapper, 288 block states, 288 item routes, ModelLoaderRegistry, native ItemModelMesher/BlockModelShapes and mesh reload");
  for(String route:new String[]{"other:native_rbmk/tile.rbmk_rod/item","hbm:native_rbmk/tile.rbmk_console/item","hbm:native_rbmk/tile.rbmk_rod/item/extra","hbm:native_rbmk/tile.rbmk_rod/bad"}){ResourceLocation loc=new ResourceLocation(route);check(!loader.accepts(loc),"reject non-owned route");rejects(()->loader.loadModel(loc),"load also rejects non-owned route");}
  check(RBMKRenderState.profile(new RBMKOwnerFixture().setRegistryName("hbm","tile.rbmk_rod"))==null,"registry name alone cannot capture wrong class");
  check(RBMKRenderState.profile(new RBMKRod().setRegistryName("other","tile.rbmk_rod"))==null,"foreign namespace untouched");
  check(RBMKRenderState.profile(new RBMKRod(){}.setRegistryName("hbm","tile.rbmk_rod"))==null,"subclasses not implicitly owned");
 }
 public static void main(String[] args)throws Exception {resources=new File(args[0]);setup();originalGeometry(new File(args[2]));lightAndDamage();failureRestoration();registry(new File(args[1]));System.out.println("PASS RBMK native "+checks+" assertions; "+vertices+" original vertex comparisons (standard="+standardVertices+", other="+objVertices+"). Complete models/loader + extracted owner render hooks only; no whole reactor/client/GL/server claim.");}
}
