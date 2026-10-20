import api.hbm.energymk2.IBatteryItem;
import com.hbm.items.machine.ItemBatteryCreative;
import java.util.Random;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Bootstrap;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;

/** Uses actual MC/Forge types. ItemBattery/Library classes here are extracted method fixtures. */
public final class BatteryEnergyNativeTest {
    private static int checks;
    private static void check(boolean condition, String message) {
        checks++;
        if (!condition) throw new AssertionError(message);
    }
    private static final class RecordingBattery extends Item implements IBatteryItem {
        long charge, max = 1000, chargeRate = 80, dischargeRate = 60;
        String calls = "";
        public void chargeBattery(ItemStack stack, long value) { calls += "charge(" + value + ");"; charge += value; }
        public void dischargeBattery(ItemStack stack, long value) { calls += "discharge(" + value + ");"; charge -= value; }
        public void setCharge(ItemStack stack, long value) { charge = value; }
        public long getCharge(ItemStack stack) { calls += "get;"; return charge; }
        public long getMaxCharge(ItemStack stack) { calls += "max;"; return max; }
        public long getChargeRate(ItemStack stack) { calls += "rateIn;"; return chargeRate; }
        public long getDischargeRate(ItemStack stack) { calls += "rateOut;"; return dischargeRate; }
        public String getChargeTagName() { return "customEnergy"; }
    }
    public static void main(String[] args) throws Exception {
        Bootstrap.register();
        ItemBattery battery = new ItemBattery(5000, 100, 60);
        net.minecraftforge.fml.common.registry.ForgeRegistries.ITEMS.register(battery.setRegistryName(new ResourceLocation("hbm_test", "battery_fixture")));
        ItemStack fresh = new ItemStack(battery);
        check(!fresh.hasTagCompound(), "native fresh stack has no compound");
        check(battery.getCharge(fresh) == 5000 && fresh.getTagCompound().getLong("charge") == 5000, "missing NBT initializes full");
        ItemStack existing = new ItemStack(battery);
        existing.setTagCompound(new NBTTagCompound());
        check(battery.getCharge(existing) == 0, "existing empty compound reads zero");
        existing.getTagCompound().setString("timer", "keep");
        battery.setCharge(existing, 123);
        check(existing.getTagCompound().getString("timer").equals("keep"), "set retains unrelated NBT");
        battery.chargeBattery(existing, 17);
        battery.dischargeBattery(existing, 40);
        check(battery.getCharge(existing) == 100, "charge and discharge accounting");
        java.io.ByteArrayOutputStream savedBytes = new java.io.ByteArrayOutputStream();
        net.minecraft.nbt.CompressedStreamTools.writeCompressed(existing.writeToNBT(new NBTTagCompound()), savedBytes);
        ItemStack loaded = new ItemStack(net.minecraft.nbt.CompressedStreamTools.readCompressed(new java.io.ByteArrayInputStream(savedBytes.toByteArray())));
        check(loaded.getItem() == battery && battery.getCharge(loaded) == 100, "native ItemStack NBT save/reload energy");
        check(loaded.getTagCompound().getString("timer").equals("keep"), "native save/reload auxiliary NBT");
        battery.setCharge(loaded, 99);
        check(battery.getCharge(existing) == 100, "native save/reload isolates NBT");
        ItemStack absentCharge = new ItemStack(battery);
        battery.chargeBattery(absentCharge, 7);
        check(battery.getCharge(absentCharge) == 7, "charge absent tag starts at amount, not full");
        ItemStack absentDischarge = new ItemStack(battery);
        battery.dischargeBattery(absentDischarge, 7);
        check(battery.getCharge(absentDischarge) == 4993, "discharge absent tag starts at capacity");
        battery.setCharge(existing, Long.MAX_VALUE);
        battery.chargeBattery(existing, 1);
        check(battery.getCharge(existing) == Long.MIN_VALUE, "charge preserves signed overflow");
        battery.dischargeBattery(existing, 1);
        check(battery.getCharge(existing) == Long.MAX_VALUE, "discharge preserves signed overflow");
        ItemStack ordinary = new ItemStack(Items.STICK);
        battery.chargeBattery(ordinary, 1);
        battery.setCharge(ordinary, 1);
        battery.dischargeBattery(ordinary, 1);
        check(battery.getCharge(ordinary) == 0 && !ordinary.hasTagCompound(), "nonbattery is unchanged");
        check(ItemBattery.getEmptyBattery(Items.STICK) == null, "legacy invalid empty helper remains null");
        check(ItemBattery.getFullBattery(Items.STICK).getItem() == Items.STICK, "legacy nonbattery full helper remains ordinary stack");
        check(battery.getCharge(ItemBattery.getEmptyBattery(battery)) == 0, "empty helper");
        check(battery.getCharge(ItemBattery.getFullBattery(battery)) == 5000, "full helper");
        check(battery.showDurabilityBar(fresh) && battery.getDurabilityForDisplay(fresh) == 0D, "full durability display");
        battery.setCreativeTab(CreativeTabs.MISC);
        NonNullList<ItemStack> creative = NonNullList.create();
        battery.getSubItems(CreativeTabs.MISC, creative);
        check(creative.size() == 2 && battery.getCharge(creative.get(0)) == 0 && battery.getCharge(creative.get(1)) == 5000, "native creative callback empty then full");
        creative.clear();
        battery.getSubItems(CreativeTabs.BUILDING_BLOCKS, creative);
        check(creative.isEmpty(), "native creative tab guard");
        battery.getSubItems(CreativeTabs.SEARCH, creative);
        check(creative.size() == 2, "native search tab includes battery");
        ItemBattery dischargeOnly = new ItemBattery(100, 0, 10);
        dischargeOnly.setCreativeTab(CreativeTabs.MISC);
        creative.clear();
        dischargeOnly.getSubItems(CreativeTabs.MISC, creative);
        check(creative.size() == 1 && dischargeOnly.getCharge(creative.get(0)) == 100, "discharge-only creative full entry");
        ItemBattery chargeOnly = new ItemBattery(100, 10, 0);
        chargeOnly.setCreativeTab(CreativeTabs.MISC);
        creative.clear();
        chargeOnly.getSubItems(CreativeTabs.MISC, creative);
        check(creative.size() == 1 && chargeOnly.getCharge(creative.get(0)) == 0, "charge-only creative empty entry");
        RecordingBattery recording = new RecordingBattery();
        ItemStack custom = new ItemStack(recording, 3, 7);
        custom.setTagCompound(new NBTTagCompound());
        custom.getTagCompound().setLong("customEnergy", 456);
        custom.getTagCompound().setString("discard", "old");
        ItemStack empty = IBatteryItem.emptyBattery(custom);
        check(empty != custom && empty.getCount() == 3 && empty.getMetadata() == 7, "interface empty copy count/metadata");
        check(empty.getTagCompound().getLong("customEnergy") == 0 && !empty.getTagCompound().hasKey("discard"), "interface custom key replaces NBT");
        check(custom.getTagCompound().getLong("customEnergy") == 456 && custom.getTagCompound().hasKey("discard"), "interface source NBT unchanged");
        empty.getTagCompound().setLong("customEnergy", 12);
        check(custom.getTagCompound().getLong("customEnergy") == 456, "interface no aliasing");
        check(IBatteryItem.emptyBattery((ItemStack)null) == null && IBatteryItem.emptyBattery(ItemStack.EMPTY) == null && IBatteryItem.emptyBattery(Items.STICK) == null, "interface invalid legacy returns");
        ItemStack[] slots = {new ItemStack(recording)};
        LibraryTransferFixture.creativeIdentity = new ItemBatteryCreative();
        recording.charge = 400;
        long result = LibraryTransferFixture.chargeTEFromItems(slots, 0, 950, 1000);
        check(result == 1000 && recording.charge == 350, "battery debit equals machine receipt");
        check(recording.calls.equals("get;rateOut;discharge(50);"), "battery-to-machine exact call order");
        recording.calls = "";
        result = LibraryTransferFixture.chargeItemsFromTE(slots, 0, 1000, 1000);
        check(result == 920 && recording.charge == 430, "machine debit equals battery receipt");
        check(recording.calls.equals("max;get;rateIn;charge(80);"), "machine-to-battery exact call order");
        recording.calls = "";
        LibraryTransferFixture.chargeTEFromItems(slots, 0, 1000, 1000);
        check(recording.calls.equals("get;rateOut;discharge(0);"), "zero outgoing still invokes mutation");
        recording.calls = "";
        LibraryTransferFixture.chargeItemsFromTE(slots, 0, 0, 1000);
        check(recording.calls.equals("max;get;rateIn;charge(0);"), "zero incoming still invokes mutation");
        check(LibraryTransferFixture.chargeItemsFromTE(null, -99, -1, 1000) == 0, "negative clamp precedes array access");
        check(LibraryTransferFixture.chargeItemsFromTE(null, -99, 1001, 1000) == 1000, "over-capacity clamp precedes array access");
        boolean throwsOnDischarge = false;
        try { LibraryTransferFixture.chargeTEFromItems(null, -99, -1, 1000); } catch (NullPointerException expected) { throwsOnDischarge = true; }
        check(throwsOnDischarge, "battery-to-machine has no early clamp");
        check(LibraryTransferFixture.chargeTEFromItems(new ItemStack[]{null}, 0, 9, 10) == 9, "null slot unchanged");
        check(LibraryTransferFixture.chargeTEFromItems(new ItemStack[]{ItemStack.EMPTY}, 0, 9, 10) == 9, "native empty slot unchanged");
        check(LibraryTransferFixture.chargeItemsFromTE(new ItemStack[]{ordinary}, 0, 9, 10) == 9, "nonbattery slot unchanged");
        ItemStack[] creativeSlot = {new ItemStack(LibraryTransferFixture.creativeIdentity)};
        check(LibraryTransferFixture.chargeTEFromItems(creativeSlot, 0, 0, 1000) == 1000, "creative identity fills machine");
        check(LibraryTransferFixture.chargeItemsFromTE(creativeSlot, 0, 500, 1000) == 0, "creative identity drains machine");
        check(LibraryTransferFixture.chargeItemsFromTE(creativeSlot, 0, 1001, 1000) == 1000, "clamp before creative branch");
        ItemStack[] sameClassDifferentItem = {new ItemStack(new ItemBatteryCreative())};
        check(LibraryTransferFixture.chargeTEFromItems(sameClassDifferentItem, 0, 0, Long.MAX_VALUE) == Long.MAX_VALUE / 100L, "creative bypass uses identity not class");
        recording.charge = 400;
        recording.dischargeRate = 60;
        result = LibraryTransferFixture.chargeTEFromItems(slots, 0, 1100, 1000);
        check(result == 1000 && recording.charge == 500, "original overfilled machine reverses transfer, no new clamp");
        recording.charge = 1100;
        recording.max = 1000;
        recording.chargeRate = 80;
        result = LibraryTransferFixture.chargeItemsFromTE(slots, 0, 400, 1000);
        check(result == 500 && recording.charge == 1000, "original overfilled battery reverses transfer, no new clamp");
        Random random = new Random(0xBA77E);
        long[] edge = {Long.MIN_VALUE, Long.MAX_VALUE, -1, 0, 1, 50, 999, 1000, 1001};
        for (int n = 0; n < 2000; n++) {
            long power = n < edge.length ? edge[n] : random.nextLong();
            long capacity = n < edge.length ? 1000 : random.nextLong();
            long before = n < edge.length ? 400 : random.nextLong();
            recording.charge = before;
            recording.dischargeRate = n < edge.length ? 60 : random.nextLong();
            recording.calls = "";
            long delta = Math.min(Math.min(capacity - power, recording.dischargeRate), before);
            long received = LibraryTransferFixture.chargeTEFromItems(slots, 0, power, capacity);
            check(received == power + delta && recording.charge == before - delta, "outgoing signed arithmetic oracle " + n);
            check(received - power == before - recording.charge, "outgoing modulo-long accounting " + n);
            recording.charge = before;
            recording.max = random.nextLong();
            recording.chargeRate = random.nextLong();
            recording.calls = "";
            delta = Math.min(Math.min(power, recording.chargeRate), recording.max - before);
            long expected = power < 0 ? 0 : power > capacity ? capacity : power - delta;
            long debited = LibraryTransferFixture.chargeItemsFromTE(slots, 0, power, capacity);
            check(debited == expected, "incoming signed arithmetic oracle " + n);
            if (power < 0 || power > capacity) check(recording.charge == before && recording.calls.isEmpty(), "incoming early clamp no battery mutation " + n);
            else check(recording.charge == before + delta && power - debited == recording.charge - before, "incoming modulo-long accounting " + n);
        }
        System.out.println("PASS: " + checks + " native battery/transfer assertions; no simulation API exists; ItemStack persistence only, no world/network acceptance");
    }
}
