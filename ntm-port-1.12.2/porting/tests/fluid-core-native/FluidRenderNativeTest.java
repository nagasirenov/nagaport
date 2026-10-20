import java.io.*;
import java.util.*;
import java.lang.reflect.*;
import org.objectweb.asm.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ResourceLocation;
import com.hbm.inventory.fluid.*;
/** Production FluidTank bytecode with only GPU entry calls redirected to explicit captures. */
public class FluidRenderNativeTest {
 public static Minecraft minecraft;public static CaptureTessellator tess;public static List<String> calls=new ArrayList<>();static int checks;
 public static Minecraft getMinecraft(){return minecraft;}public static Tessellator getInstance(){return tess;}
 public static void glEnable(int mode){calls.add("enable:"+mode);}public static void glDisable(int mode){calls.add("disable:"+mode);}public static void glColor3d(double r,double g,double b){calls.add("color:"+r+","+g+","+b);}
 static void check(boolean v,String m){checks++;if(!v)throw new AssertionError(m);}
 public static class CaptureTextures extends TextureManager {CaptureTextures(){super(null);}public void bindTexture(ResourceLocation r){calls.add("texture:"+r);}}
 public static class CaptureTessellator extends Tessellator {
  float[][] vertices;CaptureTessellator(){super(512);}
  public void draw(){BufferBuilder b=getBuffer();b.finishDrawing();check(b.getVertexCount()==4&&b.getVertexFormat().equals(net.minecraft.client.renderer.vertex.DefaultVertexFormats.POSITION_TEX),"native layout/count");vertices=new float[4][5];java.nio.ByteBuffer bytes=b.getByteBuffer();for(int i=0;i<4;i++)for(int n=0;n<5;n++)vertices[i][n]=bytes.getFloat(i*20+n*4);b.reset();calls.add("draw");}
 }
 public static void main(String[] args)throws Exception {
  net.minecraft.init.Bootstrap.register();Field u=sun.misc.Unsafe.class.getDeclaredField("theUnsafe");u.setAccessible(true);sun.misc.Unsafe unsafe=(sun.misc.Unsafe)u.get(null);minecraft=(Minecraft)unsafe.allocateInstance(Minecraft.class);minecraft.renderEngine=new CaptureTextures();tess=new CaptureTessellator();
  String name="com.hbm.inventory.fluid.tank.FluidTank";InputStream input=FluidRenderNativeTest.class.getClassLoader().getResourceAsStream(name.replace('.','/')+".class");ClassReader reader=new ClassReader(input);ClassWriter writer=new ClassWriter(0);final int[] redirects={0};
  reader.accept(new ClassVisitor(Opcodes.ASM5,writer){public MethodVisitor visitMethod(int access,String n,String d,String sig,String[] ex){return new MethodVisitor(Opcodes.ASM5,super.visitMethod(access,n,d,sig,ex)){public void visitMethodInsn(int op,String owner,String method,String desc,boolean itf){if(op==Opcodes.INVOKESTATIC&&((owner.equals("org/lwjgl/opengl/GL11")&&(method.equals("glEnable")||method.equals("glDisable")||method.equals("glColor3d")))||(owner.equals("net/minecraft/client/Minecraft")&&method.equals("getMinecraft"))||(owner.equals("net/minecraft/client/renderer/Tessellator")&&method.equals("getInstance")))){owner="FluidRenderNativeTest";redirects[0]++;}super.visitMethodInsn(op,owner,method,desc,itf);}};}},0);
  check(redirects[0]==6,"only six expected GPU/engine-holder boundary calls redirected");byte[] bytes=writer.toByteArray();Class<?> target=new ClassLoader(FluidRenderNativeTest.class.getClassLoader()){Class<?> make(){return defineClass(name,bytes,0,bytes.length);}}.make();Object tank=target.getConstructor(FluidType.class,int.class).newInstance(Fluids.WATER,16000);Method set=target.getMethod("setFill",int.class),render=target.getMethod("renderTank",int.class,int.class,double.class,int.class,int.class,int.class);
  for(int fill:new int[]{0,1,7999,8000,15999,16000,17000,-100,Integer.MAX_VALUE})for(int orientation:new int[]{0,1,2})for(int width:new int[]{1,16,31})for(int height:new int[]{1,16,39}) {
   set.invoke(tank,fill);calls.clear();render.invoke(tank,7,101,4.25,width,height,orientation);int x=7,y=101-height,i=fill*height/16000;double minX=x,maxX=x,minY=y,maxY=y,minV=1D-i/16D,maxV=1,minU=0,maxU=width/16D;
   if(orientation==0){maxX+=width;minY+=height-i;maxY+=height;}if(orientation==1){i=fill*width/16000;maxX+=i;maxY+=height;minV=0;maxV=height/16D;minU=1;maxU=1D-i/16D;}
   double[][] expected={{minX,maxY,4.25,minU,maxV},{maxX,maxY,4.25,maxU,maxV},{maxX,minY,4.25,maxU,minV},{minX,minY,4.25,minU,minV}};for(int v=0;v<4;v++)for(int n=0;n<5;n++)check(tess.vertices[v][n]==(float)expected[v][n],"original geometry/UV/int overflow/degenerate orientation");
   check(calls.size()==6&&calls.get(0).equals("enable:3042")&&calls.get(1).equals("color:"+(0xab/255D)+","+(0xcd/255D)+","+(0xef/255D))&&calls.get(2).equals("texture:hbm:test")&&calls.get(3).equals("draw")&&calls.get(4).equals("color:1.0,1.0,1.0")&&calls.get(5).equals("disable:3042"),"original bind/tint/blend/postcondition order");
  }
  System.out.println("PASS "+checks+" fluid GUI native BufferBuilder assertions. Only six GPU/holder boundary calls redirected; no live GL/game acceptance.");
 }
}
