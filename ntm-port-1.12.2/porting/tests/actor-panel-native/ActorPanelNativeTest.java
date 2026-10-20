import java.util.*;
import java.lang.reflect.*;
import java.nio.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.client.renderer.block.model.ModelManager;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.client.resources.*;
import net.minecraft.client.resources.data.MetadataSerializer;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.init.*;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import com.hbm.wiaj.actors.ActorBasicPanel;

public class ActorPanelNativeTest {
 static int checks;static Minecraft mc;static Framebuffer fb;static Font font;static Recorder renderer;
 static void check(boolean b,String s){checks++;if(!b)throw new AssertionError(s);}
 static void field(Object o,String n,Object v)throws Exception{Field f=o.getClass().getDeclaredField(n);f.setAccessible(true);f.set(o,v);}
 static class Font extends FontRenderer {
  List<String> draws=new ArrayList<>();
  Font(TextureManager t){super(mc.gameSettings,new ResourceLocation("textures/font/ascii.png"),t,false);}
  public int getStringWidth(String s){return s.length()*6;}
  public int drawStringWithShadow(String s,float x,float y,int color){draws.add(s+":"+x+":"+y+":"+color);return 0;}
 }
 static ModelManager manager(){try{ModelManager m=new ModelManager(new TextureMap("textures"));field(m,"modelRegistry",new net.minecraft.util.registry.RegistrySimple<>());return m;}catch(Exception e){throw new RuntimeException(e);}}
 static class Recorder extends RenderItem {
  List<String> calls=new ArrayList<>();ItemStack last;boolean fail;
  Recorder(TextureManager t){super(t,manager(),new ItemColors());}
  public void renderItemAndEffectIntoGUI(ItemStack s,int x,int y){last=s;calls.add("item:"+x+":"+y+":"+zLevel);if(fail)throw new IllegalStateException("expected item failure");}
  public void renderItemOverlayIntoGUI(FontRenderer f,ItemStack s,int x,int y,String text){calls.add("overlay:"+x+":"+y+":"+zLevel+":"+text);}
 }
 static class Panel extends ActorBasicPanel {
  List<int[]> rectangles=new ArrayList<>();
  Panel(int x,int y,Object[]...lines){super(x,y,lines);}
  protected void drawGradientRect(int a,int b,int c,int d,int e,int f){rectangles.add(new int[]{a,b,c,d,e,f});super.drawGradientRect(a,b,c,d,e,f);}
  void gradient(int a,int b){super.drawGradientRect(5,7,51,49,a,b);}
 }
 static void reset(){fb.bindFramebuffer(true);GL11.glViewport(0,0,64,64);GL11.glMatrixMode(GL11.GL_PROJECTION);GL11.glLoadIdentity();GL11.glOrtho(0,64,64,0,-1000,1000);GL11.glMatrixMode(GL11.GL_MODELVIEW);GL11.glLoadIdentity();GlStateManager.disableLighting();GlStateManager.disableCull();GlStateManager.disableDepth();GL11.glClearColor(0,0,0,0);GL11.glClear(GL11.GL_COLOR_BUFFER_BIT|GL11.GL_DEPTH_BUFFER_BIT);font.draws.clear();renderer.calls.clear();}
 static byte[] pixels(){ByteBuffer b=org.lwjgl.BufferUtils.createByteBuffer(64*64*4);GL11.glReadPixels(0,0,64,64,GL11.GL_RGBA,GL11.GL_UNSIGNED_BYTE,b);byte[] out=new byte[b.capacity()];b.get(out);return out;}
 static void color(int c){GL11.glColor4ub((byte)(c>>16),(byte)(c>>8),(byte)c,(byte)(c>>24));}
 static void reference(int a,int b){GlStateManager.disableTexture2D();GlStateManager.enableBlend();GlStateManager.disableAlpha();GlStateManager.tryBlendFuncSeparate(770,771,1,0);GlStateManager.shadeModel(GL11.GL_SMOOTH);GL11.glBegin(GL11.GL_QUADS);color(a);GL11.glVertex3d(51,7,300);GL11.glVertex3d(5,7,300);color(b);GL11.glVertex3d(5,49,300);GL11.glVertex3d(51,49,300);GL11.glEnd();GlStateManager.shadeModel(GL11.GL_FLAT);GlStateManager.disableBlend();GlStateManager.enableAlpha();GlStateManager.enableTexture2D();}
 public static void main(String[] args)throws Exception {
  Constructor<?> c=Class.forName("MaterialTextureAtlasSpriteNativeTest$Context").getDeclaredConstructor();c.setAccessible(true);
  try(AutoCloseable context=(AutoCloseable)c.newInstance()){
   Method init=Class.forName("MaterialTextureAtlasSpriteNativeTest").getDeclaredMethod("initializeNativeClientSettings");init.setAccessible(true);init.invoke(null);mc=Minecraft.getMinecraft();Bootstrap.register();OpenGlHelper.initializeTextures();
   SimpleReloadableResourceManager resources=new SimpleReloadableResourceManager(new MetadataSerializer());java.net.URL location=TextureMap.class.getProtectionDomain().getCodeSource().getLocation();if(location.getProtocol().equals("jar"))location=((java.net.JarURLConnection)location.openConnection()).getJarFileURL();resources.reloadResourcePack(new FileResourcePack(new java.io.File(location.toURI())));field(mc,"resourceManager",resources);mc.renderEngine=new TextureManager(resources);font=new Font(mc.renderEngine);mc.fontRenderer=font;renderer=new Recorder(mc.renderEngine);field(mc,"renderItem",renderer);fb=new Framebuffer(64,64,true);
   Panel panel=new Panel(0,0);for(int[] colors:new int[][]{{0xF0100010,0xF0100010},{0x505000FF,0x5028007F},{0xffff0000,0xffb0b0b0},{0x12345678,0x89abcdef}}){reset();reference(colors[0],colors[1]);byte[] expected=pixels();reset();panel.gradient(colors[0],colors[1]);check(Arrays.equals(expected,pixels()),"actual native BufferBuilder gradient pixels equal original ordered vertices/colors");check(GL11.glGetError()==0,"gradient no GL error");}
   for(int count:new int[]{0,1,64}){
    ItemStack stack=new ItemStack(Items.DIAMOND,count);NBTTagCompound tag=new NBTTagCompound();tag.setString("payload","unchanged");stack.setTagCompound(tag);NBTTagCompound before=stack.writeToNBT(new NBTTagCompound());Panel p=new Panel(0,0,new Object[]{"ab",stack,"c"},new Object[]{"z"});reset();renderer.zLevel=17;p.drawForegroundComponent(100,100,0,0);
    check(renderer.zLevel==17,"shared native renderer depth restored");check(renderer.calls.equals(Arrays.asList("item:76:38:300.0","overlay:76:38:300.0:"+(count<=0?Integer.toString(count):"null"))),"native item+overlay call order/placement/zero count");check(font.draws.equals(Arrays.asList("ab:62.0:42.0:-1","c:94.0:42.0:-1","z:62.0:58.0:-1")),"mixed text/item line layout exact");check(p.rectangles.size()==(count==0?11:9),"zero count retains two warning rectangles");check(stack.writeToNBT(new NBTTagCompound()).equals(before),"source count/NBT never resurrected or mutated");check(renderer.last.getCount()==(count<=0?1:count)&&renderer.last.getTagCompound().equals(tag),"zero preview identity/NBT retained on copy");check((count>0)||renderer.last!=stack,"zero preview is separate stack");
   }
   reset();Panel clipped=new Panel(0,0,new Object[]{"ab",new ItemStack(Items.DIAMOND),"c"},new Object[]{"z"});clipped.drawForegroundComponent(40,30,0,0);check(renderer.calls.get(0).equals("item:-18:-4:300.0"),"right and bottom clipping retain original offsets");check(Arrays.equals(clipped.rectangles.get(0),new int[]{-35,-8,7,-7,0xF0100010,0xF0100010}),"clipped panel border exact");
   reset();Panel empty=new Panel(0,0);renderer.zLevel=23;empty.drawForegroundComponent(40,40,0,0);check(empty.rectangles.isEmpty()&&renderer.calls.isEmpty()&&renderer.zLevel==23,"empty panel untouched");
   reset();renderer.fail=true;Panel fail=new Panel(0,0,new Object[]{new ItemStack(Items.DIAMOND)});boolean caught=false;try{fail.drawForegroundComponent(100,100,0,0);}catch(IllegalStateException expected){caught=true;}check(caught&&renderer.zLevel==23,"throwing external item draw restores shared depth");check(GL11.glIsEnabled(GL11.GL_DEPTH_TEST)&&GL11.glIsEnabled(GL11.GL_LIGHTING),"original caller post-state restored after failure");renderer.fail=false;GlStateManager.disableDepth();check(!GL11.glIsEnabled(GL11.GL_DEPTH_TEST),"native depth cache remains coherent after panel return");GlStateManager.enableDepth();check(GL11.glIsEnabled(GL11.GL_DEPTH_TEST),"native depth can reenable after panel return");
   Recorder replacement=new Recorder(mc.renderEngine);field(mc,"renderItem",replacement);new Panel(0,0,new Object[]{new ItemStack(Items.DIAMOND)}).drawForegroundComponent(100,100,0,0);check(replacement.calls.size()==2,"foreground resolves replacement native renderer, no stale owner");
   System.out.println("PASS "+checks+" actual native panel gradient/caller/zero-preview checks; text glyphs and item meshes are recording boundaries, not whole tutorial/game verification.");
  }
 }
}
