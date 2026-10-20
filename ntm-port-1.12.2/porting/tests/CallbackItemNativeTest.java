import com.hbm.render.item.ItemRenderBase;
import com.hbm.render.item.nativeitem.*;
import com.hbm.render.item.nativeitem.callback.*;
import com.hbm.render.item.nativeitem.staticfamily.*;
import java.util.*;
import java.lang.reflect.*;
import javax.vecmath.Matrix4f;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.init.*;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.lwjgl.opengl.GL11;
public class CallbackItemNativeTest {
 static int checks;static void check(boolean a,String b){checks++;if(!a)throw new AssertionError(b);}
 static class Renderer extends ItemRenderBase {
  List<String> calls=new ArrayList<>();ItemStack seen;boolean fail;int animation=1;
  public void renderInventory(){calls.add("inventory");GL11.glTranslated(1,animation,3);GL11.glScaled(2,3,4);}
  public void renderNonInv(){calls.add("noninv");GL11.glRotated(27,0,1,0);}
  public void renderCommon(){calls.add("common");if(fail)throw new IllegalStateException("controlled draw failure");}
  public void renderCommonWithStack(ItemStack stack){calls.add("stack");seen=stack;}
 }
 static float[] matrix(){java.nio.FloatBuffer b=org.lwjgl.BufferUtils.createFloatBuffer(16);GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX,b);float[] a=new float[16];b.get(a);return a;}
 static void registrationReload() {
  net.minecraft.item.Item item=new net.minecraft.item.Item().setRegistryName("test","callback_owner");Renderer first=new Renderer(),last=new Renderer();
  Map<net.minecraft.item.Item,ItemRenderBase> source=new IdentityHashMap<>();source.put(item,first);source.put(item,last);
  CallbackItemRegistration registration=new CallbackItemRegistration(source);source.clear();
  check(registration.items().contains(item)&&registration.items().size()==1,"last explicit owner retained in defensive identity map");
  registration.register(new net.minecraftforge.client.event.ModelRegistryEvent());
  ModelResourceLocation key=new ModelResourceLocation(item.getRegistryName(),"inventory");
  IBakedModel previous=null;
  for(int reload=0;reload<2;reload++) {
   TextureAtlasSprite sprite=new TextureAtlasSprite("test:particle"+reload){};
   IBakedModel original=new StaticMeshItemModel(StaticItemPerspectives.complete(new ItemTransformMatrix().build()),sprite);
   net.minecraft.util.registry.RegistrySimple<ModelResourceLocation,IBakedModel> registry=new net.minecraft.util.registry.RegistrySimple<>();registry.putObject(key,original);
   registration.bake(new net.minecraftforge.client.event.ModelBakeEvent(null,registry,null));
   IBakedModel actual=registry.getObject(key);check(actual instanceof CallbackItemModel&&actual!=previous,"fresh native model each reload");check(actual.getParticleTexture()==sprite,"reload preserves native source particle identity");check(item.getTileEntityItemStackRenderer()==last,"actual item TEISR installed without stack mutation");previous=actual;
  }
 }
 public static void main(String[] args)throws Exception {
  Constructor<?> c=Class.forName("MaterialTextureAtlasSpriteNativeTest$Context").getDeclaredConstructor();c.setAccessible(true);
  try(AutoCloseable context=(AutoCloseable)c.newInstance()) {
   Method init=Class.forName("MaterialTextureAtlasSpriteNativeTest").getDeclaredMethod("initializeNativeClientSettings");init.setAccessible(true);init.invoke(null);
   Bootstrap.register();
   Field colors=net.minecraft.client.Minecraft.class.getDeclaredField("itemColors");colors.setAccessible(true);colors.set(net.minecraft.client.Minecraft.getMinecraft(),new net.minecraft.client.renderer.color.ItemColors());
   Field atlas=net.minecraft.client.Minecraft.class.getDeclaredField("textureMapBlocks");atlas.setAccessible(true);atlas.set(net.minecraft.client.Minecraft.getMinecraft(),new net.minecraft.client.renderer.texture.TextureMap("textures"));
   Renderer renderer=new Renderer();TextureAtlasSprite sprite=new TextureAtlasSprite("test:callback"){};CallbackItemModel model=new CallbackItemModel(sprite,renderer);
   for(TransformType type:TransformType.values()) {
    GL11.glMatrixMode(GL11.GL_MODELVIEW);GL11.glLoadIdentity();GL11.glTranslatef(17,19,23);float[] before=matrix();renderer.calls.clear();
    Matrix4f actual=model.handlePerspective(type).getRight();
    check(Arrays.equals(before,matrix()),"perspective capture restores caller matrix "+type);
    check(renderer.calls.equals(Collections.singletonList(type==TransformType.GUI?"inventory":"noninv")),"correct callback exactly once "+type);
    Matrix4f callback=type==TransformType.GUI?new ItemTransformMatrix().translate(1,1,3).scale(2,3,4).build():new ItemTransformMatrix().rotateY(27).build();
    Matrix4f base=StaticItemPerspectives.complete(new ItemTransformMatrix().build()).get(type);
    Matrix4f expected=new ItemTransformMatrix().multiply(base).translate(-.5,-.5,-.5).multiply(callback).translate(.5,.5,.5).build();
    check(actual.epsilonEquals(expected,0.00001F),"ordered native matrix insertion "+type);
   }
   Matrix4f before=model.handlePerspective(TransformType.GUI).getRight();renderer.animation=2;check(!before.equals(model.handlePerspective(TransformType.GUI).getRight()),"animated transform not baked/frozen");
   GL11.glMatrixMode(GL11.GL_TEXTURE);int mode=GL11.glGetInteger(GL11.GL_MATRIX_MODE);float[] previous=matrix();
   try{CallbackItemModel.captureTransform(()->{GL11.glTranslated(7,8,9);throw new IllegalStateException();});throw new AssertionError("expected callback failure");}catch(IllegalStateException ok){check(GL11.glGetInteger(GL11.GL_MATRIX_MODE)==mode&&Arrays.equals(previous,matrix()),"throwing callback restores matrix and mode");}
   GL11.glMatrixMode(GL11.GL_MODELVIEW);ItemStack stack=new ItemStack(Items.DIAMOND_SWORD,7,12);stack.addEnchantment(Enchantments.SHARPNESS,3);NBTTagCompound tag=stack.writeToNBT(new NBTTagCompound());renderer.calls.clear();float[] original=matrix();renderer.renderByItem(stack,.25F);
   check(renderer.calls.equals(Arrays.asList("common","stack"))&&renderer.seen==stack,"native draw ordered callbacks preserve exact stack identity");check(tag.equals(stack.writeToNBT(new NBTTagCompound())),"stack count/damage/enchantment unchanged");check(Arrays.equals(original,matrix()),"draw restores matrix");
   renderer.fail=true;renderer.calls.clear();try{renderer.renderByItem(stack);throw new AssertionError("expected draw failure");}catch(IllegalStateException ok){check(renderer.calls.equals(Collections.singletonList("common"))&&Arrays.equals(original,matrix()),"draw exception restores matrix without extra callback");}
   check(model.isBuiltInRenderer()&&model.getQuads(null,null,0).isEmpty()&&model.getParticleTexture()==sprite,"native TEISR owns complete geometry");
   registrationReload();
   check(GL11.glGetError()==0,"native GL no errors");
   System.out.println("PASS "+checks+" native callback/perspective/stack/GL-state checks. Controlled draws; not all provider bodies, game or visual acceptance.");
  }
 }
}
