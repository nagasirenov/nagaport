import java.lang.reflect.*;
import java.util.*;
import net.minecraft.entity.player.*;
import net.minecraft.item.*;
import net.minecraft.init.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import sun.misc.Unsafe;
/** Actual native InventoryPlayer/NonNullList/ItemStack, no replacement platform APIs. */
public final class NativeWriteTest {
 static int checks; static String trace=""; static EntityPlayer player; static ItemStack value; static int slot; static String fail=""; static InventoryPlayer replacement;
 static void ok(boolean x,String why){checks++;if(!x)throw new AssertionError(why);}
 static Unsafe unsafe()throws Exception{Field f=Unsafe.class.getDeclaredField("theUnsafe");f.setAccessible(true);return (Unsafe)f.get(null);}
 static EntityPlayer fresh()throws Exception{EntityPlayer p=(EntityPlayer)unsafe().allocateInstance(EntityPlayerMP.class);p.inventory=new InventoryPlayer(p);return p;}
 static class Fault extends RuntimeException {}
 static EntityPlayer receiver(){trace+="P";if(fail.equals("P"))throw new Fault();return player;}
 static int index(){trace+="I";if(fail.equals("I"))throw new Fault();return slot;}
 static ItemStack rhs(){trace+="R";if(fail.equals("R"))throw new Fault();if(replacement!=null)player.inventory=replacement;return value;}
 static void invoke(){receiver().inventory.mainInventory.set(index(),rhs());}
 static class ObservedList extends NonNullList<ItemStack>{
  ObservedList(){super(new ArrayList<ItemStack>(Collections.nCopies(36,ItemStack.EMPTY)),ItemStack.EMPTY);}
  @Override public ItemStack set(int i,ItemStack s){trace+="S";return super.set(i,s);}
 }
 static void setMain(InventoryPlayer i,NonNullList<ItemStack> list)throws Exception{Field f=InventoryPlayer.class.getDeclaredField("mainInventory");f.setAccessible(true);f.set(i,list);}
 interface Action{void run()throws Exception;}
 static void expect(Class<? extends Throwable> kind,Action action,String why)throws Exception{try{action.run();throw new AssertionError("no exception: "+why);}catch(Throwable t){if(!kind.isInstance(t))throw new AssertionError(why,t);checks++;}}
 static void reset()throws Exception{player=fresh();setMain(player.inventory,new ObservedList());trace="";slot=2;value=new ItemStack(Items.STICK,7);fail="";replacement=null;}
 public static void main(String[] args)throws Exception{
  Bootstrap.register();EntityPlayer p=fresh();InventoryPlayer inv=p.inventory;
  ok(inv.mainInventory.size()==36&&inv.armorInventory.size()==4&&inv.offHandInventory.size()==1,"actual native constructor list dimensions");
  for(ItemStack s:inv.mainInventory)ok(s==ItemStack.EMPTY,"native main initially canonical EMPTY");
  for(ItemStack s:inv.armorInventory)ok(s==ItemStack.EMPTY,"native armor initially canonical EMPTY");
  ItemStack tagged=new ItemStack(Items.STICK,7,3);NBTTagCompound tag=new NBTTagCompound();tag.setString("unknown","retain");tag.setInteger("sentinel",91);tagged.setTagCompound(tag);
  ItemStack zero=new ItemStack(Items.STICK,0),negative=new ItemStack(Items.STICK,-2),air=new ItemStack(Blocks.AIR),large=new ItemStack(Items.STICK,127);
  zero.setTagCompound(tag);negative.setTagCompound(tag);air.setTagCompound(tag);large.setTagCompound(tag);
  ItemStack[] cases={ItemStack.EMPTY,tagged,zero,negative,air,large};ItemStack offhand=new ItemStack(Items.DIAMOND,2);inv.offHandInventory.set(0,offhand);
  for(NonNullList<ItemStack> list:Arrays.asList(inv.mainInventory,inv.armorInventory))for(int i=0;i<list.size();i++)for(ItemStack s:cases){
   int count=s.getCount(),damage=s.getItemDamage();NBTTagCompound before=s.getTagCompound();
   list.set(i,s);ok(list.get(i)==s,"all 36/4 slots retain exact RHS identity, including noncanonical empties");
   ok(s.getCount()==count&&s.getItemDamage()==damage&&s.getTagCompound()==before,"set does not mutate count, damage, or NBT identity");
   list.set(i,ItemStack.EMPTY);ok(list.get(i)==ItemStack.EMPTY,"explicit canonical removal");
   ok(s.getCount()==count&&s.getTagCompound()==before,"removal does not mutate removed alias");
  }
  ok(inv.offHandInventory.get(0)==offhand,"offhand excluded");
  ok(zero!=ItemStack.EMPTY&&zero.isEmpty()&&negative!=ItemStack.EMPTY&&negative.isEmpty(),"malformed noncanonical empty fixtures are genuine native values");
  for(final NonNullList<ItemStack> list:Arrays.asList(inv.mainInventory,inv.armorInventory)){
   expect(NullPointerException.class,()->list.set(0,null),"native rejects null");
   expect(IndexOutOfBoundsException.class,()->list.set(-1,tagged),"negative index");
   expect(IndexOutOfBoundsException.class,()->list.set(list.size(),ItemStack.EMPTY),"upper index even for removal");
   expect(NullPointerException.class,()->list.set(-1,null),"native null validation precedes bounds; not generalized to nullable RHS");
  }
  // Twenty exact post-edit production statements, with only data collaborators supplied.
  ProductionWrites bodies=new ProductionWrites(); ProductionWrites.Holder pedestal=new ProductionWrites.Holder();pedestal.item=tagged;p.inventory.currentItem=6;
  Field blockMap=Item.class.getDeclaredField("BLOCK_TO_ITEM");blockMap.setAccessible(true);Map map=(Map)blockMap.get(null);map.put(bodies,Items.STICK);
  for(int n=0;n<ProductionWrites.COUNT;n++){
   for(int i=0;i<36;i++)p.inventory.mainInventory.set(i,tagged);for(int i=0;i<4;i++)p.inventory.armorInventory.set(i,tagged);
   bodies.write(n,p,2,3,pedestal);
   NonNullList<ItemStack> list=ProductionWrites.ARMOR[n]?p.inventory.armorInventory:p.inventory.mainInventory;int at=ProductionWrites.INDEX[n];ItemStack result=list.get(at);
   if(ProductionWrites.KIND[n].equals("NULL_LITERAL"))ok(result==ItemStack.EMPTY,"exact production removal statement "+n);
   else if(ProductionWrites.KIND[n].equals("METHOD_INVOCATION"))ok(result!=tagged&&result.getTagCompound()!=tag&&result.getTagCompound().equals(tag)&&result.getCount()==7&&result.getItemDamage()==3,"exact production native-copy statement "+n);
   else ok(result!=tagged&&result.getItem()==Items.STICK&&result.getCount()==1,"exact production native-constructor statement "+n);
   for(int i=0;i<list.size();i++)if(i!=at)ok(list.get(i)==tagged,"production statement leaves neighboring aliases untouched");
  }
  map.remove(bodies);
  // ItemFolderPacket's existing stackSize access cannot compile natively yet. Its exact set expression template uses an explicit native RHS argument, not a quantity migration.
  ProductionWrites.folderBoundary(p,8,tagged);ok(p.inventory.mainInventory.get(8)==tagged&&tagged.getTagCompound()==tag&&tagged.getCount()==7,"packet write boundary preserves RHS alias without claiming quantity/handler compilation");
  pedestal.item=zero; bodies.copyBoundary(p,pedestal);ItemStack zeroCopy=p.inventory.mainInventory.get(6);ok(zeroCopy!=ItemStack.EMPTY&&zeroCopy!=zero&&zeroCopy.isEmpty()&&zeroCopy.getCount()==zero.getCount()&&zeroCopy.getTagCompound()!=tag&&zeroCopy.getTagCompound().equals(tag),"actual Forge copy preserves a new noncanonical tagged zero-count object; write must not normalize it");
  reset();invoke();ok(trace.equals("PIRS")&&player.inventory.mainInventory.get(2)==value,"receiver then index then RHS then set exactly once");
  reset();InventoryPlayer original=player.inventory;replacement=new InventoryPlayer(player);invoke();ok(original.mainInventory.get(2)==value&&replacement.mainInventory.get(2)==ItemStack.EMPTY&&trace.equals("PIRS"),"list field receiver captured before RHS mutates inventory");
  reset();player=null;expect(NullPointerException.class,()->invoke(),"null player");ok(trace.equals("P"),"null player throws before index and RHS");
  reset();player.inventory=null;expect(NullPointerException.class,()->invoke(),"null inventory");ok(trace.equals("P"),"null inventory throws before index and RHS");
  reset();setMain(player.inventory,null);expect(NullPointerException.class,()->invoke(),"null list");ok(trace.equals("PIR"),"null list invocation throws only after index and RHS");
  for(String failure:new String[]{"P","I","R"}){reset();fail=failure;expect(Fault.class,()->invoke(),"evaluation exception "+failure);ok(trace.equals(failure.equals("P")?"P":failure.equals("I")?"PI":"PIR"),"exception prevents subsequent evaluation and write");}
  reset();slot=36;expect(IndexOutOfBoundsException.class,()->invoke(),"bad index");ok(trace.equals("PIRS"),"bounds checked in set after RHS");
  reset();slot=-1;fail="R";expect(Fault.class,()->invoke(),"RHS precedes bounds");ok(trace.equals("PIR"),"RHS failure suppresses invalid index error");
  reset();setMain(player.inventory,null);fail="R";expect(Fault.class,()->invoke(),"RHS precedes null-list invocation");ok(trace.equals("PIR"),"RHS failure suppresses null-list error");
  // The actual Java array assignment has the same relevant evaluation cut points.
  fail="";trace="";ItemStack[] none=null;expect(NullPointerException.class,()->{none[index()]=rhs();},"null array");ok(trace.equals("IR"),"array null check also occurs after index/RHS");
  fail="";trace="";ItemStack[] arr=new ItemStack[36];slot=36;expect(IndexOutOfBoundsException.class,()->{arr[index()]=rhs();},"array index");ok(trace.equals("IR"),"array bounds check also occurs after RHS");
  System.out.println("PASS "+checks+" actual native InventoryPlayer write, removal, identity/NBT/count, constructor/copy statement, and evaluation/exception checks");
 }
}
