import java.net.*;import java.io.*;import java.lang.reflect.*;
import net.minecraft.init.Bootstrap;import net.minecraft.item.*;import net.minecraft.inventory.EntityEquipmentSlot;import sun.misc.Unsafe;
public class ArmorNameOracle {
 static int n;static void check(boolean b){n++;if(!b)throw new AssertionError(n);}
 // Test-only descendants, not stand-ins for NTM owner compilation or gameplay.
 static class ArmorChild extends ItemArmor {ArmorChild(){super(ArmorMaterial.IRON,0,EntityEquipmentSlot.CHEST);}}
 static class NamedArmor extends ArmorChild {public String getTranslationKey(){return "virtual.armor.key";}}
 public static void main(String[] args)throws Exception{
 Bootstrap.register();URLClassLoader old=new URLClassLoader(new URL[]{new File(args[0]).toURI().toURL()},ArmorNameOracle.class.getClassLoader());
 Class<?> item=old.loadClass("adb"),armor=old.loadClass("abb");
 Field uf=Unsafe.class.getDeclaredField("theUnsafe");uf.setAccessible(true);Unsafe unsafe=(Unsafe)uf.get(null);Object original=unsafe.allocateInstance(armor);
 Method setter=armor.getMethod("c",String.class),getter=armor.getMethod("a");check(setter.getDeclaringClass()==item);check(ItemArmor.class.getMethod("setTranslationKey",String.class).getDeclaringClass()==Item.class);
 ArmorChild nativeArmor=new ArmorChild();Field raw=Item.class.getDeclaredField("translationKey");raw.setAccessible(true);
 for(String key:new String[]{null,"","armor","item.armor","tile.armor","ñ漢字","armor.name","space key"}){
 String value=key==null?null:new String(key);check(setter.invoke(original,value)==original);check(nativeArmor.setTranslationKey(value)==nativeArmor);check(raw.get(nativeArmor)==value);check(getter.invoke(original).equals(nativeArmor.getTranslationKey()));check(nativeArmor.getTranslationKey().equals("item."+value));check(nativeArmor.getTranslationKey(new ItemStack(nativeArmor)).equals("item."+value));
 }
 Item virtual=new NamedArmor();check(virtual.setTranslationKey("ignored")==virtual);check(virtual.getTranslationKey().equals("virtual.armor.key"));check(virtual.getTranslationKey(new ItemStack(virtual)).equals("item.ignored"));
 old.close();System.out.println("PASS "+n+" real original/native armor inherited setter, raw identity, fluent return, key-prefix, stack and virtual getter assertions");
 }
}
