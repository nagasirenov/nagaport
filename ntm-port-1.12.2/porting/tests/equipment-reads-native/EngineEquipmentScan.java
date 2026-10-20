import java.io.*;
import java.util.*;
import java.util.jar.*;
import org.objectweb.asm.*;
/** Complete offline class/method census; names alone never establish ownership. */
public final class EngineEquipmentScan {
 static class Type {String parent;List<String> methods=new ArrayList<>();}
 static final Map<String,Type> types=new TreeMap<>();
 static boolean child(String name,String base) {
  Set<String> seen=new HashSet<>();
  while(name!=null&&seen.add(name)) {if(name.equals(base))return true;Type t=types.get(name);name=t==null?null:t.parent;}
  return false;
 }
 public static void main(String[] args)throws Exception {
  boolean old=args[0].equals("legacy");
  try(JarFile jar=new JarFile(args[1])) {
   Enumeration<JarEntry> entries=jar.entries();
   while(entries.hasMoreElements()) {JarEntry entry=entries.nextElement();if(!entry.getName().endsWith(".class"))continue;
    try(InputStream in=jar.getInputStream(entry)) {new ClassReader(in).accept(new ClassVisitor(Opcodes.ASM5){Type type;
     public void visit(int v,int a,String name,String sig,String parent,String[] itfs){type=new Type();type.parent=parent;types.put(name,type);}
     public MethodVisitor visitMethod(int a,String name,String desc,String sig,String[] ex){type.methods.add(name+desc);return null;}
    },ClassReader.SKIP_CODE|ClassReader.SKIP_DEBUG|ClassReader.SKIP_FRAMES);}
   }
  }
  System.out.println("CLASSES\t"+types.size());
  for(Map.Entry<String,Type> entry:types.entrySet()) for(String method:entry.getValue().methods) {
   String name=entry.getKey();
   boolean match=old ? child(name,"sv")&&(method.equals("q(I)Ladd;")||child(name,"yz")&&method.equals("r(I)Ladd;")) || child(name,"yx")&&method.equals("d(I)Ladd;") : child(name,"net/minecraft/entity/EntityLivingBase")&&method.equals("getItemStackFromSlot(Lnet/minecraft/inventory/EntityEquipmentSlot;)Lnet/minecraft/item/ItemStack;") || child(name,"net/minecraft/entity/player/InventoryPlayer")&&method.equals("armorItemInSlot(I)Lnet/minecraft/item/ItemStack;");
   if(match)System.out.println("DECLARE\t"+name+"\t"+entry.getValue().parent+"\t"+method);
  }
 }
}
