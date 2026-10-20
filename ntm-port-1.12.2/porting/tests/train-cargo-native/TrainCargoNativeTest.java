import com.hbm.entity.train.EntityRailCarCargo;
import net.minecraft.init.*;
import net.minecraft.item.*;
import net.minecraft.nbt.*;
import net.minecraft.network.*;
import net.minecraft.network.datasync.*;
import net.minecraft.world.*;
import net.minecraft.entity.player.*;
import java.util.*;
import java.lang.reflect.*;

public class TrainCargoNativeTest {
 static int checks;static void check(boolean b,String s){checks++;if(!b)throw new AssertionError(s);}
 static sun.misc.Unsafe unsafe()throws Exception{Field f=sun.misc.Unsafe.class.getDeclaredField("theUnsafe");f.setAccessible(true);return (sun.misc.Unsafe)f.get(null);}
 static World world(boolean client)throws Exception{World w=(World)unsafe().allocateInstance(WorldServer.class);Field f=World.class.getDeclaredField("isRemote");f.setAccessible(true);f.setBoolean(w,client);Field provider=World.class.getDeclaredField("provider");provider.setAccessible(true);provider.set(w,new WorldProviderSurface());return w;}
 public static class Cargo extends EntityRailCarCargo {
  public Cargo(){super(null);}public int getSizeInventory(){return 45;}public String getInventoryName(){return hasCustomInventoryName()?getEntityName():"container.trainTest";}
  public NBTTagCompound save(){NBTTagCompound n=new NBTTagCompound();writeEntityToNBT(n);return n;}public void load(NBTTagCompound n){readEntityFromNBT(n);}
  public void raw(int i,ItemStack s){slots[i]=s;}
 }
 static ItemStack stack(int count){ItemStack s=new ItemStack(Items.DIAMOND,count);NBTTagCompound n=new NBTTagCompound();n.setString("payload","preserved");s.setTagCompound(n);return s;}
 static void sync(Cargo from,Cargo to)throws Exception{PacketBuffer b=new PacketBuffer(io.netty.buffer.Unpooled.buffer());EntityDataManager.writeEntries(from.getDataManager().getAll(),b);java.util.List<EntityDataManager.DataEntry<?>> entries=EntityDataManager.readEntries(b);to.getDataManager().setEntryValues(entries);b.release();}
 public static void main(String[] args)throws Exception{
  Bootstrap.register();Cargo c=new Cargo();c.world=world(false);check(c.isEmpty()&&c.countOccupiedSlots()==0&&c.getSyncedOccupiedSlots()==0,"initial slots and sync empty");for(int i=0;i<45;i++)check(c.getStackInSlot(i)==ItemStack.EMPTY,"native empty boundary "+i);
  for(int count:new int[]{1,2,63,64,65})for(int amount:new int[]{1,2,64,65}){
   c.clear();ItemStack source=stack(count);c.setInventorySlotContents(3,source);int stored=Math.min(64,count);check(source.getCount()==stored,"original setter clamps caller alias");ItemStack removed=c.decrStackSize(3,amount);check(removed.getCount()==Math.min(stored,amount),"decrement amount");check(removed.getTagCompound().getString("payload").equals("preserved"),"decrement NBT");check(c.getStackInSlot(3).getCount()==Math.max(0,stored-amount),"remaining count");check(c.countOccupiedSlots()==(stored<=amount?0:1),"presence after decrement");
  }
  c.clear();check(c.decrStackSize(0,1)==ItemStack.EMPTY&&c.removeStackFromSlot(0)==ItemStack.EMPTY,"empty extraction native boundary");ItemStack s=stack(12);c.setInventorySlotContents(0,s);check(c.removeStackFromSlot(0)==s&&c.getStackInSlot(0)==ItemStack.EMPTY,"remove returns original alias");c.setInventorySlotContents(0,null);check(c.isEmpty(),"null normalization");c.setInventorySlotContents(0,ItemStack.EMPTY);check(c.isEmpty(),"EMPTY normalization");
  s=stack(1);c.setInventorySlotContents(0,s);s.shrink(1);check(c.getStackInSlot(0)==ItemStack.EMPTY&&c.countOccupiedSlots()==1&&c.isEmpty(),"original occupied presence survives externally consumed live stack, native empty view");c.onUpdate();check(c.baseTicks==1&&c.getSyncedOccupiedSlots()==1,"server updates only on original tick after super once");Cargo client=new Cargo();client.world=world(true);sync(c,client);check(client.getSyncedOccupiedSlots()==1,"native data serializer transfers occupied count");client.onUpdate();check(client.baseTicks==1&&client.getSyncedOccupiedSlots()==1,"client tick never recomputes server count");
  c.clear();for(int i:new int[]{0,23,44})c.setInventorySlotContents(i,stack(i+1));check(c.getSyncedOccupiedSlots()==1,"set/clear do not invent eager sync");c.onUpdate();check(c.getSyncedOccupiedSlots()==3,"server tick updates presence");NBTTagCompound saved=c.save();check(c.baseWrites==1&&saved.getTagList("Items",10).tagCount()==3,"original NBT keys and super write once");Cargo loaded=new Cargo();loaded.load(saved);check(loaded.baseReads==1&&loaded.getSyncedOccupiedSlots()==3,"load rebuilds slots/sync after super once");for(int i:new int[]{0,23,44})check(ItemStack.areItemStacksEqual(c.getStackInSlot(i),loaded.getStackInSlot(i)),"slot/NBT roundtrip "+i);
  NBTTagList list=saved.getTagList("Items",10);NBTTagCompound duplicate=stack(7).writeToNBT(new NBTTagCompound());duplicate.setByte("Slot",(byte)23);list.appendTag(duplicate);NBTTagCompound invalid=stack(9).writeToNBT(new NBTTagCompound());invalid.setByte("Slot",(byte)255);list.appendTag(invalid);loaded.load(saved);check(loaded.getStackInSlot(23).getCount()==7&&loaded.countOccupiedSlots()==3,"unsigned invalid slot ignored, duplicate last wins");NBTTagCompound empty=new NBTTagCompound();empty.setByte("Slot",(byte)23);list.appendTag(empty);loaded.load(saved);check(loaded.getStackInSlot(23)==ItemStack.EMPTY&&loaded.getSyncedOccupiedSlots()==2,"invalid duplicate clears original slot");loaded.load(new NBTTagCompound());check(loaded.isEmpty()&&loaded.getSyncedOccupiedSlots()==0,"missing Items resets inventory");
  String defaultName=c.getName();check(!c.hasCustomName()&&!c.hasCustomInventoryName()&&c.getInventoryName().equals("container.trainTest"),"entity name and legacy domain inventory label stay separate");for(String name:new String[]{"Custom cargo",""}){c.setEntityName(name);check(c.hasCustomName()&&c.hasCustomInventoryName()&&c.getName().equals(name)&&c.getInventoryName().equals(name),"nonnull custom name including empty string retained");}c.setEntityName(null);check(c.getName().equals(defaultName),"null name uses native entity default");c.setCustomNameTag("Native tag");check(c.getName().equals("Native tag")&&c.hasCustomName()&&!c.hasCustomInventoryName(),"inherited entity name tag remains distinct from domain inventory label");
  EntityPlayer player=(EntityPlayer)unsafe().allocateInstance(net.minecraft.entity.player.EntityPlayerMP.class);c.setPosition(0,0,0);player.posX=8;check(c.isUsableByPlayer(player),"usable inclusive squared64");player.posX=8.001;check(!c.isUsableByPlayer(player),"outside distance refused");player.posX=0;c.setDead();check(!c.isUsableByPlayer(player),"dead inventory refused");check(c.getFieldCount()==0&&c.getField(0)==0,"no synthetic container progress fields");
  System.out.println("PASS "+checks+" actual native cargo inventory/NBT/data-serializer/tick/name/distance checks; train physics superclass is an explicit fixture, not full train/game acceptance.");
 }
}
