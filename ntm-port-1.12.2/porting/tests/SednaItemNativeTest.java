import com.hbm.main.ResourceManager;
import com.hbm.render.anim.HbmAnimations;
import com.hbm.items.weapon.sedna.ItemGunBaseNT;
import com.hbm.render.item.weapon.sedna.*;
import com.hbm.render.item.weapon.sedna.nativehand.SednaHandHandler;
import com.hbm.render.item.nativeitem.context.*;
import java.lang.reflect.*;
import java.util.*;
import javax.vecmath.Matrix4f;
import net.minecraft.client.*;
import net.minecraft.client.entity.*;
import net.minecraft.client.multiplayer.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.entity.*;
import net.minecraft.init.*;
import net.minecraft.item.*;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.event.*;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.*;
import org.lwjgl.opengl.GL11;

public class SednaItemNativeTest extends ContextItemNativeTest {
 static class Player extends ContextItemNativeTest.Player {
  ItemStack held=ItemStack.EMPTY;EnumHandSide side=EnumHandSide.RIGHT;boolean sleeping;
  @Override public boolean isPlayerSleeping(){return sleeping;}
  @Override public boolean isHandActive(){return false;}@Override public float getSwingProgress(float p){return 0;}
  @Override public EnumHandSide getPrimaryHand(){return side;}@Override public ItemStack getHeldItemMainhand(){return held;}
  @Override public float getEyeHeight(){return 1.62F;}
 }
 static class World extends WorldClient {
  World(){super(null,null,0,null,null);}
  @Override public int getCombinedLight(BlockPos pos,int extra){return 0xD000A0;}
  @Override public net.minecraft.block.state.IBlockState getBlockState(BlockPos pos){return Blocks.AIR.getDefaultState();}
 }
 static class Controller extends PlayerControllerMP {Controller(Minecraft m){super(m,null);}@Override public boolean isSpectator(){return false;}}
 static class EntityRender extends EntityRenderer {
  EntityRender(){super(null,null);}
  @Override public void enableLightmap(){}@Override public void disableLightmap(){}
 }
 static class Hands extends ItemRenderer {
  int overlays;Matrix4f overlayProjection;final List<ItemStack> ordinary=new ArrayList<>();
  Hands(Minecraft mc){super(mc);}
  @Override public void renderOverlays(float p){overlays++;overlayProjection=projection();}
  @Override public void renderItemInFirstPerson(AbstractClientPlayer player,float pt,float pitch,EnumHand hand,float swing,ItemStack stack,float equip){ordinary.add(stack);}
 }
 static Matrix4f projection(){java.nio.FloatBuffer b=org.lwjgl.BufferUtils.createFloatBuffer(16);GL11.glGetFloat(GL11.GL_PROJECTION_MATRIX,b);float[] f=new float[16];b.get(f);Matrix4f m=new Matrix4f(f);m.transpose();return m;}
 static void named(Object o,Class<?> c,String name,Object value)throws Exception{Field f=c.getDeclaredField(name);f.setAccessible(true);f.set(o,value);}
 static class Coil extends ItemRenderCoilgun {
  final List<ItemStack> seen=new ArrayList<>();final List<EnumHandSide> sides=new ArrayList<>();boolean fail;
  @Override public void renderNativeHand(ItemStack stack,float partial,EnumHand hand,EnumHandSide side){seen.add(stack);sides.add(side);super.renderNativeHand(stack,partial,hand,side);}
  @Override public void renderFirstPerson(ItemStack stack){super.renderFirstPerson(stack);if(fail)throw new IllegalStateException("controlled weapon failure");}
 }
 static void hands()throws Exception {
  Fixture f=new Fixture();f.install();Player player=(Player)unsafe.allocateInstance(Player.class);net.minecraft.network.datasync.EntityDataManager data=new net.minecraft.network.datasync.EntityDataManager(player);set(player,Entity.class,net.minecraft.network.datasync.EntityDataManager.class,data);Field health=EntityLivingBase.class.getDeclaredField("HEALTH");health.setAccessible(true);data.register((net.minecraft.network.datasync.DataParameter<Float>)health.get(null),20F);player.held=new ItemStack(Items.APPLE);player.side=EnumHandSide.RIGHT;f.mc.player=player;World world=(World)unsafe.allocateInstance(World.class);f.mc.world=world;player.world=world;named(f.mc,Minecraft.class,"renderViewEntity",player);f.mc.playerController=new Controller(f.mc);f.mc.gameSettings.viewBobbing=false;f.mc.gameSettings.renderDistanceChunks=8;f.mc.gameSettings.fovSetting=70;
  f.textures.loadTexture(ResourceManager.flaregun_tex,new DynamicTexture(1,1));f.textures.loadTexture(ResourceManager.coilgun_tex,new DynamicTexture(1,1));
  Hands hands=new Hands(f.mc);set(f.mc,Minecraft.class,ItemRenderer.class,hands);EntityRender entity=(EntityRender)unsafe.allocateInstance(EntityRender.class);set(entity,EntityRenderer.class,Minecraft.class,f.mc);set(entity,EntityRenderer.class,ItemRenderer.class,hands);named(entity,EntityRenderer.class,"farPlaneDistance",128F);f.mc.entityRenderer=entity;
  Coil coil=new Coil();f.item.setTileEntityItemStackRenderer(coil);ItemStack cached=new ItemStack(f.item,7,4);ItemStack off=new ItemStack(f.item,2,3);named(hands,ItemRenderer.class,"itemStackMainHand",cached);named(hands,ItemRenderer.class,"itemStackOffHand",ItemStack.EMPTY);
  SednaHandHandler handler=new SednaHandHandler();MinecraftForge.EVENT_BUS.register(handler);Method render=EntityRenderer.class.getDeclaredMethod("renderHand",float.class,int.class);render.setAccessible(true);
  try {
   render.invoke(entity,.4F,0);check(coil.seen.equals(Arrays.asList(cached))&&coil.sides.equals(Arrays.asList(EnumHandSide.RIGHT)),"actual native frame draws cached switched-away weapon, not player's new held stack");check(hands.overlays==1,"actual native canceled hand frame draws overlays exactly once");check(hands.ordinary.size()==1&&hands.ordinary.get(0).isEmpty(),"native other hand routing retained");GL11.glMatrixMode(GL11.GL_PROJECTION);GL11.glPushMatrix();GL11.glLoadIdentity();org.lwjgl.util.glu.Project.gluPerspective(70,(float)f.mc.displayWidth/f.mc.displayHeight,.05F,256);Matrix4f expected=projection();GL11.glPopMatrix();GL11.glMatrixMode(GL11.GL_MODELVIEW);check(hands.overlayProjection.epsilonEquals(expected,.000001F),"native overlay uses independently calculated original 70-degree custom projection");
   coil.seen.clear();coil.sides.clear();hands.ordinary.clear();named(hands,ItemRenderer.class,"itemStackOffHand",off);render.invoke(entity,.6F,0);check(coil.seen.equals(Arrays.asList(cached,off))&&coil.sides.equals(Arrays.asList(EnumHandSide.RIGHT,EnumHandSide.LEFT)),"actual native per-hand events preserve two cached stacks and mirror offhand");check(hands.overlays==2&&hands.overlayProjection.epsilonEquals(expected,.000001F),"two custom hands still one native overlay in intended projection");
   coil.seen.clear();coil.sides.clear();hands.ordinary.clear();named(hands,ItemRenderer.class,"itemStackMainHand",new ItemStack(Items.APPLE));render.invoke(entity,.3F,0);check(coil.seen.equals(Arrays.asList(off))&&coil.sides.equals(Arrays.asList(EnumHandSide.LEFT))&&hands.ordinary.size()==1&&hands.ordinary.get(0).getItem()==Items.APPLE,"mixed ordinary main/custom offhand preserved");
   coil.seen.clear();named(hands,ItemRenderer.class,"itemStackMainHand",ItemStack.EMPTY);player.side=EnumHandSide.LEFT;render.invoke(entity,.2F,0);check(coil.sides.get(coil.sides.size()-1)==EnumHandSide.RIGHT,"left-primary player offhand uses actual right side");
   Matrix4f beforeModel=matrix(),beforeProjection=projection();int modelDepth=GL11.glGetInteger(GL11.GL_MODELVIEW_STACK_DEPTH);coil.fail=true;try{coil.renderNativeHand(cached,.8F,EnumHand.MAIN_HAND,EnumHandSide.LEFT);throw new AssertionError();}catch(IllegalStateException expectedFailure){}check(matrix().equals(beforeModel)&&projection().equals(beforeProjection)&&GL11.glGetInteger(GL11.GL_MODELVIEW_STACK_DEPTH)==modelDepth,"custom projection/matrix restored after weapon draw failure");coil.fail=false;
   int calls=coil.seen.size();f.mc.gameSettings.hideGUI=true;render.invoke(entity,.5F,0);check(coil.seen.size()==calls,"hide GUI still suppresses body without losing native overlay path");f.mc.gameSettings.hideGUI=false;
  } finally {MinecraftForge.EVENT_BUS.unregister(handler);}
 }

 static void oldCoilLocal(TransformType type,float aim,double recoil,double reload){
  if(type==TransformType.FIRST_PERSON_LEFT_HAND||type==TransformType.FIRST_PERSON_RIGHT_HAND){
   GL11.glTranslated(0,0,.875);float offset=.8F;GL11.glTranslated(-1.25F*offset*(1-aim),-1.5F*offset+(-7.5/8D+1.5F*offset)*aim,2.5F*offset+(1-2.5F*offset)*aim);
   GL11.glScaled(.75,.75,.75);GL11.glRotated(-90,0,1,0);GL11.glTranslated(-1.5-recoil*.5,0,0);GL11.glRotated(recoil*45,0,0,1);GL11.glTranslated(1.5,0,0);GL11.glTranslated(-2.5,0,0);GL11.glRotated(reload*-45,0,0,1);GL11.glTranslated(2.5,0,0);
  }else{
   if(type==TransformType.GUI){GL11.glScaled(1,1,-1);GL11.glTranslated(8,8,0);GL11.glRotated(225,0,0,1);GL11.glRotated(90,0,1,0);GL11.glScaled(4,4,4);GL11.glRotated(25,1,0,0);GL11.glRotated(45,0,1,0);GL11.glTranslated(-.25,-.25,0);}
   else if(type==TransformType.THIRD_PERSON_LEFT_HAND||type==TransformType.THIRD_PERSON_RIGHT_HAND){GL11.glScaled(.125,.125,.125);GL11.glRotatef(15,0,0,1);GL11.glRotatef(12.5F,0,1,0);GL11.glRotatef(15,1,0,0);GL11.glTranslated(3.5,0,0);GL11.glScaled(3,3,3);GL11.glTranslated(0,.25,1.25);}
   else{GL11.glScaled(.125,.125,.125);GL11.glRotated(-90,0,1,0);}
   GL11.glRotated(-90,0,1,0);
  }
 }
 static void coilContexts()throws Exception{
  Fixture f=new Fixture();ContextRenderItem r=f.install();ItemRenderCoilgun coil=new ItemRenderCoilgun();ContextItemModel model=new ContextItemModel(f.originalModel,coil);f.registry.putObject(f.key,model);
  f.textures.loadTexture(ResourceManager.flaregun_tex,new DynamicTexture(1,1));f.textures.loadTexture(ResourceManager.coilgun_tex,new DynamicTexture(1,1));
  ItemStack stack=new ItemStack(f.item,13,6);net.minecraft.nbt.NBTTagCompound tag=new net.minecraft.nbt.NBTTagCompound();tag.setString("unchanged","source");stack.setTagCompound(tag);Player holder=(Player)unsafe.allocateInstance(Player.class);
  for(TransformType type:TransformType.values())for(float aim:new float[]{0,.3F,1})for(double recoil:new double[]{0,.7}){
   HbmAnimations.recoil=recoil;HbmAnimations.reload=.4;ItemGunBaseNT.prevAimingProgress=ItemGunBaseNT.aimingProgress=aim;
   GL11.glLoadIdentity();oldCoilLocal(type,aim,recoil,.4);Matrix4f expected=matrix();GL11.glLoadIdentity();ResourceManager.coilgun.parts.clear();ResourceManager.coilgun.matrices.clear();coil.render(stack,new NativeItemContext(type,holder));
   check(ResourceManager.coilgun.parts.equals(Arrays.asList("All"))&&ResourceManager.coilgun.matrices.get(0).epsilonEquals(expected,.00001F),"original Coilgun setup/draw/recoil/reload/aim "+type+"/"+aim+"/"+recoil);
   ResourceManager.coilgun.parts.clear();GL11.glLoadIdentity();r.renderItem(stack,holder,type,type==TransformType.FIRST_PERSON_LEFT_HAND||type==TransformType.THIRD_PERSON_LEFT_HAND);check(ResourceManager.coilgun.parts.equals(Arrays.asList("All")),"actual native Coilgun model/holder path "+type);
  }
  check(stack.getCount()==13&&stack.getMetadata()==6&&stack.getTagCompound()==tag&&tag.getString("unchanged").equals("source"),"Coilgun actual stack count/metadata/NBT preserved");
  ItemGunBaseNT.prevAimingProgress=ItemGunBaseNT.aimingProgress=0;HbmAnimations.recoil=HbmAnimations.reload=0;
 }
 static void scopes()throws Exception{
  int a=GL11.glGenTextures(),b=GL11.glGenTextures(),c=GL11.glGenTextures();GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);GlStateManager.bindTexture(a);GlStateManager.enableTexture2D();GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);GlStateManager.bindTexture(b);GlStateManager.disableTexture2D();OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit,17,29);GlStateManager.shadeModel(GL11.GL_FLAT);GlStateManager.disableLighting();GL11.glMatrixMode(GL11.GL_PROJECTION);GL11.glLoadIdentity();GL11.glTranslated(2,3,4);Matrix4f p=projection();GL11.glMatrixMode(GL11.GL_MODELVIEW);GL11.glLoadIdentity();GL11.glTranslated(5,6,7);Matrix4f m=matrix();
  try(NativeProjectionScope outer=new NativeProjectionScope()){
   GlStateManager.bindTexture(c);GlStateManager.enableTexture2D();GlStateManager.enableLighting();OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit,99,101);GL11.glTranslatef(8,9,10);Matrix4f nested=matrix();
   try{try(NativeProjectionScope inner=new NativeProjectionScope()){GL11.glPushMatrix();GL11.glTranslatef(1,2,3);GL11.glMatrixMode(GL11.GL_PROJECTION);GL11.glPushMatrix();GL11.glLoadIdentity();GlStateManager.shadeModel(GL11.GL_SMOOTH);throw new IllegalStateException("nested");}}catch(IllegalStateException expected){}
   check(matrix().equals(nested)&&OpenGlHelper.lastBrightnessX==99&&OpenGlHelper.lastBrightnessY==101,"nested projection scope restores its own lexical state");
  }
  check(matrix().equals(m)&&projection().equals(p)&&OpenGlHelper.lastBrightnessX==17&&OpenGlHelper.lastBrightnessY==29&&!GL11.glIsEnabled(GL11.GL_LIGHTING)&&GL11.glGetInteger(GL11.GL_SHADE_MODEL)==GL11.GL_FLAT,"outer projection scope restores matrices/lightmap/lighting/shade after nested failure");
  GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);GlStateManager.bindTexture(a);GlStateManager.enableTexture2D();check(GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D)==a&&GL11.glIsEnabled(GL11.GL_TEXTURE_2D),"default native texture cache agrees after scope");GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);GlStateManager.bindTexture(b);GlStateManager.disableTexture2D();check(GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D)==b&&!GL11.glIsEnabled(GL11.GL_TEXTURE_2D),"lightmap native texture cache agrees after scope");GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);GL11.glDeleteTextures(a);GL11.glDeleteTextures(b);GL11.glDeleteTextures(c);GL11.glMatrixMode(GL11.GL_MODELVIEW);
 }
 static void smoke()throws Exception{
  ItemGunBaseNT.SmokeNode n=new ItemGunBaseNT.SmokeNode(),p=new ItemGunBaseNT.SmokeNode();n.forward=1;n.lift=2;n.side=3;n.width=.5;n.alpha=.6;p.forward=4;p.lift=5;p.side=6;p.width=1;p.alpha=.2;
  try(NativeProjectionScope scope=new NativeProjectionScope()){ItemRenderWeaponBase.renderSmokeNodes(Arrays.asList(n,p),2);}
  BufferBuilder buffer=Tessellator.getInstance().getBuffer();java.nio.ByteBuffer bytes=buffer.getByteBuffer();int stride=buffer.getVertexFormat().getSize();double[][] expected={{1,2,3,.6},{1,2,4,0},{4,5,8,0},{4,5,6,.2},{1,2,3,.6},{1,2,2,0},{4,5,4,0},{4,5,6,.2}};
  for(int i=0;i<8;i++){int offset=i*stride;check(bytes.getFloat(offset)==expected[i][0]&&bytes.getFloat(offset+4)==expected[i][1]&&bytes.getFloat(offset+8)==expected[i][2]&&(bytes.get(offset+15)&255)==(int)((float)expected[i][3]*255)&&bytes.get(offset+16)==0&&bytes.get(offset+17)==127&&bytes.get(offset+18)==0,"actual native smoke position/color/normal bytes "+i);}
 }

 static void flash()throws Exception{
  Minecraft.getMinecraft().getTextureManager().loadTexture(ItemRenderWeaponBase.laser_flash,new DynamicTexture(1,1));
  try(NativeProjectionScope scope=new NativeProjectionScope()){ItemRenderWeaponBase.renderLaserFlash(System.currentTimeMillis()-50,1000,2,0x123456);}
  BufferBuilder buffer=Tessellator.getInstance().getBuffer();java.nio.ByteBuffer bytes=buffer.getByteBuffer();int stride=buffer.getVertexFormat().getSize();int[][] uv={{1,1},{0,1},{0,0},{1,0}};
  for(int i=0;i<4;i++){int offset=i*stride;check((bytes.get(offset+12)&255)==0x12&&(bytes.get(offset+13)&255)==0x34&&(bytes.get(offset+14)&255)==0x56&&(bytes.get(offset+15)&255)==255&&bytes.getFloat(offset+16)==uv[i][0]&&bytes.getFloat(offset+20)==uv[i][1]&&bytes.getShort(offset+24)==240&&bytes.getShort(offset+26)==0&&bytes.get(offset+28)==0&&bytes.get(offset+29)==127&&bytes.get(offset+30)==0,"actual native laser color/UV/packed-light/normal bytes "+i);}
 }

 static void fov()throws Exception{
  Player player=(Player)unsafe.allocateInstance(Player.class);Minecraft.getMinecraft().player=player;Item item=new Item();ItemStack stack=new ItemStack(item);player.held=stack;ItemRenderWeaponBase.interp=.25F;ItemGunBaseNT.prevAimingProgress=.2F;ItemGunBaseNT.aimingProgress=.6F;
  SednaFovNativeFixture handler=new SednaFovNativeFixture();net.minecraftforge.client.event.FOVUpdateEvent event=new net.minecraftforge.client.event.FOVUpdateEvent(player,1.2F);handler.setupFOV(event);check(event.getNewfov()==1.2F,"unactivated native item cannot acquire custom weapon FOV");
  item.setTileEntityItemStackRenderer(new ItemRenderCoilgun());handler.setupFOV(event);check(Math.abs(event.getNewfov()-1.2F*(1-.3F*.33F))<.000001F,"actual native FOV event retains held-stack weapon aiming formula");player.held=ItemStack.EMPTY;event=new net.minecraftforge.client.event.FOVUpdateEvent(player,.8F);handler.setupFOV(event);check(event.getNewfov()==.8F,"empty current held stack does not inherit cached weapon FOV");ItemGunBaseNT.prevAimingProgress=ItemGunBaseNT.aimingProgress=0;
 }
 public static void main(String[]args)throws Exception {
  Field u=sun.misc.Unsafe.class.getDeclaredField("theUnsafe");u.setAccessible(true);unsafe=(sun.misc.Unsafe)u.get(null);Constructor<?> ctor=Class.forName("MaterialTextureAtlasSpriteNativeTest$Context").getDeclaredConstructor();ctor.setAccessible(true);
  try(AutoCloseable context=(AutoCloseable)ctor.newInstance()){Method init=Class.forName("MaterialTextureAtlasSpriteNativeTest").getDeclaredMethod("initializeNativeClientSettings");init.setAccessible(true);init.invoke(null);Bootstrap.register();net.minecraftforge.fml.common.ModMetadata metadata=new net.minecraftforge.fml.common.ModMetadata();metadata.modId="hbm";metadata.name="Sedna native probe";metadata.version="1";net.minecraftforge.fml.common.DummyModContainer owner=new net.minecraftforge.fml.common.DummyModContainer(metadata);net.minecraftforge.fml.common.Loader.instance().setupTestHarness(owner);net.minecraftforge.fml.common.Loader.instance().setActiveModContainer(owner);OpenGlHelper.initializeTextures();GL11.glMatrixMode(GL11.GL_MODELVIEW);hands();coilContexts();scopes();smoke();flash();fov();check(GL11.glGetError()==0,"native GL error-free");System.out.println("PASS "+checks+" native Sedna hand/context/animation/projection/overlay/vertex checks. Controlled draw assets and world, no whole-game acceptance.");}
 }
}
