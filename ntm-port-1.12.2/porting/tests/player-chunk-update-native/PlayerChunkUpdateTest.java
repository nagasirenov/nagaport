import java.util.*;
import java.lang.reflect.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Bootstrap;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.*;
import net.minecraft.server.management.*;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.*;
import net.minecraft.world.*;
import net.minecraft.world.chunk.Chunk;
import it.unimi.dsi.fastutil.longs.*;

/** Reuses CP88 real-native fixtures; startup/transport are the only seams. */
public class PlayerChunkUpdateTest extends NotificationTest {
    static class Fixture {
        final WorldServer world;
        final PlayerChunkMap map;
        final Long2ObjectOpenHashMap<PlayerChunkMapEntry> entries = new Long2ObjectOpenHashMap<>();
        final Set<PlayerChunkMapEntry> dirty = new HashSet<>();
        final Entry entry;
        Fixture(BlockPos p, boolean sent) throws Exception {
            world = (WorldServer) U.allocateInstance(WorldServer.class);
            map = (PlayerChunkMap) U.allocateInstance(PlayerChunkMap.class);
            field(WorldServer.class, "playerChunkMap").set(world, map);
            field(PlayerChunkMap.class, "world").set(map, world);
            field(PlayerChunkMap.class, "entryMap").set(map, entries);
            field(PlayerChunkMap.class, "dirtyEntries").set(map, dirty);
            entry = (Entry) U.allocateInstance(Entry.class);
            entry.packets = new ArrayList<>();
            field(PlayerChunkMapEntry.class, "playerChunkMap").set(entry, map);
            field(PlayerChunkMapEntry.class, "pos").set(entry, new ChunkPos(p.getX() >> 4, p.getZ() >> 4));
            field(PlayerChunkMapEntry.class, "changedBlocks").set(entry, new short[64]);
            field(PlayerChunkMapEntry.class, "sentToPlayers").setBoolean(entry, sent);
            Method index = PlayerChunkMap.class.getDeclaredMethod("getIndex", int.class, int.class);
            index.setAccessible(true);
            entries.put((Long) index.invoke(null, p.getX() >> 4, p.getZ() >> 4), entry);
            // Chunk provider, entry chunk, event listeners and server are deliberately null.
            // Any attempt to read storage, invoke a World listener or construct a chunk fails.
        }
        int changes() throws Exception { return field(PlayerChunkMapEntry.class, "changes").getInt(entry); }
        int sections() throws Exception { return field(PlayerChunkMapEntry.class, "changedSectionFilter").getInt(entry); }
        short packed() throws Exception { return ((short[]) field(PlayerChunkMapEntry.class, "changedBlocks").get(entry))[0]; }
    }

    static void call(int index, WorldServer world, BlockPos p) {
        SelectedPlayerCalls owner = new SelectedPlayerCalls();
        owner.probePosition(p);
        TileEntity peer = new TileEntity() {}; peer.setPos(p);
        owner.apply(index, world, p.getX(), p.getY(), p.getZ(), peer, peer);
    }

    static void nativeQueue() throws Exception {
        int[] xs = {Integer.MIN_VALUE, -30000001, -30000000, -17, -1, 0, 15, 16, 29999999, 30000000, Integer.MAX_VALUE};
        int[] ys = {Integer.MIN_VALUE, -1, 0, 15, 16, 80, 255, 256, Integer.MAX_VALUE};
        for (int i = 0; i < 7; i++) for (int x : xs) for (int y : ys) for (int z : new int[] {-19, 0, 16}) {
            BlockPos p = new BlockPos(x, y, z);
            Fixture f = new Fixture(p, true);
            call(i, f.world, p); call(i, f.world, p);
            check(f.dirty.size() == 1 && f.dirty.contains(f.entry), "one existing watched entry queued");
            check(f.changes() == 1, "duplicate coalesced without any storage access");
            check(f.sections() == 1 << (y >> 4), "unchanged Java section shift including out-of-domain ints");
            check(f.packed() == (short) (((x & 15) << 12) | ((z & 15) << 8) | y), "same original local coordinate packing");
            check(f.entry.packets.isEmpty(), "never an early packet");
            f.entries.clear(); f.dirty.clear();
            call(i, f.world, p);
            check(f.entries.isEmpty() && f.dirty.isEmpty() && f.changes() == 1, "unwatched means no creation, queue, state read or callback");
        }
        Fixture pending = new Fixture(new BlockPos(0, 80, 0), false);
        for (int i = 0; i < 7; i++) call(i, pending.world, new BlockPos(0, 80, 0));
        check(pending.dirty.isEmpty() && pending.changes() == 0 && pending.sections() == 0, "native unsent-entry gate retained, not emulated away");
    }

    static void deferredPackets() throws Exception {
        for (int i = 0; i < 7; i++) {
            MetaBlock block = new MetaBlock(); TestWorld source = new TestWorld(false);
            BlockPos p = new BlockPos(-17, 80, 19); IBlockState state = block.getStateFromMeta(11);
            TestTile tile = new TestTile(); tile.paint = block; Chunk chunk = seed(source, p, state, tile);
            Fixture f = new Fixture(p, true);
            field(World.class, "chunkProvider").set(f.world, source.createChunkProvider());
            field(World.class, "addedTileEntityList").set(f.world, new ArrayList<TileEntity>());
            field(PlayerChunkMapEntry.class, "chunk").set(f.entry, chunk);
            call(i, f.world, p);
            check(source.requests == 0 && tile.packetRequests == 0, "direct scheduling does not read state or tile");
            tile.paint = null; // A later change must be observed by native deferred packet construction.
            f.entry.update();
            check(f.entry.packets.size() == 2, "native deferred block and tile packets");
            SPacketBlockChange statePacket = (SPacketBlockChange) f.entry.packets.get(0);
            check(statePacket.getBlockPosition().equals(p) && statePacket.getBlockState() == state, "actual block packet captures stored state");
            SPacketUpdateTileEntity tilePacket = (SPacketUpdateTileEntity) f.entry.packets.get(1);
            check(tilePacket.getPos().equals(p) && tilePacket.getNbtCompound().getInteger("paint") == 0 && tile.packetRequests == 1, "actual tile packet captures later data");
            check(f.changes() == 0 && f.sections() == 0, "native queue flush completed");
            check(source.mutations == 0 && source.neighbors == 0 && source.light == 0, "no block write, neighbor or light effect");
        }
    }

    static class ObservedMap extends PlayerChunkMap {
        ObservedMap() { super(null); }
        public void markBlockForUpdate(BlockPos p) {
            trace.append('m');
            check(p.getClass() == BlockPos.class && p.equals(new BlockPos(1, 64, 3)), "one exact immutable native position");
        }
    }
    static class ObservedServer extends WorldServer {
        PlayerChunkMap selected; boolean fail;
        ObservedServer() { super(null, null, null, 0, null); }
        public PlayerChunkMap getPlayerChunkMap() {
            trace.append('g'); if (fail) throw new IllegalStateException("getter failure"); return selected;
        }
    }
    static class ObservedPos extends BlockPos {
        boolean failY;
        ObservedPos() { super(1, 64, 3); }
        public int getX() { trace.append('x'); return 1; }
        public int getY() { trace.append('y'); if (failY) throw new IllegalArgumentException("y failure"); return 64; }
        public int getZ() { trace.append('z'); return 3; }
    }
    static class ObservedTile extends TileEntity {
        BlockPos selected;
        public BlockPos getPos() { trace.append('p'); return selected; }
    }
    static WorldServer selectedServer;
    static WorldServer receiver() { trace.append('r'); return selectedServer; }
    static int coordinate(int n) { trace.append(n); return n == 2 ? 64 : n; }
    static void evaluationOrder() throws Exception {
        ObservedServer server = (ObservedServer) U.allocateInstance(ObservedServer.class);
        server.selected = (ObservedMap) U.allocateInstance(ObservedMap.class);
        SelectedPlayerCalls owner = new SelectedPlayerCalls();
        ObservedPos p = new ObservedPos(); owner.probePosition(p);
        ObservedTile peer = new ObservedTile(); peer.selected = p;
        for (int i = 0; i < 7; i++) {
            String coordinates = i == 0 ? "" : (i == 4 || i == 6 ? "pxpypz" : "xyz");
            trace.setLength(0); owner.apply(i, server, 1, 64, 3, peer, peer);
            check(trace.toString().equals("g" + coordinates + "m"), "exact extracted call getter/coordinates once in original order: " + i + " " + trace);
            PlayerChunkMap map = server.selected; server.selected = null;
            trace.setLength(0);
            try { owner.apply(i, server, 1, 64, 3, peer, peer); throw new AssertionError(); }
            catch (NullPointerException expected) { check(trace.toString().equals("g" + coordinates), "null map throws after coordinates"); }
            server.selected = map; server.fail = true; trace.setLength(0);
            try { owner.apply(i, server, 1, 64, 3, peer, peer); throw new AssertionError(); }
            catch (IllegalStateException expected) { check(trace.toString().equals("g"), "getter failure precedes coordinates"); }
            server.fail = false; trace.setLength(0);
            try { owner.apply(i, null, 1, 64, 3, peer, peer); throw new AssertionError(); }
            catch (NullPointerException expected) { check(trace.length() == 0, "null world fails before coordinates"); }
            if (i > 0) {
                p.failY = true; trace.setLength(0);
                try { owner.apply(i, server, 1, 64, 3, peer, peer); throw new AssertionError(); }
                catch (IllegalArgumentException expected) { check(trace.toString().equals("g" + (i == 4 || i == 6 ? "pxpy" : "xy")), "coordinate exception before later coordinate/update"); }
                p.failY = false;
            }
        }
        selectedServer = server; trace.setLength(0);
        receiver().getPlayerChunkMap().markBlockForUpdate(new BlockPos(coordinate(1), coordinate(2), coordinate(3)));
        check(trace.toString().equals("rg123m"), "full receiver then getter then int argument order");
    }

    public static void main(String[] args) throws Exception {
        Bootstrap.register(); nativeQueue(); deferredPackets(); evaluationOrder();
        System.out.println("PASS: " + checks + " native direct-map queue, no-state/no-create, deferred packet and extracted-call evaluation assertions");
    }
}
