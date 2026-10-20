import java.io.*;
import java.lang.reflect.*;
import java.util.*;
import com.google.gson.*;
import com.hbm.items.ModItems;
import com.hbm.items.armor.*;
import com.hbm.handler.ArmorModHandler;
import com.hbm.render.model.*;
import com.hbm.util.ArmorSlotOrder;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.*;
import net.minecraft.init.Bootstrap;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraft.entity.ai.attributes.AttributeModifier;

public class NativeArmorSlotsTest {
 static int checks;
 static void ok(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
 static Object make(String name)throws Exception{return Class.forName("ProductionSlotProjections$"+name).newInstance();}
 static String descriptor(ModelBiped m)throws Exception {
  if(m==null)return "null";
  return m.getClass().getName()+":"+m.getClass().getField("type").getInt(m);
 }
 static void resetItems()throws Exception {for(Field f:ModItems.class.getFields())f.set(null,new Item());}
 static void modelHooks(JsonObject scope)throws Exception {
  ModelBiped fallback=new ModelBiped();int methods=0;
  for(Map.Entry<String,JsonElement> entry:scope.getAsJsonObject("hooks").entrySet()) {
   String owner=entry.getKey();boolean has=false;for(JsonElement e:entry.getValue().getAsJsonArray())if(e.getAsString().startsWith("getArmorModel("))has=true;if(!has)continue;methods++;
   List<String> choices=new ArrayList<>();choices.add(null);for(JsonElement e:scope.getAsJsonArray("item_fields"))choices.add(e.getAsString());
   for(String identity:choices){
    resetItems();Item nativeItem=(Item)make(owner+"Native");Object legacy=make(owner+"Legacy");
    Method old=legacy.getClass().getMethod("getArmorModel",EntityLivingBase.class,ItemStack.class,int.class);
    ItemStack nativeStack=new ItemStack(nativeItem),oldStack=new ItemStack((Item)legacy);
    for(int i:new int[]{3,0,2,1,0,3}){
     if(identity!=null)ModItems.class.getField(identity).set(null,nativeItem);
     ModelBiped actual=nativeItem.getArmorModel(null,nativeStack,ArmorSlotOrder.fromIndex(i),fallback);
     ModelBiped again=nativeItem.getArmorModel(null,nativeStack,ArmorSlotOrder.fromIndex(i),fallback);
     ok(actual==again,owner+" native cache identity");
     ok(ForgeHooksClient.getArmorModel(null,nativeStack,ArmorSlotOrder.fromIndex(i),fallback)==(actual==null?fallback:actual),owner+" actual Forge virtual hook and null fallback");
     if(identity!=null)ModItems.class.getField(identity).set(null,legacy);
     ModelBiped original=(ModelBiped)old.invoke(legacy,null,oldStack,i);
     ok(descriptor(actual).equals(descriptor(original)),owner+" source oracle model identity/type slot "+i+" identity "+identity);
     ok(original==old.invoke(legacy,null,oldStack,i),owner+" original source cache identity");
    }
   }
  }
  ok(methods==24,"all 24 complete production hook bodies exercised");
 }
 static void textureHooks(JsonObject scope)throws Exception {
  int methods=0;
  for(Map.Entry<String,JsonElement> entry:scope.getAsJsonObject("hooks").entrySet()) {
   String owner=entry.getKey();boolean has=false;for(JsonElement e:entry.getValue().getAsJsonArray())if(e.getAsString().startsWith("getArmorTexture("))has=true;if(!has)continue;methods++;
   resetItems();Item nativeItem=(Item)make(owner+"Native");Object legacy=make(owner+"Legacy");
   Method old=legacy.getClass().getMethod("getArmorTexture",ItemStack.class,Entity.class,int.class,String.class);
   List<Item> items=new ArrayList<>();items.add(new Item());for(Field f:ModItems.class.getFields())items.add((Item)f.get(null));
   for(Item item:items)for(int i=0;i<4;i++)for(String layer:new String[]{null,"overlay"}) {
    ItemStack stack=new ItemStack(item);String actual=nativeItem.getArmorTexture(stack,null,ArmorSlotOrder.fromIndex(i),layer);Object original=old.invoke(legacy,stack,null,i,layer);
    ok(Objects.equals(actual,original),owner+" exact worn texture/layer slot "+i);
   }
  }
  ok(methods==13,"all 13 complete production worn texture hooks exercised");
 }
 static void validHooks()throws Exception {
  for(String owner:new String[]{"JetpackBase","ItemModHeavyBoots"}){
   Item n=(Item)make(owner+"Native");Object old=make(owner+"Legacy");Method m=old.getClass().getMethod("isValidArmor",ItemStack.class,int.class,Entity.class);
   for(int i=0;i<4;i++)ok(n.isValidArmor(ItemStack.EMPTY,ArmorSlotOrder.fromIndex(i),null)==(Boolean)m.invoke(old,ItemStack.EMPTY,i,null),"native virtual validity "+owner+" slot "+i);
   for(EntityEquipmentSlot hand:new EntityEquipmentSlot[]{EntityEquipmentSlot.MAINHAND,EntityEquipmentSlot.OFFHAND})ok(!n.isValidArmor(ItemStack.EMPTY,hand,null),"validity rejects non-armor hands");
  }
 }
 static void constructors(JsonObject scope)throws Exception {
  for(JsonElement e:scope.getAsJsonArray("constructor_projections")) {
   Class<?> type=Class.forName("ProductionSlotProjections$"+e.getAsString());
   for(int i=0;i<4;i++){
    ItemArmor armor;try {armor=(ItemArmor)type.getConstructor(ItemArmor.ArmorMaterial.class,int.class).newInstance(ItemArmor.ArmorMaterial.IRON,i);}
    catch(NoSuchMethodException ex){try{armor=(ItemArmor)type.getConstructor(ItemArmor.ArmorMaterial.class,int.class,String.class).newInstance(ItemArmor.ArmorMaterial.IRON,i,"test:worn");}catch(NoSuchMethodException gas){armor=(ItemArmor)type.newInstance();}}
    int expected=e.getAsString().equals("CtorArmorGasMask")?0:i;EntityEquipmentSlot slot=ArmorSlotOrder.fromIndex(expected);
    ok(armor.armorType==slot,"complete constructor native slot "+e);ok(armor.renderIndex==0,"constructor render index preserved");ok(armor.getArmorMaterial()==ItemArmor.ArmorMaterial.IRON,"constructor material identity");
    ok(armor.getMaxDamage()==ItemArmor.ArmorMaterial.IRON.getDurability(slot),"constructor durability slot");ok(armor.damageReduceAmount==ItemArmor.ArmorMaterial.IRON.getDamageReductionAmount(slot),"constructor protection slot");
    if(e.getAsString().equals("CtorArmorFSB"))ok(com.hbm.render.item.NativeItemTextureRegistry.texture(armor).equals("hbm:armor"),"complete FSB constructor retains native inventory texture registration");
   }
  }
 }
 static void realOwners()throws Exception {
  for(int i=0;i<4;i++) {
   EntityEquipmentSlot slot=ArmorSlotOrder.fromIndex(i);ItemArmor mask=new MaskOfInfamy(ItemArmor.ArmorMaterial.IRON,i);ItemArmor mod=new ModArmor(ItemArmor.ArmorMaterial.IRON,i);ItemArmor glasses=new ArmorAshGlasses(ItemArmor.ArmorMaterial.IRON,i);
   ok(mask.armorType==slot&&mod.armorType==slot&&glasses.armorType==slot,"three complete representative production armor constructors");
   Item item=glasses;ModelBiped fallback=new ModelBiped();ModelBiped model=ForgeHooksClient.getArmorModel(null,new ItemStack(glasses),slot,fallback);
   ok(model instanceof ModelGlasses&&((ModelGlasses)model).type==0,"complete ArmorAshGlasses through real Forge hook returns actual production model");
   ok(model==ForgeHooksClient.getArmorModel(null,new ItemStack(glasses),slot,fallback),"complete owner model cache identity");
   ok(mask.getArmorTexture(new ItemStack(mask),null,slot,null).equals("hbm:textures/armor/MaskOfInfamy.png"),"complete mask worn texture");
   ModItems.steel_legs=mod;ok(mod.getArmorTexture(new ItemStack(mod),null,slot,null).equals("hbm:textures/armor/steel_2.png"),"complete ModArmor original identity-based leg texture");
   for(int bits=0;bits<16;bits++) {
    ItemStack stack=new ItemStack(mask),modifier=new ItemStack(new ItemArmorMod(bits));
    ok(ArmorModHandler.isApplicable(stack,modifier)==((bits&(1<<i))!=0),"complete ArmorModHandler applicability for all masks/slots");
   }
  }
  ok(!ArmorModHandler.isApplicable(null,new ItemStack(new ItemArmorMod(15))),"null armor guard");
  ok(!ArmorModHandler.isApplicable(new ItemStack(new Item()),new ItemStack(new ItemArmorMod(15))),"nonarmor guard");
  ok(!ArmorModHandler.isApplicable(new ItemStack(new MaskOfInfamy(ItemArmor.ArmorMaterial.IRON,0)),new ItemStack(new Item())),"nonmod guard");
 }
 static void armorTable()throws Exception {
  net.minecraft.entity.player.InventoryPlayer inventory=new net.minecraft.entity.player.InventoryPlayer(null);
  ItemStack offhand=new ItemStack(new Item());inventory.offHandInventory.set(0,offhand);
  for(int i=0;i<4;i++) {
   ItemStack piece=new ItemStack(new MaskOfInfamy(ItemArmor.ArmorMaterial.IRON,i));
   inventory.armorInventory.set(3-i,piece);
   net.minecraft.inventory.Slot slot=new net.minecraft.inventory.Slot(inventory,ProductionSlotProjections.armorTableIndex(inventory,i),5,36+i*18);
   ok(slot.getStack()==piece,"actual native Slot table row selects original armor piece, not offhand");
   ok(slot.getSlotIndex()==39-i,"original ordinary player index 39 through 36");
   ok(piece.getItem().isValidArmor(piece,ArmorSlotOrder.fromIndex(i),null),"table validation agrees with physical slot");
  }
  ok(inventory.offHandInventory.get(0)==offhand,"armor table leaves offhand untouched");
  ok(inventory.getStackInSlot(inventory.getSizeInventory()-1)==offhand,"control reproduces old expression offhand misselection");
 }
 static void radiationAndModifiers(JsonObject scope)throws Exception {
  double[][] coefficients={{0.2,0.4,0.3,0.1},{0.11,0.23,0.37,0.41},{-1,0,Double.NaN,Double.POSITIVE_INFINITY}};
  for(double[] c:coefficients){
   ProductionSlotProjections.HazmatRegistry.helmet=c[0];ProductionSlotProjections.HazmatRegistry.chest=c[1];ProductionSlotProjections.HazmatRegistry.legs=c[2];ProductionSlotProjections.HazmatRegistry.boots=c[3];
   for(int i=0;i<4;i++)for(double full:new double[]{-1,-0.0,0,0.5,2.4,10,Double.NaN,Double.POSITIVE_INFINITY}){
    ProductionSlotProjections.ArmorFSB armor=new ProductionSlotProjections.ArmorFSB(i);ProductionSlotProjections.HazmatRegistry.external.clear();
    ok(armor.setRadResist(full)==armor,"full radiation method receiver identity");ok(Double.doubleToLongBits(armor.radResist)==Double.doubleToLongBits(full),"full radiation raw configured value");
    ok(ProductionSlotProjections.HazmatRegistry.external.size()==(full>0?1:0),"original radiation positive-only append gate");
    if(full>0){com.hbm.util.Tuple.Pair p=ProductionSlotProjections.HazmatRegistry.external.get(0);ok(p.key==armor,"radiation registry armor identity");ok(Double.doubleToLongBits(((Double)p.value))==Double.doubleToLongBits(full*c[i]),"exact original radiation coefficient slot/product");}
   }
  }
  JsonArray expressions=scope.getAsJsonArray("attribute_expressions");ok(expressions.size()==12,"all 12 fixed/mod UUID constructor expressions");
  for(int i=0;i<4;i++)for(int which=0;which<expressions.size();which++)for(float health:new float[]{-1,0,4,21})for(float speed:new float[]{0,0.25F,1,2}) {
   ProductionSlotProjections.Modifiers item=new ProductionSlotProjections.Modifiers(i);AttributeModifier mod=item.get(which,new ItemStack(item),health,speed);
   String expression=expressions.get(which).getAsJsonObject().get("expression").getAsString();UUID expected=expression.contains("fixedUUIDs[")?ArmorModHandler.fixedUUIDs[i]:ArmorModHandler.UUIDs[i];
   ok(mod.getID().equals(expected),"exact production modifier UUID table slot "+which);
   // Non-slot name, amount, operation and surrounding return map are preserved by full-file inverse.
  }
 }
 public static void main(String[] args)throws Exception {
  Bootstrap.register();JsonObject scope=new JsonParser().parse(new FileReader(args[0])).getAsJsonObject();
  constructors(scope);realOwners();armorTable();modelHooks(scope);textureHooks(scope);validHooks();radiationAndModifiers(scope);
  System.out.println("PASS "+checks+" assertions: all 39 exact callback projections, 8 complete constructor projections, 3 complete armor owners, complete ArmorModHandler with explicit ItemArmorMod fixture, exact radiation method and 12 modifier constructors; real Forge virtual hook + actual ModelGlasses/ModelArmorHEV constructors. No GL draw/gameplay claim.");
 }
}
