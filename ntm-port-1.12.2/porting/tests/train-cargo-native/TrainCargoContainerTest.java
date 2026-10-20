import com.hbm.entity.train.*;
import net.minecraft.entity.player.*;
import net.minecraft.init.*;
import net.minecraft.inventory.*;
import net.minecraft.item.*;
import net.minecraft.nbt.*;
import api.hbm.energymk2.IBatteryItem;
public class TrainCargoContainerTest {
 static int checks;static void check(boolean b,String s){checks++;if(!b)throw new AssertionError(s);}
 static class Battery extends Item implements IBatteryItem {public void chargeBattery(ItemStack s,long n){}public void setCharge(ItemStack s,long n){}public void dischargeBattery(ItemStack s,long n){}public long getCharge(ItemStack s){return 0;}public long getMaxCharge(ItemStack s){return 1;}public long getChargeRate(ItemStack s){return 1;}public long getDischargeRate(ItemStack s){return 1;}}
 static ItemStack stack(Item item,int n){ItemStack s=new ItemStack(item,n);NBTTagCompound tag=new NBTTagCompound();tag.setString("cargo","same");s.setTagCompound(tag);return s;}
 static int total(InventoryPlayer player,EntityRailCarCargo cargo,Item item){int n=0;for(int i=0;i<player.getSizeInventory();i++)if(player.getStackInSlot(i).getItem()==item)n+=player.getStackInSlot(i).getCount();for(int i=0;i<cargo.getSizeInventory();i++)if(cargo.getStackInSlot(i).getItem()==item)n+=cargo.getStackInSlot(i).getCount();return n;}
 public static void main(String[] args)throws Exception{Bootstrap.register();EntityPlayer player=(EntityPlayer)TrainCargoNativeTest.unsafe().allocateInstance(net.minecraft.entity.player.EntityPlayerMP.class);player.inventory=new InventoryPlayer(player);Item battery=new Battery();
  for(boolean trailer:new boolean[]{false,true}){
   EntityRailCarCargo cargo=trailer?new TrainCargoTramTrailer():new TrainCargoTram();Container container=trailer?new TrainCargoTramTrailer.ContainerTrainCargoTramTrailer(player.inventory,(TrainCargoTramTrailer)cargo):new TrainCargoTram.ContainerTrainCargoTram(player.inventory,(TrainCargoTram)cargo);int size=cargo.getSizeInventory();check(container.inventorySlots.size()==size+36,"original slot count");check(container.transferStackInSlot(player,0)==ItemStack.EMPTY,"empty shift-click native contract");
   for(Item item:new Item[]{Items.DIAMOND,battery})for(int amount:new int[]{1,20,64}){cargo.clear();player.inventory.clear();ItemStack s=stack(item,amount);player.inventory.setInventorySlotContents(9,s);ItemStack result=container.transferStackInSlot(player,size);int target=!trailer&&item==battery?28:0;check(result.getCount()==amount&&result.getItem()==item,"original copied return");check(cargo.getStackInSlot(target).getCount()==amount&&player.inventory.getStackInSlot(9).isEmpty(),"native merge target and source empty");check(cargo.getStackInSlot(target).getTagCompound().getString("cargo").equals("same"),"NBT retained");check(total(player.inventory,cargo,item)==amount,"count conserved incoming");ItemStack back=container.transferStackInSlot(player,target);check(back.getCount()==amount&&cargo.getStackInSlot(target).isEmpty(),"reverse transfer removes cargo");check(total(player.inventory,cargo,item)==amount,"count conserved reverse");check(player.inventory.getStackInSlot(8).getCount()==amount,"original reverse player ordering");}
   cargo.clear();player.inventory.clear();for(int i=0;i<(trailer?45:28);i++)cargo.setInventorySlotContents(i,new ItemStack(Blocks.COBBLESTONE,64));player.inventory.setInventorySlotContents(9,stack(Items.DIAMOND,10));check(container.transferStackInSlot(player,size)==ItemStack.EMPTY&&player.inventory.getStackInSlot(9).getCount()==10,"full ordinary destination refusal, no battery fallback");
   if(!trailer){cargo.clear();cargo.setInventorySlotContents(28,new ItemStack(Blocks.COBBLESTONE,64));player.inventory.clear();player.inventory.setInventorySlotContents(9,stack(battery,10));check(container.transferStackInSlot(player,size)==ItemStack.EMPTY&&player.inventory.getStackInSlot(9).getCount()==10,"blocked battery slot does not reroute to cargo");}
   cargo.clear();player.inventory.clear();cargo.setInventorySlotContents(0,stack(Items.DIAMOND,60));player.inventory.setInventorySlotContents(9,stack(Items.DIAMOND,10));check(container.transferStackInSlot(player,size).getCount()==10,"partial+spill reports original copy");check(cargo.getStackInSlot(0).getCount()==64&&cargo.getStackInSlot(1).getCount()==6&&total(player.inventory,cargo,Items.DIAMOND)==70,"native merge partial then spill preserves quantities");
  }
  System.out.println("PASS "+checks+" complete verbatim native train-container slot/shift-click/NBT/count/routing checks; outer train classes are declared domain fixtures.");
 }
}
