import com.hbm.items.ItemInventory;
import net.minecraft.init.*;import net.minecraft.item.*;import net.minecraft.inventory.*;import net.minecraft.nbt.*;import net.minecraft.util.ResourceLocation;import net.minecraft.util.text.*;import net.minecraft.block.Block;
public class NestedInventoryTest {
 static int tests;static void eq(boolean ok,String msg){tests++;if(!ok)throw new AssertionError(msg);}
 static ItemStack stack(int count,int damage){ItemStack s=new ItemStack(Items.DIAMOND,count,damage);NBTTagCompound n=new NBTTagCompound();n.setString("payload","retained");s.setTagCompound(n);return s;}
 static NBTTagCompound serialized(ItemStack s){return s.writeToNBT(new NBTTagCompound());}
 static void common(ItemInventory i,int size,int limit,String name) {
  IInventory nativeInv=i;eq(nativeInv.getSizeInventory()==size,"size");eq(nativeInv.getInventoryStackLimit()==limit,"limit");eq(nativeInv.getName().equals(name),"key");eq(!nativeInv.hasCustomName(),"not custom");eq(nativeInv.getDisplayName() instanceof TextComponentTranslation,"translation");eq(nativeInv.isEmpty(),"empty");eq(nativeInv.getFieldCount()==0&&nativeInv.getField(19)==0,"fieldless");nativeInv.setField(19,4);
  for(int n=0;n<size;n++){eq(i.getStackInSlotNullable(n)==null,"internal vacancy");eq(nativeInv.getStackInSlot(n)==ItemStack.EMPTY,"native vacancy");eq(!new Slot(nativeInv,n,0,0).getHasStack(),"native Slot vacancy");}
  ItemStack s=stack(64,7);nativeInv.setInventorySlotContents(0,s);eq(i.slots[0]==s,"set identity");eq(s.getCount()==limit,"upper clamp");eq(new Slot(nativeInv,0,0,0).getHasStack(),"native Slot content");eq(nativeInv.removeStackFromSlot(0)==s,"remove identity");eq(i.slots[0]==null,"remove internal null");eq(nativeInv.removeStackFromSlot(0)==ItemStack.EMPTY,"remove vacancy");
  ItemStack neg=stack(-3,11);nativeInv.setInventorySlotContents(0,neg);eq(i.slots[0]==neg,"negative identity preserved");eq(serialized(i.slots[0]).getByte("Count")==-3,"negative payload preserved");eq(!new Slot(nativeInv,0,0,0).getHasStack(),"native empty interpretation");nativeInv.clear();eq(i.slots[0]==null,"clear null");
  i.target.setStackDisplayName("Custom inventory");eq(nativeInv.hasCustomName(),"custom flag");eq(nativeInv.getDisplayName() instanceof TextComponentString,"literal component");eq(nativeInv.getDisplayName().getUnformattedText().equals("Custom inventory"),"custom literal");
 }
 static final class ObservedWorld extends net.minecraft.world.World {
  ObservedWorld(){super(new net.minecraft.world.storage.SaveHandlerMP(),new net.minecraft.world.storage.WorldInfo(new NBTTagCompound()),new net.minecraft.world.WorldProviderSurface(),new net.minecraft.profiler.Profiler(),false);provider.setWorld(this);}
  protected net.minecraft.world.chunk.IChunkProvider createChunkProvider(){return null;}
  protected boolean isChunkLoaded(int x,int z,boolean allowEmpty){return false;}
 }
 static final class ObservedPlayer extends net.minecraft.entity.player.EntityPlayer {
  ObservedPlayer(net.minecraft.world.World world){super(world,new com.mojang.authlib.GameProfile(new java.util.UUID(7,8),"NestedInventory"));}
  public boolean isSpectator(){return false;}public boolean isCreative(){return false;}
 }
 static final class ObservedContainer extends Container {int changes;public boolean canInteractWith(net.minecraft.entity.player.EntityPlayer p){return true;}public void detectAndSendChanges(){changes++;}}
 static ItemStack owner(){return new ItemStack(Items.CHEST_MINECART);}
 public static void main(String[]args){Bootstrap.register();
  for(int ownerCount:new int[]{Integer.MIN_VALUE,-1,0,1,64}) for(int contentCount:new int[]{-128,-7,-1,0,1}) for(int kind=0;kind<3;kind++) {
   ItemStack target=new ItemStack(Items.CHEST_MINECART,ownerCount);NBTTagCompound n=new NBTTagCompound();n.setString("unrelated","owner NBT retained");NBTTagCompound entry=serialized(stack(contentCount,9));entry.setByte("slot",(byte)0);NBTTagList list=new NBTTagList();list.appendTag(entry);n.setTag("items",list);target.setTagCompound(n);
   ItemInventory opened=kind==0?new ItemLeadBox.InventoryLeadBox(null,target):kind==1?new ItemToolBox.InventoryToolBox(null,target):new ItemRebarPlacer.InventoryRebar(null,target);
   eq(opened.target==target&&target.getTagCompound()==n,"constructor owner and compound identities");eq(n.getString("unrelated").equals("owner NBT retained")&&n.getTagList("items",10)==list,"constructor untouched raw owner data");NBTTagCompound expected=entry.copy();expected.removeTag("slot");eq(opened.slots[0]!=null&&serialized(opened.slots[0]).equals(expected),"constructor loads signed contents for raw owner");
  }
  ItemStack leadOwner=owner();ItemLeadBox.InventoryLeadBox lead=new ItemLeadBox.InventoryLeadBox(null,leadOwner);common(lead,20,1,"container.leadBox");
  ItemStack toolOwner=owner();ItemToolBox.InventoryToolBox tool=new ItemToolBox.InventoryToolBox(null,toolOwner);common(tool,24,64,"container.toolBox");
  ItemStack rebarOwner=owner();ItemRebarPlacer.InventoryRebar rebar=new ItemRebarPlacer.InventoryRebar(null,rebarOwner);common(rebar,1,1,"container.rebar");
  eq(lead.isUsableByPlayer(null)&&tool.isUsableByPlayer(null)&&rebar.isUsableByPlayer(null),"inherited usability");
  ModItems.toolbox=Items.CHEST_MINECART;eq(!tool.isItemValidForSlot(0,owner()),"reject same toolbox identity");eq(tool.isItemValidForSlot(0,new ItemStack(Items.DIAMOND)),"accept other");eq(lead.isItemValidForSlot(0,null),"original null validity");
  ItemStack[] owners={leadOwner,toolOwner,rebarOwner};ItemInventory[] invs={lead,tool,rebar};
  for(int k=0;k<owners.length;k++){ItemInventory i=invs[k];i.slots[0]=stack(-2,12);i.markDirty();NBTTagList list=owners[k].getTagCompound().getTagList("items",10);eq(list.tagCount()==1&&list.getCompoundTagAt(0).getByte("Count")==-2,"negative list persistence");ItemInventory reopened=k==0?new ItemLeadBox.InventoryLeadBox(null,owners[k]):k==1?new ItemToolBox.InventoryToolBox(null,owners[k]):new ItemRebarPlacer.InventoryRebar(null,owners[k]);NBTTagCompound expected=list.getCompoundTagAt(0).copy();expected.removeTag("slot");eq(reopened.slots[0]!=null&&serialized(reopened.slots[0]).equals(expected),"negative reopen exact payload");}
  Block[] blocks={ModBlocks.crate_iron,ModBlocks.crate_steel,ModBlocks.crate_desh,ModBlocks.crate_tungsten,ModBlocks.crate_template,ModBlocks.safe};int[] sizes={36,54,104,27,27,15};String[] names={"crateIron","crateSteel","crateDesh","crateTungsten","crateTemplate","safe"};
  for(int k=0;k<blocks.length;k++) {ItemBlockStorageCrate item=new ItemBlockStorageCrate(blocks[k]);ItemStack target=new ItemStack(Item.getItemFromBlock(blocks[k]));ItemBlockStorageCrate.InventoryCrate crate=new ItemBlockStorageCrate.InventoryCrate(null,target);common(crate,sizes[k],64,"container."+names[k]);eq(!lead.isItemValidForSlot(0,new ItemStack(item)),"lead rejects crate class");
   target.getTagCompound().setString("unrelated","retained");target.getTagCompound().setInteger("lock",43);crate.slots[0]=stack(-4,17);crate.slots[1]=stack(0,19);crate.markDirty();eq(crate.slots[1]!=null,"crate does not inherit zero cleanup");eq(target.getTagCompound().hasKey("stacklock",4),"stacklock retained");eq(target.getTagCompound().getInteger("lock")==43&&target.getTagCompound().getString("unrelated").equals("retained"),"unrelated NBT retained");
   NBTTagCompound n=target.getTagCompound();eq(n.getCompoundTag("slot0").getByte("Count")==-4,"crate negative persisted");eq(n.getCompoundTag("slot1").getByte("Count")==0,"crate zero persisted");ItemBlockStorageCrate.InventoryCrate reopened=new ItemBlockStorageCrate.InventoryCrate(null,target);eq(serialized(reopened.slots[0]).equals(n.getCompoundTag("slot0")),"crate negative reopened");eq(serialized(reopened.slots[1]).equals(n.getCompoundTag("slot1")),"crate zero reopened");reopened.clear();eq(!n.hasKey("slot0")&&!n.hasKey("slot1")&&n.hasKey("stacklock"),"crate clear virtual writer");
  }
  ObservedWorld world=new ObservedWorld();ObservedPlayer player=new ObservedPlayer(world);ObservedContainer container=new ObservedContainer();player.inventoryContainer=container;
  ItemStack toolTarget=owner();ItemToolBox.InventoryToolBox closingTool=new ItemToolBox.InventoryToolBox(player,toolTarget);toolTarget.getTagCompound().setBoolean("isOpen",true);closingTool.closeInventory(player);eq(!toolTarget.getTagCompound().hasKey("isOpen")&&toolTarget.getTagCompound().hasKey("rand",3)&&container.changes==1,"tool close native sound and update");
  for(Block block:blocks){ItemStack target=new ItemStack(Item.getItemFromBlock(block));ItemBlockStorageCrate.InventoryCrate crate=new ItemBlockStorageCrate.InventoryCrate(player,target);player.inventory.mainInventory.set(0,target);eq(crate.isUsableByPlayer(player),"crate same held identity");player.inventory.mainInventory.set(0,target.copy());eq(!crate.isUsableByPlayer(player),"crate equal copied held rejected");player.inventory.mainInventory.set(0,ItemStack.EMPTY);eq(!crate.isUsableByPlayer(player),"crate empty held rejected");int before=container.changes;crate.closeInventory(player);eq(container.changes==before+1&&target.getTagCompound()==null,"empty crate close removes stacklock and compound");}
  System.out.println("PASS "+tests+" native nested inventory assertions; four complete declarations, explicit outer registry/type fixtures and crate type projections");
 }
}
