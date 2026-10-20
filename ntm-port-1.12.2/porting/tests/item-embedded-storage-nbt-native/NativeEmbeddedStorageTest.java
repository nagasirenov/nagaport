import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.minecraft.init.Bootstrap;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

/** Actual extracted production storage code; test wrappers are not native owners. */
public class NativeEmbeddedStorageTest {
    static int checks;
    static Field rawCount;
    static void check(boolean value, String message) { checks++; if (!value) throw new AssertionError(message); }
    interface Operation { void run() throws Exception; }
    static void failure(Class<? extends Throwable> type, Operation op, String message) throws Exception {
        try { op.run(); } catch (Throwable error) { check(type.isInstance(error), message + ": " + error); return; }
        throw new AssertionError(message + ": expected failure");
    }
    static NBTTagCompound record(String id, int count, int damage) {
        NBTTagCompound n = new NBTTagCompound();
        if (id != null) n.setString("id", id);
        n.setByte("Count", (byte)count); n.setShort("Damage", (short)damage);
        NBTTagCompound custom = new NBTTagCompound(); custom.setString("custom", "preserved"); n.setTag("tag", custom);
        return n;
    }
    static ItemStack host() { return new ItemStack(Items.STICK); }
    static NBTTagCompound tag(ItemStack stack) { if (!stack.hasTagCompound()) stack.setTagCompound(new NBTTagCompound()); return stack.getTagCompound(); }
    static ExtractedEmbeddedStorage.Crate crate(int size) {
        ExtractedEmbeddedStorage.Crate c = new ExtractedEmbeddedStorage.Crate(); c.target = host(); c.slots = new ItemStack[size]; return c;
    }
    static void loaded(ItemStack value, NBTTagCompound source, int count, String label) throws Exception {
        check(value != null && value != ItemStack.EMPTY, label + " registered record remains separate non-null object");
        check(rawCount.getInt(value) == count && value.isEmpty() == (count <= 0), label + " raw signed count/native emptiness");
        check(value.getTagCompound() == source.getCompoundTag("tag"), label + " native NBT tag alias");
        if (count > 0) check(value.getItem() == Items.IRON_INGOT && value.getCount() == count && value.getItemDamage() == 7, label + " populated native getters");
    }
    static List<String> tooltip(ItemStack item) { List<String> result = new ArrayList<>(); ExtractedEmbeddedStorage.lockedTooltip(item, result); return result; }
    static boolean contains(List<String> lines, String text) { for (String line : lines) if (line.contains(text)) return true; return false; }
    static void crates() throws Exception {
        ExtractedEmbeddedStorage.Crate c = crate(4); ItemStack[] alias = c.slots;
        c.slots[0] = new ItemStack(Items.DIAMOND); ExtractedEmbeddedStorage.Crate.rand = new Random(32);
        c.read();
        check(c.slots == alias && c.slots[0] == null && c.slots[3] == null, "crate constructor storage slice fills existing supplied array, absent slots null");
        check(c.target.hasTagCompound() && c.target.getTagCompound().getLong("stacklock") == new Random(32).nextLong(), "untagged crate receives original stacklock initialization");
        NBTTagCompound n = c.target.getTagCompound(); n.setInteger("lock", 451); n.setBoolean("spiders", true); n.setString("foreign", "keep");
        NBTTagCompound input = record("minecraft:iron_ingot", 3, 7); n.setTag("slot1", input); n.setTag("slot2", record("missing:unregistered",1,0)); n.setTag("slot9", record("minecraft:diamond",1,0));
        long lock = n.getLong("stacklock"); c.read();
        check(c.target.getTagCompound() == n && n.getLong("stacklock") == lock, "tagged crate read preserves root and existing stacklock");
        loaded(c.slots[1], input, 3, "crate reader");
        check(c.slots[2] == null && n.hasKey("slot2") && n.hasKey("slot9"), "crate reader nulls invalid entries without deleting source or outside slot records");
        check(ExtractedEmbeddedStorage.crateTooltipLoad(c.target,1).getCount() == 3, "unlocked tooltip uses actual slot schema");
        check(ExtractedEmbeddedStorage.hazardCrateLoad(c.target,1).getCount() == 3, "crate hazard uses actual slot schema");
        check(ExtractedEmbeddedStorage.massTypeLoad(c.target).getCount() == 3, "mass-storage type reads slot1");
        c.slots[0] = new ItemStack(Items.DIAMOND, 2); c.slots[1] = null; c.slots[3] = new ItemStack(Items.IRON_INGOT,5,7);
        NBTTagCompound nested = new NBTTagCompound(); nested.setInteger("payload", 17); c.slots[3].setTagCompound(nested);
        ExtractedEmbeddedStorage.Crate.rand = new Random(64); c.write(4);
        check(c.target.getTagCompound() == n && n.getInteger("lock") == 451 && n.getBoolean("spiders") && n.getString("foreign").equals("keep"), "crate writer preserves root, lock, spiders and foreign keys");
        check(!n.hasKey("slot1") && !n.hasKey("slot2") && n.hasKey("slot9"), "crate writer removes null in-range keys but preserves keys outside size");
        check(n.getLong("stacklock") == new Random(64).nextLong(), "crate writer replaces stacklock with exact random sequence");
        check(n.getCompoundTag("slot3").getCompoundTag("tag") == nested, "crate writer retains native serialized tag alias");
        c.read(); check(c.slots[0].getItem() == Items.DIAMOND && c.slots[0].getCount() == 2 && c.slots[1] == null && c.slots[3].getCount() == 5, "crate actual writer-reader sparse roundtrip");
        check(c.slots[3].getTagCompound() == nested, "crate roundtrip aliases nested compound");
        for (String invalid : new String[]{null,"missing:unregistered","minecraft:air"}) {
            n.setTag("slot1",record(invalid,1,7)); c.read();
            check(c.slots[1] == null && ExtractedEmbeddedStorage.crateTooltipLoad(c.target,1) == null && ExtractedEmbeddedStorage.hazardCrateLoad(c.target,1) == null && ExtractedEmbeddedStorage.massTypeLoad(c.target) == null, "all crate consumers agree on missing/invalid/AIR null: " + invalid);
        }
        NBTTagCompound numeric = record("minecraft:iron_ingot",1,7); numeric.setShort("id",(short)265); n.setTag("slot1",numeric); c.read();
        check(c.slots[1] == null && ExtractedEmbeddedStorage.massTypeLoad(c.target) == null, "no numeric-ID world import");
        for (int count : new int[]{-128,-1,0,1,64,127}) {
            NBTTagCompound r = record("minecraft:iron_ingot",count,7); n.setTag("slot1",r); c.read();
            loaded(c.slots[1],r,count,"crate " + count);
            loaded(ExtractedEmbeddedStorage.crateTooltipLoad(c.target,1),r,count,"tooltip " + count);
            loaded(ExtractedEmbeddedStorage.hazardCrateLoad(c.target,1),r,count,"hazard " + count);
            loaded(ExtractedEmbeddedStorage.massTypeLoad(c.target),r,count,"mass type " + count);
            ItemStack value = c.slots[1]; c.write(4);
            check(n.getCompoundTag("slot1").equals(value.writeToNBT(new NBTTagCompound())), "non-null crate writer uses unaltered native serialization " + count);
        }
        ItemStack blockCargo = new ItemStack(Items.IRON_INGOT,6,7); blockCargo.setTagCompound(nested);
        NBTTagCompound blockSaved = new NBTTagCompound(); ExtractedEmbeddedStorage.blockCrateWrite(blockCargo,blockSaved,2);
        ItemStack blockItem = host(); blockItem.setTagCompound(blockSaved);
        check(ExtractedEmbeddedStorage.crateTooltipLoad(blockItem,2).getCount() == 6 && ExtractedEmbeddedStorage.hazardCrateLoad(blockItem,2).getTagCompound() == nested, "actual block-crate writer fragment feeds tooltip/hazard schema");
        ExtractedEmbeddedStorage.blockMassWrite(blockCargo,blockSaved,1);
        check(ExtractedEmbeddedStorage.massTypeLoad(blockItem).getCount() == 6, "actual block-mass writer fragment feeds slot1 type reader");
        // Entire selected locked/spider branches are exercised, including their early returns.
        ItemStack locked = host(); tag(locked).setInteger("lock",1);
        check(contains(tooltip(locked),"locked") && contains(tooltip(locked),"empty"), "locked empty crate messages");
        tag(locked).setTag("slot103",record("minecraft:iron_ingot",0,7));
        check(contains(tooltip(locked),"heavy") && !contains(tooltip(locked),"empty"), "locked scan retains zero-count non-null at slot103");
        tag(locked).removeTag("slot103"); tag(locked).setTag("slot104",record("minecraft:diamond",1,0));
        check(contains(tooltip(locked),"empty"), "locked scan excludes slot104 exactly");
        tag(locked).setTag("slot0",record("missing:unregistered",1,0));
        check(contains(tooltip(locked),"empty"), "locked invalid record remains empty");
        tag(locked).setTag("slot1",record("minecraft:iron_ingot",3,7)); tag(locked).setBoolean("spiders",true);
        check(contains(tooltip(locked),"Skittering") && !contains(tooltip(locked),"heavy"), "spider branch returns before cargo scan");
        check(tooltip(host()).isEmpty(), "untagged tooltip guard preserved");
        ExtractedEmbeddedStorage.Crate empty = crate(0); empty.write(0);
        check(empty.target.hasTagCompound() && empty.target.getTagCompound().hasKey("stacklock"), "empty writer retains tag compound and stacklock");
    }
    static final String MODS = "ntm_armor_mods", SLOT = "mod_slot_";
    static void armor() throws Exception {
        ItemStack armor = new ItemStack(Items.IRON_CHESTPLATE);
        check(!ExtractedEmbeddedStorage.Armor.hasMods(armor), "fresh armor has no mods");
        ItemStack[] absent = ExtractedEmbeddedStorage.Armor.pryMods(armor);
        check(absent.length == 9 && absent[0] == null && absent[8] == null && !armor.hasTagCompound(), "fresh armor array uses nine null slots without mutating NBT");
        check(ExtractedEmbeddedStorage.Armor.pryMod(armor,0) == null, "fresh single-mod reader returns null");
        tag(armor).setString("foreign","keep"); NBTTagCompound root = armor.getTagCompound();
        ItemStack mod = new ItemStack(Items.IRON_INGOT,3,7); NBTTagCompound custom = new NBTTagCompound(); custom.setString("test","retained"); mod.setTagCompound(custom);
        ExtractedEmbeddedStorage.Armor.writeMod(armor,mod,4);
        NBTTagCompound mods = root.getCompoundTag(MODS);
        check(root == armor.getTagCompound() && root.getString("foreign").equals("keep") && mods.hasKey(SLOT+4), "mod writer preserves outer root and slot schema");
        check(ExtractedEmbeddedStorage.Armor.hasMods(armor), "hasMods sees stored compound");
        check(ExtractedEmbeddedStorage.Armor.pryMod(armor,4).getCount() == 3 && ExtractedEmbeddedStorage.Armor.pryMod(armor,4).getTagCompound() == custom, "single mod writer-reader roundtrip/tag alias");
        ItemStack[] all = ExtractedEmbeddedStorage.Armor.pryMods(armor);
        check(all[4].getCount() == 3 && all[0] == null && all[8] == null && root.getCompoundTag(MODS) == mods, "multi-mod reader returns slot4 and retains existing mods alias");
        for (String invalid : new String[]{null,"missing:unregistered","minecraft:air"}) {
            mods.setTag(SLOT+2,record(invalid,1,7));
            check(ExtractedEmbeddedStorage.Armor.pryMod(armor,2) == null && !mods.hasKey(SLOT+2) && mods.hasKey(SLOT+4), "single invalid mod removed without removing good sibling: " + invalid);
        }
        for (int count : new int[]{-128,-1,0,1,64,127}) {
            NBTTagCompound r = record("minecraft:iron_ingot",count,7); mods.setTag(SLOT+2,r);
            loaded(ExtractedEmbeddedStorage.Armor.pryMod(armor,2),r,count,"single armor " + count);
            loaded(ExtractedEmbeddedStorage.Armor.pryMods(armor)[2],r,count,"multi armor " + count);
            check(mods.getCompoundTag(SLOT+2) == r, "registered nonpositive armor record not removed " + count);
        }
        mods.setTag(SLOT+0,record("missing:unregistered",1,0)); mods.setString("foreignModKey","keep");
        ExtractedEmbeddedStorage.Armor.pryMods(armor);
        check(!mods.hasKey(SLOT+0) && mods.getString("foreignModKey").equals("keep"), "multi read removes invalid record but retains foreign mod data");
        mods.setTag(SLOT+123,record("minecraft:iron_ingot",2,7));
        check(ExtractedEmbeddedStorage.Armor.pryMod(armor,123).getCount() == 2, "single mod reader preserves original unbounded integer key semantics");
        ExtractedEmbeddedStorage.Armor.clearMods(armor);
        check(!root.hasKey(MODS) && root.getString("foreign").equals("keep") && armor.getTagCompound() == root, "clearMods removes only compound, retaining root alias/foreign data");
        ExtractedEmbeddedStorage.Armor.writeMod(armor,mod,0);
        ExtractedEmbeddedStorage.Armor.removeMod(armor,0);
        check(!root.hasKey(MODS), "native NBT isEmpty cleanup removes parent after last mod");
        root.setTag(MODS,new NBTTagCompound()); root.getCompoundTag(MODS).setTag(SLOT+0,record("missing:unregistered",1,0));
        check(ExtractedEmbeddedStorage.Armor.pryMods(armor)[0] == null && !root.hasKey(MODS), "multi-read invalid final mod clears parent");
        root.setString(MODS,"wrong type"); check(ExtractedEmbeddedStorage.Armor.hasMods(armor), "hasMods keeps original any-tag-type presence test");
        check(ExtractedEmbeddedStorage.Armor.pryMod(armor,0) == null && !root.hasKey(MODS), "wrong-type mods use original empty compound cleanup behavior");
        ItemStack empty = host(); ExtractedEmbeddedStorage.Armor.removeMod(empty,7);
        check(empty.hasTagCompound() && empty.getTagCompound().isEmpty(), "remove nonexistent mod retains original allocated empty root");
        ExtractedEmbeddedStorage.Armor.removeMod(null,0); check(true,"removeMod null guard retained");
        failure(NullPointerException.class, () -> ExtractedEmbeddedStorage.Armor.pryMods(null), "pryMods null owner remains unguarded");
        failure(NullPointerException.class, () -> ExtractedEmbeddedStorage.Armor.pryMod(null,0), "pryMod null owner remains unguarded");
    }
    static final String FILTER = "hfrFilter";
    static void filters() throws Exception {
        check(ExtractedEmbeddedStorage.Filter.getGasMaskFilter(null) == null, "null mask returns null");
        ItemStack mask = host(); check(ExtractedEmbeddedStorage.Filter.getGasMaskFilter(mask) == null && !mask.hasTagCompound(), "missing root returns null without allocation");
        ExtractedEmbeddedStorage.Filter.removeFilter(mask); check(!mask.hasTagCompound(), "remove absent filter does not allocate root");
        ExtractedEmbeddedStorage.Filter.installGasMaskFilter(mask,null); check(!mask.hasTagCompound(), "install null filter does not allocate root");
        ExtractedEmbeddedStorage.Filter.installGasMaskFilter(null,new ItemStack(Items.IRON_INGOT)); check(true,"install null mask returns");
        NBTTagCompound root = tag(mask); root.setString("foreign","keep");
        for (String invalid : new String[]{null,"missing:unregistered","minecraft:air"}) {
            NBTTagCompound r = record(invalid,1,7); root.setTag(FILTER,r);
            check(ExtractedEmbeddedStorage.Filter.getGasMaskFilter(mask) == null && root.getCompoundTag(FILTER) == r, "invalid filter returns null without deleting record: " + invalid);
        }
        root.setString(FILTER,"wrong type"); check(ExtractedEmbeddedStorage.Filter.getGasMaskFilter(mask) == null && root.getString(FILTER).equals("wrong type"), "wrong-type filter is not cleaned up");
        for (int count : new int[]{-128,-1,0,1,64,127}) {
            NBTTagCompound r = record("minecraft:iron_ingot",count,7); root.setTag(FILTER,r);
            loaded(ExtractedEmbeddedStorage.Filter.getGasMaskFilter(mask),r,count,"filter " + count);
        }
        ItemStack filter = new ItemStack(Items.IRON_INGOT,4,7); NBTTagCompound tag = new NBTTagCompound(); tag.setInteger("custom",82); filter.setTagCompound(tag);
        ExtractedEmbeddedStorage.Filter.installGasMaskFilter(mask,filter);
        check(mask.getTagCompound() == root && root.getString("foreign").equals("keep"), "filter install preserves root/foreign keys");
        check(ExtractedEmbeddedStorage.Filter.getGasMaskFilter(mask).getCount() == 4 && ExtractedEmbeddedStorage.Filter.getGasMaskFilter(mask).getTagCompound() == tag, "filter writer-reader roundtrip and nested alias");
        ItemStack armor = new ItemStack(Items.IRON_HELMET); ExtractedEmbeddedStorage.Armor.writeMod(armor,mask,0);
        ItemStack recoveredMask = ExtractedEmbeddedStorage.Armor.pryMod(armor,0);
        check(ExtractedEmbeddedStorage.Filter.getGasMaskFilter(recoveredMask).getCount() == 4, "nested armor-mod and mask-filter storage roundtrip");
        ExtractedEmbeddedStorage.Filter.removeFilter(mask);
        check(!root.hasKey(FILTER) && root.getString("foreign").equals("keep") && mask.getTagCompound() == root, "filter removal keeps root alias and unrelated data");
        check(ExtractedEmbeddedStorage.Filter.getGasMaskFilter(recoveredMask) == null, "nested storage preserves native tag alias instead of inventing copies");
    }
    public static void main(String[] args) throws Exception {
        Bootstrap.register(); rawCount = ItemStack.class.getDeclaredField("stackSize"); rawCount.setAccessible(true);
        crates(); armor(); filters();
        System.out.println("PASS actual item-embedded storage reader/writer/cleanup slices with genuine native NBT and ItemStack: " + checks + " checks");
    }
}
