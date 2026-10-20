import com.hbm.render.item.ItemRenderMissileGeneric;
import com.hbm.render.item.ItemRenderMissileGeneric.RenderMissileType;
import com.hbm.render.item.weapon.ItemRenderFireExt;
import com.hbm.render.item.nativeitem.*;
import com.hbm.render.item.nativeitem.direct.*;
import com.hbm.main.ResourceManager;
import com.hbm.items.ModItems;
import com.hbm.items.weapon.sedna.ItemGunBaseNT;
import com.hbm.items.weapon.sedna.factory.XFactoryTool;
import java.util.*;
import java.lang.reflect.*;
import javax.vecmath.Matrix4f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.init.*;
import net.minecraft.item.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

public class DirectMeshItemNativeTest {
 static int checks;
 static void check(boolean b,String s){checks++;if(!b)throw new AssertionError(s);}
 static Matrix4f matrix(){java.nio.FloatBuffer b=BufferUtils.createFloatBuffer(16);GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX,b);float[] f=new float[16];b.get(f);Matrix4f m=new Matrix4f(f);m.transpose();return m;}
 static void mult(Matrix4f m){float[] f={m.m00,m.m10,m.m20,m.m30,m.m01,m.m11,m.m21,m.m31,m.m02,m.m12,m.m22,m.m32,m.m03,m.m13,m.m23,m.m33};java.nio.FloatBuffer b=BufferUtils.createFloatBuffer(16);b.put(f).flip();GL11.glMultMatrix(b);}
 static void scale(double s){GL11.glScaled(s,s,s);}
 static void rx(double d){GL11.glRotated(d,1,0,0);}static void ry(double d){GL11.glRotated(d,0,1,0);}static void rz(double d){GL11.glRotated(d,0,0,1);}
 static void t(double x,double y,double z){GL11.glTranslated(x,y,z);}
 static boolean first(TransformType t){return t==TransformType.FIRST_PERSON_RIGHT_HAND||t==TransformType.FIRST_PERSON_LEFT_HAND;}
 static boolean third(TransformType t){return t==TransformType.THIRD_PERSON_RIGHT_HAND||t==TransformType.THIRD_PERSON_LEFT_HAND;}
 // Direct transcription of old renderer GL calls, not production matrix factories.
 static void missileLocal(RenderMissileType kind,TransformType type,long time){
  if(first(type)){t(.5,.25,0);scale(.1);}else if(third(type)){t(.5,-.25,0);scale(.15);}
  else if(type==TransformType.GUI){double[] scales={3.75,2.5,2,1.25,1.75,2.25,1.375,1.25};double[] offsets={10.75,8.5,6.5,1,4.75,7,1.5,2};scale(scales[kind.ordinal()]);rz(135);GL11.glRotatef(time/15%360,0,1,0);t(0,-16+offsets[kind.ordinal()],0);}
  else scale(.15);
 }
 static void fireLocal(TransformType type,long time){
  if(first(type)){rz(25);t(.5,-.5,-.5F);ry(80);scale(.35);}
  else if(third(type)){scale(.5);GL11.glRotatef(20,0,0,1);GL11.glRotatef(-5,0,1,1);GL11.glRotatef(10,0,1,0);GL11.glRotatef(15,1,0,0);GL11.glTranslatef(.75F,-2.75F,.5F);}
  else if(type==TransformType.GUI){t(2,14,0);ry(-90);rx(-135);ry(time/10%360);GL11.glScaled(4.5,4.5,-4.5);}
  else scale(.3);
 }
 static void helper(){GL11.glTranslatef(0,-.3F,0);scale(1.5);ry(50);rz(335);t(-.9375,-.0625,0);}
 static void oldCaller(TransformType type){
  if(type==TransformType.GUI)t(0,0,-3);
  else if(first(type)){scale(.4);helper();}
  else if(third(type)){t(-.0625,.4375,.0625);t(.25,.1875,-.1875);scale(.375);rz(60);rx(-90);rz(20);helper();}
  else if(type==TransformType.FIXED){t(0,-.06,0);scale(.5);ry(180);}
  else scale(.5);
 }
 static void nativeCaller(TransformType type){
  if(type==TransformType.GUI){t(8,8,100);GL11.glScaled(16,-16,16);}
  else if(first(type))ry(-45);
  else if(third(type)){rx(-90);ry(180);t(.0625,.125,-.625);}
  else if(type==TransformType.GROUND)t(0,.25,0);
  else if(type==TransformType.FIXED)scale(.5);
 }
 static void perspectives(){
  for(long time:new long[]{0,15123,359999})for(TransformType type:TransformType.values()){
   for(RenderMissileType kind:RenderMissileType.values()){
    DirectMeshItemRenderer renderer=new ItemRenderMissileGeneric(kind);
    GL11.glLoadIdentity();missileLocal(kind,type,time);Matrix4f local=matrix();
    check(local.epsilonEquals(renderer.localTransform(type,time),0.00001F),"missile original local "+kind+" "+type);
    GL11.glLoadIdentity();oldCaller(type);missileLocal(kind,type,time);Matrix4f old=matrix();
    GL11.glLoadIdentity();nativeCaller(type);mult(PlainItemPerspectives.compose(type,renderer.localTransform(type,time)));t(-.5,-.5,-.5);
    check(old.epsilonEquals(matrix(),0.00005F),"missile old/native composed "+kind+" "+type);
   }
   DirectMeshItemRenderer fire=new ItemRenderFireExt();GL11.glLoadIdentity();fireLocal(type,time);Matrix4f local=matrix();
   check(local.epsilonEquals(fire.localTransform(type,time),0.00001F),"fire original local "+type);
   GL11.glLoadIdentity();oldCaller(type);fireLocal(type,time);Matrix4f old=matrix();
   GL11.glLoadIdentity();nativeCaller(type);mult(PlainItemPerspectives.compose(type,fire.localTransform(type,time)));t(-.5,-.5,-.5);
   check(old.epsilonEquals(matrix(),0.00005F),"fire old/native composed "+type);
  }
 }
 static class Textures extends TextureManager {
  ResourceLocation last;Textures(){super(null);}@Override public void bindTexture(ResourceLocation r){last=r;}
 }
 static void draws(Textures textures)throws Exception {
  ItemRenderMissileGeneric.init();check(ItemRenderMissileGeneric.renderers.size()==28,"all original missile materials retained");
  ItemStack stack=new ItemStack(ModItems.missile_test,37,0);NBTTagCompound tag=new NBTTagCompound();tag.setString("probe","unchanged");stack.setTagCompound(tag);NBTTagCompound saved=tag.copy();
  GL11.glLoadIdentity();GL11.glTranslatef(7,8,9);Matrix4f before=matrix();GL11.glAlphaFunc(GL11.GL_LESS,.75F);GL11.glShadeModel(GL11.GL_FLAT);GL11.glEnable(GL11.GL_CULL_FACE);
  new ItemRenderMissileGeneric(RenderMissileType.TYPE_TIER0).renderByItem(stack,.5F);
  check(textures.last==ResourceManager.missileMicroTest_tex&&ResourceManager.missileMicro.calls==1,"missile actual stack/count key and original material/model");
  check(ResourceManager.missileMicro.shade==GL11.GL_SMOOTH&&!ResourceManager.missileMicro.cull&&ResourceManager.missileMicro.lighting&&ResourceManager.missileMicro.alpha==0,"original missile draw state");
  check(matrix().equals(before)&&GL11.glGetFloat(GL11.GL_ALPHA_TEST_REF)==.75F&&GL11.glGetInteger(GL11.GL_ALPHA_TEST_FUNC)==GL11.GL_LESS&&GL11.glIsEnabled(GL11.GL_CULL_FACE),"native guard restores missile matrix/alpha/cull");
  check(stack.getCount()==37&&stack.getMetadata()==0&&stack.getTagCompound().equals(saved),"missile count/metadata/NBT preserved");
  new ItemRenderMissileGeneric(RenderMissileType.TYPE_TIER0).renderByItem(new ItemStack(ModItems.missile_test,2,1));check(ResourceManager.missileMicro.calls==1,"unknown metadata still draws nothing");
  GL11.glLoadIdentity();new ItemRenderMissileGeneric(RenderMissileType.TYPE_TIER2).renderByItem(new ItemStack(ModItems.missile_strong,5));check(ResourceManager.missileStrong.matrix.epsilonEquals(new ItemTransformMatrix().scale(1.5).build(),0.00001F),"large missile draw retains 1.5 material scale");
  ItemGunBaseNT gun=new ItemGunBaseNT();stack=new ItemStack(gun,11,3);stack.setTagCompound(saved.copy());
  Object[] types={null,XFactoryTool.fext_foam,XFactoryTool.fext_sand};ResourceLocation[] materials={ResourceManager.fireext_tex,ResourceManager.fireext_foam_tex,ResourceManager.fireext_sand_tex};
  for(int i=0;i<types.length;i++){gun.type=types[i];gun.seen.clear();new ItemRenderFireExt().renderByItem(stack);check(textures.last==materials[i],"fire extinguisher original magazine material "+i);check(gun.seen.size()==5,"original config/receiver/magazine/two type lookup sequence");for(ItemStack s:gun.seen)check(s==stack,"fire exact native stack, no synthetic copy");}
  check(stack.getCount()==11&&stack.getMetadata()==3&&stack.getTagCompound().equals(saved),"fire stack unchanged");
  check(ResourceManager.fireext.calls==3&&ResourceManager.fireext.shade==GL11.GL_SMOOTH&&ResourceManager.fireext.cull,"fire complete smooth culled mesh preserved");
 }
 static class Nested extends DirectMeshItemRenderer {
  List<ItemStack> seen=new ArrayList<>();ItemStack inner;boolean fail;int depth;int texture;
  public Matrix4f localTransform(TransformType type,long time){return new ItemTransformMatrix().build();}
  protected void draw(ItemStack s){seen.add(s);GL11.glTranslated(1,2,3);GL11.glShadeModel(GL11.GL_SMOOTH);if(texture!=0)GlStateManager.bindTexture(texture);if(depth==0&&inner!=null){depth++;try{renderByItem(inner);}finally{depth--;}}if(fail)throw new IllegalStateException("controlled");seen.add(s);}
 }
 static void isolation(){
  Nested r=new Nested();ItemStack outer=new ItemStack(Items.DIAMOND,13),inner=new ItemStack(Items.STICK,21);r.inner=inner;
  GL11.glLoadIdentity();GL11.glTranslatef(2,4,6);Matrix4f before=matrix();
  int originalTexture=GL11.glGenTextures();r.texture=GL11.glGenTextures();GlStateManager.bindTexture(originalTexture);GL11.glShadeModel(GL11.GL_FLAT);r.renderByItem(outer);
  check(GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D)==originalTexture&&GL11.glGetInteger(GL11.GL_SHADE_MODEL)==GL11.GL_FLAT,"normal draw restores native texture binding and shade");
  check(r.seen.equals(Arrays.asList(outer,inner,inner,outer))&&matrix().equals(before),"nested native draws keep actual stacks and balance matrices");
  r.fail=true;r.seen.clear();try{r.renderByItem(outer);throw new AssertionError();}catch(IllegalStateException expected){check(r.seen.equals(Arrays.asList(outer,inner))&&matrix().equals(before),"nested exception restores both scopes, no stale context");}
  check(GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D)==originalTexture&&GL11.glGetInteger(GL11.GL_SHADE_MODEL)==GL11.GL_FLAT,"nested exception restores native texture and shade");
  GlStateManager.bindTexture(r.texture);check(GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D)==r.texture,"restored native texture cache accepts alternate binding");GlStateManager.bindTexture(originalTexture);
  r.inner=null;r.fail=false;r.seen.clear();r.renderByItem(inner);check(r.seen.equals(Arrays.asList(inner,inner)),"next native draw unaffected by nested exception");
  TextureAtlasSprite sprite=new TextureAtlasSprite("test:direct"){};DirectMeshItemModel model=new DirectMeshItemModel(sprite,r);
  IBakedModel a=model.getOverrides().handleItemState(model,outer,null,null),b=model.getOverrides().handleItemState(model,inner,null,null);
  check(a!=b&&a!=model&&a.isBuiltInRenderer()&&a.getQuads(null,null,0).isEmpty()&&a.getParticleTexture()==sprite,"fresh immutable native model per override call");
  IBakedModel reset=a.getOverrides().handleItemState(a,inner,null,null);check(reset!=a,"prior wrapper cannot retain previous holder when holder absent");
  Matrix4f first=a.handlePerspective(TransformType.GROUND).getRight();first.setZero();
  check(!a.handlePerspective(TransformType.GROUND).getRight().equals(first),"perspective matrix cannot mutate model snapshot");
 }
 static float[] light(int light){java.nio.FloatBuffer b=BufferUtils.createFloatBuffer(4);GL11.glGetLight(light,GL11.GL_POSITION,b);float[] f=new float[4];b.get(f);return f;}
 static void guiLighting(){
  GL11.glLoadIdentity();t(11,13,17);net.minecraft.client.renderer.RenderHelper.enableGUIStandardItemLighting();float[] expected=light(GL11.GL_LIGHT0);
  GL11.glLoadIdentity();t(19,21,120);GL11.glScaled(16,-16,16);Matrix4f before=matrix();
  new DirectMeshItemModel(new TextureAtlasSprite("test:gui"){},new ItemRenderMissileGeneric(RenderMissileType.TYPE_TIER0)).handlePerspective(TransformType.GUI);
  float[] actual=light(GL11.GL_LIGHT0);boolean same=true;for(int i=0;i<4;i++)same&=Math.abs(expected[i]-actual[i])<.000001F;
  check(same&&matrix().equals(before),"missile GUI light directions match old caller before local transforms, matrix balanced");
  GL11.glDisable(GL11.GL_LIGHTING);new DirectMeshItemModel(new TextureAtlasSprite("test:firegui"){},new ItemRenderFireExt()).handlePerspective(TransformType.GUI);
  check(GL11.glIsEnabled(GL11.GL_LIGHTING)&&Arrays.equals(actual,light(GL11.GL_LIGHT0))&&matrix().equals(before),"fire GUI enables lighting without changing native caller directions or matrix");
 }
 static void registration(){
  Item item=new Item().setRegistryName("test","direct_owner");Nested renderer=new Nested();Map<Item,DirectMeshItemRenderer> source=new IdentityHashMap<>();source.put(item,renderer);DirectMeshItemRegistration owner=new DirectMeshItemRegistration(source);source.clear();check(owner.items().contains(item)&&owner.items().size()==1,"defensive identity registration");owner.register(new net.minecraftforge.client.event.ModelRegistryEvent());
  ModelResourceLocation key=StaticMeshItemRegistration.modelLocation(item);IBakedModel previous=null;
  for(int i=0;i<2;i++){TextureAtlasSprite sprite=new TextureAtlasSprite("test:particle"+i){};Map<TransformType,Matrix4f> matrices=new EnumMap<>(TransformType.class);for(TransformType t:TransformType.values())matrices.put(t,new ItemTransformMatrix().build());IBakedModel original=new StaticMeshItemModel(matrices,sprite);net.minecraft.util.registry.RegistrySimple<ModelResourceLocation,IBakedModel> registry=new net.minecraft.util.registry.RegistrySimple<>();registry.putObject(key,original);owner.bake(new net.minecraftforge.client.event.ModelBakeEvent(null,registry,null));IBakedModel actual=registry.getObject(key);check(actual instanceof DirectMeshItemModel&&actual!=previous&&actual.getParticleTexture()==sprite&&item.getTileEntityItemStackRenderer()==renderer,"native bake/reload owns model and renderer, source particle retained");previous=actual;}
 }
 public static void main(String[]args)throws Exception {
  Constructor<?> ctor=Class.forName("MaterialTextureAtlasSpriteNativeTest$Context").getDeclaredConstructor();ctor.setAccessible(true);
  try(AutoCloseable context=(AutoCloseable)ctor.newInstance()){
   Method init=Class.forName("MaterialTextureAtlasSpriteNativeTest").getDeclaredMethod("initializeNativeClientSettings");init.setAccessible(true);init.invoke(null);Bootstrap.register();
   Field colors=Minecraft.class.getDeclaredField("itemColors");colors.setAccessible(true);colors.set(Minecraft.getMinecraft(),new net.minecraft.client.renderer.color.ItemColors());
   Field atlas=Minecraft.class.getDeclaredField("textureMapBlocks");atlas.setAccessible(true);atlas.set(Minecraft.getMinecraft(),new TextureMap("textures"));
   Textures textures=new Textures();Field engine=Minecraft.class.getDeclaredField("renderEngine");engine.setAccessible(true);engine.set(Minecraft.getMinecraft(),textures);
   GL11.glMatrixMode(GL11.GL_MODELVIEW);perspectives();draws(textures);isolation();guiLighting();registration();check(GL11.glGetError()==0,"native GL error-free");
   System.out.println("PASS "+checks+" native direct mesh perspective/draw/stack/reentrancy/registration checks. Controlled NTM collaborators; no full-game visual acceptance.");
  }
 }
}
