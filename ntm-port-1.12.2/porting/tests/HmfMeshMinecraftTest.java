import com.hbm.render.loader.HmfController;
import com.hbm.render.mesh.*;
import com.hbm.render.mesh.ObjMeshRenderer.Shading;
import com.hbm.util.Clock;
import java.io.*;
import java.lang.reflect.Field;
import java.nio.*;
import java.nio.file.*;
import java.util.*;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.resources.*;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

/** Actual production mesh, Clock and HmfController classes; headless native bytes, not a GL draw. */
public final class HmfMeshMinecraftTest {
    static int assertions;
    static void check(boolean value, String label) {
        assertions++;
        if(!value) throw new AssertionError(label);
    }
    static void same(float actual, float expected, String label) {
        check(Float.floatToIntBits(actual) == Float.floatToIntBits(expected), label + ": " + actual + " != " + expected);
    }
    static ByteBuffer emit(ObjMesh mesh, Shading shading, float offset) {
        check(mesh.parts.size() == 1, "bounded Tom migration requires its actual single source part");
        List<ObjMesh.Face> faces = mesh.parts.get(0).faces;
        for(ObjMesh.Face face : faces) {
            check(face.corners.size() == 3, "Tom source is triangles");
            for(ObjMesh.Corner corner : face.corners)
                check(corner.uv != null && corner.normal != null, "Tom source has every UV and normal");
        }
        BufferBuilder buffer = new BufferBuilder(32);
        buffer.begin(GL11.GL_TRIANGLES, ObjMeshRenderer.format(faces, shading));
        ObjMeshRenderer.append(buffer, faces, shading, offset, new ObjMeshRenderer.Cursor());
        buffer.finishDrawing();
        check(buffer.getVertexCount() == faces.size() * 3, "unchanged raw triangle corner count");
        ByteBuffer copy = ByteBuffer.allocate(buffer.getByteBuffer().remaining()).order(ByteOrder.nativeOrder());
        copy.put(buffer.getByteBuffer().duplicate()).flip();
        return copy;
    }

    static final class Resources implements IResourceManager {
        final Path root;
        int reads;
        Resources(Path root) { this.root = root; }
        public Set<String> getResourceDomains() { return Collections.singleton("hbm"); }
        public IResource getResource(ResourceLocation location) throws IOException {
            reads++;
            check(location.getNamespace().equals("hbm"), "exact Tom asset namespace");
            return new SimpleResource("Tom asset test", location,
                    Files.newInputStream(root.resolve(location.getPath().substring("models/".length()))), null, null);
        }
        public List<IResource> getAllResources(ResourceLocation location) throws IOException {
            return Collections.singletonList(getResource(location));
        }
    }

    public static void main(String[] args) throws Exception {
        Path root = Paths.get(args[0]);
        // Reuse only the independent actual-resource byte oracle, not the previous 102k test suite.
        ObjMeshMinecraftTest.resource(root, "weapons/tom_main.obj", 106, Shading.LEGACY_FLAT);
        ObjMeshMinecraftTest.resource(root, "weapons/tom_flame.hmf", 60, Shading.HMF);
        Resources resources = new Resources(root);
        ReloadableObjMesh main = new ReloadableObjMesh(resources,
                new ResourceLocation("hbm", "models/weapons/tom_main.obj"), false);
        ReloadableObjMesh flame = new ReloadableObjMesh(resources,
                new ResourceLocation("hbm", "models/weapons/tom_flame.hmf"), false);
        check(resources.reads == 2, "two eager reads in main/flame field order");
        ObjMesh originalMain = main.getMesh(), originalFlame = flame.getMesh();
        ByteBuffer fixed = emit(originalMain, Shading.LEGACY_FLAT, 0);
        ByteBuffer baseline = emit(originalFlame, Shading.HMF, 0);
        check(fixed.remaining() == 318 * 24, "106 main triangles retain 318 native vertices");
        check(baseline.remaining() == 180 * 24, "60 flame triangles retain 180 native vertices");
        boolean wrapped = false;
        for(int i = 0; i < 180; i++) {
            float v = baseline.getFloat(i * 24 + 16);
            wrapped |= v < 0 || v > 1;
        }
        check(wrapped, "real flame UVs remain outside the atlas unit range");

        Field cached = Clock.class.getDeclaredField("time_ms");
        cached.setAccessible(true);
        long savedTime = cached.getLong(null);
        double savedModulo = HmfController.modoloMod, savedQuotient = HmfController.quotientMod;
        int precisionDifferences = 0;
        try {
            HmfController.resetMod();
            check(HmfController.modoloMod == 100000D && HmfController.quotientMod == 5000D, "original controller defaults");
            HmfController.setMod(50000D, 2500D);
            check(HmfController.modoloMod == 50000D && HmfController.quotientMod == 2500D, "Tom controller values");
            for(long time : new long[]{0, -1, 49999, 50000, 100001, 16777217L, 1700000000001L, 1792396800123L, Long.MAX_VALUE}) {
                cached.setLong(null, time);
                for(int draw = 0; draw < 20; draw++) {
                    // Exact production call-site conversion: long cached frame clock to float BEFORE modulo.
                    float currentTime = Clock.get_ms();
                    float actual = ObjMeshRenderer.hmfOffset(currentTime, HmfController.modoloMod, HmfController.quotientMod);
                    float expected = (float)(((double)(float)time % 50000D) / 2500D);
                    same(actual, expected, "cached long -> float -> double modulo/division");
                    check(Clock.get_ms() == time, "draw never refreshes cached clock or calls Clock.update");
                    if(draw == 0 && Float.floatToIntBits(actual) != Float.floatToIntBits((float)((time % 50000D) / 2500D))) precisionDifferences++;
                    ByteBuffer animated = emit(originalFlame, Shading.HMF, actual);
                    for(int vertex = 0; vertex < 180; vertex++) {
                        int off = vertex * 24;
                        for(int byteIndex = 0; byteIndex < 24; byteIndex++) {
                            if(byteIndex >= 16 && byteIndex < 20) continue;
                            check(animated.get(off + byteIndex) == baseline.get(off + byteIndex), "animation changes only V, not geometry/U/normal/padding");
                        }
                        same(animated.getFloat(off + 16), baseline.getFloat(off + 16) + expected, "V inset then animation addition");
                    }
                }
            }
            check(precisionDifferences > 0, "fixtures detect forbidden long/double-before-float clock rewrite");
            HmfController.resetMod();
            check(HmfController.modoloMod == 100000D && HmfController.quotientMod == 5000D, "Tom reset restores original global defaults");
            check(resources.reads == 2 && main.getMesh() == originalMain && flame.getMesh() == originalFlame,
                    "draws retain eager snapshots without extra resource reads or reload subscription");
        } finally {
            cached.setLong(null, savedTime);
            HmfController.setMod(savedModulo, savedQuotient);
        }
        System.out.println("PASS " + (assertions + ObjMeshMinecraftTest.assertions) + " assertions: two actual Tom assets, native BufferBuilder bytes, actual cached Clock/HmfController, float-loss timing and eager snapshots.");
        System.out.println("LIMIT: TomPronter/ResourceManager source delta audited, not whole-class native compilation or execution. No GL/client/GPU/VBO/resource-pack visual acceptance; no general multi-part renderAll migration claimed.");
    }
}
