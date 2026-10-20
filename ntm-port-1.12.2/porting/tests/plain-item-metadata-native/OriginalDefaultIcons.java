import java.io.File;
import java.lang.reflect.*;
import java.net.URLClassLoader;
import java.util.*;
/** Executes unmodified Mojang 1.7.10 Item bytecode; verified SRG mapping recorded by run.py. */
final class OriginalDefaultIcons {
 static void run(File jar)throws Exception {
  try(URLClassLoader loader=new URLClassLoader(new java.net.URL[]{jar.toURI().toURL()},OriginalDefaultIcons.class.getClassLoader())){
   Class<?> item=Class.forName("adb",true,loader),icon=Class.forName("rf",true,loader),register=Class.forName("rg",true,loader);
   sun.misc.Unsafe unsafe=(sun.misc.Unsafe)NativeItemFoundationTest.field(sun.misc.Unsafe.class,"theUnsafe").get(null);
   Object value=unsafe.allocateInstance(item);String texture=new String("hbm:ingot_steel");
   Method setter=item.getDeclaredMethod("f",String.class);setter.setAccessible(true);
   if(setter.invoke(value,texture)!=value||NativeItemFoundationTest.field(item,"l").get(value)!=texture)throw new AssertionError("original texture fluent identity");
   List<String> registered=new ArrayList<>();Object sprite=Proxy.newProxyInstance(loader,new Class<?>[]{icon},(p,m,a)->null);
   Object reg=Proxy.newProxyInstance(loader,new Class<?>[]{register},(p,m,a)->{registered.add((String)a[0]);return sprite;});
   item.getMethod("a",register).invoke(value,reg);
   if(!registered.equals(Collections.singletonList(texture))||NativeItemFoundationTest.field(item,"k").get(value)!=sprite)throw new AssertionError("original one-icon registration");
   Method direct=item.getMethod("b_",int.class),pass=item.getMethod("a",int.class,int.class);int checks=2;
   for(int damage=-32768;damage<=65535;damage++){
    if(direct.invoke(value,damage)!=sprite||pass.invoke(value,damage,0)!=sprite||pass.invoke(value,damage,1)!=sprite)throw new AssertionError("old default icon depends on damage/pass");checks+=3;}
   for(int damage:new int[]{Integer.MIN_VALUE,-32769,65536,Integer.MAX_VALUE}){
    if(direct.invoke(value,damage)!=sprite||pass.invoke(value,damage,Integer.MAX_VALUE)!=sprite)throw new AssertionError("old default integer bounds");checks+=2;}
   System.out.println("PASS original unmodified 1.7.10 Item bytecode: "+checks+" icon identity assertions; metadata/pass-independent default icon, original texture setter and single icon registration.");
  }
 }
}
