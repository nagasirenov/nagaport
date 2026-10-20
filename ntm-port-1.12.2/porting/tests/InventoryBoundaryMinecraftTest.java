import com.hbm.tileentity.TileEntityInventoryBase;
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
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;

/** Full production base with native Slot, ItemStack and NBT. Not a running mod. */
public final class InventoryBoundaryMinecraftTest {
    private static int checks;
    public static final class Inventory extends TileEntityInventoryBase {
        int dirty;
        public Inventory() { this(130); }
        Inventory(int size) { super(size); }
        @Override protected String getDefaultName() { return "container.soyuzCapsule"; }
        @Override public void markDirty() { dirty++; }
    }
    /** Real native World subclass, only the tile lookup is controlled for the distance test. */
    private static final class InventoryWorld extends World {
        TileEntity current;
        InventoryWorld() {
            super(new SaveHandlerMP(), new WorldInfo(new NBTTagCompound()), new WorldProviderSurface(), new Profiler(), false);
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
            check(inv.getSlotsForFace(side).length == 1 && inv.getSlotsForFace(side)[0] == 0, "legacy default side slot");
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
        InventoryWorld world = new InventoryWorld();
        InventoryPlayer player = new InventoryPlayer(world);
        edge.setWorld(world); edge.setPos(new BlockPos(10, 20, 30)); world.current = edge;
        player.setPosition(18.5, 20.5, 38.5);
        check(player.getDistanceSq(10.5, 20.5, 30.5) == 128 && edge.isUsableByPlayer(player), "exact squared distance 128 included");
        player.setPosition(18.5001, 20.5, 38.5);
        check(!edge.isUsableByPlayer(player), "beyond squared distance 128 excluded");
        player.setPosition(10.5, 20.5, 30.5);
        check(edge.isUseableByPlayer(player), "legacy usability wrapper delegates native method");
        world.current = new Inventory();
        check(!edge.isUsableByPlayer(player), "replaced tile rejected even at center");
        world.current = null;
        check(!edge.isUsableByPlayer(player), "missing tile rejected");
        check(ItemStack.EMPTY.isEmpty() && ItemStack.EMPTY.getCount() == 0, "EMPTY sentinel not mutated");
        System.out.println("PASS: " + checks + " full production InventoryBase/native Slot/ItemStack/NBT checks; no furnace gameplay claim");
    }
}
