package inventoryengine;

import java.io.*;
import java.lang.reflect.Field;
import java.nio.file.*;
import java.util.Random;
import net.minecraft.init.*;
import net.minecraft.item.*;
import net.minecraft.nbt.*;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

/** Actual Forge ItemStack/NBT operations. No platform replacement classes. */
public final class NativeEngineOracle {
    static int checks;
    static final Field RAW = ReflectionHelper.findField(ItemStack.class, "stackSize", "field_77994_a");
    static void check(boolean condition, String message) {
        checks++;
        if (!condition) throw new AssertionError(message);
    }
    /** Candidate read-only quantity contract, not a claim to invoke production. */
    static int quantity(ItemStack stack) {
        int visible = stack.getCount();
        if (visible != 0) return visible;
        try { return RAW.getInt(stack); }
        catch (IllegalAccessException e) { throw new IllegalStateException(e); }
    }
    /** Proposed boundary expression only; domain storage remains nullable. */
    static ItemStack boundary(ItemStack stack) { return stack == null ? ItemStack.EMPTY : stack; }
    static NBTTagCompound data(int length) {
        NBTTagCompound root = new NBTTagCompound();
        root.setString("label", "inventory oracle \u03bb");
        root.setByte("byte", (byte)-7);
        root.setShort("short", (short)32760);
        root.setInteger("int", 12345678);
        root.setLong("long", -1234567890123L);
        root.setFloat("float", 1.25F);
        root.setDouble("double", -2.125D);
        root.setIntArray("ints", new int[]{Integer.MIN_VALUE, -1, 0, 1, Integer.MAX_VALUE});
        byte[] payload = new byte[length];
        new Random(9917).nextBytes(payload);
        root.setByteArray("payload", payload);
        NBTTagCompound child = new NBTTagCompound();
        child.setString("name", "tag");
        root.setTag("nested", child);
        return root;
    }
    public static void main(String[] args) throws Exception {
        Bootstrap.register();
        check(RAW.getName().equals(System.getProperty("expectedQuantityField", "stackSize")), "field layout");
        int[] counts = {Integer.MIN_VALUE, -65536, -257, -256, -129, -128, -1, 0, 1, 64, 127, 128, 256, Integer.MAX_VALUE};
        for (int count : counts) {
            ItemStack original = new ItemStack(Items.DIAMOND, count, 9);
            NBTTagCompound tag = new NBTTagCompound(); tag.setString("payload", "kept");
            original.setTagCompound(tag);
            check(quantity(original) == count, "raw count " + count);
            check(original.getCount() == (count > 0 ? count : 0), "visible count " + count);
            check(boundary(original) == original, "nonnull boundary identity " + count);
            ItemStack copy = original.copy();
            check(copy != original && quantity(copy) == count, "patched copy count/identity " + count);
            check(copy.getTagCompound() != tag && copy.getTagCompound().equals(tag), "patched copy deep tag " + count);
            copy.setCount(1);
            check(copy.getItem() == Items.DIAMOND && copy.getItemDamage() == 9, "copy raw item/damage retained " + count);
            NBTTagCompound written = original.writeToNBT(new NBTTagCompound());
            check(written.getByte("Count") == (byte)count, "NBT signed byte count " + count);
            check(written.getString("id").equals("minecraft:diamond"), "NBT raw item retained " + count);
            for (int amount : new int[]{Integer.MIN_VALUE, -2, -1, 0, 1, 3, 64, Integer.MAX_VALUE}) {
                if (count <= amount) continue; // Exact old ItemInventory split-branch precondition.
                ItemStack source = original.copy();
                ItemStack result = source.splitStack(amount);
                check(result != source && quantity(result) == amount, "split result " + count + "/" + amount);
                check(quantity(source) == count - amount, "split source signed overflow " + count + "/" + amount);
                check(result.getTagCompound() != source.getTagCompound(), "split tag copy " + count + "/" + amount);
            }
        }
        check(boundary(null) == ItemStack.EMPTY, "null boundary sentinel");
        check(boundary(ItemStack.EMPTY) == ItemStack.EMPTY, "sentinel identity");
        check(quantity(ItemStack.EMPTY) == RAW.getInt(ItemStack.EMPTY), "EMPTY raw identity not normalized");
        try { quantity(null); throw new AssertionError("null must fail"); }
        catch (NullPointerException expected) { checks++; }
        ItemStack repaired = new ItemStack(Items.DIAMOND, 64, 65536);
        repaired.setItemDamage(0);
        check(!repaired.isEmpty() && repaired.getCount() == 0 && quantity(repaired) == 64, "cached-empty repaired stack");
        ItemStack last = new ItemStack(Items.DIAMOND, 7, 9);
        NBTTagCompound tag = new NBTTagCompound(); tag.setString("payload", "last");
        last.setTagCompound(tag);
        Item item = last.getItem(); int damage = last.getItemDamage(); NBTTagCompound captured = last.getTagCompound();
        last.shrink(7);
        check(last.getItem() == Items.AIR && !last.hasTagCompound(), "post-depletion item/tag masking");
        ItemStack drop = new ItemStack(item, 7, damage); drop.setTagCompound(captured.copy());
        check(drop.getItem() == Items.DIAMOND && drop.getItemDamage() == 9 && drop.getCount() == 7, "pre-shrink item/damage");
        check(drop.getTagCompound() != tag && drop.getTagCompound().equals(tag), "pre-shrink tag deep copy");
        Path out = Paths.get(args[0]); Files.createDirectories(out);
        for (int length = 5700; length <= 6100; length++) {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            NBTTagCompound root = data(length); CompressedStreamTools.writeCompressed(root, bytes);
            byte[] compressed = bytes.toByteArray();
            Files.write(out.resolve(length + ".nbt.gz"), compressed);
            check(CompressedStreamTools.readCompressed(new ByteArrayInputStream(compressed)).equals(root), "native NBT roundtrip " + length);
        }
        System.out.println("PASS native engine checks=" + checks + " quantityField=" + RAW.getName());
    }
}
