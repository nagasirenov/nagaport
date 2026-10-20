// Reconstructed after workspace loss. See recovery-status.json; no current native run is implied.
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import net.minecraft.init.Bootstrap;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

/** Real Minecraft/Forge values exercise extracted production storage statements.
 * These small hosts are not entity replacements and do not implement native APIs.
 */
public final class NativeTransportNbtTest {
    private static int checks;
    private static Field rawCount;
    private static void check(boolean condition, String label) {
        if (!condition) throw new AssertionError(label);
        checks++;
    }
    private interface Operation { void run() throws Exception; }
    private static void fails(Class<? extends Throwable> expected, Operation operation, String label) throws Exception {
        try { operation.run(); } catch (Throwable e) {
            if (e instanceof InvocationTargetException) e = ((InvocationTargetException)e).getCause();
            check(expected.isInstance(e), label + ": wrong failure " + e); return;
        }
        throw new AssertionError(label + ": did not fail");
    }
    private static NBTTagCompound record(String slotKey, int slot, String id, int count, int damage) {
        NBTTagCompound result = new NBTTagCompound();
        if (slotKey != null) result.setByte(slotKey, (byte)slot);
        if (id != null) result.setString("id", id);
        result.setByte("Count", (byte)count);
        result.setShort("Damage", (short)damage);
        NBTTagCompound tag = new NBTTagCompound(); tag.setString("custom", "retained");
        result.setTag("tag", tag);
        return result;
    }
    private static NBTTagCompound root(String listKey, int count, NBTTagCompound... records) {
        NBTTagCompound result = new NBTTagCompound();
        NBTTagList list = new NBTTagList();
        for (NBTTagCompound record : records) list.appendTag(record);
        result.setTag(listKey, list); result.setInteger("count", count);
        result.setString("unknown", "untouched"); return result;
    }
    private static final class ArrayHost {
        final String name, listKey, slotKey;
        final boolean resets;
        final Object host;
        final Field storage;
        final Method read, write;
        ArrayHost(String name, String field, String listKey, String slotKey, boolean resets) throws Exception {
            this.name = name; this.listKey = listKey; this.slotKey = slotKey; this.resets = resets;
            Class<?> type = Class.forName("ExtractedTransportNbt$" + name);
            host = type.newInstance(); storage = type.getDeclaredField(field); storage.setAccessible(true);
            read = type.getMethod("read", NBTTagCompound.class); write = type.getMethod("write", NBTTagCompound.class);
        }
        ItemStack[] get() throws Exception { return (ItemStack[])storage.get(host); }
        void set(ItemStack[] value) throws Exception { storage.set(host, value); }
        void load(NBTTagCompound value) throws Exception { read.invoke(host, value); }
        void save(NBTTagCompound value) throws Exception { write.invoke(host, value); }
        NBTTagCompound data(NBTTagCompound... values) { return root(listKey, 18, values); }
        NBTTagCompound item(int slot, String id, int count) { return record(slotKey, slot, id, count, 7); }
        void check(boolean result, String label) { NativeTransportNbtTest.check(result, name + ": " + label); }
    }
    private static void arrays(ArrayHost h) throws Exception {
        ItemStack[] original = new ItemStack[18];
        ItemStack sentinel = new ItemStack(Items.GOLD_INGOT, 4);
        original[0] = sentinel; original[2] = sentinel; h.set(original);
        NBTTagCompound valid = h.item(1, "minecraft:iron_ingot", 3);
        NBTTagCompound input = h.data(valid, h.item(2, "notregistered:missing", 1), h.item(-1, "minecraft:diamond", 1), h.item(18, "minecraft:diamond", 1), h.item(128, "minecraft:diamond", 1));
        NBTTagCompound snapshot = input.copy(); h.load(input);
        h.check((h.get() != original) == h.resets, "original reset/array alias policy");
        h.check(h.get().length == 18, "storage length");
        h.check(h.get()[0] == (h.resets ? null : sentinel), "sparse entries retain or reset according to owner");
        h.check(h.get()[1].getItem() == Items.IRON_INGOT && h.get()[1].getCount() == 3 && h.get()[1].getItemDamage() == 7, "valid count/item/damage");
        h.check(h.get()[1].getTagCompound() == valid.getCompoundTag("tag"), "native input tag alias");
        h.check(h.get()[2] == null && h.get()[17] == null, "invalid records overwrite vacancy, out-of-bounds ignored");
        h.check(input.equals(snapshot), "input list and unrelated NBT remain unchanged");
        h.load(h.data(h.item(1, "notregistered:missing", 1), h.item(1, "minecraft:diamond", 2), h.item(2, "minecraft:diamond", 1), h.item(2, "notregistered:missing", 1)));
        h.check(h.get()[1].getItem() == Items.DIAMOND && h.get()[1].getCount() == 2 && h.get()[2] == null, "last duplicate wins including invalid last record");
        NBTTagCompound noSlot = h.item(8, "minecraft:iron_ingot", 4); noSlot.removeTag(h.slotKey);
        h.load(h.data(noSlot)); h.check(h.get()[0].getCount() == 4, "missing slot defaults to index zero");
        NBTTagCompound wrongCase = record(h.slotKey.equals("Slot") ? "slot" : "Slot", 5, "minecraft:diamond", 6, 0);
        h.load(h.data(wrongCase)); h.check(h.get()[0].getItem() == Items.DIAMOND && h.get()[0].getCount() == 6, "slot key remains case-sensitive");
        for (String invalid : new String[]{null, "notregistered:missing", "minecraft:air"}) {
            h.get()[1] = sentinel; h.load(h.data(h.item(1, invalid, 1)));
            h.check(h.get()[1] == null, "absent/unregistered/AIR remains null: " + invalid);
        }
        NBTTagCompound numeric = h.item(1, "minecraft:iron_ingot", 1); numeric.setShort("id", (short)265);
        h.load(h.data(numeric)); h.check(h.get()[1] == null, "no old numeric-world importer");
        for (int count : new int[]{-128, -1, 0, 1, 64, 127}) {
            NBTTagCompound item = h.item(1, "minecraft:iron_ingot", count);
            h.load(h.data(item)); ItemStack value = h.get()[1];
            h.check(value != null && value != ItemStack.EMPTY, "registered record not discarded at count " + count);
            h.check(rawCount.getInt(value) == count && value.isEmpty() == (count <= 0), "raw signed count and native emptiness " + count);
            h.check(value.getTagCompound() == item.getCompoundTag("tag"), "tag alias at count " + count);
            // Save handling remains native. Do not pretend empty native public getters are legacy getters.
            h.set(new ItemStack[18]); h.get()[1] = value;
            NBTTagCompound output = new NBTTagCompound(); h.save(output);
            NBTTagList list = output.getTagList(h.listKey, 10);
            NBTTagCompound nativeOutput = value.writeToNBT(new NBTTagCompound()); nativeOutput.setByte(h.slotKey, (byte)1);
            h.check(list.tagCount() == 1 && list.getCompoundTagAt(0).equals(nativeOutput), "writer retains non-null record with native serialization " + count);
        }
        NBTTagCompound negativeDamage = h.item(1, "minecraft:iron_ingot", 1); negativeDamage.setShort("Damage", (short)-1);
        h.load(h.data(negativeDamage)); h.check(h.get()[1].getItemDamage() == 0, "native negative damage clamp");
        ItemStack[] beforeEmpty = h.get(); beforeEmpty[0] = sentinel;
        NBTTagCompound wrongList = new NBTTagCompound(); wrongList.setString(h.listKey, "not a list"); wrongList.setInteger("count", 18);
        h.load(wrongList);
        h.check((h.get() != beforeEmpty) == h.resets && h.get()[0] == (h.resets ? null : sentinel), "wrong-type list preserves owner reset behavior");
        // Actual writer -> reader roundtrip with sparse slots and unselected root data.
        h.set(new ItemStack[18]); h.get()[0] = new ItemStack(Items.DIAMOND, 2); h.get()[3] = new ItemStack(Items.IRON_INGOT, 5);
        NBTTagCompound custom = new NBTTagCompound(); custom.setString("cargo", "probe"); h.get()[3].setTagCompound(custom);
        NBTTagCompound output = new NBTTagCompound(); output.setInteger("unrelated", 19); h.save(output);
        NBTTagList saved = output.getTagList(h.listKey, 10);
        h.check(saved.tagCount() == 2, "writer omits null slots");
        h.check(saved.getCompoundTagAt(0).getByte(h.slotKey) == 0 && saved.getCompoundTagAt(1).getByte(h.slotKey) == 3, "writer preserves order and slot key");
        h.check(output.getInteger("unrelated") == 19, "writer does not clear unrelated fields");
        h.check(saved.getCompoundTagAt(1).getCompoundTag("tag") == custom, "native write tag alias");
        h.set(new ItemStack[18]); h.load(output);
        h.check(h.get()[0].getItem() == Items.DIAMOND && h.get()[0].getCount() == 2 && h.get()[1] == null && h.get()[3].getCount() == 5, "writer/read populated sparse roundtrip");
        h.check(h.get()[3].getTagCompound().equals(custom), "roundtrip custom NBT");
        if (h.name.equals("EntityMovingPackage")) {
            h.check(output.getInteger("count") == 18, "dynamic length saved");
            h.load(root(h.listKey, 256, h.item(128, "minecraft:diamond", 1), h.item(255, "minecraft:iron_ingot", 2)));
            h.check(h.get().length == 256 && h.get()[128].getItem() == Items.DIAMOND && h.get()[255].getCount() == 2, "unsigned slot mask accepts 128/255 in dynamic array");
            NBTTagCompound high = new NBTTagCompound(); h.save(high);
            h.check(high.getTagList(h.listKey, 10).getCompoundTagAt(1).getByte(h.slotKey) == -1, "writer retains signed-byte slot encoding");
            h.load(high); h.check(h.get()[255].getCount() == 2, "high unsigned slot roundtrip");
            h.load(new NBTTagCompound()); h.check(h.get().length == 0, "missing count produces original zero-length storage");
            final NBTTagCompound badCount = new NBTTagCompound(); badCount.setInteger("count", -1);
            fails(NegativeArraySizeException.class, () -> h.load(badCount), "MovingPackage negative count still fails");
        }
    }
    private static void requestDrone() throws Exception {
        ExtractedTransportNbt.EntityRequestDrone h = new ExtractedTransportNbt.EntityRequestDrone();
        ItemStack sentinel = new ItemStack(Items.GOLD_INGOT, 2); h.heldItem = sentinel;
        h.read(new NBTTagCompound()); check(h.heldItem == sentinel, "RequestDrone missing held preserves prior reference");
        NBTTagCompound input = new NBTTagCompound(); input.setTag("held", record(null,0,"minecraft:diamond",3,4));
        h.read(input); check(h.heldItem.getItem() == Items.DIAMOND && h.heldItem.getCount() == 3 && h.heldItem.getItemDamage() == 4, "RequestDrone nested held loads");
        check(h.heldItem.getTagCompound() == input.getCompoundTag("held").getCompoundTag("tag"), "RequestDrone tag alias");
        NBTTagCompound saved = new NBTTagCompound(); saved.setInteger("unrelated", 12); h.write(saved);
        check(saved.getInteger("unrelated") == 12 && saved.getCompoundTag("held").getByte("Count") == 3, "RequestDrone selected writer preserves root and count");
        h.heldItem = null; h.read(saved); check(h.heldItem.getCount() == 3, "RequestDrone writer/reader roundtrip");
        input.setString("held", "wrong type"); h.read(input); check(h.heldItem == null, "RequestDrone present wrong-type held clears prior cargo");
        NBTTagCompound unchanged = saved.copy(); h.write(saved); check(saved.equals(unchanged), "RequestDrone null save does not remove preexisting held tag");
        NBTTagCompound vacant = new NBTTagCompound(); h.write(vacant); check(!vacant.hasKey("held"), "RequestDrone null save omits held on fresh root");
        for (int count : new int[]{0,-1}) {
            input.setTag("held", record(null,0,"minecraft:iron_ingot",count,0)); h.read(input);
            check(h.heldItem != null && rawCount.getInt(h.heldItem) == count && h.heldItem.isEmpty(), "RequestDrone nonpositive record retained " + count);
        }
    }
    private static void bobmazon() throws Exception {
        ExtractedTransportNbt.EntityBobmazon h = new ExtractedTransportNbt.EntityBobmazon();
        ItemStack sentinel = new ItemStack(Items.GOLD_INGOT); h.payload = sentinel;
        fails(NullPointerException.class, () -> h.read(new NBTTagCompound()), "Bobmazon absent payload still fails");
        check(h.payload == sentinel, "Bobmazon failed missing load does not assign");
        NBTTagCompound wrong = new NBTTagCompound(); wrong.setString("payload", "wrong type");
        fails(ClassCastException.class, () -> h.read(wrong), "Bobmazon wrong payload type still fails");
        NBTTagCompound input = new NBTTagCompound(); input.setTag("payload", record(null,0,"notregistered:missing",1,0));
        h.read(input); check(h.payload == null, "Bobmazon invalid registry clears payload");
        fails(NullPointerException.class, () -> h.write(new NBTTagCompound()), "Bobmazon original null writer remains unguarded");
        input.setTag("payload", record(null,0,"minecraft:diamond",7,8)); h.read(input);
        check(h.payload.getCount() == 7 && h.payload.getItemDamage() == 8, "Bobmazon count and damage");
        check(h.payload.getTagCompound() == input.getCompoundTag("payload").getCompoundTag("tag"), "Bobmazon native tag alias");
        NBTTagCompound output = new NBTTagCompound(); output.setInteger("unrelated", 9); h.write(output);
        check(output.getInteger("unrelated") == 9, "Bobmazon writer preserves unrelated fields");
        h.payload = null; h.read(output); check(h.payload.getItem() == Items.DIAMOND && h.payload.getCount() == 7, "Bobmazon roundtrip");
        input.setTag("payload", record(null,0,"minecraft:iron_ingot",-1,0)); h.read(input);
        check(h.payload != null && rawCount.getInt(h.payload) == -1 && h.payload.isEmpty(), "Bobmazon registered negative record not null");
        output = new NBTTagCompound(); h.write(output);
        check(output.getCompoundTag("payload").equals(h.payload.writeToNBT(new NBTTagCompound())), "Bobmazon nonpositive native serialization");
    }
    private static void parachuteCrate() throws Exception {
        ExtractedTransportNbt.EntityParachuteCrate h = new ExtractedTransportNbt.EntityParachuteCrate();
        List<ItemStack> alias = h.items; h.items.add(new ItemStack(Items.GOLD_INGOT));
        NBTTagCompound first = record(null,0,"minecraft:diamond",4,2);
        NBTTagCompound input = root("items",0,first,record(null,0,"notregistered:missing",1,0),record(null,0,"minecraft:iron_ingot",0,0));
        h.read(input);
        check(h.items == alias && h.items.size() == 3, "ParachuteCrate clears/reuses owned list rather than replacing it");
        check(h.items.get(0).getItem() == Items.DIAMOND && h.items.get(0).getCount() == 4, "ParachuteCrate record order/count");
        check(h.items.get(0).getTagCompound() == first.getCompoundTag("tag"), "ParachuteCrate tag alias");
        check(h.items.get(1) == null && h.items.get(2) != null && h.items.get(2).isEmpty(), "ParachuteCrate null and zero-count entries stay distinct");
        final NBTTagCompound preexisting = new NBTTagCompound(); preexisting.setString("items", "prior");
        fails(NullPointerException.class, () -> h.write(preexisting), "ParachuteCrate original null-list writer remains unguarded");
        check(preexisting.getString("items").equals("prior"), "ParachuteCrate failing writer leaves old root tag intact");
        h.read(new NBTTagCompound()); check(h.items == alias && h.items.isEmpty(), "ParachuteCrate missing list clears previous entries in place");
        h.read(root("items",0,first,record(null,0,"minecraft:iron_ingot",2,0)));
        NBTTagCompound output = new NBTTagCompound(); output.setInteger("unrelated",10); h.write(output);
        check(output.getInteger("unrelated") == 10 && output.getTagList("items",10).tagCount() == 2, "ParachuteCrate writer order/length and unrelated fields");
        h.read(output); check(h.items.size() == 2 && h.items.get(0).getItem() == Items.DIAMOND && h.items.get(1).getCount() == 2, "ParachuteCrate writer/read roundtrip");
    }
    private static void capsuleField() {
        ExtractedTransportNbt.EntitySoyuzCapsule h = new ExtractedTransportNbt.EntitySoyuzCapsule();
        NBTTagCompound n = new NBTTagCompound(); n.setInteger("soyuz", 6); h.read(n);
        check(h.soyuz == 6, "SoyuzCapsule full reader retains soyuz discriminator");
        NBTTagCompound saved = new NBTTagCompound(); h.write(saved); check(saved.getInteger("soyuz") == 6, "SoyuzCapsule full writer retains soyuz discriminator");
    }
    public static void main(String[] args) throws Exception {
        Bootstrap.register(); rawCount = ItemStack.class.getDeclaredField("stackSize"); rawCount.setAccessible(true);
        arrays(new ArrayHost("EntityDeliveryDrone", "slots", "Items", "Slot", true));
        arrays(new ArrayHost("EntityMovingPackage", "contents", "contents", "slot", true));
        arrays(new ArrayHost("EntitySoyuz", "payload", "items", "slot", false));
        arrays(new ArrayHost("EntitySoyuzCapsule", "payload", "items", "slot", false));
        requestDrone(); bobmazon(); parachuteCrate(); capsuleField();
        System.out.println("PASS 14 exact production transport NBT reader/writer slices with genuine mapped ItemStack/NBT: " + checks + " checks");
    }
}
