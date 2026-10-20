import java.nio.file.*;import java.util.*;import java.util.jar.*;import org.objectweb.asm.*;
/** Original complete merge method bytecode with explicit native representation/name edges only. */
public class AdaptOriginalMerge {
 public static void main(String[]a)throws Exception {
  Map<String,String> names=new HashMap<>();String[] pairs={"func_77985_e","isStackable","func_77976_d","getMaxStackSize","func_75219_a","getSlotStackLimit","func_75214_a","isItemValid","func_77981_g","getHasSubtypes","func_75215_d","putStack","func_77979_a","splitStack"};for(int i=0;i<pairs.length;i+=2)names.put(pairs[i],pairs[i+1]);
  ClassWriter w=new ClassWriter(0);w.visit(Opcodes.V1_8,Opcodes.ACC_PUBLIC,"OriginalMergeOracle",null,"java/lang/Object",null);
  try(JarFile jar=new JarFile(a[0])) {new ClassReader(jar.getInputStream(jar.getJarEntry("com/hbm/util/InventoryUtil.class"))).accept(new ClassVisitor(Opcodes.ASM5){
   public MethodVisitor visitMethod(int access,String name,String desc,String sig,String[]exceptions){if(!name.equals("mergeItemStack"))return null;return new MethodVisitor(Opcodes.ASM5,w.visitMethod(access,name,desc,sig,exceptions)){
    public void visitFieldInsn(int op,String owner,String name,String desc){if(owner.equals("net/minecraft/item/ItemStack")&&name.equals("field_77994_a")){if(op==Opcodes.GETFIELD)super.visitMethodInsn(Opcodes.INVOKESTATIC,"NativeBoxContainersTest","raw","(Lnet/minecraft/item/ItemStack;)I",false);else super.visitMethodInsn(Opcodes.INVOKEVIRTUAL,owner,"setCount","(I)V",false);}else throw new AssertionError(owner+name);}
    public void visitMethodInsn(int op,String owner,String name,String desc,boolean iface){
     if(owner.equals("net/minecraft/inventory/Slot")&&name.equals("func_75211_c")){super.visitMethodInsn(Opcodes.INVOKESTATIC,"NativeBoxContainersTest","nullable","(Lnet/minecraft/inventory/Slot;)Lnet/minecraft/item/ItemStack;",false);return;}
     if(owner.equals("net/minecraft/item/ItemStack")&&name.equals("func_77973_b")){super.visitMethodInsn(Opcodes.INVOKESTATIC,"NativeBoxContainersTest","rawItem","(Lnet/minecraft/item/ItemStack;)Lnet/minecraft/item/Item;",false);return;}
     if(owner.equals("net/minecraft/item/ItemStack")&&name.equals("func_77960_j")){super.visitMethodInsn(Opcodes.INVOKESTATIC,"NativeBoxContainersTest","rawDamage","(Lnet/minecraft/item/ItemStack;)I",false);return;}
     if(owner.equals("net/minecraft/item/ItemStack")&&name.equals("func_77970_a")){super.visitMethodInsn(Opcodes.INVOKESTATIC,"NativeBoxContainersTest","rawTagsEqual",desc,false);return;}
     if(owner.equals("com/hbm/util/ItemStackUtil")&&name.equals("carefulCopyWithSize")){super.visitMethodInsn(Opcodes.INVOKESTATIC,"NativeBoxContainersTest","originalCopy",desc,false);return;}
     if(names.containsKey(name))name=names.get(name);else if(name.startsWith("func_"))throw new AssertionError(name);
     super.visitMethodInsn(op,owner,name,desc,iface);
    }
   };}
  },0);}
  w.visitEnd();Files.write(Paths.get(a[1]),w.toByteArray());
 }
}
