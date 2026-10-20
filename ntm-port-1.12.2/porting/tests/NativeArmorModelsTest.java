import com.hbm.main.ResourceManager;
import com.hbm.render.loader.ModelRendererObj;
import com.hbm.render.model.*;
import com.mojang.authlib.GameProfile;
import java.lang.reflect.*;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.client.renderer.block.model.ModelManager;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.client.resources.*;
import net.minecraft.client.resources.data.MetadataSerializer;
import net.minecraft.entity.*;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.init.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.profiler.Profiler;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.*;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.storage.*;
import net.minecraftforge.fml.common.*;
import org.lwjgl.opengl.GL11;

/** Whole real model bodies and native pose/render-manager routing. NTM meshes record actual GL matrices. */
public final class NativeArmorModelsTest {
 static int checks;static Minecraft mc;static RenderManager manager;static TextureManager textures;
 static void check(boolean ok,String message){checks++;if(!ok)throw new AssertionError(message);}
 static void field(Object o,String name,Object value)throws Exception{Field f=o.getClass().getDeclaredField(name);f.setAccessible(true);f.set(o,value);}
 static final class WorldFixture extends World {
  WorldFixture(){super(new SaveHandlerMP(),new WorldInfo(new NBTTagCompound()),new WorldProviderSurface(),new Profiler(),true);provider.setWorld(this);}
  protected IChunkProvider createChunkProvider(){throw new AssertionError("No chunk service in model fixture");}
  protected boolean isChunkLoaded(int x,int z,boolean empty){return false;}
  public net.minecraft.block.state.IBlockState getBlockState(BlockPos p){return Blocks.AIR.getDefaultState();}
 }
 static final class Player extends AbstractClientPlayer {
  final String skin;Player(World world,String skin){super(world,new GameProfile(new UUID(8,9),"ArmorProbe"));this.skin=skin;}
  public String getSkinType(){return skin;}public boolean isSpectator(){return false;}public boolean isCreative(){return false;}
 }
 static String[] names={"head","body","leftArm","rightArm","leftLeg","rightLeg","leftFoot","rightFoot"};
 static ModelRendererObj part(Object model,String name)throws Exception{return (ModelRendererObj)model.getClass().getField(name).get(model);}
 static float[] state(ModelRendererObj p){return new float[]{p.offsetX,p.offsetY,p.offsetZ,p.rotateAngleX,p.rotateAngleY,p.rotateAngleZ,p.rotationPointX,p.rotationPointY,p.rotationPointZ,p.originPointX,p.originPointY,p.originPointZ};}
 static float[] pose(ModelRenderer p){return new float[]{p.offsetX,p.offsetY,p.offsetZ,p.rotateAngleX,p.rotateAngleY,p.rotateAngleZ,p.rotationPointX,p.rotationPointY,p.rotationPointZ};}
 static float[] pose(ModelRendererObj p){return Arrays.copyOf(state(p),9);}
 static ModelRenderer[] nativeParts(ModelBiped m){return new ModelRenderer[]{m.bipedHead,m.bipedBody,m.bipedLeftArm,m.bipedRightArm,m.bipedLeftLeg,m.bipedRightLeg,m.bipedLeftLeg,m.bipedRightLeg};}
 static float[] matrix(){java.nio.FloatBuffer b=org.lwjgl.BufferUtils.createFloatBuffer(16);GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX,b);float[] f=new float[16];b.get(f);return f;}
 static void copies(ModelArmorBase m,ModelBiped source)throws Exception{
  ModelRenderer[] n=nativeParts(source);for(int i=0;i<names.length;i++)check(Arrays.equals(pose(n[i]),pose(part(m,names[i]))),"native current posed part copied exactly: "+names[i]);
  check(m.leftArmPose==source.leftArmPose&&m.rightArmPose==source.rightArmPose&&m.isSneak==source.isSneak&&m.isRiding==source.isRiding,"native arm/sneak/riding state copied");
 }
 static void setup()throws Exception{
  Bootstrap.register();FMLCommonHandler.instance().beginLoading(net.minecraftforge.fml.server.FMLServerHandler.instance());ModMetadata meta=new ModMetadata();meta.modId="hbm";meta.name="Native armor models";meta.version="probe";Loader.instance().setupTestHarness(new DummyModContainer(meta));
  Method init=MaterialTextureAtlasSpriteNativeTest.class.getDeclaredMethod("initializeNativeClientSettings");init.setAccessible(true);init.invoke(null);mc=Minecraft.getMinecraft();OpenGlHelper.initializeTextures();
  textures=new TextureManager(new SimpleReloadableResourceManager(new MetadataSerializer()));field(mc,"renderEngine",textures);
  ModelManager models=new ModelManager(new TextureMap("textures"));field(models,"modelRegistry",new net.minecraft.util.registry.RegistrySimple<>());RenderItem item=new RenderItem(textures,models,new ItemColors());manager=new RenderManager(textures,item);manager.options=mc.gameSettings;field(mc,"renderManager",manager);field(mc,"renderItem",item);
  for(Field f:ResourceManager.class.getFields())if(f.getType()==ResourceLocation.class){DynamicTexture texture=new DynamicTexture(1,1);texture.getTextureData()[0]=-1;texture.updateDynamicTexture();textures.loadTexture((ResourceLocation)f.get(null),texture);}
  GL11.glMatrixMode(GL11.GL_MODELVIEW);GL11.glLoadIdentity();
 }
 static void fallback(World world)throws Exception{
  EntityItem entity=new EntityItem(world);EntityItem mount=new EntityItem(world);
  check(!(manager.getEntityRenderObject(entity) instanceof RenderBiped),"native non-biped renderer enters actual fallback");
  ModelArmorBase actual=new ModelArmorBase(3);LegacyArmorFallbackOracle original=new LegacyArmorFallbackOracle(3);
  for(boolean ride:new boolean[]{true,false})for(boolean sneak:new boolean[]{true,false})for(ModelBiped.ArmPose left:ModelBiped.ArmPose.values())for(ModelBiped.ArmPose right:ModelBiped.ArmPose.values())for(float swing:new float[]{-10000F,0F,.37F,1F}){
   if(ride)entity.startRiding(mount,true);else entity.dismountRidingEntity();entity.setSneaking(sneak);
   actual.leftArmPose=left;actual.rightArmPose=right;actual.swingProgress=swing;
   original.heldItemLeft=left==ModelBiped.ArmPose.EMPTY?0:left==ModelBiped.ArmPose.BLOCK?3:1;original.heldItemRight=right==ModelBiped.ArmPose.EMPTY?0:right==ModelBiped.ArmPose.BLOCK?3:1;original.aimedBow=left==ModelBiped.ArmPose.BOW_AND_ARROW||right==ModelBiped.ArmPose.BOW_AND_ARROW;original.onGround=swing;
   actual.setRotationAngles(1.7F,.42F,9.3F,27F,-13F,.0625F,entity);original.setRotationAngles(1.7F,.42F,9.3F,27F,-13F,.0625F,entity);
   boolean equal=actual.isSneak==original.isSneak&&actual.isRiding==original.isRiding;for(String name:names)equal&=Arrays.equals(state(part(actual,name)),state(part(original,name)));
   check(equal,"exact original fallback equations/state transitions: "+left+"/"+right+" swing="+swing+" riding="+ride+" sneak="+sneak);
  }
  check(actual.type==3,"original armor type retained");
 }
 static void sourcePose(ModelBiped model,EntityLivingBase entity,float phase){
  model.swingProgress=.35F;model.isSneak=true;model.isRiding=true;model.leftArmPose=ModelBiped.ArmPose.BLOCK;model.rightArmPose=ModelBiped.ArmPose.BOW_AND_ARROW;
  model.setRotationAngles(1.3F,.45F,phase,35,-17,.0625F,entity);
  int i=0;for(ModelRenderer p:nativeParts(model)){p.offsetX=i*.03F;p.offsetY=i*.02F;p.offsetZ=-i*.01F;i++;}
 }
 static void nativeSources(World world)throws Exception{
  ModelArmorBase armor=new ModelArmorBase(0);
  for(String skin:new String[]{"default","slim"}){
   Player player=new Player(world,skin);RenderPlayer renderer=(RenderPlayer)(Render<?>)manager.getEntityRenderObject(player);ModelPlayer main=renderer.getMainModel();sourcePose(main,player,7.2F);
   armor.setModelAttributes(main);armor.setRotationAngles(-99,-99,-99,-99,-99,.0625F,player);copies(armor,main);check(armor.swingProgress==main.swingProgress&&armor.isChild==main.isChild,"native armor attribute stage carries swing/child state");
   sourcePose(main,player,23.1F);armor.setRotationAngles(99,99,99,99,99,.0625F,player);copies(armor,main);
  }
  EntityZombie zombie=new EntityZombie(world);RenderBiped<?> renderer=(RenderBiped<?>)(Render<?>)manager.getEntityRenderObject(zombie);ModelBiped source=(ModelBiped)renderer.getMainModel();sourcePose(source,zombie,13F);armor.setRotationAngles(0,0,0,0,0,.0625F,zombie);copies(armor,source);
 }
 static void wholeDraws(World world)throws Exception{
  Player player=new Player(world,"default");RenderPlayer renderer=(RenderPlayer)(Render<?>)manager.getEntityRenderObject(player);sourcePose(renderer.getMainModel(),player,17F);ModelMan man=new ModelMan();
  float[] before=matrix();ResourceManager.player_manly_af.calls.clear();ResourceManager.player_manly_af.matrices.clear();
  man.render(player,0,0,0,0,0,.0625F);copies(man,renderer.getMainModel());check(ResourceManager.player_manly_af.calls.equals(Arrays.asList("Head","Body","LeftArm","RightArm","LeftLeg","RightLeg")),"complete ordinary man render retains all six mesh draws/order");check(Arrays.equals(before,matrix()),"ordinary man render balances actual GL matrix");
  RenderPlayer explicit=new RenderPlayer(manager,true);sourcePose(explicit.getMainModel(),player,43F);for(ModelRenderer p:nativeParts(explicit.getMainModel()))p.isHidden=true;
  ResourceManager.player_manly_af.calls.clear();ResourceManager.player_manly_af.matrices.clear();man.render(player,0,0,0,0,0,.0625F,explicit);
  for(int i=0;i<6;i++)check(Arrays.equals(pose(nativeParts(explicit.getMainModel())[i]),pose(part(man,names[i]))),"explicit renderer overload overrides manager pose "+names[i]);
  check(ResourceManager.player_manly_af.calls.size()==6,"native skin hiding does not accidentally hide replacement man mesh");check(Arrays.equals(before,matrix()),"explicit man render balances matrix");
  ModelRendererObj arm=man.rightArm;GL11.glPushMatrix();arm.postRender(.0625F);check(!Arrays.equals(before,matrix()),"public man arm remains usable by original first-person postRender caller");GL11.glPopMatrix();
  String[][] expected={{"Head"},{"Body","LeftArm","RightArm"},{"LeftLeg","RightLeg"},{"LeftFoot","RightFoot"}};
  for(int type=0;type<4;type++){
   ResourceManager.armor_hev.calls.clear();ModelArmorHEV hev=new ModelArmorHEV(type);hev.render(player,0,0,0,0,0,.0625F);copies(hev,renderer.getMainModel());check(ResourceManager.armor_hev.calls.equals(Arrays.asList(expected[type])),"unchanged full HEV descendant draws exact armor slot "+type);check(Arrays.equals(before,matrix()),"HEV actual matrix balanced "+type);
  }
  GlStateManager.disableLighting();man.render(player,0,0,0,0,0,.0625F,explicit);check(GL11.glIsEnabled(GL11.GL_LIGHTING),"man retains original lighting enable");GlStateManager.disableLighting();check(!GL11.glIsEnabled(GL11.GL_LIGHTING),"following native lighting disable must not see a stale cache");
  man.leftArm.doRender=false;ResourceManager.player_manly_af.calls.clear();man.render(player,0,0,0,0,0,.0625F,explicit);check(!ResourceManager.player_manly_af.calls.contains("LeftArm")&&ResourceManager.player_manly_af.calls.size()==5,"public OBJ per-part doRender control preserved");
  check(GL11.glGetError()==0,"whole native model/descendant draws have no GL errors");
 }
 public static void main(String[] args)throws Exception{
  Constructor<?> c=Class.forName("MaterialTextureAtlasSpriteNativeTest$Context").getDeclaredConstructor();c.setAccessible(true);
  try(AutoCloseable gl=(AutoCloseable)c.newInstance()) {setup();World world=new WorldFixture();fallback(world);nativeSources(world);wholeDraws(world);}
  System.out.println("PASS "+checks+" native armor/man pose, original fallback, public API and complete model-render checks. NTM mesh/world services are declared fixtures; layer/event/game acceptance is separate.");
 }
}
