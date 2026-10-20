import com.hbm.main.ResourceManager;
import com.hbm.render.tileentity.*;
import com.hbm.render.util.*;
import com.hbm.tileentity.machine.*;
import com.hbm.tileentity.machine.storage.TileEntityMassStorage;
import java.util.*;
import java.lang.reflect.*;
import net.minecraft.item.ItemStack;
import net.minecraft.init.Items;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;

/** Complete real TESR bodies; only NTM machine storage/mesh data are declared fixture boundaries. */
public final class NativeMachineConsumerTest {
 static int checks;static void check(boolean ok,String msg){checks++;if(!ok)throw new AssertionError(msg);}
 static void following(int flag,Runnable enable,Runnable disable){enable.run();check(GL11.glIsEnabled(flag),"following native enable works "+flag);disable.run();check(!GL11.glIsEnabled(flag),"following native disable works "+flag);}
 public static void run()throws Exception {
  for(Field f:ResourceManager.class.getFields())if(f.getType()==ResourceLocation.class){DynamicTexture t=new DynamicTexture(1,1);t.getTextureData()[0]=0xFFFFFFFF;t.updateDynamicTexture();NativeMachineDrawTest.textures.loadTexture((ResourceLocation)f.get(null),t);}
  TileEntityRendererDispatcher.instance.renderEngine=NativeMachineDrawTest.textures;
  TileEntityRendererDispatcher.instance.fontRenderer=NativeMachineDrawTest.mc.fontRenderer;
  ItemStack source=new ItemStack(Items.DIAMOND_SWORD,37);source.addEnchantment(net.minecraft.init.Enchantments.SHARPNESS,2);NBTTagCompound before=source.writeToNBT(new NBTTagCompound());
  NativeMachineDrawTest.Custom custom=new NativeMachineDrawTest.Custom();source.getItem().setTileEntityItemStackRenderer(custom);NativeMachineDrawTest.model(source.getItem(),new NativeMachineDrawTest.BaseOwner(NativeMachineDrawTest.icon));
  TileEntityMachinePress press=new TileEntityMachinePress();press.syncStack=source;press.lastPress=20;press.renderPress=80;press.maxPress=100;
  RenderPress renderer=new RenderPress();renderer.setRendererDispatcher(TileEntityRendererDispatcher.instance);
  for(float partial:new float[]{0,.375F,1}){
   NativeMachineDrawTest.reset();float[] prior=NativeMachineItemTest.matrix();int calls=custom.calls;
   renderer.render(press,1,2,3,partial,-1,1);
   check(custom.calls==calls+1&&custom.last!=source&&custom.last.getCount()==1,"whole press copies exactly one custom item");
   check(before.equals(source.writeToNBT(new NBTTagCompound())),"whole press source stack/NBT preserved");
   check(Arrays.equals(prior,NativeMachineItemTest.matrix()),"whole press balances outer matrix");
   GL11.glLoadIdentity();GL11.glTranslated(1.5,2,3.5);GL11.glScalef(.99F,1,.99F);
   double p=(20+(80-20)*partial)/100D;GL11.glTranslated(0,Math.max(0,Math.min(1,1-p))*.875,0);
   check(Arrays.equals(NativeMachineItemTest.matrix(),ResourceManager.press_head.matrices.get(ResourceManager.press_head.matrices.size()-1)),"whole press exact head interpolation matrix "+partial);
  }
  check(ResourceManager.press_body.calls.size()==3&&ResourceManager.press_head.calls.size()==3,"whole press meshes each drawn once per frame");
  TileEntityMassStorage storage=new TileEntityMassStorage();storage.type=source;storage.stock=12345;storage.capacity=20000;
  RenderMassStorage mass=new RenderMassStorage();mass.setRendererDispatcher(TileEntityRendererDispatcher.instance);
  for(boolean fancy:new boolean[]{false,true})for(int direction=0;direction<4;direction++){
   storage.meta=direction*4;NativeMachineDrawTest.mc.gameSettings.fancyGraphics=fancy;NativeMachineDrawTest.reset();int calls=custom.calls;float[] prior=NativeMachineItemTest.matrix();
   mass.render(storage,1,2,3,.375F,-1,1);
   check(custom.calls==calls+(fancy?1:0),"whole mass-storage fancy/base dispatch direction "+direction+" fancy="+fancy);
   check(Arrays.equals(prior,NativeMachineItemTest.matrix()),"whole mass-storage matrix balanced");
   check(before.equals(source.writeToNBT(new NBTTagCompound())),"whole mass-storage preserves stored stack");
   check(GL11.glGetError()==0,"whole mass-storage native font/gauge/item draw GL success");
  }
  // Reproduce an outer legacy GL attrib scope restoring a flag behind native caches.
  NativeMachineDrawTest.reset();NativeMachineDrawTest.mc.gameSettings.fancyGraphics=true;
  GlStateManager.enableRescaleNormal();
  mass.render(storage,1,2,3,0,-1,1);
  check(GL11.glIsEnabled(org.lwjgl.opengl.GL12.GL_RESCALE_NORMAL),"mass-storage raw attrib restores preexisting rescale state");
  GlStateManager.disableRescaleNormal();
  check(!GL11.glIsEnabled(org.lwjgl.opengl.GL12.GL_RESCALE_NORMAL),"following native disable sees synchronized rescale cache");
  com.hbm.render.item.ItemRenderBase callback=new com.hbm.render.item.ItemRenderBase(){};
  source.getItem().setTileEntityItemStackRenderer(callback);
  NativeMachineDrawTest.model(source.getItem(),new com.hbm.render.item.nativeitem.callback.CallbackItemModel(NativeMachineDrawTest.icon.getParticleTexture(),callback,NativeMachineDrawTest.icon));
  for(boolean enabled:new boolean[]{false,true}) {
   NativeMachineDrawTest.reset();
   if(enabled){GlStateManager.enableLight(0);GlStateManager.enableLight(1);GlStateManager.enableColorMaterial();GlStateManager.enableNormalize();GlStateManager.enableAlpha();GlStateManager.enableBlend();GlStateManager.enableTexture2D();}
   else {GlStateManager.disableLight(0);GlStateManager.disableLight(1);GlStateManager.disableColorMaterial();GlStateManager.disableNormalize();GlStateManager.disableAlpha();GlStateManager.disableBlend();GlStateManager.disableTexture2D();}
   GlStateManager.alphaFunc(GL11.GL_GREATER,.13F);GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA,GL11.GL_ONE,GL11.GL_ONE,GL11.GL_ZERO);GlStateManager.colorMaterial(GL11.GL_FRONT,GL11.GL_AMBIENT);
   mass.render(storage,1,2,3,0,-1,1);
   check(GL11.glIsEnabled(GL11.GL_LIGHT0)==enabled&&GL11.glIsEnabled(GL11.GL_LIGHT1)==enabled,"mass-storage retains original individual light enables");
   following(GL11.GL_LIGHT0,()->GlStateManager.enableLight(0),()->GlStateManager.disableLight(0));
   following(GL11.GL_LIGHT1,()->GlStateManager.enableLight(1),()->GlStateManager.disableLight(1));
   following(GL11.GL_COLOR_MATERIAL,GlStateManager::enableColorMaterial,GlStateManager::disableColorMaterial);
   following(GL11.GL_NORMALIZE,GlStateManager::enableNormalize,GlStateManager::disableNormalize);
   following(GL11.GL_ALPHA_TEST,GlStateManager::enableAlpha,GlStateManager::disableAlpha);
   following(GL11.GL_BLEND,GlStateManager::enableBlend,GlStateManager::disableBlend);
   following(GL11.GL_TEXTURE_2D,GlStateManager::enableTexture2D,GlStateManager::disableTexture2D);
   following(GL11.GL_LIGHTING,GlStateManager::enableLighting,GlStateManager::disableLighting);
   following(GL11.GL_CULL_FACE,GlStateManager::enableCull,GlStateManager::disableCull);
   GlStateManager.alphaFunc(GL11.GL_GREATER,.1F);check(GL11.glGetFloat(GL11.GL_ALPHA_TEST_REF)==.1F,"following native alpha function updates restored raw value");
   GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA,GL11.GL_ONE_MINUS_SRC_ALPHA,GL11.GL_ONE,GL11.GL_ZERO);check(GL11.glGetInteger(org.lwjgl.opengl.GL14.GL_BLEND_DST_RGB)==GL11.GL_ONE_MINUS_SRC_ALPHA,"following native blend factors update restored raw values");
   GlStateManager.colorMaterial(GL11.GL_FRONT_AND_BACK,GL11.GL_AMBIENT_AND_DIFFUSE);check(GL11.glGetInteger(GL11.GL_COLOR_MATERIAL_FACE)==GL11.GL_FRONT_AND_BACK&&GL11.glGetInteger(GL11.GL_COLOR_MATERIAL_PARAMETER)==GL11.GL_AMBIENT_AND_DIFFUSE,"following native color-material state updates restored raw values");
   GlStateManager.color(1,1,1,1);java.nio.FloatBuffer color=org.lwjgl.BufferUtils.createFloatBuffer(16);GL11.glGetFloat(GL11.GL_CURRENT_COLOR,color);check(color.get(0)==1&&color.get(1)==1&&color.get(2)==1&&color.get(3)==1,"following native color setter is not a stale no-op after raw stock gauge");
  }
  Method count=RenderMassStorage.class.getDeclaredMethod("getTextForCount",int.class,boolean.class);count.setAccessible(true);
  for(boolean unicode:new boolean[]{false,true})for(int n:new int[]{0,9999,10000,99999,100000,999999,1000000,99999999,100000000}){
   String expected=n>=100000000||(n>=1000000&&unicode)?String.format("%.0fM",n/1000000F):n>=1000000?String.format("%.1fM",n/1000000F):n>=100000||(n>=10000&&unicode)?String.format("%.0fK",n/1000F):n>=10000?String.format("%.1fK",n/1000F):String.valueOf(n);
   check(expected.equals(count.invoke(mass,n,unicode)),"unchanged stock label threshold "+n+"/"+unicode);
  }
  System.out.println("PASS "+checks+" full native press/mass-storage consumer draw, animation/source-stack/font/gauge/direction/label checks; NTM data/mesh fixtures, not complete game.");
 }
}
