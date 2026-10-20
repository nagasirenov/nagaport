import java.nio.file.*;
import java.lang.reflect.*;
import java.net.*;
import net.minecraft.init.Bootstrap;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
public class VanillaConstantsNativeTest {
 public static void main(String[] args)throws Exception {
  Bootstrap.register();int checked=0;
  URLClassLoader legacy=new URLClassLoader(new URL[]{new java.io.File(args[1]).toURI().toURL()},VanillaConstantsNativeTest.class.getClassLoader());
  Class<?> oldBlock=legacy.loadClass("aji"),oldItem=legacy.loadClass("adb");oldBlock.getMethod("p").invoke(null);oldItem.getMethod("l").invoke(null);
  for(String row:Files.readAllLines(Paths.get(args[0]))) {
   String[] r=row.split("\t");Class<?> owner=Class.forName(r[0]);Field f=owner.getField(r[2]);
   if(!Modifier.isStatic(f.getModifiers())||!Modifier.isFinal(f.getModifiers()))throw new AssertionError(row);
   Object value=f.get(null);Object id=value instanceof Block?Block.REGISTRY.getNameForObject((Block)value):Item.REGISTRY.getNameForObject((Item)value);
   if(!r[3].equals(String.valueOf(id)))throw new AssertionError(row+" resolved to "+id);
   if(value instanceof Block && Block.REGISTRY.getObject(new net.minecraft.util.ResourceLocation(r[3]))!=value)throw new AssertionError(row);
   if(value instanceof Item && Item.REGISTRY.getObject(new net.minecraft.util.ResourceLocation(r[3]))!=value)throw new AssertionError(row);
   String[] member=r[5].split("/");Object previous=legacy.loadClass(member[0]).getField(member[1]).get(null);
   int originalId=(Integer)(value instanceof Block?oldBlock.getMethod("b",oldBlock):oldItem.getMethod("b",oldItem)).invoke(null,previous);
   int nativeId=value instanceof Block?Block.getIdFromBlock((Block)value):Item.getIdFromItem((Item)value);
   if(originalId!=nativeId)throw new AssertionError(row+" original ID "+originalId+" native "+nativeId);
   checked++;
  }
  legacy.close();
  System.out.println("PASS "+checked+" actual original-engine/native static-final registry identity and numeric-ID mappings. No block behavior/state/worldgen equivalence claimed.");
 }
}
