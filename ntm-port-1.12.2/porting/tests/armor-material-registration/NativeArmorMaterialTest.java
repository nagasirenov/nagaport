import java.io.*;
import java.lang.reflect.*;
import java.net.*;
import java.util.*;
import com.google.gson.*;
import net.minecraft.init.Bootstrap;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemArmor.ArmorMaterial;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.client.renderer.entity.layers.LayerBipedArmor;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.oredict.OreDictionary;
/** Real native EnumHelper and ItemArmor; exact production registration slices, no platform stand-ins. */
public final class NativeArmorMaterialTest {
 static int checks, initial;
 static final EntityEquipmentSlot[] SLOTS={EntityEquipmentSlot.HEAD,EntityEquipmentSlot.CHEST,EntityEquipmentSlot.LEGS,EntityEquipmentSlot.FEET};
 static final Map<String,ArmorMaterial> materials=new LinkedHashMap<>();
 static final Map<String,ItemArmor[]> armor=new LinkedHashMap<>();
 static final List<String> events=new ArrayList<>();
 static void check(boolean b,String s){checks++;if(!b)throw new AssertionError(s);}
 static void created(String name,ArmorMaterial material){
  check(material!=null,"EnumHelper returned null "+name);
  check(material.ordinal()==initial+materials.size(),"original creation order "+name);
  check(!materials.containsKey(name),"unique source variable");
  check(ArmorMaterial.values()[material.ordinal()]==material,"real enum values array retains exact identity");
  materials.put(name,material);events.add("create:"+name);
  check(material.getRepairItemStack().isEmpty(),"empty repair before original assignment "+name);
  ItemArmor[] items=new ItemArmor[4];for(int i=0;i<4;i++)items[i]=new ItemArmor(material,0,SLOTS[i]);armor.put(name,items);
 }
 static void repaired(String name,Item expected){
  ArmorMaterial material=materials.get(name);events.add("repair:"+name);
  check(material.getRepairItemStack()==material.repairMaterial,"exact assigned native stack");
  check(material.repairMaterial.getItem()==expected,"exact repair Item identity "+name);
  check(material.repairMaterial.getCount()==1,"repair count");
  check(material.repairMaterial.getMetadata()==OreDictionary.WILDCARD_VALUE,"item-only repair wildcard");
  check(new ItemStack(expected,1,Integer.MAX_VALUE).isEmpty(),"native invalid metadata boundary, not a repair rule");
  for(ItemArmor a:armor.get(name))for(int metadata:new int[]{0,1,17,32766,32767,65535}){
   ItemStack candidate=new ItemStack(expected,1,metadata);NBTTagCompound tag=new NBTTagCompound();tag.setString("sentinel","ignored in original item-only repair");candidate.setTagCompound(tag);
   check(a.getIsRepairable(new ItemStack(a),candidate),"native item-only repair, including metadata and NBT "+name+":"+metadata+":"+candidate.isEmpty()+":"+material.repairMaterial.isEmpty()+":"+candidate.getItem()+":"+material.repairMaterial.getItem());
   check(!a.getIsRepairable(new ItemStack(a),new ItemStack(new Item(),1,metadata)),"different item excluded "+name);
  }
 }
 public static void main(String[] args)throws Exception {
  Bootstrap.register();initial=ArmorMaterial.values().length;
  JsonObject scope=new JsonParser().parse(new FileReader(args[0])).getAsJsonObject();
  ProductionMaterialSlice.createMain();
  check(materials.size()==16,"main static material phase");
  ProductionMaterialSlice.createArmor();
  check(materials.size()==30,"armor init material phase");
  for(String name:ProductionMaterialSlice.MAIN_NAMES)check(materials.get(name).getRepairItemStack().isEmpty(),"main repairs still deferred after armor init");
  ProductionMaterialSlice.createLiquidator();
  check(materials.size()==31,"liquidator later in ModItems init");
  ProductionMaterialSlice.repairMain();
  check(ArmorMaterial.values().length==initial+31,"all and only 31 dynamic enum values");
  for(JsonElement e:scope.getAsJsonArray("events"))check(events.remove(0).equals(e.getAsString()),"exact creation/repair ordering");
  check(events.isEmpty(),"no additional registration events");
  ArmorMaterial taur=materials.get("aMatTaurun"),trench=materials.get("aMatTrench");
  check(taur!=trench&&taur.name().equals(trench.name()),"duplicate names retain distinct identity");
  check(taur.getEnchantability()==10&&trench.getEnchantability()==0,"duplicate names retain distinct properties");
  check(materials.get("aMatEuph").getRepairItemStack().isEmpty(),"Euphemium intentionally unrepaired");
  // Mutability is intentional: the original public repair Item field permitted later reassignment.
  ArmorMaterial mutable=materials.get("aMatRags");ItemStack saved=mutable.repairMaterial;
  Item replacement=new Item();mutable.repairMaterial=new ItemStack(replacement,1,OreDictionary.WILDCARD_VALUE);
  check(armor.get("aMatRags")[0].getIsRepairable(ItemStack.EMPTY,new ItemStack(replacement)),"existing armor sees reassignment immediately");mutable.repairMaterial=saved;
  LayerBipedArmor layer=new LayerBipedArmor(null);
  try(URLClassLoader old=new URLClassLoader(new URL[]{new File(args[1]).toURI().toURL()},NativeArmorMaterialTest.class.getClassLoader())){
   Class<?> oldMat=Class.forName("abd",true,old),oldArmor=Class.forName("abb",true,old),oldItem=Class.forName("adb",true,old),oldRenderer=Class.forName("bnw",true,old);
   Constructor<?> constructor=oldMat.getDeclaredConstructor(String.class,int.class,int.class,int[].class,int.class);constructor.setAccessible(true);
   sun.reflect.ConstructorAccessor maker=sun.reflect.ReflectionFactory.getReflectionFactory().newConstructorAccessor(constructor);
   int ordinal=5;
   for(JsonElement e:scope.getAsJsonArray("creations")){
    JsonObject row=e.getAsJsonObject();String name=row.get("variable").getAsString();ArmorMaterial material=materials.get(name);
    int[] damage=new int[4];for(int i=0;i<4;i++)damage[i]=row.getAsJsonArray("damage").get(i).getAsInt();
    Object original=maker.newInstance(new Object[]{row.get("name").getAsString(),ordinal++,row.get("durability").getAsInt(),damage,row.get("enchantability").getAsInt()});
    check(material.name().equals(((Enum<?>)original).name()),"original enum name");
    check(material.getEnchantability()==((Integer)oldMat.getMethod("a").invoke(original)),"original enchantability");
    check(material!=ArmorMaterial.LEATHER,"fallback name never leather enum identity");
    check(material.getName().equals("leather"),"original render-index-0 fallback name");
    check(material.getSoundEvent()==SoundEvents.ITEM_ARMOR_EQUIP_GENERIC,"ordinary native generic equip sound");
    check(material.getToughness()==0.0F,"no invented toughness balance");
    for(int slot=0;slot<4;slot++){
     ItemArmor current=armor.get(name)[slot];Object prior=oldArmor.getConstructor(oldMat,int.class,int.class).newInstance(original,0,slot);
     check(material.getDurability(SLOTS[slot])==((Integer)oldMat.getMethod("a",int.class).invoke(original,slot)),"exact original material durability "+name+":"+slot);
     check(material.getDamageReductionAmount(SLOTS[slot])==((Integer)oldMat.getMethod("b",int.class).invoke(original,slot)),"exact original material reduction "+name+":"+slot);
     check(current.getMaxDamage()==((Integer)oldItem.getMethod("o").invoke(prior)),"real ItemArmor constructor durability "+name+":"+slot);
     check(current.damageReduceAmount==oldArmor.getField("c").getInt(prior),"real ItemArmor constructor reduction "+name+":"+slot);
     check(current.getItemEnchantability()==((Integer)oldArmor.getMethod("c").invoke(prior)),"real ItemArmor enchantability");
     check(current.getArmorMaterial()==material&&current.armorType==SLOTS[slot],"native material and slot identity");
     check(!current.hasColor(new ItemStack(current))&&!current.hasOverlay(new ItemStack(current)),"custom enum remains non-leather");
     String oldResource=oldRenderer.getMethod("a",oldArmor,int.class).invoke(null,prior,slot).toString();
     check(layer.getArmorResource(null,new ItemStack(current),SLOTS[slot],null).toString().equals(oldResource),"real original/native fallback texture "+name+":"+slot);
    }
   }
   // Observe inherited engine differences, rather than hiding them behind an armor override.
   Class.forName("kl",true,old).getMethod("b").invoke(null);
   Class<?> oldStack=Class.forName("add",true,old);
   Object oldIron=oldMat.getField("c").get(null),oldRepair=oldMat.getMethod("b").invoke(oldIron);
   Object priorIron=oldArmor.getConstructor(oldMat,int.class,int.class).newInstance(oldIron,0,0);
   ItemArmor nativeIron=new ItemArmor(ArmorMaterial.IRON,0,EntityEquipmentSlot.HEAD);
   Method oldRepairable=oldArmor.getMethod("a",oldStack,oldStack);
   try(PrintWriter report=new PrintWriter(args[2])){
    report.println("scenario\told_item_identity\tnative_item_identity\tnative_isEmpty\told_repair\tnative_repair\told_metadata\tnative_metadata");
    String[] cases={"canonical","metadata_17","metadata_65535","metadata_minus1","metadata_minus32768","metadata_minus32769","metadata_MAX","count_zero","count_negative","restore_count","cached_empty_metadata_restore","live_invalid_metadata","live_metadata_restore"};
    int[] metadata={0,17,65535,-1,-32768,-32769,Integer.MAX_VALUE,0,0,0,Integer.MAX_VALUE,0,0};
    int[] counts={1,1,1,1,1,1,1,0,-1,0,1,1,1};
    for(int i=0;i<cases.length;i++){
     Object candidate=oldStack.getConstructor(oldItem,int.class,int.class).newInstance(oldRepair,counts[i],metadata[i]);
     ItemStack nativeCandidate=new ItemStack(net.minecraft.init.Items.IRON_INGOT,counts[i],metadata[i]);
     if(i==9){oldStack.getField("b").setInt(candidate,1);nativeCandidate.setCount(1);}
     if(i==10){oldStack.getMethod("b",int.class).invoke(candidate,0);nativeCandidate.setItemDamage(0);}
     if(i==11||i==12){oldStack.getMethod("b",int.class).invoke(candidate,Integer.MAX_VALUE);nativeCandidate.setItemDamage(Integer.MAX_VALUE);}
     if(i==12){oldStack.getMethod("b",int.class).invoke(candidate,0);nativeCandidate.setItemDamage(0);}
     boolean oi=oldStack.getMethod("b").invoke(candidate)==oldRepair,ni=nativeCandidate.getItem()==net.minecraft.init.Items.IRON_INGOT;
     boolean was=(Boolean)oldRepairable.invoke(priorIron,null,candidate),now=nativeIron.getIsRepairable(ItemStack.EMPTY,nativeCandidate);
     check(was,"original ItemArmor ignores candidate count and metadata");
     boolean same=i<=5||i==9||i==12;check(now==same,"explicit native boundary "+cases[i]);
     report.println(cases[i]+"\t"+oi+"\t"+ni+"\t"+nativeCandidate.isEmpty()+"\t"+was+"\t"+now+"\t"+oldStack.getMethod("k").invoke(candidate)+"\t"+nativeCandidate.getMetadata());
    }
    Object unconfigured=maker.newInstance(new Object[]{"UNCONFIGURED",ordinal++,20,new int[]{1,1,1,1},0});
    Object unconfiguredArmor=oldArmor.getConstructor(oldMat,int.class,int.class).newInstance(unconfigured,0,0);
    Object nullItem=oldStack.getConstructor(oldItem,int.class,int.class).newInstance(null,1,0);
    boolean was=(Boolean)oldRepairable.invoke(unconfiguredArmor,null,nullItem);
    boolean now=armor.get("aMatEuph")[0].getIsRepairable(ItemStack.EMPTY,new ItemStack((Item)null,1,0));
    check(was&&!now,"malformed null-item candidate and absent repair: explicit inherited boundary");
    report.println("unconfigured_null_item\ttrue\tfalse\ttrue\t"+was+"\t"+now+"\t0\t0");
   }
   // Boundary controls execute both engines' int multiplication, rather than clamping or widening.
   for(int factor:new int[]{0,-1,Integer.MAX_VALUE,Integer.MIN_VALUE,15000000}){
    Object original=maker.newInstance(new Object[]{"OVERFLOW_CONTROL",ordinal++,factor,new int[]{1,2,3,4},9});
    ArmorMaterial current=EnumHelper.addArmorMaterial("OVERFLOW_CONTROL","leather",factor,new int[]{4,3,2,1},9,SoundEvents.ITEM_ARMOR_EQUIP_GENERIC,0.0F);
    for(int slot=0;slot<4;slot++)check(current.getDurability(SLOTS[slot])==((Integer)oldMat.getMethod("a",int.class).invoke(original,slot)),"signed int overflow parity");
   }
  }
  System.out.println("PASS "+checks+" real native/original assertions: 31 native enum creations, 30 exact repair identities, source creation/assignment timing, duplicate names, all slots, original constructors/durability/enchantability/render fallbacks, item-only repair metadata/NBT and signed-int overflow controls.");
 }
}
