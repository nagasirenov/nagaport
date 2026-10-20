import java.nio.file.*;
import org.objectweb.asm.*;
/** Test-only real-class field rename. No method body or field value changes. */
public final class RemapQuantityField {
 public static void main(String[]args)throws Exception {
  String owner="net/minecraft/item/ItemStack",from="stackSize",to="field_77994_a";
  byte[] original=Files.readAllBytes(Paths.get(args[0]));ClassReader r=new ClassReader(original);ClassWriter w=new ClassWriter(0);
  r.accept(new ClassVisitor(Opcodes.ASM5,w){
   @Override public FieldVisitor visitField(int access,String name,String descriptor,String signature,Object value){return super.visitField(access,name.equals(from)?to:name,descriptor,signature,value);}
   @Override public MethodVisitor visitMethod(int access,String name,String descriptor,String signature,String[]exceptions){return new MethodVisitor(Opcodes.ASM5,super.visitMethod(access,name,descriptor,signature,exceptions)){
    @Override public void visitFieldInsn(int opcode,String o,String n,String d){super.visitFieldInsn(opcode,o,o.equals(owner)&&n.equals(from)?to:n,d);}
   };}
  },0);
  Path out=Paths.get(args[1]);Files.createDirectories(out.getParent());Files.write(out,w.toByteArray());
 }
}
