import java.io.*;
import java.lang.reflect.*;
import java.nio.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import javax.imageio.ImageIO;
import com.hbm.blocks.generic.BlockWand;
import com.hbm.main.WandModelHandler;
import com.hbm.render.block.nativeblock.WandBakedModel;
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
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public final class WandNativeTest {
 static long checks,vertices;static final BlockPos POS=new BlockPos(8,64,8);
 static BlockWand block;static sun.misc.Unsafe unsafe;static SimpleReloadableResourceManager resources;static File root;
 static final Map<String,TextureAtlasSprite> sprites=new HashMap<>();
 static void check(boolean b,String s){checks++;if(!b)throw new AssertionError(s);}
 static Field field(Class<?> c,String n)throws Exception{Field f=c.getDeclaredField(n);f.setAccessible(true);return f;}
 static Object get(Object o,Class<?> c,String n)throws Exception{return field(c,n).get(o);}
 static void set(Object o,Class<?> c,String n,Object v)throws Exception{field(c,n).set(o,v);}
 interface Throwing {void run()throws Exception;}
 static void rejects(Throwing r,String s)throws Exception{boolean b=false;try{r.run();}catch(Exception e){b=true;}check(b,s);}
 static final class Sprite extends TextureAtlasSprite {
  Sprite(String name)throws Exception{super(name);java.awt.image.BufferedImage image=ImageIO.read(new File(root,"assets/hbm/textures/blocks/wand_air.png"));setIconWidth(image.getWidth());setIconHeight(image.getHeight());setFramesTextureData(Collections.singletonList(new int[][]{image.getRGB(0,0,image.getWidth(),image.getHeight(),null,0,image.getWidth())}));initSprite(512,128,64,32,false);}
 }
 static TextureAtlasSprite sprite(String name){try{TextureAtlasSprite s=sprites.get(name);if(s==null){s=new Sprite(name);sprites.put(name,s);}return s;}catch(Exception e){throw new RuntimeException(e);}}
 static TextureAtlasSprite sprite(){return sprite("hbm:blocks/wand_air");}
 static void setup()throws Exception{
  Bootstrap.register();net.minecraftforge.fml.common.ModMetadata md=new net.minecraftforge.fml.common.ModMetadata();md.modId="hbm";md.name="Wand native proof";md.version="probe";net.minecraftforge.fml.common.DummyModContainer c=new net.minecraftforge.fml.common.DummyModContainer(md);net.minecraftforge.fml.common.Loader.instance().setupTestHarness(c);net.minecraftforge.fml.common.Loader.instance().setActiveModContainer(c);
  unsafe=(sun.misc.Unsafe)field(sun.misc.Unsafe.class,"theUnsafe").get(null);resources=new SimpleReloadableResourceManager(new net.minecraft.client.resources.data.MetadataSerializer());net.minecraft.client.Minecraft mc=(net.minecraft.client.Minecraft)unsafe.allocateInstance(net.minecraft.client.Minecraft.class);set(null,net.minecraft.client.Minecraft.class,"instance",mc);set(mc,net.minecraft.client.Minecraft.class,"resourceManager",resources);if(net.minecraft.launchwrapper.Launch.blackboard==null)net.minecraft.launchwrapper.Launch.blackboard=new HashMap<>();net.minecraft.launchwrapper.Launch.blackboard.put("fml.deobfuscatedEnvironment",Boolean.TRUE);
 }
 static void deferredItems()throws Exception{
  block=new BlockWand(Blocks.AIR);ForgeRegistries.BLOCKS.register(block.setTranslationKey("wand_air").setRegistryName("hbm","tile.wand_air"));
  check(Item.getItemFromBlock(block)==Items.AIR,"construction and block registration do not allocate premature ItemBlock");
  rejects(()->WandModelHandler.registerModels(new net.minecraftforge.client.event.ModelRegistryEvent()),"missing ItemBlock must not bind AIR");
  check(((Map<?,?>)get(null,ModelLoader.class,"customModels")).isEmpty(),"missing item did not create custom model routes");
  ItemBlock item=new ItemBlock(block);item.setHasSubtypes(true);item.setRegistryName(block.getRegistryName());ForgeRegistries.ITEMS.register(item);((Map<Block,Item>)get(null,Item.class,"BLOCK_TO_ITEM")).put(block,item);check(Item.getItemFromBlock(block)==item,"deferred real native item identity");
 }
 static final class NativeWorld extends World {
  final Map<Long,Chunk> chunks=new HashMap<>();
  NativeWorld(){super(new SaveHandlerMP(),new WorldInfo(new NBTTagCompound()),new WorldProviderSurface(),new Profiler(),false);provider.setWorld(this);chunkProvider=createChunkProvider();for(int x=-1;x<=1;x++)for(int z=-1;z<=1;z++){Chunk c=new Chunk(this,x,z);c.markLoaded(true);chunks.put(key(x,z),c);}}
  static long key(int x,int z){return ((long)x<<32)^(z&0xffffffffL);}
  protected IChunkProvider createChunkProvider(){return new IChunkProvider(){public Chunk getLoadedChunk(int x,int z){return chunks.get(key(x,z));}public Chunk provideChunk(int x,int z){return getLoadedChunk(x,z);}public boolean tick(){return false;}public String makeString(){return "wand native proof";}public boolean isChunkGeneratedAt(int x,int z){return getLoadedChunk(x,z)!=null;}};}
  protected boolean isChunkLoaded(int x,int z,boolean empty){return chunks.containsKey(key(x,z));}
  public boolean checkLight(BlockPos p){return true;}
 }
 static final class LightingWorld implements IBlockAccess {
  final IBlockState own;final boolean asymmetric;final int mask;final Block other;
  LightingWorld(IBlockState s,boolean a,int m,Block o){own=s;asymmetric=a;mask=m;other=o;}
  public IBlockState getBlockState(BlockPos p){if(p.equals(POS))return own;for(EnumFacing side:EnumFacing.values())if(p.equals(POS.offset(side)))return ((mask&(1<<side.getIndex()))!=0?own.getBlock():other).getDefaultState();return asymmetric&&p.getX()<8&&p.getZ()>=8?Blocks.STONE.getDefaultState():Blocks.AIR.getDefaultState();}
  public int getCombinedLight(BlockPos p,int emission){if(!asymmetric)return 0x00f000f0;int x=p.getX()-8,y=p.getY()-64,z=p.getZ()-8;return Math.floorMod(11+3*x+5*y+7*z,16)*16<<16|Math.floorMod(7+5*x+3*y+z,16)*16;}
  public TileEntity getTileEntity(BlockPos p){return null;}public boolean isAirBlock(BlockPos p){return getBlockState(p).getBlock()==Blocks.AIR;}public net.minecraft.world.biome.Biome getBiome(BlockPos p){return Biomes.PLAINS;}public int getStrongPower(BlockPos p,EnumFacing f){return 0;}public WorldType getWorldType(){return WorldType.DEFAULT;}public boolean isSideSolid(BlockPos p,EnumFacing f,boolean fallback){return getBlockState(p).isOpaqueCube();}
 }
 static void ownerBehavior()throws Exception{
  NativeWorld world=new NativeWorld();BlockWand differentOwner=new BlockWand(Blocks.AIR);
  check(block.exportAs==Blocks.AIR,"original AIR structure-export identity");
  for(int m=0;m<16;m++){
   IBlockState s=block.getStateFromMeta(m);check(block.getMetaFromState(s)==m,"metadata survives native state round trip");world.setBlockState(POS,s,2);check(world.getBlockState(POS)==s&&world.getTileEntity(POS)==null,"real native World/Chunk preserves state with no tile");
   check(!s.isOpaqueCube()&&!s.isFullCube()&&!block.hasTileEntity(s),"original nonopaque/nonfull/non-tile");check(s.getMaterial()==Material.GLASS&&s.getRenderType()==EnumBlockRenderType.MODEL&&block.getRenderLayer()==BlockRenderLayer.CUTOUT_MIPPED,"native glass alpha-tested nonblended MODEL path");check(block.getSoundType(s,world,POS,null)==SoundType.STONE&&block.getBlockHardness(s,world,POS)==0,"original default sound/hardness unchanged");check(block.getLightOpacity(s)==0&&s.getLightValue()==0,"original nonopaque light opacity/no emission");check(block.getItemDropped(s,new Random(0),0)==Item.getItemFromBlock(block)&&block.damageDropped(s)==0,"original own-item zero-damage drop");check(block.getBoundingBox(s,world,POS).equals(new AxisAlignedBB(1D/16,1D/16,1D/16,15D/16,15D/16,15D/16)),"original inset selection");check(block.getCollisionBoundingBox(s,world,POS)==null,"original null collision");List<AxisAlignedBB> boxes=new ArrayList<>();block.addCollisionBoxToList(s,world,POS,new AxisAlignedBB(0,0,0,100,100,100),boxes,null,false);check(boxes.isEmpty(),"actual native collision pipeline adds no box");
   for(int mask=0;mask<64;mask++)for(Block other:new Block[]{Blocks.AIR,Blocks.STONE,Blocks.GLASS,differentOwner})for(EnumFacing side:EnumFacing.values()){LightingWorld w=new LightingWorld(s,false,mask,other);check(block.shouldSideBeRendered(s,w,POS,side)==((mask&(1<<side.getIndex()))==0),"exact same-owner side suppression, not opacity or class");check(block.getBlockFaceShape(w,s,POS,side)==BlockFaceShape.UNDEFINED,"no support-face invented");}
  }
  System.out.println("PASS actual native owner and World/Chunk: export-as AIR, inset selection, no collision/tile, 16 metadata, defaults and every six-neighbor same-identity mask.");
 }
 static List<BakedQuad> quads(IBakedModel m,IBlockState s){check(m.getQuads(s,null,0).isEmpty(),"world geometry only in native culling buckets");List<BakedQuad> r=new ArrayList<>();for(EnumFacing side:EnumFacing.values())r.addAll(m.getQuads(s,side,0));return r;}
 static void originalGeometry(File jar)throws Exception{
  IBakedModel model=new WandBakedModel(sprite());check(model.isAmbientOcclusion()&&!model.isGui3d()&&!model.isBuiltInRenderer(),"native AO, flat item semantics, no built-in renderer");check(model.getParticleTexture()==sprite()&&model.getOverrides()==ItemOverrideList.NONE,"original texture and no item override");
  double[] bounds={1D/16,1D/16,1D/16,15D/16,15D/16,15D/16};
  try(OriginalWandOracle.LegacyFaces legacy=new OriginalWandOracle.LegacyFaces(jar)){
   for(int meta=0;meta<16;meta++)for(EnumFacing side:EnumFacing.values()){
    List<BakedQuad> pair=model.getQuads(block.getStateFromMeta(meta),side,0);check(pair.size()==2,"inside then outside per visible side");try{pair.add(null);throw new AssertionError("mutable mesh");}catch(UnsupportedOperationException expected){checks++;}
    for(int pass=0;pass<2;pass++){
     BakedQuad q=pair.get(pass);double[][] want=legacy.standardFace(bounds,side.getIndex(),sprite(),pass==0);int[] got=q.getVertexData();check(q.getFormat().equals(DefaultVertexFormats.ITEM)&&q.shouldApplyDiffuseLighting()&&!q.hasTintIndex()&&q.getFace()==side,"native normal/diffuse identity retained for both passes");int shiftFound=-1;
     for(int shift=0;shift<4;shift++){boolean same=true;for(int v=0;v<4;v++)for(int axis=0;axis<5;axis++)if(got[v*7+(axis<3?axis:axis+1)]!=Float.floatToIntBits((float)want[(v+shift)%4][axis]))same=false;if(same)shiftFound=shift;}
     check(shiftFound==new int[]{0,2,1,0,1,3}[side.getIndex()],"original face bytecode exact XYZ/UV/winding with canonical cyclic shift side="+side+" inside="+(pass==0));
     for(int v=0;v<4;v++){vertices++;check(got[v*7+3]==-1,"no baked world light/color");int n=got[v*7+6];check((byte)n==side.getXOffset()*127&&(byte)(n>>8)==side.getYOffset()*127&&(byte)(n>>16)==side.getZOffset()*127,"same outward original lighting normal on inner and outer");}
    }
   }
  }
  System.out.println("PASS independent pinned original 1.7.10 RenderBlocks six face bytecodes: 16 metadata x six faces x two passes, exact position/UV/inside winding; no geometry oracle derived from production.");
 }
 static int[][] render(IBakedModel model,LightingWorld w,boolean forge,boolean smooth,boolean cull){BufferBuilder out=new BufferBuilder(8192);out.begin(7,DefaultVertexFormats.BLOCK);BlockColors colors=new BlockColors();boolean rendered;if(forge){VertexLighterFlat l=smooth?new VertexLighterSmoothAo(colors):new VertexLighterFlat(colors);VertexBufferConsumer sink=new VertexBufferConsumer(out);sink.setOffset(POS);l.setParent(sink);rendered=ForgeBlockModelRenderer.render(l,w,model,w.own,POS,out,cull,0);}else{BlockModelRenderer r=new BlockModelRenderer(colors);rendered=smooth?r.renderModelSmooth(w,model,w.own,POS,out,cull,0):r.renderModelFlat(w,model,w.own,POS,out,cull,0);}out.finishDrawing();check(rendered==(out.getVertexCount()>0),"actual renderer handled result");int[][] result=new int[out.getVertexCount()][7];ByteBuffer b=out.getByteBuffer();for(int v=0;v<result.length;v++)for(int k=0;k<7;k++)result[v][k]=b.getInt(v*28+k*4);return result;}
 static void lighting()throws Exception{
  IBakedModel model=new WandBakedModel(sprite());IBlockState state=block.getDefaultState();
  for(boolean forge:new boolean[]{false,true})for(boolean smooth:new boolean[]{false,true})for(int mask=0;mask<64;mask++)for(Block other:new Block[]{Blocks.AIR,Blocks.STONE}){
   int[][] got=render(model,new LightingWorld(state,true,mask,other),forge,smooth,true);check(got.length==(6-Integer.bitCount(mask))*8,"actual native renderer exact same-owner culling count");for(int[] v:got){check(v[3]>>>24==255,"native opaque color");for(int a=0;a<3;a++)check(Float.isFinite(Float.intBitsToFloat(v[a])),"finite lit position");}
  }
  for(boolean forge:new boolean[]{false,true}){
   int[][] flat=render(model,new LightingWorld(state,true,0,Blocks.AIR),forge,false,true),smooth=render(model,new LightingWorld(state,true,0,Blocks.AIR),forge,true,true);boolean varies=false;Set<Integer> values=new HashSet<>();for(int v=0;v<smooth.length;v++){values.add(smooth[v][6]);varies|=smooth[v][6]!=flat[v][6]||smooth[v][3]!=flat[v][3];}check(varies&&values.size()>1,"native smooth AO/light response not a flat placeholder");check(render(model,new LightingWorld(state,false,63,Blocks.AIR),forge,false,false).length==48,"native checkSides=false renders all inner and outer faces");
  }
  for(boolean forge:new boolean[]{false,true})for(boolean smooth:new boolean[]{false,true}){int[][] uniform=render(model,new LightingWorld(state,false,0,Blocks.AIR),forge,smooth,true);StringBuilder colors=new StringBuilder();for(int side=0;side<6;side++){if(side>0)colors.append(",");colors.append(Integer.toHexString(uniform[side*8][3]));for(int vertex=0;vertex<4;vertex++)check(uniform[side*8+vertex][3]==uniform[side*8+4+vertex][3]&&uniform[side*8+vertex][6]==uniform[side*8+4+vertex][6],"uniform native inner/outer face lighting direction unchanged");}System.out.println("OBSERVED "+(forge?"Forge":"vanilla")+" "+(smooth?"smooth":"flat")+" uniform six-face RGBA "+colors);}
  System.out.println("PASS actual Forge and vanilla native flat/smooth lighting and culling, all 64 masks with air/opaque neighbors; nonconstant asymmetric native AO response. This is not original-vs-native nondefault-light parity.");
  LightingWorld w=new LightingWorld(state,false,0,Blocks.AIR);IBakedModel damage=net.minecraftforge.client.ForgeHooksClient.getDamageModel(model,sprite("minecraft:blocks/destroy_stage_3"),state,w,POS);List<BakedQuad> before=quads(model,state),after=quads(damage,state);check(before.size()==after.size(),"native damage maintains face count");for(int i=0;i<before.size();i++)check(Arrays.equals(new BakedQuadRetextured(before.get(i),sprite("minecraft:blocks/destroy_stage_3")).getVertexData(),after.get(i).getVertexData()),"native damage retexture data equality");int[][] ordinary=render(model,w,true,false,true),damaged=render(damage,w,true,false,true);check(ordinary.length==damaged.length,"damage actual render count");int delta=0;for(int i=0;i<ordinary.length;i++)if(ordinary[i][3]!=damaged[i][3]||ordinary[i][6]!=damaged[i][6])delta++;System.out.println("OBSERVED damage flat lighting differs from ordinary on "+delta+"/"+ordinary.length+" vertices under uniform light; damage retexturing remains a separate native-engine gate, not ordinary lighting proof.");
 }
 static void exportDraw(File target)throws Exception {
  Map<String,Object> data=new LinkedHashMap<>();java.awt.image.BufferedImage image=ImageIO.read(new File(root,"assets/hbm/textures/blocks/wand_air.png"));data.put("width",image.getWidth());data.put("height",image.getHeight());data.put("pixels",image.getRGB(0,0,image.getWidth(),image.getHeight(),null,0,image.getWidth()));List<Map<String,Object>> cases=new ArrayList<>();IBakedModel model=new WandBakedModel(sprite());
  for(boolean forge:new boolean[]{false,true})for(boolean smooth:new boolean[]{false,true})for(boolean asymmetric:new boolean[]{false,true})for(int mask:new int[]{0,1,2,4,8,16,32,21,42,63}){Map<String,Object> c=new LinkedHashMap<>();c.put("forge",forge);c.put("smooth",smooth);c.put("asymmetric",asymmetric);c.put("mask",mask);c.put("vertices",render(model,new LightingWorld(block.getDefaultState(),asymmetric,mask,Blocks.AIR),forge,smooth,true));cases.add(c);}data.put("cases",cases);try(Writer out=new OutputStreamWriter(new FileOutputStream(target),StandardCharsets.UTF_8)){new com.google.gson.Gson().toJson(data,out);}System.out.println("PASS exported "+cases.size()+" actual native renderer vertex streams for independent OpenGL draw-order framebuffer check");
 }
 static ResourceLocation route(boolean item){return new ResourceLocation("hbm","native_wand/"+(item?"item":"world"));}
 static void reload(IResourcePack pack)throws Exception{resources.reloadResources(Collections.singletonList(pack));ModelLoaderRegistry.clearModelCache(resources);}
 static IBakedModel bake(ResourceLocation id)throws Exception{IModel m=ModelLoaderRegistry.getModel(id);check(m!=ModelLoaderRegistry.getMissingModel(),"real custom loader route");check(m.getTextures().equals(Collections.singletonList(new ResourceLocation("hbm","blocks/wand_air"))),"exact original texture dependency");if(id.getPath().endsWith("item"))check(m instanceof ItemLayerModel,"original flat icon uses genuine ItemLayerModel");return m.bake(m.getDefaultState(),DefaultVertexFormats.ITEM,t->sprite(t.toString()));}
 static void registry(IResourcePack pack)throws Exception{
  reload(pack);TextureMap atlas=new TextureMap("textures");ModelManager initial=new ModelManager(atlas);BlockModelShapes shapes=new BlockModelShapes(initial);new ModelLoader(resources,atlas,shapes);net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(WandModelHandler.class);net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.ModelRegistryEvent());net.minecraftforge.common.MinecraftForge.EVENT_BUS.unregister(WandModelHandler.class);reload(pack);
  Map<?,?> custom=(Map<?,?>)get(null,ModelLoader.class,"customModels");check(custom.size()==16,"only exact owner x16 item metadata routes");ModelLoader.onRegisterAllBlocks(shapes);Map<IBlockState,ModelResourceLocation> mapped=shapes.getBlockStateMapper().getVariants(block);check(mapped.size()==16,"all sixteen native stored metadata map");ModelBlockDefinition json;try(Reader in=new InputStreamReader(pack.getInputStream(new ResourceLocation("hbm","blockstates/tile.wand_air.json")),StandardCharsets.UTF_8)){json=ModelBlockDefinition.parseFromReader(in,block.getRegistryName());}
  Variant v=json.getVariant("normal").getVariantList().get(0);check(v.getModelLocation().getPath().equals("block/"+route(false).getPath())&&v.getRotation()==ModelRotation.X0_Y0&&!v.isUvLock(),"block JSON exact native route, no unintended transform");
  IBakedModel world=bake(v.getModelLocation()),item=bake(route(true));check(quads(world,block.getDefaultState()).size()==12,"native loader returns original two-sided cube");check(!item.isGui3d()&&!item.isBuiltInRenderer()&&!item.getQuads(null,null,0).isEmpty(),"native generated flat item not world cube");for(EnumFacing side:EnumFacing.values())check(item.getQuads(null,side,0).isEmpty(),"native item quads unculled");
  net.minecraft.util.registry.RegistrySimple<ModelResourceLocation,IBakedModel> registry=new net.minecraft.util.registry.RegistrySimple<>();Item ownerItem=Item.getItemFromBlock(block);
  for(int meta=0;meta<16;meta++){ModelResourceLocation model=mapped.get(block.getStateFromMeta(meta));check(model.equals(new ModelResourceLocation(block.getRegistryName(),"normal")),"all owner metadata normal world route");registry.putObject(model,world);ModelResourceLocation id=(ModelResourceLocation)custom.get(org.apache.commons.lang3.tuple.Pair.of(ownerItem.delegate,meta));check(id.equals(new ModelResourceLocation(route(true),"inventory")),"actual deferred item registration exact identity");registry.putObject(id,item);}
  ResourceLocation png=new ResourceLocation("hbm","textures/blocks/wand_air.png");check(pack.resourceExists(png),"exact-case original PNG path");try(InputStream in=pack.getInputStream(png)){check(ImageIO.read(in)!=null,"original texture decodes in real resource pack");}
  ModelManager manager=(ModelManager)unsafe.allocateInstance(ModelManager.class);set(manager,ModelManager.class,"modelRegistry",registry);set(manager,ModelManager.class,"defaultModel",world);ItemModelMesher mesher=new ItemModelMesher(manager);ModelLoader.onRegisterItems(mesher);mesher.rebuildCache();for(int meta=0;meta<16;meta++)check(mesher.getItemModel(new ItemStack(ownerItem,1,meta))==item,"actual native item mesher routes every metadata");BlockModelShapes actual=new BlockModelShapes(manager);ModelLoader.onRegisterAllBlocks(actual);actual.reloadModels();for(IBlockState state:mapped.keySet())check(actual.getModelForState(state)==world,"actual native block model shape route");
  IModel old=ModelLoaderRegistry.getModel(route(false));reload(pack);sprites.clear();IBakedModel fresh=bake(route(false));check(fresh.getParticleTexture()!=world.getParticleTexture()&&old!=ModelLoaderRegistry.getModel(route(false)),"native reload renews descriptor/atlas material");check(quads(world,block.getDefaultState()).get(0).getSprite()==world.getParticleTexture(),"old baked model snapshot unchanged");
  System.out.println("PASS real ModelRegistryEvent, owner state map, world loader, flat ItemLayerModel, 16 item-mesher routes and original PNG/reload: "+pack.getPackName());
 }
 static void isolation()throws Exception{
  ForgeRegistries.BLOCKS.register(new BlockWand(Blocks.AIR).setRegistryName("foreign","tile.wand_air"));ForgeRegistries.BLOCKS.register(new BlockWand(Blocks.AIR).setRegistryName("hbm","tile.unknown_wand"));WandModelHandler.registerModels(new net.minecraftforge.client.event.ModelRegistryEvent());check(((Map<?,?>)get(null,ModelLoader.class,"customModels")).size()==16,"foreign and unknown same-class owners untouched");
  ICustomModelLoader loader=(ICustomModelLoader)get(null,WandModelHandler.class,"LOADER");for(String prefix:new String[]{"","block/","models/","models/block/"})for(boolean item:new boolean[]{false,true})check(loader.accepts(new ResourceLocation("hbm",prefix+route(item).getPath())),"native model prefix supported");
  for(ResourceLocation bad:new ResourceLocation[]{new ResourceLocation("foreign",route(false).getPath()),new ResourceLocation("hbm","native_wand/world/extra"),new ResourceLocation("hbm","native_wand/other"),new ResourceLocation("hbm","native_wand/item0"),new ResourceLocation("hbm","other/world")}){check(!loader.accepts(bad),"unrelated route not claimed");rejects(()->loader.loadModel(bad),"unrelated route rejected");}
 }
 static void wrongOwner(String mode)throws Exception{
  Block wrong=mode.equals("replacement")?new Block(Material.GLASS){}:mode.equals("subclass")?new BlockWand(Blocks.AIR){}:new BlockWand(Blocks.STONE);ForgeRegistries.BLOCKS.register(wrong.setRegistryName("hbm","tile.wand_air"));WandModelHandler.registerModels(new net.minecraftforge.client.event.ModelRegistryEvent());check(((Map<?,?>)get(null,ModelLoader.class,"customModels")).isEmpty(),"wrong owner semantic/class never receives native wand routes");System.out.println("PASS isolated wrong-owner route rejection: "+mode);
 }
 public static void main(String[] args)throws Exception{root=new File(args[0]);setup();if(!args[3].equals("normal")){wrongOwner(args[3]);return;}deferredItems();ownerBehavior();originalGeometry(new File(args[2]));lighting();if(args.length>4)exportDraw(new File(args[4]));registry(new FolderResourcePack(root));try(FileResourcePack jar=new FileResourcePack(new File(args[1]))){registry(jar);}isolation();System.out.println("PASS wand native slice: "+checks+" assertions, "+vertices+" direct oracle vertices. Complete actual production owner/model/helper/handler and native Minecraft/Forge, zero collaborator stubs. Controlled atlas and Minecraft holder, not full-mod/live-game acceptance.");}
}
