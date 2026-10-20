import java.io.*;
import java.lang.reflect.*;
import java.util.*;
import javax.vecmath.Matrix4f;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.opengl.GL11;
import com.hbm.blocks.generic.BlockDecoCRT;
import com.hbm.main.CRTModelHandler;
import com.hbm.render.block.nativeblock.CRTBakedModel;
import com.hbm.render.mesh.*;
import com.hbm.render.util.NativeMachineItemRenderer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.entity.RenderItemFrame;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.entity.item.*;
import net.minecraft.item.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

/** Genuine native EGL draws, current model mesher, real Forge frame event and original matrices. */
public final class CRTFrameNativeTest {
 static int checks; static BlockDecoCRT block;static ItemBlock item;static ObjMesh mesh;
 static final List<float[]> matrices=new ArrayList<>();
 static void check(boolean b,String s){checks++;if(!b)throw new AssertionError(s);}
 static void equal(float[] expected,float[] actual,String s){for(int i=0;i<16;i++)check(Math.abs(expected[i]-actual[i])<.000003F,s+" component "+i+": "+expected[i]+" != "+actual[i]);}
 static final class Recording implements IBakedModel {
  final IBakedModel delegate;
  Recording(IBakedModel model){delegate=model;}
  public List<BakedQuad> getQuads(IBlockState state,EnumFacing side,long seed){if(side==null)matrices.add(NativeMachineItemTest.matrix());return delegate.getQuads(state,side,seed);}
  public boolean isAmbientOcclusion(){return delegate.isAmbientOcclusion();}public boolean isGui3d(){return delegate.isGui3d();}public boolean isBuiltInRenderer(){return delegate.isBuiltInRenderer();}
  public TextureAtlasSprite getParticleTexture(){return delegate.getParticleTexture();}public ItemCameraTransforms getItemCameraTransforms(){return delegate.getItemCameraTransforms();}public ItemOverrideList getOverrides(){return delegate.getOverrides();}
  public Pair<? extends IBakedModel,Matrix4f> handlePerspective(TransformType type){Pair<? extends IBakedModel,Matrix4f> p=delegate.handlePerspective(type);return Pair.of(this,p.getRight());}
 }
 static void model(String resources,int variant)throws Exception {
  String name=new String[]{"crt_clean","crt_broken","crt_blinking","crt_bsod"}[variant];
  java.awt.image.BufferedImage image=javax.imageio.ImageIO.read(new File(resources,"assets/hbm/textures/blocks/"+name+".png"));
  // One original animation frame. Animation timing is covered by the resource test, not frozen here.
  int size=image.getWidth();DynamicTexture texture=new DynamicTexture(size,size);image.getRGB(0,0,size,size,texture.getTextureData(),0,size);texture.updateDynamicTexture();NativeMachineDrawTest.textures.loadTexture(TextureMap.LOCATION_BLOCKS_TEXTURE,texture);
  TextureAtlasSprite sprite=new TextureAtlasSprite("hbm:blocks/"+name){};sprite.setIconWidth(size);sprite.setIconHeight(size);sprite.initSprite(size,size,0,0,false);
  ModelResourceLocation key=new ModelResourceLocation("hbm:native_crt/tile.deco_crt/"+variant+"/item","inventory");
  NativeMachineDrawTest.models.putObject(key,new Recording(new CRTBakedModel(mesh,variant,true,sprite)));
  NativeMachineDrawTest.renderer.getItemModelMesher().register(item,variant,key);NativeMachineDrawTest.renderer.getItemModelMesher().rebuildCache();
 }
 static void reset(){NativeMachineDrawTest.reset();matrices.clear();GlStateManager.matrixMode(GL11.GL_PROJECTION);GlStateManager.loadIdentity();GL11.glOrtho(-1,1,-1,1,-100,100);GlStateManager.matrixMode(GL11.GL_MODELVIEW);GlStateManager.loadIdentity();}
 static void visible(){byte[] pixels=NativeMachineDrawTest.pixels();check(!Arrays.equals(pixels,new byte[pixels.length]),"shipped CRT geometry and original texture produce framebuffer pixels");}
 static void ordinaryFrames(String resources)throws Exception {
  RenderItemFrame renderer=new RenderItemFrame(Minecraft.getMinecraft().getRenderManager(),NativeMachineDrawTest.renderer);
  Method render=RenderItemFrame.class.getDeclaredMethod("renderItem",EntityItemFrame.class);render.setAccessible(true);
  Method set=EntityItemFrame.class.getDeclaredMethod("setDisplayedItemWithUpdate",ItemStack.class,boolean.class);set.setAccessible(true);
  Method rotate=EntityItemFrame.class.getDeclaredMethod("setRotation",int.class,boolean.class);rotate.setAccessible(true);
  float[][] offsets={{0,0},{-.16F,-.16F},{0,-.32F},{.16F,-.16F}};
  for(int variant=0;variant<4;variant++) {model(resources,variant);for(int k=0;k<4;k++) {
   EntityItemFrame frame=new EntityItemFrame(null);frame.setSilent(true);set.invoke(frame,new ItemStack(item,1,variant),false);rotate.invoke(frame,k*2,false);
   ItemStack stack=frame.getDisplayedItem();NBTTagCompound before=stack.writeToNBT(new NBTTagCompound());
   reset();render.invoke(renderer,frame);check(matrices.size()==1,"native frame emits one complete CRT model");float[] actual=matrices.get(0);visible();
   GL11.glLoadIdentity();GL11.glTranslatef(0,-.18F,.015625F);GL11.glRotatef(180,0,1,0);GL11.glRotatef(-90*k,0,0,1);GL11.glTranslatef(offsets[k][0],offsets[k][1],0);GL11.glTranslatef(0,.1F,0);GL11.glScalef(1.25F,1.25F,1.25F);GL11.glTranslatef(0,.05F,0);GL11.glRotatef(-90,0,1,0);GL11.glScalef(.25F,.25F,.25F);GL11.glTranslatef(-.5F,-.5F,-.5F);
   equal(NativeMachineItemTest.matrix(),actual,"original ordinary frame matrix, variant "+variant+", rotation "+k);
   check(before.equals(stack.writeToNBT(new NBTTagCompound())),"ordinary frame preserves item/count/damage/NBT");check(GL11.glGetInteger(GL11.GL_MODELVIEW_STACK_DEPTH)==1,"ordinary frame restores matrix stack");
  }}
 }
 static void unrelatedFrames()throws Exception {
  // Same drawable geometry, different item identity: listener must leave it alone.
  NativeMachineDrawTest.model(net.minecraft.init.Items.STICK,new Recording(new CRTBakedModel(mesh,0,true,NativeMachineDrawTest.icon.getParticleTexture())));
  RenderItemFrame renderer=new RenderItemFrame(Minecraft.getMinecraft().getRenderManager(),NativeMachineDrawTest.renderer);
  Method render=RenderItemFrame.class.getDeclaredMethod("renderItem",EntityItemFrame.class);render.setAccessible(true);
  Method set=EntityItemFrame.class.getDeclaredMethod("setDisplayedItemWithUpdate",ItemStack.class,boolean.class);set.setAccessible(true);
  EntityItemFrame frame=new EntityItemFrame(null);frame.setSilent(true);set.invoke(frame,new ItemStack(net.minecraft.init.Items.STICK),false);
  reset();render.invoke(renderer,frame);float[] with=matrices.get(0);byte[] pixels=NativeMachineDrawTest.pixels();
  MinecraftForge.EVENT_BUS.unregister(CRTModelHandler.class);
  try {reset();render.invoke(renderer,frame);equal(with,matrices.get(0),"unrelated native item frame matrix unchanged");check(Arrays.equals(pixels,NativeMachineDrawTest.pixels()),"unrelated native item frame pixels unchanged");}
  finally{MinecraftForge.EVENT_BUS.register(CRTModelHandler.class);}
 }
 static void machineFrames(String resources)throws Exception {
  for(int variant=0;variant<4;variant++) {model(resources,variant);for(int count:new int[]{1,2,5,6,20,21,40,41})for(NativeMachineItemRenderer.Display display:NativeMachineItemRenderer.Display.values()) {
   ItemStack stack=new ItemStack(item,count,variant);stack.setTagCompound(new NBTTagCompound());stack.getTagCompound().setString("proof","unchanged");EntityItem entity=new EntityItem(null,0,0,0,stack);entity.hoverStart=.75F;
   NBTTagCompound before=stack.writeToNBT(new NBTTagCompound());
   reset();new NativeMachineItemRenderer().renderFrame(entity,0,0,0,0,.375F,display);List<float[]> actual=new ArrayList<>(matrices);visible();
   boolean decoration=display==NativeMachineItemRenderer.Display.DECORATION_FRAME;int copies=decoration?1:count>40?5:count>20?4:count>5?3:count>1?2:1;
   check(actual.size()==copies,"original ordinary block frame count thresholds");Random random=new Random(187L);
   for(int i=0;i<copies;i++) {
    GL11.glLoadIdentity();float bob=decoration?0:net.minecraft.util.math.MathHelper.sin((entity.getAge()+.375F)/10F+entity.hoverStart)*.1F+.1F;GL11.glTranslatef(0,bob,0);GL11.glRotatef(((entity.getAge()+.375F)/20F+entity.hoverStart)*57.295776F,0,1,0);GL11.glScalef(1.25F,1.25F,1.25F);GL11.glTranslatef(0,.05F,0);GL11.glRotatef(-90,0,1,0);GL11.glScalef(.25F,.25F,.25F);GL11.glTranslatef(-.5F,-.5F,-.5F);
    if(i>0)GL11.glTranslatef((random.nextFloat()*2F-1F)*.2F/.25F,(random.nextFloat()*2F-1F)*.2F/.25F,(random.nextFloat()*2F-1F)*.2F/.25F);
    equal(NativeMachineItemTest.matrix(),actual.get(i),"original ordinary machine frame "+display+" copy "+i);
   }
   check(before.equals(stack.writeToNBT(new NBTTagCompound())),"machine frame preserves item/count/damage/NBT");check(NativeMachineDrawTest.textures.glints==0,"ordinary block machine frame has no generic glint");check(GL11.glGetInteger(GL11.GL_MODELVIEW_STACK_DEPTH)==1,"machine frame restores matrix stack");
  }}
 }
 public static void main(String[] args)throws Exception {
  Constructor<?> constructor=Class.forName("MaterialTextureAtlasSpriteNativeTest$Context").getDeclaredConstructor();constructor.setAccessible(true);
  try(AutoCloseable context=(AutoCloseable)constructor.newInstance()) {
   Method init=MaterialTextureAtlasSpriteNativeTest.class.getDeclaredMethod("initializeNativeClientSettings");init.setAccessible(true);init.invoke(null);NativeMachineDrawTest.setup();
   net.minecraftforge.fml.common.ModMetadata md=new net.minecraftforge.fml.common.ModMetadata();md.modId="hbm";md.name="CRT frame proof";md.version="test";net.minecraftforge.fml.common.DummyModContainer container=new net.minecraftforge.fml.common.DummyModContainer(md);net.minecraftforge.fml.common.Loader.instance().setupTestHarness(container);net.minecraftforge.fml.common.Loader.instance().setActiveModContainer(container);
   block=new BlockDecoCRT(Material.IRON);block.setRegistryName("hbm","tile.deco_crt");ForgeRegistries.BLOCKS.register(block);item=new ItemBlock(block);item.setHasSubtypes(true);item.setRegistryName(block.getRegistryName());ForgeRegistries.ITEMS.register(item);
   try(Reader reader=new FileReader(new File(args[0],"assets/hbm/models/blocks/crt.obj"))){mesh=ObjMeshParser.parse(reader,"crt",false);}
   MinecraftForge.EVENT_BUS.register(CRTModelHandler.class);
   try{MinecraftForge.EVENT_BUS.post(new ModelRegistryEvent());ordinaryFrames(args[0]);machineFrames(args[0]);unrelatedFrames();}
   finally{MinecraftForge.EVENT_BUS.unregister(CRTModelHandler.class);}
   check(GL11.glGetError()==0,"native CRT draws produce no GL errors");NativeMachineDrawTest.framebuffer.deleteFramebuffer();
  }
  System.out.println("PASS "+checks+" genuine native CRT ordinary/machine frame matrix, copy, pixel, source identity and state checks");
 }
}
