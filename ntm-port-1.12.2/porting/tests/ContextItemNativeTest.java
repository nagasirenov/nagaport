import com.hbm.render.item.ItemRenderBoltgun;
import com.hbm.render.item.nativeitem.*;
import com.hbm.render.item.nativeitem.context.*;
import com.hbm.main.ResourceManager;
import com.hbm.render.anim.HbmAnimations;
import java.util.*;
import java.lang.reflect.*;
import javax.vecmath.Matrix4f;
import net.minecraft.client.*;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.entity.*;
import net.minecraft.entity.item.*;
import net.minecraft.entity.projectile.*;
import net.minecraft.init.*;
import net.minecraft.item.*;
import net.minecraft.nbt.*;
import net.minecraft.util.*;
import net.minecraft.util.registry.*;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import sun.misc.Unsafe;

public class ContextItemNativeTest {
 static int checks,serial;static Unsafe unsafe;
 static void check(boolean ok,String s){checks++;if(!ok)throw new AssertionError(s);}
 static Field field(Class<?> owner,Class<?> type){Field found=null;for(Field f:owner.getDeclaredFields())if(!Modifier.isStatic(f.getModifiers())&&f.getType()==type){if(found!=null)throw new AssertionError("ambiguous "+type);found=f;}if(found==null)throw new AssertionError("missing "+type);found.setAccessible(true);return found;}
 static void set(Object owner,Class<?> declaration,Class<?> type,Object value)throws Exception{field(declaration,type).set(owner,value);}
 static Object get(Object owner,Class<?> declaration,Class<?> type)throws Exception{return field(declaration,type).get(owner);}
 static Matrix4f matrix(){java.nio.FloatBuffer b=BufferUtils.createFloatBuffer(16);GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX,b);float[] f=new float[16];b.get(f);Matrix4f m=new Matrix4f(f);m.transpose();return m;}
 static Matrix4f identity(){Matrix4f m=new Matrix4f();m.setIdentity();return m;}
 static class Player extends EntityPlayerSP {
  Player(){super(null,null,null,null,null);}
  @Override public boolean isSneaking(){return false;}
 }
 static class Model implements IBakedModel {
  int quads;final boolean builtin;final TextureAtlasSprite sprite=new TextureAtlasSprite("test:particle"){};
  Model(){this(false);}Model(boolean builtin){this.builtin=builtin;}
  public List<BakedQuad> getQuads(net.minecraft.block.state.IBlockState s,EnumFacing f,long seed){quads++;return Collections.emptyList();}
  public boolean isAmbientOcclusion(){return false;}public boolean isGui3d(){return true;}public boolean isBuiltInRenderer(){return builtin;}
  public TextureAtlasSprite getParticleTexture(){return sprite;}public ItemCameraTransforms getItemCameraTransforms(){return ItemCameraTransforms.DEFAULT;}public ItemOverrideList getOverrides(){return ItemOverrideList.NONE;}
 }
 static class Recorder extends ContextItemRenderer {
  final List<ItemStack> stacks=new ArrayList<>();final List<NativeItemContext> contexts=new ArrayList<>();final List<Matrix4f> matrices=new ArrayList<>();
  Runnable nested;boolean fail;int nesting;
  public Matrix4f localTransform(ItemStack stack,NativeItemContext context){return identity();}
  protected void draw(ItemStack stack,NativeItemContext context){stacks.add(stack);contexts.add(context);matrices.add(matrix());GL11.glShadeModel(GL11.GL_SMOOTH);if(nested!=null&&nesting==0){nesting++;try{nested.run();}finally{nesting--;}}if(fail)throw new IllegalStateException("controlled");check(contexts.get(contexts.size()-1)==context||nesting==0,"lexical context");}
  void clear(){stacks.clear();contexts.clear();matrices.clear();}
 }
 static class FailRegistry extends RegistrySimple<ModelResourceLocation,IBakedModel>{boolean fail,alwaysFail;@Override public void putObject(ModelResourceLocation key,IBakedModel value){if(fail||alwaysFail){fail=false;throw new IllegalStateException("controlled registry write");}super.putObject(key,value);}}
 static class OtherRenderItem extends RenderItem {OtherRenderItem(Fixture f){super(f.textures,f.models,f.mc.getItemColors());}}
 static class OtherEntityItem extends RenderEntityItem {OtherEntityItem(RenderManager m,RenderItem r){super(m,r);}}
 static class Fixture {
  final Minecraft mc=Minecraft.getMinecraft();final TextureManager textures;final ModelManager models;final RenderItem original;final RenderManager manager;final Item item;final Recorder recorder=new Recorder();final Model originalModel=new Model();final ModelResourceLocation key;final FailRegistry registry=new FailRegistry();final List<String>warnings=new ArrayList<>();final ContextItemRegistration registration;final List<Object> owners=new ArrayList<>();final List<Class<?>> declarations=new ArrayList<>();
  Fixture()throws Exception {
   mc.currentScreen=null;mc.ingameGUI=null;
   textures=new TextureManager(null);textures.loadTexture(TextureMap.LOCATION_BLOCKS_TEXTURE,new DynamicTexture(1,1));textures.loadTexture(new ResourceLocation("textures/misc/enchanted_item_glint.png"),new DynamicTexture(1,1));textures.loadTexture(ResourceManager.boltgun_tex,new DynamicTexture(1,1));
   set(mc,Minecraft.class,TextureManager.class,textures);set(mc,Minecraft.class,ItemColors.class,new ItemColors());TextureMap map=new TextureMap("textures");set(mc,Minecraft.class,TextureMap.class,map);
   models=new ModelManager(map);set(models,ModelManager.class,IRegistry.class,registry);original=new RenderItem(textures,models,mc.getItemColors());set(mc,Minecraft.class,RenderItem.class,original);
   manager=(RenderManager)unsafe.allocateInstance(RenderManager.class);set(manager,RenderManager.class,TextureManager.class,textures);manager.options=mc.gameSettings;Field entity=RenderManager.class.getDeclaredField("entityRenderMap");entity.setAccessible(true);entity.set(manager,new HashMap<Class<? extends Entity>,Render<? extends Entity>>());set(mc,Minecraft.class,RenderManager.class,manager);
   set(mc,Minecraft.class,ItemRenderer.class,new ItemRenderer(mc));owner(mc,Minecraft.class);owner(mc.getItemRenderer(),ItemRenderer.class);
   RenderEntityItem dropped=new RenderEntityItem(manager,original);manager.entityRenderMap.put(EntityItem.class,dropped);owner(dropped,RenderEntityItem.class);
   RenderItemFrame frame=new RenderItemFrame(manager,original);manager.entityRenderMap.put(EntityItemFrame.class,frame);owner(frame,RenderItemFrame.class);
   RenderSnowball<EntitySnowball> snow=new RenderSnowball<>(manager,Items.SNOWBALL,original);manager.entityRenderMap.put(EntitySnowball.class,snow);owner(snow,RenderSnowball.class);
   RenderPotion potion=new RenderPotion(manager,original);manager.entityRenderMap.put(EntityPotion.class,potion);owner(potion,RenderSnowball.class);
   item=new Item().setRegistryName("test","context"+serial++);key=StaticMeshItemRegistration.modelLocation(item);registry.putObject(key,originalModel);original.getItemModelMesher().register(item,stack->key);
   registration=new ContextItemRegistration(Collections.singletonMap(item,recorder),warnings::add);registration.bake(new net.minecraftforge.client.event.ModelBakeEvent(models,registry,null));
   mc.player=(Player)unsafe.allocateInstance(Player.class);
  }
  void owner(Object o,Class<?> c){owners.add(o);declarations.add(c);}
  void ownersAre(RenderItem r)throws Exception{for(int i=0;i<owners.size();i++)check(get(owners.get(i),declarations.get(i),RenderItem.class)==r,"cache "+declarations.get(i).getSimpleName());}
  ContextRenderItem install()throws Exception{check(!registration.isActive()&&registry.getObject(key)==originalModel,"bake inactive before transactional ownership");NativeItemRendererOwnership owner=new NativeItemRendererOwnership(registration);check(owner.install(),"installer succeeds "+warnings);check(owner.install(),"idempotent installation");ownersAre(owner.getInstalled());check(registration.isActive()&&registry.getObject(key) instanceof ContextItemModel&&item.getTileEntityItemStackRenderer()==recorder,"models/TEISR activate together");check(owner.getInstalled().getItemModelMesher()==original.getItemModelMesher(),"original mesher identity shared");for(Field f:RenderItem.class.getDeclaredFields())if(!Modifier.isStatic(f.getModifiers())){f.setAccessible(true);check(Objects.equals(f.get(original),f.get(owner.getInstalled())),"all original native RenderItem field state retained "+f.getName());}return owner.getInstalled();}
 }
 static void ownership()throws Exception {
  Fixture good=new Fixture();ContextRenderItem installed=good.install();Model newModel=new Model();good.registry.putObject(good.key,newModel);good.original.onResourceManagerReload(null);check(installed.getItemModelWithOverrides(new ItemStack(good.item),null,null)==newModel,"original reload listener refreshes shared replacement mesher");
  RegistrySimple<ModelResourceLocation,IBakedModel> reloaded=new RegistrySimple<>();reloaded.putObject(good.key,newModel);set(good.models,ModelManager.class,IRegistry.class,reloaded);good.registration.bake(new net.minecraftforge.client.event.ModelBakeEvent(good.models,reloaded,null));good.original.onResourceManagerReload(null);IBakedModel current=installed.getItemModelWithOverrides(new ItemStack(good.item),null,null);check(current instanceof ContextItemModel&&((com.hbm.render.util.NativeItemBaseModel)current).getBaseModel()==newModel,"active native reload refreshes custom model and retained base together");
  net.minecraft.client.gui.GuiIngame hud=new net.minecraft.client.gui.GuiIngame(good.mc);check(get(hud,net.minecraft.client.gui.GuiIngame.class,RenderItem.class)==installed,"future native HUD captures replacement");net.minecraft.client.gui.GuiScreen screen=new net.minecraft.client.gui.GuiScreen(){};screen.setWorldAndResolution(good.mc,10,10);check(get(screen,net.minecraft.client.gui.GuiScreen.class,RenderItem.class)==installed,"future native screen captures replacement");
  Fixture unknown=new Fixture();unknown.manager.entityRenderMap.put(EntityItem.class,new OtherEntityItem(unknown.manager,unknown.original));check(!new NativeItemRendererOwnership(unknown.registration).install(),"unknown inherited entity cache rejects");unknown.ownersAre(unknown.original);check(!unknown.registration.isActive()&&unknown.registry.getObject(unknown.key)==unknown.originalModel&&unknown.warnings.size()==1,"unknown rejection preserves models and warns");
  Fixture inherited=new Fixture();inherited.manager.entityRenderMap.put(EntityEgg.class,new OtherEntityItem(inherited.manager,inherited.original));check(!new NativeItemRendererOwnership(inherited.registration).install()&&inherited.mc.getRenderItem()==inherited.original,"unknown non-primary renderer with inherited cache rejects");
  Fixture mismatch=new Fixture();set(mismatch.owners.get(2),RenderEntityItem.class,RenderItem.class,installed);check(!new NativeItemRendererOwnership(mismatch.registration).install(),"mismatched identity rejects");check(mismatch.mc.getRenderItem()==mismatch.original&&mismatch.registry.getObject(mismatch.key)==mismatch.originalModel,"mismatch leaves global/models unchanged");
  Fixture rollback=new Fixture();Object oldTeisr=rollback.item.getTileEntityItemStackRenderer();rollback.registry.fail=true;check(!new NativeItemRendererOwnership(rollback.registration).install(),"controlled registry failure aborts transaction");rollback.ownersAre(rollback.original);check(!rollback.registration.isActive()&&rollback.registry.getObject(rollback.key)==rollback.originalModel&&rollback.item.getTileEntityItemStackRenderer()==oldTeisr,"registry failure rolls back models and TEISR");
  Fixture fatal=new Fixture();fatal.registry.alwaysFail=true;try{new NativeItemRendererOwnership(fatal.registration).install();throw new AssertionError("expected unrecoverable rollback");}catch(IllegalStateException expected){check(expected.getMessage().contains("rollback failed"),"unrecoverable foreign registry rollback fails closed");}fatal.ownersAre(fatal.original);
  Fixture modded=new Fixture();OtherRenderItem other=new OtherRenderItem(modded);set(modded.mc,Minecraft.class,RenderItem.class,other);check(!new NativeItemRendererOwnership(modded.registration).install()&&modded.mc.getRenderItem()==other&&modded.registry.getObject(modded.key)==modded.originalModel,"unknown global renderer remains untouched");
  Fixture late=new Fixture();late.mc.ingameGUI=(net.minecraft.client.gui.GuiIngame)unsafe.allocateInstance(net.minecraft.client.gui.GuiIngame.class);check(!new NativeItemRendererOwnership(late.registration).install()&&late.mc.getRenderItem()==late.original,"late GUI window rejects");
  Fixture missing=new Fixture();missing.manager.entityRenderMap.remove(EntityItemFrame.class);check(!new NativeItemRendererOwnership(missing.registration).install()&&missing.mc.getRenderItem()==missing.original,"missing primary frame owner rejects");
 }
 static void paths()throws Exception {
  Fixture f=new Fixture();ContextRenderItem r=f.install();ItemStack stack=new ItemStack(f.item,17,3);NBTTagCompound tag=new NBTTagCompound();tag.setString("probe","kept");stack.setTagCompound(tag);EntityLivingBase holder=(Player)unsafe.allocateInstance(Player.class);
  for(TransformType type:TransformType.values()){f.recorder.clear();r.renderItem(stack,holder,type,type==TransformType.FIRST_PERSON_LEFT_HAND||type==TransformType.THIRD_PERSON_LEFT_HAND);check(f.recorder.stacks.size()==1&&f.recorder.stacks.get(0)==stack&&f.recorder.contexts.get(0).holder==holder&&f.recorder.contexts.get(0).transform==type,"actual native holder path "+type);}
  f.recorder.clear();f.mc.getItemRenderer().renderItemSide(holder,stack,TransformType.FIRST_PERSON_LEFT_HAND,true);check(f.recorder.contexts.size()==1&&f.recorder.contexts.get(0).holder==holder&&f.recorder.contexts.get(0).transform==TransformType.FIRST_PERSON_LEFT_HAND,"native first-person cached owner invokes replacement");
  f.recorder.clear();r.renderItem(stack,TransformType.GROUND);check(f.recorder.contexts.get(0).holder==null&&f.recorder.contexts.get(0).transform==TransformType.GROUND,"actual holderless native path");
  f.recorder.clear();r.renderItemAndEffectIntoGUI(holder,stack,5,9);check(f.recorder.contexts.get(0).holder==holder&&f.recorder.contexts.get(0).transform==TransformType.GUI,"actual native GUI override/perspective path");
  
  f.recorder.clear();EntityItem dropped=new EntityItem(null,0,0,0,new ItemStack(f.item));((RenderEntityItem)f.manager.entityRenderMap.get(EntityItem.class)).doRender(dropped,0,0,0,0,0);check(f.recorder.stacks.size()==1&&f.recorder.stacks.get(0)==dropped.getItem()&&f.recorder.contexts.get(0).transform==TransformType.GROUND,"actual native dropped renderer cache reaches typed model draw");
  f.recorder.clear();r.getItemModelMesher().register(Items.SNOWBALL,s->f.key);((RenderSnowball)f.manager.entityRenderMap.get(EntitySnowball.class)).doRender(new EntitySnowball((net.minecraft.world.World)null),0,0,0,0,0);check(f.recorder.stacks.size()==1&&f.recorder.stacks.get(0).getItem()==Items.SNOWBALL&&f.recorder.contexts.get(0).transform==TransformType.GROUND,"actual native snowball cached renderer reaches typed draw");
  f.recorder.clear();EntityPotion potion=new EntityPotion((net.minecraft.world.World)null);potion.setItem(new ItemStack(Items.SPLASH_POTION));r.getItemModelMesher().register(Items.SPLASH_POTION,s->f.key);((RenderPotion)f.manager.entityRenderMap.get(EntityPotion.class)).doRender(potion,0,0,0,0,0);check(f.recorder.stacks.size()==1&&f.recorder.stacks.get(0)==potion.getPotion()&&f.recorder.contexts.get(0).transform==TransformType.GROUND,"actual native potion inherited cache reaches typed draw");
  ItemStack inner=new ItemStack(f.item,2,7);EntityLivingBase second=(Player)unsafe.allocateInstance(Player.class);f.recorder.clear();f.recorder.nested=()->r.renderItem(inner,second,TransformType.FIXED,false);r.renderItem(stack,holder,TransformType.FIRST_PERSON_RIGHT_HAND,false);check(f.recorder.stacks.equals(Arrays.asList(stack,inner))&&f.recorder.contexts.get(0).holder==holder&&f.recorder.contexts.get(1).holder==second&&f.recorder.contexts.get(0).transform==TransformType.FIRST_PERSON_RIGHT_HAND,"actual nested virtual draws isolate context and stack");
  check(!GL11.glIsEnabled(org.lwjgl.opengl.GL12.GL_RESCALE_NORMAL),"nested native cleanup leaves rescale disabled");
  f.recorder.fail=true;GL11.glLoadIdentity();GL11.glTranslatef(2,3,4);Matrix4f before=matrix();int depth=GL11.glGetInteger(GL11.GL_MODELVIEW_STACK_DEPTH);try{r.renderItem(stack,holder,TransformType.FIRST_PERSON_LEFT_HAND,true);throw new AssertionError("missing failure");}catch(IllegalStateException expected){}check(matrix().equals(before)&&GL11.glGetInteger(GL11.GL_MODELVIEW_STACK_DEPTH)==depth,"native outer paths restore matrices after nested failure");
  
  f.recorder.nested=null;r.zLevel=23;try{r.renderItemAndEffectIntoGUI(holder,stack,5,9);throw new AssertionError("missing GUI failure");}catch(net.minecraft.util.ReportedException expected){}check(matrix().equals(before)&&GL11.glGetInteger(GL11.GL_MODELVIEW_STACK_DEPTH)==depth&&r.zLevel==23,"native GUI failure restores matrix depth and zLevel");
  f.recorder.fail=false;f.recorder.nested=null;f.recorder.clear();r.renderItem(inner,TransformType.GROUND);check(f.recorder.stacks.get(0)==inner&&f.recorder.contexts.get(0).holder==null,"next draw has no stale holder/stack");
  f.recorder.clear();f.recorder.renderByItem(inner);check(f.recorder.contexts.get(0).transform==TransformType.NONE&&f.recorder.contexts.get(0).holder==null,"direct TEISR explicitly lacks context");
  check(stack.getCount()==17&&stack.getMetadata()==3&&stack.getTagCompound()==tag&&tag.getString("probe").equals("kept"),"actual stack count/meta/NBT untouched");
  
  ContextItemModel root=(ContextItemModel)f.registry.getObject(f.key);IBakedModel abandoned=root.getOverrides().handleItemState(root,stack,null,holder).handlePerspective(TransformType.FIRST_PERSON_LEFT_HAND).getLeft();IBakedModel actual=root.getOverrides().handleItemState(root,inner,null,second).handlePerspective(TransformType.FIXED).getLeft();f.recorder.clear();r.renderItem(inner,actual);r.renderItem(stack,abandoned);check(f.recorder.stacks.equals(Arrays.asList(inner,stack))&&f.recorder.contexts.get(0).holder==second&&f.recorder.contexts.get(1).holder==holder,"abandoned and reordered perspective calls cannot poison typed draws");check(((com.hbm.render.util.NativeItemBaseModel)actual).getBaseModel()==f.originalModel&&((com.hbm.render.util.NativeItemBaseModel)abandoned).getBaseModel()==f.originalModel,"original base model retained through override and perspective snapshots");
  ItemStack enchanted=new ItemStack(Items.DIAMOND_SWORD);enchanted.addEnchantment(Enchantments.SHARPNESS,1);Model model=new Model();f.original.renderItem(enchanted,model);int expected=model.quads;model.quads=0;r.renderItem(enchanted,model);check(expected>7&&model.quads==expected,"unaffected native enchantment glint executes exact native model passes");
 }

 static void boltLocal(TransformType type){
  if(type==TransformType.FIRST_PERSON_LEFT_HAND||type==TransformType.FIRST_PERSON_RIGHT_HAND){GL11.glTranslated(.5,.35,-.25F);GL11.glRotated(15,0,0,1);GL11.glRotated(80,0,1,0);GL11.glScaled(.15,.15,.15);}
  else if(type==TransformType.THIRD_PERSON_LEFT_HAND||type==TransformType.THIRD_PERSON_RIGHT_HAND){GL11.glScaled(.25,.25,.25);GL11.glRotated(10,0,1,0);GL11.glRotated(10,0,0,1);GL11.glRotated(10,1,0,0);GL11.glTranslated(1.5,-.25,1);}
  else if(type==TransformType.GUI){GL11.glTranslated(7,10,0);GL11.glRotated(-90,0,1,0);GL11.glRotated(-135,1,0,0);GL11.glScaled(1.75,1.75,-1.75);}
  else GL11.glScaled(.1,.1,.1);
 }
 static void boltgun()throws Exception {
  Fixture f=new Fixture();ContextRenderItem r=f.install();ItemRenderBoltgun bolt=new ItemRenderBoltgun();ContextItemModel model=new ContextItemModel(f.originalModel.sprite,bolt);f.registry.putObject(f.key,model);ItemStack stack=new ItemStack(f.item,7,4);Player holder=(Player)unsafe.allocateInstance(Player.class);
  for(TransformType type:TransformType.values()) {GL11.glLoadIdentity();boltLocal(type);check(matrix().epsilonEquals(bolt.localTransform(stack,new NativeItemContext(type,holder)),.000001F),"original Boltgun local transform "+type);ResourceManager.boltgun.parts.clear();ResourceManager.boltgun.matrices.clear();HbmAnimations.recoil=.75;f.mc.player.isSwingInProgress=true;holder.isSwingInProgress=true;r.renderItem(stack,holder,type,false);boolean first=type==TransformType.FIRST_PERSON_LEFT_HAND||type==TransformType.FIRST_PERSON_RIGHT_HAND;check(ResourceManager.boltgun.parts.equals(first?Arrays.asList("Barrel","Gun"):Arrays.asList("Gun","Barrel")),"original Boltgun part order "+type);check(f.mc.player.isSwingInProgress!=first&&holder.isSwingInProgress,"original local-only swing side effect "+type);if(first){Matrix4f expected=new Matrix4f(ResourceManager.boltgun.matrices.get(1));expected.mul(new ItemTransformMatrix().translate(0,0,-.75).build());check(expected.epsilonEquals(ResourceManager.boltgun.matrices.get(0),.00001F),"original barrel-only recoil "+type);}}
 }

 static void auditNativeOwners()throws Exception {
  final String desc="Lnet/minecraft/client/renderer/RenderItem;";final Set<String> owners=new TreeSet<>(),writes=new TreeSet<>();final int[] synthetic={0},derived={0},retainedReads={0};final List<String> initOrder=new ArrayList<>(),loadOrder=new ArrayList<>(),modelReload=new ArrayList<>(),listenerRegistration=new ArrayList<>();
  java.io.File file=new java.io.File(RenderItem.class.getProtectionDomain().getCodeSource().getLocation().toURI());
  try(java.util.jar.JarFile jar=new java.util.jar.JarFile(file)){Enumeration<java.util.jar.JarEntry> entries=jar.entries();while(entries.hasMoreElements()){java.util.jar.JarEntry entry=entries.nextElement();if(!entry.getName().endsWith(".class"))continue;try(java.io.InputStream in=jar.getInputStream(entry)){new org.objectweb.asm.ClassReader(in).accept(new org.objectweb.asm.ClassVisitor(org.objectweb.asm.Opcodes.ASM5){String owner;
   public void visit(int v,int access,String name,String sig,String parent,String[]interfaces){owner=name;if("net/minecraft/client/renderer/RenderItem".equals(parent))derived[0]++;}
   public org.objectweb.asm.FieldVisitor visitField(int access,String name,String type,String sig,Object value){if(desc.equals(type)){if((access&org.objectweb.asm.Opcodes.ACC_SYNTHETIC)!=0)synthetic[0]++;else owners.add(owner+"."+name);}return null;}
   public org.objectweb.asm.MethodVisitor visitMethod(int access,String name,String descriptor,String sig,String[]exceptions){return new org.objectweb.asm.MethodVisitor(org.objectweb.asm.Opcodes.ASM5){
    public void visitFieldInsn(int op,String target,String field,String type){if((op==org.objectweb.asm.Opcodes.PUTFIELD||op==org.objectweb.asm.Opcodes.PUTSTATIC)&&desc.equals(type)&&!target.startsWith("net/minecraft/client/renderer/RenderItem$"))writes.add(target+"."+field+":"+owner+"."+name);if(op==org.objectweb.asm.Opcodes.GETFIELD&&target.startsWith("net/minecraft/client/renderer/RenderItem$")&&desc.equals(type))retainedReads[0]++;}
    public void visitMethodInsn(int op,String target,String method,String type,boolean itf){if(owner.equals("net/minecraft/client/Minecraft")&&name.equals("init"))initOrder.add(target+"."+method);if(owner.equals("net/minecraftforge/fml/client/FMLClientHandler")&&name.equals("finishMinecraftLoading"))loadOrder.add(target+"."+method);if(owner.equals("net/minecraft/client/renderer/block/model/ModelManager")&&name.equals("onResourceManagerReload"))modelReload.add(target+"."+method);if(owner.equals("net/minecraft/client/resources/SimpleReloadableResourceManager")&&name.equals("registerReloadListener"))listenerRegistration.add(target+"."+method);}
   };}
  },org.objectweb.asm.ClassReader.SKIP_DEBUG|org.objectweb.asm.ClassReader.SKIP_FRAMES);}}}
  check(owners.equals(new TreeSet<>(Arrays.asList("net/minecraft/client/Minecraft.renderItem","net/minecraft/client/gui/GuiIngame.itemRenderer","net/minecraft/client/gui/GuiScreen.itemRender","net/minecraft/client/renderer/ItemRenderer.itemRenderer","net/minecraft/client/renderer/entity/RenderEntityItem.itemRenderer","net/minecraft/client/renderer/entity/RenderItemFrame.itemRenderer","net/minecraft/client/renderer/entity/RenderSnowball.itemRenderer"))),"exhaustive actual jar audit finds exactly seven native cache declarations");
  Set<String> expectedWrites=new TreeSet<>();for(String owner:owners){String declaration=owner.substring(0,owner.lastIndexOf('.'));String method=declaration.equals("net/minecraft/client/Minecraft")?"init":declaration.equals("net/minecraft/client/gui/GuiScreen")?"setWorldAndResolution":"<init>";expectedWrites.add(owner+":"+declaration+"."+method);}check(writes.equals(expectedWrites),"all native cache assignments occur only in audited construction/init methods");
  check(synthetic[0]==10&&retainedReads[0]==0&&derived[0]==0,"ten inert captured renderer references never read; no hidden native subclass");
  check(initOrder.indexOf("net/minecraftforge/fml/client/FMLClientHandler.finishMinecraftLoading")>=0&&initOrder.indexOf("net/minecraftforge/fml/client/FMLClientHandler.finishMinecraftLoading")<initOrder.indexOf("net/minecraftforge/client/GuiIngameForge.<init>"),"actual native loading completes before HUD captures renderer");
  int modelCtor=initOrder.indexOf("net/minecraft/client/renderer/block/model/ModelManager.<init>"),itemCtor=initOrder.indexOf("net/minecraft/client/renderer/RenderItem.<init>");check(modelCtor>=0&&modelCtor<itemCtor&&initOrder.subList(modelCtor,itemCtor).contains("net/minecraft/client/resources/IReloadableResourceManager.registerReloadListener")&&listenerRegistration.contains("net/minecraft/client/resources/IResourceManagerReloadListener.onResourceManagerReload")&&modelReload.contains("net/minecraftforge/client/ForgeHooksClient.onModelBake"),"native initial model bake is synchronous before RenderItem construction/load complete");
  int mods=loadOrder.indexOf("net/minecraftforge/fml/common/Loader.initializeMods"),entities=loadOrder.indexOf("net/minecraftforge/fml/client/registry/RenderingRegistry.loadEntityRenderers"),complete=loadOrder.indexOf("net/minecraftforge/fml/common/Loader.loadingComplete");check(mods>=0&&mods<entities&&entities<complete,"actual Forge load-complete follows entity registry ownership");
 }
 public static void main(String[]args)throws Exception {
  Field u=Unsafe.class.getDeclaredField("theUnsafe");u.setAccessible(true);unsafe=(Unsafe)u.get(null);Constructor<?> ctor=Class.forName("MaterialTextureAtlasSpriteNativeTest$Context").getDeclaredConstructor();ctor.setAccessible(true);
  try(AutoCloseable context=(AutoCloseable)ctor.newInstance()){Method init=Class.forName("MaterialTextureAtlasSpriteNativeTest").getDeclaredMethod("initializeNativeClientSettings");init.setAccessible(true);init.invoke(null);Bootstrap.register();GL11.glMatrixMode(GL11.GL_MODELVIEW);auditNativeOwners();ownership();paths();boltgun();check(GL11.glGetError()==0,"native GL error-free");System.out.println("PASS "+checks+" actual native context/ownership/rollback/reload/GL/stack/part checks. Controlled assets and holders, no full-game acceptance.");}
 }
}
