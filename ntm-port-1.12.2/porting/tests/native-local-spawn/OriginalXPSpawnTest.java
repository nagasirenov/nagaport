import java.lang.reflect.*;import java.net.*;import java.io.*;import java.util.*;
/** Invokes genuine vanilla 1.7.10 binary. Forge1614 additions are checked separately from supplied patches. */
public class OriginalXPSpawnTest {
 static sun.misc.Unsafe U;static int checks;static Field f(Class<?> c,String n)throws Exception{Field f=c.getDeclaredField(n);f.setAccessible(true);return f;}static void check(boolean b,String s){checks++;if(!b)throw new AssertionError(s);}
 public static void main(String[] args)throws Exception{
  Field uf=f(sun.misc.Unsafe.class,"theUnsafe");U=(sun.misc.Unsafe)uf.get(null);
  try(URLClassLoader l=new URLClassLoader(new URL[]{new File(args[0]).toURI().toURL()},OriginalXPSpawnTest.class.getClassLoader())){
   Class.forName("kl",true,l).getMethod("b").invoke(null);Class<?> world=Class.forName("ahb",true,l),client=Class.forName("bjf",true,l),server=Class.forName("mt",true,l),entity=Class.forName("sa",true,l),item=Class.forName("sq",true,l),chunk=Class.forName("apx",true,l),provider=Class.forName("apu",true,l),block=Class.forName("aji",true,l),stack=Class.forName("add",true,l),dimension=Class.forName("aqo",true,l),listener=Class.forName("ahh",true,l);
   check(server.getMethod("d",entity).getDeclaringClass()==world,"legacy server inherits World spawn without UUID/dead precheck");
   for(boolean loaded:new boolean[]{false,true})for(boolean force:new boolean[]{false,true})for(boolean dead:new boolean[]{false,true}){
    Object w=U.allocateInstance(client);f(world,"E").setBoolean(w,true);f(world,"t").set(w,dimension.getMethod("a",int.class).invoke(null,0));f(world,"C").set(w,Class.forName("qi",true,l).newInstance());
    List<Object> entities=new ArrayList<>();f(world,"e").set(w,entities);f(world,"h").set(w,new ArrayList<>());f(client,"J").set(w,new HashSet<>());f(client,"K").set(w,new HashSet<>());
    int[] callbacks={0};Object obs=java.lang.reflect.Proxy.newProxyInstance(l,new Class[]{listener},(o,m,a)->{if(m.getName().equals("a")&&a!=null&&a.length==1&&entity.isInstance(a[0]))callbacks[0]++;return null;});f(world,"u").set(w,new ArrayList<>(Arrays.asList(obs)));
    Object c=chunk.getConstructor(world,int.class,int.class).newInstance(w,-2,1);Object cp=java.lang.reflect.Proxy.newProxyInstance(l,new Class[]{provider},(o,m,a)->{if(m.getReturnType()==chunk)return c;if(m.getReturnType()==boolean.class)return loaded;if(m.getReturnType()==int.class)return 0;return null;});f(world,"v").set(w,cp);
    Object stone=block.getMethod("e",int.class).invoke(null,1);Object s=stack.getConstructor(block).newInstance(stone);Object e=item.getConstructor(world,double.class,double.class,double.class,int.class).newInstance(w,-17.25,65.0,16.75,1);
    check(!f(entity,"K").getBoolean(e),"old exact vanilla constructor alive");f(entity,"K").setBoolean(e,dead);f(entity,"n").setBoolean(e,force);
    boolean result=(Boolean)world.getMethod("d",entity).invoke(w,e),expected=loaded||force;
    check(result==expected,"original return including dead (excluded target divergence)");check(entities.contains(e)==expected,"original list identity");check(callbacks[0]==(expected?1:0),"original native callback count");check(((Set<?>)f(client,"J").get(w)).contains(e),"old client attempted set");check(((Set<?>)f(client,"K").get(w)).contains(e)==!expected,"old client queue");
   }
  }
  System.out.println("PASS "+checks+" genuine original vanilla XP spawn checks; Forge1614 event/snapshot additions independently audited, not claimed executed here");
 }
}
