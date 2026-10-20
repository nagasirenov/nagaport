import com.hbm.util.SubChunkKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import java.net.URLClassLoader;
import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

/** Real target classes, not a world/gameplay test. Optional official 1.7.10 jar oracle. */
public final class ChunkPosMigrationTest {
    private static int checks;
    private static void check(boolean value, String label) {
        checks++;
        if (!value) throw new AssertionError(label);
    }
    private static int legacyHash(int x, int z) {
        // Verified against official 1.7.10 server agu.hashCode bytecode.
        return (1664525 * x + 1013904223) ^ (1664525 * (z ^ -559038737) + 1013904223);
    }
    private static void pair(int x, int z) {
        ChunkPos pos = new ChunkPos(x, z);
        check(pos.x == x && pos.z == z, "coordinates");
        long packed = ChunkPos.asLong(x, z);
        check((int) packed == x && (int) (packed >>> 32) == z, "signed packed round trip");
        check(pos.hashCode() == legacyHash(x, z), "legacy station ID/hash");
        check(pos.equals(new ChunkPos(x, z)), "value equality");
        check(!pos.equals(new ChunkPos(x ^ 1, z)), "different x");
        check(!pos.equals(new ChunkPos(x, z ^ 1)), "different z");
        HashMap<ChunkPos, String> map = new HashMap<>();
        map.put(pos, "waypoint");
        check("waypoint".equals(map.get(new ChunkPos(x, z))), "map lookup independent instance");
        HashSet<ChunkPos> set = new HashSet<>();
        set.add(pos);
        check(!set.add(new ChunkPos(x, z)), "decoration de-duplication");
        check(pos.getXStart() == (x << 4) && pos.getZStart() == (z << 4), "chunk start/impact positions");
        check(pos.getXEnd() == (x << 4) + 15 && pos.getZEnd() == (z << 4) + 15, "inclusive end");
        for (int y : new int[] {-1, 0, 15, 16, 255}) {
            SubChunkKey key = new SubChunkKey(pos, y);
            check(key.getPos().equals(pos) && key.getSubY() == y, "subchunk coordinates");
            check(key.hashCode() == 31 * (31 * y + x) + z, "subchunk hash preserved");
            check(key.equals(new SubChunkKey(x, z, y)), "subchunk equality");
            check(!key.equals(new SubChunkKey(x, z, y ^ 1)), "subchunk height distinct");
            HashMap<SubChunkKey, Integer> keys = new HashMap<>();
            keys.put(key, 7);
            check(keys.get(new SubChunkKey(x, z, y)) == 7, "destruction map lookup");
            SubChunkKey reusable = new SubChunkKey(0, 0, 0);
            check(reusable.update(x, z, y) == reusable && reusable.equals(key)
                    && reusable.hashCode() == key.hashCode(), "mutable lookup key update");
        }
        // Native NBT format contract. Source parity separately proves production retains these operations.
        NBTTagCompound entry = new NBTTagCompound();
        entry.setInteger("chunkX", pos.x);
        entry.setInteger("chunkZ", pos.z);
        entry.setFloat("soot", 3.25F);
        NBTTagList entries = new NBTTagList();
        entries.appendTag(entry);
        NBTTagCompound root = new NBTTagCompound();
        root.setTag("entries", entries);
        NBTTagCompound restored = root.copy().getTagList("entries", 10).getCompoundTagAt(0);
        ChunkPos restoredPos = new ChunkPos(restored.getInteger("chunkX"), restored.getInteger("chunkZ"));
        check(map.get(restoredPos).equals("waypoint") && restored.getFloat("soot") == 3.25F, "pollution coordinate NBT contract");
    }
    public static void main(String[] args) throws Exception {
        // Pinned from actual official 1.7.10 agu instances, not this test's formula.
        int[][] idCoords = {{0, 0}, {1, -1}, {-1, 1}, {-17, 16},
                {Integer.MIN_VALUE, Integer.MAX_VALUE}, {Integer.MAX_VALUE, Integer.MIN_VALUE}};
        String[] ids = {"0x56C52CDD", "0x3290F843", "0x56C7F427", "0x56FD6CD0", "0x32765270", "0x56FE52D0"};
        for (int i = 0; i < ids.length; i++) {
            check(ids[i].equals("0x" + Integer.toHexString(new ChunkPos(idCoords[i][0], idCoords[i][1]).hashCode()).toUpperCase()), "pinned station identifier");
        }
        int[] coords = {Integer.MIN_VALUE, -30000000, -65, -64, -63, -17, -16, -15, -1, 0, 1, 15, 16, 17, 63, 64, 65, 30000000, Integer.MAX_VALUE};
        for (int x : coords) for (int z : coords) pair(x, z);
        for (int block : coords) {
            ChunkPos pos = new ChunkPos(new BlockPos(block, 128, block));
            check(pos.x == Math.floorDiv(block, 16) && pos.z == Math.floorDiv(block, 16), "negative block-to-chunk floor");
            int pollutionCell = block >> 6;
            check(pollutionCell == Math.floorDiv(block, 64), "64-block pollution floor");
            check((pollutionCell << 6) <= block && block <= ((pollutionCell << 6) | 63), "pollution cell boundaries");
        }
        Random rng = new Random(0x4348554e4bL);
        for (int i = 0; i < 1000; i++) pair(rng.nextInt(), rng.nextInt());
        if (args.length != 0) {
            try (URLClassLoader old = new URLClassLoader(new java.net.URL[] {new File(args[0]).toURI().toURL()}, null)) {
                Class<?> type = old.loadClass("agu");
                Constructor<?> ctor = type.getConstructor(int.class, int.class);
                Method packed = type.getMethod("a", int.class, int.class);
                Method centerX = type.getMethod("a");
                Method centerZ = type.getMethod("b");
                for (int x : coords) for (int z : coords) {
                    Object legacy = ctor.newInstance(x, z);
                    ChunkPos target = new ChunkPos(x, z);
                    check(legacy.hashCode() == target.hashCode(), "official legacy binary hash oracle");
                    check(((Long) packed.invoke(null, x, z)) == ChunkPos.asLong(x, z), "official binary packed key oracle");
                    check(((Integer) centerX.invoke(legacy)) - 8 == target.getXStart(), "official binary center X oracle");
                    check(((Integer) centerZ.invoke(legacy)) - 8 == target.getZStart(), "official binary center Z oracle");
                }
            }
        }
        System.out.println("PASS: " + checks + " native ChunkPos/SubChunkKey checks; coordinate NBT contract (not full pollution class/gameplay)." );
    }
}
