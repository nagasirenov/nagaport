import com.hbm.tileentity.TileEntityMachineBase;
import net.minecraft.init.Bootstrap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.profiler.Profiler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldProviderSurface;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.storage.SaveHandlerMP;
import net.minecraft.world.storage.WorldInfo;
import com.mojang.authlib.GameProfile;
import java.util.UUID;
import net.minecraft.init.Items;
import net.minecraft.inventory.Slot;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;

/** Full production base with native Slot, ItemStack and NBT. Not a running mod. */
public final class MachineCoreNativeTest {
    private static int checks;
    public static class Inventory extends TileEntityMachineBase {
        int dirty;
        @Override public void update() {}
        public Inventory() { this(130); }
        Inventory(int size) { super(size); }
        @Override public String getDefaultName() { return "container.soyuzCapsule"; }
        @Override public void markDirty() { dirty++; }
    }
    /** Real native World subclass, only the tile lookup is controlled for the distance test. */
    private static final class InventoryWorld extends World {
        TileEntity current;
        long time; int chunkDirty;
        java.util.Map<BlockPos,net.minecraft.block.state.IBlockState> states = new java.util.HashMap<>();
        @Override public long getTotalWorldTime(){return time;}
        @Override public void markChunkDirty(BlockPos p,TileEntity t){check(t==current && p.equals(t.getPos()),"chunk dirty owner/position"); chunkDirty++;}
        @Override public net.minecraft.block.state.IBlockState getBlockState(BlockPos p){return states.containsKey(p)?states.get(p):net.minecraft.init.Blocks.AIR.getDefaultState();}
        InventoryWorld() { this(false); }
        InventoryWorld(boolean remote) {
            super(new SaveHandlerMP(), new WorldInfo(new NBTTagCompound()), new WorldProviderSurface(), new Profiler(), remote);
            provider.setWorld(this);
        }
        @Override protected IChunkProvider createChunkProvider() { return null; }
        @Override protected boolean isChunkLoaded(int x, int z, boolean allowEmpty) { return false; }
        @Override public TileEntity getTileEntity(BlockPos pos) { return current != null && current.getPos().equals(pos) ? current : null; }
    }
    private static final class InventoryPlayer extends EntityPlayer {
        InventoryPlayer(World world) { super(world, new GameProfile(new UUID(0, 1), "InventoryTest")); }
        @Override public boolean isSpectator() { return false; }
        @Override public boolean isCreative() { return false; }
    }
    private static void check(boolean ok, String label) {
        checks++;
        if(!ok) throw new AssertionError(label);
    }
    private static NBTTagCompound entry(int slot, ItemStack stack) {
        NBTTagCompound n = new NBTTagCompound();
        stack.writeToNBT(n);
        n.setByte("slot", (byte)slot);
        return n;
    }
    public static void main(String[] args) {
        Bootstrap.register();
        TileEntity.register("hbm_inventory_boundary_test", Inventory.class);
        Inventory inv = new Inventory();
        ItemStack[] backing = inv.slots;
        check(inv.getSizeInventory() == 130 && inv.isEmpty(), "empty construction");
        check(inv.slots[0] == null && inv.getStackInSlot(0) == ItemStack.EMPTY, "nullable owner / native empty");
        check(inv.decrStackSize(0, 1) == ItemStack.EMPTY && inv.removeStackFromSlot(0) == ItemStack.EMPTY, "empty removal");
        inv.setInventorySlotContents(0, ItemStack.EMPTY);
        check(inv.slots[0] == null, "EMPTY ingress");
        inv.setInventorySlotContents(0, null);
        check(inv.slots[0] == null, "legacy null ingress");
        ItemStack alias = new ItemStack(Items.STICK, 80);
        inv.setInventorySlotContents(0, alias);
        check(alias.getCount() == 64 && inv.slots[0] == alias && inv.getStackInSlot(0) == alias, "same-object clamp");
        check(!inv.isEmpty(), "populated inventory");
        ItemStack part = inv.decrStackSize(0, 7);
        check(part != alias && part.getCount() == 7 && alias.getCount() == 57 && inv.slots[0] == alias, "split/remainder aliases");
        check(inv.decrStackSize(0, 0) == ItemStack.EMPTY && alias.getCount() == 57, "zero removal");
        check(inv.decrStackSize(0, 57) == alias && inv.slots[0] == null, "whole removal alias");
        inv.setInventorySlotContents(0, alias);
        check(inv.decrStackSize(0, 100) == alias && inv.slots[0] == null, "excess removal alias");
        inv.setInventorySlotContents(0, alias);
        check(inv.removeStackFromSlot(0) == alias && inv.slots[0] == null, "closing removal alias");
        inv.setInventorySlotContents(0, alias);
        inv.clear();
        check(inv.slots == backing && inv.isEmpty() && alias.getCount() == 57, "clear in place without mutating removed stacks");
        check(inv.dirty == 0, "legacy mutation methods do not mark dirty");
        Slot slot = new Slot(inv, 0, 0, 0);
        check(!slot.getHasStack() && slot.getStack() == ItemStack.EMPTY, "native Slot empty read");
        alias = new ItemStack(Items.STICK, 9);
        slot.putStack(alias);
        check(slot.getHasStack() && slot.getStack() == alias && inv.dirty == 1, "native Slot owns dirty notification");
        part = slot.decrStackSize(4);
        check(part.getCount() == 4 && alias.getCount() == 5 && inv.dirty == 1, "native Slot split");
        slot.putStack(ItemStack.EMPTY);
        check(inv.slots[0] == null && inv.dirty == 2, "native Slot empty insertion");
        check(!inv.hasCustomName() && inv.getName().equals("container.soyuzCapsule"), "default native name");
        check(inv.getDisplayName() instanceof TextComponentTranslation, "default translated display");
        inv.setCustomName("Capsule A");
        check(inv.hasCustomName() && inv.getName().equals("Capsule A"), "native custom name");
        check(inv.getDisplayName() instanceof TextComponentString && inv.getDisplayName().getUnformattedText().equals("Capsule A"), "literal custom display");
        check(inv.getInventoryName().equals(inv.getName()) && inv.hasCustomInventoryName(), "actual legacy GUI wrappers");
        check(inv.dirty == 3, "custom name marks dirty once");
        inv.setCustomName("");
        check(!inv.hasCustomName() && inv.getName().equals("container.soyuzCapsule"), "empty custom name fallback");
        inv.setCustomName(null);
        check(!inv.hasCustomName(), "null custom name fallback");
        for(EnumFacing side : EnumFacing.values()) {
            check(inv.getSlotsForFace(side).length == 0, "legacy empty default side slots");
            check(!inv.canInsertItem(0, alias, side) && !inv.canExtractItem(0, alias, side), "legacy side rejection");
        }
        inv.setField(4, 900);
        check(inv.getFieldCount() == 0 && inv.getField(4) == 0, "no native window fields");
        inv.openInventory(null); inv.closeInventory(null);
        ItemStack tagged = new ItemStack(Items.DYE, 6, 4);
        NBTTagCompound tag = new NBTTagCompound(); tag.setString("payload", "preserved"); tagged.setTagCompound(tag);
        inv.slots[0] = tagged;
        inv.slots[127] = new ItemStack(Items.STICK, 2);
        inv.slots[128] = new ItemStack(Items.STICK, 3);
        inv.slots[129] = ItemStack.EMPTY;
        inv.setCustomName("Saved name");
        NBTTagCompound out = new NBTTagCompound();
        check(inv.writeToNBT(out) == out, "NBT return identity");
        check(out.getTagList("items", 10).tagCount() == 3, "write nullable/EMPTY omission");
        check(out.getTagList("items", 10).getCompoundTagAt(2).getByte("slot") == -128, "legacy signed-byte encoding");
        Inventory loaded = new Inventory(); loaded.readFromNBT(out);
        check(loaded.slots[0] != tagged && loaded.slots[0].getCount() == 6 && loaded.slots[0].getMetadata() == 4, "native stack decode");
        check(loaded.slots[0].getTagCompound().getString("payload").equals("preserved"), "item NBT payload");
        check(loaded.slots[127].getCount() == 2 && loaded.slots[128] == null, "legacy signed-byte >=128 read rejection");
        check(loaded.getName().equals("Saved name") && loaded.dirty == 0, "load name without dirty");
        ItemStack survivor = loaded.slots[0];
        loaded.readFromNBT(new NBTTagCompound());
        check(loaded.slots[0] == survivor && !loaded.hasCustomName(), "missing items overlays; missing name clears name");
        NBTTagList list = new NBTTagList();
        list.appendTag(entry(1, new ItemStack(Items.STICK, 2)));
        list.appendTag(entry(1, new ItemStack(Items.DYE, 3, 5)));
        list.appendTag(entry(-1, new ItemStack(Items.STICK)));
        list.appendTag(entry(127, new ItemStack(Items.STICK)));
        NBTTagCompound patch = new NBTTagCompound(); patch.setTag("items", list);
        Inventory small = new Inventory(3); small.slots[0] = survivor; small.readFromNBT(patch);
        check(small.slots[0] == survivor && small.slots[1].getItem() == Items.DYE && small.slots[1].getCount() == 3, "overlay / duplicate last wins / invalid bounds ignored");
        NBTTagCompound invalid = new NBTTagCompound(); invalid.setByte("slot", (byte)1);
        NBTTagList invalidList = new NBTTagList(); invalidList.appendTag(invalid);
        patch.setTag("items", invalidList); small.readFromNBT(patch);
        check(small.slots[1] == null, "invalid native decoded stack normalizes null");
        Inventory edge = new Inventory(3);
        ItemStack negative = new ItemStack(Items.STICK, 5);
        edge.slots[0] = negative;
        check(edge.decrStackSize(0, -2) == ItemStack.EMPTY && negative.getCount() == 7 && edge.slots[0] == negative, "native negative split still increases remainder; no added clamp");
        ItemStack zero = new ItemStack(Items.STICK, 0);
        edge.slots[0] = zero;
        check(edge.getStackInSlot(0) == ItemStack.EMPTY && edge.isEmpty(), "zero-count backing is natively empty");
        check(edge.decrStackSize(0, 1) == ItemStack.EMPTY && edge.slots[0] == zero, "zero-count backing is not consumed by decrement");
        check(edge.removeStackFromSlot(0) == ItemStack.EMPTY && edge.slots[0] == null, "zero-count remove normalizes and clears");
        edge.slots[0] = ItemStack.EMPTY;
        check(edge.decrStackSize(0, 1) == ItemStack.EMPTY && edge.slots[0] == ItemStack.EMPTY, "direct EMPTY backing decrement");
        check(edge.removeStackFromSlot(0) == ItemStack.EMPTY && edge.slots[0] == null, "direct EMPTY backing removal");
        edge.setInventorySlotContents(0, new ItemStack(Items.STICK, -1));
        check(edge.slots[0] == null, "negative-count ingress natively empty");
        edge.slots[0]=new ItemStack(Items.STICK,5);check(edge.decrStackSize(0,Integer.MIN_VALUE)==ItemStack.EMPTY&&edge.slots[0]==null,"overflow split native empty normalization");
        InventoryWorld world = new InventoryWorld();
        InventoryPlayer player = new InventoryPlayer(world);
        edge.setWorld(world); edge.setPos(new BlockPos(10, 20, 30)); world.current = edge;
        player.setPosition(18.5, 20.5, 38.5);
        check(player.getDistanceSq(10.5, 20.5, 30.5) == 128 && edge.isUsableByPlayer(player), "exact squared distance 128 included");
        player.setPosition(18.5001, 20.5, 38.5);
        check(!edge.isUsableByPlayer(player), "beyond squared distance 128 excluded");
        player.setPosition(10.5, 20.5, 30.5);
        check(edge.isUsableByPlayer(player), "native usability dispatch");
        world.current = new Inventory();
        check(!edge.isUsableByPlayer(player), "replaced tile rejected even at center");
        world.current = null;
        check(!edge.isUsableByPlayer(player), "missing tile rejected");
        check(ItemStack.EMPTY.isEmpty() && ItemStack.EMPTY.getCount() == 0, "EMPTY sentinel not mutated");
        extraCoreTests();
        proxyTests();
        System.out.println("PASS: " + checks + " complete production MachineBase + LoadedBase with native Slot/ItemStack/NBT/World/Block/Netty APIs; controlled NTM collaborators, not live mod");
    }

    // @PROXY_FIXTURES@
    static class TileEntityPWRController extends Inventory { ItemStack incoming; @Override public void setInventorySlotContents(int slot,ItemStack stack){incoming=stack;super.setInventorySlotContents(slot,stack);} }
    static class LegacyInventory extends TileEntityPWRController { @Override public ItemStack getStackInSlot(int slot){return slots[slot];} @Override public ItemStack decrStackSize(int slot,int n){return slots[slot]==null?null:super.decrStackSize(slot,n);} @Override public ItemStack removeStackFromSlot(int slot){return slots[slot]==null?null:super.removeStackFromSlot(slot);} }
    private static void proxyTests(){
        Proxy p=new Proxy();Combo c=new Combo();PWR w=new PWR();
        for(Inventory inv:new Inventory[]{p,c,w})check(inv.getStackInSlot(0)==ItemStack.EMPTY&&inv.decrStackSize(0,1)==ItemStack.EMPTY&&inv.removeStackFromSlot(0)==ItemStack.EMPTY,"missing proxy native EMPTY");
        LegacyInventory core=new LegacyInventory();p.core=core;c.core=core;w.core=core;
        for(Inventory inv:new Inventory[]{p,c,w}){
            check(inv.getStackInSlot(0)==ItemStack.EMPTY&&inv.decrStackSize(0,1)==ItemStack.EMPTY&&inv.removeStackFromSlot(0)==ItemStack.EMPTY,"legacy nullable delegate adapted");
            ItemStack alias=new ItemStack(Items.STICK,7);inv.setInventorySlotContents(0,alias);check(core.slots[0]==alias&&inv.getStackInSlot(0)==alias,"proxy live ingress/read identity");
            check(inv.decrStackSize(0,3).getCount()==3&&alias.getCount()==4,"proxy native decrement dispatch");
            check(inv.removeStackFromSlot(0)==alias&&core.slots[0]==null,"proxy native removal dispatch identity");
            inv.setInventorySlotContents(0,null);check(core.incoming==ItemStack.EMPTY&&core.slots[0]==null,"proxy null outbound normalized EMPTY");
        }
        c.inventory=false;core.slots[0]=new ItemStack(Items.STICK,5);c.setInventorySlotContents(0,ItemStack.EMPTY);
        check(c.getStackInSlot(0)==ItemStack.EMPTY&&c.removeStackFromSlot(0)==ItemStack.EMPTY&&core.slots[0].getCount()==5,"Combo disabled read/removal/setter retain gates");
        check(c.decrStackSize(0,1).getCount()==1&&core.slots[0].getCount()==4,"Combo legacy ungated decrement retained");
        w.metadata=0;check(w.getStackInSlot(0)==ItemStack.EMPTY&&w.decrStackSize(0,1)==ItemStack.EMPTY&&w.removeStackFromSlot(0)==ItemStack.EMPTY,"PWR metadata gate");w.metadata=1;w.block=null;check(w.getStackInSlot(0)==ItemStack.EMPTY,"PWR missing block gate");
    }
    private static final java.util.List<String> events = new java.util.ArrayList<>();
    private static final class Neighbor extends net.minecraft.block.Block {
        final String label; final boolean solid,weak;
        Neighbor(String label,boolean solid,boolean weak){super(net.minecraft.block.material.Material.ROCK);this.label=label;this.solid=solid;this.weak=weak;}
        @Override public void onNeighborChange(net.minecraft.world.IBlockAccess w,BlockPos at,BlockPos from){events.add(label+":tile:"+at+":"+from);}
        @Override public void neighborChanged(net.minecraft.block.state.IBlockState state,World w,BlockPos at,net.minecraft.block.Block block,BlockPos from){check(state.getBlock()==this,"callback correct state"); events.add(label+":block:"+at+":"+from);}
        @Override public boolean isNormalCube(net.minecraft.block.state.IBlockState s,net.minecraft.world.IBlockAccess w,BlockPos p){events.add(label+":solid"); return solid;}
        @Override public boolean getWeakChanges(net.minecraft.world.IBlockAccess w,BlockPos p){events.add(label+":weak"); return weak;}
    }
    private static final class MutatingNeighbor extends net.minecraft.block.Block {
        final boolean far;
        MutatingNeighbor(boolean far){super(net.minecraft.block.material.Material.ROCK);this.far=far;}
        @Override public boolean getWeakChanges(net.minecraft.world.IBlockAccess w,BlockPos at){((InventoryWorld)w).states.put(at,net.minecraft.init.Blocks.GLASS.getDefaultState());return true;}
        @Override public void onNeighborChange(net.minecraft.world.IBlockAccess w,BlockPos at,BlockPos from){((InventoryWorld)w).states.put(at,net.minecraft.init.Blocks.STONE.getDefaultState());}
        @Override public void neighborChanged(net.minecraft.block.state.IBlockState state,World w,BlockPos at,net.minecraft.block.Block block,BlockPos from){check(state==w.getBlockState(at)&&state.getBlock()==net.minecraft.init.Blocks.STONE,"captured original block callback sees state refreshed after tile callback");((InventoryWorld)w).states.put(at,net.minecraft.init.Blocks.DIRT.getDefaultState());events.add(far?"far-mutated":"near-mutated");}
        @Override public boolean isNormalCube(net.minecraft.block.state.IBlockState state,net.minecraft.world.IBlockAccess w,BlockPos at){check(state==w.getBlockState(at)&&state.getBlock()==net.minecraft.init.Blocks.DIRT,"solidity sees state refreshed after block callback, with captured original block");return true;}
    }
    private static void extraCoreTests(){
        Inventory masked=new Inventory(){public ItemStack getStackInSlot(int slot){return ItemStack.EMPTY;}};ItemStack backing=new ItemStack(Items.STICK,7);masked.slots[0]=backing;
        check(masked.decrStackSize(0,2).getCount()==2&&backing.getCount()==5,"decrement uses backing storage rather than overridden getter");check(masked.removeStackFromSlot(0)==backing&&masked.slots[0]==null,"removal uses backing identity rather than overridden getter");
        Inventory inv=new Inventory(); InventoryWorld world=new InventoryWorld();
        inv.setWorld(world);inv.setPos(new BlockPos(7,8,9));world.current=inv;
        inv.markChanged();check(world.chunkDirty==1 && inv.dirty==0,"markChanged chunk save only");
        check(inv.isLoaded(),"initial loaded flag true");inv.onChunkUnload();check(!inv.isLoaded(),"unload flag false");inv.onLoad();check(!inv.isLoaded(),"inherited onLoad does not reset existing lifecycle");
        inv.muffled=true;check(inv.getVolume(5)==0.5F,"muffled factor");
        NBTTagCompound saved=inv.writeToNBT(new NBTTagCompound());Inventory loaded=new Inventory();loaded.readFromNBT(saved);check(loaded.muffled,"muffled saved with machine NBT");
        io.netty.buffer.ByteBuf buf=io.netty.buffer.Unpooled.buffer();inv.serialize(buf);check(buf.readableBytes()==1 && buf.readBoolean(),"muffled packet byte first");buf.clear();buf.writeBoolean(false);inv.deserialize(buf);check(!inv.muffled,"muffled packet decode");buf.release();
        final int[] audio=new int[2];final com.hbm.sound.AudioWrapper replacement=new com.hbm.sound.AudioWrapper(){public void startSound(){audio[1]++;}};
        Inventory sounding=new Inventory(){public com.hbm.sound.AudioWrapper createAudioLoop(){return replacement;}};
        check(sounding.rebootAudio(new com.hbm.sound.AudioWrapper(){public void stopSound(){audio[0]++;}})==replacement && audio[0]==1 && audio[1]==1,"audio stop/create/start preserves identity");
        world.provider.setDimension(42);world.time=1;inv.networkPackNT(64);
        check(com.hbm.handler.threading.PacketThreading.calls==1,"first packet sent");
        io.netty.buffer.ByteBuf first=com.hbm.handler.threading.PacketThreading.packet.getCompiledBuffer();
        check(first.getInt(0)==7 && first.getInt(4)==8 && first.getInt(8)==9 && !first.getBoolean(12),"packet coordinate/order/payload");
        net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint target=com.hbm.handler.threading.PacketThreading.target;
        check(target.dimension==42 && target.x==7 && target.y==8 && target.z==9 && target.range==64,"native dimension/target coordinates/range");
        inv.networkPackNT(64);check(com.hbm.handler.threading.PacketThreading.calls==1,"duplicate packet suppressed");
        world.time=20;inv.networkPackNT(64);check(com.hbm.handler.threading.PacketThreading.calls==2,"global twentieth tick resends duplicate");
        world.time=21;inv.muffled=true;inv.networkPackNT(64);check(com.hbm.handler.threading.PacketThreading.calls==3,"changed byte sends before global tick");
        check(first.refCnt()==1,"existing buffer lifetime retained, not silently repaired");
        Inventory client=new Inventory();client.setWorld(new InventoryWorld(true));client.networkPackNT(64);check(com.hbm.handler.threading.PacketThreading.calls==3,"client packet path returns before serialization");
        Inventory custom=new Inventory(){public void serialize(io.netty.buffer.ByteBuf b){super.serialize(b);b.writeInt(23);b.writeLong(987L);}};
        custom.muffled=true;io.netty.buffer.ByteBuf payload=io.netty.buffer.Unpooled.buffer();custom.serialize(payload);
        check(payload.readBoolean()&&payload.readInt()==23&&payload.readLong()==987L,"custom progress/power channel remains after muffled byte");payload.release();
        ItemStack tooMany=new ItemStack(Items.STICK,128);inv.slots[0]=tooMany;Inventory countLoaded=new Inventory();countLoaded.readFromNBT(inv.writeToNBT(new NBTTagCompound()));check(countLoaded.slots[0]==null,"signed byte stack Count128 becomes native empty on load, no setter clamp during write");
        for(boolean solid:new boolean[]{false,true})for(boolean weak:new boolean[]{false,true}){
            events.clear();world.states.clear();
            BlockPos near=new BlockPos(8,8,9),far=new BlockPos(9,8,9);
            world.states.put(near,new Neighbor("near",solid,false).getDefaultState());world.states.put(far,new Neighbor("far",false,weak).getDefaultState());
            inv.updateRedstoneConnection(new com.hbm.util.fauxpointtwelve.DirPos(8,8,9,com.hbm.lib.ForgeDirection.EAST));
            check(events.get(0).equals("near:tile:"+near+":"+inv.getPos()) && events.get(1).equals("near:block:"+near+":"+inv.getPos()) && events.get(2).equals("near:solid"),"direct callbacks then solidity order");
            check(events.size()==(solid?(weak?6:4):3),"solid/weak propagation gate");
            if(solid)check(events.get(3).equals("far:weak"),"weak queried after solid");
            if(solid&&weak)check(events.get(4).equals("far:tile:"+far+":"+inv.getPos())&&events.get(5).equals("far:block:"+far+":"+inv.getPos()),"far callbacks keep original tile position");
        }
        events.clear();world.states.clear();world.states.put(new BlockPos(8,8,9),new MutatingNeighbor(false).getDefaultState());world.states.put(new BlockPos(9,8,9),new MutatingNeighbor(true).getDefaultState());
        inv.updateRedstoneConnection(new com.hbm.util.fauxpointtwelve.DirPos(8,8,9,com.hbm.lib.ForgeDirection.EAST));check(events.equals(java.util.Arrays.asList("near-mutated","far-mutated")),"mutation preserves captured near/far block identity and order");
        com.hbm.dim.trait.CBT_Atmosphere at=new com.hbm.dim.trait.CBT_Atmosphere();com.hbm.dim.CelestialBody.atmosphere=at;
        com.hbm.handler.atmosphere.AtmosphereBlob blob=new com.hbm.handler.atmosphere.AtmosphereBlob();com.hbm.handler.atmosphere.ChunkAtmosphereManager.proxy.blobs.add(blob);
        check(!inv.breatheAir(6),"absent breathable atmosphere");at.breathable=true;blob.breathable=true;check(inv.breatheAir(6)&&blob.consumed==0,"ambient air precedes blob consumption");at.breathable=false;check(inv.breatheAir(6)&&blob.consumed==6,"fallback blob consumed once");
    }
}
