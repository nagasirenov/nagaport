import java.net.*;import java.io.*;import net.minecraft.item.*;
public class HierarchyOracle {
 public static void main(String[] a)throws Exception {
 URLClassLoader old=new URLClassLoader(new URL[]{new File(a[0]).toURI().toURL()},HierarchyOracle.class.getClassLoader());
 Class<?> armor=Class.forName("abb",false,old),item=Class.forName("adb",false,old);
 if(armor.getSuperclass()!=item||armor.getMethod("c",String.class).getDeclaringClass()!=item)throw new AssertionError("old armor setter override");
 if(ItemArmor.class.getSuperclass()!=Item.class||ItemArmor.class.getMethod("setTranslationKey",String.class).getDeclaringClass()!=Item.class)throw new AssertionError("native armor setter override");
 old.close();System.out.println("PASS 4 actual original/native armor superclass and inherited setter declaration checks");
 }
}
