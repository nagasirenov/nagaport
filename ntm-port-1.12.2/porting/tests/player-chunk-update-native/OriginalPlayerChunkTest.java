import java.lang.reflect.*;
import java.util.*;

/** Actual 1.7.10 getter/map/entry methods, without remapping original instructions. */
public class OriginalPlayerChunkTest {
    static final sun.misc.Unsafe U;
    static int checks;
    static StringBuilder trace = new StringBuilder();
    static mt selected;
    static {
        try { Field f = sun.misc.Unsafe.class.getDeclaredField("theUnsafe"); f.setAccessible(true); U = (sun.misc.Unsafe) f.get(null); }
        catch (Exception e) { throw new RuntimeException(e); }
    }
    static Field field(Class<?> c, String n) throws Exception { Field f = c.getDeclaredField(n); f.setAccessible(true); return f; }
    static void check(boolean b, String s) { checks++; if (!b) throw new AssertionError(s); }
    static mt receiver() { trace.append('r'); return selected; }
    static int coordinate(int n) { trace.append(n); return n == 2 ? 64 : n; }
    public static class Server extends mt {
        mq selected; boolean fail;
        public Server() { super(null, null, null, 0, null, null); }
        public mq t() { trace.append('g'); if (fail) throw new IllegalStateException(); return selected; }
    }
    public static class Map extends mq {
        public Map() { super(null); }
        public void a(int x, int y, int z) { trace.append('m'); check(x == 1 && y == 64 && z == 3, "old exact coordinates"); }
    }
    public static void main(String[] args) throws Exception {
        kl.b();
        for (int x : new int[] {Integer.MIN_VALUE, -30000001, -30000000, -17, -1, 0, 15, 16, 29999999, 30000000, Integer.MAX_VALUE})
        for (int y : new int[] {Integer.MIN_VALUE, -1, 0, 15, 16, 80, 255, 256, Integer.MAX_VALUE})
        for (int z : new int[] {-19, 0, 16}) {
            mt world = (mt) U.allocateInstance(mt.class);
            mq map = (mq) U.allocateInstance(mq.class);
            field(mt.class, "L").set(world, map); field(mq.class, "b").set(map, world);
            qd entries = new qd(); field(mq.class, "d").set(map, entries);
            List<Object> dirty = new ArrayList<>(); field(mq.class, "e").set(map, dirty);
            Class<?> entryClass = Class.forName("mr"); Object entry = U.allocateInstance(entryClass);
            field(entryClass, "a").set(entry, map); field(entryClass, "d").set(entry, new short[64]);
            long key = ((long) (x >> 4) + 2147483647L) | (((long) (z >> 4) + 2147483647L) << 32);
            entries.a(key, entry);
            // Actual WorldServer getter and direct manager call, no World listener path.
            // The original server chunk provider, listeners and entry chunk are unset.
            world.t().a(x, y, z); world.t().a(x, y, z);
            check(dirty.size() == 1 && dirty.get(0) == entry, "old dirty queue once");
            check(field(entryClass, "e").getInt(entry) == 1, "old duplicate coalescing");
            check(field(entryClass, "f").getInt(entry) == 1 << (y >> 4), "old section shift");
            check(((short[]) field(entryClass, "d").get(entry))[0] == (short) (((x & 15) << 12) | ((z & 15) << 8) | y), "old coordinate packing");
            entries.d(key); dirty.clear(); world.t().a(x, y, z);
            check(entries.a() == 0 && dirty.isEmpty(), "old existing-entry only with no chunk/state/callback");
        }
        Server server = (Server) U.allocateInstance(Server.class); selected = server;
        server.selected = (Map) U.allocateInstance(Map.class); trace.setLength(0);
        receiver().t().a(coordinate(1), coordinate(2), coordinate(3));
        check(trace.toString().equals("rg123m"), "old receiver getter arguments dispatch order");
        server.selected = null; trace.setLength(0);
        try { receiver().t().a(coordinate(1), coordinate(2), coordinate(3)); throw new AssertionError(); }
        catch (NullPointerException expected) { check(trace.toString().equals("rg123"), "old null manager after arguments"); }
        server.fail = true; trace.setLength(0);
        try { receiver().t().a(coordinate(1), coordinate(2), coordinate(3)); throw new AssertionError(); }
        catch (IllegalStateException expected) { check(trace.toString().equals("rg"), "old getter failure before arguments"); }
        selected = null; trace.setLength(0);
        try { receiver().t().a(coordinate(1), coordinate(2), coordinate(3)); throw new AssertionError(); }
        catch (NullPointerException expected) { check(trace.toString().equals("r"), "old null world before arguments"); }
        selected = server; server.fail = false; trace.setLength(0);
        try { receiver().t().a(coordinate(1), (new int[0])[coordinate(2)], coordinate(3)); throw new AssertionError(); }
        catch (ArrayIndexOutOfBoundsException expected) { check(trace.toString().equals("rg12"), "old coordinate failure before null manager and final coordinate"); }
        System.out.println("PASS: " + checks + " authentic original direct manager no-state/no-create, duplicate queue and evaluation assertions");
    }
}
