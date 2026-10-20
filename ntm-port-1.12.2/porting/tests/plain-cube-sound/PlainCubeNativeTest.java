import java.io.*;
import java.lang.reflect.*;
import java.net.*;
import java.util.*;
import javax.imageio.ImageIO;
import net.minecraft.block.*;
import net.minecraft.block.state.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.client.renderer.vertex.*;
import net.minecraft.client.resources.*;
import net.minecraft.client.resources.data.MetadataSerializer;
import net.minecraft.init.*;
import net.minecraft.item.*;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraftforge.client.model.pipeline.*;
import java.nio.ByteBuffer;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.*;
import net.minecraftforge.fml.common.*;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import com.hbm.blocks.PlainCubeTextures;
import com.hbm.main.PlainCubeModelHandler;
public class PlainCubeNativeTest {
 static MetadataSerializer metadata(){ MetadataSerializer m=new MetadataSerializer();m.registerMetadataSectionType(new net.minecraft.client.resources.data.AnimationMetadataSectionSerializer(),net.minecraft.client.resources.data.AnimationMetadataSection.class);return m;}
 static final BlockPos ORIGIN=new BlockPos(8,64,8);
 static class LightingWorld implements IBlockAccess {
  final IBlockState own;final boolean asymmetric;final Map<BlockPos,IBlockState> overrides=new HashMap<>();
  LightingWorld(IBlockState s,boolean a){own=s;asymmetric=a;}
  public IBlockState getBlockState(BlockPos p){if(overrides.containsKey(p))return overrides.get(p);if(p.equals(ORIGIN))return own;return asymmetric&&p.getX()<8&&p.getZ()>=8?Blocks.STONE.getDefaultState():Blocks.AIR.getDefaultState();}
  public int getCombinedLight(BlockPos p,int emission){if(!asymmetric)return 0x00f000f0;int x=p.getX()-8,y=p.getY()-64,z=p.getZ()-8;return Math.floorMod(11+3*x+5*y+7*z,16)*16<<16|Math.floorMod(7+5*x+3*y+z,16)*16;}
  public TileEntity getTileEntity(BlockPos p){return null;}public boolean isAirBlock(BlockPos p){return getBlockState(p).getBlock()==Blocks.AIR;}public net.minecraft.world.biome.Biome getBiome(BlockPos p){return Biomes.PLAINS;}public int getStrongPower(BlockPos p,EnumFacing f){return 0;}public WorldType getWorldType(){return WorldType.DEFAULT;}public boolean isSideSolid(BlockPos p,EnumFacing f,boolean fallback){return getBlockState(p).isOpaqueCube();}
 }
 static int[][] render(IBakedModel model,LightingWorld w,IBlockState state,BlockPos pos,boolean forge,boolean smooth){
  BufferBuilder out=new BufferBuilder(8192);out.begin(7,DefaultVertexFormats.BLOCK);BlockColors colors=new BlockColors();boolean rendered;
  if(forge){VertexLighterFlat l=smooth?new VertexLighterSmoothAo(colors):new VertexLighterFlat(colors);VertexBufferConsumer sink=new VertexBufferConsumer(out);sink.setOffset(pos);l.setParent(sink);rendered=ForgeBlockModelRenderer.render(l,w,model,state,pos,out,true,0);}else{BlockModelRenderer r=new BlockModelRenderer(colors);rendered=smooth?r.renderModelSmooth(w,model,state,pos,out,true,0):r.renderModelFlat(w,model,state,pos,out,true,0);}
  out.finishDrawing();check(rendered==(out.getVertexCount()>0),"native renderer result");int[][] result=new int[out.getVertexCount()][7];ByteBuffer b=out.getByteBuffer();for(int v=0;v<result.length;v++)for(int k=0;k<7;k++)result[v][k]=b.getInt(v*28+k*4);return result;
 }

 static int checks; static void check(boolean b,String s){checks++;if(!b)throw new AssertionError(s);}
 static Field field(Class<?> c,String name)throws Exception{Field f=c.getDeclaredField(name);f.setAccessible(true);return f;}
 static File sourceJar(Class<?> c)throws Exception{URL u=c.getProtectionDomain().getCodeSource().getLocation();if(u.getProtocol().equals("jar"))u=((JarURLConnection)u.openConnection()).getJarFileURL();return new File(u.toURI());}
 static final java.util.List<String> order=new java.util.ArrayList<>();
 static String nameArg(){order.add("name");return "sound_order";}
 static net.minecraft.creativetab.CreativeTabs tabArg(Block b){check(b.getTranslationKey().equals("tile.sound_order"),"name setter completed before tab argument");order.add("tab");return net.minecraft.creativetab.CreativeTabs.BUILDING_BLOCKS;}
 static SoundType soundArg(Block b){check(b.getTranslationKey().equals("tile.sound_order")&&b.getCreativeTab()==net.minecraft.creativetab.CreativeTabs.BUILDING_BLOCKS,"all preceding setters completed before sound argument");check(b.getSoundType()==SoundType.STONE,"sound assignment not moved earlier");order.add("sound");return SoundType.CLOTH;}
 static float hardnessArg(Block b){check(b.getSoundType()==SoundType.CLOTH,"sound setter completed before following argument");order.add("hardness");return 2F;}
 static void soundBuilders(Block[] blocks)throws Exception {
  SoundType[] expected={SoundType.CLOTH,SoundType.GLASS,SoundType.GLASS,SoundType.GLASS,SoundType.SAND,SoundType.SAND,SoundType.WOOD,SoundType.GLASS,SoundType.GLASS};
  for(int i=0;i<blocks.length;i++) {
   Block b=blocks[i];check(b.getSoundType()==expected[i],"exact construction native sound identity "+i);
   check(b.getSoundType(b.getDefaultState(),null,BlockPos.ORIGIN,null)==expected[i],"native world sound accessor dispatch");
   Method builder=b.getClass().getMethod("withSoundType",SoundType.class);
   for(SoundType value:new SoundType[]{SoundType.CLOTH,SoundType.GLASS,SoundType.SAND,SoundType.WOOD,null}) {
    check(builder.invoke(b,value)==b,"native builder return preserves receiver");check(b.getSoundType()==value,"native protected setter stores exact sound reference");
   }
   builder.invoke(b,expected[i]);
  }
  com.hbm.blocks.generic.BlockOutgas a=new com.hbm.blocks.generic.BlockOutgas(net.minecraft.block.material.Material.ROCK,false,1,false);
  order.clear();Block result=((com.hbm.blocks.generic.BlockOutgas)a.setTranslationKey(nameArg()).setCreativeTab(tabArg(a))).withSoundType(soundArg(a)).setHardness(hardnessArg(a));
  check(result==a&&order.equals(java.util.Arrays.asList("name","tab","sound","hardness")),"cast chain preserves argument order and receiver identity");
  com.hbm.blocks.generic.BlockStepEffect b=new com.hbm.blocks.generic.BlockStepEffect(net.minecraft.block.material.Material.ROCK);
  order.clear();result=((com.hbm.blocks.generic.BlockStepEffect)b.noFortune().setTranslationKey(nameArg()).setCreativeTab(tabArg(b))).withSoundType(soundArg(b)).setHardness(hardnessArg(b));
  check(result==b&&!b.allowFortune&&order.equals(java.util.Arrays.asList("name","tab","sound","hardness")),"noFortune identity and exact later side-effect order");
  System.out.println("PASS nine native sound identities,world accessors,exact setter/return/null dispatch and receiver/argument sequencing for both exact owners.");
 }
 public static void main(String[] args)throws Exception {
  Bootstrap.register();FMLCommonHandler.instance().beginLoading(net.minecraftforge.fml.server.FMLServerHandler.instance());
  ModMetadata md=new ModMetadata();md.modId="hbm";md.name="Plain cube probe";md.version="1";DummyModContainer owner=new DummyModContainer(md);Loader.instance().setupTestHarness(owner);Loader.instance().setActiveModContainer(owner);
  Block[] blocks=CubeConstruction.create();soundBuilders(blocks);check(blocks.length==9,"exact 9 construction slices");
  for(int i=0;i<blocks.length;i++){
   Block b=blocks[i];check(PlainCubeTextures.bindings().get(b).equals("hbm:"+CubeConstruction.textures[i]),"original texture argument");
   check(PlainCubeTextures.bind(b,PlainCubeTextures.bindings().get(b))==b,"original Block return identity");
   ForgeRegistries.BLOCKS.register(b.setRegistryName("hbm","tile."+CubeConstruction.names[i]));check(Item.getItemFromBlock(b)==Items.AIR,"no premature ItemBlock binding");
  }
  try(NativeAtlasEnvironment.Context context=new NativeAtlasEnvironment.Context()) {
   NativeAtlasEnvironment.initializeNativeClientSettings();
   SimpleReloadableResourceManager resources=new SimpleReloadableResourceManager(metadata());
   field(Minecraft.class,"resourceManager").set(Minecraft.getMinecraft(),resources);
   net.minecraftforge.fml.common.asm.FMLSanityChecker.fmlLocation=sourceJar(TextureMap.class);
   List<IResourcePack> packs=Arrays.asList(new FileResourcePack(sourceJar(TextureMap.class)),new FolderResourcePack(new File(args[0])));
   resources.reloadResources(packs);
   TextureMap atlas=new TextureMap("textures");atlas.setMipmapLevels(2);
   net.minecraft.launchwrapper.Launch.blackboard.put("fml.deobfuscatedEnvironment",Boolean.TRUE);
   ModelManager manager=new ModelManager(atlas);
   BlockModelShapes shapes=new BlockModelShapes(manager);new ModelLoader(resources,atlas,shapes);
   boolean missing=false;try{PlainCubeModelHandler.register(new ModelRegistryEvent());}catch(IllegalStateException expected){missing=true;}check(missing,"missing late ItemBlock fails rather than registering AIR");
   for(int i=0;i<blocks.length;i++){Block b=blocks[i];String name=CubeConstruction.names[i];
    ItemBlock item=name.equals("ore_uranium_scorched")?new com.hbm.items.block.ItemBlockBase(b):name.equals("ore_gneiss_schrabidium")?new com.hbm.items.block.ItemBlockLore(b):name.equals("ore_gneiss_rare")?new com.hbm.items.special.ItemOreBlock(b):new ItemBlock(b);ForgeRegistries.ITEMS.register(item.setRegistryName(b.getRegistryName()));check(Item.getItemFromBlock(b)==item,"real Forge registry maps late ItemBlock");}
   net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(PlainCubeModelHandler.class);
   net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new ModelRegistryEvent());
   ModelLoader.onRegisterAllBlocks(shapes);
   Map<?,?> custom=(Map<?,?>)field(ModelLoader.class,"customModels").get(null);
   check(custom.size()==9,"one native inventory variant per item");
   TextureAtlasSprite previous=null;
   try(OriginalCubeFaces.LegacyFaces original=new OriginalCubeFaces.LegacyFaces(new File(args[1]))) {
    original.plainBlockSemantics();
    for(int reload=0;reload<2;reload++) {
     resources.reloadResources(packs);ModelLoaderRegistry.clearModelCache(resources);
     // Mirror the native loadSprites reset; whole-client bucket bootstrap is outside this isolated atlas probe.
     ((Map<?,?>)field(TextureMap.class,"mapRegisteredSprites").get(atlas)).clear();
     List<IModel> models=new ArrayList<>();
     net.minecraft.util.registry.RegistrySimple<ModelResourceLocation,IBakedModel> bakedRegistry=new net.minecraft.util.registry.RegistrySimple<>();
     List<IBakedModel> itemBakes=new ArrayList<>();
     for(int i=0;i<blocks.length;i++){
      Block b=blocks[i];Map<IBlockState,ModelResourceLocation> variants=shapes.getBlockStateMapper().getVariants(b);
      check(variants.size()==1,"unchanged plain native Block state count");
      ModelResourceLocation world=variants.get(b.getDefaultState());check(world.getVariant().equals("normal"),"world state mapper route");
      IModel model=ModelLoaderRegistry.getModel(world);check(model!=ModelLoaderRegistry.getMissingModel(),"actual native world loader");models.add(model);
      Set<ResourceLocation> textures=new HashSet<>(model.getTextures());check(textures.equals(Collections.singleton(new ResourceLocation("hbm:blocks/"+CubeConstruction.textures[i]))),"exact native cube texture dependency");
      for(ResourceLocation t:textures)atlas.registerSprite(t);
      for(int meta=0;meta<1;meta++){
       ModelResourceLocation item=(ModelResourceLocation)custom.get(org.apache.commons.lang3.tuple.Pair.of(Item.getItemFromBlock(b).delegate,meta));
       check(item!=null&&item.getPath().equals(world.getPath())&&item.getVariant().equals("inventory"),"single baked inventory route");
       check(ModelLoaderRegistry.getModel(item).getTextures().equals(model.getTextures()),"item/world native material equality");
      }
     }
     Method init=TextureMap.class.getDeclaredMethod("initMissingImage");init.setAccessible(true);init.invoke(atlas);atlas.loadTextureAtlas(resources);
     GlStateManager.bindTexture(atlas.getGlTextureId());
     int atlasWidth=org.lwjgl.opengl.GL11.glGetTexLevelParameteri(org.lwjgl.opengl.GL11.GL_TEXTURE_2D,0,org.lwjgl.opengl.GL11.GL_TEXTURE_WIDTH);
     int atlasHeight=org.lwjgl.opengl.GL11.glGetTexLevelParameteri(org.lwjgl.opengl.GL11.GL_TEXTURE_2D,0,org.lwjgl.opengl.GL11.GL_TEXTURE_HEIGHT);
     java.nio.IntBuffer pixels=org.lwjgl.BufferUtils.createIntBuffer(atlasWidth*atlasHeight);
     org.lwjgl.opengl.GL11.glGetTexImage(org.lwjgl.opengl.GL11.GL_TEXTURE_2D,0,32993,33639,pixels);
     for(int i=0;i<blocks.length;i++) {
      String texture="hbm:blocks/"+CubeConstruction.textures[i];TextureAtlasSprite sprite=atlas.getAtlasSprite(texture);
      check(sprite!=atlas.getMissingSprite(),"original PNG successfully stitched "+texture);
      if(i==0){check(sprite!=previous,"reload fresh atlas sprite");previous=sprite;}
      java.awt.image.BufferedImage png=ImageIO.read(new File(args[0],"assets/hbm/textures/blocks/"+CubeConstruction.textures[i]+".png"));
      for(int y=0;y<sprite.getIconHeight();y++)for(int x=0;x<sprite.getIconWidth();x++)check(pixels.get((sprite.getOriginY()+y)*atlasWidth+sprite.getOriginX()+x)==png.getRGB(x,y),"original first-frame pixels survive native GL upload");
      check(sprite.getFrameCount()==png.getHeight()/png.getWidth(),"every original animation frame retained");
      for(int frame=0;frame<sprite.getFrameCount();frame++)for(int y=0;y<sprite.getIconHeight();y++)for(int x=0;x<sprite.getIconWidth();x++)check(sprite.getFrameTextureData(frame)[0][y*sprite.getIconWidth()+x]==png.getRGB(x,frame*sprite.getIconHeight()+y),"every original animation texel retained");
      check(sprite.getIconWidth()==png.getWidth()&&sprite.getIconHeight()==png.getWidth(),"original PNG dimensions");
      IBakedModel baked=models.get(i).bake(models.get(i).getDefaultState(),DefaultVertexFormats.ITEM,t->atlas.getAtlasSprite(t.toString()));
      itemBakes.add(baked);
      bakedRegistry.putObject(new ModelResourceLocation("hbm:native_plain_cube/tile."+CubeConstruction.names[i],"inventory"),baked);
      IBakedModel reference=ModelLoaderRegistry.getModel(new ResourceLocation("minecraft:block/cube_all")).retexture(com.google.common.collect.ImmutableMap.of("all",texture)).bake(models.get(i).getDefaultState(),DefaultVertexFormats.ITEM,t->atlas.getAtlasSprite(t.toString()));
      for(ItemCameraTransforms.TransformType type:ItemCameraTransforms.TransformType.values()) {
       org.apache.commons.lang3.tuple.Pair<? extends IBakedModel,javax.vecmath.Matrix4f> perspective=baked.handlePerspective(type);
       check(perspective.getLeft()==baked,"perspective never bypasses original UV adapter");
       check(java.util.Objects.equals(perspective.getRight(),reference.handlePerspective(type).getRight()),"native display transform exact "+type);
      }
      if(reload==0 && i==0) for(boolean forge:new boolean[]{false,true})for(boolean smooth:new boolean[]{false,true})for(boolean asymmetric:new boolean[]{false,true})for(int mask=0;mask<64;mask++) {
       IBlockState own=blocks[i].getDefaultState();LightingWorld world=new LightingWorld(own,asymmetric);
       for(EnumFacing f:EnumFacing.values()) world.overrides.put(ORIGIN.offset(f),(mask&(1<<f.getIndex()))==0?Blocks.AIR.getDefaultState():Blocks.STONE.getDefaultState());
       int[][] actual=render(baked,world,own,ORIGIN,forge,smooth),expected=render(reference,world,own,ORIGIN,forge,smooth);
       check(actual.length==expected.length,"all neighboring culling masks exact native renderer");
       for(int v=0;v<actual.length;v++)for(int a:new int[]{0,1,2,3,6})check(actual[v][a]==expected[v][a],"native XYZ/color/lighting unchanged");
      }
      check(baked.isAmbientOcclusion()&&baked.isGui3d()&&!baked.isBuiltInRenderer(),"native AO/full 3D inventory normal model");
      check(baked.getParticleTexture()==sprite,"original particle texture");check(baked.getQuads(blocks[i].getDefaultState(),null,0).isEmpty(),"all faces natively culled");
      for(EnumFacing face:EnumFacing.values()) {
       List<BakedQuad> q=baked.getQuads(blocks[i].getDefaultState(),face,0);check(q.size()==1,"one face in native culling bucket");BakedQuad quad=q.get(0);
       check(quad.getFace()==face&&quad.getSprite()==sprite&&!quad.hasTintIndex(),"original face material and untinted white color");
       double[][] want=original.standardFace(new double[]{0,0,0,1,1,1},face.getIndex(),sprite);int[] data=quad.getVertexData();int stride=quad.getFormat().getIntegerSize();boolean found=false;
       for(int shift=0;shift<4;shift++){boolean equal=true;for(int v=0;v<4;v++)for(int a=0;a<5;a++){float actual=Float.intBitsToFloat(data[v*stride+(a<3?a:a+1)]);float target=(float)want[(v+shift)%4][a];if(Float.floatToIntBits(actual)!=Float.floatToIntBits(target))equal=false;}if(equal)found=true;}
       if(!found && i==0 && reload==0){System.out.println("WANT "+face+" "+java.util.Arrays.deepToString(want));for(int v=0;v<4;v++){float[] actual=new float[5];for(int a=0;a<5;a++)actual[a]=Float.intBitsToFloat(data[v*stride+(a<3?a:a+1)]);System.out.println("GOT "+java.util.Arrays.toString(actual));}}
       check(found,"original six-face XYZ/UV/winding owner="+i+" face="+face);
      }
     }
     IBakedModel missingModel=ModelLoaderRegistry.getMissingModel().bake(net.minecraftforge.common.model.TRSRTransformation.identity(),DefaultVertexFormats.ITEM,t->atlas.getMissingSprite());
     field(ModelManager.class,"modelRegistry").set(manager,bakedRegistry);field(ModelManager.class,"defaultModel").set(manager,missingModel);
     net.minecraftforge.client.ItemModelMesherForge mesher=new net.minecraftforge.client.ItemModelMesherForge(manager);
     ModelLoader.onRegisterItems(mesher);
     // Genuine previous implementation as a negative control, not a mock lookup.
     net.minecraftforge.client.ItemModelMesherForge limited=new net.minecraftforge.client.ItemModelMesherForge(manager);
     for(int i=0;i<blocks.length;i++) {
      Item item=Item.getItemFromBlock(blocks[i]);ModelResourceLocation route=new ModelResourceLocation("hbm:native_plain_cube/tile."+CubeConstruction.names[i],"inventory");
      for(int m=0;m<16;m++)limited.register(item,m,route);
      for(int damage:new int[]{Integer.MIN_VALUE,-32769,-32768,-1,0,1,15,16,32767,32768,65535,65536,Integer.MAX_VALUE}) {
       ItemStack ordinary=new ItemStack(item,1,damage);
       check(ordinary.getMetadata()==Math.max(0,damage),"native constructor damage clamp, not placement metadata");
       check(item.getMetadata(damage)==0,"actual inherited int placement metadata normalized");
       check(item.getMaxDamage()==0&&!item.getHasSubtypes(),"original plain item has no durability/subtypes");
       if(damage>65535)check(ordinary.isEmpty()&&ordinary.getItem()==Items.AIR&&mesher.getItemModel(ordinary)==missingModel,"native invalid constructor damage remains empty, never hijacked by mesh");
       else check(!ordinary.isEmpty()&&mesher.getLocation(ordinary).equals(route)&&mesher.getItemModel(ordinary)==itemBakes.get(i),"native constructor lookup arbitrary valid damage exact owner");
       ItemStack raw=new ItemStack(item,1,0);field(ItemStack.class,"itemDamage").setInt(raw,damage);
       check(raw.getItem()==item&&raw.getMetadata()==damage,"raw stack damage dispatched by actual native Item method");
       check(mesher.getLocation(raw).equals(route)&&mesher.getItemModel(raw)==itemBakes.get(i),"native Forge raw-edge lookup exact registered item owner");
       if(damage<0||damage>15)check(limited.getItemModel(raw)==missingModel,"old finite map demonstrably misses raw damage");
       else check(limited.getItemModel(raw)==itemBakes.get(i),"old finite map control valid metadata");
      }
     }
     mesher.rebuildCache();
     check(mesher.getItemModel(new ItemStack(Item.getItemFromBlock(blocks[0]),1,32767))==itemBakes.get(0),"mesh survives actual native cache rebuild");
     System.out.println("PASS native Forge mesher raw-edge ownership and missing-model negative control, reload "+reload);
    }
   }
   atlas.deleteGlTexture();
  }
  System.out.println("PASS "+checks+" native cube checks; production loader/helper, 9 exact constructor chains, actual native model/atlas/reload and original RenderBlocks bytecode. Gameplay owner bodies are deliberately not compiled in this bounded constructor fixture.");
 }
}
