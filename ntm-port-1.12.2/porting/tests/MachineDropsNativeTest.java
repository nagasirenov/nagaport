import java.util.*;
import net.minecraft.init.Bootstrap;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Item;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.profiler.Profiler;
import net.minecraft.world.World;
import net.minecraft.world.WorldProviderSurface;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.storage.SaveHandlerMP;
import net.minecraft.world.storage.WorldInfo;

/** Real native ItemStack/NBT/EntityItem with bounded production drop-block extraction. */
public final class MachineDropsNativeTest {
    static int checks;
    static void check(boolean value, String label) { checks++; if(!value) throw new AssertionError(label); }
    static final class DropWorld extends World {
        final List<EntityItem> drops = new ArrayList<>();
        DropWorld() {
            super(new SaveHandlerMP(), new WorldInfo(new NBTTagCompound()), new WorldProviderSurface(), new Profiler(), false);
            provider.setWorld(this);
        }
        @Override protected IChunkProvider createChunkProvider() { return null; }
        @Override protected boolean isChunkLoaded(int x, int z, boolean allowEmpty) { return false; }
        @Override public boolean spawnEntity(Entity entity) { drops.add((EntityItem) entity); return true; }
    }
    // Explicit old algorithm: legacy stacks retained item/meta/tag after count depletion.
    // An independent integer remaining count models that old field; native source count is
    // still mutated so alias/depletion effects can be compared with production.
    static void legacy(DropWorld world, ItemStack source, Random rand) {
        if(source == null || source.isEmpty()) return;
        Item item = source.getItem(); int meta = source.getItemDamage(); NBTTagCompound tag = source.getTagCompound();
        int remaining = source.getCount();
        float x = rand.nextFloat() * 0.8F + 0.1F;
        float y = rand.nextFloat() * 0.8F + 0.1F;
        float z = rand.nextFloat() * 0.8F + 0.1F;
        while(remaining > 0) {
            int count = rand.nextInt(21) + 10;
            if(count > remaining) count = remaining;
            remaining -= count; source.setCount(remaining);
            EntityItem drop = new EntityItem(world, 4 + x, 8 + y, 12 + z, new ItemStack(item, count, meta));
            if(tag != null) drop.getItem().setTagCompound(tag.copy());
            float motion = 0.05F;
            drop.motionX = (float) rand.nextGaussian() * motion;
            drop.motionY = (float) rand.nextGaussian() * motion + 0.2F;
            drop.motionZ = (float) rand.nextGaussian() * motion;
            world.spawnEntity(drop);
        }
    }
    static ItemStack stack(int count, boolean tagged) {
        ItemStack result = new ItemStack(Items.STICK, count, 3);
        if(tagged) { NBTTagCompound tag = new NBTTagCompound(); tag.setString("marker", "preserve-final-drop"); result.setTagCompound(tag); }
        return result;
    }
    public static void main(String[] args) {
        Bootstrap.register();
        for(int method = 0; method < ExtractedMachineDrops.DROPS.length; method++) {
            ExtractedMachineDrops.Drop algorithm = ExtractedMachineDrops.DROPS[method];
            for(int count : new int[]{1, 9, 10, 21, 31, 64, 130}) for(boolean tagged : new boolean[]{false, true}) for(long seed : new long[]{0, 1, 42, 971}) {
                DropWorld actual = new DropWorld(), expected = new DropWorld();
                ItemStack source = stack(count, tagged), reference = stack(count, tagged), alias = source;
                NBTTagCompound originalTag = source.getTagCompound();
                Random a = new Random(seed), b = new Random(seed);
                algorithm.run(actual, source, a); legacy(expected, reference, b);
                check(source == alias && source.isEmpty() && reference.isEmpty(), "source alias/depletion " + method);
                check(a.nextLong() == b.nextLong(), "RNG order " + method);
                check(actual.drops.size() == expected.drops.size(), "drop count " + method);
                int total = 0;
                for(int i = 0; i < actual.drops.size(); i++) {
                    EntityItem got = actual.drops.get(i), want = expected.drops.get(i);
                    check(ItemStack.areItemStacksEqual(got.getItem(), want.getItem()), "item/meta/NBT including last drop " + method);
                    check(got.getItem() != source && got.getItem().getItem() == Items.STICK, "drop identity " + method);
                    check(got.posX == want.posX && got.posY == want.posY && got.posZ == want.posZ, "position " + method);
                    check(got.motionX == want.motionX && got.motionY == want.motionY && got.motionZ == want.motionZ, "motion " + method);
                    if(tagged) check(got.getItem().getTagCompound() != originalTag, "NBT deep copy " + method);
                    total += got.getItem().getCount();
                }
                check(total == count, "conservation " + method);
            }
            for(ItemStack empty : new ItemStack[]{null, ItemStack.EMPTY, stack(0, true), stack(-1, true)}) {
                DropWorld world = new DropWorld(); Random rng = new Random(42), untouched = new Random(42);
                algorithm.run(world, empty, rng);
                check(world.drops.isEmpty() && rng.nextLong() == untouched.nextLong(), "empty no RNG/drop " + method);
            }
        }
        check(ItemStack.EMPTY.isEmpty() && ItemStack.EMPTY.getCount() == 0, "shared EMPTY not mutated");
        System.out.println("PASS: MachineDropsNativeTest " + checks + " checks; " + ExtractedMachineDrops.DROPS.length + " extracted production blocks");
    }
}
