package furnaceoracle;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import sun.misc.Unsafe;

/** Optional actual official-1.7.10 bytecode oracle; no copied/fake Minecraft types. */
public final class ElectricFurnaceLegacyFaceOracle {
    private static int assertions;
    private static void check(boolean value, String message) {
        assertions++;
        if(!value) throw new AssertionError(message);
    }
    private static float[] expectedUV(int side, float x, float y, float z) {
        switch(side) {
            case 0: case 1: return new float[]{x, z};
            case 2: return new float[]{1-x, 1-y};
            case 3: return new float[]{x, 1-y};
            case 4: return new float[]{z, 1-y};
            case 5: return new float[]{1-z, 1-y};
            default: throw new AssertionError(side);
        }
    }
    public static void main(String[] args) throws Exception {
        Field uf = Unsafe.class.getDeclaredField("theUnsafe"); uf.setAccessible(true);
        Unsafe unsafe = (Unsafe) uf.get(null);
        Class<?> renderer = Class.forName("blm"), block = Class.forName("aji"), iconType = Class.forName("rf"), tessType = Class.forName("bmh");
        // Skip the game singleton constructor; actual face methods only need bounds,
        // zero rotation/flip flags, the provided real-IIcon proxy and actual Tessellator.
        Object render = unsafe.allocateInstance(renderer), tess = tessType.getField("a").get(null);
        for(String name : new String[]{"j", "l", "n"}) {
            Field maximum = renderer.getDeclaredField(name); maximum.setAccessible(true); maximum.setDouble(render, 1);
        }
        Object icon = Proxy.newProxyInstance(iconType.getClassLoader(), new Class<?>[]{iconType}, (proxy, method, arguments) -> {
            if(method.getParameterCount() > 0) return ((Double) arguments[0]).floatValue() / 16;
            switch(method.getName()) {
                case "a": case "b": return 16;
                case "c": case "e": return 0F;
                case "d": case "f": return 1F;
                default: return "furnace-uv-oracle";
            }
        });
        Field raw = tessType.getDeclaredField("f"), count = tessType.getDeclaredField("g"), pointer = tessType.getDeclaredField("p");
        raw.setAccessible(true); count.setAccessible(true); pointer.setAccessible(true);
        for(int side = 0; side < 6; side++) {
            count.setInt(tess, 0); pointer.setInt(tess, 0);
            String name = String.valueOf((char) ('a' + side));
            renderer.getMethod(name, block, double.class, double.class, double.class, iconType).invoke(render, null, 0D, 0D, 0D, icon);
            check(count.getInt(tess) == 4, "actual old face emits four vertices");
            int[] vertices = (int[]) raw.get(tess);
            for(int vertex = 0; vertex < 4; vertex++) {
                int offset = vertex * 8;
                float x = Float.intBitsToFloat(vertices[offset]), y = Float.intBitsToFloat(vertices[offset+1]), z = Float.intBitsToFloat(vertices[offset+2]);
                float[] uv = expectedUV(side, x, y, z);
                for(float coordinate : new float[]{x, y, z}) check(coordinate == 0 || coordinate == 1, "full cube vertex bounds");
                float plane = side < 2 ? y : side < 4 ? z : x;
                check(plane == (side % 2), "actual old face side numbering");
                check(Float.intBitsToFloat(vertices[offset+3]) == uv[0], "actual old U side " + side);
                check(Float.intBitsToFloat(vertices[offset+4]) == uv[1], "actual old V side " + side);
            }
        }
        System.out.println("PASS official 1.7.10 RenderBlocks six-face UV oracle: " + assertions + " assertions; actual Tessellator bytes, no GL draw.");
    }
}
