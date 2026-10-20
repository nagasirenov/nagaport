import java.util.Random;
import net.minecraft.block.Block;import net.minecraft.entity.item.EntityItem;import net.minecraft.inventory.IInventory;import net.minecraft.item.*;import net.minecraft.nbt.NBTTagCompound;import net.minecraft.util.math.BlockPos;import net.minecraft.world.World;
class ProductionSpill {
 static void spillItems(World w,int x,int y,int z,Block block,Random rng){
  IInventory inv=(IInventory)w.getTileEntity(new BlockPos(x,y,z));if(inv==null)return;
  for(int slot=0;slot<inv.getSizeInventory();slot++){
   ItemStack source=inv.getStackInSlot(slot);if(source==null)continue;
   float ox=rng.nextFloat()*0.8F+0.1F,oy=rng.nextFloat()*0.8F+0.1F,oz=rng.nextFloat()*0.8F+0.1F;
   int remaining=source.getCount();Item item=source.getItem();int meta=source.getItemDamage();NBTTagCompound tag=source.getTagCompound();
   while(remaining>0){int amount=Math.min(rng.nextInt(21)+10,remaining);remaining-=amount;source.setCount(remaining);
    EntityItem drop=new EntityItem(w,x+ox,y+oy,z+oz,new ItemStack(item,amount,meta));if(tag!=null)drop.getItem().setTagCompound(tag.copy());
    float speed=0.05F;drop.motionX=(float)rng.nextGaussian()*speed;drop.motionY=(float)rng.nextGaussian()*speed+0.2F;drop.motionZ=(float)rng.nextGaussian()*speed;w.spawnEntity(drop);
   }
  }
  w.updateComparatorOutputLevel(new BlockPos(x,y,z),block);
 }
}
