import java.lang.reflect.*;
import java.net.*;
import net.minecraft.entity.*;
import net.minecraft.entity.monster.*;
import net.minecraft.entity.player.*;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.init.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import sun.misc.Unsafe;
public class NativeEquipmentWriteTest {
 static int checks; static void ok(boolean v,String s){checks++;if(!v)throw new AssertionError(s);}
 static Unsafe unsafe() throws Exception {Field f=Unsafe.class.getDeclaredField("theUnsafe");f.setAccessible(true);return (Unsafe)f.get(null);}
 static void field(Class<?> c,Object x,String n,Object v)throws Exception {Field f=c.getDeclaredField(n);f.setAccessible(true);f.set(x,v);}
 public static class TrackingZombie extends EntityZombie {public int calls;public EntityEquipmentSlot slot;public ItemStack item;public TrackingZombie(){super((World)null);} public void setItemStackToSlot(EntityEquipmentSlot s,ItemStack i){calls++;slot=s;item=i;super.setItemStackToSlot(s,i);}}
 public static class SoundPlayer extends EntityPlayerMP {public int sounds;public SoundPlayer(){super(null,null,null,null);} @Override protected void playEquipSound(ItemStack s){sounds++;super.playEquipSound(s);}}
 public static void main(String[] args)throws Exception {
  Bootstrap.register();Unsafe u=unsafe();
  EntityEquipmentSlot[] slots={EntityEquipmentSlot.MAINHAND,EntityEquipmentSlot.FEET,EntityEquipmentSlot.LEGS,EntityEquipmentSlot.CHEST,EntityEquipmentSlot.HEAD};
  URLClassLoader old=new URLClassLoader(new URL[]{new java.io.File(args[0]).toURI().toURL(),new java.io.File(args[1]).toURI().toURL()},NativeEquipmentWriteTest.class.getClassLoader());
  Class<?> living=old.loadClass("sw"),stack=old.loadClass("add");Object mob=u.allocateInstance(old.loadClass("yq"));Object oa=Array.newInstance(stack,5);field(living,mob,"br",oa);float[] od=new float[5];field(living,mob,"e",od);
  EntityZombie zombie=(EntityZombie)u.allocateInstance(EntityZombie.class);NonNullList<ItemStack> hands=NonNullList.withSize(2,ItemStack.EMPTY),armor=NonNullList.withSize(4,ItemStack.EMPTY);field(EntityLiving.class,zombie,"inventoryHands",hands);field(EntityLiving.class,zombie,"inventoryArmor",armor);float[] hd=new float[2],ad=new float[4];field(EntityLiving.class,zombie,"inventoryHandsDropChances",hd);field(EntityLiving.class,zombie,"inventoryArmorDropChances",ad);
  ItemStack offhand=new ItemStack(Items.DIAMOND);hands.set(1,offhand);hd[1]=0.33F;
  ItemStack normal=new ItemStack(Items.STICK,3),zero=new ItemStack(Items.STICK,0),negative=new ItemStack(Items.STICK,-4);NBTTagCompound tag=new NBTTagCompound();tag.setString("unknown","retain");zero.setTagCompound(tag);
  Object os=u.allocateInstance(stack);Object oz=u.allocateInstance(stack);stack.getField("b").setInt(oz,0);Object oldTag=old.loadClass("dh").newInstance();stack.getField("d").set(oz,oldTag);
  for(int slot=0;slot<5;slot++)for(ItemStack s:new ItemStack[]{normal,zero,negative,new ItemStack(Blocks.AIR),ItemStack.EMPTY}){
    Object original=s==ItemStack.EMPTY?null:s==zero?oz:os;if(original!=null)stack.getField("b").setInt(original,s.getCount());living.getMethod("c",int.class,stack).invoke(mob,slot,original);zombie.setItemStackToSlot(slots[slot],s);
    ok(Array.get(oa,slot)==original,"old concrete setter identity");ok(zombie.getItemStackFromSlot(slots[slot])==s,"native concrete setter identity");ok(hands.get(1)==offhand,"native offhand excluded");if(original!=null)ok(stack.getField("b").getInt(original)==s.getCount(),"old/native count unchanged");
  }
  ok(zero.getTagCompound()==tag&&zero.getCount()==0,"tag/count retained");ok(stack.getField("d").get(oz)==oldTag,"original tagged-zero payload retained");
  for(int slot=0;slot<5;slot++)for(float f:new float[]{0,1,-1,-0.0F,Float.POSITIVE_INFINITY,Float.NEGATIVE_INFINITY,Float.intBitsToFloat(0x7fc01234)}){
    living.getMethod("a",int.class,float.class).invoke(mob,slot,f);zombie.setDropChance(slots[slot],f);float actual=slot==0?hd[0]:ad[slot-1];ok(Float.floatToRawIntBits(actual)==Float.floatToRawIntBits(od[slot]),"old/native exact float drop bits");ok(hd[1]==0.33F,"drop offhand excluded");
  }
  TrackingZombie custom=(TrackingZombie)u.allocateInstance(TrackingZombie.class);field(EntityLiving.class,custom,"inventoryHands",NonNullList.withSize(2,ItemStack.EMPTY));field(EntityLiving.class,custom,"inventoryArmor",NonNullList.withSize(4,ItemStack.EMPTY));
  EntityLivingBase base=custom;for(int slot=0;slot<5;slot++){base.setItemStackToSlot(slots[slot],zero);ok(custom.calls==slot+1&&custom.slot==slots[slot]&&custom.item==zero,"native virtual dispatch retained");}
  Class<?> originalTracking=old.loadClass("OriginalWriteTracking");Object customOld=u.allocateInstance(originalTracking);field(living,customOld,"br",Array.newInstance(stack,5));
  for(int slot=0;slot<5;slot++){living.getMethod("c",int.class,stack).invoke(customOld,slot,oz);ok(originalTracking.getField("calls").getInt(customOld)==slot+1&&originalTracking.getField("slot").getInt(customOld)==slot&&originalTracking.getField("stack").get(customOld)==oz,"original concrete-mob virtual dispatch retained");}
  EntityPlayerMP player=(EntityPlayerMP)u.allocateInstance(EntityPlayerMP.class);player.inventory=new InventoryPlayer(player);for(int slot=0;slot<5;slot++){player.setItemStackToSlot(slots[slot],ItemStack.EMPTY);ok(player.getItemStackFromSlot(slots[slot])==ItemStack.EMPTY,"native empty player removal no world/sound access");}
  SoundPlayer observed=(SoundPlayer)u.allocateInstance(SoundPlayer.class);observed.inventory=new InventoryPlayer(observed);observed.setItemStackToSlot(EntityEquipmentSlot.HEAD,ItemStack.EMPTY);ok(observed.sounds==1,"native protected sound override runs even for EMPTY");
  ProductionWrites production=(ProductionWrites)u.allocateInstance(ProductionWrites.class);
  field(EntityLiving.class,production,"inventoryHands",NonNullList.withSize(2,ItemStack.EMPTY));field(EntityLiving.class,production,"inventoryArmor",NonNullList.withSize(4,ItemStack.EMPTY));
  field(EntityLiving.class,production,"inventoryHandsDropChances",new float[2]);field(EntityLiving.class,production,"inventoryArmorDropChances",new float[4]);
  ItemStack untouchedOffhand=new ItemStack(Items.DIAMOND);production.setItemStackToSlot(EntityEquipmentSlot.OFFHAND,untouchedOffhand);
  for(int which=0;which<ProductionWrites.COUNT;which++) {
   int beforeSets=production.setterCalls,beforeDrops=production.dropCalls;production.write(which,production,production);
   ok(production.lastSlot==slots[ProductionWrites.SLOT[which]],"complete production statement correct slot");
   if(ProductionWrites.DROP[which]){
    ok(production.dropCalls==beforeDrops+1&&production.setterCalls==beforeSets,"complete drop statement one virtual call");
    ok(Float.floatToRawIntBits(production.lastChance)==Float.floatToRawIntBits(ProductionWrites.expectedChance(which)),"complete unchanged drop argument");
   } else {
    ok(production.setterCalls==beforeSets+1&&production.dropCalls==beforeDrops,"complete setter statement one virtual call");
    ok(production.lastStack.getItem()==ProductionWrites.expectedItem(which),"complete unchanged constructor item RHS");
    ok(production.lastStack.getCount()==1&&production.lastStack.getItemDamage()==0&&production.lastStack.getTagCompound()==null,"complete RHS default count damage NBT");
    ok(production.getItemStackFromSlot(production.lastSlot)==production.lastStack,"complete RHS object stored by identity");
   }
   ok(production.getItemStackFromSlot(EntityEquipmentSlot.OFFHAND)==untouchedOffhand,"complete statement offhand excluded");
  }
  old.close();System.out.println("PASS "+checks+" actual original/native concrete zombie setter aliases, zero/tag payload, exact drop float bits, virtual dispatch, offhand exclusion and player EMPTY sound-hook checks");
 }
}
