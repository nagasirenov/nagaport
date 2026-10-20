import java.lang.reflect.*;
import java.nio.file.*;
import java.util.*;
import java.util.jar.*;
import org.objectweb.asm.*;
import sun.misc.Unsafe;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.world.WorldEvent;
public class NativeEventTest {
 static int checks; static String trace="";
 static void ok(boolean b,String s){checks++;if(!b)throw new AssertionError(s);}
 static EntityEvent receiver(EntityEvent e){trace+="R";return e;}
 static EntityEvent failure(){trace+="X";throw new IllegalStateException();}
 static class OverrideEvent extends EntityEvent {OverrideEvent(Entity e){super(e);} public Entity getEntity(){return null;}}
 public static void main(String[] args)throws Exception{
  net.minecraft.init.Bootstrap.register();
  Field uf=Unsafe.class.getDeclaredField("theUnsafe");uf.setAccessible(true);Unsafe u=(Unsafe)uf.get(null);
  Class<?>[] owners={EntityEvent.class,LivingEvent.class,WorldEvent.class};
  String[] fields={"entity","entityLiving","world"}, getters={"getEntity","getEntityLiving","getWorld"};
  Object[] objects={u.allocateInstance(EntityItem.class),u.allocateInstance(EntityPig.class),u.allocateInstance(WorldServer.class)};
  Class<?>[] params={Entity.class,EntityLivingBase.class,World.class};
  for(int i=0;i<3;i++){
   Class<?> owner=owners[i];String field=fields[i],getter=getters[i];Field f=owner.getDeclaredField(field);f.setAccessible(true);
   ok(Modifier.isPrivate(f.getModifiers())&&Modifier.isFinal(f.getModifiers())&&f.getType()==params[i],"actual native owner/type/private final");
   Object event=owner.getConstructor(params[i]).newInstance(objects[i]);Method m=owner.getMethod(getter);
   ok(f.get(event)==objects[i]&&m.invoke(event)==objects[i],"actual event constructor/getter reference identity");
   ok(m.invoke(event)==m.invoke(event),"repeated getter stable identity");
   Object nil=owner.getConstructor(params[i]).newInstance(new Object[]{null});ok(m.invoke(nil)==null,"null payload preserved");
   List<String> code=new ArrayList<>();ClassReader cr=new ClassReader(owner.getResourceAsStream(owner.getSimpleName()+".class"));
   cr.accept(new ClassVisitor(Opcodes.ASM5){public MethodVisitor visitMethod(int a,String n,String d,String s,String[] x){if(!n.equals(getter))return null;return new MethodVisitor(Opcodes.ASM5){public void visitVarInsn(int op,int v){code.add(op+":"+v);}public void visitFieldInsn(int op,String o,String f,String d){code.add(op+":"+o+":"+f+":"+d);}public void visitInsn(int op){code.add(""+op);}public void visitMethodInsn(int op,String o,String n,String d,boolean itf){throw new AssertionError("getter invokes method");}};}},0);
   ok(code.equals(Arrays.asList("25:0","180:"+owner.getName().replace('.','/')+":"+field+":"+org.objectweb.asm.Type.getDescriptor(params[i]),"176")),"exact raw getter bytecode");
  }
  Entity e=(Entity)objects[0];EntityEvent event=new EntityEvent(e);
  ok(receiver(event).getEntity()==e&&trace.equals("R"),"receiver exactly once");
  trace="";try{receiver(null).getEntity();throw new AssertionError();}catch(NullPointerException ex){ok(trace.equals("R"),"null receiver timing");}
  trace="";try{failure().getEntity();throw new AssertionError();}catch(IllegalStateException ex){ok(trace.equals("X"),"receiver exception");}
  LivingEvent.LivingUpdateEvent update=new LivingEvent.LivingUpdateEvent((EntityLivingBase)objects[1]);
  boolean canceled=update.isCanceled(),cancelable=update.isCancelable();
  ok(update.getEntityLiving()==objects[1]&&update.getEntity()==objects[1],"derived actual event shared entity identity");
  ok(update.isCanceled()==canceled&&update.isCancelable()==cancelable,"getters leave existing cancellation flags unchanged; no transformed lifecycle claim");
  OverrideEvent override=new OverrideEvent(e);ok(override.getEntity()==null,"virtual override negative witness: external overrides not certified");
  Map<String,String> parents=new HashMap<>();Map<String,Set<String>> declarations=new HashMap<>();int jars=0;
  for(String path:new String(Files.readAllBytes(Paths.get(args[0]))).trim().split(java.io.File.pathSeparator)){
   if(!path.endsWith(".jar")||!Files.isRegularFile(Paths.get(path)))continue;jars++;
   try(JarFile jar=new JarFile(path)){Enumeration<JarEntry> es=jar.entries();while(es.hasMoreElements()){
    JarEntry entry=es.nextElement();if(!entry.getName().endsWith(".class"))continue;ClassReader cr=new ClassReader(jar.getInputStream(entry));parents.put(cr.getClassName(),cr.getSuperName());
    cr.accept(new ClassVisitor(Opcodes.ASM5){public MethodVisitor visitMethod(int a,String n,String d,String s,String[] x){for(int i=0;i<3;i++)if(n.equals(getters[i])&&d.equals("()"+org.objectweb.asm.Type.getDescriptor(params[i])))declarations.computeIfAbsent(cr.getClassName(),k->new HashSet<>()).add(n);return null;}},ClassReader.SKIP_CODE);
   }}
  }
  for(int i=0;i<3;i++){String root=owners[i].getName().replace('.','/');for(Map.Entry<String,Set<String>> d:declarations.entrySet()){if(!d.getValue().contains(getters[i]))continue;String c=d.getKey();Set<String> seen=new HashSet<>();while(c!=null&&seen.add(c)){if(c.equals(root)){ok(d.getKey().equals(root),"dependency accessor override: "+d.getKey());break;}c=parents.get(c);}}}
  System.out.println("PASS "+checks+" actual native checks; "+jars+" dependency jars inspected; not full handler/game lifecycle execution");
  System.exit(0);
 }
}
