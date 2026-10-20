import java.io.File;
import java.lang.reflect.*;
import java.net.URL;
import java.net.URLClassLoader;
import com.hbm.util.ArmorSlotOrder;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.init.Bootstrap;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import sun.misc.Unsafe;

/** Bounded actual-engine oracle; no game/Forge stand-ins and no NTM initialization. */
public final class NativeArmorEngineTest {
    static int checks, constructorCases, setterCases, virtualCases;
    static void ok(boolean condition, String label) {
        checks++;
        if (!condition) throw new AssertionError(label);
    }
    static Unsafe unsafe() throws Exception {
        Field f = Unsafe.class.getDeclaredField("theUnsafe");
        f.setAccessible(true);
        return (Unsafe) f.get(null);
    }
    static void field(Class<?> owner, Object target, String name, Object value) throws Exception {
        Field f = owner.getDeclaredField(name);
        f.setAccessible(true);
        f.set(target, value);
    }
    public static class TrackingZombie extends EntityZombie {
        public int calls;
        public EntityEquipmentSlot slot;
        public ItemStack stack;
        public TrackingZombie() { super((World) null); }
        @Override public void setItemStackToSlot(EntityEquipmentSlot slot, ItemStack stack) {
            calls++;
            this.slot = slot;
            this.stack = stack;
            super.setItemStackToSlot(slot, stack);
        }
    }
    static void seedNative(EntityLiving mob) throws Exception {
        field(EntityLiving.class, mob, "inventoryHands", NonNullList.withSize(2, ItemStack.EMPTY));
        field(EntityLiving.class, mob, "inventoryArmor", NonNullList.withSize(4, ItemStack.EMPTY));
    }
    public static void main(String[] args) throws Exception {
        Bootstrap.register();
        Unsafe unsafe = unsafe();
        URLClassLoader old = new URLClassLoader(new URL[] {
            new File(args[0]).toURI().toURL(), new File(args[1]).toURI().toURL()
        }, NativeArmorEngineTest.class.getClassLoader());
        Class<?> oldArmor = old.loadClass("abb"), oldMaterial = old.loadClass("abd");
        Class<?> oldItem = old.loadClass("adb"), oldStack = old.loadClass("add");
        old.loadClass("kl").getMethod("b").invoke(null);
        ok(oldArmor.getClassLoader() == old, "original armor loaded by isolated archive loader");
        ok(ItemArmor.class.getClassLoader() != old, "native armor not loaded as original");
        String[] names = {"CLOTH", "CHAIN", "IRON", "GOLD", "DIAMOND"};
        String[] oldRepairFields = {"aA", "j", "j", "k", "i"};
        Item[] repairs = {Items.LEATHER, Items.IRON_INGOT, Items.IRON_INGOT, Items.GOLD_INGOT, Items.DIAMOND};
        ItemArmor.ArmorMaterial[] materials = {ItemArmor.ArmorMaterial.LEATHER, ItemArmor.ArmorMaterial.CHAIN,
            ItemArmor.ArmorMaterial.IRON, ItemArmor.ArmorMaterial.GOLD, ItemArmor.ArmorMaterial.DIAMOND};
        EntityEquipmentSlot[] expectedSlots = {EntityEquipmentSlot.HEAD, EntityEquipmentSlot.CHEST,
            EntityEquipmentSlot.LEGS, EntityEquipmentSlot.FEET};
        int[] bases = {11, 16, 15, 13};
        int[] multipliers = {5, 15, 15, 7, 33};
        int[][] reductions = {{1, 3, 2, 1}, {2, 5, 4, 1}, {2, 6, 5, 2}, {2, 5, 3, 1}, {3, 8, 6, 3}};
        Object[] originals = new Object[4];
        ItemArmor[] natives = new ItemArmor[4];
        for (int m = 0; m < names.length; m++) {
            Object material = oldMaterial.getMethod("valueOf", String.class).invoke(null, names[m]);
            Object originalRepair = oldMaterial.getMethod("b").invoke(material);
            Object expectedOriginalRepair = old.loadClass("ade").getField(oldRepairFields[m]).get(null);
            ok(originalRepair == expectedOriginalRepair, "original builtin repair item identity " + names[m]);
            ok(materials[m].getRepairItem() == repairs[m], "native builtin repair item identity " + names[m]);
            ok(materials[m].getRepairItemStack().getItem() == repairs[m], "native repair stack item identity " + names[m]);
            ok((Integer) oldMaterial.getMethod("a").invoke(material) == materials[m].getEnchantability(), "enchantability " + names[m]);
            for (int type = 0; type < 4; type++) {
                EntityEquipmentSlot slot = ArmorSlotOrder.fromIndex(type);
                ok(slot == expectedSlots[type], "production helper expected slot " + type);
                ok(ArmorSlotOrder.index(slot) == type, "production helper round trip " + type);
                ok(slot.getIndex() == 3 - type, "native inventory index reverses constructor type");
                ok(slot.getSlotIndex() == 4 - type, "native network equipment index");
                Object original = oldArmor.getConstructor(oldMaterial, int.class, int.class).newInstance(material, 7, type);
                ItemArmor nativeArmor = new ItemArmor(materials[m], 7, slot);
                constructorCases++;
                ok(oldArmor.getField("b").getInt(original) == type, "original constructor retains type");
                ok(nativeArmor.armorType == slot && nativeArmor.getEquipmentSlot() == slot, "native constructor retains enum");
                ok(oldArmor.getField("d").getInt(original) == nativeArmor.renderIndex && nativeArmor.renderIndex == 7, "render index unchanged");
                ok(oldArmor.getMethod("m_").invoke(original) == material && nativeArmor.getArmorMaterial() == materials[m], "material reference identity");
                int oldDurability = (Integer) oldItem.getMethod("o").invoke(original);
                int oldReduction = oldArmor.getField("c").getInt(original);
                ok(oldDurability == bases[type] * multipliers[m], "original exact durability " + names[m] + '/' + type);
                ok(oldDurability == nativeArmor.getMaxDamage(), "native exact constructor durability " + names[m] + '/' + type);
                ok(oldReduction == reductions[m][type] && oldReduction == nativeArmor.damageReduceAmount, "per-piece reduction " + names[m] + '/' + type);
                ok((Integer) oldMaterial.getMethod("a", int.class).invoke(material, type) == materials[m].getDurability(slot), "material durability dispatch");
                ok((Integer) oldMaterial.getMethod("b", int.class).invoke(material, type) == materials[m].getDamageReductionAmount(slot), "material reduction dispatch");
                if (m == 2) { originals[type] = original; natives[type] = nativeArmor; }
            }
        }
        for (int invalid : new int[] {-1, 4, Integer.MIN_VALUE, Integer.MAX_VALUE}) {
            try { ArmorSlotOrder.fromIndex(invalid); throw new AssertionError("accepted invalid index"); }
            catch (IllegalArgumentException expected) { ok(true, "invalid legacy type rejected"); }
        }
        for (EntityEquipmentSlot hand : new EntityEquipmentSlot[] {EntityEquipmentSlot.MAINHAND, EntityEquipmentSlot.OFFHAND}) {
            try { ArmorSlotOrder.index(hand); throw new AssertionError("accepted hand as armor"); }
            catch (IllegalArgumentException expected) { ok(true, "hand slot rejected"); }
        }
        try { ArmorSlotOrder.index(null); throw new AssertionError("accepted null"); }
        catch (NullPointerException expected) { ok(true, "null is not silently converted"); }

        Class<?> oldLiving = old.loadClass("sw");
        Object originalMob = unsafe.allocateInstance(old.loadClass("yq"));
        Object oldInventory = Array.newInstance(oldStack, 5);
        field(oldLiving, originalMob, "br", oldInventory);
        EntityZombie nativeMob = (EntityZombie) unsafe.allocateInstance(EntityZombie.class);
        seedNative(nativeMob);
        ItemStack main = new ItemStack(Items.STICK), off = new ItemStack(Items.DIAMOND);
        nativeMob.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, main);
        nativeMob.setItemStackToSlot(EntityEquipmentSlot.OFFHAND, off);
        Object originalHand = oldStack.getConstructor(oldItem, int.class, int.class).newInstance(originals[0], 1, 0);
        oldLiving.getMethod("c", int.class, oldStack).invoke(originalMob, 0, originalHand);
        Field nativeCount = ItemStack.class.getDeclaredField("stackSize"); nativeCount.setAccessible(true);
        for (int type = 0; type < 4; type++) {
            for (int count : new int[] {3, 0, -4}) {
                EntityEquipmentSlot slot = ArmorSlotOrder.fromIndex(type);
                Object originalStack = oldStack.getConstructor(oldItem, int.class, int.class).newInstance(originals[type], count, 2);
                ItemStack nativeStack = new ItemStack(natives[type], count, 2);
                Object oldTag = old.loadClass("dh").newInstance(); oldStack.getField("d").set(originalStack, oldTag);
                NBTTagCompound nativeTag = new NBTTagCompound(); nativeTag.setString("unknown", "retained"); nativeStack.setTagCompound(nativeTag);
                int oldSlot = 4 - type;
                oldLiving.getMethod("c", int.class, oldStack).invoke(originalMob, oldSlot, originalStack);
                nativeMob.setItemStackToSlot(slot, nativeStack); setterCases++;
                ok(Array.get(oldInventory, oldSlot) == originalStack, "original actual setter retains stack identity");
                ok(oldLiving.getMethod("q", int.class).invoke(originalMob, oldSlot) == originalStack, "original concrete getter alias");
                ok(nativeMob.getItemStackFromSlot(slot) == nativeStack, "native actual setter/getter retains stack identity");
                ok(oldStack.getField("b").getInt(originalStack) == count && nativeCount.getInt(nativeStack) == count, "raw count unchanged including zero/negative");
                ok(oldStack.getField("d").get(originalStack) == oldTag && nativeStack.getTagCompound() == nativeTag, "NBT alias retained including empty stacks");
                ok(Array.get(oldInventory, 0) == originalHand, "original hand untouched");
                ok(nativeMob.getItemStackFromSlot(EntityEquipmentSlot.MAINHAND) == main && nativeMob.getItemStackFromSlot(EntityEquipmentSlot.OFFHAND) == off, "both native hands untouched");
            }
            oldLiving.getMethod("c", int.class, oldStack).invoke(originalMob, 4 - type, null);
            nativeMob.setItemStackToSlot(ArmorSlotOrder.fromIndex(type), ItemStack.EMPTY); setterCases++;
            ok(Array.get(oldInventory, 4 - type) == null && nativeMob.getItemStackFromSlot(ArmorSlotOrder.fromIndex(type)) == ItemStack.EMPTY, "null to native EMPTY removal");
        }
        TrackingZombie nativeTracking = (TrackingZombie) unsafe.allocateInstance(TrackingZombie.class); seedNative(nativeTracking);
        Class<?> trackingClass = old.loadClass("OriginalArmorTracking"); Object originalTracking = unsafe.allocateInstance(trackingClass);
        Object trackingInventory = Array.newInstance(oldStack, 5); field(oldLiving, originalTracking, "br", trackingInventory);
        EntityLivingBase nativeBase = nativeTracking;
        for (int type = 0; type < 4; type++) {
            EntityEquipmentSlot slot = ArmorSlotOrder.fromIndex(type);
            Object originalStack = oldStack.getConstructor(oldItem, int.class, int.class).newInstance(originals[type], 1, 0);
            ItemStack nativeStack = new ItemStack(natives[type]);
            oldLiving.getMethod("c", int.class, oldStack).invoke(originalTracking, 4 - type, originalStack);
            nativeBase.setItemStackToSlot(slot, nativeStack); virtualCases++;
            ok(trackingClass.getField("calls").getInt(originalTracking) == type + 1 && trackingClass.getField("slot").getInt(originalTracking) == 4 - type, "original virtual dispatch exactly once with mapped slot");
            ok(trackingClass.getField("stack").get(originalTracking) == originalStack && Array.get(trackingInventory, 4 - type) == originalStack, "original virtual dispatch preserves aliases and superclass storage");
            ok(nativeTracking.calls == type + 1 && nativeTracking.slot == slot, "native base-type virtual dispatch exactly once");
            ok(nativeTracking.stack == nativeStack && nativeTracking.getItemStackFromSlot(slot) == nativeStack, "native virtual dispatch preserves aliases and superclass storage");
        }
        old.close();
        System.out.println("PASS " + checks + " assertions; " + constructorCases + " original/native builtin material-piece constructors; " + setterCases + " concrete mob armor setter cases; " + virtualCases + " paired virtual-dispatch cases; production ArmorSlotOrder compiled/executed.");
        System.out.println("SCOPE: real original and native engine methods; mob objects Unsafe-allocated with actual inventory fields seeded to avoid unrelated world/AI construction. No platform stubs, custom materials, full mod build, Forge event simulation, or gameplay claimed.");
        System.out.println("DIFFERENCES: original CLOTH maps to native LEATHER; 1.12 armor material arrays use reverse indices; original empty is null versus native ItemStack.EMPTY; native includes offhand and toughness. Original Forge1614 player setter correction is source-audited, not substituted by a vanilla player runtime test.");
    }
}
