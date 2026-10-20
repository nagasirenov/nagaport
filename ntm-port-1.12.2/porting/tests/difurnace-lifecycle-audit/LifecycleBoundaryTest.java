import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLClassLoader;
import net.minecraft.init.Bootstrap;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

/** Counterexample audit, NOT a machine execution or accepted migration test. */
public final class LifecycleBoundaryTest {
    private static int checks;
    private static void check(boolean value, String message) {
        checks++;
        if (!value) throw new AssertionError(message);
    }
    public static void main(String[] args) throws Exception {
        Bootstrap.register();
        Field raw = ReflectionHelper.findField(ItemStack.class, "stackSize", "field_77994_a");
        try (URLClassLoader old = new URLClassLoader(new URL[] {new File(args[0]).toURI().toURL()}, LifecycleBoundaryTest.class.getClassLoader())) {
            Class<?> blocks = old.loadClass("aji"), items = old.loadClass("adb"), stacks = old.loadClass("add");
            blocks.getMethod("p").invoke(null);
            items.getMethod("l").invoke(null);
            Object coal = items.getMethod("d", int.class).invoke(null, 263);
            Constructor<?> ctor = stacks.getConstructor(items, int.class, int.class);
            Field oldCount = stacks.getField("b");
            for (int n : new int[] {Integer.MIN_VALUE, -1, 0, 1, 64, Integer.MAX_VALUE}) {
                Object before = ctor.newInstance(coal, n, 0);
                ItemStack after = new ItemStack(Items.COAL, n);
                check(stacks.getMethod("b").invoke(before) == coal, "original coal identity " + n);
                check(raw.getInt(after) == oldCount.getInt(before), "raw quantity " + n);
                check((after.getItem() == Items.COAL) == (n > 0), "native identity depends on count " + n);
                check((after.getCount() == n) == (n >= 0), "native count projection " + n);
                check(raw.getInt(after.copy()) == n, "copy preserves raw quantity " + n);
                check((after.copy().getItem() == Items.COAL) == (n > 0), "copy does not restore visible identity " + n);
            }
            ItemStack lava = new ItemStack(Items.LAVA_BUCKET, 1);
            Object oldLava = items.getMethod("d", int.class).invoke(null, 327);
            Object oldDepleted = ctor.newInstance(oldLava, 0, 0);
            lava.shrink(1);
            check(stacks.getMethod("b").invoke(oldDepleted) == oldLava, "original depleted lava identity");
            check(lava.getItem() != Items.LAVA_BUCKET, "native depleted lava callback receiver lost");
            check(raw.getInt(ItemStack.EMPTY) == 1, "canonical EMPTY is not legacy quantity zero");
        }
        System.out.println("PASS " + checks + " genuine old/native boundary checks; no reflection writes, no singleton mutation; machine execution NOT claimed");
    }
}
