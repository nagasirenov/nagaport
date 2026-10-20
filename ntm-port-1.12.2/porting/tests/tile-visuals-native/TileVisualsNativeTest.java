import java.io.*;
import java.lang.reflect.*;
import java.nio.*;
import java.util.*;
import java.util.zip.ZipFile;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import com.hbm.blocks.*;
import com.hbm.main.ResourceManager;
import com.hbm.render.loader.*;
import com.hbm.render.tileentity.*;
import com.hbm.render.item.nativeitem.context.NativeProjectionScope;
import com.hbm.tileentity.machine.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.client.renderer.tileentity.*;
import net.minecraft.client.renderer.vertex.*;
import net.minecraft.client.resources.*;
import net.minecraft.client.resources.data.MetadataSerializer;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.init.Bootstrap;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.*;
import tileoracle.*;

/** Real native dispatcher, Tessellator, BufferBuilder, GL and production HFR VBO.
 * Explicit NTM registry/tile storage fixtures exclude machine construction/ticking/network gameplay. */
public final class TileVisualsNativeTest {
 static long checks,vertices,pixelChecks;static Minecraft mc;static Framebuffer framebuffer;static TextureMap atlas;
 static List<Batch> batches=new ArrayList<>();static boolean capture=true;static int draws;
 static void check(boolean b,String s){checks++;if(!b)throw new AssertionError(s);}
 static Field field(Class<?> c,String name)throws Exception{Field f=c.getDeclaredField(name);f.setAccessible(true);return f;}
 static void set(Object o,String name,Object v)throws Exception{field(o.getClass(),name).set(o,v);}
 static float[] matrix(){FloatBuffer b=BufferUtils.createFloatBuffer(16);GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX,b);float[] v=new float[16];b.get(v);return v;}
 static float[] color(){FloatBuffer b=BufferUtils.createFloatBuffer(16);GL11.glGetFloat(GL11.GL_CURRENT_COLOR,b);return new float[]{b.get(0),b.get(1),b.get(2),b.get(3)};}
 static final class Batch {float[][] v;float[] m=matrix(),c=color();int texture=GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);Batch(float[][] v){this.v=v;}}
 static final class RecordingTessellator extends Tessellator {
  RecordingTessellator(){super(2097152);}
  @Override public void draw(){draws++;if(capture){BufferBuilder b=getBuffer();ByteBuffer bytes=b.getByteBuffer();VertexFormat f=b.getVertexFormat();int stride=f.getSize();float[][] v=new float[b.getVertexCount()][8];for(int i=0;i<v.length;i++){int o=i*stride;for(int j=0;j<3;j++)v[i][j]=bytes.getFloat(o+j*4);if(f.hasUvOffset(0))for(int j=0;j<2;j++)v[i][j+3]=bytes.getFloat(o+f.getUvOffsetById(0)+j*4);if(f.hasNormal())for(int j=0;j<3;j++)v[i][j+5]=bytes.get(o+f.getNormalOffset()+j);}batches.add(new Batch(v));}super.draw();}
 }
 static void captureLegacy(){draws++;if(!capture)return;int count=(Integer)LegacyTess.field("g");int[] raw=(int[])LegacyTess.field("f");boolean normal=(Boolean)LegacyTess.field("o");float[][] v=new float[count][8];for(int i=0;i<count;i++){for(int j=0;j<5;j++)v[i][j]=Float.intBitsToFloat(raw[i*8+j]);if(normal)for(int j=0;j<3;j++)v[i][j+5]=(byte)(raw[i*8+6]>>(j*8));}batches.add(new Batch(v));}
 static void compare(List<Batch> expected,List<Batch> actual,String name){check(expected.size()==actual.size(),name+" batch count "+expected.size()+"/"+actual.size());for(int i=0;i<expected.size();i++){Batch a=expected.get(i),b=actual.get(i);check(Arrays.equals(a.m,b.m),name+" matrix "+i);check(Arrays.equals(a.c,b.c),name+" GL color "+i);check(a.texture==b.texture,name+" material "+i);check(a.v.length==b.v.length,name+" vertices/batch");for(int j=0;j<a.v.length;j++){vertices++;for(int k=0;k<8;k++)check(Float.floatToIntBits(a.v[j][k])==Float.floatToIntBits(b.v[j][k]),name+" vertex "+i+":"+j+":"+k+" "+a.v[j][k]+" != "+b.v[j][k]);}}}
 static final class State {float[] matrix=matrix(),color=color();int depth=GL11.glGetInteger(GL11.GL_MODELVIEW_STACK_DEPTH),shade=GL11.glGetInteger(GL11.GL_SHADE_MODEL);boolean alpha=GL11.glIsEnabled(GL11.GL_ALPHA_TEST),blend=GL11.glIsEnabled(GL11.GL_BLEND),cull=GL11.glIsEnabled(GL11.GL_CULL_FACE),lighting=GL11.glIsEnabled(GL11.GL_LIGHTING);void same(){check(Arrays.equals(matrix,matrix()),"matrix restored");check(Arrays.equals(color,color()),"current color restored");check(depth==GL11.glGetInteger(GL11.GL_MODELVIEW_STACK_DEPTH),"matrix stack restored");check(shade==GL11.glGetInteger(GL11.GL_SHADE_MODEL),"shade restored");check(alpha==GL11.glIsEnabled(GL11.GL_ALPHA_TEST)&&blend==GL11.glIsEnabled(GL11.GL_BLEND)&&cull==GL11.glIsEnabled(GL11.GL_CULL_FACE)&&lighting==GL11.glIsEnabled(GL11.GL_LIGHTING),"enable flags restored");}}
 static void reset(int view){framebuffer.bindFramebuffer(true);GL11.glViewport(0,0,96,96);GlStateManager.matrixMode(GL11.GL_PROJECTION);GlStateManager.loadIdentity();GL11.glOrtho(-10,10,-10,10,-100,100);GlStateManager.matrixMode(GL11.GL_MODELVIEW);GlStateManager.loadIdentity();GL11.glRotatef(25+view*17,1,0,0);GL11.glRotatef(30+view*33,0,1,0);GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);GlStateManager.enableTexture2D();GlStateManager.disableLighting();GlStateManager.disableCull();GlStateManager.disableBlend();GlStateManager.enableAlpha();GlStateManager.enableDepth();GlStateManager.depthMask(true);GlStateManager.depthFunc(GL11.GL_LEQUAL);GlStateManager.color(1,1,1,1);GlStateManager.shadeModel(GL11.GL_FLAT);GlStateManager.clearColor(0,0,0,0);GlStateManager.clear(GL11.GL_COLOR_BUFFER_BIT|GL11.GL_DEPTH_BUFFER_BIT);batches=new ArrayList<>();draws=0;ResourceManager.magma_drill.calls.clear();ResourceManager.magma_drill.matrices.clear();}
 static byte[] pixels(){ByteBuffer b=BufferUtils.createByteBuffer(96*96*4);GL11.glReadPixels(0,0,96,96,GL11.GL_RGBA,GL11.GL_UNSIGNED_BYTE,b);byte[] v=new byte[b.capacity()];b.get(v);return v;}
 static void pixelsEqual(byte[] a,byte[] b,String label){check(a.length==b.length,label+" length");for(int i=0;i<a.length;i++){pixelChecks++;check(a[i]==b[i],label+" pixel byte "+i);}check(!Arrays.equals(a,new byte[a.length]),label+" actually emits pixels");}
 static void setup(String resources)throws Exception{
  Bootstrap.register();mc=Minecraft.getMinecraft();OpenGlHelper.initializeTextures();
  SimpleReloadableResourceManager manager=new SimpleReloadableResourceManager(new MetadataSerializer());
  File nativeJar=new File(TextureMap.class.getProtectionDomain().getCodeSource().getLocation().toURI());manager.reloadResourcePack(new FileResourcePack(nativeJar));manager.reloadResourcePack(new FolderResourcePack(new File(resources)));set(mc,"resourceManager",manager);
  mc.renderEngine=new TextureManager(manager);atlas=new TextureMap("textures");set(mc,"textureMapBlocks",atlas);
  DynamicTexture lava=new DynamicTexture(16,16);try(IResource r=manager.getResource(new ResourceLocation("minecraft","textures/blocks/lava_still.png"))){BufferedImage image=ImageIO.read(r.getInputStream());image.getRGB(0,0,16,16,lava.getTextureData(),0,16);}lava.updateDynamicTexture();mc.renderEngine.loadTexture(TextureMap.LOCATION_BLOCKS_TEXTURE,lava);
  sprite(16,16,0,0);framebuffer=new Framebuffer(96,96,true);
  TileEntityRendererDispatcher.instance.renderEngine=mc.renderEngine;
  for(Field f:Tessellator.class.getDeclaredFields())if(Modifier.isStatic(f.getModifiers())&&f.getType()==Tessellator.class){f.setAccessible(true);field(Field.class,"modifiers").setInt(f,f.getModifiers()&~Modifier.FINAL);f.set(null,new RecordingTessellator());}
  Bindings.bind();
  check(TileEntityRendererDispatcher.instance.getRenderer(TileEntityMachineMagma.class).getClass()==RenderMagma.class,"real registered magma callback");
  check(TileEntityRendererDispatcher.instance.getRenderer(TileEntityMultiblock.class).getClass()==RenderMultiblock.class,"real registered multiblock callback");
  check(TileEntityRendererDispatcher.instance.getRenderer(TileEntitySoyuzStruct.class).getClass()==RenderSoyuzMultiblock.class,"real registered Soyuz callback");
 }
 static TextureAtlasSprite sprite(int width,int height,int x,int y)throws Exception{
  TextureAtlasSprite s=new TextureAtlasSprite("minecraft:blocks/lava_still"){};s.setIconWidth(16);s.setIconHeight(16);s.initSprite(width,height,x,y,false);
  ((Map<String,TextureAtlasSprite>)field(TextureMap.class,"mapUploadedSprites").get(atlas)).put(s.getIconName(),s);return s;
 }
 static TileEntitySpecialRenderer bind(TileEntitySpecialRenderer r){r.setRendererDispatcher(TileEntityRendererDispatcher.instance);return r;}
 static void dispatch(TileEntity tile,double x,double y,double z,float partial,int damage,float alpha){TileEntityRendererDispatcher.instance.render(tile,x,y,z,partial,damage,alpha);}
 static void materialParity(String legacy)throws Exception{
  try(ZipFile old=new ZipFile(legacy);IResource r=mc.getResourceManager().getResource(new ResourceLocation("minecraft","textures/blocks/lava_still.png"));IResource meta=mc.getResourceManager().getResource(new ResourceLocation("minecraft","textures/blocks/lava_still.png.mcmeta"))){
   BufferedImage a=ImageIO.read(old.getInputStream(old.getEntry("assets/minecraft/textures/blocks/lava_still.png"))),b=ImageIO.read(r.getInputStream());check(a.getWidth()==b.getWidth()&&a.getHeight()==b.getHeight(),"original/native full lava strip dimensions");for(int y=0;y<a.getHeight();y++)for(int x=0;x<a.getWidth();x++)check(a.getRGB(x,y)==b.getRGB(x,y),"every original/native animated lava pixel");
   com.google.gson.JsonParser p=new com.google.gson.JsonParser();check(p.parse(new InputStreamReader(old.getInputStream(old.getEntry("assets/minecraft/textures/blocks/lava_still.png.mcmeta")))).equals(p.parse(new InputStreamReader(meta.getInputStream()))),"entire original/native lava animation frame/time metadata");
  }
 }
 static void magma()throws Exception{
  OriginalRenderMagma old=(OriginalRenderMagma)bind(new OriginalRenderMagma());TileEntityMachineMagma tile=new TileEntityMachineMagma();
  float[][] values={{0,0,0,0},{-29,173,0,.02F},{359,1,.01F,.01F},{120,-20,.010001F,.8F},{1,3,.8F,2F}};
  for(int atlasMode=0;atlasMode<2;atlasMode++){sprite(atlasMode==0?16:256,atlasMode==0?16:256,atlasMode==0?0:32,atlasMode==0?0:80);
   for(int meta=0;meta<16;meta++)for(float partial:new float[]{0,.25F,1})for(float[] v:values){tile.metadata=meta;tile.prevDrillRotation=v[0];tile.drillRotation=v[1];tile.prevLavaHeight=v[2];tile.lavaHeight=v[3];
    reset(0);try(NativeProjectionScope s=new NativeProjectionScope()){old.renderTileEntityAt(tile,.125,-.5,1.75,partial);}List<Batch> expected=batches;List<String> parts=new ArrayList<>(ResourceManager.magma_drill.calls);List<float[]> transforms=new ArrayList<>(ResourceManager.magma_drill.matrices);
    reset(0);State prior=new State();dispatch(tile,.125,-.5,1.75,partial,(meta%11)-1,.17F);prior.same();compare(expected,batches,"magma "+meta+" "+partial);check(parts.equals(ResourceManager.magma_drill.calls)&&parts.equals(Arrays.asList("except[DrillHead, Blades]","DrillHead","Blades")),"exact magma group selection and order");for(int i=0;i<3;i++)check(Arrays.equals(transforms.get(i),ResourceManager.magma_drill.matrices.get(i)),"original interpolated opposed drill rotation and height offset");check(draws==((v[2]+(v[3]-v[2])*partial)>.01F?1:0),"original strict lava visibility threshold");
   }
  }
  sprite(16,16,0,0);
  for(int height:new int[]{-2,0,3,7}){((BlockDummyable)ModBlocks.machine_magma).height=height;tile.metadata=15;tile.prevDrillRotation=-32;tile.drillRotation=46;tile.prevLavaHeight=.7F;tile.lavaHeight=1.5F;
   reset(0);try(NativeProjectionScope scope=new NativeProjectionScope()){old.renderTileEntityAt(tile,-2.25,1.75,3.5,.7F);}List<Batch> expected=batches;List<float[]> matrices=new ArrayList<>(ResourceManager.magma_drill.matrices);
   reset(0);dispatch(tile,-2.25,1.75,3.5,.7F,-1,1);compare(expected,batches,"height offset "+height);for(int i=0;i<3;i++)check(Arrays.equals(matrices.get(i),ResourceManager.magma_drill.matrices.get(i)),"height offset applied to actual selected model groups");
  }((BlockDummyable)ModBlocks.machine_magma).height=3;
  // Exception unwinding uses production lexical ownership; no tile/mesh success is fabricated.
  reset(0);State prior=new State();ResourceManager.magma_drill.fail=true;boolean failed=false;try{((RenderMagma)TileEntityRendererDispatcher.instance.<TileEntityMachineMagma>getRenderer(TileEntityMachineMagma.class)).render(tile,2,3,4,.4F,-1,1);}catch(IllegalStateException expected){failed=true;}finally{ResourceManager.magma_drill.fail=false;}check(failed,"model failure propagated");prior.same();
  // Actual original model goes through the unchanged native HFR VBO backend in both
  // callbacks. This independently compares each complete TESR transform/material/lava pass.
  HFRWavefrontObjectVBO model=new HFRWavefrontObject(new ResourceLocation("hbm","models/machines/magma_drill.obj")).asVBO();ResourceManager.magma_drill.delegate=model;
  check(model.getPartNames().contains("Drillhead")&&model.getPartNames().contains("Blades"),"actual original moving groups loaded");
  for(int view=0;view<4;view++)for(int meta:new int[]{12,13,14,15})for(float level:new float[]{0,.7F}){tile.metadata=meta;tile.prevDrillRotation=17;tile.drillRotation=127;tile.prevLavaHeight=level;tile.lavaHeight=level;reset(view);try(NativeProjectionScope s=new NativeProjectionScope()){old.renderTileEntityAt(tile,0,3,0,.37F);}byte[] expected=pixels();reset(view);dispatch(tile,0,3,0,.37F,-1,1);pixelsEqual(expected,pixels(),"actual magma VBO/framebuffer "+view+":"+meta+":"+level);}
  model.destroy();ResourceManager.magma_drill.delegate=null;
 }
 static void previews()throws Exception{
  OriginalRenderMultiblock old=(OriginalRenderMultiblock)bind(new OriginalRenderMultiblock());TileEntityMultiblock tile=new TileEntityMultiblock();
  for(net.minecraft.block.Block block:new net.minecraft.block.Block[]{ModBlocks.struct_launcher_core,net.minecraft.init.Blocks.AIR})for(int view=0;view<4;view++){
   tile.block=block;reset(view);try(NativeProjectionScope s=new NativeProjectionScope()){old.renderTileEntityAt(tile,.25,-.1,.4,.5F);}List<Batch> expected=batches;byte[] rgba=pixels();reset(view);State prior=new State();dispatch(tile,.25,-.1,.4,.5F,view-1,.23F);prior.same();compare(expected,batches,"compact/unowned");if(block==ModBlocks.struct_launcher_core){check(draws==8*6,"all eight compact preview cubes");pixelsEqual(rgba,pixels(),"compact framebuffer");}else check(draws==0,"unowned block no mesh");
  }
  tile.block=ModBlocks.struct_launcher_core_large;
  Set<Integer> phases=new HashSet<>();while(phases.size()<4){long before=System.currentTimeMillis();int phase=(int)(before%4000/1000);if(phases.contains(phase)){Thread.sleep(100);continue;}if(before%1000>750){Thread.sleep(260);continue;}reset(1);try(NativeProjectionScope s=new NativeProjectionScope()){old.renderTileEntityAt(tile,0,-3,0,.5F);}List<Batch> expected=batches;byte[] rgba=pixels();reset(1);State prior=new State();dispatch(tile,0,-3,0,.5F,phase,1);long after=System.currentTimeMillis();prior.same();if((int)(after%4000/1000)!=phase)continue;compare(expected,batches,"large phase "+phase);check(draws==(80+11)*6,"exact launch-table pad+scaffold count");pixelsEqual(rgba,pixels(),"launch table phase "+phase);phases.add(phase);if(phases.size()<4)Thread.sleep(250);}
  OriginalRenderSoyuzMultiblock soyuz=(OriginalRenderSoyuzMultiblock)bind(new OriginalRenderSoyuzMultiblock());TileEntitySoyuzStruct st=new TileEntitySoyuzStruct();
  for(int view=0;view<3;view++){reset(view);GL11.glScaled(.3,.3,.3);GL11.glTranslated(0,-25,0);try(NativeProjectionScope s=new NativeProjectionScope()){soyuz.renderTileEntityAt(st,.25,.5,-.3,.875F);}List<Batch> expected=batches;byte[] rgba=pixels();reset(view);GL11.glScaled(.3,.3,.3);GL11.glTranslated(0,-25,0);State prior=new State();dispatch(st,.25,.5,-.3,.875F,view+1,.1F);prior.same();compare(expected,batches,"all Soyuz previews");check(draws==1155*6,"all original Soyuz cubes");pixelsEqual(rgba,pixels(),"Soyuz framebuffer");}
 }
 static void previewFailures()throws Exception{
  TextureManager real=TileEntityRendererDispatcher.instance.renderEngine;
  TileEntityRendererDispatcher.instance.renderEngine=new TextureManager(mc.getResourceManager()){@Override public void bindTexture(ResourceLocation target){throw new IllegalStateException("controlled texture binding failure");}};
  try{
   TileEntityMultiblock t=new TileEntityMultiblock();t.block=ModBlocks.struct_launcher_core;
   for(TileEntity tile:new TileEntity[]{t,new TileEntitySoyuzStruct()}){reset(0);State prior=new State();boolean failed=false;try{((TileEntitySpecialRenderer)TileEntityRendererDispatcher.instance.getRenderer(tile)).render(tile,1,2,3,.25F,-1,1);}catch(IllegalStateException expected){failed=true;}check(failed,"preview texture failure propagated");prior.same();
    BufferBuilder b=Tessellator.getInstance().getBuffer();b.begin(GL11.GL_QUADS,DefaultVertexFormats.POSITION);b.finishDrawing();b.reset();check(true,"binding failure leaves native buffer available");
   }
  }finally{TileEntityRendererDispatcher.instance.renderEngine=real;}
 }
 static void stateCaches()throws Exception{
  TileEntityMultiblock tile=new TileEntityMultiblock();tile.block=ModBlocks.struct_launcher_core;
  for(int combination=0;combination<16;combination++){
   reset(0); // deliberately mix legacy raw GL and native cached state, as other TESRs do
   if((combination&1)!=0)GL11.glEnable(GL11.GL_BLEND);if((combination&2)!=0)GL11.glDisable(GL11.GL_ALPHA_TEST);if((combination&4)!=0)GL11.glEnable(GL11.GL_CULL_FACE);if((combination&8)!=0)GL11.glEnable(GL11.GL_LIGHTING);GL11.glColor4f(.2F,.4F,.6F,.8F);State before=new State();dispatch(tile,0,0,0,.5F,-1,1);before.same();
   GlStateManager.disableBlend();GlStateManager.enableAlpha();GlStateManager.disableCull();GlStateManager.disableLighting();GlStateManager.color(1,1,1,1);check(!GL11.glIsEnabled(GL11.GL_BLEND)&&GL11.glIsEnabled(GL11.GL_ALPHA_TEST)&&!GL11.glIsEnabled(GL11.GL_CULL_FACE)&&!GL11.glIsEnabled(GL11.GL_LIGHTING)&&Arrays.equals(color(),new float[]{1,1,1,1}),"native cache repaired after raw GL caller");
  }
 }
 public static void main(String[] args)throws Exception{
  Constructor<?> c=Class.forName("MaterialTextureAtlasSpriteNativeTest$Context").getDeclaredConstructor();c.setAccessible(true);
  try(AutoCloseable context=(AutoCloseable)c.newInstance()){
   Method init=MaterialTextureAtlasSpriteNativeTest.class.getDeclaredMethod("initializeNativeClientSettings");init.setAccessible(true);init.invoke(null);setup(args[1]);LegacyTess.initialize(args[0]);LegacyTess.capture=TileVisualsNativeTest::captureLegacy;LegacyBlocksRenderer.getInstance().initialize();materialParity(args[0]);magma();previews();previewFailures();stateCaches();check(GL11.glGetError()==GL11.GL_NO_ERROR,"all native/original draws leave no GL error");framebuffer.deleteFramebuffer();
   System.out.println("PASS "+checks+" assertions, "+vertices+" actual original Tessellator/native vertices, "+pixelChecks+" genuine GL pixel-byte comparisons. Whole TESRs registered and called by real native dispatcher; original texture/animation pixels, lava face bytecode, original layouts, animations, actual magma VBO, and native state restoration verified. NTM tile/registry fixtures explicitly exclude gameplay and loaded-client acceptance.");
  }
 }
}
