import com.hbm.render.util.*;
import com.google.common.collect.*;
import java.lang.reflect.*;
import java.nio.*;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.client.resources.*;
import net.minecraft.client.resources.data.MetadataSerializer;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.*;
import net.minecraft.item.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.util.registry.RegistrySimple;
import net.minecraftforge.client.model.*;
import org.lwjgl.opengl.GL11;

/** Genuine RenderItem/model mesher/texture manager/GL framebuffer draws, not a fake platform renderer. */
public class NativeMachineDrawTest {
 static int checks,baseSelections;
 static void check(boolean ok,String msg){checks++;if(!ok)throw new AssertionError(msg);}
 static void field(Object object,String name,Object value)throws Exception {Field f=object.getClass().getDeclaredField(name);f.setAccessible(true);f.set(object,value);}
 static final class Textures extends TextureManager {
  int glints;
  Textures(IResourceManager resources){super(resources);}
  @Override public void bindTexture(ResourceLocation location){if(location.getPath().contains("enchanted_item_glint"))glints++;super.bindTexture(location);}
 }
 static final class Renderer extends RenderItem {
  int gui,draws;final List<Float> guiDepths=new ArrayList<>();final List<float[]> matrices=new ArrayList<>();
  Renderer(TextureManager t,ModelManager m,ItemColors c){super(t,m,c);}
  @Override public void renderItemAndEffectIntoGUI(ItemStack stack,int x,int y){gui++;guiDepths.add(zLevel);super.renderItemAndEffectIntoGUI(stack,x,y);}
  @Override public void renderItem(ItemStack stack,IBakedModel model){draws++;matrices.add(NativeMachineItemTest.matrix());super.renderItem(stack,model);}
 }
 static class BaseOwner implements IBakedModel, NativeItemBaseModel {
  final IBakedModel base;BaseOwner(IBakedModel b){base=b;}
  public IBakedModel getBaseModel(){return base;}
  public List<BakedQuad> getQuads(net.minecraft.block.state.IBlockState state,EnumFacing face,long seed){return Collections.emptyList();}
  public boolean isAmbientOcclusion(){return false;}public boolean isGui3d(){return true;}public boolean isBuiltInRenderer(){return true;}
  public TextureAtlasSprite getParticleTexture(){return base.getParticleTexture();}public ItemCameraTransforms getItemCameraTransforms(){return ItemCameraTransforms.DEFAULT;}public ItemOverrideList getOverrides(){return ItemOverrideList.NONE;}
 }
 static final class SelectiveBaseOwner extends BaseOwner implements NativeMachineItemRenderer.FrameBaseModel {
  int selections;SelectiveBaseOwner(IBakedModel base){super(base);}
  public IBakedModel machineFrameModel(ItemStack stack){selections++;return base;}
 }
 static void selectiveFrames() {
  for(boolean enchanted:new boolean[]{false,true})for(boolean fancy:new boolean[]{false,true})for(int count:new int[]{1,2,17,37,64})for(NativeMachineItemRenderer.Display display:NativeMachineItemRenderer.Display.values()) {
   ItemStack stack=new ItemStack(Items.DIAMOND_SWORD,count);if(enchanted)stack.addEnchantment(Enchantments.UNBREAKING,1);EntityItem entity=new EntityItem(null,0,0,0,stack);entity.hoverStart=0;mc.gameSettings.fancyGraphics=fancy;
   model(stack.getItem(),icon);reset();new NativeMachineItemRenderer().renderFrame(entity,32,32,0,0,0,display);byte[] expected=pixels();int glints=textures.glints;
   SelectiveBaseOwner wrapper=new SelectiveBaseOwner(icon);model(stack.getItem(),wrapper);reset();new NativeMachineItemRenderer().renderFrame(entity,32,32,0,0,0,display);
   check(wrapper.selections==1&&textures.glints==glints,"selective declined callback retains actual ordinary count/glint route "+enchanted+fancy+count+display);
   check(Arrays.equals(expected,pixels()),"selective declined callback preserves actual ordinary framebuffer geometry "+enchanted+fancy+count+display);
   check(GL11.glGetInteger(GL11.GL_MODELVIEW_STACK_DEPTH)==1,"selective ordinary frame balanced");
  }
 }
 static final class Custom extends net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer {
  int calls;ItemStack last;float[] matrix;
  @Override public void renderByItem(ItemStack s){calls++;last=s;matrix=NativeMachineItemTest.matrix();}
 }
 static Textures textures;static Renderer renderer;static RegistrySimple<ModelResourceLocation,IBakedModel> models;static Framebuffer framebuffer;static IBakedModel icon;static Minecraft mc;
 static void model(Item item,IBakedModel model){ModelResourceLocation key=new ModelResourceLocation("test:machine","inventory");models.putObject(key,model);renderer.getItemModelMesher().register(item,0,key);renderer.getItemModelMesher().rebuildCache();}
 static void reset(){framebuffer.bindFramebuffer(true);GL11.glViewport(0,0,64,64);GlStateManager.matrixMode(GL11.GL_PROJECTION);GlStateManager.loadIdentity();GL11.glOrtho(0,64,64,0,-1000,1000);GlStateManager.matrixMode(GL11.GL_MODELVIEW);GlStateManager.loadIdentity();GlStateManager.enableTexture2D();GlStateManager.disableLighting();GlStateManager.disableCull();GlStateManager.enableDepth();GlStateManager.depthMask(true);GlStateManager.depthFunc(GL11.GL_LEQUAL);GlStateManager.clearColor(0,0,0,0);GlStateManager.clear(GL11.GL_COLOR_BUFFER_BIT|GL11.GL_DEPTH_BUFFER_BIT);GlStateManager.color(1,1,1,1);textures.glints=0;}
 static byte[] pixels(){ByteBuffer b=org.lwjgl.BufferUtils.createByteBuffer(64*64*4);GL11.glReadPixels(0,0,64,64,GL11.GL_RGBA,GL11.GL_UNSIGNED_BYTE,b);byte[] result=new byte[b.capacity()];b.get(result);return result;}
 static void setup()throws Exception {
  Bootstrap.register();mc=Minecraft.getMinecraft();OpenGlHelper.initializeTextures();
  SimpleReloadableResourceManager resources=new SimpleReloadableResourceManager(new MetadataSerializer());
  java.net.URL nativeSource=TextureMap.class.getProtectionDomain().getCodeSource().getLocation();
  if(nativeSource.getProtocol().equals("jar")) nativeSource=((java.net.JarURLConnection)nativeSource.openConnection()).getJarFileURL();
  resources.reloadResourcePack(new FileResourcePack(new java.io.File(nativeSource.toURI())));
  field(mc,"resourceManager",resources);textures=new Textures(resources);mc.renderEngine=textures;
  mc.fontRenderer=new net.minecraft.client.gui.FontRenderer(mc.gameSettings,new ResourceLocation("textures/font/ascii.png"),textures,false);mc.fontRenderer.onResourceManagerReload(resources);
  DynamicTexture atlas=new DynamicTexture(16,16);Arrays.fill(atlas.getTextureData(),0xFF303030);atlas.updateDynamicTexture();textures.loadTexture(TextureMap.LOCATION_BLOCKS_TEXTURE,atlas);
  DynamicTexture glint=new DynamicTexture(16,16);Arrays.fill(glint.getTextureData(),0xFFFFFFFF);glint.updateDynamicTexture();textures.loadTexture(new ResourceLocation("textures/misc/enchanted_item_glint.png"),glint);
  TextureAtlasSprite sprite=new TextureAtlasSprite("test:icon"){};sprite.setIconWidth(16);sprite.setIconHeight(16);sprite.initSprite(16,16,0,0,false);int[] data=new int[256];Arrays.fill(data,0xFFFFFFFF);sprite.setFramesTextureData(Collections.singletonList(new int[][]{data}));
  icon=new BakedItemModel(ImmutableList.copyOf(ItemLayerModel.getQuadsForSprite(-1,sprite,net.minecraft.client.renderer.vertex.DefaultVertexFormats.ITEM,Optional.empty())),sprite,ImmutableMap.of(),new ItemOverrideList(Collections.emptyList()) { @Override public IBakedModel handleItemState(IBakedModel original,ItemStack stack,net.minecraft.world.World world,net.minecraft.entity.EntityLivingBase holder){baseSelections++;return original;} });
  ModelManager manager=new ModelManager(new TextureMap("textures"));models=new RegistrySimple<>();field(manager,"modelRegistry",models);field(manager,"defaultModel",icon);
  ItemColors colors=new ItemColors();field(mc,"itemColors",colors);renderer=new Renderer(textures,manager,colors);field(mc,"renderItem",renderer);field(mc,"modelManager",manager);field(mc,"textureMapBlocks",manager.getTextureMap());
  RenderManager entities=new RenderManager(textures,renderer);entities.options=mc.gameSettings;field(mc,"renderManager",entities);framebuffer=new Framebuffer(64,64,true);
 }
 static final class Cube extends SimpleBakedModel {
  int queries;final List<float[]> matrices=new ArrayList<>();
  Cube(List<BakedQuad> quads,TextureAtlasSprite sprite){super(quads,new EnumMap<>(EnumFacing.class),false,true,sprite,ItemCameraTransforms.DEFAULT,ItemOverrideList.NONE);}
  @Override public List<BakedQuad> getQuads(net.minecraft.block.state.IBlockState state,EnumFacing face,long seed){queries++;matrices.add(NativeMachineItemTest.matrix());return face==null?super.getQuads(state,null,seed):Collections.emptyList();}
 }
 static void cubes() {
  TextureAtlasSprite sprite=icon.getParticleTexture();List<BakedQuad> quads=new ArrayList<>();FaceBakery bakery=new FaceBakery();
  for(EnumFacing face:EnumFacing.values())quads.add(bakery.makeBakedQuad(new org.lwjgl.util.vector.Vector3f(0,0,0),new org.lwjgl.util.vector.Vector3f(16,16,16),new BlockPartFace(null,-1,"test",new BlockFaceUV(new float[]{0,0,16,16},0)),sprite,face,ModelRotation.X0_Y0,null,false,true));
  for(net.minecraft.block.Block block:new net.minecraft.block.Block[]{Blocks.STONE,Blocks.GLASS}) {
   ItemStack stack=new ItemStack(block,37);stack.addEnchantment(Enchantments.UNBREAKING,1);Cube cube=new Cube(quads,sprite);model(stack.getItem(),cube);EntityItem entity=new EntityItem(null,0,0,0,stack);entity.hoverStart=0;
   for(NativeMachineItemRenderer.Display display:NativeMachineItemRenderer.Display.values()) {
    reset();cube.queries=0;cube.matrices.clear();new NativeMachineItemRenderer().renderFrame(entity,32,32,0,0,0,display);
    check(cube.queries==7*(display==NativeMachineItemRenderer.Display.DECORATION_FRAME?1:4),"real native cube retains exact legacy copy count "+display);
    check(textures.glints==0,"ordinary legacy cube branch has no enchant glint");
    check(GL11.glGetInteger(GL11.GL_MODELVIEW_STACK_DEPTH)==1,"ordinary cube matrix balanced");
   }
   reset();new RenderDecoItem().renderGui(stack,20,20,false,true);check(textures.glints==0,"world GUI cube also omits generic glint");
  }
 }
 static final class TypedDraw extends com.hbm.render.item.nativeitem.context.ContextItemRenderer {
  final List<net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType> types=new ArrayList<>();
  public javax.vecmath.Matrix4f localTransform(ItemStack stack,com.hbm.render.item.nativeitem.context.NativeItemContext context){return new com.hbm.render.item.nativeitem.ItemTransformMatrix().build();}
  protected void draw(ItemStack stack,com.hbm.render.item.nativeitem.context.NativeItemContext context){types.add(context.transform);}
 }
 static void typedContext(ItemStack stack)throws Exception {
  RenderItem original=new RenderItem(textures,renderer.getItemModelMesher().getModelManager(),mc.getItemColors());
  RenderItem scoped=new com.hbm.render.item.nativeitem.context.ContextRenderItem(original);
  TypedDraw draw=new TypedDraw();
  com.hbm.render.item.nativeitem.context.ContextItemModel typed=new com.hbm.render.item.nativeitem.context.ContextItemModel(icon,draw);
  ModelResourceLocation location=new ModelResourceLocation("test:typed","inventory");models.putObject(location,typed);scoped.getItemModelMesher().register(stack.getItem(),0,location);scoped.getItemModelMesher().rebuildCache();stack.getItem().setTileEntityItemStackRenderer(draw);field(mc,"renderItem",scoped);
  try {
   reset();new RenderDecoItem().renderGui(stack,20,20,true,true);check(draw.types.equals(Arrays.asList(ItemCameraTransforms.TransformType.GUI)),"complete world GUI reaches typed native GUI dispatcher, not TEISR NONE");
   reset();new RenderDecoItem().renderGui(stack,20,20,false,true);check(draw.types.size()==1,"nonfancy typed item uses retained original geometry");
   reset();NativeGuiItemDraw.withoutEffect(scoped,stack,20,20);check(draw.types.size()==1&&textures.glints==0,"typed no-effect preserves base ownership without recursion");
   EntityItem item=new EntityItem(null,0,0,0,stack);item.hoverStart=0;
   reset();new NativeMachineItemRenderer().renderFrame(item,0,0,0,0,0,NativeMachineItemRenderer.Display.DECORATION_FRAME);check(draw.types.equals(Arrays.asList(ItemCameraTransforms.TransformType.GUI,ItemCameraTransforms.TransformType.GROUND)),"complete machine reaches typed GROUND dispatcher");
  } finally {field(mc,"renderItem",renderer);}
 }
 public static void run()throws Exception {
  setup();ItemStack stack=new ItemStack(Items.DIAMOND_SWORD);stack.addEnchantment(Enchantments.SHARPNESS,2);model(stack.getItem(),icon);NBTTagCompound nbt=stack.writeToNBT(new NBTTagCompound());
  reset();NativeGuiItemDraw.withoutEffect(renderer,stack,20,20);byte[] noEffect=pixels();check(textures.glints==0,"real no-effect never binds glint");check(!Arrays.equals(noEffect,new byte[noEffect.length]),"real no-effect emits pixels");
  reset();renderer.renderItemAndEffectIntoGUI(new ItemStack(stack.getItem()),20,20);check(Arrays.equals(noEffect,pixels()),"no-effect enchanted pixels equal genuine native unenchanted draw");
  reset();renderer.renderItemAndEffectIntoGUI(stack,20,20);byte[] effect=pixels();check(textures.glints>0,"genuine native GUI adds effect");check(!Arrays.equals(noEffect,effect),"effect genuinely changes framebuffer");
  reset();renderer.zLevel=37;int gui=renderer.gui;stack.setAnimationsToGo(4);RenderItemStack.renderItemStack(20,20,.5F,stack);check(renderer.gui-gui==2,"intentional doubled GUI effect calls retained");check(renderer.zLevel==37&&renderer.guiDepths.get(renderer.guiDepths.size()-1)==0F&&renderer.guiDepths.get(renderer.guiDepths.size()-2)==0F,"both dedicated GUI calls own zero depth and restore shared renderer depth");check(GL11.glGetInteger(GL11.GL_MODELVIEW_STACK_DEPTH)==1,"animation matrix balanced");
  reset();int selections=baseSelections;RenderItemStack.renderItemStackNoEffect(20,20,.5F,stack);check(baseSelections-selections==2,"intentional doubled no-effect model draws retained");check(textures.glints==0,"both animated no-effect draws have no glint");check(renderer.zLevel==37,"no-effect shared GUI depth restored");renderer.zLevel=0;check(GL11.glGetInteger(GL11.GL_MODELVIEW_STACK_DEPTH)==1,"no-effect animation matrix balanced");
  Custom custom=new Custom();stack.getItem().setTileEntityItemStackRenderer(custom);BaseOwner owner=new BaseOwner(icon);model(stack.getItem(),owner);
  reset();renderer.renderItemAndEffectIntoGUI(stack,20,20);check(custom.calls==1&&custom.last==stack,"real native custom renderer receives original stack");
  reset();NativeGuiItemDraw.withoutEffect(renderer,stack,20,20);check(custom.calls==1&&textures.glints==0,"no-effect resolves retained base, never recurses custom");check(Arrays.equals(noEffect,pixels()),"retained full base geometry exactly rendered");
  reset();renderer.zLevel=37;RenderDecoItem deco=new RenderDecoItem();deco.renderGui(stack,20,20,true,true);check(renderer.zLevel==37&&renderer.guiDepths.get(renderer.guiDepths.size()-1)==0F,"world GUI owns zero depth without leaking shared z");renderer.zLevel=0;check(custom.calls==2,"world GUI fancy dispatches native custom");
  reset();deco.renderGui(stack,20,20,false,true);check(custom.calls==2&&textures.glints==1,"world GUI nonfancy uses base + original flat effect once");
  NativeMachineItemRenderer machine=new NativeMachineItemRenderer();EntityItem entity=new EntityItem(null,0,0,0,stack);entity.hoverStart=0;
  reset();machine.renderFrame(entity,0,0,0,0,0,NativeMachineItemRenderer.Display.DECORATION_FRAME);check(custom.calls==3,"machine full native custom draw");
  Renderer replacement=new Renderer(textures,renderer.getItemModelMesher().getModelManager(),mc.getItemColors());field(mc,"renderItem",replacement);ModelResourceLocation key=new ModelResourceLocation("test:machine","inventory");replacement.getItemModelMesher().register(stack.getItem(),0,key);replacement.getItemModelMesher().rebuildCache();
  reset();machine.renderFrame(entity,0,0,0,0,0,NativeMachineItemRenderer.Display.DECORATION_FRAME);check(replacement.draws==1&&custom.calls==4,"existing machine object resolves replaced native renderer at draw time");field(mc,"renderItem",renderer);
  model(stack.getItem(),icon);
  for(boolean fancy:new boolean[]{false,true}) {mc.gameSettings.fancyGraphics=fancy;reset();machine.renderFrame(entity,32,32,0,0,0,NativeMachineItemRenderer.Display.DECORATION_FRAME);check(textures.glints==(fancy?1:0),"machine icon fancy-only glint "+fancy);check(GL11.glGetInteger(GL11.GL_MODELVIEW_STACK_DEPTH)==1,"machine matrix balance "+fancy);}
  check(nbt.equals(stack.writeToNBT(new NBTTagCompound())),"all native draws preserve item/count/damage/enchantment/NBT");
  for(int count:new int[]{0,1,2,5,6,15,16,20,21,31,32,40,41,64}) {check(NativeMachineItemRenderer.blockCount(count)==(count>40?5:count>20?4:count>5?3:count>1?2:1),"legacy cube count "+count);check(NativeMachineItemRenderer.iconCount(count)==(count<2?1:count<16?2:count<32?3:4),"legacy fancy icon count "+count);}
  check(GL11.glGetError()==GL11.GL_NO_ERROR,"complete native draws produce no GL error");
  selectiveFrames();
  cubes();
  NativeBaseModelOwnershipTest.run(icon);
  NativeMachineConsumerTest.run();
  typedContext(stack);
  framebuffer.deleteFramebuffer();System.out.println("PASS "+checks+" genuine native machine/GUI full-draw, framebuffer effect, custom ownership, renderer replacement and stack checks; no fullmod gameplay claim.");
 }
}
