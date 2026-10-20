import com.hbm.inventory.container.ContainerNT;
import com.hbm.inventory.container.ContainerBase;
import com.mojang.authlib.GameProfile;
import java.util.UUID;
import net.minecraft.init.Bootstrap;
import net.minecraft.init.Items;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.inventory.*;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.profiler.Profiler;
import net.minecraft.world.*;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.storage.*;

public class ContainerClicksMinecraftTest {
 static int checks;
 static void check(boolean b,String s){checks++;if(!b)throw new AssertionError(s);}
 static class W extends World {
  W(){super(new SaveHandlerMP(),new WorldInfo(new NBTTagCompound()),new WorldProviderSurface(),new Profiler(),false);provider.setWorld(this);}
  protected IChunkProvider createChunkProvider(){return null;}
  protected boolean isChunkLoaded(int x,int z,boolean allow){return false;}
 }
 static class P extends EntityPlayer {
  int dropped;
  P(){super(new W(),new GameProfile(new UUID(0,1),"ClicksTest"));}
  public boolean isSpectator(){return false;}
  public boolean isCreative(){return capabilities.isCreativeMode;}
  public EntityItem dropItem(ItemStack s,boolean random){check(s!=null,"drop nonnull");dropped+=s.getCount();return null;}
 }
 static class S extends Slot {
  int takes; boolean accepts=true; int limit=64; int emptyRemoval;
  S(IInventory inv,int n){super(inv,n,0,0);}
  public boolean isItemValid(ItemStack s){check(s!=null,"valid nonnull");return accepts;}
  public int getSlotStackLimit(){return limit;}
  public ItemStack decrStackSize(int n){return emptyRemoval==1?ItemStack.EMPTY:emptyRemoval==2?null:super.decrStackSize(n);}
  public void putStack(ItemStack s){check(s!=null,"put nonnull");super.putStack(s);}
  public ItemStack onTake(EntityPlayer p,ItemStack s){check(s!=null,"take nonnull");takes++;return super.onTake(p,s);}
 }
 static class C extends ContainerNT {
  InventoryBasic inv=new InventoryBasic("test",false,4); S[] s=new S[4];
  C(){for(int i=0;i<4;i++){s[i]=new S(inv,i);addSlotToContainer(s[i]);}}
  public boolean canInteractWith(EntityPlayer p){return true;}
 }
 static class Moving extends C {
  int moves;
  public ItemStack transferStackInSlot(EntityPlayer p,int index){
   ItemStack before=inv.getStackInSlot(index).copy();
   if(before.isEmpty())return ItemStack.EMPTY;
   moves++;inv.decrStackSize(index,1);return before;
  }
 }
 static class B extends ContainerBase {
  B(P p,InventoryBasic inv){super(p.inventory,inv);for(int i=0;i<inv.getSizeInventory();i++)addSlotToContainer(new S(inv,i));}
  boolean merge(ItemStack s,boolean reverse){return mergeItemStack(s,0,inventorySlots.size(),reverse);}
 }
 static ItemStack stack(int n){return new ItemStack(Items.APPLE,n);}
 static void click(C c,P p,int index,int button,ClickType t){check(c.slotClick(index,button,t,p)!=null,"native click result "+t);}
 static void drag(C c,P p,int mode){click(c,p,-999,Container.getQuickcraftMask(0,mode),ClickType.QUICK_CRAFT);click(c,p,0,Container.getQuickcraftMask(1,mode),ClickType.QUICK_CRAFT);click(c,p,1,Container.getQuickcraftMask(1,mode),ClickType.QUICK_CRAFT);click(c,p,-999,Container.getQuickcraftMask(2,mode),ClickType.QUICK_CRAFT);}
 public static void main(String[] args){
  Bootstrap.register(); P p=new P(); C c=new C();
  check(c.transferStackInSlot(p,0)==ItemStack.EMPTY,"default shift EMPTY");
  click(c,p,0,0,ClickType.PICKUP);check(p.inventory.getItemStack().isEmpty(),"empty pickup");
  p.inventory.setItemStack(stack(5));click(c,p,0,1,ClickType.PICKUP);check(c.inv.getStackInSlot(0).getCount()==1&&p.inventory.getItemStack().getCount()==4,"right place");
  click(c,p,0,0,ClickType.PICKUP);check(c.inv.getStackInSlot(0).getCount()==5&&p.inventory.getItemStack()==ItemStack.EMPTY,"merge cursor");
  click(c,p,0,1,ClickType.PICKUP);check(p.inventory.getItemStack().getCount()==3&&c.inv.getStackInSlot(0).getCount()==2,"half pickup");
  click(c,p,-999,1,ClickType.PICKUP);check(p.dropped==1&&p.inventory.getItemStack().getCount()==2,"outside right");
  click(c,p,-999,0,ClickType.PICKUP);check(p.dropped==3&&p.inventory.getItemStack()==ItemStack.EMPTY,"outside left");
  click(c,p,0,0,ClickType.THROW);check(p.dropped==4&&c.inv.getStackInSlot(0).getCount()==1,"throw single");
  click(c,p,0,1,ClickType.THROW);check(p.dropped==5&&c.inv.getStackInSlot(0).isEmpty(),"throw all");
  c.inv.setInventorySlotContents(0,stack(7));click(c,p,0,2,ClickType.SWAP);check(p.inventory.getStackInSlot(2).getCount()==7&&c.inv.getStackInSlot(0).isEmpty(),"hotbar extract");
  click(c,p,0,2,ClickType.SWAP);check(p.inventory.getStackInSlot(2)==ItemStack.EMPTY&&c.inv.getStackInSlot(0).getCount()==7,"hotbar insert");
  p.inventory.setInventorySlotContents(40,stack(3));click(c,p,0,40,ClickType.SWAP);check(c.inv.getStackInSlot(0).getCount()==7&&p.inventory.getStackInSlot(40).getCount()==3,"1.12 offhand no GUI swap");
  click(c,p,0,0,ClickType.CLONE);check(p.inventory.getItemStack().isEmpty(),"survival clone denied");p.capabilities.isCreativeMode=true;click(c,p,0,0,ClickType.CLONE);check(p.inventory.getItemStack().getCount()==64,"creative clone");
  c=new C();p.capabilities.isCreativeMode=false;p.inventory.setItemStack(stack(8));drag(c,p,0);check(c.inv.getStackInSlot(0).getCount()==4&&c.inv.getStackInSlot(1).getCount()==4&&p.inventory.getItemStack().isEmpty(),"even drag");
  c=new C();p.inventory.setItemStack(stack(8));drag(c,p,1);check(c.inv.getStackInSlot(0).getCount()==1&&c.inv.getStackInSlot(1).getCount()==1&&p.inventory.getItemStack().getCount()==6,"one drag");
  c=new C();p.inventory.setItemStack(stack(1));drag(c,p,2);check(c.inv.isEmpty()&&p.inventory.getItemStack().getCount()==1,"survival creative drag denied");
  c=new C();p.capabilities.isCreativeMode=true;p.inventory.setItemStack(stack(1));drag(c,p,2);check(c.inv.isEmpty()&&p.inventory.getItemStack().getCount()==1,"creative drag mode2 rejected by original algorithm");
  c=new C();p.inventory.setItemStack(stack(2));c.inv.setInventorySlotContents(1,stack(5));click(c,p,0,0,ClickType.PICKUP_ALL);check(p.inventory.getItemStack().getCount()==7&&c.inv.isEmpty(),"collect all");
  c=new C();p.inventory.setItemStack(ItemStack.EMPTY);c.inv.setInventorySlotContents(0,stack(6));click(c,p,0,0,ClickType.QUICK_MOVE);check(c.inv.getStackInSlot(0).getCount()==6,"default shift no-op");
  InventoryBasic inv=new InventoryBasic("base",false,2);B b=new B(p,inv);ItemStack moving=stack(70);check(b.merge(moving,false)&&moving.isEmpty()&&inv.getStackInSlot(0).getCount()==64&&inv.getStackInSlot(1).getCount()==6,"base merge empty and overflow");
  check(!b.merge(ItemStack.EMPTY,false),"base EMPTY source");inv.clear();inv.setInventorySlotContents(0,stack(60));moving=stack(10);check(b.merge(moving,false)&&inv.getStackInSlot(0).getCount()==64&&inv.getStackInSlot(1).getCount()==6,"base merge occupied then empty");
  Moving m=new Moving();m.inv.setInventorySlotContents(0,stack(3));p.inventory.setItemStack(ItemStack.EMPTY);ItemStack ret=m.slotClick(0,0,ClickType.QUICK_MOVE,p);check(m.moves==3&&m.inv.isEmpty()&&ret.getCount()==3,"legacy shift repeat and first return");
  c=new C();p.inventory.setItemStack(stack(10));c.s[0].limit=2;click(c,p,0,0,ClickType.PICKUP);check(c.inv.getStackInSlot(0).getCount()==2&&p.inventory.getItemStack().getCount()==8,"custom place slot limit");
  c=new C();p.inventory.setItemStack(stack(2));c.s[0].accepts=false;c.inv.setInventorySlotContents(0,stack(5));click(c,p,0,0,ClickType.PICKUP);check(p.inventory.getItemStack().getCount()==7&&c.inv.isEmpty()&&c.s[0].takes==1,"take-only merge into cursor callback");
  c=new C();p.inventory.setItemStack(stack(2));click(c,p,-999,Container.getQuickcraftMask(0,0),ClickType.QUICK_CRAFT);click(c,p,0,0,ClickType.PICKUP);check(c.inv.isEmpty(),"interrupted drag consumes first non-drag");click(c,p,0,0,ClickType.PICKUP);check(c.inv.getStackInSlot(0).getCount()==2,"next pickup after drag reset");
  c=new C();p.inventory.setItemStack(stack(2));ItemStack tagged=stack(3);NBTTagCompound tag=new NBTTagCompound();tag.setString("test","different");tagged.setTagCompound(tag);c.inv.setInventorySlotContents(0,tagged);click(c,p,0,0,ClickType.PICKUP);check(p.inventory.getItemStack().hasTagCompound()&&c.inv.getStackInSlot(0).getCount()==2,"NBT mismatch swaps rather than merges");
  c=new C();p.inventory.setItemStack(ItemStack.EMPTY);p.inventory.clear();p.inventory.setInventorySlotContents(2,new ItemStack(Items.STICK,4));c.inv.setInventorySlotContents(0,stack(5));click(c,p,0,2,ClickType.SWAP);check(p.inventory.getStackInSlot(2).getItem()==Items.APPLE&&p.inventory.getStackInSlot(0).getItem()==Items.STICK&&c.inv.isEmpty(),"legacy foreign hotbar swap rehomes old hotbar instead of slot");
  inv.clear();moving=stack(2);check(b.merge(moving,true)&&inv.getStackInSlot(1).getCount()==2&&inv.getStackInSlot(0).isEmpty(),"base reverse order");
  inv.clear();((S)b.inventorySlots.get(0)).accepts=false;moving=stack(2);check(b.merge(moving,false)&&inv.getStackInSlot(1).getCount()==2&&inv.getStackInSlot(0).isEmpty(),"base rejects invalid slot");
  c=new C();p.inventory.setItemStack(ItemStack.EMPTY);c.inv.setInventorySlotContents(0,stack(5));c.s[0].emptyRemoval=1;click(c,p,0,0,ClickType.PICKUP);check(p.inventory.getItemStack()==ItemStack.EMPTY,"EMPTY removal stays native cursor");
  c.s[0].emptyRemoval=2;click(c,p,0,0,ClickType.PICKUP);check(p.inventory.getItemStack()==ItemStack.EMPTY,"legacy null removal normalized");
  check(ItemStack.EMPTY.getCount()==0&&ItemStack.EMPTY.isEmpty(),"EMPTY unmodified");
  System.out.println("PASS container native clicks/merge "+checks+" assertions");
 }
}
