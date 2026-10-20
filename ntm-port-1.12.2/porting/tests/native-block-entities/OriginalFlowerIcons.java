import java.io.*;import java.util.*;import java.util.zip.*;import java.lang.reflect.*;
import org.objectweb.asm.*;import org.objectweb.asm.commons.*;
/** Executes original BlockFlower.getIcon bytecode with icon handles as strings. */
public class OriginalFlowerIcons {
 public static void verify(String jar)throws Exception {
  byte[] source;try(ZipFile z=new ZipFile(jar);InputStream in=z.getInputStream(z.getEntry("alc.class"));ByteArrayOutputStream out=new ByteArrayOutputStream()){byte[] b=new byte[4096];for(int n;(n=in.read(b))>0;)out.write(b,0,n);source=out.toByteArray();}
  ClassWriter w=new ClassWriter(0);w.visit(Opcodes.V1_7,Opcodes.ACC_PUBLIC,"OriginalFlowerIconBytecode",null,"java/lang/Object",null);w.visitField(Opcodes.ACC_PUBLIC,"N","[Ljava/lang/String;",null,null).visitEnd();Map<String,String> mapping=new HashMap<>();mapping.put("alc","OriginalFlowerIconBytecode");mapping.put("rf","java/lang/String");Remapper remap=new SimpleRemapper(mapping);int[] methods={0};
  new ClassReader(source).accept(new ClassVisitor(Opcodes.ASM5){public MethodVisitor visitMethod(int access,String name,String desc,String sig,String[] ex){if(name.equals("a")&&desc.equals("(II)Lrf;")){methods[0]++;return new MethodRemapper(w.visitMethod(Opcodes.ACC_PUBLIC,name,remap.mapMethodDesc(desc),null,null),remap);}return null;}},0);
  if(methods[0]!=1)throw new AssertionError("one exact old getIcon method");MethodVisitor m=w.visitMethod(Opcodes.ACC_PUBLIC,"<init>","()V",null,null);m.visitCode();m.visitVarInsn(Opcodes.ALOAD,0);m.visitMethodInsn(Opcodes.INVOKESPECIAL,"java/lang/Object","<init>","()V",false);m.visitInsn(Opcodes.RETURN);m.visitMaxs(1,1);m.visitEnd();w.visitEnd();byte[] bytes=w.toByteArray();Class<?> c=new ClassLoader(OriginalFlowerIcons.class.getClassLoader()){Class<?> make(){return defineClass("OriginalFlowerIconBytecode",bytes,0,bytes.length);}}.make();Object flower=c.newInstance();java.lang.reflect.Method get=c.getMethod("a",int.class,int.class);
  for(int count:new int[]{1,9}){String[] icons=new String[count];for(int i=0;i<count;i++)icons[i]="flower_"+i;c.getField("N").set(flower,icons);for(int side=0;side<6;side++)for(int meta=0;meta<=count;meta++)BlockEntityNativeTest.eq(icons[meta>=count?0:meta],get.invoke(flower,side,meta),"actual old yellow/red flower getIcon ignores side and preserves subtype/clamp");}
 }
}
