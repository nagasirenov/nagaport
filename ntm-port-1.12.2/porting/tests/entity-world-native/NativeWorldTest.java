import java.lang.reflect.*;
import java.net.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.util.IntHashMap;
import sun.misc.Unsafe;
public class NativeWorldTest {
 static int checks;
 static void ok(boolean b,String s){checks++;if(!b)throw new AssertionError(s);}
 static Unsafe unsafe()throws Exception{Field f=Unsafe.class.getDeclaredField("theUnsafe");f.setAccessible(true);return (Unsafe)f.get(null);}
 public static void main(String[] a)throws Exception {
  Unsafe u=unsafe();
  Field field=Entity.class.getField("world");ok(field.getDeclaringClass()==Entity.class&&field.getType()==World.class&&Modifier.isPublic(field.getModifiers())&&!Modifier.isFinal(field.getModifiers()),"native exact public mutable Entity world field");
  URLClassLoader old=new URLClassLoader(new URL[]{new java.io.File(a[0]).toURI().toURL()},NativeWorldTest.class.getClassLoader());
  Class<?> oldEntity=old.loadClass("sa"),oldWorld=old.loadClass("ahb");Field f=oldEntity.getField("o");
  ok(f.getType()==oldWorld&&Modifier.isPublic(f.getModifiers())&&!Modifier.isFinal(f.getModifiers()),"original SRG Entity.field_70170_p exact public mutable World field");
  Object oe=u.allocateInstance(old.loadClass("xk")),ow=u.allocateInstance(old.loadClass("mt"));
  f.set(oe,ow);ok(f.get(oe)==ow,"original field stores exact world identity");f.set(oe,null);ok(f.get(oe)==null,"original permits null world");
  WorldServer w=(WorldServer)u.allocateInstance(WorldServer.class),w2=(WorldServer)u.allocateInstance(WorldServer.class);
  EntityItem e=(EntityItem)u.allocateInstance(EntityItem.class),e2=(EntityItem)u.allocateInstance(EntityItem.class);
  NativeAccess.trace="";ok(NativeAccess.assign(e,w)==w&&e.world==w&&NativeAccess.trace.equals("RW"),"receiver before RHS, assignment expression returns same world");
  NativeAccess.trace="";ok(NativeAccess.read(e)==w&&NativeAccess.trace.equals("R"),"one receiver evaluation and alias read");
  Entity alias=e;ok(NativeAccess.assign(alias,w2)==w2&&e.world==w2,"alias write changes original identity");
  ok(NativeAccess.assign(e,null)==null&&e.world==null,"null world write/read retained");
  NativeAccess.trace="";try{NativeAccess.assign(null,w);throw new AssertionError("expected NPE");}catch(NullPointerException expected){ok(NativeAccess.trace.equals("RW"),"null receiver fails after RHS");}
  NativeAccess.trace="";try{NativeAccess.read(null);throw new AssertionError("expected NPE");}catch(NullPointerException expected){ok(NativeAccess.trace.equals("R"),"null read receiver once");}
  NativeAccess.trace="";try{NativeAccess.throwing(null);throw new AssertionError("expected RHS exception");}catch(IllegalStateException expected){ok(NativeAccess.trace.equals("RX"),"RHS exception wins before null field write");}
  Field map=World.class.getDeclaredField("entitiesById");map.setAccessible(true);IntHashMap<Entity> one=new IntHashMap<>(),two=new IntHashMap<>();one.addKey(17,e);two.addKey(17,e2);map.set(w,one);map.set(w2,two);
  GeneratedProduction p=(GeneratedProduction)u.allocateInstance(GeneratedProduction.class);p.headID=17;p.world=w;
  ok(p.getHead()==e,"actual production getHead body calls real native World entity lookup");p.world=w2;ok(p.getHead()==e2,"production observes reassigned world");p.headID=-1;ok(p.getHead()==null,"production preserves missing ID result");p.world=null;try{p.getHead();throw new AssertionError("expected NPE");}catch(NullPointerException expected){checks++;}
  old.close();System.out.println("PASS "+checks+" original/native field identity, evaluation order, direct writes, aliases, null failures and exact production getHead execution checks");
 }
}
