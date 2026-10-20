import java.io.*;import java.util.*;import java.util.zip.*;import java.lang.reflect.*;
import org.objectweb.asm.*;import org.objectweb.asm.commons.*;
import net.minecraft.block.Block;import net.minecraft.client.renderer.texture.TextureAtlasSprite;
/** Exact original RenderBlocks six face methods, not handwritten face equations.
 * Only the output buffer and icon interfaces are remapped to CPU capture. */
public class OriginalCubeFaces {
 public interface Icon {float a(double u);float b(double v);float c();float d();float e();float f();}
 public static class Capture {public static final Capture a=new Capture();public final List<float[]> vertices=new ArrayList<>();public void a(double x,double y,double z,double u,double v){vertices.add(new float[]{(float)x,(float)y,(float)z,(float)u,(float)v});}public void b(float r,float g,float b){throw new AssertionError("AO color not part of inventory");}public void b(int light){throw new AssertionError("world light not part of inventory");}}
 static Object oracle;static Class<?> type;
 public static void initialize(String jar)throws Exception {
  byte[] original;try(ZipFile z=new ZipFile(jar);InputStream in=z.getInputStream(z.getEntry("blm.class"));ByteArrayOutputStream out=new ByteArrayOutputStream()){byte[] b=new byte[8192];for(int n;(n=in.read(b))>0;)out.write(b,0,n);original=out.toByteArray();}
  ClassWriter w=new ClassWriter(0);w.visit(Opcodes.V1_7,Opcodes.ACC_PUBLIC,"OriginalCubeFaceBytecode",null,"java/lang/Object",null);
  Map<String,String> names=new HashMap<>();names.put("blm","OriginalCubeFaceBytecode");names.put("rf","OriginalCubeFaces$Icon");names.put("bmh","OriginalCubeFaces$Capture");names.put("aji","net/minecraft/block/Block");Remapper remapper=new SimpleRemapper(names);final int[] methods={0};
  new ClassReader(original).accept(new ClassVisitor(Opcodes.ASM5){public FieldVisitor visitField(int access,String name,String desc,String sig,Object value){if(desc.length()==1||desc.equals("Lrf;"))return w.visitField(Opcodes.ACC_PUBLIC|(access&Opcodes.ACC_STATIC),name,remapper.mapDesc(desc),null,value);return null;}public MethodVisitor visitMethod(int access,String name,String desc,String sig,String[] ex){if(desc.equals("(Laji;DDDLrf;)V")){methods[0]++;return new MethodRemapper(w.visitMethod(Opcodes.ACC_PUBLIC,name,remapper.mapMethodDesc(desc),null,null),remapper);}return null;}},0);
  if(methods[0]!=6)throw new AssertionError("exact six original face bodies required");
  MethodVisitor m=w.visitMethod(Opcodes.ACC_PUBLIC,"<init>","()V",null,null);m.visitCode();m.visitVarInsn(Opcodes.ALOAD,0);m.visitMethodInsn(Opcodes.INVOKESPECIAL,"java/lang/Object","<init>","()V",false);m.visitInsn(Opcodes.RETURN);m.visitMaxs(1,1);m.visitEnd();
  m=w.visitMethod(Opcodes.ACC_PUBLIC,"b","()Z",null,null);m.visitCode();m.visitInsn(Opcodes.ICONST_0);m.visitInsn(Opcodes.IRETURN);m.visitMaxs(1,1);m.visitEnd();w.visitEnd();byte[] bytes=w.toByteArray();type=new ClassLoader(OriginalCubeFaces.class.getClassLoader()){Class<?> make(){return defineClass("OriginalCubeFaceBytecode",bytes,0,bytes.length);}}.make();oracle=type.newInstance();
  for(String n:new String[]{"j","l","n"})type.getField(n).setDouble(oracle,1);
 }
 public static List<float[]> face(int side,final TextureAtlasSprite sprite)throws Exception {
  Capture.a.vertices.clear();Icon icon=new Icon(){public float a(double u){return sprite.getInterpolatedU(u);}public float b(double v){return sprite.getInterpolatedV(v);}public float c(){return sprite.getMinU();}public float d(){return sprite.getMaxU();}public float e(){return sprite.getMinV();}public float f(){return sprite.getMaxV();}};
  type.getMethod("abcdef".substring(side,side+1),Block.class,double.class,double.class,double.class,Icon.class).invoke(oracle,null,0D,0D,0D,icon);return new ArrayList<>(Capture.a.vertices);
 }
}
