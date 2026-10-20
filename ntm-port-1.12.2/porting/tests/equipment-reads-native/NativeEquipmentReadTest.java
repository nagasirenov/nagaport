import java.io.File;
import java.lang.reflect.*;
import java.net.*;
import java.util.*;
import sun.misc.Unsafe;
import com.hbm.util.LivingEquipmentRead;
import net.minecraft.entity.*;
import net.minecraft.entity.player.*;
import net.minecraft.entity.monster.*;
import net.minecraft.entity.passive.EntityCow;
import net.minecraft.init.*;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingEvent;
/** Uses actual engine classes. Unsafe skips unrelated world/network construction only. */
public final class NativeEquipmentReadTest {
 static int checks;
 static void ok(boolean value,String why){checks++;if(!value)throw new AssertionError(why);}
 static Unsafe unsafe()throws Exception{Field f=Unsafe.class.getDeclaredField("theUnsafe");f.setAccessible(true);return (Unsafe)f.get(null);}
 static final EntityEquipmentSlot[] SLOTS={EntityEquipmentSlot.MAINHAND,EntityEquipmentSlot.FEET,EntityEquipmentSlot.LEGS,EntityEquipmentSlot.CHEST,EntityEquipmentSlot.HEAD};
 static void lists(EntityLiving mob)throws Exception {
  Field h=EntityLiving.class.getDeclaredField("inventoryHands"),a=EntityLiving.class.getDeclaredField("inventoryArmor");h.setAccessible(true);a.setAccessible(true);
  h.set(mob,NonNullList.withSize(2,ItemStack.EMPTY));a.set(mob,NonNullList.withSize(4,ItemStack.EMPTY));
 }
 static void set(EntityLiving mob,ItemStack[] slots)throws Exception {
  Field h=EntityLiving.class.getDeclaredField("inventoryHands"),a=EntityLiving.class.getDeclaredField("inventoryArmor");h.setAccessible(true);a.setAccessible(true);
  NonNullList<ItemStack> hands=(NonNullList<ItemStack>)h.get(mob),armor=(NonNullList<ItemStack>)a.get(mob);
  hands.set(0,slots[0]);hands.set(1,new ItemStack(Items.DIAMOND));for(int i=0;i<4;i++)armor.set(i,slots[i+1]);
 }
 static void set(EntityPlayer p,ItemStack[] slots) {p.inventory.currentItem=3;p.inventory.mainInventory.set(3,slots[0]);p.inventory.offHandInventory.set(0,new ItemStack(Items.DIAMOND));for(int i=0;i<4;i++)p.inventory.armorInventory.set(i,slots[i+1]);}
 static ItemStack nullable(ItemStack s){return s==ItemStack.EMPTY?null:s;}
 public static final class CustomMob extends EntityZombie {
  ItemStack[] results;int calls;List<EntityEquipmentSlot> slots=new ArrayList<>();
  public CustomMob(){super((World)null);}
  @Override public ItemStack getItemStackFromSlot(EntityEquipmentSlot slot){slots.add(slot);return results[calls++];}
 }
 public static final class CustomPlayer extends EntityPlayerMP {
  int equipmentCalls;
  public CustomPlayer(){super(null,null,null,null);}
  @Override public ItemStack getItemStackFromSlot(EntityEquipmentSlot slot){equipmentCalls++;throw new AssertionError("armor must bypass equipment dispatch");}
 }
 public static final class CustomInventory extends InventoryPlayer {
  ItemStack result;int calls,last;
  CustomInventory(EntityPlayer p){super(p);}
  @Override public ItemStack armorItemInSlot(int slot){calls++;last=slot;return result;}
 }
 static CustomMob custom(Unsafe u,ItemStack... results)throws Exception{CustomMob m=(CustomMob)u.allocateInstance(CustomMob.class);m.results=results;m.slots=new ArrayList<>();return m;}
 static String trace="";
 static EntityLivingBase receiver(EntityLivingBase e){trace+="R";return e;}
 static EntityPlayer playerReceiver(EntityPlayer p){trace+="R";return p;}
 static int index(int i,boolean fail){trace+="I";if(fail)throw new IllegalStateException("index");return i;}
 interface Op {Object run()throws Exception;}
 static Throwable failure(Op op)throws Exception{try{op.run();throw new AssertionError("expected exception");}catch(InvocationTargetException e){return e.getCause();}catch(RuntimeException e){return e;}}
 static Object field(Class<?> type,String name,Object owner)throws Exception{Field f=type.getDeclaredField(name);f.setAccessible(true);return f.get(owner);}
 public static void main(String[] args)throws Exception {
  Bootstrap.register();Unsafe u=unsafe();EntityPlayerMP p=(EntityPlayerMP)u.allocateInstance(EntityPlayerMP.class);p.inventory=new InventoryPlayer(p);
  ProductionExpressions expressions=(ProductionExpressions)u.allocateInstance(ProductionExpressions.class);lists(expressions);
  ItemStack tagged=new ItemStack(Items.STICK,3),zero=new ItemStack(Items.STICK,0),negative=new ItemStack(Items.STICK,-1),air=new ItemStack(Blocks.AIR),noItem=new ItemStack((Item)null),badNbt=new ItemStack(new NBTTagCompound());
  NBTTagCompound tag=new NBTTagCompound();tag.setString("unknown","retain");tag.setInteger("test",41);tagged.setTagCompound(tag);zero.setTagCompound(tag);negative.setTagCompound(tag);badNbt.setTagCompound(tag);
  ItemStack[] values={ItemStack.EMPTY,new ItemStack(Items.IRON_HELMET),tagged,zero,negative,air,noItem,badNbt};
  Class<?>[] concrete={EntityZombie.class,EntitySkeleton.class,EntityCow.class,ProductionExpressions.class};
  for(Class<?> type:concrete){EntityLiving mob=(EntityLiving)u.allocateInstance(type);lists(mob);
   for(int shift=0;shift<values.length;shift++){ItemStack[] slots=new ItemStack[5];for(int i=0;i<5;i++)slots[i]=values[(i+shift)%values.length];set(mob,slots);set(p,slots);
    for(int i=0;i<5;i++){ok(LivingEquipmentRead.getEquipmentNullable(mob,i)==nullable(slots[i]),"native concrete mob slot/alias "+type+" "+i);ok(LivingEquipmentRead.getEquipmentNullable(p,i)==nullable(slots[i]),"native player slot/alias "+i);}
    for(int i=0;i<4;i++)ok(LivingEquipmentRead.getArmorNullable(p,i)==nullable(slots[i+1]),"native player armor index "+i);
   }
  }
  for(ItemStack s:values)for(int i=0;i<5;i++){CustomMob m=custom(u,s);ok(LivingEquipmentRead.getEquipmentNullable(m,i)==nullable(s)&&m.calls==1&&m.slots.get(0)==SLOTS[i],"native virtual slot dispatch once, full alias set");}
  CustomMob nullResult=custom(u,(ItemStack)null);ok(LivingEquipmentRead.getEquipmentNullable(nullResult,4)==null&&nullResult.calls==1,"custom null result preserved");
  CustomPlayer special=(CustomPlayer)u.allocateInstance(CustomPlayer.class);CustomInventory inventory=new CustomInventory(special);special.inventory=inventory;
  for(ItemStack s:values)for(int i:new int[]{Integer.MIN_VALUE,-1,0,3,4,37,Integer.MAX_VALUE}){inventory.result=s;int before=inventory.calls;ok(LivingEquipmentRead.getArmorNullable(special,i)==nullable(s)&&inventory.calls==before+1&&inventory.last==i&&special.equipmentCalls==0,"distinct virtual armor inventory hook, no new bounds clamp");}
  for(final int i:new int[]{Integer.MIN_VALUE,-1,5,Integer.MAX_VALUE}) {
   ok(failure(()->LivingEquipmentRead.getEquipmentNullable(null,i)) instanceof NullPointerException,"null receiver wins over invalid mapping");
   CustomMob m=custom(u,tagged);ok(failure(()->LivingEquipmentRead.getEquipmentNullable(m,i)) instanceof ArrayIndexOutOfBoundsException&&m.calls==0,"invalid equipment slot fails before virtual read");
  }
  trace="";ok(failure(()->LivingEquipmentRead.getEquipmentNullable(receiver(null),index(5,true))) instanceof IllegalStateException&&trace.equals("RI"),"throwing index precedes receiver null check");
  trace="";ok(failure(()->LivingEquipmentRead.getEquipmentNullable(receiver(null),index(5,false))) instanceof NullPointerException&&trace.equals("RI"),"receiver and index evaluated once before null check");
  trace="";ok(failure(()->LivingEquipmentRead.getArmorNullable(playerReceiver(null),index(0,true))) instanceof IllegalStateException&&trace.equals("RI"),"armor receiver/index evaluation");
  trace="";ok(failure(()->LivingEquipmentRead.getArmorNullable(playerReceiver(null),index(-1,false))) instanceof NullPointerException&&trace.equals("RI"),"armor null wins over bounds after index evaluation");
  ok(zero.isEmpty()&&zero.getTagCompound()==tag&&negative.getTagCompound()==tag,"test malformed identity/payload premise");
  ok(zero.getItem()==Item.getItemFromBlock(Blocks.AIR),"native getItem malformed policy intentionally not repaired");
  // Complete production predicates and hazard traversal, with only applyHazards instrumented.
  ItemStack helmet=new ItemStack(Items.IRON_HELMET),chest=new ItemStack(Items.IRON_CHESTPLATE),legs=new ItemStack(Items.IRON_LEGGINGS),feet=new ItemStack(Items.IRON_BOOTS);
  ItemStack[] full={tagged,feet,legs,chest,helmet};set(expressions,full);set(p,full);
  ok(ProductionExpressions.checkArmor(p,Items.IRON_HELMET,Items.IRON_CHESTPLATE,Items.IRON_LEGGINGS,Items.IRON_BOOTS),"exact full checkArmor calls complete piece/null predicates");
  CustomMob once=custom(u,ItemStack.EMPTY);ok(!ProductionExpressions.checkArmorPiece(once,Items.STICK,3)&&once.calls==1,"complete piece predicate empty short circuit");
  CustomMob twice=custom(u,tagged,helmet);ok(!ProductionExpressions.checkArmorPiece(twice,Items.STICK,3)&&twice.calls==2,"complete piece predicate retains repeated reads");
  CustomMob disappearing=custom(u,tagged,ItemStack.EMPTY);ok(failure(()->ProductionExpressions.checkArmorPiece(disappearing,Items.STICK,3)) instanceof NullPointerException&&disappearing.calls==2,"second read becomes vacant: original dereference semantics");
  CustomMob worn=custom(u,helmet,helmet,chest,chest,legs,legs,feet,feet);Item[] wornItems=ProductionExpressions.worn(worn);ok(Arrays.equals(wornItems,new Item[]{Items.IRON_HELMET,Items.IRON_CHESTPLATE,Items.IRON_LEGGINGS,Items.IRON_BOOTS})&&worn.calls==8,"complete production worn-set expression, eight reads");ok(worn.slots.equals(Arrays.asList(SLOTS[4],SLOTS[4],SLOTS[3],SLOTS[3],SLOTS[2],SLOTS[2],SLOTS[1],SLOTS[1])),"worn-set exact short-circuit dispatch order");
  CustomMob emptyWorn=custom(u,ItemStack.EMPTY,ItemStack.EMPTY,ItemStack.EMPTY,ItemStack.EMPTY);ok(Arrays.equals(ProductionExpressions.worn(emptyWorn),new Item[4])&&emptyWorn.calls==4,"empty worn-set reads once per slot");
  CustomMob hazard=custom(u,ItemStack.EMPTY,zero,negative,tagged,helmet);ProductionExpressions.seen.clear();ProductionExpressions.owners.clear();ProductionExpressions.updateLivingInventory(hazard);ok(ProductionExpressions.seen.equals(Arrays.asList(zero,negative,tagged,helmet))&&hazard.calls==5&&hazard.slots.equals(Arrays.asList(SLOTS)),"complete hazard traversal: only canonical empty excluded; mainhand plus armor, no offhand");for(EntityLivingBase owner:ProductionExpressions.owners)ok(owner==hazard,"unchanged hazard receiver identity");
  // Actual 1.7.10 classes loaded in a separate namespace; reads are unchanged by Forge1614.
  URLClassLoader old=new URLClassLoader(new URL[]{new File(args[0]).toURI().toURL(),new File(args[1]).toURI().toURL()},NativeEquipmentReadTest.class.getClassLoader());
  Class<?> oldBase=old.loadClass("sv"),oldPlayer=old.loadClass("yz"),oldMob=old.loadClass("sw"),oldStack=old.loadClass("add"),oldInv=old.loadClass("yx"),oldItem=old.loadClass("adb"),oldExpressions=old.loadClass("OriginalExpressions"),oldEvent=old.loadClass("OriginalEvent");
  Object op=u.allocateInstance(old.loadClass("mw")),oi=oldInv.getConstructor(oldPlayer).newInstance(op),om=u.allocateInstance(oldExpressions);oldPlayer.getField("bm").set(op,oi);
  Field oldEquipment=oldMob.getDeclaredField("br");oldEquipment.setAccessible(true);Object oldSlots=Array.newInstance(oldStack,5);oldEquipment.set(om,oldSlots);
  Object main=oldInv.getField("a").get(oi),armor=oldInv.getField("b").get(oi);oldInv.getField("c").setInt(oi,3);
  Object[] oldValues=new Object[values.length];for(int i=1;i<oldValues.length;i++){oldValues[i]=u.allocateInstance(oldStack);oldStack.getField("b").setInt(oldValues[i],values[i].getCount());}
  Object originalItem=u.allocateInstance(oldItem);Field rawItem=oldStack.getDeclaredField("e");rawItem.setAccessible(true);for(int i=1;i<oldValues.length;i++)rawItem.set(oldValues[i],originalItem);
  Constructor<?> eventConstructor=oldEvent.getDeclaredConstructor(oldBase);eventConstructor.setAccessible(true);Object oe=eventConstructor.newInstance(om);
  Method expressionRead=oldExpressions.getMethod("read",int.class,oldBase,oldBase,oldBase,oldPlayer,oldPlayer,oldEvent,int.class,int.class);
  LivingEvent event=new LivingEvent(expressions);
  for(int shift=0;shift<values.length;shift++) {
   ItemStack[] current=new ItemStack[5];Object[] original=new Object[5];for(int i=0;i<5;i++){int at=(i+shift)%values.length;current[i]=values[at];original[i]=oldValues[at];Array.set(oldSlots,i,original[i]);if(i==0)Array.set(main,3,original[i]);else Array.set(armor,i-1,original[i]);}set(expressions,current);set(p,current);
   for(int i=0;i<5;i++){ok(oldBase.getMethod("q",int.class).invoke(om,i)==original[i],"actual original mob slot alias");ok(oldBase.getMethod("q",int.class).invoke(op,i)==original[i],"actual original player slot alias");}
   for(int i=0;i<4;i++)ok(oldPlayer.getMethod("r",int.class).invoke(op,i)==original[i+1],"actual original armor inventory mapping");
   for(int index=0;index<4;index++)for(int which=0;which<ProductionExpressions.COUNT;which++){
    ItemStack result=expressions.read(which,expressions,expressions,expressions,p,p,event,index,index);Object before=expressionRead.invoke(om,which,om,om,om,op,op,oe,index,index);
    int at=-1;for(int i=0;i<5;i++)if(before==original[i])at=i;ok(at>=0&&result==nullable(current[at]),"every exact selected original/native expression alias at "+which+" index "+index);
   }
   ProductionExpressions.seen.clear();ProductionExpressions.owners.clear();ProductionExpressions.updateLivingInventory(expressions);((List<?>)field(oldExpressions,"seen",null)).clear();((List<?>)field(oldExpressions,"owners",null)).clear();oldExpressions.getMethod("updateLivingInventory",oldBase).invoke(null,om);
   List<?> beforeSeen=(List<?>)field(oldExpressions,"seen",null);ok(beforeSeen.size()==ProductionExpressions.seen.size(),"actual original/native complete hazard traversal size");int at=0;for(int i=0;i<5;i++)if(original[i]!=null){ok(beforeSeen.get(at)==original[i]&&ProductionExpressions.seen.get(at)==current[i],"actual original/native complete hazard traversal alias/order");at++;}
  }
  for(final int index:new int[]{Integer.MIN_VALUE,-1,5,Integer.MAX_VALUE}) {
   ok(failure(()->oldBase.getMethod("q",int.class).invoke(om,index)) instanceof ArrayIndexOutOfBoundsException,"actual original mob invalid equipment bounds");
   ok(failure(()->oldBase.getMethod("q",int.class).invoke(op,index)) instanceof ArrayIndexOutOfBoundsException,"actual original player invalid equipment bounds");
  }
  for(final int index:new int[]{Integer.MIN_VALUE,-1,4,Integer.MAX_VALUE}) {
   Throwable before=failure(()->oldPlayer.getMethod("r",int.class).invoke(op,index)),after=failure(()->LivingEquipmentRead.getArmorNullable(p,index));ok(before instanceof IndexOutOfBoundsException&&after instanceof IndexOutOfBoundsException,"actual original/native armor invalid index policy");
  }
  Class<?> oldCustomInv=old.loadClass("OriginalInventory");Object customInv=oldCustomInv.getConstructor(oldPlayer).newInstance(op);oldPlayer.getField("bm").set(op,customInv);oldCustomInv.getField("result").set(customInv,oldValues[3]);inventory.result=zero;
  for(int index:new int[]{Integer.MIN_VALUE,-1,0,3,4,37,Integer.MAX_VALUE}) {int before=oldCustomInv.getField("calls").getInt(customInv);ok(oldPlayer.getMethod("r",int.class).invoke(op,index)==oldValues[3]&&oldCustomInv.getField("calls").getInt(customInv)==before+1&&oldCustomInv.getField("last").getInt(customInv)==index,"actual original virtual armor inventory accepts extended index");ok(LivingEquipmentRead.getArmorNullable(special,index)==zero,"matching native custom armor index remains valid");}
  Class<?> oldCustom=old.loadClass("OriginalCustom");Object customOld=u.allocateInstance(oldCustom);Object results=Array.newInstance(oldStack,1);Array.set(results,0,oldValues[3]);oldCustom.getField("result").set(customOld,results);
  for(int index=0;index<5;index++){oldCustom.getField("calls").setInt(customOld,0);ok(oldBase.getMethod("q",int.class).invoke(customOld,index)==oldValues[3]&&oldCustom.getField("calls").getInt(customOld)==1&&oldCustom.getField("last").getInt(customOld)==index,"actual original virtual equipment read remains alias");}
  old.close();
  System.out.println("PASS "+checks+" actual original/native equipment read assertions, all 44 complete expressions, distinct virtual hooks, receiver/index order, canonical-only vacancy, complete armor predicates/worn-set expression/hazard traversal");
 }
}
