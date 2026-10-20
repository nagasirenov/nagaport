import java.lang.reflect.*;
import java.util.*;
import sun.misc.Unsafe;
import net.minecraft.entity.player.*;
import net.minecraft.inventory.*;
import net.minecraft.item.*;
import net.minecraft.nbt.*;
import net.minecraft.init.*;
import net.minecraft.util.*;
import net.minecraft.world.*;
import com.hbm.main.ModSoundEvents;

/** Native interface + exact production method slices; not whole-owner or game proof. */
public final class LifecycleNativeTest {
 static int checks; static List<String> events=new ArrayList<>(); static List<Object[]> sounds=new ArrayList<>();
 static void check(boolean v,String label){checks++;if(!v)throw new AssertionError(label);}
 static Unsafe unsafe;
 static World world(boolean remote)throws Exception {
  World w=(World)unsafe.allocateInstance(net.minecraft.client.multiplayer.WorldClient.class);
  Field side=World.class.getDeclaredField("isRemote");side.setAccessible(true);side.setBoolean(w,remote);
  Field random=World.class.getDeclaredField("rand");random.setAccessible(true);random.set(w,new Random(123));
  IWorldEventListener listener=(IWorldEventListener)Proxy.newProxyInstance(LifecycleNativeTest.class.getClassLoader(),new Class[]{IWorldEventListener.class},(p,m,a)->{if(m.getName().equals("playSoundToAllNearExcept")){events.add("sound");sounds.add(a);}return null;});
  Field f=World.class.getDeclaredField("eventListeners");f.setAccessible(true);f.set(w,new ArrayList<>(Arrays.asList(listener)));return w;
 }
 static EntityPlayer player(World w)throws Exception {
  EntityPlayer p=(EntityPlayer)unsafe.allocateInstance(EntityPlayerMP.class);p.world=w;p.posX=2;p.posY=3;p.posZ=4;
  p.inventoryContainer=new Container(){public boolean canInteractWith(EntityPlayer p){return true;} @Override public void detectAndSendChanges(){events.add("sync");}};return p;
 }
 static void sound(int index,String key,double x,double y,double z,float volume,float pitch){Object[] a=sounds.get(index);check(a[0]==null,"coordinate broadcast");check(a[1]==ModSoundEvents.worldEffect(new ResourceLocation(key)),"native sound identity "+key);check(a[2]==SoundCategory.BLOCKS,"category");check(a[3].equals(x)&&a[4].equals(y)&&a[5].equals(z),"coordinates");check(a[6].equals(volume)&&a[7].equals(pitch),"volume and raw pitch");}
 static void reset(){events.clear();sounds.clear();}
 public static void main(String[] args)throws Exception {
  Field f=Unsafe.class.getDeclaredField("theUnsafe");f.setAccessible(true);unsafe=(Unsafe)f.get(null);Bootstrap.register();
  World server=world(false),client=world(true);EntityPlayer owner=player(server),viewer=player(client);
  Crate crate=new Crate();crate.world=server;IInventory inv=crate;inv.openInventory(viewer);inv.closeInventory(viewer);check(sounds.size()==2,"crate sound counts");sound(0,"hbm:block.crateOpen",-6.5,29.5,30.5,1,1);sound(1,"hbm:block.crateClose",-6.5,29.5,30.5,1,1);
  reset();Mass mass=new Mass();mass.world=server;inv=mass;inv.openInventory(null);inv.closeInventory(null);check(sounds.size()==2,"mass sound counts null viewer");sound(0,"hbm:block.storageOpen",-6.5,29.5,30.5,.5F,1);sound(1,"hbm:block.storageClose",-6.5,29.5,30.5,.5F,1);
  reset();Cabinet cabinet=new Cabinet();cabinet.world=server;inv=cabinet;inv.openInventory(viewer);inv.openInventory(null);check(cabinet.playersUsing==2,"cabinet native virtual open");inv.closeInventory(viewer);check(cabinet.playersUsing==1,"cabinet decrement");inv.closeInventory(null);inv.closeInventory(null);check(cabinet.playersUsing==-1,"original no clamp retained");check(sounds.isEmpty(),"cabinet override does not call crate sound");cabinet.world=client;inv.openInventory(viewer);inv.closeInventory(viewer);check(cabinet.playersUsing==-1,"client counter untouched");
  reset();ItemBase item=new ItemBase();item.player=owner;inv=item;inv.openInventory(viewer);inv.closeInventory(null);check(sounds.size()==2,"item owner sounds");sound(0,"hbm:block.crateOpen",2,3,4,1,.8F);sound(1,"hbm:block.crateClose",2,3,4,1,.8F);
  for(ItemBase bag:Arrays.asList(new AmmoBag(),new CasingBag(),new PlasticBag())){reset();bag.player=owner;inv=bag;inv.openInventory(viewer);inv.closeInventory(viewer);check(sounds.isEmpty(),"bag override suppresses parent sound");}
  reset();ItemCrate box=new ItemCrate();box.player=owner;box.target=new ItemStack(Items.STICK);box.target.setTagCompound(new NBTTagCompound());box.target.getTagCompound().setLong("stacklock",123);inv=box;inv.closeInventory(viewer);check(events.equals(Arrays.asList("sound","checkNBT","sync")),"crate ordered superclass, bounded checkNBT, sync");check(!box.target.hasTagCompound(),"empty crate tag removed");sound(0,"hbm:block.crateClose",2,3,4,1,.8F);
  reset();box.target.setTagCompound(new NBTTagCompound());box.target.getTagCompound().setString("retained","payload");box.target.getTagCompound().setLong("stacklock",123);inv.closeInventory(null);check(box.target.getTagCompound().getString("retained").equals("payload"),"payload survives");check(box.target.getTagCompound().getLong("stacklock")!=123,"nonempty crate relocked");
  reset();ToolBox tool=new ToolBox();tool.player=owner;tool.target=new ItemStack(Items.STICK);tool.target.setTagCompound(new NBTTagCompound());tool.target.getTagCompound().setBoolean("isOpen",true);inv=tool;inv.closeInventory(viewer);check(events.equals(Arrays.asList("sound","sync")),"toolbox superclass before sync");check(!tool.target.getTagCompound().hasKey("isOpen")&&tool.target.getTagCompound().hasKey("rand"),"toolbox tag cleanup");check(tool.target.getTagCompound().getInteger("rand")==new Random(123).nextInt(),"stored owner's world RNG, not viewer");
  reset();crate.world=null;try{((IInventory)crate).openInventory(viewer);throw new AssertionError("null world should fail");}catch(NullPointerException expected){checks++;}check(sounds.isEmpty(),"no sound on null world");
  for(int i=0;i<62;i++){inv=(IInventory)Class.forName("Empty"+i).newInstance();inv.openInventory(null);inv.closeInventory(viewer);checks++;}
  System.out.println("PASS LifecycleNativeTest: "+checks+" assertions; native IInventory dispatch, exact callback slices, real world listeners/sound helper, server/client cabinet counters, stored-owner and descendant NBT semantics. Bounded checkNBT collaborator only; not whole-machine/game validation.");
 }
}
