import java.util.*;
import org.objectweb.asm.*;
import org.objectweb.asm.commons.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
/** Executes the original 1.7.10 drawCrossedSquares method bytecode with only icon/vertex recording remapped. */
public final class NativePlantLegacyOracle {
 public interface Icon { float c();float d();float e();float f(); }
 public static final class Recorder {
  public static final Recorder a=new Recorder();public final List<double[]> vertices=new ArrayList<>();
  public void a(double x,double y,double z,double u,double v){vertices.add(new double[]{x,y,z,u,v});}
 }
 static Class<?> oracle;
 public static void initialize(String jar)throws Exception {
  byte[] source;try(java.util.zip.ZipFile zip=new java.util.zip.ZipFile(jar);java.io.InputStream in=zip.getInputStream(zip.getEntry("blm.class"))){java.io.ByteArrayOutputStream out=new java.io.ByteArrayOutputStream();byte[] buf=new byte[8192];for(int n;(n=in.read(buf))>0;)out.write(buf,0,n);source=out.toByteArray();}
  final ClassWriter writer=new ClassWriter(0);writer.visit(Opcodes.V1_7,Opcodes.ACC_PUBLIC,"OriginalReedCross",null,"java/lang/Object",null);
  writer.visitField(Opcodes.ACC_PUBLIC,"d","LNativePlantLegacyOracle$Icon;",null,null).visitEnd();
  MethodVisitor ctor=writer.visitMethod(Opcodes.ACC_PUBLIC,"<init>","()V",null,null);ctor.visitCode();ctor.visitVarInsn(Opcodes.ALOAD,0);ctor.visitMethodInsn(Opcodes.INVOKESPECIAL,"java/lang/Object","<init>","()V",false);ctor.visitInsn(Opcodes.RETURN);ctor.visitMaxs(1,1);ctor.visitEnd();
  MethodVisitor override=writer.visitMethod(Opcodes.ACC_PUBLIC,"b","()Z",null,null);override.visitCode();override.visitInsn(Opcodes.ICONST_0);override.visitInsn(Opcodes.IRETURN);override.visitMaxs(1,1);override.visitEnd();
  Map<String,String> map=new HashMap<>();map.put("blm","OriginalReedCross");map.put("rf","NativePlantLegacyOracle$Icon");map.put("bmh","NativePlantLegacyOracle$Recorder");final Remapper remapper=new SimpleRemapper(map);
  new ClassReader(source).accept(new ClassVisitor(Opcodes.ASM5){public MethodVisitor visitMethod(int access,String name,String desc,String signature,String[] exceptions){if(name.equals("a")&&desc.equals("(Lrf;DDDF)V"))return new MethodRemapper(writer.visitMethod(Opcodes.ACC_PUBLIC,"a",remapper.mapMethodDesc(desc),null,null),remapper);return null;}},0);
  writer.visitEnd();final byte[] bytes=writer.toByteArray();oracle=new ClassLoader(NativePlantLegacyOracle.class.getClassLoader()){Class<?> load(){return defineClass("OriginalReedCross",bytes,0,bytes.length);}}.load();
 }
 public static List<double[]> reeds(final TextureAtlasSprite sprite,int depth)throws Exception {
  Recorder.a.vertices.clear();Icon icon=new Icon(){public float c(){return sprite.getMinU();}public float d(){return sprite.getMaxU();}public float e(){return sprite.getMinV();}public float f(){return sprite.getMaxV();}};
  oracle.getMethod("a",Icon.class,double.class,double.class,double.class,float.class).invoke(oracle.newInstance(),icon,0.,(double)-depth,0.,1F);return new ArrayList<>(Recorder.a.vertices);
 }
}
