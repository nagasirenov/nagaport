import com.hbm.main.ResourceManager;
import com.hbm.items.weapon.sedna.ItemGunBaseNT;
import com.hbm.items.armor.IArmorDisableModel;
import com.hbm.packet.PermaSyncHandler;
import com.hbm.render.item.nativeitem.StaticMeshItemRegistration;
import com.hbm.render.item.nativeitem.context.*;
import com.hbm.render.item.weapon.sedna.ItemRenderCoilgun;
import com.hbm.render.item.weapon.sedna.nativeplayer.*;
import java.lang.reflect.*;
import java.util.*;
import net.minecraft.client.*;
import net.minecraft.client.entity.*;
import net.minecraft.client.model.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.entity.layers.*;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.entity.*;
import net.minecraft.entity.player.*;
import net.minecraft.init.*;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.profiler.Profiler;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.*;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.storage.*;
import net.minecraftforge.client.event.*;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.*;
import net.minecraftforge.fml.common.eventhandler.*;
import org.lwjgl.opengl.GL11;

public class SednaPlayerNativeTest extends ContextItemNativeTest {
 static class WorldFixture extends World {
  WorldFixture(){super(new SaveHandlerMP(),new WorldInfo(new NBTTagCompound()),new WorldProviderSurface(),new Profiler(),true);provider.setWorld(this);}
  protected IChunkProvider createChunkProvider(){throw new AssertionError("No chunk service");}protected boolean isChunkLoaded(int x,int z,boolean empty){return false;}
  public net.minecraft.block.state.IBlockState getBlockState(BlockPos p){return Blocks.AIR.getDefaultState();}
 }
 static class Player extends EntityPlayerSP {
  String skin="default";EnumHandSide side=EnumHandSide.RIGHT;
  Player(Minecraft mc,World world){super(mc,world,new net.minecraft.client.network.NetHandlerPlayClient(mc,null,new net.minecraft.network.NetworkManager(net.minecraft.network.EnumPacketDirection.CLIENTBOUND),new com.mojang.authlib.GameProfile(new UUID(3,4),"SednaProbe")),new net.minecraft.stats.StatisticsManager(),new net.minecraft.stats.RecipeBook());}
  public String getSkinType(){return skin;}public boolean isSpectator(){return false;}public boolean isCreative(){return false;}public boolean isUser(){return false;}
  public ResourceLocation getLocationSkin(){return ResourceManager.coilgun_tex;}public EnumHandSide getPrimaryHand(){return side;}
  public boolean isWearing(EnumPlayerModelParts p){return false;}
 }
 static class Gun extends ItemRenderCoilgun {
  boolean akimbo,left,fail;int secondary;final List<ItemStack> seen=new ArrayList<>();final List<EntityLivingBase> holders=new ArrayList<>();
  public boolean isAkimbo(EntityLivingBase e){return akimbo;}public boolean isLeftHanded(){return left;}
  public void renderEquippedAkimbo(ItemStack stack,EntityLivingBase e){secondary++;seen.add(stack);holders.add(e);super.renderEquippedAkimbo(stack,e);if(fail)throw new IllegalStateException("secondary failure");}
 }
 static class Arm extends ModelRenderer {
  int draws;float lastY;boolean fail;
  Arm(ModelBase model){super(model);}
  @Override public void render(float scale){if(!isHidden&&showModel){draws++;lastY=rotateAngleY;if(fail)throw new IllegalStateException("arm failure");}super.render(scale);}
 }
 static class HiddenArmor extends Item implements IArmorDisableModel {public boolean disablesPart(EntityPlayer p,ItemStack s,EnumPlayerPart part){return part==EnumPlayerPart.LEFT_ARM;}}
 static class UnknownModel extends ModelPlayer {UnknownModel(){super(0,false);}}
 static class UnknownLayer implements LayerRenderer<EntityLivingBase> {public void doRenderLayer(EntityLivingBase e,float a,float b,float c,float d,float f,float g,float h){}public boolean shouldCombineTextures(){return false;}}
 static void named(Object o,Class<?> c,String name,Object value)throws Exception{Field f=c.getDeclaredField(name);f.setAccessible(true);f.set(o,value);}
 static List<LayerRenderer<?>> layers(RenderPlayer r)throws Exception{return (List<LayerRenderer<?>>)get(r,RenderLivingBase.class,List.class);}
 static class Setup {
  final Fixture f=new Fixture();final ContextRenderItem itemRenderer;final RenderPlayer normal=new RenderPlayer(f.manager),slim=new RenderPlayer(f.manager,true);final Gun gun=new Gun();
  final Item item=new ItemGunBaseNT().setRegistryName("test","sedna"+serial++);final ModelResourceLocation key=StaticMeshItemRegistration.modelLocation(item);final Model original=new Model();final ContextItemRegistration registration;final List<String>warnings=new ArrayList<>();final Player player;
  Setup()throws Exception{this(true);}
  Setup(boolean install)throws Exception{
   itemRenderer=install?f.install():null;f.mc.entityRenderer=(EntityRenderer)unsafe.allocateInstance(EntityRenderer.class);named(f.mc.entityRenderer,EntityRenderer.class,"fogColorBuffer",org.lwjgl.BufferUtils.createFloatBuffer(16));
   Map<String,RenderPlayer> skins=new HashMap<>();skins.put("default",normal);skins.put("slim",slim);named(f.manager,RenderManager.class,"skinMap",skins);set(f.manager,RenderManager.class,RenderPlayer.class,normal);
   f.registry.putObject(key,original);f.mc.getRenderItem().getItemModelMesher().register(item,s->key);registration=new ContextItemRegistration(Collections.singletonMap(item,gun),warnings::add);registration.bake(new ModelBakeEvent(f.models,f.registry,null));
   WorldFixture world=new WorldFixture();player=new Player(f.mc,world);f.mc.player=player;f.manager.world=world;f.manager.renderViewEntity=player;player.setHeldItem(EnumHand.MAIN_HAND,new ItemStack(item,9,3));
   for(ResourceLocation texture:Arrays.asList(ResourceManager.coilgun_tex,ResourceManager.flaregun_tex,ResourceManager.player_manly_tex))f.textures.loadTexture(texture,new DynamicTexture(1,1));
  }
  boolean install(){return new SednaPlayerOwnership(registration,itemRenderer,warnings::add).install();}
 }
 static void ownership()throws Exception{
  Setup s=new Setup();ModelPlayer old=s.normal.getMainModel();ModelRenderer head=old.bipedHead;List<LayerRenderer<?>> oldLayers=layers(s.normal);check(s.install(),"separate player transaction activates: "+s.warnings);check(s.normal.getMainModel() instanceof SednaPlayerModel&&s.slim.getMainModel() instanceof SednaPlayerModel&&s.normal.getMainModel().bipedHead==head,"default/slim replacement retains cached native head identity");check(s.registration.isActive()&&s.item.getTileEntityItemStackRenderer()==s.gun&&s.f.mc.getRenderItem()==s.itemRenderer,"Sedna activated without replacing existing contextual renderer");
  for(Field field:ModelPlayer.class.getDeclaredFields())if(!Modifier.isStatic(field.getModifiers())){field.setAccessible(true);check(Objects.equals(field.get(old),field.get(s.normal.getMainModel())),"native ModelPlayer field identity retained "+field.getName());}
  Setup rejected=new Setup();ModelPlayer before=rejected.normal.getMainModel();List<LayerRenderer<?>> beforeLayers=layers(rejected.normal);rejected.slim.addLayer(new UnknownLayer());check(!rejected.install(),"unknown player layer rejected");check(rejected.normal.getMainModel()==before&&layers(rejected.normal)==beforeLayers&&!rejected.registration.isActive()&&rejected.f.registry.getObject(rejected.key)==rejected.original&&rejected.f.mc.getRenderItem()==rejected.itemRenderer&&rejected.f.registration.isActive(),"unknown rejection preserves ordinary models and prior Boltgun registration");check(rejected.warnings.size()==1,"unsupported ownership explicitly warns");
  Setup model=new Setup();set(model.slim,RenderLivingBase.class,ModelBase.class,new UnknownModel());check(!model.install()&&model.normal.getMainModel().getClass()==ModelPlayer.class,"unknown native model subtype left untouched");
  Setup rollback=new Setup();ModelPlayer normal=rollback.normal.getMainModel(),slim=rollback.slim.getMainModel();List<LayerRenderer<?>> normalLayers=layers(rollback.normal),slimLayers=layers(rollback.slim);Object oldTeisr=rollback.item.getTileEntityItemStackRenderer();rollback.f.registry.fail=true;check(!rollback.install(),"controlled model-registry commit failure rejected");check(rollback.normal.getMainModel()==normal&&rollback.slim.getMainModel()==slim&&layers(rollback.normal)==normalLayers&&layers(rollback.slim)==slimLayers&&rollback.item.getTileEntityItemStackRenderer()==oldTeisr&&!rollback.registration.isActive()&&rollback.f.registry.getObject(rollback.key)==rollback.original&&rollback.f.mc.getRenderItem()==rollback.itemRenderer,"all player/layer/model/TEISR slots roll back together; Boltgun preserved");
  Setup mismatch=new Setup();set(mismatch.f.mc,Minecraft.class,RenderItem.class,mismatch.f.original);check(!mismatch.install()&&mismatch.normal.getMainModel().getClass()==ModelPlayer.class,"missing exact installed renderer identity rejects before any player mutation");
 }
 public static class Cancellation {boolean cancel;int post;@SubscribeEvent public void pre(RenderPlayerEvent.Pre e){if(cancel)e.setCanceled(true);}@SubscribeEvent public void post(RenderPlayerEvent.Post e){post++;}}
 static void renderPhases()throws Exception{
  Setup s=new Setup();LayerBipedArmor armor=null;for(LayerRenderer<?> layer:layers(s.normal))if(layer instanceof LayerBipedArmor)armor=(LayerBipedArmor)layer;check(s.install(),"phase fixture installation");ModelPlayer model=s.normal.getMainModel();Arm left=new Arm(model),right=new Arm(model);model.bipedLeftArm=left;model.bipedRightArm=right;
  s.gun.akimbo=true;s.player.rotationYawHead=20;s.player.renderYawOffset=3;s.player.rotationPitch=10;ResourceManager.coilgun.parts.clear();
  Cancellation cancel=new Cancellation();MinecraftForge.EVENT_BUS.register(cancel);
  try{
   s.normal.doRender(s.player,0,0,0,0,.4F);
   check(cancel.post==1&&left.draws==4&&right.draws==1,"actual RenderPlayer phases hide base secondary arm then render four original manual armor-phase arms");
   check(Math.abs(left.lastY-(.1F+model.bipedHead.rotateAngleY))<.000001F&&s.gun.secondary==1&&s.gun.seen.get(0)==s.player.getHeldItemMainhand()&&s.gun.holders.get(0)==s.player,"actual pose/held layer retains akimbo arm Y, real stack and holder");
   check(!left.isHidden&&!right.isHidden&&ResourceManager.coilgun.parts.size()==2,"base arm visibility restored and native primary plus secondary weapon both draw");
   int draws=left.draws,secondary=s.gun.secondary;cancel.cancel=true;s.normal.doRender(s.player,0,0,0,0,.5F);check(left.draws==draws&&s.gun.secondary==secondary&&cancel.post==1,"canceled native player frame does not acquire pose/layer side effects");cancel.cancel=false;
   ModelBiped armorModel=armor.getModelFromSlot(EntityEquipmentSlot.CHEST);Arm armorLeft=new Arm(armorModel),armorRight=new Arm(armorModel);armorModel.bipedLeftArm=armorLeft;armorModel.bipedRightArm=armorRight;s.f.textures.loadTexture(new ResourceLocation("minecraft:textures/models/armor/iron_layer_1.png"),new DynamicTexture(1,1));ItemStack plate=new ItemStack(Items.IRON_CHESTPLATE);plate.addEnchantment(net.minecraft.init.Enchantments.PROTECTION,1);s.player.setItemStackToSlot(EntityEquipmentSlot.CHEST,plate);s.normal.doRender(s.player,0,0,0,0,.4F);check(armorLeft.draws==3&&armorRight.draws==3&&Math.abs(armorLeft.lastY-(.5F+model.bipedHead.rotateAngleY))<.000001F,"actual native armor slot and both glint passes retain effective old vanilla bow Y, not temporary skin-arm Y");s.player.setItemStackToSlot(EntityEquipmentSlot.CHEST,ItemStack.EMPTY);
   s.gun.left=true;s.gun.akimbo=false;left.draws=right.draws=0;s.normal.doRender(s.player,0,0,0,0,.2F);check(left.draws==4&&right.draws==4,"left-handed weapon retains four manual draws of both hidden base arms");
   s.gun.left=false;s.gun.akimbo=true;s.player.side=EnumHandSide.LEFT;left.draws=right.draws=0;s.normal.doRender(s.player,0,0,0,0,.2F);check(left.draws==1&&right.draws==4,"left-primary akimbo uses mirrored physical secondary arm");
   s.player.side=EnumHandSide.RIGHT;ItemStack off=s.player.getHeldItemMainhand();s.player.setHeldItem(EnumHand.MAIN_HAND,ItemStack.EMPTY);s.player.setHeldItem(EnumHand.OFF_HAND,off);left.draws=right.draws=0;int prior=s.gun.secondary;s.normal.doRender(s.player,0,0,0,0,.2F);check(left.draws==1&&right.draws==4&&s.gun.secondary==prior+1&&s.gun.seen.get(s.gun.seen.size()-1)==off,"offhand-only akimbo retains mirrored skin and actual second weapon draw");s.player.setHeldItem(EnumHand.MAIN_HAND,off);s.player.setHeldItem(EnumHand.OFF_HAND,ItemStack.EMPTY);
   s.gun.left=false;s.gun.akimbo=false;left.draws=right.draws=0;PermaSyncHandler.boykissers.add(s.player.getEntityId());ResourceManager.player_manly_af.parts.clear();s.normal.doRender(s.player,0,0,0,0,.7F);check(left.draws==0&&right.draws==0&&ResourceManager.player_manly_af.parts.size()==24,"actual native armor phases preserve four complete six-part ModelMan draws and suppress vanilla skin");PermaSyncHandler.boykissers.clear();
   s.player.setHeldItem(EnumHand.MAIN_HAND,ItemStack.EMPTY);left.draws=right.draws=0;s.normal.doRender(s.player,0,0,0,0,.3F);check(left.draws==1&&right.draws==1,"next ordinary player frame is unaffected by preceding custom/manly contexts");
   s.player.setItemStackToSlot(EntityEquipmentSlot.CHEST,new ItemStack(new HiddenArmor()));left.draws=right.draws=0;s.normal.doRender(s.player,0,0,0,0,.3F);check(left.draws==0&&right.draws==1&&!left.isHidden,"actual native body retains armor-disable policy and restores visibility for next player");s.player.setItemStackToSlot(EntityEquipmentSlot.CHEST,ItemStack.EMPTY);
   s.player.setHeldItem(EnumHand.MAIN_HAND,new ItemStack(s.item));s.gun.akimbo=true;right.fail=true;boolean hidden=left.isHidden;boolean failed=false;try(NativeProjectionScope scope=new NativeProjectionScope()){try{model.render(s.player,0,0,2,0,0,.0625F);}catch(IllegalStateException expected){failed=true;}}check(failed&&left.isHidden==hidden,"lexical body visibility restored after actual model draw failure");right.fail=false;
   // The actual vanilla first-person arm phase must not inherit full-body bow pose.
   s.normal.renderRightArm(s.player);check(right.rotateAngleY==0,"ordinary native first-person arm phase is not changed by body-only weapon pose");
  }finally{PermaSyncHandler.boykissers.clear();MinecraftForge.EVENT_BUS.unregister(cancel);}
 }

 static Set<Object> subscribers()throws Exception{Field field=net.minecraftforge.fml.common.eventhandler.EventBus.class.getDeclaredField("listeners");field.setAccessible(true);return new HashSet<>(((Map<?,?>)field.get(MinecraftForge.EVENT_BUS)).keySet());}
 static void integration()throws Exception{
  for(int mode=0;mode<3;mode++){
   Set<Object> before=subscribers();Setup s=new Setup(false);if(mode==1)s.slim.addLayer(new UnknownLayer());if(mode==2)set(s.f.mc,Minecraft.class,RenderItem.class,new OtherRenderItem(s.f));
   SednaProxyNativeFixture proxy=new SednaProxyNativeFixture();proxy.nativeContextItems=s.f.registration;proxy.nativeSednaItems=s.registration;proxy.nativeOneMeshItems=Collections.newSetFromMap(new IdentityHashMap<Item,Boolean>());proxy.nativeOneMeshItems.add(Items.APPLE);proxy.onLoadComplete();Set<Object> added=subscribers();added.removeAll(before);
   check(proxy.nativeItemOwnershipFinished&&proxy.nativeOneMeshItems.contains(Items.APPLE)&&proxy.nativeOneMeshItems.contains(s.f.item)==(mode!=2)&&proxy.nativeOneMeshItems.contains(s.item)==(mode==0),"actual extracted load-complete success/companion-abort/context-abort union "+mode);
   check(s.f.registration.isActive()==(mode!=2)&&s.registration.isActive()==(mode==0),"actual load-complete gates independent registrations "+mode);
   int subscriptions=added.size();proxy.onLoadComplete();Set<Object> repeated=subscribers();repeated.removeAll(before);check(repeated.size()==subscriptions,"actual load-complete one-shot guard does not subscribe twice "+mode);
   int dropped=0;for(Object listener:added)if(listener instanceof com.hbm.render.item.nativeitem.staticfamily.StaticItemEntityOwnership){dropped++;Field owned=listener.getClass().getDeclaredField("ownedItems");owned.setAccessible(true);check(new HashSet<>((List<Item>)owned.get(listener)).equals(proxy.nativeOneMeshItems),"actual deferred dropped owner captures committed-only union "+mode);}
   check(dropped==1&&s.f.manager.entityRenderMap.get(net.minecraft.entity.item.EntityItem.class).getClass()==RenderEntityItem.class,"actual dropped ownership remains deferred beyond model/player preflight "+mode);
   for(Object listener:added)MinecraftForge.EVENT_BUS.unregister(listener);
  }
  SednaProxyNativeFixture missing=new SednaProxyNativeFixture();try{missing.onLoadComplete();throw new AssertionError("missing registry guard");}catch(IllegalStateException expected){}check(!missing.nativeItemOwnershipFinished,"actual missing-registration guard fails before one-shot state");
 }
 public static void main(String[]args)throws Exception{
  Field u=sun.misc.Unsafe.class.getDeclaredField("theUnsafe");u.setAccessible(true);unsafe=(sun.misc.Unsafe)u.get(null);Constructor<?> ctor=Class.forName("MaterialTextureAtlasSpriteNativeTest$Context").getDeclaredConstructor();ctor.setAccessible(true);
  try(AutoCloseable context=(AutoCloseable)ctor.newInstance()){
   Method init=Class.forName("MaterialTextureAtlasSpriteNativeTest").getDeclaredMethod("initializeNativeClientSettings");init.setAccessible(true);init.invoke(null);Bootstrap.register();FMLCommonHandler.instance().beginLoading(net.minecraftforge.fml.server.FMLServerHandler.instance());ModMetadata metadata=new ModMetadata();metadata.modId="hbm";metadata.name="Sedna player probe";metadata.version="1";DummyModContainer owner=new DummyModContainer(metadata);Loader.instance().setupTestHarness(owner);Loader.instance().setActiveModContainer(owner);OpenGlHelper.initializeTextures();GL11.glMatrixMode(GL11.GL_MODELVIEW);
   ownership();renderPhases();integration();check(GL11.glGetError()==0,"native GL error-free");System.out.println("PASS "+checks+" native Sedna player ownership/rollback/render-phase checks. No whole-game acceptance.");
  }
 }
}
