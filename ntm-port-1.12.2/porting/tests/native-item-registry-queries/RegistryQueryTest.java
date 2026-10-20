import net.minecraft.init.*;
import net.minecraft.item.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import com.google.gson.stream.JsonWriter;
import java.io.*;
import java.util.*;
public final class RegistryQueryTest {
 static int checks,calls; static String trace="";
 static void ok(boolean b,String m){checks++;if(!b)throw new AssertionError(m);}
 static Item argument(Item i){calls++;trace+="A";return i;}
 static JsonWriter writer(StringWriter s){trace+="W";return new JsonWriter(s);}
 static String name(Item i){return java.util.Objects.toString(Item.REGISTRY.getNameForObject(i), null);}
 public static void main(String[] args)throws Exception {
  Bootstrap.register(); aji.p(); adb.l();
  ok(Item.REGISTRY.getClass().getName().equals("net.minecraftforge.registries.NamespacedWrapper"),"actual Forge registry");
  ok(adb.e.c(null)==null && name(null)==null,"null identity has null name, not literal null");
  ok(adb.e.c(new adb())==null && name(new Item())==null,"unregistered identity has null name");
  ResourceLocation missing=new ResourceLocation("hbm:missing_item_query_probe");
  ok(adb.e.a(missing.toString())==null,"original missing object is null");
  ok(Item.REGISTRY.getObject(missing)==null,"native missing object is null");
  ok(!Item.REGISTRY.containsKey(missing),"missing not registered");
  ok(adb.e.a("MINECRAFT:APPLE")==null && Item.REGISTRY.getObject(new ResourceLocation("MINECRAFT:APPLE"))==Items.APPLE,"case normalization mismatch explicitly excludes input lookups");
  Set<Item> identities=Collections.newSetFromMap(new IdentityHashMap<Item,Boolean>());
  for(Item item:Item.REGISTRY) {
   ok(identities.add(item),"iterator exposes each actual identity once");
   String n=name(item);ok(n!=null,"registered name");
   ok(Item.REGISTRY.getObject(new ResourceLocation(n))==item,"native saved-name identity roundtrip");
   NBTTagCompound tag=ProductionSavedNames.nbt(item);
   ok(Item.REGISTRY.getObject(new ResourceLocation(tag.getString("item")))==item,"real NBT saved name");
   StringWriter out=new StringWriter();JsonWriter w=new JsonWriter(out);w.beginArray();ProductionSavedNames.json(w,new ItemStack(item));w.endArray().close();
   ok(new com.google.gson.JsonParser().parse(out.toString()).getAsJsonArray().get(0).getAsString().equals(n),"JSON scalar remains string");
   Object old=adb.e.a(n);
   if(old!=null){ok(n.equals(adb.e.c(old)),"original common saved key exact spelling");}
  }
  ok(identities.size()==Item.REGISTRY.getKeys().size(),"keys/iterator same native membership, no old-order assumption");
  for(Item i:new Item[]{Items.APPLE,Items.DIAMOND,null,new Item()}) {
   StringWriter s=new StringWriter();trace="";calls=0;
   writer(s).value(java.util.Objects.toString(Item.REGISTRY.getNameForObject(argument(i)), null)).close();
   ok(trace.equals("WA")&&calls==1,"writer before item argument, once each");
   ok(s.toString().equals(i==Items.APPLE?"\"minecraft:apple\"":i==Items.DIAMOND?"\"minecraft:diamond\"":"null"),"JSON null/string contract");
  }
  System.out.println("PASS "+checks+" real original/native registry, saved-key, identity, JSON/NBT and order assertions; missing-object null behavior verified; case-changing input lookups excluded");
 }
}
