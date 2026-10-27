import java.io.*;import java.lang.reflect.*;import java.util.*;import java.nio.*;
import com.hbm.blocks.ModBlocks;import com.hbm.blocks.generic.BarbedWire;import com.hbm.main.BarbedWireObjModelHandler;import com.hbm.render.icon.LegacyIconRegister;
import net.minecraft.client.Minecraft;import net.minecraft.init.Bootstrap;import net.minecraft.client.resources.*;import net.minecraft.client.resources.data.MetadataSerializer;import net.minecraft.client.renderer.texture.*;import net.minecraft.client.renderer.block.model.*;import net.minecraft.client.renderer.*;import net.minecraft.util.*;import net.minecraft.util.registry.RegistrySimple;import net.minecraft.item.*;import net.minecraft.block.state.IBlockState;import net.minecraftforge.client.model.*;import net.minecraftforge.client.model.obj.OBJModel;import net.minecraftforge.client.event.*;
import org.lwjgl.opengl.GL11;import org.lwjgl.BufferUtils;
public class BarbedResourceTest {
 static int checks;static void check(boolean b,String s){checks++;if(!b)throw new AssertionError(s);}
 static void set(Object o,Class<?> c,String n,Object v)throws Exception{Field f=c.getDeclaredField(n);f.setAccessible(true);f.set(o,v);}
 static Object get(Object o,Class<?> c,String n)throws Exception{Field f=c.getDeclaredField(n);f.setAccessible(true);return f.get(o);}
 static BarbedWire[] blocks(){return new BarbedWire[]{(BarbedWire)ModBlocks.barbed_wire,(BarbedWire)ModBlocks.barbed_wire_fire,(BarbedWire)ModBlocks.barbed_wire_poison,(BarbedWire)ModBlocks.barbed_wire_acid,(BarbedWire)ModBlocks.barbed_wire_wither,(BarbedWire)ModBlocks.barbed_wire_ultradeath};}
 static SimpleReloadableResourceManager resources=new SimpleReloadableResourceManager(new MetadataSerializer());
 static int originalWorldColor(BakedQuad q){
  int[] d=q.getVertexData();check(q.getFormat().equals(net.minecraft.client.renderer.vertex.DefaultVertexFormats.BLOCK)&&d.length==28,"world quad native BLOCK format");
  float x0=Float.intBitsToFloat(d[0]),y0=Float.intBitsToFloat(d[1]),z0=Float.intBitsToFloat(d[2]);
  float x1=Float.intBitsToFloat(d[7]),y1=Float.intBitsToFloat(d[8]),z1=Float.intBitsToFloat(d[9]);
  float x2=Float.intBitsToFloat(d[14]),y2=Float.intBitsToFloat(d[15]),z2=Float.intBitsToFloat(d[16]);
  float ax=x1-x0,ay=y1-y0,az=z1-z0,bx=x2-x0,by=y2-y0,bz=z2-z0;
  float nx=ay*bz-az*by,ny=az*bx-ax*bz,nz=ax*by-ay*bx,len=(float)Math.sqrt(nx*nx+ny*ny+nz*nz);
  if(len>0F){nx/=len;ny/=len;nz/=len;}else{EnumFacing f=q.getFace();nx=f==null?0:f.getXOffset();ny=f==null?0:f.getYOffset();nz=f==null?0:f.getZOffset();}
  float shade=(ny+.7F)*.9F-Math.abs(nx)*.1F+Math.abs(nz)*.1F;if(shade<.45F)shade=.45F;
  int c=Math.max(0,Math.min(255,(int)(shade*255F)));return 0xff000000|c<<16|c<<8|c;
 }
 static void worldColors(IBakedModel model,String label){
  int saturated=0;for(BakedQuad q:model.getQuads(null,null,0)){int expected=originalWorldColor(q);if(expected==0xffffffff)saturated++;for(int v=0;v<4;v++)check(q.getVertexData()[v*7+3]==expected,label+" legacy saturated world color");}
  check(saturated>0,label+" exercises over-one legacy shade saturation");
 }
 static final net.minecraft.util.math.BlockPos LIGHT_POS=new net.minecraft.util.math.BlockPos(11,64,13);
 static final class LightingWorld implements net.minecraft.world.IBlockAccess {
  final IBlockState state;final int own=0x00F000F0;LightingWorld(IBlockState state){this.state=state;}
  public net.minecraft.tileentity.TileEntity getTileEntity(net.minecraft.util.math.BlockPos p){return null;}
  public int getCombinedLight(net.minecraft.util.math.BlockPos p,int emission){return p.equals(LIGHT_POS)?own:0x00100010*((Math.abs(p.getX()+2*p.getY()+3*p.getZ())%8)+1);}
  public IBlockState getBlockState(net.minecraft.util.math.BlockPos p){return p.equals(LIGHT_POS)?state:net.minecraft.init.Blocks.STONE.getDefaultState();}
  public boolean isAirBlock(net.minecraft.util.math.BlockPos p){return false;}
  public net.minecraft.world.biome.Biome getBiome(net.minecraft.util.math.BlockPos p){return net.minecraft.init.Biomes.PLAINS;}
  public int getStrongPower(net.minecraft.util.math.BlockPos p,EnumFacing side){return 0;}
  public net.minecraft.world.WorldType getWorldType(){return net.minecraft.world.WorldType.DEFAULT;}
  public boolean isSideSolid(net.minecraft.util.math.BlockPos p,EnumFacing side,boolean fallback){return !p.equals(LIGHT_POS);}
 }
 static void nativeLighting(IBakedModel model,IBlockState state,String label){
  LightingWorld world=new LightingWorld(state);net.minecraft.client.renderer.color.BlockColors colors=new net.minecraft.client.renderer.color.BlockColors();List<BakedQuad> quads=model.getQuads(state,null,0);
  for(boolean forge:new boolean[]{false,true}){
   BufferBuilder out=new BufferBuilder(65536);out.begin(7,net.minecraft.client.renderer.vertex.DefaultVertexFormats.BLOCK);
   if(forge){net.minecraftforge.client.model.pipeline.VertexLighterFlat lighter=new net.minecraftforge.client.model.pipeline.VertexLighterFlat(colors);net.minecraftforge.client.model.pipeline.VertexBufferConsumer consumer=new net.minecraftforge.client.model.pipeline.VertexBufferConsumer(out);consumer.setOffset(LIGHT_POS);lighter.setParent(consumer);lighter.setWorld(world);lighter.setState(state);lighter.setBlockPos(LIGHT_POS);lighter.updateBlockInfo();for(BakedQuad q:quads)q.pipe(lighter);lighter.resetBlockInfo();}
   else check(new net.minecraft.client.renderer.BlockModelRenderer(colors).renderModelFlat(world,model,state,LIGHT_POS,out,true,0),label+" native vanilla emits model");
   out.finishDrawing();check(out.getVertexCount()==quads.size()*4,label+" native emitted vertices "+forge);ByteBuffer data=out.getByteBuffer();
   for(int v=0;v<out.getVertexCount();v++){check(data.getInt(v*28+24)==world.own,label+" "+(forge?"Forge":"vanilla")+" samples owner packed light");check(data.getInt(v*28+12)==quads.get(v/4).getVertexData()[v%4*7+3],label+" "+(forge?"Forge":"vanilla")+" preserves pre-shaded color");}
  }
 }
 static long[] run(IResourcePack pack)throws Exception {
  set(Minecraft.getMinecraft(),Minecraft.class,"resourceManager",resources);resources.reloadResources(Collections.singletonList(pack));
  TextureMap atlas=new TextureMap("textures");atlas.setMipmapLevels(2);set(Minecraft.getMinecraft(),Minecraft.class,"textureMapBlocks",atlas);
  ModelManager manager=new ModelManager(atlas);BlockModelShapes shapes=new BlockModelShapes(manager);ModelLoader loader=new ModelLoader(resources,atlas,shapes);BarbedWireObjModelHandler handler=new BarbedWireObjModelHandler();handler.onModelRegistry(new ModelRegistryEvent());Class<?> routeType=Class.forName("com.hbm.main.BarbedWireObjModelHandler$StateModelLoader");Field singleton=routeType.getDeclaredField("INSTANCE");singleton.setAccessible(true);ICustomModelLoader own=(ICustomModelLoader)singleton.get(null);check(own.accepts(new ResourceLocation("hbm:models/block/barbed_wire")),"exact owned native resolved loader request");for(String other:new String[]{"other:models/block/barbed_wire","hbm:models/block/barbed_wire_extra","hbm:models/blocks/barbed_wire.obj","hbm:models/block/fence_metal"})check(!own.accepts(new ResourceLocation(other)),"unknown route not intercepted "+other);
  resources.reloadResources(Collections.singletonList(pack));ModelLoaderRegistry.clearModelCache(resources);
  for(BarbedWire block:blocks())block.registerBlockIcons(new LegacyIconRegister(atlas));
  Method missing=TextureMap.class.getDeclaredMethod("initMissingImage");missing.setAccessible(true);missing.invoke(atlas);atlas.loadTextureAtlas(resources);
  GlStateManager.bindTexture(atlas.getGlTextureId());int width=GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D,0,GL11.GL_TEXTURE_WIDTH),height=GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D,0,GL11.GL_TEXTURE_HEIGHT);IntBuffer pixels=BufferUtils.createIntBuffer(width*height);GL11.glGetTexImage(GL11.GL_TEXTURE_2D,0,32993,33639,pixels);
  for(BarbedWire block:blocks()){
   String name=block.getRegistryName().getPath();check(name.startsWith("tile.barbed_wire"),"unchanged registry "+name);String texture="hbm:blocks/"+name.substring(5)+"_model";TextureAtlasSprite sprite=atlas.getAtlasSprite(texture);check(sprite!=atlas.getMissingSprite(),"real stitched original sprite "+texture);check(block.getIcon(0,0).getIconName().equals(texture),"actual factory/owner texture");
   int[] frame=sprite.getFrameTextureData(0)[0];for(int y=0;y<sprite.getIconHeight();y++)for(int x=0;x<sprite.getIconWidth();x++)check(frame[y*sprite.getIconWidth()+x]==pixels.get((y+sprite.getOriginY())*width+x+sprite.getOriginX()),"GL original sprite pixel");
  }
  ModelLoader.onRegisterAllBlocks(shapes);
  for(BarbedWire block:blocks()){
   Map<IBlockState,ModelResourceLocation> mapping=shapes.getBlockStateMapper().getVariants(block);check(mapping.size()==6,"six native orientation states");
   for(int n=0;n<7;n++){
    ModelResourceLocation route=n==6?new ModelResourceLocation(block.getRegistryName(),"inventory"):mapping.get(block.getStateFromMeta(n));
    IModel model=ModelLoaderRegistry.getModel(route);check(model!=ModelLoaderRegistry.getMissingModel(),"native state/inventory JSON route "+route);
    IBakedModel baked=model.bake(model.getDefaultState(),net.minecraft.client.renderer.vertex.DefaultVertexFormats.BLOCK,ModelLoader.defaultTextureGetter());check(!baked.getQuads(null,null,0).isEmpty(),"real OBJ geometry from JSON "+route);
    for(BakedQuad q:baked.getQuads(null,null,0))check(q.getSprite().getIconName().equals(block.getIcon(0,0).getIconName()),"JSON original material "+route+" actual="+q.getSprite().getIconName());
   }
  }
  RegistrySimple<ModelResourceLocation,IBakedModel> registry=new RegistrySimple<>();handler.onModelBake(new ModelBakeEvent(manager,registry,loader));
  long geometry=1,pixelHash=1;
  for(BarbedWire block:blocks())for(int n=0;n<7;n++){
   ModelResourceLocation id=new ModelResourceLocation(block.getRegistryName(),n==6?"inventory":"orientation="+n);IBakedModel baked=registry.getObject(id);check(baked!=null,"actual handler publication "+id);List<BakedQuad> quads=baked.getQuads(null,null,0);check(!quads.isEmpty(),"actual original transformed geometry");for(BakedQuad q:quads)for(int v=0;v<4;v++)geometry=geometry*31+q.getVertexData()[v*7];for(BakedQuad q:quads)check(q.getSprite()==atlas.getAtlasSprite(block.getIcon(0,0).getIconName()),"handler original material");
  }
  BarbedWire base=(BarbedWire)ModBlocks.barbed_wire;for(int n=0;n<6;n++){IBakedModel world=registry.getObject(new ModelResourceLocation(base.getRegistryName(),"orientation="+n));worldColors(world,"barbed orientation="+n);if(n==0)nativeLighting(world,base.getStateFromMeta(n),"barbed orientation=0");}
  set(manager,ModelManager.class,"modelRegistry",registry);ItemModelMesher mesher=new ItemModelMesher(manager);ModelLoader.onRegisterItems(mesher);mesher.rebuildCache();for(BarbedWire block:blocks())for(int n=0;n<6;n++)check(mesher.getItemModel(new ItemStack(Item.getItemFromBlock(block),1,n))==registry.getObject(new ModelResourceLocation(block.getRegistryName(),"inventory")),"native item model dispatch");
  for(BarbedWire b:blocks())for(int pixel:atlas.getAtlasSprite(b.getIcon(0,0).getIconName()).getFrameTextureData(0)[0])pixelHash=pixelHash*31+pixel;
  check(GL11.glGetError()==GL11.GL_NO_ERROR,"native GL error-free");atlas.deleteGlTexture();System.out.println("PASS native reload/atlas upload/OBJ state and item bake "+pack.getPackName());return new long[]{geometry,pixelHash};
 }
 static final class ChangedPack implements IResourcePack {
  final IResourcePack source;ChangedPack(IResourcePack source){this.source=source;}
  public InputStream getInputStream(ResourceLocation r)throws IOException {
   try(InputStream in=source.getInputStream(r)){
    ByteArrayOutputStream out=new ByteArrayOutputStream();byte[] buffer=new byte[4096];int n;while((n=in.read(buffer))>=0)out.write(buffer,0,n);byte[] data=out.toByteArray();
    if(r.getPath().endsWith("barbed_wire.obj"))return new ByteArrayInputStream(new String(data,"UTF-8").replace("v -0.500000", "v -0.375000").getBytes("UTF-8"));
    if(r.getPath().endsWith("_model.png")){java.awt.image.BufferedImage image=javax.imageio.ImageIO.read(new ByteArrayInputStream(data));image.setRGB(0,0,0xff123456);out.reset();javax.imageio.ImageIO.write(image,"png",out);data=out.toByteArray();}
    return new ByteArrayInputStream(data);
   }
  }
  public boolean resourceExists(ResourceLocation r){return source.resourceExists(r);}public Set<String> getResourceDomains(){return source.getResourceDomains();}
  public <T extends net.minecraft.client.resources.data.IMetadataSection>T getPackMetadata(MetadataSerializer s,String n)throws IOException{return source.getPackMetadata(s,n);}public java.awt.image.BufferedImage getPackImage()throws IOException{return source.getPackImage();}public String getPackName(){return "changed original OBJ and PNG";}
 }
 public static void main(String[] a)throws Exception{
  Class<?> contextType=Class.forName("MaterialTextureAtlasSpriteNativeTest$Context");Constructor<?> constructor=contextType.getDeclaredConstructor();constructor.setAccessible(true);try(AutoCloseable context=(AutoCloseable)constructor.newInstance()){
   Method init=MaterialTextureAtlasSpriteNativeTest.class.getDeclaredMethod("initializeNativeClientSettings");init.setAccessible(true);init.invoke(null);Bootstrap.register();BarbedConstruction.initialize();net.minecraft.launchwrapper.Launch.blackboard=new HashMap<String,Object>();net.minecraft.launchwrapper.Launch.blackboard.put("fml.deobfuscatedEnvironment",true);net.minecraftforge.fml.common.asm.FMLSanityChecker.fmlLocation=new File(TextureMap.class.getProtectionDomain().getCodeSource().getLocation().toURI());
   IResourcePack folder=new FolderResourcePack(new File(a[0]));long[] original=run(folder);try(FileResourcePack jar=new FileResourcePack(new File(a[1]))){check(Arrays.equals(original,run(jar)),"folder/jar exact geometry/pixels");}
   long[] changed=run(new ChangedPack(folder));check(changed[0]!=original[0]&&changed[1]!=original[1],"same native manager reload invalidates OBJ and sprite data");check(Arrays.equals(original,run(folder)),"restored original resources restore geometry/pixels");
  }System.out.println("PASS "+checks+" native barbed resource assertions; offscreen real GL upload, no live lighting acceptance");
 }
}
