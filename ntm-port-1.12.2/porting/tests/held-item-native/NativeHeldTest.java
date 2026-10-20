import java.lang.reflect.*;
import java.net.*;
import net.minecraft.entity.*;
import net.minecraft.entity.player.*;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.*;
import net.minecraft.init.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import com.hbm.util.NativeHeldItem;
import sun.misc.Unsafe;
public final class NativeHeldTest {
 static int checks;
 static void ok(boolean b,String message){checks++;if(!b)throw new AssertionError(message);}
 static Unsafe unsafe()throws Exception{Field f=Unsafe.class.getDeclaredField("theUnsafe");f.setAccessible(true);return (Unsafe)f.get(null);}
 public static class CustomMob extends EntityZombie {
  ItemStack result; int calls; EntityEquipmentSlot last;
  public CustomMob(){super((World)null);}
  @Override public ItemStack getItemStackFromSlot(EntityEquipmentSlot slot){calls++;last=slot;return result;}
 }
 public static class CustomMain extends EntityZombie {
  ItemStack result; int calls;
  public CustomMain(){super((World)null);}
  @Override public ItemStack getHeldItemMainhand(){calls++;return result;}
 }
 public static class ChangingPlayer extends EntityPlayerMP {
  ItemStack[] results; int calls;
  public ChangingPlayer(){super(null,null,null,null);}
  @Override public ItemStack getHeldItemMainhand(){return results[calls++];}
 }
 public static void main(String[] args)throws Exception {
  Bootstrap.register(); Unsafe u=unsafe();
  EntityPlayerMP p=(EntityPlayerMP)u.allocateInstance(EntityPlayerMP.class);p.inventory=new InventoryPlayer(p);
  EntityZombie mob=(EntityZombie)u.allocateInstance(EntityZombie.class);
  Field hands=EntityLiving.class.getDeclaredField("inventoryHands");hands.setAccessible(true);
  NonNullList<ItemStack> inventory=NonNullList.withSize(2,ItemStack.EMPTY);hands.set(mob,inventory);
  ItemStack equipped=new ItemStack(Items.IRON_SWORD),tagged=new ItemStack(Items.STICK,3),zero=new ItemStack(Items.STICK,0);
  NBTTagCompound tag=new NBTTagCompound();tag.setString("unknown","retain");tagged.setTagCompound(tag);zero.setTagCompound(tag);
  ItemStack[] cases={ItemStack.EMPTY,equipped,tagged,zero,new ItemStack(Items.STICK,-1),new ItemStack(Blocks.AIR)};
  for(ItemStack s:cases){
   p.inventory.mainInventory.set(0,s);p.inventory.offHandInventory.set(0,new ItemStack(Items.DIAMOND));inventory.set(0,s);inventory.set(1,new ItemStack(Items.DIAMOND));
   ok(NativeHeldItem.getMainHandNullable(p)==(s==ItemStack.EMPTY?null:s),"player exact nullable identity");
   ok(NativeHeldItem.getMainHandNullable(mob)==(s==ItemStack.EMPTY?null:s),"mob exact nullable identity");
   CustomMob custom=(CustomMob)u.allocateInstance(CustomMob.class);custom.result=s;
   ok(NativeHeldItem.getMainHandNullable(custom)==(s==ItemStack.EMPTY?null:s)&&custom.calls==1&&custom.last==EntityEquipmentSlot.MAINHAND,"virtual custom slot mainhand once");
   CustomMain main=(CustomMain)u.allocateInstance(CustomMain.class);main.result=s;
   ok(NativeHeldItem.getMainHandNullable(main)==(s==ItemStack.EMPTY?null:s)&&main.calls==1,"native custom mainhand override once");
  }
  p.inventory.mainInventory.set(0,zero);ok(zero.isEmpty()&&NativeHeldItem.getMainHandNullable(p)==zero&&zero.getTagCompound()==tag,"noncanonical zero tagged stack NOT normalized");
  ok(zero.getItem()==Item.getItemFromBlock(Blocks.AIR),"document native zero getItem AIR engine boundary");
  CustomMob nullable=(CustomMob)u.allocateInstance(CustomMob.class);ok(NativeHeldItem.getMainHandNullable(nullable)==null&&nullable.calls==1,"custom null override retained");
  try{NativeHeldItem.getMainHandNullable(null);throw new AssertionError();}catch(NullPointerException expected){checks++;}
  p.inventory.currentItem=-1;ok(NativeHeldItem.getMainHandNullable(p)==null,"invalid hotbar index canonical empty");p.inventory.currentItem=9;ok(NativeHeldItem.getMainHandNullable(p)==null,"non-hotbar index canonical empty");p.inventory.currentItem=0;
  for(ItemStack s:cases){p.inventory.mainInventory.set(0,s);ok(ProductionBodies.checkForHeld(p,Items.STICK)==(s==tagged),"exact complete Library checkForHeld method");ProductionBodies b=new ProductionBodies();b.target=(s==ItemStack.EMPTY?null:s);ok(b.isUseableByPlayer(p),"exact complete crate ownership method identity");b.target=new ItemStack(Items.STICK);ok(!b.isUseableByPlayer(p),"crate equal-value distinct stack not ownership");}
  p.inventory.mainInventory.set(0,ItemStack.EMPTY);ok(ProductionBodies.machineGate(p,Items.STICK),"exact transporter predicate empty");p.inventory.mainInventory.set(0,tagged);ok(!ProductionBodies.machineGate(p,Items.STICK),"exact transporter predicate designated item");ok(ProductionBodies.machineGate(p,Items.DIAMOND),"exact transporter predicate other item");
  p.inventory.mainInventory.set(0,ItemStack.EMPTY);ok(!ProductionBodies.weaponGate(p),"exact Crucible gate empty safe");p.inventory.mainInventory.set(0,equipped);ok(ProductionBodies.weaponGate(p),"exact Crucible gate equipped");equipped.setItemDamage(equipped.getMaxDamage());ok(!ProductionBodies.weaponGate(p),"exact Crucible gate exhausted");
  ChangingPlayer changing=(ChangingPlayer)u.allocateInstance(ChangingPlayer.class);changing.results=new ItemStack[]{ItemStack.EMPTY};ok(!ProductionBodies.checkForHeld(changing,Items.STICK)&&changing.calls==1,"production empty guard short circuits once");
  changing.calls=0;changing.results=new ItemStack[]{tagged,equipped};ok(!ProductionBodies.checkForHeld(changing,Items.STICK)&&changing.calls==2,"production repeated access not cached");
  changing.calls=0;changing.results=new ItemStack[]{tagged,ItemStack.EMPTY};try{ProductionBodies.checkForHeld(changing,Items.STICK);throw new AssertionError();}catch(NullPointerException expected){ok(changing.calls==2,"original second-null dereference behavior preserved");}
  URLClassLoader old=new URLClassLoader(new URL[]{new java.io.File(args[0]).toURI().toURL(),new java.io.File(args[1]).toURI().toURL()},NativeHeldTest.class.getClassLoader());
  Class<?> player=old.loadClass("yz"),living=old.loadClass("sw"),stack=old.loadClass("add"),inv=old.loadClass("yx");
  Object op=u.allocateInstance(old.loadClass("mw")),om=u.allocateInstance(old.loadClass("yq")),oi=inv.getConstructor(player).newInstance(op);player.getField("bm").set(op,oi);
  Object array=java.lang.reflect.Array.newInstance(stack,5);Field mobInventory=living.getDeclaredField("br");mobInventory.setAccessible(true);mobInventory.set(om,array);
  Object slots=inv.getField("a").get(oi),os=u.allocateInstance(stack),oz=u.allocateInstance(stack);stack.getField("b").setInt(os,3);stack.getField("b").setInt(oz,0);Object ot=old.loadClass("dh").newInstance();stack.getField("d").set(oz,ot);
  for(Object s:new Object[]{null,os,oz}){java.lang.reflect.Array.set(slots,0,s);java.lang.reflect.Array.set(array,0,s);ok(player.getMethod("be").invoke(op)==s,"actual original player current mainhand exact identity");ok(living.getMethod("be").invoke(om)==s,"actual original mob slot zero exact identity");}
  ok(stack.getField("d").get(oz)==ot,"original tagged zero identity retained");
  Class<?> item=old.loadClass("adb"),bodies=old.loadClass("OriginalBodies");Object originalItem=u.allocateInstance(item);Field rawItem=stack.getDeclaredField("e");rawItem.setAccessible(true);rawItem.set(os,originalItem);rawItem.set(oz,originalItem);ok(stack.getMethod("b").invoke(oz)==originalItem,"document original zero getItem retains item unlike native AIR");Field max=item.getDeclaredField("b");max.setAccessible(true);max.setInt(originalItem,100);Field damage=stack.getDeclaredField("f");damage.setAccessible(true);
  for(Object value:new Object[]{null,os}) {
   java.lang.reflect.Array.set(slots,0,value);p.inventory.mainInventory.set(0,value==null?ItemStack.EMPTY:tagged);
   boolean original=(Boolean)bodies.getMethod("checkForHeld",player,item).invoke(null,op,originalItem);
   ok(original==ProductionBodies.checkForHeld(p,Items.STICK),"actual original/current complete production Library parity");
   boolean machine=(Boolean)bodies.getMethod("machineGate",player,item).invoke(null,op,originalItem);
   ok(machine==ProductionBodies.machineGate(p,Items.STICK),"actual original/current production machine predicate parity");
   p.inventory.mainInventory.set(0,value==null?ItemStack.EMPTY:equipped);equipped.setItemDamage(0);damage.setInt(os,0);
   boolean weapon=(Boolean)bodies.getMethod("weaponGate",player).invoke(null,op);
   ok(weapon==ProductionBodies.weaponGate(p),"actual original/current production weapon predicate parity");
  }
  damage.setInt(os,100);equipped.setItemDamage(equipped.getMaxDamage());ok((Boolean)bodies.getMethod("weaponGate",player).invoke(null,op)==ProductionBodies.weaponGate(p),"original/current exhausted weapon predicate parity");
  Class<?> customOld=old.loadClass("OriginalCustom");
  for(Object value:new Object[]{null,os,oz}) {Object custom=u.allocateInstance(customOld);customOld.getField("result").set(custom,value);ok(old.loadClass("sv").getMethod("be").invoke(custom)==value&&customOld.getField("calls").getInt(custom)==1,"actual original custom mainhand virtual dispatch once");}
  Object originalCrate=bodies.newInstance();ProductionBodies nativeCrate=new ProductionBodies();
  for(Object value:new Object[]{null,os}) {java.lang.reflect.Array.set(slots,0,value);p.inventory.mainInventory.set(0,value==null?ItemStack.EMPTY:tagged);bodies.getField("target").set(originalCrate,value);nativeCrate.target=value==null?null:tagged;ok((Boolean)bodies.getMethod("isUseableByPlayer",player).invoke(originalCrate,op)==nativeCrate.isUseableByPlayer(p),"actual original/current complete crate identity method parity");}
  old.close();
  System.out.println("PASS "+checks+" actual original/native held-stack, virtual override, empty, offhand exclusion, tagged zero and exact production-method checks");
 }
}
