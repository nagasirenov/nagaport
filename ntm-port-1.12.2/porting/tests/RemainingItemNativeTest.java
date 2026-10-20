import java.io.*;
import java.lang.reflect.*;
import java.util.*;
import javax.vecmath.Matrix4f;
import sun.misc.Unsafe;
import com.hbm.main.ResourceManager;
import com.hbm.items.ModItems;
import com.hbm.items.special.ItemHot;
import com.hbm.items.weapon.ItemCustomMissilePart;
import com.hbm.render.item.*;
import com.hbm.render.item.weapon.*;
import com.hbm.render.item.nativeitem.context.*;
import com.hbm.render.mesh.*;
import com.hbm.render.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.client.resources.*;
import net.minecraft.client.resources.data.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.*;
import net.minecraft.item.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import org.lwjgl.opengl.GL11;

/** Real native callback/GL evidence. NTM gameplay owner seams are listed in the runner. */
public final class RemainingItemNativeTest {
 static int checks,draws;static Unsafe unsafe;static boolean fail;static List<Record> records=new ArrayList<>();
 static Minecraft mc;static ContextRenderItem owner;static IBakedModel ordinary;static net.minecraft.util.registry.RegistrySimple<ModelResourceLocation,IBakedModel> registry;static int serial;
 static void check(boolean yes,String label){checks++;if(!yes)throw new AssertionError(label);}
 static void set(Object o,Class<?> c,String n,Object v)throws Exception{Field f=c.getDeclaredField(n);f.setAccessible(true);f.set(o,v);}
 static Matrix4f matrix(){java.nio.FloatBuffer b=org.lwjgl.BufferUtils.createFloatBuffer(16);GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX,b);float[] f=new float[16];b.get(f);Matrix4f m=new Matrix4f(f);m.transpose();return m;}
 static void mult(Matrix4f m){java.nio.FloatBuffer b=org.lwjgl.BufferUtils.createFloatBuffer(16);b.put(new float[]{m.m00,m.m10,m.m20,m.m30,m.m01,m.m11,m.m21,m.m31,m.m02,m.m12,m.m22,m.m32,m.m03,m.m13,m.m23,m.m33}).flip();GL11.glMultMatrix(b);}
 static class Record {
  final String mesh,part;final Matrix4f matrix=RemainingItemNativeTest.matrix();final int texture=GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D),shade=GL11.glGetInteger(GL11.GL_SHADE_MODEL);final float x=OpenGlHelper.lastBrightnessX,y=OpenGlHelper.lastBrightnessY;final boolean light=GL11.glIsEnabled(GL11.GL_LIGHTING),cull=GL11.glIsEnabled(GL11.GL_CULL_FACE);final float pump;final boolean rescale=GL11.glIsEnabled(org.lwjgl.opengl.GL12.GL_RESCALE_NORMAL);
  Record(String m,String p,float pump){mesh=m;part=p;this.pump=pump;}
 }
 static void record(String mesh,String part,float pump){records.add(new Record(mesh,part,pump));draws++;if(fail)throw new IllegalStateException("controlled original geometry failure");}
 static class Mesh implements MeshModel {
  final String name;final MeshModel actual;Mesh(String n,MeshModel a){name=n;actual=a;}
  public ObjMesh getMesh(){return actual.getMesh();}public List<String> getPartNames(){return actual.getPartNames();}
  public void renderAll(){record(name,"ALL",0);actual.renderAll();}public void renderPart(String n){record(name,n,0);actual.renderPart(n);}
  public void renderOnly(String...n){record(name,Arrays.toString(n),0);actual.renderOnly(n);}public void renderAllExcept(String...n){record(name,"EXCEPT"+Arrays.toString(n),0);actual.renderAllExcept(n);}
 }
 static class Player extends EntityPlayerSP {
  ItemStack main=ItemStack.EMPTY,off=ItemStack.EMPTY;boolean active,left;
  Player(){super(null,null,null,null,null);}
  @Override public boolean isSneaking(){return false;}
  @Override public ItemStack getHeldItemMainhand(){return main;}
  @Override public ItemStack getHeldItemOffhand(){return off;}
  @Override public boolean isHandActive(){return active;}
  @Override public ItemStack getActiveItemStack(){return active?main:ItemStack.EMPTY;}
  @Override public EnumHandSide getPrimaryHand(){return left?EnumHandSide.LEFT:EnumHandSide.RIGHT;}
 }
 static class BlockingItem extends Item {
  BlockingItem(){setFull3D();setMaxDamage(100);}
  @Override public EnumAction getItemUseAction(ItemStack s){return EnumAction.BLOCK;}
 }
 static class Big extends com.hbm.render.model.ModelBigSword {@Override public void render(Entity e,float a,float b,float c,float d,float f,float s){record("big","JAVA",0);super.render(e,a,b,c,d,f,s);}}
 static class Sword extends com.hbm.render.model.ModelSword {@Override public void render(Entity e,float a,float b,float c,float d,float f,float s){record("sword","JAVA",0);super.render(e,a,b,c,d,f,s);}}
 static float pump(com.hbm.render.model.ModelB92 m){try{Field f=com.hbm.render.model.ModelB92.class.getDeclaredField("Pump1");f.setAccessible(true);return ((net.minecraft.client.model.ModelRenderer)f.get(m)).offsetX;}catch(Exception e){throw new RuntimeException(e);}}
 static class B92 extends com.hbm.render.model.ModelB92 {@Override public void render(Entity e,float a,float b,float c,float d,float f,float s){record("b92","JAVA",pump(this));super.render(e,a,b,c,d,f,s);}}
 static void models(ContextItemRenderer renderer)throws Exception{
  if(renderer instanceof ItemRenderBigSword)set(renderer,ItemRenderBigSword.class,"bigSwordModel",new Big());
  if(renderer instanceof ItemRenderRedstoneSword)set(renderer,ItemRenderRedstoneSword.class,"swordModel",new Sword());
  if(renderer instanceof ItemRenderGunAnim)set(renderer,ItemRenderGunAnim.class,"b92",new B92());
 }
 static ContextItemModel register(Item item,ContextItemRenderer renderer){ContextItemModel m=ContextItemModel.create(ordinary,renderer);ModelResourceLocation key=new ModelResourceLocation("probe:item"+(serial++),"inventory");registry.putObject(key,m);owner.getItemModelMesher().register(item,s->key);return m;}
 static void clean(){GL11.glMatrixMode(GL11.GL_MODELVIEW);GL11.glLoadIdentity();OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit,71,113);GL11.glColor4f(1,1,1,1);GL11.glDisable(GL11.GL_BLEND);GL11.glEnable(GL11.GL_LIGHTING);GL11.glDisable(GL11.GL_CULL_FACE);GL11.glShadeModel(GL11.GL_FLAT);records.clear();}
 static List<Record> invoke(ContextItemRenderer renderer,ItemStack stack,NativeItemContext context){clean();int depth=GL11.glGetInteger(GL11.GL_MODELVIEW_STACK_DEPTH);int attr=GL11.glGetInteger(GL11.GL_ATTRIB_STACK_DEPTH);renderer.render(stack,context);check(GL11.glGetInteger(GL11.GL_MODELVIEW_STACK_DEPTH)==depth&&GL11.glGetInteger(GL11.GL_ATTRIB_STACK_DEPTH)==attr,"balanced callback scope");check(OpenGlHelper.lastBrightnessX==71&&OpenGlHelper.lastBrightnessY==113,"native lightmap restore");check(GL11.glGetError()==0,"actual original geometry GL clean");return new ArrayList<>(records);}
 static void compare(List<Record> a,List<Record>b,String label){check(a.size()==b.size(),label+" draw count "+a.size()+"/"+b.size());for(int i=0;i<a.size();i++){Record x=a.get(i),y=b.get(i);check(x.mesh.equals(y.mesh)&&x.part.equals(y.part),label+" original ordered model/part");check(x.matrix.epsilonEquals(y.matrix,.00005F),label+" original ordered GL transform "+i);check(x.texture==y.texture&&x.shade==y.shade&&x.light==y.light&&x.cull==y.cull&&x.x==y.x&&x.y==y.y&&x.pump==y.pump,label+" original material/light/shade/animation");}}
 static void weapons(Player player)throws Exception{
  NativeOriginalItemRenderer[] actual={new ItemRenderBigSword(),new ItemRenderRedstoneSword(),new ItemRenderGunAnim(),new ItemRenderShim(),new ItemRenderGavel(),new ItemRenderChainsaw(){@Override protected long animationTimeMillis(){return 123456;}},new ItemRenderCrucible()};
  NativeOriginalItemRenderer[] original={new OracleBigSword(),new OracleRedstoneSword(),new OracleGunAnim(),new OracleShim(),new OracleGavel(),new OracleChainsaw(),new OracleCrucible()};
  Item[][] variants={{new BlockingItem()},{new BlockingItem()},{ModItems.gun_b92},{ModItems.shimmer_sledge,ModItems.shimmer_axe,ModItems.stopsign,ModItems.sopsign,ModItems.chernobylsign},{ModItems.wood_gavel,ModItems.lead_gavel,ModItems.diamond_gavel,ModItems.mese_gavel},{ModItems.chainsaw},{new BlockingItem()}};
  for(int i=0;i<actual.length;i++){models(actual[i]);models(original[i]);for(Item item:variants[i])for(boolean active:new boolean[]{false,true}){
   ItemStack stack=new ItemStack(item,37,active?item.getMaxDamage():0);NBTTagCompound nbt=new NBTTagCompound();nbt.setInteger("animation",17);nbt.setBoolean("nativeProbeFuel",active);stack.setTagCompound(nbt);player.main=stack;player.active=active;ContextItemModel root=register(item,actual[i]);
   for(TransformType type:TransformType.values()){
    NativeItemContext context=new NativeItemContext(type,player);IBakedModel selected=root.getOverrides().handleItemState(root,stack,null,player).handlePerspective(type).getLeft();
    if(!actual[i].handlesPerspective(stack,type)){check(!(selected instanceof ContextItemModel),"declined GUI retains entire base");continue;}
    List<Record> expected=invoke(original[i],stack,context),observed=invoke(actual[i],stack,context);compare(expected,observed,i+" "+type+" "+active);
    clean();owner.renderItem(stack,selected);check(!records.isEmpty()&&GL11.glGetError()==0,"actual native model override/perspective/draw invocation "+type);
   }
   check(stack.getCount()==37&&stack.getTagCompound()==nbt&&nbt.getInteger("animation")==17,"stack identity/count/animation NBT retained");
  }}
  for(int i=0;i<actual.length;i++){
   ItemStack stack=new ItemStack(variants[i][0],64);player.main=stack;
   fail=true;clean();int depth=GL11.glGetInteger(GL11.GL_MODELVIEW_STACK_DEPTH),attrs=GL11.glGetInteger(GL11.GL_ATTRIB_STACK_DEPTH);
   try{actual[i].render(stack,new NativeItemContext(TransformType.FIRST_PERSON_RIGHT_HAND,player));throw new AssertionError("missing failure");}catch(IllegalStateException expected){}finally{fail=false;}
   check(GL11.glGetInteger(GL11.GL_MODELVIEW_STACK_DEPTH)==depth&&GL11.glGetInteger(GL11.GL_ATTRIB_STACK_DEPTH)==attrs,"nested original geometry failure restores scopes");check(OpenGlHelper.lastBrightnessX==71&&OpenGlHelper.lastBrightnessY==113,"failed emissive draw restores lightmap");
   invoke(actual[i],stack,new NativeItemContext(TransformType.FIRST_PERSON_RIGHT_HAND,player));
  }
 }
 static void missiles(Player player)throws Exception{
  // Invoke the actual complete registry, then retain all renderer-captured part identities.
  NativeMissileLifecycle lifecycle=new NativeMissileLifecycle();lifecycle.initialize();Map<Integer,MissilePart> before=new HashMap<>(MissilePart.parts);lifecycle.postLoad();check(before.equals(MissilePart.parts),"later post-load call preserves captured original parts");check(before.size()>100,"complete original missile registration table");
  int id=20000;for(MissilePart part:before.values()){net.minecraftforge.fml.common.registry.ForgeRegistries.ITEMS.register(part.part.setRegistryName(new ResourceLocation("minecraft","remaining_item_probe_"+(id++))));check(part.model!=null&&part.texture!=null,"native client table has original model and material");ItemRenderMissilePart renderer=new ItemRenderMissilePart(part){@Override protected long animationTimeMillis(){return 123456;}};ItemStack stack=new ItemStack(part.part,64);ContextItemModel root=register(part.part,renderer);player.main=stack;OracleMissilePart original=new OracleMissilePart(part);
   for(TransformType type:TransformType.values()){
    IBakedModel selected=root.getOverrides().handleItemState(root,stack,null,player).handlePerspective(type).getLeft();
    if(!part.renderInventoryModel){check(!(selected instanceof ContextItemModel)&&!renderer.handlesDropped(stack),"hidden original part uses complete ordinary native fallback");continue;}
    compare(invoke(original,stack,new NativeItemContext(type,player)),invoke(renderer,stack,new NativeItemContext(type,player)),"original missile-part transform "+type);clean();owner.renderItem(stack,selected);check(records.size()==1&&GL11.glGetError()==0,"complete real missile part draw "+type);check(stack.getCount()==64,"part stack count unchanged");
   }
  }
  MissilePart nullModel=MissilePart.registerPart(new ItemCustomMissilePart(ItemCustomMissilePart.PartType.WARHEAD),ItemCustomMissilePart.PartType.WARHEAD,1,1);check(!new ItemRenderMissilePart(nullModel).handlesDropped(new ItemStack(nullModel.part)),"model-less part declines rather than invisible draw");
  ItemRenderMissile missile=new ItemRenderMissile(){@Override protected long animationTimeMillis(){return 123456;}};ItemStack stack=new ItemStack(ModItems.missile_custom,37);NBTTagCompound tag=new NBTTagCompound();tag.setInteger("warhead",Item.getIdFromItem(ModItems.mp_warhead_10_he));tag.setInteger("fuselage",Item.getIdFromItem(ModItems.mp_fuselage_10_kerosene));tag.setInteger("stability",Item.getIdFromItem(ModItems.mp_stability_10_flat));tag.setInteger("thruster",Item.getIdFromItem(ModItems.mp_thruster_10_kerosene));stack.setTagCompound(tag);ContextItemModel root=register(stack.getItem(),missile);player.main=stack;
  for(TransformType type:TransformType.values()){compare(invoke(new OracleMissile(),stack,new NativeItemContext(type,player)),invoke(missile,stack,new NativeItemContext(type,player)),"original assembled missile transform "+type);clean();owner.renderItem(stack,root.getOverrides().handleItemState(root,stack,null,player).handlePerspective(type).getLeft());check(records.size()==4,"original assembled thruster/fins/fuselage/warhead order "+type);check(records.get(0).mesh.equals("mp_t_10_kerosene")&&records.get(1).mesh.equals("mp_s_10_flat")&&records.get(2).mesh.equals("mp_f_10_kerosene")&&records.get(3).mesh.equals("mp_w_10_he"),"original multipart materials and mesh order");check(stack.getCount()==37&&stack.getTagCompound()==tag,"missile input unchanged");}
  check(!missile.handlesDropped(new ItemStack(Items.DIAMOND)),"invalid missile declines native dropped dispatch");
 }
 static float fieldFloat(Object o,String n)throws Exception{Field f=net.minecraft.client.renderer.ItemRenderer.class.getDeclaredField(n);f.setAccessible(true);return f.getFloat(o);}
 static class ForeignHand extends net.minecraft.client.renderer.ItemRenderer{ForeignHand(){super(mc);}}
 static void handState(Player player)throws Exception{
  net.minecraft.client.renderer.ItemRenderer hand=new net.minecraft.client.renderer.ItemRenderer(mc);set(mc,Minecraft.class,"itemRenderer",hand);
  ItemStack main=new ItemStack(new BlockingItem()),off=new ItemStack(new BlockingItem());player.main=main;player.off=off;
  for(boolean leftDominant:new boolean[]{false,true})for(boolean mainHand:new boolean[]{false,true})for(float[] progress:new float[][]{{.8F,.2F},{.2F,.8F},{1,1}}){
   player.left=leftDominant;String current=mainHand?"equippedProgressMainHand":"equippedProgressOffHand",prev=mainHand?"prevEquippedProgressMainHand":"prevEquippedProgressOffHand";
   set(hand,net.minecraft.client.renderer.ItemRenderer.class,"equippedProgressMainHand",.37F);set(hand,net.minecraft.client.renderer.ItemRenderer.class,"prevEquippedProgressMainHand",.37F);set(hand,net.minecraft.client.renderer.ItemRenderer.class,"equippedProgressOffHand",.37F);set(hand,net.minecraft.client.renderer.ItemRenderer.class,"prevEquippedProgressOffHand",.37F);set(hand,net.minecraft.client.renderer.ItemRenderer.class,current,progress[1]);set(hand,net.minecraft.client.renderer.ItemRenderer.class,prev,progress[0]);
   TransformType type=(mainHand==leftDominant)?TransformType.FIRST_PERSON_LEFT_HAND:TransformType.FIRST_PERSON_RIGHT_HAND;
   float[] read=NativeWeaponHandState.snapEquip(mainHand?main:off,new NativeItemContext(type,player));float snap=progress[0]==progress[1]?progress[1]:progress[1]<progress[0]?0:1;
   check(Arrays.equals(read,progress),"original pre-snap equip values retained for blade condition");check(fieldFloat(hand,current)==snap&&fieldFloat(hand,prev)==snap,"exact selected native hand snaps");check(fieldFloat(hand,mainHand?"equippedProgressOffHand":"equippedProgressMainHand")==.37F,"other hand untouched");
  }
  ForeignHand foreign=new ForeignHand();set(mc,Minecraft.class,"itemRenderer",foreign);set(foreign,net.minecraft.client.renderer.ItemRenderer.class,"equippedProgressMainHand",.25F);NativeWeaponHandState.snapEquip(main,new NativeItemContext(TransformType.FIRST_PERSON_RIGHT_HAND,player));check(fieldFloat(foreign,"equippedProgressMainHand")==.25F,"unknown mod first-person renderer never mutated");set(mc,Minecraft.class,"itemRenderer",hand);
  com.hbm.render.anim.BusAnimation animation=new com.hbm.render.anim.BusAnimation().addBus("SWING_ROT",new com.hbm.render.anim.BusAnimationSequence().setPos(10,20,30));
  com.hbm.render.anim.HbmAnimations.hotbar[0][0]=new com.hbm.render.anim.HbmAnimations.Animation(main.getItem().getTranslationKey(),0,animation);player.inventory.currentItem=0;
  check(com.hbm.render.anim.HbmAnimations.getRelevantTransformation(main,player,"SWING_ROT")[0]==10,"actual original hotbar rail drives selected stack");check(com.hbm.render.anim.HbmAnimations.getRelevantTransformation(off,player,"SWING_ROT")[0]==0,"offhand cannot borrow main-hand animation");check(com.hbm.render.anim.HbmAnimations.getRelevantTransformation(main,null,"SWING_ROT")[0]==0,"absent holder cannot borrow local animation");
  com.hbm.render.anim.HbmAnimations.hotbar[0][0]=null;player.left=false;player.active=false;set(mc,Minecraft.class,"itemRenderer",null);
 }
 static void callerAndDropped(Player player)throws Exception{
  NativeOriginalItemRenderer[] renderers={new ItemRenderBigSword(),new ItemRenderGavel()};Item[] items={new BlockingItem(),ModItems.wood_gavel};
  for(int i=0;i<renderers.length;i++){models(renderers[i]);ItemStack stack=new ItemStack(items[i],64);player.main=stack;ContextItemModel model=register(items[i],renderers[i]);
   for(int count:new int[]{1,2,16,64}){stack.setCount(count);EntityItem entity=new EntityItem(null,0,0,0,stack);entity.hoverStart=.73F;clean();renderers[i].renderDropped(entity,3,4,5,17,.25F,false,0);List<Record> observed=new ArrayList<>(records);check(observed.size()==1,"one original dropped mesh regardless of oversized count");
    clean();double bob=i==1?net.minecraft.util.math.MathHelper.sin(.25F/10F+entity.hoverStart)*.1F+.1F:0;GL11.glTranslated(3,4+bob,5);if(i==1)GL11.glRotatef((.25F/20F+entity.hoverStart)*(180F/(float)Math.PI),0,1,0);GL11.glScaled(.5,.5,.5);renderers[i].render(stack,new NativeItemContext(TransformType.GROUND,null));compare(new ArrayList<>(records),observed,"original bob/spin caller "+i);
    clean();renderers[i].renderFrame(entity,3,4,5,17,.25F,NativeMachineItemRenderer.Display.ENTITY_FRAME);compare(observed,new ArrayList<>(records),"machine frame uses original one-mesh helper");
   }
  }
  for(boolean rotate:new boolean[]{false,true})for(boolean block:new boolean[]{false,true}){
   Item item=new BlockingItem(){@Override public boolean shouldRotateAroundWhenRendering(){return rotate;}};ItemStack stack=new ItemStack(item);player.main=stack;player.active=block;NativeItemContext context=new NativeItemContext(TransformType.THIRD_PERSON_RIGHT_HAND,player);
   clean();GL11.glTranslatef(-.0625F,.4375F,.0625F);if(rotate){GL11.glRotatef(180,0,0,1);GL11.glTranslatef(0,-.125F,0);}if(block){GL11.glTranslatef(.05F,0,-.1F);GL11.glRotatef(-50,0,1,0);GL11.glRotatef(-10,1,0,0);GL11.glRotatef(-60,0,0,1);}GL11.glTranslatef(0,.1875F,0);GL11.glScalef(.625F,-.625F,.625F);GL11.glRotatef(-100,1,0,0);GL11.glRotatef(45,0,1,0);GL11.glTranslatef(0,-.3F,0);GL11.glScalef(1.5F,1.5F,1.5F);GL11.glRotatef(50,0,1,0);GL11.glRotatef(335,0,0,1);GL11.glTranslatef(-.9375F,-.0625F,0);Matrix4f expected=matrix();
   clean();GL11.glRotatef(-90,1,0,0);GL11.glRotatef(180,0,1,0);GL11.glTranslatef(.0625F,.125F,-.625F);mult(NativeToolItemPerspectives.compose(stack,context,new com.hbm.render.item.nativeitem.ItemTransformMatrix().build()));GL11.glTranslatef(-.5F,-.5F,-.5F);check(matrix().epsilonEquals(expected,.00005F),"independent original full3D/block/rotate caller compensation");
  }
  player.active=false;
 }
 static class CountRender extends RenderItem {
  final List<Matrix4f> calls=new ArrayList<>();CountRender(){super(mc.getTextureManager(),owner.getItemModelMesher().getModelManager(),mc.getItemColors());}
  @Override public void renderItem(ItemStack stack,IBakedModel model){calls.add(matrix());super.renderItem(stack,model);}
 }
 static void droppedFallback()throws Exception{
  net.minecraft.client.renderer.entity.RenderManager manager=(net.minecraft.client.renderer.entity.RenderManager)unsafe.allocateInstance(net.minecraft.client.renderer.entity.RenderManager.class);set(manager,net.minecraft.client.renderer.entity.RenderManager.class,"renderEngine",mc.getTextureManager());manager.options=mc.gameSettings;
  CountRender renderer=new CountRender();ModelResourceLocation key=new ModelResourceLocation("probe:fallback","inventory");registry.putObject(key,ordinary);renderer.getItemModelMesher().register(Items.DIAMOND,s->key);
  net.minecraft.client.renderer.entity.RenderEntityItem vanilla=new net.minecraft.client.renderer.entity.RenderEntityItem(manager,renderer);
  com.hbm.render.item.nativeitem.staticfamily.SingleMeshEntityItemRenderer extended=new com.hbm.render.item.nativeitem.staticfamily.SingleMeshEntityItemRenderer(manager,renderer,Collections.emptySet(),Collections.singletonMap(Items.DIAMOND,new ItemRenderMissile()));
  for(int count:new int[]{1,2,6,17,33,49,64}){ItemStack stack=new ItemStack(Items.DIAMOND,count);EntityItem entity=new EntityItem(null,0,0,0,stack);entity.hoverStart=.43F;clean();renderer.calls.clear();vanilla.doRender(entity,1,2,3,0,.25F);List<Matrix4f> expected=new ArrayList<>(renderer.calls);clean();renderer.calls.clear();extended.doRender(entity,1,2,3,0,.25F);check(expected.size()==renderer.calls.size()&&!expected.isEmpty(),"declined custom missile uses real native count dispatch");for(int i=0;i<expected.size();i++)check(expected.get(i).epsilonEquals(renderer.calls.get(i),.00001F),"declined custom missile preserves native matrices/spread");check(stack.getCount()==count&&!stack.hasTagCompound(),"declined dispatch never mutates count or creates NBT");}
  final int[] called={0};com.hbm.render.item.nativeitem.staticfamily.SingleMeshEntityItemRenderer.BlockItemDraw old=new com.hbm.render.item.nativeitem.staticfamily.SingleMeshEntityItemRenderer.BlockItemDraw(){public void renderDropped(EntityItem e,double x,double y,double z,float yaw,float p,boolean outlines,int color){called[0]++;}};
  check(old.handlesDropped(new ItemStack(Items.DIAMOND)),"existing block-item delegates default to original owned path");new com.hbm.render.item.nativeitem.staticfamily.SingleMeshEntityItemRenderer(manager,renderer,Collections.emptySet(),Collections.singletonMap(Items.DIAMOND,old)).doRender(new EntityItem(null,0,0,0,new ItemStack(Items.DIAMOND)),0,0,0,0,0);check(called[0]==1,"existing block-item delegate invoked once");
 }
 static final int[] materialFaces={GL11.GL_FRONT,GL11.GL_BACK},materialParameters={GL11.GL_AMBIENT,GL11.GL_DIFFUSE,GL11.GL_SPECULAR,GL11.GL_EMISSION};
 static java.nio.FloatBuffer vector(float... values){java.nio.FloatBuffer b=org.lwjgl.BufferUtils.createFloatBuffer(values.length);b.put(values).flip();return b;}
 static float[] materialSnapshot(){float[] values=new float[32];int offset=0;for(int face:materialFaces)for(int parameter:materialParameters){java.nio.FloatBuffer b=org.lwjgl.BufferUtils.createFloatBuffer(4);GL11.glGetMaterial(face,parameter,b);b.get(values,offset,4);offset+=4;}return values;}
 static float[] colorSnapshot(){java.nio.FloatBuffer b=org.lwjgl.BufferUtils.createFloatBuffer(16);GL11.glGetFloat(GL11.GL_CURRENT_COLOR,b);return new float[]{b.get(0),b.get(1),b.get(2),b.get(3)};}
 static void seedMaterials(int face,int mode,boolean enabled){
  GlStateManager.disableColorMaterial();GlStateManager.resetColor();GlStateManager.color(.91F,.82F,.73F,.64F);
  for(int i=0;i<materialFaces.length;i++)for(int j=0;j<materialParameters.length;j++)GL11.glMaterial(materialFaces[i],materialParameters[j],vector(.11F+.05F*i+.03F*j,.22F+.02F*j,.33F+.01F*i,1));
  GlStateManager.colorMaterial(face,mode);if(enabled)GlStateManager.enableColorMaterial();
 }
 static void materialCacheState()throws Exception{
  Method synchronize=Class.forName("com.hbm.render.item.nativeitem.context.NativeItemFailureScope").getDeclaredMethod("synchronizeNativeCaches");synchronize.setAccessible(true);
  for(boolean enabled:new boolean[]{false,true})for(int face:new int[]{GL11.GL_FRONT,GL11.GL_BACK,GL11.GL_FRONT_AND_BACK})for(int mode:new int[]{GL11.GL_AMBIENT,GL11.GL_DIFFUSE,GL11.GL_SPECULAR,GL11.GL_EMISSION,GL11.GL_AMBIENT_AND_DIFFUSE}){
   RenderHelper.disableStandardItemLighting();seedMaterials(face,mode,enabled);float[] before=materialSnapshot(),color=colorSnapshot();int depth=GL11.glGetInteger(GL11.GL_ATTRIB_STACK_DEPTH);
   GL11.glPushAttrib(GL11.GL_ENABLE_BIT|GL11.GL_LIGHTING_BIT|GL11.GL_CURRENT_BIT);RenderHelper.enableGUIStandardItemLighting();GL11.glPopAttrib();
   check(Arrays.equals(before,materialSnapshot()),"original attrib pop restores both-face material vectors before cache synchronization");synchronize.invoke(null);
   check(Arrays.equals(before,materialSnapshot()),"all 30 enabled/face/mode combinations retain ambient/diffuse/specular/emission vectors");
   check(Arrays.equals(color,colorSnapshot()),"material cache repair retains actual current color");check(GL11.glGetInteger(GL11.GL_ATTRIB_STACK_DEPTH)==depth,"material cache repair balances attrib stack");
   check(GL11.glIsEnabled(GL11.GL_COLOR_MATERIAL)==enabled&&GL11.glGetInteger(GL11.GL_COLOR_MATERIAL_FACE)==face&&GL11.glGetInteger(GL11.GL_COLOR_MATERIAL_PARAMETER)==mode,"material cache repair retains original enable/face/mode");
   RenderHelper.enableStandardItemLighting();check(GL11.glIsEnabled(GL11.GL_LIGHT0)&&GL11.glIsEnabled(GL11.GL_LIGHT1)&&GL11.glIsEnabled(GL11.GL_COLOR_MATERIAL),"next native helper still enables actual GPU lights and color material");
   check(GL11.glGetInteger(GL11.GL_COLOR_MATERIAL_FACE)==GL11.GL_FRONT_AND_BACK&&GL11.glGetInteger(GL11.GL_COLOR_MATERIAL_PARAMETER)==GL11.GL_AMBIENT_AND_DIFFUSE,"next native helper still changes cached material selection");check(GL11.glGetError()==0,"material synchronization GL clean");
  }
  RenderHelper.disableStandardItemLighting();
 }
 static void lightingCaches(Player player)throws Exception{
  for(boolean failure:new boolean[]{false,true})for(boolean enabled:new boolean[]{false,true})for(boolean meteor:new boolean[]{false,true}){
   ContextItemRenderer callback;
   if(meteor)callback=new ItemRendererMeteorSword(1,.5F,0){@Override public void renderGlintFlat(){if(failure)throw new IllegalStateException("controlled meteor GUI failure");super.renderGlintFlat();}};
   else callback=new ItemRendererHot("ingot_steel_dusted"){@Override protected void draw(ItemStack stack,NativeItemContext context){super.draw(stack,context);if(failure)throw new IllegalStateException("controlled hot GUI failure");}};
   ItemStack stack=meteor?new ItemStack(Items.DIAMOND_SWORD):ItemHot.heatUp(new ItemStack(new ItemHot(100)));register(stack.getItem(),callback);
   clean();for(int light=0;light<8;light++){if(enabled)GlStateManager.enableLight(light);else GlStateManager.disableLight(light);}seedMaterials(GL11.GL_FRONT,GL11.GL_SPECULAR,enabled);float[] materials=materialSnapshot(),color=colorSnapshot();int attrs=GL11.glGetInteger(GL11.GL_ATTRIB_STACK_DEPTH);
   try{callback.render(stack,new NativeItemContext(TransformType.GUI,player));check(!failure,"expected controlled GUI failure");}catch(IllegalStateException expected){check(failure,"only controlled GUI failure");}
   check(Arrays.equals(materials,materialSnapshot()),"Hot/Meteor normal and failed callbacks preserve both-face material vectors");check(Arrays.equals(color,colorSnapshot()),"Hot/Meteor normal and failed callbacks preserve current color");check(GL11.glGetInteger(GL11.GL_ATTRIB_STACK_DEPTH)==attrs,"Hot/Meteor normal and failed callbacks balance attrib stack");
   for(int light=0;light<8;light++)check(GL11.glIsEnabled(GL11.GL_LIGHT0+light)==enabled,"all original light GPU enables restored");check(GL11.glIsEnabled(GL11.GL_COLOR_MATERIAL)==enabled,"original material enable restored");check(GL11.glGetInteger(GL11.GL_COLOR_MATERIAL_FACE)==GL11.GL_FRONT&&GL11.glGetInteger(GL11.GL_COLOR_MATERIAL_PARAMETER)==GL11.GL_SPECULAR,"original material face/mode restored");
   RenderHelper.enableStandardItemLighting();check(GL11.glIsEnabled(GL11.GL_LIGHT0)&&GL11.glIsEnabled(GL11.GL_LIGHT1)&&GL11.glIsEnabled(GL11.GL_COLOR_MATERIAL),"subsequent real native helper honors synchronized caches");check(GL11.glGetInteger(GL11.GL_COLOR_MATERIAL_FACE)==GL11.GL_FRONT_AND_BACK&&GL11.glGetInteger(GL11.GL_COLOR_MATERIAL_PARAMETER)==GL11.GL_AMBIENT_AND_DIFFUSE,"subsequent native helper updates cached material face/mode");
   RenderHelper.disableStandardItemLighting();check(!GL11.glIsEnabled(GL11.GL_LIGHT0)&&!GL11.glIsEnabled(GL11.GL_LIGHT1)&&!GL11.glIsEnabled(GL11.GL_COLOR_MATERIAL),"subsequent native disable helper honors synchronized caches");
  }
  ItemRenderBigSword sword=new ItemRenderBigSword();models(sword);ItemStack stack=new ItemStack(new BlockingItem());EntityItem entity=new EntityItem(null,0,0,0,stack);
  for(boolean enabled:new boolean[]{false,true})for(boolean failure:new boolean[]{false,true}){
   if(enabled)GlStateManager.enableRescaleNormal();else GlStateManager.disableRescaleNormal();clean();fail=failure;
   try{sword.renderDropped(entity,1,2,3,0,.5F,false,0);check(!failure,"expected dropped failure");}catch(IllegalStateException expected){check(failure,"controlled dropped mesh failure");}finally{fail=false;}
   check(!records.isEmpty()&&records.get(0).rescale,"original dropped caller enables rescale normals before mesh");check(GL11.glIsEnabled(org.lwjgl.opengl.GL12.GL_RESCALE_NORMAL)==enabled,"dropped rescale restore on normal and failed exit");GlStateManager.enableRescaleNormal();check(GL11.glIsEnabled(org.lwjgl.opengl.GL12.GL_RESCALE_NORMAL),"subsequent native rescale enable honors cache");GlStateManager.disableRescaleNormal();check(!GL11.glIsEnabled(org.lwjgl.opengl.GL12.GL_RESCALE_NORMAL),"subsequent native rescale disable honors cache");
  }
 }
 static void hotAndMeteor(Player player)throws Exception{
  for(String texture:new String[]{"ingot_steel_dusted","ingot_chainsteel","ingot_meteorite","ingot_meteorite_forged","blade_meteorite"}){
   ItemHot hot=new ItemHot(100);ItemStack stack=ItemHot.heatUp(new ItemStack(hot,37),.75);ContextItemModel root=register(hot,new ItemRendererHot(texture));
   for(TransformType type:TransformType.values()){IBakedModel selected=root.getOverrides().handleItemState(root,stack,null,player).handlePerspective(type).getLeft();if(type!=TransformType.GUI)check(!(selected instanceof ContextItemModel),"hot complete ordinary fallback "+type);else{clean();owner.renderItem(stack,selected);ResourceLocation key=new ResourceLocation("hbm","textures/items/"+texture+"_hot.png");check(mc.getTextureManager().getTexture(key) instanceof SimpleTexture,"real original heat overlay loaded");check(GL11.glGetError()==0&&ItemHot.getHeat(stack)==.75,"real native GUI alpha overlay draw preserves heat");}}
  }
  for(float[] color:new float[][]{{1,.5F,0},{.5F,1,1},{.25F,.25F,.25F},{0,.5F,1},{1,1,0},{.5F,1,.5F},{1,1,.5F},{.5F,.5F,0},{.75F,1,0},{1,0,.5F},{0,1,0}}){ItemRendererMeteorSword meteor=new ItemRendererMeteorSword(color[0],color[1],color[2]);ItemStack stack=new ItemStack(Items.DIAMOND_SWORD,1);ContextItemModel root=register(stack.getItem(),meteor);player.main=stack;for(TransformType type:TransformType.values()){IBakedModel selected=root.getOverrides().handleItemState(root,stack,null,player).handlePerspective(type).getLeft();if(!meteor.handlesPerspective(stack,type)){check(!(selected instanceof ContextItemModel),"meteor ordinary entity/frame fallback");continue;}clean();owner.renderItem(stack,selected);check(GL11.glGetError()==0,"always-on original colored HBM glint even without enchantment "+type);check(!stack.hasEffect(),"custom meteor shimmer does not add enchantment NBT");}}
 }
 public static void main(String[] args)throws Exception{
  Field u=Unsafe.class.getDeclaredField("theUnsafe");u.setAccessible(true);unsafe=(Unsafe)u.get(null);
  Constructor<?> c=Class.forName("MaterialTextureAtlasSpriteNativeTest$Context").getDeclaredConstructor();c.setAccessible(true);
  try(AutoCloseable gl=(AutoCloseable)c.newInstance()){
   Method init=Class.forName("MaterialTextureAtlasSpriteNativeTest").getDeclaredMethod("initializeNativeClientSettings");init.setAccessible(true);init.invoke(null);Bootstrap.register();mc=Minecraft.getMinecraft();OpenGlHelper.initializeTextures();
   MetadataSerializer metadata=new MetadataSerializer();metadata.registerMetadataSectionType(new TextureMetadataSectionSerializer(),TextureMetadataSection.class);SimpleReloadableResourceManager resources=new SimpleReloadableResourceManager(metadata);resources.reloadResourcePack(new FileResourcePack(new File(Minecraft.class.getProtectionDomain().getCodeSource().getLocation().toURI())));resources.reloadResourcePack(new File(args[0]).isDirectory()?new FolderResourcePack(new File(args[0])):new FileResourcePack(new File(args[0])));set(mc,Minecraft.class,"resourceManager",resources);TextureManager textures=new TextureManager(resources);set(mc,Minecraft.class,"renderEngine",textures);set(mc,Minecraft.class,"itemColors",new net.minecraft.client.renderer.color.ItemColors());TextureMap atlas=new TextureMap("textures");set(mc,Minecraft.class,"textureMapBlocks",atlas);textures.loadTexture(TextureMap.LOCATION_BLOCKS_TEXTURE,new DynamicTexture(1,1));
   ModelManager models=new ModelManager(atlas);registry=new net.minecraft.util.registry.RegistrySimple<>();set(models,ModelManager.class,"modelRegistry",registry);owner=new ContextRenderItem(new RenderItem(textures,models,mc.getItemColors()));set(mc,Minecraft.class,"renderItem",owner);
   TextureAtlasSprite sprite=new TextureAtlasSprite("probe:original"){};sprite.setIconWidth(16);sprite.setIconHeight(16);sprite.initSprite(16,16,0,0,false);int[] pixels=new int[256];Arrays.fill(pixels,-1);sprite.setFramesTextureData(Collections.singletonList(new int[][]{pixels}));ordinary=new net.minecraftforge.client.model.BakedItemModel(com.google.common.collect.ImmutableList.copyOf(net.minecraftforge.client.model.ItemLayerModel.getQuadsForSprite(0,sprite,net.minecraft.client.renderer.vertex.DefaultVertexFormats.ITEM,Optional.empty())),sprite,com.google.common.collect.ImmutableMap.of(),ItemOverrideList.NONE);
   Player player=(Player)unsafe.allocateInstance(Player.class);player.main=player.off=ItemStack.EMPTY;player.inventory=new net.minecraft.entity.player.InventoryPlayer(player);mc.player=player;
   for(Field field:ResourceManager.class.getDeclaredFields())if(field.getType()==MeshModel.class){MeshModel actual=(MeshModel)field.get(null);check(actual!=null&&!actual.getPartNames().isEmpty(),"original mesh load "+field.getName());field.set(null,new Mesh(field.getName(),actual));}
   weapons(player);missiles(player);hotAndMeteor(player);handState(player);callerAndDropped(player);droppedFallback();materialCacheState();lightingCaches(player);
   for(Field field:ResourceManager.class.getDeclaredFields())if(field.getType()==ResourceLocation.class){ResourceLocation resource=(ResourceLocation)field.get(null);textures.bindTexture(resource);check(textures.getTexture(resource) instanceof SimpleTexture,"actual selected material is not missing texture "+resource);}
   for(boolean remote:new boolean[]{false,true}){net.minecraft.world.World world=(net.minecraft.world.World)unsafe.allocateInstance(net.minecraft.world.WorldServer.class);set(world,net.minecraft.world.World.class,"isRemote",remote);ItemHot hot=new ItemHot(2);ItemStack stack=ItemHot.heatUp(new ItemStack(hot,7));hot.onUpdate(stack,world,null,0,false);check(ItemHot.getHeat(stack)==(remote?1:.5),"original server-only heat decay");if(!remote){hot.onUpdate(stack,world,null,0,false);check(stack.hasTagCompound()&&ItemHot.getHeat(stack)==0,"original final heat tick retains zero tag until next update");hot.onUpdate(stack,world,null,0,false);check(!stack.hasTagCompound()&&stack.getCount()==7,"original next-tick tag removal without count mutation");}}
   mc.player=null;check(com.hbm.render.anim.HbmAnimations.getRelevantAnim()==null,"native animation lookup without world/player is empty");mc.player=player;
   check(GL11.glGetError()==0,"terminal GL clean");System.out.println("PASS "+checks+" native remaining item checks, "+draws+" actual original geometry draw calls. No full-client or gameplay acceptance.");
  }
 }
}
