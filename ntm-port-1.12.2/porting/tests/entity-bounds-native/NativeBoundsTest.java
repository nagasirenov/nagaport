import java.lang.reflect.*;
import java.net.*;
import java.util.*;
import java.util.jar.*;
import java.nio.file.*;
import org.objectweb.asm.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import sun.misc.Unsafe;
public class NativeBoundsTest {
 static int checks;
 static void ok(boolean b,String s){checks++;if(!b)throw new AssertionError(s);}
 static class OverrideEntity extends EntityItem {
  AxisAlignedBB other;int calls;
  OverrideEntity(){super((World)null);}
  @Override public AxisAlignedBB getEntityBoundingBox(){calls++;return other;}
 }
 public static void main(String[] args)throws Exception{
  Field uf=Unsafe.class.getDeclaredField("theUnsafe");uf.setAccessible(true);Unsafe u=(Unsafe)uf.get(null);
  Field f=Entity.class.getDeclaredField("boundingBox");f.setAccessible(true);
  ok(f.getType()==AxisAlignedBB.class&&Modifier.isPrivate(f.getModifiers()),"native actual field owner/type/access");
  for(String name:new String[]{"getEntityBoundingBox","setEntityBoundingBox"}){
   List<String> code=new ArrayList<>();ClassReader cr=new ClassReader(Entity.class.getResourceAsStream("Entity.class"));
   cr.accept(new ClassVisitor(Opcodes.ASM5){public MethodVisitor visitMethod(int access,String n,String d,String sig,String[] ex){if(!n.equals(name))return null;
    return new MethodVisitor(Opcodes.ASM5){public void visitVarInsn(int op,int var){code.add(op+":"+var);}public void visitFieldInsn(int op,String owner,String field,String desc){code.add(op+":"+owner+":"+field+":"+desc);}public void visitInsn(int op){code.add(""+op);}public void visitMethodInsn(int op,String ow,String n,String d,boolean i){throw new AssertionError("accessor calls another method");}};
   }},0);
   String field="net/minecraft/entity/Entity:boundingBox:Lnet/minecraft/util/math/AxisAlignedBB;";
   ok(code.equals(name.startsWith("get")?Arrays.asList("25:0","180:"+field,"176"):Arrays.asList("25:0","25:1","181:"+field,"177")),"native accessor exact bytecode "+name);
  }
  Map<String,String> parents=new HashMap<>();Set<String> declarations=new TreeSet<>();int jars=0;
  for(String path: new String(Files.readAllBytes(Paths.get(args[1]))).trim().split(java.io.File.pathSeparator)){
   if(!path.endsWith(".jar")||!Files.isRegularFile(Paths.get(path)))continue;jars++;
   try(JarFile jar=new JarFile(path)){Enumeration<JarEntry> es=jar.entries();while(es.hasMoreElements()){
    JarEntry e=es.nextElement();if(!e.getName().endsWith(".class"))continue;
    ClassReader cr=new ClassReader(jar.getInputStream(e));parents.put(cr.getClassName(),cr.getSuperName());
    cr.accept(new ClassVisitor(Opcodes.ASM5){public MethodVisitor visitMethod(int a,String n,String d,String s,String[] x){if((n.equals("getEntityBoundingBox")&&d.equals("()Lnet/minecraft/util/math/AxisAlignedBB;"))||(n.equals("setEntityBoundingBox")&&d.equals("(Lnet/minecraft/util/math/AxisAlignedBB;)V")))declarations.add(cr.getClassName());return null;}},ClassReader.SKIP_CODE);
   }}
  }
  for(String d:declarations){String c=d;Set<String> seen=new HashSet<>();while(c!=null&&seen.add(c)){if(c.equals("net/minecraft/entity/Entity")){ok(d.equals(c),"no accessor override in resolved runtime closure: "+d);break;}c=parents.get(c);}}
  EntityItem e=(EntityItem)u.allocateInstance(EntityItem.class);AxisAlignedBB a=new AxisAlignedBB(-3,2,7,9,13,17),b=new AxisAlignedBB(0,0,0,1,1,1);
  e.setEntityBoundingBox(a);ok(f.get(e)==a&&e.getEntityBoundingBox()==a,"actual setter/getter exact reference");
  NativeAccess.trace="";ok(NativeAccess.read(e)==a&&NativeAccess.trace.equals("R"),"receiver evaluated once");
  Entity alias=e;e.setEntityBoundingBox(b);ok(NativeAccess.read(alias)==b,"alias sees current field");
  e.setEntityBoundingBox(null);ok(NativeAccess.read(e)==null,"null box retained");
  NativeAccess.trace="";try{NativeAccess.read(null);throw new AssertionError();}catch(NullPointerException ex){ok(NativeAccess.trace.equals("R"),"null receiver after one evaluation");}
  NativeAccess.trace="";try{NativeAccess.failure();throw new AssertionError();}catch(IllegalStateException ex){ok(NativeAccess.trace.equals("X"),"receiver exception retained");}
  GeneratedProduction p=(GeneratedProduction)u.allocateInstance(GeneratedProduction.class);p.setEntityBoundingBox(a);e.setEntityBoundingBox(b);
  ok(p.getBoundingBox()==a&&p.getCollisionBox(e)==b,"exact production callback bodies return requested native fields");
  e.setEntityBoundingBox(null);ok(p.getCollisionBox(e)==null,"production null box");
  try{p.getCollisionBox(null);throw new AssertionError();}catch(NullPointerException ex){checks++;}
  OverrideEntity o=(OverrideEntity)u.allocateInstance(OverrideEntity.class);o.setEntityBoundingBox(a);o.other=b;
  ok(f.get(o)==a&&NativeAccess.read(o)==b&&o.calls==1,"negative control demonstrates virtual override is NOT field-equivalent");
  try(URLClassLoader old=new URLClassLoader(new URL[]{new java.io.File(args[0]).toURI().toURL()},NativeBoundsTest.class.getClassLoader())){
   Class<?> entity=old.loadClass("sa"),box=old.loadClass("azt");Field bf=entity.getField("C");
   ok(bf.getType()==box&&Modifier.isPublic(bf.getModifiers())&&Modifier.isFinal(bf.getModifiers()),"actual SRG original final public box field");
   Object oe=u.allocateInstance(old.loadClass("xk")),oa=box.getMethod("a",double.class,double.class,double.class,double.class,double.class,double.class).invoke(null,-3.,2.,7.,9.,13.,17.);
   bf.setAccessible(true);bf.set(oe,oa);ok(bf.get(oe)==oa,"original field exact reference");bf.set(oe,null);ok(bf.get(oe)==null,"original null read");
  }
  System.out.println("PASS "+checks+" actual native/original field, accessor bytecode, reference, evaluation, exception, production callback and override negative controls; "+jars+" resolved dependency JARs scanned");
 }
}
