import java.io.*;
import java.lang.reflect.*;
import java.util.*;
import javax.vecmath.Matrix4f;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderDetonatorLaser;
import com.hbm.render.item.nativeitem.context.*;
import com.hbm.render.loader.*;
import com.hbm.render.mesh.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.client.resources.*;
import net.minecraft.client.resources.data.MetadataSerializer;
import net.minecraft.init.*;
import net.minecraft.item.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public final class SelectiveItemNativeTest {
 static MetadataSerializer metadata(){MetadataSerializer m=new MetadataSerializer();m.registerMetadataSectionType(new net.minecraft.client.resources.data.TextureMetadataSectionSerializer(),net.minecraft.client.resources.data.TextureMetadataSection.class);return m;}
 static int checks;
 static void check(boolean b,String s){checks++;if(!b)throw new AssertionError(s);}
 static void set(Object o,String field,Object v)throws Exception{Field f=o.getClass().getDeclaredField(field);f.setAccessible(true);f.set(o,v);}
 static Matrix4f matrix(){java.nio.FloatBuffer b=org.lwjgl.BufferUtils.createFloatBuffer(16);GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX,b);float[] f=new float[16];b.get(f);Matrix4f m=new Matrix4f(f);m.transpose();return m;}
 static void original(TransformType t){switch(t){case FIRST_PERSON_LEFT_HAND:case FIRST_PERSON_RIGHT_HAND:GL11.glScaled(.25,.25,.25);GL11.glRotatef(80,0,1,0);GL11.glRotatef(-20,1,0,0);GL11.glTranslatef(1,.5F,3);break;case THIRD_PERSON_LEFT_HAND:case THIRD_PERSON_RIGHT_HAND:GL11.glScaled(-.125,-.125,-.125);GL11.glRotatef(85,0,1,0);GL11.glRotatef(145,1,0,0);GL11.glTranslatef(-.5F,-1,6.5F);break;case GUI:GL11.glScaled(3.5,3.5,-3.5);GL11.glTranslatef(1.5F,2.75F,0);GL11.glRotatef(180,1,0,0);GL11.glRotatef(-90,0,1,0);GL11.glRotatef(-45,1,0,0);break;default:GL11.glRotatef(-90,0,1,0);GL11.glScaled(.25,.25,.25);}}
 static void mult(Matrix4f m){java.nio.FloatBuffer b=org.lwjgl.BufferUtils.createFloatBuffer(16);b.put(new float[]{m.m00,m.m10,m.m20,m.m30,m.m01,m.m11,m.m21,m.m31,m.m02,m.m12,m.m22,m.m32,m.m03,m.m13,m.m23,m.m33}).flip();GL11.glMultMatrix(b);}
 static void originalDeco(com.hbm.render.item.block.NativeDecoItemRenderer deco,TransformType type){
  boolean pole=deco instanceof com.hbm.render.item.block.ItemRenderPoleTop,satellite=deco instanceof com.hbm.render.item.block.ItemRenderSatelliteReceiver;
  if(type==TransformType.FIRST_PERSON_RIGHT_HAND||type==TransformType.FIRST_PERSON_LEFT_HAND){GL11.glRotatef(-135,0,0,1);GL11.glTranslatef(-.6F,-.6F,-.1F);if(satellite)GL11.glRotatef(90,0,1,0);GL11.glScalef(.5F,.5F,.5F);}
  else if(type==TransformType.THIRD_PERSON_RIGHT_HAND||type==TransformType.THIRD_PERSON_LEFT_HAND){GL11.glScalef(.5F,.5F,.5F);if(pole){GL11.glRotatef(180,1,0,0);GL11.glTranslatef(.8F,-1.8F,-.1F);}else{GL11.glTranslatef(.8F,-.3F,.2F);if(satellite)GL11.glRotatef(90,0,-1,0);}}
  else{GL11.glScalef(.5F,.5F,.5F);GL11.glRotatef(180,1,0,0);GL11.glTranslatef(0,-1,0);}
 }
 static void originalTool(TransformType type,double[] r,double[] t,double[] scale,double[] first){
  if(type==TransformType.GUI){GL11.glTranslated(8,8,0);GL11.glScaled(1,1,1);GL11.glTranslated(-8,-8,0);return;}
  GL11.glRotated(r[1],0,1,0);GL11.glRotated(r[2],0,0,1);GL11.glRotated(r[0],1,0,0);
  if(type==TransformType.FIRST_PERSON_RIGHT_HAND||type==TransformType.FIRST_PERSON_LEFT_HAND){GL11.glTranslated(.5,.5,0);GL11.glTranslated(-.5,-.5,0);GL11.glRotated(180,0,1,0);GL11.glRotated(-90,0,0,1);GL11.glTranslated(.5,.5,0);GL11.glScaled(first[0]*2,first[1]*2,first[2]*2);GL11.glTranslated(-.5,-.5,.25);}
  else {GL11.glTranslated(t[0],t[1],t[2]);GL11.glRotated(45,0,0,1);GL11.glRotated(180,0,1,0);GL11.glRotated(-45,0,0,1);GL11.glScaled(scale[0],scale[1],scale[2]);}
 }
 static class Font extends FontRenderer {
  int calls; boolean fail;
  Font(Minecraft mc){super(mc.gameSettings,new ResourceLocation("textures/font/ascii.png"),mc.getTextureManager(),false);}
  @Override public int drawString(String text,int x,int y,int color){check(text.matches("[1-9][0-9]{5}")&&x==0&&y==0&&color==0xff0000,"original six-digit red readout");calls++;if(fail)throw new IllegalStateException("controlled font failure");return super.drawString(text,x,y,color);}
 }
 static class Draw implements MeshModel {
  final HFRWavefrontObjectVBO actual; final List<String> parts=new ArrayList<>(); boolean fail;
  Draw(HFRWavefrontObjectVBO a){actual=a;}
  public ObjMesh getMesh(){return actual.getMesh();}public List<String> getPartNames(){return actual.getPartNames();}
  public void renderPart(String name){parts.add(name);check(GL11.glGetInteger(GL11.GL_SHADE_MODEL)==GL11.GL_SMOOTH,"original smooth mesh");if(name.equals("Main"))check(GL11.glIsEnabled(GL11.GL_CULL_FACE),"main culled");else {check(!GL11.glIsEnabled(GL11.GL_LIGHTING)&&!GL11.glIsEnabled(GL11.GL_TEXTURE_2D)&&!GL11.glIsEnabled(GL11.GL_CULL_FACE),"red emissive lights state");check(OpenGlHelper.lastBrightnessX==240&&OpenGlHelper.lastBrightnessY==240,"fullbright lights");if(fail)throw new IllegalStateException("controlled mesh failure");}actual.renderPart(name);}
  public void renderAll(){throw new AssertionError();}public void renderOnly(String...n){throw new AssertionError();}public void renderAllExcept(String...n){throw new AssertionError();}
 }
 public static void main(String[] args)throws Exception {
  Constructor<?> c=Class.forName("MaterialTextureAtlasSpriteNativeTest$Context").getDeclaredConstructor();c.setAccessible(true);
  try(AutoCloseable context=(AutoCloseable)c.newInstance()){
   Method init=Class.forName("MaterialTextureAtlasSpriteNativeTest").getDeclaredMethod("initializeNativeClientSettings");init.setAccessible(true);init.invoke(null);Bootstrap.register();Minecraft mc=Minecraft.getMinecraft();OpenGlHelper.initializeTextures();
   SimpleReloadableResourceManager resources=new SimpleReloadableResourceManager(metadata());
   resources.reloadResourcePack(new FileResourcePack(new File(Minecraft.class.getProtectionDomain().getCodeSource().getLocation().toURI())));resources.reloadResourcePack(new File(args[0]).isDirectory()?new FolderResourcePack(new File(args[0])):new FileResourcePack(new File(args[0])));
   set(mc,"resourceManager",resources);TextureManager textures=new TextureManager(resources);set(mc,"renderEngine",textures);set(mc,"itemColors",new net.minecraft.client.renderer.color.ItemColors());
   TextureMap atlas=new TextureMap("textures");set(mc,"textureMapBlocks",atlas);textures.loadTexture(TextureMap.LOCATION_BLOCKS_TEXTURE,new DynamicTexture(1,1));
   Font font=new Font(mc);mc.fontRenderer=font;Draw draw=new Draw(new HFRWavefrontObject(resources,new ResourceLocation("hbm","models/weapons/detonator_laser.obj"),false).asVBO());ResourceManager.detonator_laser=draw;
   for(String name:new String[]{"steelroof","poletop","polesatellitereceiver"}){ResourceLocation location=new ResourceLocation("hbm","textures/models/"+name+".png");try(IResource res=resources.getResource(location)){check(javax.imageio.ImageIO.read(res.getInputStream())!=null,"actual native resource lookup "+name);}textures.bindTexture(location);check(textures.getTexture(location) instanceof SimpleTexture,"actual native material loaded, not missing texture "+name);}
   check(draw.getPartNames().containsAll(Arrays.asList("Main","Lights")),"actual pinned original mesh parts");
   ModelManager models=new ModelManager(atlas);net.minecraft.util.registry.RegistrySimple<ModelResourceLocation,IBakedModel> registry=new net.minecraft.util.registry.RegistrySimple<>();set(models,"modelRegistry",registry);ContextRenderItem owner=new ContextRenderItem(new RenderItem(textures,models,mc.getItemColors()));set(mc,"renderItem",owner);
   ItemRenderDetonatorLaser renderer=new ItemRenderDetonatorLaser();ItemStack stack=new ItemStack(Items.DIAMOND,37,2);NBTTagCompound tag=new NBTTagCompound();tag.setString("probe","keep");stack.setTagCompound(tag);
   ContextItemModel root=new ContextItemModel(new TextureAtlasSprite("test:detonator"){},renderer);
   GL11.glMatrixMode(GL11.GL_MODELVIEW);
   for(TransformType t:TransformType.values()){
    GL11.glLoadIdentity();original(t);check(matrix().epsilonEquals(renderer.localTransform(stack,new NativeItemContext(t,null)),.00001F),"independent original transform "+t);
    GL11.glLoadIdentity();GL11.glTranslatef(7,8,9);Matrix4f before=matrix();int depth=GL11.glGetInteger(GL11.GL_MODELVIEW_STACK_DEPTH);OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit,83,117);
    IBakedModel selected=root.getOverrides().handleItemState(root,stack,null,null).handlePerspective(t).getLeft();draw.parts.clear();int textBefore=font.calls;
    owner.renderItem(stack,selected);
    check(draw.parts.equals(Arrays.asList("Main","Lights"))&&font.calls==textBefore+3,"actual native virtual boundary draws real VBO parts and native text "+t);
    check(matrix().equals(before)&&GL11.glGetInteger(GL11.GL_MODELVIEW_STACK_DEPTH)==depth,"matrix scope balanced "+t);check(OpenGlHelper.lastBrightnessX==83&&OpenGlHelper.lastBrightnessY==117,"lightmap restored "+t);
    check(GL11.glGetError()==0,"GL error-free actual draw "+t);
   }
   for(boolean mesh:new boolean[]{true,false}){draw.fail=mesh;font.fail=!mesh;GL11.glLoadIdentity();int depth=GL11.glGetInteger(GL11.GL_MODELVIEW_STACK_DEPTH);int attrib=GL11.glGetInteger(GL11.GL_ATTRIB_STACK_DEPTH);OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit,41,73);try{renderer.render(stack,new NativeItemContext(TransformType.GUI,null));throw new AssertionError();}catch(IllegalStateException expected){}check(depth==GL11.glGetInteger(GL11.GL_MODELVIEW_STACK_DEPTH)&&attrib==GL11.glGetInteger(GL11.GL_ATTRIB_STACK_DEPTH),"nested mesh/font exception balanced");check(OpenGlHelper.lastBrightnessX==41&&OpenGlHelper.lastBrightnessY==73,"failed draw lightmap restored");}
   draw.fail=false;font.fail=false;renderer.renderByItem(stack);
   for(com.hbm.render.item.block.NativeDecoItemRenderer deco : new com.hbm.render.item.block.NativeDecoItemRenderer[]{new com.hbm.render.item.block.ItemRenderDecoBlock(),new com.hbm.render.item.block.ItemRenderPoleTop(),new com.hbm.render.item.block.ItemRenderSatelliteReceiver()}) {
    TextureAtlasSprite sprite=new TextureAtlasSprite("test:deco"){};
    java.util.Map<TransformType,Matrix4f> transforms=new java.util.EnumMap<>(TransformType.class);for(TransformType t:TransformType.values()){Matrix4f m=new Matrix4f();m.setIdentity();transforms.put(t,m);}
    IBakedModel base=new com.hbm.render.item.nativeitem.StaticMeshItemModel(transforms,sprite);
    ContextItemModel wrapper=ContextItemModel.create(base,deco);
    for(TransformType ttype:TransformType.values())if(ttype!=TransformType.GUI){GL11.glLoadIdentity();originalDeco(deco,ttype);check(matrix().epsilonEquals(deco.localTransform(stack,new NativeItemContext(ttype,null)),.00001F),"original deco local matrix "+deco.getClass()+ttype);}
    IBakedModel resolved=wrapper.getOverrides().handleItemState(wrapper,stack,null,null);
    check(resolved.handlePerspective(TransformType.GUI).getLeft()==base,"deco exact ordinary GUI fallback");
    check(resolved instanceof com.hbm.render.util.NativeMachineItemRenderer.FrameItemModel,"selective machine frame marker survives overrides");
    for(TransformType t:TransformType.values())if(t!=TransformType.GUI){IBakedModel selected=resolved.handlePerspective(t).getLeft();check(selected instanceof com.hbm.render.util.NativeMachineItemRenderer.FrameItemModel,"frame marker survives perspective");owner.renderItem(stack,selected);check(GL11.glGetError()==0,"actual complete Java model native draw "+t);}
    net.minecraft.entity.item.EntityItem entity=new net.minecraft.entity.item.EntityItem(null,0,0,0,stack);
    int depth=GL11.glGetInteger(GL11.GL_MODELVIEW_STACK_DEPTH);deco.renderDropped(entity,1,2,3,45,.5F,false,0);deco.renderFrame(entity,1,2,3,45,.5F,com.hbm.render.util.NativeMachineItemRenderer.Display.ENTITY_FRAME);check(GL11.glGetInteger(GL11.GL_MODELVIEW_STACK_DEPTH)==depth&&GL11.glGetError()==0,"actual no-helper dropped/machine draws balance");
   }
   TextureAtlasSprite icon=new TextureAtlasSprite("test:tool"){};icon.setIconWidth(16);icon.setIconHeight(16);icon.initSprite(16,16,0,0,false);int[] pixels=new int[256];Arrays.fill(pixels,-1);icon.setFramesTextureData(Collections.singletonList(new int[][]{pixels}));
   IBakedModel ordinary=new net.minecraftforge.client.model.BakedItemModel(com.google.common.collect.ImmutableList.copyOf(net.minecraftforge.client.model.ItemLayerModel.getQuadsForSprite(0,icon,net.minecraft.client.renderer.vertex.DefaultVertexFormats.ITEM,java.util.Optional.empty())),icon,com.google.common.collect.ImmutableMap.of(),ItemOverrideList.NONE);
   double[] r={0,180,-90},t={.125,.625,0},scale={1.7,1.7,.85},zero={0,0,0},one={1,1,1};
   com.hbm.render.item.ItemRenderTransformer transformer=new com.hbm.render.item.ItemRenderTransformer(r,t,scale,r,zero,one,zero,zero,one);
   ContextItemModel tool=ContextItemModel.create(ordinary,transformer);ModelResourceLocation toolKey=new ModelResourceLocation("test:tool","inventory");registry.putObject(toolKey,tool);owner.getItemModelMesher().register(Items.DIAMOND_SWORD,s->toolKey);
   ItemStack sword=new ItemStack(Items.DIAMOND_SWORD,1,3);sword.addEnchantment(Enchantments.UNBREAKING,1);
   for(TransformType ttype:new TransformType[]{TransformType.FIRST_PERSON_RIGHT_HAND,TransformType.FIRST_PERSON_LEFT_HAND,TransformType.THIRD_PERSON_RIGHT_HAND,TransformType.THIRD_PERSON_LEFT_HAND,TransformType.GUI}){GL11.glLoadIdentity();originalTool(ttype,r,t,scale,one);check(matrix().epsilonEquals(transformer.localTransform(sword,new NativeItemContext(ttype,null)),.00001F),"original transformer local matrix "+ttype);}
   GL11.glLoadIdentity();GL11.glTranslatef(-.0625F,.4375F,.0625F);GL11.glTranslatef(0,.1875F,0);GL11.glScalef(.625F,-.625F,.625F);GL11.glRotatef(-100,1,0,0);GL11.glRotatef(45,0,1,0);GL11.glTranslatef(0,-.3F,0);GL11.glScalef(1.5F,1.5F,1.5F);GL11.glRotatef(50,0,1,0);GL11.glRotatef(335,0,0,1);GL11.glTranslatef(-.9375F,-.0625F,0);originalTool(TransformType.THIRD_PERSON_RIGHT_HAND,r,t,scale,one);Matrix4f oldTool=matrix();
   GL11.glLoadIdentity();GL11.glRotatef(-90,1,0,0);GL11.glRotatef(180,0,1,0);GL11.glTranslatef(.0625F,.125F,-.625F);mult(transformer.composePerspective(sword,new NativeItemContext(TransformType.THIRD_PERSON_RIGHT_HAND,null)));GL11.glTranslatef(-.5F,-.5F,-.5F);check(matrix().epsilonEquals(oldTool,.00005F),"original full3D tool caller composed against real native suffix");
   textures.bindTexture(new ResourceLocation("hbm","textures/misc/glint.png"));check(textures.getTexture(new ResourceLocation("hbm","textures/misc/glint.png")) instanceof SimpleTexture,"original HBM transformer glint material loaded");
   check(tool.getItemCameraTransforms()==ordinary.getItemCameraTransforms(),"selective ground preserves original camera scale/lift");
   check(tool.machineFrameModel(sword)==ordinary,"selective ground preserves ordinary machine frame owner");
   for(TransformType ttype:TransformType.values()) {
    IBakedModel selected=tool.getOverrides().handleItemState(tool,sword,null,null).handlePerspective(ttype).getLeft();
    if(ttype==TransformType.GROUND||ttype==TransformType.FIXED||ttype==TransformType.HEAD||ttype==TransformType.NONE)check(selected==ordinary,"tool ordinary base context retained "+ttype);
    else {owner.renderItem(sword,selected);check(GL11.glGetError()==0,"actual native tool extrusion/GUI and glint "+ttype);}
   }
   class Template extends com.hbm.render.item.ItemRenderTemplate { boolean shift;@Override protected boolean isShiftHeld(){return shift;} }
   Template template=new Template();ContextItemModel templateModel=ContextItemModel.create(ordinary,template);ItemStack templateStack=new ItemStack(com.hbm.items.ModItems.crucible_template,17,4);
   com.hbm.inventory.recipes.CrucibleRecipes.indexMapping.put(4,new com.hbm.inventory.recipes.CrucibleRecipes.CrucibleRecipe(sword));
   for(boolean shift:new boolean[]{false,true}) {template.shift=shift;for(TransformType ttype:TransformType.values()) {
    IBakedModel selected=templateModel.getOverrides().handleItemState(templateModel,templateStack,null,null).handlePerspective(ttype).getLeft();
    if(shift&&ttype==TransformType.GUI){owner.renderItem(templateStack,selected);check(GL11.glGetError()==0,"actual nested shift preview including enchanted native tool GUI");}else check(!(selected instanceof ContextItemModel)&&selected==ordinary.handlePerspective(ttype).getLeft(),"template complete ordinary fallback "+shift+ttype);
   }}
   com.hbm.inventory.recipes.CrucibleRecipes.indexMapping.clear();check(templateModel.getOverrides().handleItemState(templateModel,templateStack,null,null).handlePerspective(TransformType.GUI).getLeft()==ordinary.handlePerspective(TransformType.GUI).getLeft(),"removed recipe cannot retain stale icon");
   check(templateStack.getCount()==17&&templateStack.getMetadata()==4,"template input unchanged");
   check(!(root.getOverrides().handleItemState(root,stack,null,null) instanceof com.hbm.render.util.NativeMachineItemRenderer.FrameItemModel),"non-selective machine fallback remains marker-free");check(stack.getCount()==37&&stack.getMetadata()==2&&stack.getTagCompound()==tag&&"keep".equals(tag.getString("probe")),"stack/count/metadata/NBT unchanged");check(GL11.glGetError()==0,"final GL clean");draw.actual.destroy();
   System.out.println("PASS "+checks+" selected native declaration, original transform, actual VBO/material/font/wave draw, typed dispatch, stack and failure-state checks. No full-game visual acceptance.");
  }
 }
}
