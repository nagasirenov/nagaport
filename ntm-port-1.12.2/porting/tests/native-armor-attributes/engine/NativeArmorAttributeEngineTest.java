import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.UUID;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeMap;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.init.Bootstrap;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

/** Real engine classes plus test-only native descendants. No fake platform classes. */
public final class NativeArmorAttributeEngineTest {
    private static int checks;
    private static final String SPEED = SharedMonsterAttributes.MOVEMENT_SPEED.getName();
    private static final UUID MARKER = UUID.fromString("f183a1b0-f430-45ce-9612-000000000001");
    private static final UUID SPRINT = UUID.fromString("6ab858ba-d712-485c-bae9-e5e765fc555a");
    private static void ok(boolean pass, String description) {
        checks++;
        if (!pass) throw new AssertionError(description);
    }
    private static void near(double actual, double expected, String description) {
        ok(Math.abs(actual - expected) < 1e-10, description + ": " + actual + " != " + expected);
    }
    private static AttributeModifier speed(UUID id, double amount) {
        return new AttributeModifier(id, "Engine test speed", amount, 0);
    }
    public static final class MarkerItem extends Item {
        int oldHookCalls, stackHookCalls;
        EntityEquipmentSlot lastSlot;
        ItemStack lastStack;
        @Override public Multimap<String, AttributeModifier> getItemAttributeModifiers(EntityEquipmentSlot slot) {
            oldHookCalls++;
            lastSlot = slot;
            Multimap<String, AttributeModifier> map = super.getItemAttributeModifiers(slot);
            map.put(SPEED, speed(MARKER, 0.25));
            return map;
        }
        @Override public Multimap<String, AttributeModifier> getAttributeModifiers(EntityEquipmentSlot slot, ItemStack stack) {
            stackHookCalls++;
            lastStack = stack;
            return super.getAttributeModifiers(slot, stack);
        }
    }
    private static NBTTagCompound nbtModifier(UUID id, double amount, String slot) {
        NBTTagCompound entry = SharedMonsterAttributes.writeAttributeModifierToNBT(speed(id, amount));
        entry.setString("AttributeName", SPEED);
        if (slot != null) entry.setString("Slot", slot);
        return entry;
    }
    private static AttributeMap attributes() {
        AttributeMap map = new AttributeMap();
        map.registerAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(0);
        map.registerAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS).setBaseValue(0);
        map.registerAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.1);
        return map;
    }
    private static void value(AttributeMap map, IAttribute attribute, double expected, String label) {
        near(map.getAttributeInstance(attribute).getAttributeValue(), expected, label);
    }
    public static void main(String[] args) throws Exception {
        Bootstrap.register();
        // This loader isolates the actual obfuscated old engine from the native named engine.
        URLClassLoader old = new URLClassLoader(new URL[] { new File(args[0]).toURI().toURL() },
                NativeArmorAttributeEngineTest.class.getClassLoader());
        old.loadClass("kl").getMethod("b").invoke(null);
        Class<?> oldItem = old.loadClass("adb"), oldArmor = old.loadClass("abb"), oldMaterial = old.loadClass("abd");
        ok(oldItem.getClassLoader() == old && Item.class.getClassLoader() != old, "separate actual engine class loaders");
        Object item = oldItem.newInstance();
        Method hook = oldItem.getMethod("k");
        Multimap<?, ?> first = (Multimap<?, ?>) hook.invoke(item), second = (Multimap<?, ?>) hook.invoke(item);
        ok(first.isEmpty() && second.isEmpty() && first != second, "legacy Item returns independent empty maps");
        Object material = oldMaterial.getMethod("valueOf", String.class).invoke(null, "DIAMOND");
        for (int slot = 0; slot < 4; slot++) {
            Object armor = oldArmor.getConstructor(oldMaterial, int.class, int.class).newInstance(material, 0, slot);
            ok(((Multimap<?, ?>) hook.invoke(armor)).isEmpty(), "legacy armor base map empty for type " + slot);
        }
        old.close();
        System.out.println("PASS real original Item and four ItemArmor empty base maps");

        MarkerItem marker = new MarkerItem();
        Item asItem = marker;
        ItemStack markerStack = new ItemStack(marker);
        for (EntityEquipmentSlot slot : EntityEquipmentSlot.values()) {
            int calls = marker.oldHookCalls;
            Multimap<String, AttributeModifier> map = asItem.getAttributeModifiers(slot, markerStack);
            ok(marker.oldHookCalls == calls + 1 && marker.lastSlot == slot && marker.lastStack == markerStack,
                    "native default bridge virtual slot/stack identity " + slot);
            ok(map.size() == 1 && map.get(SPEED).iterator().next().getID().equals(MARKER), "bridge marker payload " + slot);
            calls = marker.stackHookCalls;
            map = markerStack.getAttributeModifiers(slot);
            ok(marker.stackHookCalls == calls + 1 && marker.lastSlot == slot && marker.lastStack == markerStack,
                    "actual ItemStack invokes stack-sensitive item hook " + slot);
            ok(map.size() == 1, "actual ItemStack marker payload " + slot);
        }
        ok(EntityEquipmentSlot.values().length == 6, "native engine exposes six equipment slots");
        System.out.println("PASS native virtual Item bridge and ItemStack dispatch across all six equipment slots");

        ItemArmor chest = new ItemArmor(ItemArmor.ArmorMaterial.DIAMOND, 0, EntityEquipmentSlot.CHEST);
        ItemStack chestStack = new ItemStack(chest);
        Multimap<String, AttributeModifier> worn = chest.getAttributeModifiers(EntityEquipmentSlot.CHEST, chestStack);
        ok(worn.size() == 2, "native matching armor base contains exactly armor and toughness");
        AttributeModifier armor = worn.get(SharedMonsterAttributes.ARMOR.getName()).iterator().next();
        AttributeModifier toughness = worn.get(SharedMonsterAttributes.ARMOR_TOUGHNESS.getName()).iterator().next();
        near(armor.getAmount(), 8, "diamond chest armor amount");
        near(toughness.getAmount(), 2, "diamond chest toughness amount");
        ok(armor.getOperation() == 0 && toughness.getOperation() == 0 && armor.getID().equals(toughness.getID()),
                "native defense modifiers use operation zero and same per-slot UUID");
        for (EntityEquipmentSlot slot : EntityEquipmentSlot.values()) {
            if (slot != EntityEquipmentSlot.CHEST)
                ok(chest.getAttributeModifiers(slot, chestStack).isEmpty(), "nonmatching armor base empty " + slot);
        }
        AttributeMap living = attributes();
        living.applyAttributeModifiers(worn);
        value(living, SharedMonsterAttributes.ARMOR, 8, "worn defense applied");
        value(living, SharedMonsterAttributes.ARMOR_TOUGHNESS, 2, "worn toughness applied");
        Multimap<String, AttributeModifier> contaminated = chest.getAttributeModifiers(EntityEquipmentSlot.CHEST, chestStack);
        contaminated.put(SPEED, speed(SPRINT, 0.1));
        living.removeAttributeModifiers(contaminated);
        value(living, SharedMonsterAttributes.ARMOR, 0, "counterexample matching-slot manual removal strips defense");
        value(living, SharedMonsterAttributes.ARMOR_TOUGHNESS, 0, "counterexample matching-slot manual removal strips toughness");
        living.applyAttributeModifiers(worn);
        Multimap<String, AttributeModifier> clean = chest.getAttributeModifiers(EntityEquipmentSlot.MAINHAND, chestStack);
        clean.put(SPEED, speed(SPRINT, 0.1));
        ok(clean.size() == 1, "MAINHAND manual map contains custom speed only");
        living.removeAttributeModifiers(clean);
        value(living, SharedMonsterAttributes.ARMOR, 8, "MAINHAND manual removal retains defense");
        value(living, SharedMonsterAttributes.ARMOR_TOUGHNESS, 2, "MAINHAND manual removal retains toughness");
        living.applyAttributeModifiers(clean);
        value(living, SharedMonsterAttributes.MOVEMENT_SPEED, 0.2, "manual sprint speed applied");
        living.removeAttributeModifiers(clean);
        value(living, SharedMonsterAttributes.MOVEMENT_SPEED, 0.1, "manual sprint speed removed");
        value(living, SharedMonsterAttributes.ARMOR, 8, "sprint cycle retains defense");
        value(living, SharedMonsterAttributes.ARMOR_TOUGHNESS, 2, "sprint cycle retains toughness");
        System.out.println("PASS actual native AttributeMap protection-strip counterexample and MAINHAND preservation");

        int hookCalls = marker.stackHookCalls;
        NBTTagCompound tag = new NBTTagCompound();
        NBTTagList list = new NBTTagList();
        tag.setTag("AttributeModifiers", list);
        markerStack.setTagCompound(tag);
        ok(markerStack.getAttributeModifiers(EntityEquipmentSlot.HEAD).isEmpty(), "empty NBT list replaces item defaults");
        ok(marker.stackHookCalls == hookCalls, "NBT replacement bypasses item hook");
        list.appendTag(nbtModifier(UUID.fromString("f183a1b0-f430-45ce-9612-000000000002"), 0.5, "head"));
        list.appendTag(nbtModifier(UUID.fromString("f183a1b0-f430-45ce-9612-000000000003"), 0.75, null));
        for (EntityEquipmentSlot slot : EntityEquipmentSlot.values()) {
            Multimap<String, AttributeModifier> map = markerStack.getAttributeModifiers(slot);
            ok(map.size() == (slot == EntityEquipmentSlot.HEAD ? 2 : 1), "NBT matching/unspecified Slot filtering " + slot);
            for (AttributeModifier modifier : map.values()) ok(!modifier.getID().equals(MARKER), "NBT never merges item marker " + slot);
        }
        ok(marker.stackHookCalls == hookCalls, "all NBT slot queries bypass item hook");
        // Direct Item queries preserve the old non-NBT path used by manually applied maps.
        ok(marker.getAttributeModifiers(EntityEquipmentSlot.MAINHAND, markerStack).size() == 1,
                "direct item hook is distinct from stack NBT replacement");
        System.out.println("PASS ordinary native NBT replacement, slot filtering, and direct-item distinction");
        System.out.println("PASS " + checks + " actual-engine assertions; no platform stubs or production classes loaded");
        System.out.println("SCOPE: legacy Forge stack bridge and old-five/native-six equipment loops are exact patch/bytecode evidence; no patched legacy runtime or full entity update/gameplay simulation is claimed.");
    }
}
