import java.io.*;
import java.lang.reflect.*;
import java.nio.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import javax.imageio.ImageIO;
import com.mojang.authlib.GameProfile;
import com.hbm.render.block.*;
import com.hbm.render.block.nativeblock.*;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.*;
import net.minecraft.entity.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.*;
import net.minecraft.item.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.profiler.Profiler;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;
import net.minecraft.world.chunk.*;
import net.minecraft.world.storage.*;
import net.minecraftforge.client.model.*;
import net.minecraftforge.client.model.pipeline.*;
import net.minecraftforge.common.property.*;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import com.hbm.blocks.generic.BlockGrate;
import com.hbm.main.GrateModelHandler;
public final class GrateRenderTest {
 static long checks,vertices;static final BlockPos POS=new BlockPos(8,64,8);
 static final String[] NAMES={"steel_grate","steel_grate_wide"};static BlockGrate[] blocks;
 static sun.misc.Unsafe unsafe;static SimpleReloadableResourceManager resources;static File resourceRoot;
 static final Map<String,TextureAtlasSprite> sprites=new HashMap<>();
 static void check(boolean b,String s){checks++;if(!b)throw new AssertionError(s);}
 static Field field(Class<?> c,String n)throws Exception{Field f=c.getDeclaredField(n);f.setAccessible(true);return f;}
 static Object get(Object o,Class<?> c,String n)throws Exception{return field(c,n).get(o);}
 static void set(Object o,Class<?> c,String n,Object v)throws Exception{field(c,n).set(o,v);}
 interface Throwing {void run()throws Exception;}
 static void rejects(Throwing r,String s)throws Exception{boolean b=false;try{r.run();}catch(Exception e){b=true;}check(b,s);}
 static final class Sprite extends TextureAtlasSprite {
  Sprite(String name)throws Exception{super(name);File path=new File(resourceRoot,"assets/"+name.replace(":","/textures/")+".png");java.awt.image.BufferedImage image=path.isFile()?ImageIO.read(path):new java.awt.image.BufferedImage(16,16,java.awt.image.BufferedImage.TYPE_INT_ARGB);int w=image.getWidth(),h=image.getHeight();setIconWidth(w);setIconHeight(h);setFramesTextureData(Collections.singletonList(new int[][]{image.getRGB(0,0,w,h,null,0,w)}));initSprite(512,128,64,32,false);}
 }
 static TextureAtlasSprite sprite(String name){try{TextureAtlasSprite s=sprites.get(name);if(s==null){s=new Sprite(name);sprites.put(name,s);}return s;}catch(Exception e){throw new RuntimeException(e);}}
 static TextureAtlasSprite top(int owner){return sprite("hbm:blocks/"+(owner==0?"grate_top":"grate_wide_top"));}
 static TextureAtlasSprite side(){return sprite("hbm:blocks/grate_side");}
 static void setup()throws Exception{
  Bootstrap.register();net.minecraftforge.fml.common.ModMetadata md=new net.minecraftforge.fml.common.ModMetadata();md.modId="hbm";md.name="Grate native proof";md.version="probe";net.minecraftforge.fml.common.DummyModContainer c=new net.minecraftforge.fml.common.DummyModContainer(md);net.minecraftforge.fml.common.Loader.instance().setupTestHarness(c);net.minecraftforge.fml.common.Loader.instance().setActiveModContainer(c);
  blocks=new BlockGrate[]{new BlockGrate(Material.IRON,false),new BlockGrate(Material.IRON,true)};
  for(int i=0;i<2;i++)ForgeRegistries.BLOCKS.register(blocks[i].setRegistryName("hbm","tile."+NAMES[i]));
  unsafe=(sun.misc.Unsafe)field(sun.misc.Unsafe.class,"theUnsafe").get(null);resources=new SimpleReloadableResourceManager(new net.minecraft.client.resources.data.MetadataSerializer());net.minecraft.client.Minecraft mc=(net.minecraft.client.Minecraft)unsafe.allocateInstance(net.minecraft.client.Minecraft.class);set(null,net.minecraft.client.Minecraft.class,"instance",mc);set(mc,net.minecraft.client.Minecraft.class,"resourceManager",resources);if(net.minecraft.launchwrapper.Launch.blackboard==null)net.minecraft.launchwrapper.Launch.blackboard=new HashMap<>();net.minecraft.launchwrapper.Launch.blackboard.put("fml.deobfuscatedEnvironment",Boolean.TRUE);
  rejects(()->GrateModelHandler.registerModels(new net.minecraftforge.client.event.ModelRegistryEvent()),"missing deferred ItemBlock must fail, not bind AIR");Map<Block,Item> map=(Map<Block,Item>)get(null,Item.class,"BLOCK_TO_ITEM");
  for(Block b:blocks){ItemBlock i=new ItemBlock(b);i.setHasSubtypes(true);i.setRegistryName(b.getRegistryName());ForgeRegistries.ITEMS.register(i);map.put(b,i);check(Item.getItemFromBlock(b)==i,"exact deferred ItemBlock identity");}
 }
 static class LightingWorld implements IBlockAccess {
  final IBlockState own;final boolean asymmetric;final Map<BlockPos,IBlockState> overrides=new HashMap<>();
  LightingWorld(IBlockState s,boolean a){own=s;asymmetric=a;}
  public IBlockState getBlockState(BlockPos p){if(overrides.containsKey(p))return overrides.get(p);if(p.equals(POS))return own;return asymmetric&&p.getX()<8&&p.getZ()>=8?Blocks.STONE.getDefaultState():Blocks.AIR.getDefaultState();}
  public int getCombinedLight(BlockPos p,int emission){if(!asymmetric)return 0x00f000f0;int x=p.getX()-8,y=p.getY()-64,z=p.getZ()-8;return Math.floorMod(11+3*x+5*y+7*z,16)*16<<16|Math.floorMod(7+5*x+3*y+z,16)*16;}
  public TileEntity getTileEntity(BlockPos p){return null;}public boolean isAirBlock(BlockPos p){return getBlockState(p).getBlock()==Blocks.AIR;}public net.minecraft.world.biome.Biome getBiome(BlockPos p){return Biomes.PLAINS;}public int getStrongPower(BlockPos p,EnumFacing f){return 0;}public WorldType getWorldType(){return WorldType.DEFAULT;}public boolean isSideSolid(BlockPos p,EnumFacing f,boolean fallback){return getBlockState(p).isOpaqueCube();}
 }
 static IBlockState extended(int i,int m){IBlockState s=blocks[i].getStateFromMeta(m);return blocks[i].getExtendedState(s,new LightingWorld(s,false),POS);}
 static List<BakedQuad> quads(IBakedModel model,IBlockState state){List<BakedQuad> q=new ArrayList<>(model.getQuads(state,null,0));for(EnumFacing side:EnumFacing.values())q.addAll(model.getQuads(state,side,0));return q;}
 static void originalGeometry(File jar)throws Exception{
  try(OriginalGrateRenderOracle.LegacyFaces legacy=new OriginalGrateRenderOracle.LegacyFaces(jar)){
   for(int i=0;i<2;i++)for(int m=0;m<16;m++){
    OriginalGrateRenderOracle.RenderBlocks want=OriginalGrateRenderOracle.layout(m,false);
    check(want.calls==1&&want.x==0&&want.z==0&&want.y==BlockGrate.renderShift(m),"original shifted render origin");
    check(OriginalGrateRenderOracle.layout(m,true).calls==0&&!new OriginalGrateRenderOracle.RenderGrate().shouldRender3DInInventory(7),"original flat item callback");
    IBakedModel model=new GrateBakedModel(m,top(i),side());List<BakedQuad> got=quads(model,extended(i,m));check(got.size()==6,"six original air-surrounded faces");
    for(int f=0;f<6;f++){
     BakedQuad q=got.get(f);check(q.getFace().getIndex()==f&&q.getSprite()==(f<2?top(i):side()),"original face material/order");double[][] expected=legacy.standardFace(want.bounds,f,q.getSprite());int[] data=q.getVertexData();int stride=q.getFormat().getIntegerSize();boolean found=false;
     for(int shift=0;shift<4;shift++){boolean equal=true;for(int v=0;v<4;v++)for(int a=0;a<5;a++){float actual=Float.intBitsToFloat(data[v*stride+(a<3?a:a+1)]);float target=(float)(expected[(v+shift)%4][a]+(a==1?want.y:0));if(Float.floatToRawIntBits(actual)!=Float.floatToRawIntBits(target))equal=false;}if(equal)found=true;}
     check(found,"actual original RenderBlocks XYZ/UV/winding owner="+i+" meta="+m+" face="+f);vertices+=4;
    }
   }
  }
  System.out.println("PASS original pristine RenderGrate/getY callbacks + actual original 1.7 RenderBlocks face bytecode: two owners, all 16 metadata, six faces, exact XYZ/UV/material/winding and flat item decision.");
 }
 static void culling()throws Exception{
  for(int i=0;i<2;i++)for(int m=0;m<16;m++)for(int mask=0;mask<64;mask++){
   IBlockState s=blocks[i].getStateFromMeta(m);LightingWorld world=new LightingWorld(s,false);BlockPos render=POS.add(0,BlockGrate.renderShift(m),0);
   for(EnumFacing f:EnumFacing.values())world.overrides.put(render.offset(f),(mask&(1<<f.getIndex()))==0?Blocks.AIR.getDefaultState():Blocks.STONE.getDefaultState());
   IBlockState e=blocks[i].getExtendedState(s,world,POS);int expected=0;float min=m==9?-.125F:m*.125F,max=min+.125F-(i==1?.001F:0);
   for(EnumFacing f:EnumFacing.values())if((f==EnumFacing.DOWN&&min>0)||(f==EnumFacing.UP&&max<1)||(mask&(1<<f.getIndex()))==0)expected|=1<<f.getIndex();
   check(BlockGrate.renderSnapshot(e).visible==expected,"original owner-bound culling at shifted position");List<BakedQuad> q=quads(new GrateBakedModel(m,top(i),side()),e);check(q.size()==Integer.bitCount(expected),"native culled face count");for(BakedQuad face:q)check((expected&(1<<face.getFace().getIndex()))!=0,"only visible original faces");world.overrides.clear();check(BlockGrate.renderSnapshot(e).visible==expected,"culling snapshot immutable");
  }
  System.out.println("PASS all 2048 owner/metadata/neighbor masks, normalized-origin culling with original unnormalized owner bounds, including wide epsilon and meta 8/9 behavior.");
 }
 static final class StaticModel implements IBakedModel {
  final List<BakedQuad> quads;StaticModel(List<BakedQuad> q){quads=q;}
  public List<BakedQuad> getQuads(IBlockState s,EnumFacing f,long seed){return f==null?quads:Collections.emptyList();}
  public boolean isAmbientOcclusion(){return true;}public boolean isGui3d(){return false;}public boolean isBuiltInRenderer(){return false;}
  public TextureAtlasSprite getParticleTexture(){return side();}public ItemCameraTransforms getItemCameraTransforms(){return ItemCameraTransforms.DEFAULT;}public ItemOverrideList getOverrides(){return ItemOverrideList.NONE;}
 }
 static int[][] render(IBakedModel model,LightingWorld w,IBlockState state,BlockPos pos,boolean forge,boolean smooth){
  BufferBuilder out=new BufferBuilder(8192);out.begin(7,DefaultVertexFormats.BLOCK);BlockColors colors=new BlockColors();boolean rendered;
  if(forge){VertexLighterFlat l=smooth?new VertexLighterSmoothAo(colors):new VertexLighterFlat(colors);VertexBufferConsumer sink=new VertexBufferConsumer(out);sink.setOffset(pos);l.setParent(sink);rendered=ForgeBlockModelRenderer.render(l,w,model,state,pos,out,true,0);}else{BlockModelRenderer r=new BlockModelRenderer(colors);rendered=smooth?r.renderModelSmooth(w,model,state,pos,out,true,0):r.renderModelFlat(w,model,state,pos,out,true,0);}
  out.finishDrawing();check(rendered==(out.getVertexCount()>0),"native renderer result");int[][] result=new int[out.getVertexCount()][7];ByteBuffer b=out.getByteBuffer();for(int v=0;v<result.length;v++)for(int k=0;k<7;k++)result[v][k]=b.getInt(v*28+k*4);return result;
 }
 static List<BakedQuad> visibleNormalized(int m,int owner,int mask){List<BakedQuad> result=new ArrayList<>();for(BakedQuad q:RenderGrate.bake(m,top(owner),side()))if((mask&(1<<q.getFace().getIndex()))!=0)result.add(q);return result;}
 static void lighting()throws Exception{
  int fallbackDifferences=0,damageDifferences=0;TextureAtlasSprite crack=sprite("minecraft:blocks/destroy_stage_3");
  for(int owner=0;owner<2;owner++)for(int m=0;m<16;m++)for(boolean asymmetric:new boolean[]{false,true})for(boolean smooth:new boolean[]{false,true}){
   IBlockState listed=blocks[owner].getStateFromMeta(m);LightingWorld w=new LightingWorld(listed,asymmetric);IBlockState s=blocks[owner].getExtendedState(listed,w,POS);BlockGrate.RenderSnapshot snapshot=BlockGrate.renderSnapshot(s);IBakedModel model=new GrateBakedModel(m,top(owner),side()),reference=new StaticModel(visibleNormalized(m,owner,snapshot.visible));
   int[][] got=render(model,w,s,POS,true,smooth),want=render(reference,w,s,snapshot.renderPos,true,smooth);check(got.length==want.length,"normalized native reference vertex count");
   for(int v=0;v<got.length;v++)for(int a=0;a<7;a++)check(got[v][a]==want[v][a],"Forge shifted-origin exact native reference owner="+owner+" meta="+m+" smooth="+smooth+" v="+v+" attr="+a);
   int[][] fallback=render(model,w,s,POS,false,smooth),fallbackReference=render(reference,w,s,snapshot.renderPos,false,smooth);for(int v=0;v<fallback.length;v++)if(fallback[v][3]!=fallbackReference[v][3]||fallback[v][6]!=fallbackReference[v][6])fallbackDifferences++;
   if(BlockGrate.renderShift(m)==0)check(Arrays.deepEquals(fallback,fallbackReference),"unshifted vanilla reference exact");
   IBakedModel damaged=net.minecraftforge.client.ForgeHooksClient.getDamageModel(model,crack,s,w,POS);List<BakedQuad> nativeCrack=quads(damaged,s),original=quads(model,s);check(nativeCrack.size()==original.size(),"native damage geometry count");for(int j=0;j<original.size();j++)check(Arrays.equals(nativeCrack.get(j).getVertexData(),new BakedQuadRetextured(original.get(j),crack).getVertexData()),"damage exact native retexture geometry/material");int[][] damage=render(damaged,w,s,POS,true,smooth);for(int v=0;v<damage.length;v++)if(damage[v][3]!=got[v][3]||damage[v][6]!=got[v][6])damageDifferences++;
  }
  check(fallbackDifferences>0&&damageDifferences>0,"unapproved fallback/damage lighting gates are detected, not hidden");
  System.out.println("PASS exact native Forge Flat/SmoothAo shifted-origin reference in 128 scenarios. OPEN GATES: vanilla fallback differs at "+fallbackDifferences+" vertices; native damage wrapper loss differs at "+damageDifferences+" vertices. Counts are test comparisons, not unique bugs.");
 }
 static void contextRestoration()throws Exception {
  for(boolean smooth:new boolean[]{false,true})for(int metadata:new int[]{8,9})for(boolean fail:new boolean[]{false,true}) {
   IBlockState listed=blocks[0].getStateFromMeta(metadata);LightingWorld source=new LightingWorld(listed,true);IBlockState snapshot=blocks[0].getExtendedState(listed,source,POS);
   BakedQuad q=quads(new GrateBakedModel(metadata,top(0),side()),snapshot).get(0);
   BufferBuilder buffer=new BufferBuilder(256);buffer.begin(7,DefaultVertexFormats.BLOCK);VertexBufferConsumer sink=new VertexBufferConsumer(buffer);sink.setOffset(POS);
   final boolean shouldFail=fail;IVertexConsumer parent=new VertexTransformer(sink){@Override public void put(int element,float... values){if(shouldFail)throw new IllegalStateException("intentional test sink failure");super.put(element,values);}};
   VertexLighterFlat lighter=smooth?new VertexLighterSmoothAo(new BlockColors()):new VertexLighterFlat(new BlockColors());
   LightingWorld previousWorld=new LightingWorld(Blocks.STONE.getDefaultState(),false);IBlockState previousState=Blocks.STONE.getDefaultState();BlockPos previousPos=POS.add(3,4,5);
   lighter.setParent(parent);lighter.setWorld(previousWorld);lighter.setState(previousState);lighter.setBlockPos(previousPos);lighter.updateBlockInfo();
   boolean threw=false;try{q.pipe(lighter);}catch(IllegalStateException failure){check(failure.getMessage().equals("intentional test sink failure"),"original sink failure preserved");threw=true;}
   check(threw==fail,"intentional sink failure only");BlockInfo info=(BlockInfo)get(lighter,VertexLighterFlat.class,"blockInfo");
   check(get(lighter,QuadGatheringTransformer.class,"parent")==parent&&info.getWorld()==previousWorld&&info.getState()==previousState&&info.getBlockPos()==previousPos,"exact native parent/world/state/position restored on success or failure");
   if(!fail)check(buffer.getVertexCount()==4,"one native shifted quad emitted");
  }
  System.out.println("PASS exact native Flat/SmoothAo parent/world/state/position restoration on normal output and intentional sink failure; unknown mod subclasses are not certified.");
 }
 static ResourceLocation route(int i,int meta,boolean item){return new ResourceLocation("hbm","native_grate/tile."+NAMES[i]+(item?"/item":"/"+meta+"/world"));}
 static void reload(IResourcePack pack)throws Exception{resources.reloadResources(Collections.singletonList(pack));ModelLoaderRegistry.clearModelCache(resources);}
 static IBakedModel bake(ResourceLocation id,boolean item,int owner)throws Exception{IModel m=ModelLoaderRegistry.getModel(id);check(m!=ModelLoaderRegistry.getMissingModel(),"actual loader route");check(new HashSet<>(m.getTextures()).equals(new HashSet<>(item?Arrays.asList(new ResourceLocation(top(owner).getIconName())):Arrays.asList(new ResourceLocation(top(owner).getIconName()),new ResourceLocation(side().getIconName())))),"original material dependencies");return m.bake(m.getDefaultState(),DefaultVertexFormats.ITEM,t->sprite(t.toString()));}
 static void registry(IResourcePack pack)throws Exception{
  reload(pack);TextureMap atlas=new TextureMap("textures");ModelManager initial=new ModelManager(atlas);BlockModelShapes shapes=new BlockModelShapes(initial);new ModelLoader(resources,atlas,shapes);net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(GrateModelHandler.class);net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.ModelRegistryEvent());net.minecraftforge.common.MinecraftForge.EVENT_BUS.unregister(GrateModelHandler.class);reload(pack);
  Map<?,?> custom=(Map<?,?>)get(null,ModelLoader.class,"customModels");check(custom.size()==32,"only exact two owners x16 registered");net.minecraft.util.registry.RegistrySimple<ModelResourceLocation,IBakedModel> registry=new net.minecraft.util.registry.RegistrySimple<>();Map<IBlockState,ModelResourceLocation> states=new LinkedHashMap<>();ModelLoader.onRegisterAllBlocks(shapes);
  for(int i=0;i<2;i++){
   Map<IBlockState,ModelResourceLocation> variants=shapes.getBlockStateMapper().getVariants(blocks[i]);check(variants.size()==16,"sixteen native listed states");ModelBlockDefinition json;try(Reader in=new InputStreamReader(pack.getInputStream(new ResourceLocation("hbm","blockstates/tile."+NAMES[i]+".json")),StandardCharsets.UTF_8)){json=ModelBlockDefinition.parseFromReader(in,blocks[i].getRegistryName());}
   for(int m=0;m<16;m++){
    IBlockState listed=blocks[i].getStateFromMeta(m);ModelResourceLocation mapped=variants.get(listed);check(mapped.getVariant().equals("metadata="+m)&&json.hasVariant(mapped.getVariant()),"native blockstate metadata route");Variant v=json.getVariant(mapped.getVariant()).getVariantList().get(0);check(v.getModelLocation().getPath().equals("block/"+route(i,m,false).getPath())&&v.getRotation()==ModelRotation.X0_Y0&&!v.isUvLock(),"world route no transform");IBakedModel world=bake(v.getModelLocation(),false,i);registry.putObject(mapped,world);states.put(listed,mapped);check(quads(world,extended(i,m)).size()==6,"actual loader world geometry");
    Item item=Item.getItemFromBlock(blocks[i]);ModelResourceLocation id=(ModelResourceLocation)custom.get(org.apache.commons.lang3.tuple.Pair.of(item.delegate,m));check(id!=null&&id.getPath().equals(route(i,m,true).getPath())&&id.getVariant().equals("inventory"),"exact deferred item metadata route");IBakedModel inventory=bake(id,true,i);registry.putObject(id,inventory);check(!inventory.isGui3d()&&!inventory.isBuiltInRenderer()&&!quads(inventory,null).isEmpty(),"original flat item model");for(BakedQuad q:quads(inventory,null))check(q.getSprite()==top(i),"flat item original top only");
   }
  }
  for(String name:new String[]{"grate_top","grate_wide_top","grate_side"}){ResourceLocation png=new ResourceLocation("hbm","textures/blocks/"+name+".png");check(pack.resourceExists(png),"exact-case original texture");try(InputStream in=pack.getInputStream(png)){check(ImageIO.read(in)!=null,"original PNG decodes");}}
  ModelManager manager=(ModelManager)unsafe.allocateInstance(ModelManager.class);set(manager,ModelManager.class,"modelRegistry",registry);set(manager,ModelManager.class,"defaultModel",registry.getObject(states.values().iterator().next()));ItemModelMesher mesher=new ItemModelMesher(manager);ModelLoader.onRegisterItems(mesher);mesher.rebuildCache();for(int i=0;i<2;i++)for(int m=0;m<16;m++){Item item=Item.getItemFromBlock(blocks[i]);ModelResourceLocation id=(ModelResourceLocation)custom.get(org.apache.commons.lang3.tuple.Pair.of(item.delegate,m));check(mesher.getItemModel(new ItemStack(item,1,m))==registry.getObject(id),"actual item model mesher identity");}BlockModelShapes actual=new BlockModelShapes(manager);ModelLoader.onRegisterAllBlocks(actual);actual.reloadModels();for(Map.Entry<IBlockState,ModelResourceLocation> e:states.entrySet())check(actual.getModelForState(e.getKey())==registry.getObject(e.getValue()),"native block model shapes identity");
  IBakedModel old=bake(route(0,2,false),false,0);IModel oldDescriptor=ModelLoaderRegistry.getModel(route(0,2,false));reload(pack);sprites.clear();IBakedModel fresh=bake(route(0,2,false),false,0);check(fresh.getParticleTexture()!=old.getParticleTexture()&&ModelLoaderRegistry.getModel(route(0,2,false))!=oldDescriptor,"reload renews model and atlas identity");System.out.println("PASS native 32 world/32 item routes, actual model mesher/block model shapes, original three PNG resources and reload: "+pack.getPackName());
 }
 static void isolation()throws Exception{
  ForgeRegistries.BLOCKS.register(new BlockGrate(Material.IRON,false).setRegistryName("foreign","tile.steel_grate"));ForgeRegistries.BLOCKS.register(new BlockGrate(Material.IRON,true).setRegistryName("hbm","tile.unknown_grate"));ForgeRegistries.BLOCKS.register(new BlockGrate(Material.IRON,false){}.setRegistryName("hbm","tile.grate_subclass"));GrateModelHandler.registerModels(new net.minecraftforge.client.event.ModelRegistryEvent());check(((Map<?,?>)get(null,ModelLoader.class,"customModels")).size()==32,"foreign/unknown/subclass owners untouched");
  ICustomModelLoader loader=(ICustomModelLoader)get(null,GrateModelHandler.class,"LOADER");for(String prefix:new String[]{"","block/","models/","models/block/"})check(loader.accepts(new ResourceLocation("hbm",prefix+route(0,2,false).getPath())),"normalized native loader prefix");for(String bad:new String[]{"unknown/0/world","tile.steel_grate/16/world","tile.steel_grate/-1/world","tile.steel_grate/no/world","tile.steel_grate_wide/0/other","tile.steel_grate_wide/0/world/extra"}){check(!loader.accepts(new ResourceLocation("hbm","native_grate/"+bad)),"unknown route not claimed");rejects(()->loader.loadModel(new ResourceLocation("hbm","native_grate/"+bad)),"invalid route rejected");}check(!loader.accepts(new ResourceLocation("foreign",route(0,0,false).getPath())),"foreign namespace not claimed");
 }
 public static void main(String[] args)throws Exception{
  resourceRoot=new File(args[0]);setup();originalGeometry(new File(args[2]));culling();lighting();contextRestoration();IResourcePack folder=new FolderResourcePack(resourceRoot);registry(folder);try(FileResourcePack jar=new FileResourcePack(new File(args[1]))){registry(jar);}isolation();System.out.println("PASS grate native render: "+checks+" assertions and "+vertices+" original/native face vertices. No live client/GL/save acceptance; lighting gates above remain unapproved.");
 }
}
