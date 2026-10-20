import java.io.File;
import java.lang.reflect.*;
import java.net.URLClassLoader;
import java.util.*;
import com.google.gson.*;
/** Executes original unmodified 1.7.10 Item and ItemArmor bytecode, mapped by Forge1614 packaged.srg. */
final class OriginalArmorIcons {
 static Field field(Class<?> c,String n)throws Exception{Field f=c.getDeclaredField(n);f.setAccessible(true);return f;}
 static void run(File jar,JsonObject scope)throws Exception {
  try(URLClassLoader loader=new URLClassLoader(new java.net.URL[]{jar.toURI().toURL()},OriginalArmorIcons.class.getClassLoader())) {
   Class<?> item=Class.forName("adb",true,loader),armor=Class.forName("abb",true,loader),material=Class.forName("abd",true,loader),icon=Class.forName("rf",true,loader),register=Class.forName("rg",true,loader),stack=Class.forName("add",true,loader);
   sun.misc.Unsafe unsafe=(sun.misc.Unsafe)field(sun.misc.Unsafe.class,"theUnsafe").get(null);
   Method setter=item.getDeclaredMethod("f",String.class);setter.setAccessible(true);
   int checks=0;
   for(JsonElement e:scope.getAsJsonArray("items")) {
    String texture=new String(e.getAsJsonObject().get("texture").getAsString());
    Object value=unsafe.allocateInstance(armor);field(armor,"p").set(value,material.getField("c").get(null));
    if(setter.invoke(value,texture)!=value||field(item,"l").get(value)!=texture)throw new AssertionError("original texture setter identity");checks++;
    List<String> registrations=new ArrayList<>();Map<String,Object> icons=new HashMap<>();
    Object reg=Proxy.newProxyInstance(loader,new Class<?>[]{register},(p,m,a)->{String name=(String)a[0];registrations.add(name);Object sprite=Proxy.newProxyInstance(loader,new Class<?>[]{icon},(pp,mm,aa)->null);icons.put(name,sprite);return sprite;});
    armor.getMethod("a",register).invoke(value,reg);
    if(!registrations.get(0).equals(texture)||registrations.size()!=2)throw new AssertionError("non-leather original icon plus vanilla empty-slot icon, no overlay");checks++;
    if((Boolean)armor.getMethod("b").invoke(value))throw new AssertionError("non-leather armor single pass");checks++;
    for(int damage:new int[]{Integer.MIN_VALUE,-1,0,1,32767,Integer.MAX_VALUE}) {
     if(armor.getMethod("a",int.class,int.class).invoke(value,damage,0)!=icons.get(texture))throw new AssertionError("original metadata-independent icon");checks++;
     if(!armor.getMethod("a",stack,int.class).invoke(value,null,0).equals(0xffffff))throw new AssertionError("original non-leather white tint");checks++;
    }
   }
   Object leather=unsafe.allocateInstance(armor);field(armor,"p").set(leather,material.getField("a").get(null));
   if(!(Boolean)armor.getMethod("b").invoke(leather))throw new AssertionError("leather multipass negative control");checks++;
   System.out.println("PASS original Item/ItemArmor bytecode: "+checks+" texture identity, one-pass, white tint and metadata-independent icon assertions; leather multipass exclusion control.");
  }
 }
}
