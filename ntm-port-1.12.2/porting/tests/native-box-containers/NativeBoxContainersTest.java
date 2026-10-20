import com.hbm.items.*;import com.hbm.items.tool.*;import com.hbm.inventory.*;import com.hbm.inventory.container.*;
import net.minecraft.entity.player.*;import net.minecraft.inventory.*;import net.minecraft.item.*;import net.minecraft.init.*;import net.minecraft.nbt.*;import net.minecraft.world.*;import net.minecraft.util.*;
import java.util.*;import java.lang.reflect.*;
public class NativeBoxContainersTest {
 static int tests;static void eq(boolean v,String s){tests++;if(!v)throw new AssertionError(s);}
 static final Field COUNT,ITEM;static{try{COUNT=net.minecraftforge.fml.relauncher.ReflectionHelper.findField(ItemStack.class,"stackSize","field_77994_a");ITEM=ItemStack.class.getDeclaredField("item");ITEM.setAccessible(true);}catch(Exception e){throw new RuntimeException(e);}}
 public static int raw(ItemStack s){try{return COUNT.getInt(s);}catch(Exception e){throw new RuntimeException(e);}}
 public static Item rawItem(ItemStack s){try{return (Item)ITEM.get(s);}catch(Exception e){throw new RuntimeException(e);}}
 public static int rawDamage(ItemStack s){return rawItem(s).getDamage(s);}
 public static ItemStack nullable(Slot s){ItemStack v=s.getStack();return v==ItemStack.EMPTY?null:v;}
 public static boolean rawTagsEqual(ItemStack a,ItemStack b){return a.getTagCompound()==null?b.getTagCompound()==null:a.getTagCompound().equals(b.getTagCompound());}
 public static ItemStack originalCopy(ItemStack s,int n){if(s==null)return null;ItemStack c=s.copy();c.setCount(n);return c;}
 static ItemStack stack(Item item,int n,int damage,String tag){ItemStack s=new ItemStack(item,n,damage);if(tag!=null){NBTTagCompound t=new NBTTagCompound();t.setString("payload",tag);s.setTagCompound(t);}return s;}
 static ItemStack stack(int n){return stack(Items.DIAMOND,n,0,null);}
 static String payload(ItemStack s){return s==null?"null":s==ItemStack.EMPTY?"EMPTY":s.writeToNBT(new NBTTagCompound()).toString()+"/raw="+raw(s);}
 static class Inv extends ItemInventory {final int limit;final List<String> log=new ArrayList<>();Inv(int size,int limit){this.limit=limit;slots=new ItemStack[size];target=new ItemStack(Items.CHEST_MINECART);}public int getSizeInventory(){return slots.length;}public int getInventoryStackLimit(){return limit;}public String getName(){return "test";}public boolean hasCustomName(){return false;}public void markDirty(){super.markDirty();log.add("dirty:"+Arrays.toString(Arrays.stream(slots).map(NativeBoxContainersTest::payload).toArray()));}}
 static class TraceSlot extends SlotNonRetarded {final Inv inv;final boolean allow;TraceSlot(Inv i,int n,boolean allow){super(i,n,0,0);inv=i;this.allow=allow;this.slotNumber=n;}public ItemStack getStack(){inv.log.add("read:"+getSlotIndex());return super.getStack();}public boolean isItemValid(ItemStack s){inv.log.add("valid:"+getSlotIndex()+":"+payload(s));return allow&&super.isItemValid(s);}public void putStack(ItemStack s){inv.log.add("put:"+getSlotIndex()+":"+payload(s));super.putStack(s);}public void onSlotChanged(){inv.log.add("changed:"+getSlotIndex());super.onSlotChanged();}}
 static void compare(int n,int count,int sourceCount,boolean reverse,int limit,int kind,boolean alias,boolean allow){
  Inv a=new Inv(3,limit),b=new Inv(3,limit);Item item=kind==1?Items.GOLD_INGOT:Items.DIAMOND;int damage=kind==2?70000:0;String tag=kind==3?"different":null;
  a.slots[n]=stack(item,count,damage,tag);b.slots[n]=a.slots[n].copy();ItemStack oldA=a.slots[n],oldB=b.slots[n];ItemStack sa=alias?oldA:stack(sourceCount),sb=alias?oldB:sa.copy();
  List<Slot> as=new ArrayList<>(),bs=new ArrayList<>();for(int k=0;k<3;k++){as.add(new TraceSlot(a,k,allow));bs.add(new TraceSlot(b,k,allow));}
  boolean x=ItemInventoryTransfer.mergeItemStack(as,sa,0,3,reverse),y=OriginalMergeOracle.mergeItemStack(bs,sb,0,3,reverse);
  String key="matrix n="+n+" count="+count+" source="+sourceCount+" rev="+reverse+" limit="+limit+" kind="+kind+" alias="+alias+" allow="+allow;
  eq(x==y,key+" success");eq(payload(sa).equals(payload(sb)),key+" source");eq(a.log.equals(b.log),key+" callback order\n"+a.log+"\n"+b.log);
  for(int k=0;k<3;k++)eq(payload(a.slots[k]).equals(payload(b.slots[k])),key+" slot "+k);
  eq((a.slots[n]==oldA)==(b.slots[n]==oldB),key+" original destination alias");eq((a.slots[n]==sa)==(b.slots[n]==sb),key+" source alias");eq(a.target.getTagCompound()==null?b.target.getTagCompound()==null:a.target.getTagCompound().equals(b.target.getTagCompound()),key+" persisted NBT");
 }
 static final class W extends World {W(boolean remote){super(new net.minecraft.world.storage.SaveHandlerMP(),new net.minecraft.world.storage.WorldInfo(new NBTTagCompound()),new WorldProviderSurface(),new net.minecraft.profiler.Profiler(),remote);provider.setWorld(this);}protected net.minecraft.world.chunk.IChunkProvider createChunkProvider(){return null;}protected boolean isChunkLoaded(int x,int z,boolean e){return false;}}
 static final class P extends EntityPlayer {int opens;P(World w){super(w,new com.mojang.authlib.GameProfile(new UUID(1,2),"BoxTest"));}public boolean isSpectator(){return false;}public boolean isCreative(){return false;}public void openGui(Object mod,int id,World w,int x,int y,int z){opens++;}}
 static void containers() {
  for(int kind=0;kind<2;kind++)for(int selected=0;selected<9;selected++) {
   P p=new P(new W(false));p.inventory.currentItem=selected;ItemStack owner=new ItemStack(Items.CHEST_MINECART);p.inventory.mainInventory.set(selected,owner);
   ItemInventory inv=kind==0?new ItemLeadBox.InventoryLeadBox(p,owner):new ItemToolBox.InventoryToolBox(p,owner);Container c=kind==0?new ContainerLeadBox(p.inventory,(ItemLeadBox.InventoryLeadBox)inv):new ContainerToolBox(p.inventory,(ItemToolBox.InventoryToolBox)inv);int size=inv.getSizeInventory();
   eq(c.inventorySlots.size()==size+36,"layout length");for(int k=0;k<size;k++)eq(c.getSlot(k).inventory==inv&&c.getSlot(k).getSlotIndex()==k,"domain index");for(int k=0;k<9;k++)eq(c.getSlot(size+27+k).getSlotIndex()==k&&c.getSlot(size+27+k).inventory==p.inventory,"hotbar layout");
   ItemStack source=stack(3);p.inventory.mainInventory.set(9,source);ItemStack result=c.transferStackInSlot(p,size);eq(result!=source&&raw(result)==3,"transfer copied snapshot");eq(p.inventory.getStackInSlot(9)==ItemStack.EMPTY,"last source cleared sentinel");eq(raw(source)==0,"same source drained");eq(inv.slots[0]!=null&&raw(inv.slots[0])==(kind==0?1:3),"forward insertion");eq(p.inventory.mainInventory.get(selected)==owner,"held box exact identity");
   ItemStack export=inv.slots[0];int amount=raw(export);ItemStack exported=c.transferStackInSlot(p,0);eq(exported!=export&&raw(exported)==amount,"outbound copy");eq(inv.slots[0]==null,"outbound raw vacancy");int destination=selected==8?7:8;eq(raw(p.inventory.mainInventory.get(destination))==amount,"reverse hotbar insertion");eq(raw(export)==0,"outbound source drained");
   eq(c.transferStackInSlot(p,0)==ItemStack.EMPTY,"empty transfer sentinel");
   // Rejected toolbox into toolbox and crate into lead do not clear or copy source storage.
   ItemStack rejected=kind==0?new ItemStack(new com.hbm.items.block.ItemBlockStorageCrate()):new ItemStack(Items.CHEST_MINECART);p.inventory.mainInventory.set(10,rejected);eq(c.transferStackInSlot(p,size+1)==ItemStack.EMPTY,"invalid item transfer denied");eq(p.inventory.mainInventory.get(10)==rejected,"rejected source identity");
  }
 }
 public static void main(String[]args){Bootstrap.register();int emptyRaw=raw(ItemStack.EMPTY);
  for(int n:new int[]{0,2})for(int count:new int[]{Integer.MIN_VALUE,-128,-3,-1,0,1,63,64,Integer.MAX_VALUE})for(int source:new int[]{1,2,5,64})for(boolean reverse:new boolean[]{false,true})for(int limit:new int[]{1,64})for(int kind=0;kind<4;kind++)compare(n,count,source,reverse,limit,kind,false,true);
  for(int n:new int[]{0,2})for(int count:new int[]{1,2,64,1073741824,Integer.MAX_VALUE})for(boolean reverse:new boolean[]{false,true})compare(n,count,count,reverse,64,0,true,true);
  compare(0,-3,2,false,64,0,false,false);
  for(int count:new int[]{-3,-1,0,1,Integer.MAX_VALUE}){Inv inv=new Inv(1,64);inv.slots[0]=stack(count);ItemStack original=inv.slots[0],source=stack(2);boolean ok=ItemInventoryTransfer.mergeItemStack(Collections.singletonList(new SlotNonRetarded(inv,0,0,0)),source,0,1,false);eq(ok,"signed same-item accepted");int expected=count+2;eq(raw(original)==expected,"signed int arithmetic");eq(inv.slots[0]==(expected==0?null:original),"exact zero cleanup only");eq(raw(source)==0,"signed source consumed");}
  containers();eq(ItemStack.EMPTY.isEmpty()&&raw(ItemStack.EMPTY)==emptyRaw,"EMPTY unmodified");System.out.println("PASS "+tests+" native box transfer assertions; original-bytecode merge differential and explicit production projections");
 }
}
