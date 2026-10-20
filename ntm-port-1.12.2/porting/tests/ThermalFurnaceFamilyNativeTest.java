import com.hbm.tileentity.machine.*;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.inventory.container.*;
import com.hbm.inventory.SlotSmelting;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemMachineUpgrade;
import com.hbm.items.machine.ItemMachineUpgrade.UpgradeType;
import com.hbm.handler.FuelHandler;
import com.hbm.handler.pollution.PollutionHandler;
import com.hbm.handler.threading.PacketThreading;
import com.hbm.items.ItemEnums.EnumAshType;
import api.hbm.tile.IHeatSource;
import net.minecraft.init.*;
import net.minecraft.item.*;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.*;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.storage.*;
import net.minecraft.profiler.Profiler;
import net.minecraft.nbt.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraftforge.oredict.OreDictionary;
import com.mojang.authlib.GameProfile;
import io.netty.buffer.*;
import java.util.*;
import java.lang.reflect.*;
public final class ThermalFurnaceFamilyNativeTest {
 static int checks; static void check(boolean v,String l){checks++;if(!v)throw new AssertionError(l);}
 static final class ProbeWorld extends World {
  long time;TileEntity current;Map<BlockPos,TileEntity> neighbors=new HashMap<>();int particles,sounds;
  ProbeWorld(boolean remote){super(new SaveHandlerMP(),new WorldInfo(new NBTTagCompound()),new WorldProviderSurface(),new Profiler(),remote);provider.setWorld(this);}
  protected IChunkProvider createChunkProvider(){return null;} protected boolean isChunkLoaded(int x,int z,boolean e){return false;}
  public long getTotalWorldTime(){return time;}public TileEntity getTileEntity(BlockPos p){return current!=null&&p.equals(current.getPos())?current:neighbors.get(p);}
  public net.minecraft.block.state.IBlockState getBlockState(BlockPos p){return Blocks.AIR.getDefaultState();}
  public void spawnParticle(EnumParticleTypes p,double x,double y,double z,double a,double b,double c,int...args){particles++;}
  public void playSound(EntityPlayer p,double x,double y,double z,SoundEvent e,SoundCategory c,float v,float t){sounds++;}
 }
 public static class Brick extends TileEntityFurnaceBrick {int dirty;public void markDirty(){dirty++;}public boolean ash(int n,EnumAshType t){return processAsh(n,t,2000);}}
 public static class Iron extends TileEntityFurnaceIron {int dirty;public void markDirty(){dirty++;}}
 public static class Steel extends TileEntityFurnaceSteel {int dirty;public void markDirty(){dirty++;}public void pull(){tryPullHeat();}public void bonus(ItemStack s,int i){addBonus(s,i);}}
 static final class Heat extends TileEntity implements IHeatSource {int heat,used;Heat(int n){heat=n;}public int getHeatStored(){return heat;}public void useUpHeat(int n){used+=n;heat-=n;}}
 static final class Player extends EntityPlayer {Player(World w){super(w,new GameProfile(new UUID(0,1),"ThermalProbe"));}public boolean isSpectator(){return false;}public boolean isCreative(){return false;}}
 static final class CallbackFuel extends Item {int calls,count;ItemStack seen; public ItemStack getContainerItem(ItemStack s){calls++;count=s.getCount();seen=s;return new ItemStack(Items.BUCKET);} }
 static Item source,result,log,ore,tar;static CallbackFuel callback;static ItemMachineUpgrade[] speed=new ItemMachineUpgrade[4];static Field last;static Method brickCan;
 static Item register(String n,Item i){net.minecraftforge.fml.common.registry.ForgeRegistries.ITEMS.register(i.setRegistryName(new ResourceLocation("thermal_probe",n)));return i;}
 static <T extends TileEntityMachineBase>T bind(T f,boolean remote){ProbeWorld w=new ProbeWorld(remote);w.current=f;f.setWorld(w);f.setPos(new BlockPos(-19,70,31));return f;}
 static ProbeWorld w(TileEntity f){return (ProbeWorld)f.getWorld();}static void tick(TileEntityMachineBase f,long t){w(f).time=t;f.update();}
 static void air(boolean yes){com.hbm.dim.trait.CBT_Atmosphere a=new com.hbm.dim.trait.CBT_Atmosphere();a.breathable=yes;com.hbm.dim.CelestialBody.atmosphere=a;}
 static boolean can(Brick f)throws Exception{return (Boolean)brickCan.invoke(f);}
 static void setup()throws Exception {
  Bootstrap.register();for(Field f:ModItems.class.getFields())if(f.getType()==Item.class)f.set(null,register(f.getName(),new Item()));
  ModItems.powder_ash.setHasSubtypes(true);source=register("source",new Item());result=register("result",new Item());log=register("log",new Item());ore=register("ore",new Item());tar=register("tar",new Item());callback=(CallbackFuel)register("callback",new CallbackFuel());
  for(Item s:new Item[]{source,log,ore,tar}){ItemStack out=new ItemStack(result,3);NBTTagCompound tag=new NBTTagCompound();tag.setString("recipe","kept");out.setTagCompound(tag);FurnaceRecipes.instance().addSmeltingRecipe(new ItemStack(s),out,0);}
  OreDictionary.registerOre("logThermal",new ItemStack(log));OreDictionary.registerOre("oreThermal",new ItemStack(ore));OreDictionary.registerOre("anyTar",new ItemStack(tar));OreDictionary.registerOre("itemCoal",new ItemStack(Items.COAL));OreDictionary.registerOre("logWood",new ItemStack(Blocks.LOG,1,OreDictionary.WILDCARD_VALUE));
  OreDictionary.registerOre("fuelLignite",new ItemStack(ModItems.lignite));OreDictionary.registerOre("fuelCoke",new ItemStack(ModItems.coke));
  net.minecraftforge.fml.common.registry.GameRegistry.registerFuelHandler(new FuelHandler());net.minecraftforge.fml.common.registry.GameRegistry.registerFuelHandler(s->s.getItem()==callback?2400:0);
  for(int i=1;i<=3;i++)speed[i]=(ItemMachineUpgrade)register("speed"+i,new ItemMachineUpgrade(UpgradeType.SPEED,i));
  TileEntity.register("thermal_brick",Brick.class);TileEntity.register("thermal_iron",Iron.class);TileEntity.register("thermal_steel",Steel.class);
  last=TileEntityFurnaceSteel.class.getDeclaredField("lastItems");last.setAccessible(true);brickCan=TileEntityFurnaceBrick.class.getDeclaredMethod("canSmelt");brickCan.setAccessible(true);air(true);
 }
 static void firstSmoke()throws Exception {
  Brick b=bind(new Brick(),false);b.slots[0]=new ItemStack(source);b.slots[1]=new ItemStack(Items.COAL);tick(b,1);check(b.burnTime==1600&&b.progress==1&&b.slots[1]==null,"Brick complete tick burns last fuel");
  Iron i=bind(new Iron(),false);i.slots[0]=new ItemStack(source);i.slots[1]=new ItemStack(Items.COAL);tick(i,1);check(i.burnTime==1999&&i.progress==1,"Iron actual ModuleBurnTime/RecipesCommon cache multiplier");
  Steel s=bind(new Steel(),false);s.heat=50000;s.slots[0]=new ItemStack(source);Heat h=new Heat(50000);w(s).neighbors.put(s.getPos().down(),h);tick(s,1);check(s.progress[0]==1666&&s.heat==48334,"Steel actual heat/processing tick");
  System.out.println("PASS first complete-class thermal ticks: native recipes, last coal, actual iron fuel cache multiplier, steel heat processing.");
 }
 static void brick()throws Exception {
  ItemStack[] inputs={new ItemStack(source),new ItemStack(Blocks.COBBLESTONE),new ItemStack(Items.CLAY_BALL)};int[] rates={1,2,4};
  for(int j=0;j<3;j++){Brick b=bind(new Brick(),false);b.slots[0]=inputs[j];b.burnTime=1000;int n=200/rates[j];for(int t=1;t<n;t++){tick(b,t);check(b.progress==t*rates[j]&&b.slots[2]==null,"Brick speed progress "+rates[j]+"/"+t);}tick(b,n);check(b.progress==0&&b.slots[0]==null&&b.slots[2]!=null&&b.burnTime==1000-n,"Brick exact200 threshold "+rates[j]);}
  Brick b=bind(new Brick(),false);b.slots[0]=new ItemStack(source);b.slots[2]=new ItemStack(Items.DIAMOND);b.burnTime=20;b.progress=5;tick(b,1);check(b.burnTime==19&&b.progress==0,"Brick blocked output still decays and resets");
  b.slots[2]=null;b.progress=5;air(false);tick(b,2);check(b.burnTime==18&&b.progress==0,"Brick noair decays");air(true);
  b=bind(new Brick(),false);b.progress=37;b.burnTime=1;tick(b,1);check(b.progress==37&&b.burnTime==0,"Brick stale progress when outer guard false preserved");
  b=bind(new Brick(),false);b.slots[0]=new ItemStack(source);b.progress=199;b.burnTime=2;tick(b,1);check(b.slots[0]==null&&b.slots[2].getCount()==3&&b.burnTime==1,"Brick last input result count");
  ItemStack[] fuels={new ItemStack(Blocks.LOG),new ItemStack(Items.COAL),new ItemStack(Items.BLAZE_ROD),new ItemStack(Items.LAVA_BUCKET)};int[] burns={300,1600,2400,20000};
  for(int n=0;n<fuels.length;n++){b=bind(new Brick(),false);b.slots[0]=new ItemStack(source);b.slots[1]=fuels[n];ItemStack alias=b.slots[1];tick(b,1);check(alias.isEmpty()&&b.burnTime==burns[n],"Brick single fuel depletion "+n);check(n==3?b.slots[1].getItem()==Items.BUCKET:b.slots[1]==null,"Brick container residue "+n);check(b.ashLevelWood==(n==0?300:0)&&b.ashLevelCoal==(n==1?1600:0)&&b.ashLevelMisc==(n>=2?burns[n]-2000:0),"Brick postdepletion ash identity "+n);check(n<2?b.slots[3]==null:b.slots[3].getItemDamage()==EnumAshType.MISC.ordinal()&&b.slots[3].getCount()==1,"Brick one ash per fuel not while loop "+n);}
  b=bind(new Brick(),false);check(!b.ash(1999,EnumAshType.WOOD)&&b.ash(2000,EnumAshType.WOOD),"Brick2000 threshold");check(!b.ash(2000,EnumAshType.COAL),"Brick mismatched ash blocked");b.slots[3].setCount(64);check(!b.ash(2000,EnumAshType.WOOD),"Brick full ash blocked");
  b=bind(new Brick(),false);b.ashLevelWood=2000;b.ashLevelCoal=2000;b.slots[0]=new ItemStack(source);b.slots[1]=new ItemStack(Items.BLAZE_ROD);tick(b,1);check(b.slots[3].getItemDamage()==0&&b.ashLevelWood==0&&b.ashLevelCoal==2000&&b.ashLevelMisc==2400,"Brick wood coal misc order and blocked counters");
  b=bind(new Brick(),false);b.slots[0]=new ItemStack(source);b.slots[1]=new ItemStack(callback);ItemStack alias=b.slots[1];callback.calls=0;tick(b,1);check(callback.calls==1&&callback.count==0&&callback.seen==alias&&b.slots[1].getItem()==Items.BUCKET,"Brick exact mutated zero-count callback argument");
  b=bind(new Brick(),false);b.slots[0]=new ItemStack(source);b.slots[1]=new ItemStack(callback,2);callback.calls=0;tick(b,1);check(callback.calls==0&&b.slots[1].getCount()==1,"Brick no callback before stack depletion");
 }
 static void iron()throws Exception {
  int[] durations={160,134,107,80};
  for(int tier=0;tier<=3;tier++){Iron f=bind(new Iron(),false);f.slots[0]=new ItemStack(source);f.burnTime=10000;if(tier>0)f.slots[4]=new ItemStack(speed[tier]);for(int t=1;t<=durations[tier];t++)tick(f,t);check(f.processingTime==durations[tier]&&f.burnTime==10000-durations[tier]&&f.slots[0]==null&&f.slots[3].getCount()==3,"Iron duration/fuel "+tier);}
  Iron f=bind(new Iron(),false);f.slots[1]=new ItemStack(Items.COAL);tick(f,1);check(f.burnTime==2000&&f.progress==0&&f.slots[1]==null&&!f.wasOn,"Iron loads fuel without input but does not burn");f.slots[0]=new ItemStack(source);f.slots[3]=new ItemStack(result,64);tick(f,2);check(f.burnTime==2000,"Iron blocked output saves burn");f.slots[3]=null;air(false);tick(f,3);check(f.burnTime==2000&&!f.wasOn,"Iron noair saves burn");air(true);tick(f,4);check(f.burnTime==1999&&f.progress==1&&f.wasOn,"Iron resume uses one burn");
  f=bind(new Iron(),false);f.slots[1]=new ItemStack(Items.STICK);f.slots[2]=new ItemStack(Items.COAL);tick(f,1);check(f.burnTime==100&&f.slots[1]==null&&f.slots[2].getCount()==1,"Iron firstvalid fuel slot");
  f=bind(new Iron(),false);f.slots[1]=new ItemStack(Items.DIAMOND);f.slots[2]=new ItemStack(Items.COAL);tick(f,1);check(f.burnTime==2000&&f.slots[2]==null,"Iron skip invalid first fuel");
  Item[] fuels={ModItems.lignite,ModItems.coke,ModItems.solid_fuel,ModItems.rocket_fuel,ModItems.solid_fuel_bf};int[] expected={1500,4800,6400,12800,64000};for(int n=0;n<fuels.length;n++)check(f.burnModule.getBurnTime(new ItemStack(fuels[n]))==expected[n],"Iron original fuel multiplier "+n);
  f=bind(new Iron(),false);f.slots[1]=new ItemStack(Items.LAVA_BUCKET);tick(f,1);check(f.slots[1].getItem()==Items.BUCKET&&f.burnTime==20000,"Iron lava residue");
  f=bind(new Iron(),false);f.slots[1]=new ItemStack(callback);ItemStack alias=f.slots[1];callback.calls=0;tick(f,1);check(callback.calls==1&&callback.count==0&&callback.seen==alias,"Iron exact mutated zero-count callback argument");
  // Complete real cache uses singular comparable keys, ignoring count and tags.
  Field cache=FuelHandler.class.getDeclaredField("burnCache");cache.setAccessible(true);Map map=(Map)cache.get(null);map.clear();ItemStack coal=new ItemStack(Items.COAL,1);check(FuelHandler.getBurnTimeFromCache(coal)==1600&&map.size()==1,"cache first key");coal.setCount(63);coal.setTagCompound(new NBTTagCompound());coal.getTagCompound().setString("ignored","tag");check(FuelHandler.getBurnTimeFromCache(coal)==1600&&map.size()==1,"cache key singular count/NBT independence");check(FuelHandler.getBurnTimeFromCache(new ItemStack(Items.COAL,1,1))==1600&&map.size()==2,"cache metadata distinguishes coal charcoal");
 }
 static Steel hot(int heat){Steel s=bind(new Steel(),false);s.heat=heat;w(s).neighbors.put(s.getPos().down(),new Heat(heat));return s;}
 static void steel()throws Exception {
  Steel f=hot(33332);f.slots[0]=new ItemStack(source);check(!f.canSmelt(0),"Steel below33333 threshold");f.heat=33333;((Heat)w(f).neighbors.get(f.getPos().down())).heat=33333;tick(f,1);check(f.canSmelt(0)&&f.progress[0]==0&&f.heat==33333&&f.wasOn,"Steel threshold zero burn still active");
  f=hot(50000);for(int n=0;n<3;n++)f.slots[n]=new ItemStack(source);tick(f,1);check(Arrays.equals(f.progress,new int[]{1666,1666,1666})&&f.heat==45002,"Steel same burn reused sequentially for3 lanes");
  f=hot(33343);for(int n=0;n<3;n++)f.slots[n]=new ItemStack(source);tick(f,1);check(Arrays.equals(f.progress,new int[]{1,1,1})&&f.heat==33340,"Steel nearthreshold sequential debit");
  f=hot(50000);ItemStack input=new ItemStack(source,2);f.slots[0]=input;((ItemStack[])last.get(f))[0]=input;f.progress[0]=39999;tick(f,1);check(f.slots[3].getCount()==3&&f.slots[0]==input&&input.getCount()==1&&((ItemStack[])last.get(f))[0]==input&&f.progress[0]==0,"Steel40000 completion aliases not copies");
  f=hot(50000);input=new ItemStack(source);f.slots[0]=input;((ItemStack[])last.get(f))[0]=input;f.progress[0]=40000;tick(f,1);check(f.slots[0]==null&&((ItemStack[])last.get(f))[0]==input&&!input.isEmpty(),"Steel last input removal retains original lastItems positive alias");f.slots[0]=new ItemStack(source);tick(f,2);check(f.progress[0]>0,"Steel replacement equal identity continues");
  f=hot(50000);input=new ItemStack(ore);f.slots[0]=input;((ItemStack[])last.get(f))[0]=input;f.progress[0]=39999;f.bonus[0]=75;f.slots[3]=new ItemStack(result,61);tick(f,1);check(f.slots[3].getCount()==64&&f.bonus[0]==0,"Steel25 ore bonus saturation loses excess");
  for(Item in:new Item[]{log,tar}){f=hot(50000);input=new ItemStack(in);f.slots[0]=input;((ItemStack[])last.get(f))[0]=input;f.progress[0]=39999;f.bonus[0]=50;tick(f,1);check(f.slots[3].getCount()==6&&f.bonus[0]==0,"Steel50 log/tar bonus");}
  f=hot(33332);input=new ItemStack(source);f.slots[0]=input;((ItemStack[])last.get(f))[0]=input;f.progress[0]=40000;f.slots[3]=new ItemStack(Items.DIAMOND,64);tick(f,1);check(f.slots[0]==null&&f.slots[3].getItem()==Items.DIAMOND&&f.slots[3].getCount()==67&&!f.wasOn,"Steel completion outside canSmelt retained, even incompatible saturated output");
  f=hot(50000);f.slots[0]=new ItemStack(source);((ItemStack[])last.get(f))[0]=new ItemStack(Items.DIAMOND);f.progress[0]=39999;f.bonus[0]=80;tick(f,1);check(f.progress[0]==1666&&f.bonus[0]==0,"Steel changed item resets progress/bonus");
  f=bind(new Steel(),false);f.heat=12345;f.pull();check(f.heat==12333,"Steel cooling floor heat/1000");f.heat=1;f.pull();check(f.heat==0,"Steel cooling min1");f.heat=0;f.pull();check(f.heat==0,"Steel nonnegative cooling");
  f=hot(10);Heat h=(Heat)w(f).neighbors.get(f.getPos().down());h.heat=11;f.pull();check(f.heat==11&&h.used==1,"Steel diffusion ceil1");h.heat=11;f.pull();check(f.heat==11&&h.used==1,"Steel equal source no cooling");h.heat=0;f.pull();check(f.heat==10,"Steel cooler source cools not returns heat");f.heat=99999;h.heat=200000;f.pull();check(f.heat==100000&&h.used==5002,"Steel full computed source drain before clamp");int used=h.used;f.pull();check(f.heat==100000&&h.used==used,"Steel maxheat early return");
 }
 static void recipesAndSides()throws Exception {
  Brick b=bind(new Brick(),false);Iron i=bind(new Iron(),false);Steel s=hot(50000);i.burnTime=1;
  for(ItemStack absent:new ItemStack[]{null,ItemStack.EMPTY,new ItemStack(source,0)}){b.slots[0]=absent;i.slots[0]=absent;s.slots[0]=absent;check(!can(b)&&!i.canSmelt()&&!s.canSmelt(0),"all native absent input");}
  for(TileEntityMachineBase f:new TileEntityMachineBase[]{b,i,s})f.slots[0]=new ItemStack(Items.STICK);check(!can(b)&&!i.canSmelt()&&!s.canSmelt(0),"all native absent recipe");
  b.slots[0]=new ItemStack(source);i.slots[0]=new ItemStack(source);s.slots[0]=new ItemStack(source);
  for(int out:new int[]{0,61,62,64}){b.slots[2]=out==0?ItemStack.EMPTY:new ItemStack(result,out);i.slots[3]=out==0?ItemStack.EMPTY:new ItemStack(result,out);s.slots[3]=out==0?ItemStack.EMPTY:new ItemStack(result,out);check(can(b)==(out<=61)&&i.canSmelt()==(out<=61)&&s.canSmelt(0)==(out<=61),"multi-count capacity "+out);}
  for(EnumFacing face:EnumFacing.values()){
   check(Arrays.equals(b.getSlotsForFace(face),face==EnumFacing.UP?new int[]{0}:face==EnumFacing.DOWN?new int[]{2,1,3}:new int[]{1}),"Brick sided order "+face);
   check(Arrays.equals(i.getSlotsForFace(face),new int[]{0,1,2,3})&&Arrays.equals(s.getSlotsForFace(face),new int[]{0,1,2,3,4,5}),"Iron Steel sided slots "+face);
   check(!b.canExtractItem(2,new ItemStack(result),face)&&i.canExtractItem(3,new ItemStack(result),face)&&!i.canExtractItem(2,new ItemStack(result),face)&&s.canExtractItem(3,new ItemStack(result),face)&&!s.canExtractItem(2,new ItemStack(result),face),"original extraction policies "+face);
   check(b.canInsertItem(0,new ItemStack(Items.STICK),face)&&!b.canInsertItem(2,new ItemStack(result),face)&&i.canInsertItem(0,new ItemStack(source),face)&&!i.canInsertItem(0,new ItemStack(Items.STICK),face)&&s.canInsertItem(2,new ItemStack(source),face)&&!s.canInsertItem(3,new ItemStack(source),face),"original insertion policies "+face);
  }
 }
 static int count(Player p,Item item){int n=0;for(ItemStack s:p.inventory.mainInventory)if(!s.isEmpty()&&s.getItem()==item)n+=s.getCount();return n;}
 static void containers()throws Exception {
  TileEntityMachineBase[] fs={bind(new Brick(),false),bind(new Iron(),false),hot(50000)};int[] outputs={2,3,3};int[] sizes={4,5,6};
  for(int n=0;n<3;n++){TileEntityMachineBase f=fs[n];Player p=new Player(w(f));p.setPosition(-18.5,70.5,31.5);Container c=n==0?new ContainerFurnaceBrick(p.inventory,(Brick)f):n==1?new ContainerFurnaceIron(p.inventory,(Iron)f):new ContainerFurnaceSteel(p.inventory,(Steel)f);check(c.inventorySlots.size()==sizes[n]+36&&c.canInteractWith(p),"container slots/usability "+n);check(c.transferStackInSlot(p,outputs[n])==ItemStack.EMPTY,"container empty transfer "+n);
   f.slots[outputs[n]]=new ItemStack(result,7);SlotSmelting.changes=SlotSmelting.takes=0;ItemStack moved=c.transferStackInSlot(p,outputs[n]);check(moved.getCount()==7&&f.slots[outputs[n]]==null&&count(p,result)==7,"container output conservation "+n);check(SlotSmelting.changes==(n==0?0:1)&&SlotSmelting.takes==0,"original output callbacks only "+n);
   p.inventory.clear();for(int k=0;k<36;k++)p.inventory.setInventorySlotContents(k,new ItemStack(Items.STICK,64));p.inventory.setInventorySlotContents(0,new ItemStack(result,63));f.slots[outputs[n]]=new ItemStack(result,5);c.transferStackInSlot(p,outputs[n]);check(f.slots[outputs[n]].getCount()==4&&p.inventory.getStackInSlot(0).getCount()==64,"partial conservation "+n);check(c.transferStackInSlot(p,outputs[n])==ItemStack.EMPTY&&f.slots[outputs[n]].getCount()==4,"full inventory no loss "+n);
   p.inventory.clear();f.clear();p.inventory.setInventorySlotContents(9,new ItemStack(Items.COAL,8));c.transferStackInSlot(p,sizes[n]);check(f.slots[n==0?1:0].getCount()==8&&p.inventory.getStackInSlot(9).isEmpty(),"original plain Slot input precedence "+n);
   p.inventory.setInventorySlotContents(9,new ItemStack(source,5));if(n==0)f.slots[0]=null;else f.slots[0]=null;c.transferStackInSlot(p,sizes[n]);check(f.slots[0].getCount()==5&&p.inventory.getStackInSlot(9).isEmpty(),"input conservation "+n);
  }
 }
 static void persistenceAndSide()throws Exception {
  Brick b=bind(new Brick(),false);b.burnTime=7;b.maxBurnTime=11;b.progress=13;b.ashLevelWood=99;b.ashLevelCoal=98;b.ashLevelMisc=97;b.muffled=true;NBTTagCompound n=b.writeToNBT(new NBTTagCompound());check(n.hasKey("burnTime")&&n.hasKey("maxBurn")&&n.hasKey("progress")&&!n.hasKey("ashLevelWood")&&!n.hasKey("ashLevelCoal")&&!n.hasKey("ashLevelMisc"),"Brick NBT exact transient omissions");Brick b2=bind(new Brick(),false);b2.readFromNBT(n);check(b2.burnTime==7&&b2.maxBurnTime==11&&b2.progress==13&&b2.ashLevelWood==0,"Brick reload transient ash");ByteBuf buf=Unpooled.buffer();b.serialize(buf);check(buf.readableBytes()==13&&buf.readBoolean()&&buf.readInt()==7&&buf.readInt()==11&&buf.readInt()==13,"Brick packet prefix/order");buf.readerIndex(0);b2.deserialize(buf);check(buf.readableBytes()==0&&b2.progress==13,"Brick packet read");buf.release();
  Iron i=bind(new Iron(),false);i.maxBurnTime=17;i.burnTime=19;i.progress=23;i.processingTime=134;i.wasOn=true;i.muffled=true;n=i.writeToNBT(new NBTTagCompound());check(!n.hasKey("processingTime")&&!n.hasKey("wasOn")&&n.getInteger("maxBurnTime")==17,"Iron NBT transient omissions");Iron i2=bind(new Iron(),false);i2.readFromNBT(n);check(i2.maxBurnTime==17&&i2.burnTime==19&&i2.progress==23&&i2.processingTime==0&&!i2.wasOn,"Iron NBT read");buf=Unpooled.buffer();i.serialize(buf);check(buf.readableBytes()==18&&buf.readBoolean()&&buf.readInt()==17&&buf.readInt()==19&&buf.readInt()==23&&buf.readInt()==134&&buf.readBoolean(),"Iron packet exact order");buf.readerIndex(0);i2.deserialize(buf);check(buf.readableBytes()==0&&i2.processingTime==134&&i2.wasOn,"Iron packet read");buf.release();
  Steel s=hot(50000);s.progress=new int[]{3,5,7};s.bonus=new int[]{11,13,17};s.wasOn=true;s.muffled=true;ItemStack alias=new ItemStack(source);((ItemStack[])last.get(s))[1]=alias;n=s.writeToNBT(new NBTTagCompound());check(!n.hasKey("wasOn")&&n.getTagList("lastItems",10).getCompoundTagAt(0).getByte("lastItem")==1,"Steel NBT sparse lastItems and omitted wasOn");Steel s2=bind(new Steel(),false);s2.readFromNBT(n);check(Arrays.equals(s.progress,s2.progress)&&Arrays.equals(s.bonus,s2.bonus)&&s2.heat==50000&&!s2.wasOn&&((ItemStack[])last.get(s2))[1].isItemEqual(alias),"Steel NBT roundtrip");buf=Unpooled.buffer();s.serialize(buf);check(buf.readableBytes()==38&&buf.readBoolean()&&buf.readInt()==3&&buf.readInt()==3&&buf.readInt()==5&&buf.readInt()==7&&buf.readInt()==3&&buf.readInt()==11&&buf.readInt()==13&&buf.readInt()==17&&buf.readInt()==50000&&buf.readBoolean(),"Steel BufferUtil packet exact lengths/order");buf.readerIndex(0);s2.deserialize(buf);check(buf.readableBytes()==0&&s2.wasOn&&s2.heat==50000,"Steel packet read");buf.release();
  // No mutation on clients; particles still execute in real World dispatch.
  b=bind(new Brick(),true);b.burnTime=20;b.progress=9;tick(b,20);check(b.burnTime==20&&b.progress==9&&b.dirty==0,"Brick client state immutable");i=bind(new Iron(),true);i.burnTime=20;i.progress=10;tick(i,20);check(i.burnTime==20&&i.progress==10&&w(i).particles==2,"Iron client particle branch no process");s=bind(new Steel(),true);s.heat=50000;s.wasOn=true;tick(s,20);check(s.heat==50000&&w(s).particles>=1,"Steel client particle branch no heat process");
  i=bind(new Iron(),false);i.burnTime=20;i.slots[0]=new ItemStack(source);i.progress=14;tick(i,20);check(w(i).sounds==1&&PollutionHandler.amount==PollutionHandler.SOOT_PER_SECOND,"Iron native sound and pollution");check(PacketThreading.target.range==50&&PacketThreading.target.x==-19&&PacketThreading.target.y==70&&PacketThreading.target.z==31,"native network target");
 }
 public static void main(String[] args)throws Exception {setup();firstSmoke();brick();iron();steel();recipesAndSides();containers();persistenceAndSide();System.out.println("PASS thermal family: "+checks+" assertions.");}
}
