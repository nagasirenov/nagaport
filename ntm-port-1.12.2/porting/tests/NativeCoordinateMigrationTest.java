import com.hbm.tileentity.machine.SolarMirrorPosition;
import net.minecraft.util.math.BlockPos;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import java.io.File;
import java.net.URLClassLoader;
import java.lang.reflect.Constructor;
import java.util.*;

/** Actual production key/native target API tests; not full consumer/gameplay execution. */
public final class NativeCoordinateMigrationTest {
    private static int checks;
    private static Constructor<?> legacy;
    private static void check(boolean value, String label) {
        checks++;
        if (!value) throw new AssertionError(label);
    }
    private static int oracle(int x, int y, int z) {
        // Exact legacy binary instruction sequence: iadd, iadd, ishl, ishl.
        int sum = x + z;
        int shifted = sum << ((8 + y) & 31);
        return shifted << 16;
    }
    private static void point(int x, int y, int z) throws Exception {
        SolarMirrorPosition key = new SolarMirrorPosition(x, y, z);
        check(key.getX() == x && key.getY() == y && key.getZ() == z, "full signed coordinates");
        check(key.hashCode() == oracle(x, y, z), "legacy binary expression hash");
        check(key.equals(new SolarMirrorPosition(x, y, z)), "duplicate equality");
        check(!key.equals(new SolarMirrorPosition(x ^ 1, y, z)), "different x");
        check(!key.equals(new SolarMirrorPosition(x, y ^ 1, z)), "different y");
        check(!key.equals(new SolarMirrorPosition(x, y, z ^ 1)), "different z");
        check(!key.equals(null) && !key.equals(new BlockPos(x, y, z)), "domain equality boundary");
        check(!new BlockPos(x, y, z).equals(key), "symmetric native inequality");
        HashSet<SolarMirrorPosition> set = new HashSet<>();
        check(set.add(key) && !set.add(new SolarMirrorPosition(x, y, z)), "set deduplication");
        if (legacy != null) {
            Object old = legacy.newInstance(x, y, z);
            check(key.hashCode() == old.hashCode(), "official 1.7.10 binary hash");
            check(old.equals(legacy.newInstance(x, y, z)), "official binary equality");
        }
        BlockPos.MutableBlockPos spawn = new BlockPos.MutableBlockPos();
        BlockPos.MutableBlockPos alias = spawn;
        check(spawn.getX() == 0 && spawn.getY() == 0 && spawn.getZ() == 0, "spawn origin");
        check(spawn.setPos(x, y, z) == alias, "mutable identity");
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setInteger("spawnX", spawn.getX());
        nbt.setInteger("spawnY", spawn.getY());
        nbt.setInteger("spawnZ", spawn.getZ());
        spawn.setPos(1, 2, 3);
        spawn.setPos(nbt.getInteger("spawnX"), nbt.getInteger("spawnY"), nbt.getInteger("spawnZ"));
        check(alias.getX() == x && alias.getY() == y && alias.getZ() == z, "NBT restore preserves alias");
        BlockPos snapshot = spawn.toImmutable();
        spawn.setPos(0, 0, 0);
        check(snapshot.getX() == x && snapshot.getY() == y && snapshot.getZ() == z, "immutable snapshot");
    }
    private static List<Integer> beamXs(Set<SolarMirrorPosition> set, int limit) {
        List<Integer> result = new ArrayList<>();
        int beamCount = 0;
        for (SolarMirrorPosition p : set) {
            beamCount++;
            if (beamCount > limit) break;
            result.add(p.getX());
        }
        return result;
    }
    private static void queues() throws Exception {
        HashSet<SolarMirrorPosition> primary = new HashSet<>(), secondary = new HashSet<>();
        Set<Object> oldPrimary = new HashSet<>(), oldSecondary = new HashSet<>();
        // Six same-hash entries remain below JDK8 treeification: deterministic fixture.
        for (int i = 0; i < 6; i++) {
            primary.add(new SolarMirrorPosition(i, 64, i * 3));
            primary.add(new SolarMirrorPosition(i, 64, i * 3));
            if (legacy != null) oldPrimary.add(legacy.newInstance(i, 64, i * 3));
        }
        check(primary.size() == 6 && secondary.isEmpty(), "registrations delayed");
        secondary.clear(); secondary.addAll(primary); primary.clear();
        check(primary.isEmpty() && secondary.size() == 6, "queue rollover");
        check(beamXs(secondary, 3).equals(Arrays.asList(0, 1, 2)), "legacy limited beam fixture");
        check(beamXs(secondary, 0).isEmpty() && beamXs(secondary, -1).isEmpty(), "nonpositive beam limit");
        check(beamXs(secondary, 99).equals(Arrays.asList(0, 1, 2, 3, 4, 5)), "all beam order fixture");
        if (legacy != null) {
            oldSecondary.clear(); oldSecondary.addAll(oldPrimary); oldPrimary.clear();
            List<Integer> oldXs = new ArrayList<>();
            for (Object p : oldSecondary) oldXs.add(p.getClass().getField("a").getInt(p));
            check(oldXs.equals(beamXs(secondary, 99)), "official binary non-tree iteration fixture");
        }
        secondary.clear(); secondary.addAll(primary); primary.clear();
        check(secondary.isEmpty(), "empty tick clears stale beams");
        // Collision-heavy histories: membership, not identity-dependent tree iteration.
        for (int tick = 0; tick < 5; tick++) {
            for (int i = 0; i < 128; i++) primary.add(new SolarMirrorPosition(i, 24, i));
            secondary.clear(); secondary.addAll(primary); primary.clear();
            check(secondary.size() == 128, "tree-bin registration count");
            for (int i = 0; i < 128; i++) check(secondary.contains(new SolarMirrorPosition(i, 24, i)), "tree-bin membership");
        }
    }
    // Synthetic sender API fixture, NOT execution of Entity.getPosition or CommandSatellites.
    private static void nativeCommandPosition() {
        for (final double value : new double[] {-17.99, -1.01, -0.99, -0.01, 0, 0.99, 1.01}) {
            ICommandSender sender = new ICommandSender() {
                public String getName() { return "coordinate-test"; }
                public boolean canUseCommand(int level, String name) { return false; }
                public World getEntityWorld() { return null; }
                public MinecraftServer getServer() { return null; }
                // Both official legacy player and native Entity bytecode use this rounding.
                public BlockPos getPosition() { return new BlockPos(value, value + 0.5, value); }
            };
            BlockPos p = sender.getPosition();
            check(p.getX() == (int)Math.floor(value) && p.getZ() == (int)Math.floor(value), "sender floor X/Z");
            check(p.getY() == (int)Math.floor(value + 0.5), "sender half-offset Y");
        }
        check(new BlockPos(-0.01, 0, -0.01).getX() == -1 && (int)-0.01 == 0, "floor and cast differ");
    }
    public static void main(String[] args) throws Exception {
        URLClassLoader loader = null;
        if (args.length != 0) {
            loader = new URLClassLoader(new java.net.URL[] {new File(args[0]).toURI().toURL()}, null);
            legacy = loader.loadClass("r").getConstructor(int.class, int.class, int.class);
        }
        check(!Comparable.class.isAssignableFrom(SolarMirrorPosition.class), "no new tree-bin comparison");
        check(!BlockPos.class.isAssignableFrom(SolarMirrorPosition.class), "no native hash contract violation");
        int[] edges = {Integer.MIN_VALUE, -30000000, -33, -32, -31, -9, -8, -1, 0, 1, 7, 8, 15, 16, 23, 24, 31, 32, 33, 64, 255, 30000000, Integer.MAX_VALUE};
        for (int v : edges) for (int y : edges) point(v, y, ~v);
        Random rng = new Random(0x534f4c4152L);
        for (int i = 0; i < 4096; i++) point(rng.nextInt(), rng.nextInt(), rng.nextInt());
        queues(); nativeCommandPosition();
        if (loader != null) loader.close();
        System.out.println("PASS: " + checks + " actual SolarMirrorPosition/native coordinate checks" +
                (legacy != null ? "; official legacy binary oracle" : "; checked legacy expression oracle") +
                ". Not full consumer compilation/gameplay.");
    }
}
