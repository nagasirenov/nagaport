import com.hbm.tileentity.machine.TileEntityMachineElectricFurnace;
import com.hbm.inventory.container.ContainerElectricFurnace;
import com.hbm.inventory.UpgradeManagerNT;
import com.hbm.inventory.SlotSmelting;
import com.hbm.inventory.SlotUpgrade;
import com.hbm.items.machine.ItemMachineUpgrade;
import com.hbm.items.machine.ItemMachineUpgrade.UpgradeType;
import com.hbm.items.machine.ItemBattery;
import com.hbm.items.ModItems;
import com.hbm.blocks.machine.MachineElectricFurnace;
import com.hbm.handler.pollution.PollutionHandler;
import com.hbm.handler.threading.PacketThreading;
import com.hbm.furnaceprobe.Observations;
import com.hbm.lib.Library;
import api.hbm.tile.ILoadedTile.TileAccessCache;
import api.hbm.energymk2.Nodespace;
import api.hbm.energymk2.IEnergyConductorMK2;
import net.minecraft.init.Bootstrap;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraft.world.WorldProviderSurface;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.storage.SaveHandlerMP;
import net.minecraft.world.storage.WorldInfo;
import net.minecraft.profiler.Profiler;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import com.mojang.authlib.GameProfile;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.util.*;
import java.io.*;
import java.lang.reflect.*;

/** Complete real production classes. World and listed NTM collaborators are controlled. */
public final class ElectricFurnaceNativeTest {
    static int checks;
    static void check(boolean value,String label){checks++;if(!value)throw new AssertionError(label);}
    static final class ProbeWorld extends World {
        long time; TileEntity current; Map<BlockPos,TileEntity> neighbors=new HashMap<>();
        ProbeWorld(boolean remote){super(new SaveHandlerMP(),new WorldInfo(new NBTTagCompound()),new WorldProviderSurface(),new Profiler(),remote);provider.setWorld(this);}
        @Override protected IChunkProvider createChunkProvider(){return null;}
        @Override protected boolean isChunkLoaded(int x,int z,boolean e){return false;}
        @Override public long getTotalWorldTime(){return time;}
        @Override public TileEntity getTileEntity(BlockPos p){return current!=null&&current.getPos().equals(p)?current:neighbors.get(p);}
        @Override public net.minecraft.block.state.IBlockState getBlockState(BlockPos p){return net.minecraft.init.Blocks.AIR.getDefaultState();}
    }
    public static class Furnace extends TileEntityMachineElectricFurnace {
        int dirty,limit=64;
        @Override public void markDirty(){dirty++;Observations.events.add("dirty");}
        @Override public int getInventoryStackLimit(){return limit;}
    }
    static final class Player extends EntityPlayer {
        Player(World w){super(w,new GameProfile(new UUID(0,1),"FurnaceProbe"));}
        @Override public boolean isSpectator(){return false;}
        @Override public boolean isCreative(){return false;}
    }
    static final class Conductor extends TileEntity implements IEnergyConductorMK2 {}
    static final class MutableProvider extends TileEntity implements com.hbm.tileentity.IUpgradeInfoProvider {
        HashMap<UpgradeType,Integer> valid=new HashMap<>();
        public HashMap<UpgradeType,Integer> getValidUpgrades(){return valid;}
        public boolean canProvideInfo(UpgradeType t,int l,boolean e){return true;}
        public void provideInfo(UpgradeType t,int l,List<String> i,boolean e){}
    }
    static ItemBattery battery;
    static Item source,result;
    static ItemMachineUpgrade[] speed=new ItemMachineUpgrade[4],power=new ItemMachineUpgrade[4];
    static Item register(String name,Item item){net.minecraftforge.fml.common.registry.ForgeRegistries.ITEMS.register(item.setRegistryName(new net.minecraft.util.ResourceLocation("furnace_probe",name)));return item;}
    static Field cooldown;
    static int cooldown(Furnace f)throws Exception{return cooldown.getInt(f);}
    static Furnace furnace(boolean remote){Furnace f=new Furnace();ProbeWorld w=new ProbeWorld(remote);w.current=f;f.setWorld(w);f.setPos(new BlockPos(-19,70,31));TileAccessCache.cache.clear();return f;}
    static ProbeWorld world(Furnace f){return (ProbeWorld)f.getWorld();}
    static void tick(Furnace f,long t){world(f).time=t;f.update();}
    static void resetObservations(){Observations.events.clear();MachineElectricFurnace.calls=0;PollutionHandler.calls=0;PacketThreading.calls=0;com.hbm.util.Compat.calls=0;}
    static void ordinary()throws Exception {
        Furnace f=furnace(false);f.slots[1]=new ItemStack(net.minecraft.init.Blocks.IRON_ORE);f.power=5000;resetObservations();
        for(int t=1;t<=99;t++){tick(f,t);check(f.progress==t&&f.power==5000-t*50,"ordinary intermediate progress/debit "+t);check(f.slots[1].getCount()==1&&f.slots[2]==null,"no premature conversion "+t);}
        tick(f,100);check(f.progress==0&&f.power==0&&f.slots[1]==null,"100th tick consumes one input and 5000 energy");
        check(f.slots[2].getItem()==Items.IRON_INGOT&&f.slots[2].getCount()==1,"native vanilla recipe output");
        check(PollutionHandler.calls==5&&PollutionHandler.amount==PollutionHandler.SOOT_PER_SECOND,"soot every20processing ticks");
        check(PollutionHandler.world==world(f)&&PollutionHandler.x==-19&&PollutionHandler.y==70&&PollutionHandler.z==31,"pollution world/native position");
        check(PacketThreading.target.range==50&&PacketThreading.target.x==-19&&PacketThreading.target.y==70&&PacketThreading.target.z==31,"native packet target");
        check(!MachineElectricFurnace.active&&f.dirty==100,"last-item completion deactivates and dirty ticks retained");
        f=furnace(false);f.power=10000;f.slots[1]=new ItemStack(net.minecraft.init.Blocks.IRON_ORE,2);f.progress=99;resetObservations();tick(f,20);
        check(f.slots[1].getCount()==1&&f.slots[2].getCount()==1&&f.power==9950&&f.progress==0,"completion count with next input");
        check(MachineElectricFurnace.calls==0&&f.dirty==1,"legacy completion skips block toggle when next input ready");
        check(Observations.events.equals(Arrays.asList("pollution","packet","dirty")),"completion pollution before packet before dirty");
        f=furnace(false);f.power=50;f.slots[1]=new ItemStack(net.minecraft.init.Blocks.IRON_ORE);resetObservations();tick(f,20);
        check(f.power==0&&f.progress==1&&cooldown(f)==0,"post-debit power exhaustion doesn't retroactively cancel progress");
        check(Observations.events.equals(Arrays.asList("pollution","block:true","packet","dirty")),"processing side-effect order");
        tick(f,21);check(f.progress==0&&cooldown(f)==20,"next insufficient tick resets progress and cooldown");
    }
    static void cooldownAndClient()throws Exception {
        Furnace f=furnace(false);f.slots[1]=new ItemStack(net.minecraft.init.Blocks.IRON_ORE);tick(f,1);check(cooldown(f)==20&&!f.canProcess(),"low power enters cooldown");
        f.power=100000;
        for(int i=1;i<=19;i++){tick(f,i+1);check(f.progress==0&&f.power==100000&&cooldown(f)==20-i,"cooldown decrement before test "+i);}
        tick(f,21);check(f.progress==1&&f.power==99950&&cooldown(f)==0,"20th powered cooldown tick resumes");
        f.power=0;tick(f,22);tick(f,23);check(cooldown(f)==20,"persistent shortage resets20 rather than decrements to resume");
        f=furnace(true);f.power=123;f.progress=7;f.maxProgress=41;f.consumption=9;cooldown.setInt(f,12);f.slots[0]=new ItemStack(battery);f.slots[1]=new ItemStack(net.minecraft.init.Blocks.IRON_ORE);resetObservations();tick(f,40);
        check(f.power==123&&f.progress==7&&f.maxProgress==41&&f.consumption==9&&cooldown(f)==12,"client tick read-only fields");
        check(!f.slots[0].hasTagCompound()&&Observations.events.isEmpty()&&PacketThreading.calls==0&&com.hbm.util.Compat.calls==0,"client no battery/network/pollution/block/dirty effects");
        f=furnace(false);f.progress=51;f.power=100000;f.slots[1]=new ItemStack(Items.STICK);tick(f,1);check(f.progress==0&&f.power==100000,"invalid recipe cancels without debit");
        f.slots[1]=new ItemStack(net.minecraft.init.Blocks.IRON_ORE);f.slots[2]=new ItemStack(Items.DIAMOND);f.progress=51;tick(f,2);check(f.progress==0&&f.power==100000,"incompatible output resets");
    }
    static void recipesAndLimits()throws Exception {
        Furnace f=furnace(false);check(!f.canProcess(),"null input");f.slots[1]=ItemStack.EMPTY;check(!f.canProcess(),"native EMPTY input");
        f.slots[1]=new ItemStack(source);ItemStack consumedAlias=f.slots[1];check(f.canProcess(),"registered native multi-count recipe");f.power=100000;f.progress=99;tick(f,1);
        check(f.slots[1]==null&&consumedAlias.isEmpty()&&consumedAlias.getItem()==Items.AIR,"depleted input alias loses native identity, backing becomes null");
        check(f.slots[2].getCount()==3&&f.slots[2].getTagCompound().getString("recipe").equals("kept"),"copy full recipe count and tags");
        check(f.slots[2]!=FurnaceRecipes.instance().getSmeltingResult(new ItemStack(source)),"output is recipe copy");
        ItemStack original=f.slots[2];original.getTagCompound().setString("existing","keep");f.slots[1]=new ItemStack(source);f.progress=99;tick(f,2);
        check(f.slots[2]==original&&original.getCount()==6&&original.getTagCompound().getString("existing").equals("keep"),"equal merge preserves output alias and NBT");
        f.slots[1]=new ItemStack(source);f.slots[2]=new ItemStack(result,63);f.progress=99;tick(f,3);check(f.slots[2].getCount()==66,"legacy doesn't reserve whole recipe result count");
        f.slots[1]=new ItemStack(source);check(!f.canProcess(),"overfull output rejects later processing");f.slots[2]=new ItemStack(result,64);check(!f.canProcess(),"full output rejected");
        f.limit=1;f.slots[2]=new ItemStack(result,20);check(f.canProcess(),"legacy OR fallback permits inventory-limit excess");
        f.slots[2]=ItemStack.EMPTY;check(f.canProcess(),"native EMPTY output maps absence");
        f.slots[1]=new ItemStack(source,0);check(f.slots[1].isEmpty()&&f.slots[1].getItem()==Items.AIR&&!f.canProcess(),"native zero-count loses item identity, no old resurrection");
        f.slots[1]=new ItemStack(source,-1);check(!f.canProcess(),"negative native input rejected, no clamp introduced");
        for(EnumFacing face:EnumFacing.values()){
            check(Arrays.equals(f.getSlotsForFace(face),new int[]{0,1,2}),"sided slot order "+face);
            check(f.canInsertItem(1,new ItemStack(source),face)&&!f.canInsertItem(1,new ItemStack(Items.STICK),face),"native recipe validation "+face);
            check(f.canInsertItem(0,new ItemStack(battery),face)&&!f.canInsertItem(2,new ItemStack(result),face)&&!f.canInsertItem(3,new ItemStack(speed[1]),face),"slot policies "+face);
            ItemStack empty=ItemBattery.getEmptyBattery(battery);check(f.canExtractItem(0,empty,face),"empty battery extract "+face);battery.setCharge(empty,1);check(!f.canExtractItem(0,empty,face),"charged battery reject "+face);
            check(f.canExtractItem(2,new ItemStack(result),face)&&!f.canExtractItem(1,new ItemStack(source),face),"output-only extraction "+face);
        }
    }
    static void upgrades()throws Exception {
        for(int tier=1;tier<=3;tier++)for(boolean fast:new boolean[]{false,true}){
            Furnace f=furnace(false);f.power=100000;f.slots[1]=new ItemStack(net.minecraft.init.Blocks.IRON_ORE);f.slots[3]=new ItemStack(fast?speed[tier]:power[tier]);tick(f,1);
            int duration=fast?100-tier*25:100+tier*10, cost=fast?50+tier*50:50-tier*15;
            check(f.maxProgress==duration&&f.consumption==cost&&f.power==100000-cost,"tier formula "+tier+" "+fast);
            for(int t=2;t<=duration;t++)tick(f,t);
            check(f.slots[1]==null&&f.slots[2].getCount()==1&&f.power==100000-(long)duration*cost,"exact upgraded completion "+tier+" "+fast);
        }
        Furnace f=furnace(false);ItemStack stack=new ItemStack(speed[1]);f.slots[3]=stack;f.upgradeManager.checkSlots(f,f.slots,3,3);
        check(f.upgradeManager.cachedSlots[0]==stack,"cache stores same stack reference");speed[1].tier=3;f.upgradeManager.checkSlots(f,f.slots,3,3);check(f.upgradeManager.getLevel(UpgradeType.SPEED)==1,"same-reference mutation not observed");
        f.slots[3]=stack.copy();f.upgradeManager.checkSlots(f,f.slots,3,3);check(f.upgradeManager.getLevel(UpgradeType.SPEED)==3,"replacement identity invalidates cache");speed[1].tier=1;
        f.slots[3]=null;f.upgradeManager.checkSlots(f,f.slots,3,3);check(f.upgradeManager.getLevel(UpgradeType.SPEED)==0,"removal clears upgrade");
        MutableProvider p=new MutableProvider();p.valid.put(UpgradeType.SPEED,3);UpgradeManagerNT m=new UpgradeManagerNT(p);
        m.checkSlots(new ItemStack[]{new ItemStack(speed[2]),new ItemStack(speed[2])},0,1);check(m.getLevel(UpgradeType.SPEED)==3,"sum capped at maximum");
        p.valid.put(UpgradeType.LM_SMELTER,1);p.valid.put(UpgradeType.LM_SHREDDER,1);
        ItemMachineUpgrade low=new ItemMachineUpgrade(UpgradeType.LM_SMELTER,1),high=new ItemMachineUpgrade(UpgradeType.LM_SHREDDER,1);register("mutex_low",low);register("mutex_high",high);
        m.checkSlots(new ItemStack[]{new ItemStack(low),new ItemStack(high)},0,1);check(m.getLevel(UpgradeType.LM_SHREDDER)==1&&m.getLevel(UpgradeType.LM_SMELTER)==0,"mutex highest ordinal wins");
        m.checkSlots(new ItemStack[]{null,null},0,1);m.checkSlots(new ItemStack[]{new ItemStack(low),null},0,1);check(m.getLevel(UpgradeType.LM_SMELTER)==0,"legacy mutexType persists after removal, lower never returns");
        m.checkSlots(new ItemStack[]{new ItemStack(high),null},0,1);check(m.getLevel(UpgradeType.LM_SHREDDER)==0,"same mutex ordinal stays absent after cache rebuild");
        f=furnace(false);f.slots[3]=new ItemStack(speed[1]);f.upgradeManager.checkSlots(f,f.slots,3,3);f.upgradeManager.upgrades.put(UpgradeType.SPEED,4);f.power=100000;f.slots[1]=new ItemStack(net.minecraft.init.Blocks.IRON_ORE);tick(f,1);
        check(f.maxProgress==0&&f.progress==0&&f.slots[2].getCount()==1,"externally mutated cached level retains zero-duration formula");
        try{f.getProgressScaled(10);throw new AssertionError("zero divisor hidden");}catch(ArithmeticException expected){checks++;}
        f.upgradeManager.upgrades.put(UpgradeType.SPEED,Integer.MAX_VALUE);f.slots[1]=new ItemStack(net.minecraft.init.Blocks.IRON_ORE);f.power=100000;tick(f,2);
        check(f.maxProgress==100-Integer.MAX_VALUE*25&&f.consumption==50+Integer.MAX_VALUE*50,"signed int upgrade overflow retained");
        List<String> info=new ArrayList<>();f.provideInfo(UpgradeType.POWER,3,info,true);check(info.size()==3&&info.get(1).contains("-90%")&&info.get(2).contains("+30%"),"actual upgrade labels/formulas");
        check(ItemMachineUpgrade.class.getMethod("addInformation",ItemStack.class,World.class,List.class,net.minecraft.client.util.ITooltipFlag.class).getReturnType()==void.class,"native tooltip override signature");
    }
    static void energyAndConnections()throws Exception {
        Furnace f=furnace(false);f.slots[0]=new ItemStack(battery);f.slots[1]=new ItemStack(net.minecraft.init.Blocks.IRON_ORE);resetObservations();tick(f,40);
        check(f.power==150&&battery.getCharge(f.slots[0])==9800&&f.progress==1&&cooldown(f)==0,"real concrete battery charges before processing and cooldown");
        check(com.hbm.util.Compat.calls==6&&Observations.events.subList(0,6).equals(Arrays.asList("subscribe:-19,69,31","subscribe:-19,71,31","subscribe:-19,70,30","subscribe:-19,70,32","subscribe:-20,70,31","subscribe:-18,70,31")),"six connection directions, coordinates, order");
        resetObservations();tick(f,41);check(com.hbm.util.Compat.calls==0,"connection refresh only40 modulo");
        Conductor c=new Conductor();world(f).neighbors.put(f.getPos().down(),c);TileAccessCache.cache.clear();Nodespace.selected=new Nodespace.PowerNode();Nodespace.selected.net=new Nodespace.Net();tick(f,80);
        check(Nodespace.selected.net.received==1&&Nodespace.selected.net.receiver==f,"actual receiver subscribes through loaded cache/conductor boundary");Nodespace.selected=null;
        Furnace running=furnace(false);running.slots[0]=ItemBattery.getFullBattery(battery);running.slots[1]=new ItemStack(net.minecraft.init.Blocks.IRON_ORE);
        for(int t=1;t<=100;t++)tick(running,t);
        check(10000-battery.getCharge(running.slots[0])==running.power+5000,"100tick real battery debit equals machine receipt plus consumption");
        check(running.slots[1]==null&&running.slots[2].getItem()==Items.IRON_INGOT&&running.slots[2].getCount()==1,"100tick battery powered input/output conservation");
        f=furnace(false);f.power=99999;f.slots[0]=ItemBattery.getFullBattery(battery);tick(f,1);check(f.power==100000&&battery.getCharge(f.slots[0])==9999,"battery bounded by remaining capacity");
        f.power=100001;tick(f,2);check(f.power==100000&&battery.getCharge(f.slots[0])==10000,"legacy negative free capacity charges source back");
        f.slots[0]=new ItemStack(ModItems.battery_creative);f.power=0;f.slots[1]=new ItemStack(net.minecraft.init.Blocks.IRON_ORE);tick(f,3);check(f.power==99950,"creative source fills capacity before consumption");
        f=furnace(false);check(f.transferPower(100001)==1&&f.power==100000,"receiver overcapacity remainder");check(f.transferPower(0)==0&&f.power==100000,"receiver zero no change");check(f.transferPower(-5)==0&&f.power==99995,"negative inherited receiver transfer retained");
        f.power=1;check(f.transferPower(Long.MAX_VALUE)==0&&f.power==Long.MIN_VALUE,"signed long receiver overflow retained");
        f.power=Long.MAX_VALUE;check(f.getPowerScaled(2)==(Long.MAX_VALUE*2)/100000,"scaled power signed overflow");
        f.progress=Integer.MAX_VALUE;f.maxProgress=100;check(f.getProgressScaled(2)==(Integer.MAX_VALUE*2)/100,"scaled progress signed overflow");
        for(long initialPower:new long[]{Long.MIN_VALUE,-1,0,99999,100000,100001,Long.MAX_VALUE})
            for(long initialCharge:new long[]{-1,0,199,200,10000,Long.MAX_VALUE}) {
                Furnace matrix=furnace(false);matrix.power=initialPower;matrix.slots[0]=new ItemStack(battery);battery.setCharge(matrix.slots[0],initialCharge);
                long moved=Math.min(Math.min(100000-initialPower,200),initialCharge);tick(matrix,1);
                check(matrix.power==initialPower+moved,"real tick signed/capacity transfer destination");
                check(battery.getCharge(matrix.slots[0])==initialCharge-moved,"real tick signed/capacity transfer source");
                check(matrix.power+battery.getCharge(matrix.slots[0])==initialPower+initialCharge,"transfer conserves wrapping long sum");
            }
        ItemBattery zero=(ItemBattery)register("zero_battery",new ItemBattery(0,0,0));ItemStack zeroStack=new ItemStack(zero);
        check(Library.chargeTEFromItems(new ItemStack[]{zeroStack},0,0,100000)==0&&zero.getCharge(zeroStack)==0,"zero-capacity/rate battery moves no energy");
        check(Double.isNaN(zero.getDurabilityForDisplay(zeroStack)),"inherited zero-capacity durability NaN not repaired");
        ItemStack b=ItemBattery.getFullBattery(battery);long charge=battery.getCharge(b);check(Library.chargeTEFromItems(new ItemStack[]{b},0,100000,100000)==100000&&battery.getCharge(b)==charge,"zero capacity no source debit");
    }
    static void nbtAndPacket()throws Exception {
        Furnace f=furnace(false);f.slots[1]=new ItemStack(source,2);f.slots[0]=ItemBattery.getFullBattery(battery);battery.setCharge(f.slots[0],8765);f.power=Long.MAX_VALUE-5;f.progress=43;f.maxProgress=75;f.consumption=100;f.muffled=true;f.setCustomName("Furnace saved");cooldown.setInt(f,17);
        NBTTagCompound n=new NBTTagCompound();check(f.writeToNBT(n)==n,"complete NBT writer return identity");
        check(n.getLong("power")==f.power&&n.getInteger("progress")==43&&n.getBoolean("muffled")&&n.getString("name").equals("Furnace saved"),"inherited and furnace keys");
        check(!n.hasKey("cooldown")&&!n.hasKey("maxProgress")&&!n.hasKey("consumption"),"original transient values not persisted");
        ByteArrayOutputStream bytes=new ByteArrayOutputStream();CompressedStreamTools.writeCompressed(n,bytes);Furnace loaded=furnace(false);loaded.readFromNBT(CompressedStreamTools.readCompressed(new ByteArrayInputStream(bytes.toByteArray())));
        check(loaded.power==f.power&&loaded.progress==43&&loaded.muffled&&loaded.getName().equals("Furnace saved"),"complete native compressed NBT roundtrip");
        check(loaded.slots[1].getCount()==2&&battery.getCharge(loaded.slots[0])==8765&&cooldown(loaded)==0&&loaded.maxProgress==100&&loaded.consumption==50,"item charge/count and transient defaults");
        ItemStack survivor=loaded.slots[1];loaded.readFromNBT(new NBTTagCompound());check(loaded.power==0&&loaded.progress==0&&!loaded.muffled&&loaded.slots[1]==survivor,"missing fields default but base inventory overlays");
        f.progress=200003;f.maxProgress=300007;
        ByteBuf b=Unpooled.buffer();f.serialize(b);check(b.readableBytes()==17&&b.readBoolean()&&b.readLong()==f.power&&b.readInt()==300007&&b.readInt()==200003,"actual bool/long/int/int payload order");b.readerIndex(0);loaded.deserialize(b);check(loaded.muffled&&loaded.power==f.power&&loaded.maxProgress==300007&&loaded.progress==200003,"actual deserialize");b.release();
        resetObservations();world(f).time=1;f.networkPackNT(50);check(PacketThreading.calls==1,"initial production packet send");
        b=PacketThreading.packet.getCompiledBuffer().duplicate();check(b.readInt()==-19&&b.readInt()==70&&b.readInt()==31&&b.readBoolean()&&b.readLong()==f.power&&b.readInt()==300007&&b.readInt()==200003,"packet coordinates before furnace payload");
        world(f).time=2;f.networkPackNT(50);check(PacketThreading.calls==1,"identical packet suppressed");world(f).time=20;f.networkPackNT(50);check(PacketThreading.calls==2,"unchanged modulo20 resend retained");f.progress++;world(f).time=21;f.networkPackNT(50);check(PacketThreading.calls==3,"changed packet immediate");
    }
    static int count(Player p,Item item){int n=0;for(ItemStack s:p.inventory.mainInventory)if(!s.isEmpty()&&s.getItem()==item)n+=s.getCount();return n;}
    static void container()throws Exception {
        Furnace f=furnace(false);Player p=new Player(world(f));p.setPosition(-18.5,70.5,31.5);ContainerElectricFurnace c=new ContainerElectricFurnace(p.inventory,f);
        check(c.inventorySlots.size()==40&&c.getSlot(0).getSlotIndex()==0&&c.getSlot(1).getSlotIndex()==1&&c.getSlot(2) instanceof SlotSmelting&&c.getSlot(3) instanceof SlotUpgrade,"complete container layout and slot types");
        net.minecraft.block.state.IBlockState north=net.minecraft.init.Blocks.FURNACE.getDefaultState().withProperty(net.minecraft.block.BlockFurnace.FACING,EnumFacing.NORTH);
        net.minecraft.block.state.IBlockState south=north.withProperty(net.minecraft.block.BlockFurnace.FACING,EnumFacing.SOUTH);
        check(!f.shouldRefresh(world(f),f.getPos(),north,south),"metadata-only native facing change retains furnace tile");
        check(f.shouldRefresh(world(f),f.getPos(),north,net.minecraft.init.Blocks.LIT_FURNACE.getDefaultState()),"different off/on block still requests native refresh");
        check(f.shouldRefresh(world(f),f.getPos(),north,net.minecraft.init.Blocks.AIR.getDefaultState()),"removal still requests native refresh");
        check(c.canInteractWith(p),"native usable distance");p.setPosition(100,70,31);check(!c.canInteractWith(p),"native unusable distance");
        check(c.transferStackInSlot(p,4)==ItemStack.EMPTY,"empty shift-click returns native sentinel");
        p.inventory.setInventorySlotContents(9,new ItemStack(battery));ItemStack transferred=c.transferStackInSlot(p,4);check(transferred.getItem()==battery&&f.slots[0].getItem()==battery&&p.inventory.getStackInSlot(9).isEmpty(),"battery routing native merge");
        p.inventory.setInventorySlotContents(9,new ItemStack(speed[2]));c.transferStackInSlot(p,4);check(f.slots[3].getItem()==speed[2]&&p.inventory.getStackInSlot(9).isEmpty(),"upgrade routing native merge");
        p.inventory.setInventorySlotContents(9,new ItemStack(net.minecraft.init.Blocks.IRON_ORE,10));c.transferStackInSlot(p,4);check(f.slots[1].getCount()==10&&p.inventory.getStackInSlot(9).isEmpty(),"recipe input routing native merge");
        p.inventory.setInventorySlotContents(9,new ItemStack(Items.STICK,8));check(c.transferStackInSlot(p,4)==ItemStack.EMPTY&&p.inventory.getStackInSlot(9).getCount()==8&&f.slots[1].getCount()==10,"invalid recipe rejected without loss");
        f.slots[2]=new ItemStack(Items.IRON_INGOT,7);SlotSmelting.takes=0;SlotSmelting.changes=0;transferred=c.transferStackInSlot(p,2);
        check(transferred.getCount()==7&&f.slots[2]==null&&count(p,Items.IRON_INGOT)==7,"output shift transfer exact counts and native clear");
        check(SlotSmelting.takes==0&&SlotSmelting.changes==1,"original transfer has onSlotChange but no added onTake/event callback");
        p.inventory.clear();for(int i=0;i<36;i++)p.inventory.setInventorySlotContents(i,new ItemStack(Items.STICK,64));p.inventory.setInventorySlotContents(0,new ItemStack(Items.IRON_INGOT,63));f.slots[2]=new ItemStack(Items.IRON_INGOT,5);c.transferStackInSlot(p,2);
        check(f.slots[2].getCount()==4&&p.inventory.getStackInSlot(0).getCount()==64,"partial merge conserves items");
        check(c.transferStackInSlot(p,2)==ItemStack.EMPTY&&f.slots[2].getCount()==4,"full inventory rejects without loss");
        check(f.provideContainer(0,p,world(f),0,0,0) instanceof ContainerElectricFurnace,"actual provider builds native container");
    }
    public static void main(String[] args)throws Exception {
        Bootstrap.register();TileEntity.register("furnace_probe",Furnace.class);TileEntity.register("furnace_actual_probe",TileEntityMachineElectricFurnace.class);
        cooldown=TileEntityMachineElectricFurnace.class.getDeclaredField("cooldown");cooldown.setAccessible(true);
        battery=(ItemBattery)register("battery",new ItemBattery(10000,200,200));register("creative",ModItems.battery_creative);
        source=register("source",new Item());result=register("result",new Item());ItemStack resultStack=new ItemStack(result,3);NBTTagCompound tag=new NBTTagCompound();tag.setString("recipe","kept");resultStack.setTagCompound(tag);FurnaceRecipes.instance().addSmeltingRecipe(new ItemStack(source),resultStack,0.7F);
        for(int t=1;t<=3;t++){speed[t]=(ItemMachineUpgrade)register("speed"+t,new ItemMachineUpgrade(UpgradeType.SPEED,t));power[t]=(ItemMachineUpgrade)register("power"+t,new ItemMachineUpgrade(UpgradeType.POWER,t));}
        ordinary();cooldownAndClient();recipesAndLimits();upgrades();energyAndConnections();nbtAndPacket();container();
        System.out.println("PASS electric furnace native core: "+checks+" assertions. Complete production furnace/container/upgrades/batteries/bases, controlled NTM collaborators; no live gameplay claim.");
    }
}
