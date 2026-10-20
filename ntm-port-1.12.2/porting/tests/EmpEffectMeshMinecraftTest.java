import java.io.Reader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import com.hbm.render.mesh.ObjMesh;
import com.hbm.render.mesh.ObjMeshParser;
import com.hbm.render.mesh.ObjMeshRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;

/** Independent raw-file oracle for the two single-part plain Forge consumers.
 * Real native BufferBuilder output, no renderer initialization or GL context. */
public final class EmpEffectMeshMinecraftTest {
    private static int checks;
    private static void check(boolean ok, String label) {
        checks++;
        if(!ok) throw new AssertionError(label);
    }
    private static void same(float actual, float expected, String label) {
        check(Float.floatToIntBits(actual) == Float.floatToIntBits(expected), label + ": " + actual + " != " + expected);
    }
    private static Path resource(Path assets, ResourceLocation location) {
        return assets.resolve(location.getNamespace()).resolve(location.getPath());
    }
    private static void canonicalResource(Path assets, ResourceLocation location, String expectedPath,
                                          String expectedSha256) throws Exception {
        check(location.getNamespace().equals("hbm"), "native resource namespace: " + location);
        check(location.getPath().equals(expectedPath), "native canonical resource path: " + location);
        Path file = resource(assets, location);
        check(Files.isRegularFile(file), "native canonical resource lookup: " + location);
        byte[] digest = MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(file));
        StringBuilder hex = new StringBuilder();
        for(byte value : digest) hex.append(String.format("%02x", value & 255));
        // Frozen original-byte hashes also recorded in resource-case-migration.json.
        check(hex.toString().equals(expectedSha256), "byte-preserved resource rename: " + location);
        System.out.println("PASS native canonical resource " + location + ": original SHA-256 preserved.");
    }
    private static void mesh(Path root, ResourceLocation location, int expectedFaces) throws Exception {
        String name = location.toString();
        Path file = resource(root, location);
        List<float[]> positions = new ArrayList<>(), uvs = new ArrayList<>();
        List<int[][]> faces = new ArrayList<>();
        List<String> groups = new ArrayList<>();
        int normals = 0;
        for(String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
            String[] s = line.trim().split("\\s+");
            if(s[0].equals("v")) positions.add(new float[]{Float.parseFloat(s[1]), Float.parseFloat(s[2]), Float.parseFloat(s[3])});
            if(s[0].equals("vt")) uvs.add(new float[]{Float.parseFloat(s[1]), 1F - Float.parseFloat(s[2])});
            if(s[0].equals("vn")) normals++;
            if(s[0].equals("o") || s[0].equals("g")) groups.add(s[1]);
            if(s[0].equals("f")) {
                check(s.length == 4, name + " only triangles");
                int[][] face = new int[3][3];
                for(int i = 1; i < 4; i++) {
                    String[] corner = s[i].split("/", -1);
                    check(corner.length == 3, name + " fully attributed corner");
                    for(int j = 0; j < 3; j++) face[i - 1][j] = Integer.parseInt(corner[j]) - 1;
                }
                faces.add(face);
            }
        }
        check(faces.size() == expectedFaces && groups.size() == 1, name + " original single-part topology");
        ObjMesh parsed;
        try(Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            parsed = ObjMeshParser.parse(reader, name, false);
        }
        check(parsed.parts.size() == 1 && parsed.parts.get(0).name.equals(groups.get(0)), name + " exact part name");
        check(parsed.parts.get(0).faces.size() == faces.size(), name + " parser face count");
        check(ObjMeshRenderer.format(parsed.parts.get(0).faces, ObjMeshRenderer.Shading.LEGACY_FLAT)
              .equals(DefaultVertexFormats.POSITION_TEX_NORMAL), name + " native textured-normal format");
        BufferBuilder buffer = new BufferBuilder(32);
        buffer.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_TEX_NORMAL);
        ObjMeshRenderer.append(buffer, parsed.parts.get(0).faces, ObjMeshRenderer.Shading.LEGACY_FLAT,
                               0, new ObjMeshRenderer.Cursor());
        buffer.finishDrawing();
        ByteBuffer data = buffer.getByteBuffer().duplicate().order(ByteOrder.nativeOrder());
        check(buffer.getVertexCount() == faces.size() * 3 && data.remaining() == faces.size() * 3 * 24,
              name + " exact vertex count and byte length");
        int vertex = 0;
        for(int[][] face : faces) {
            float[] a = positions.get(face[0][0]), b = positions.get(face[1][0]), c = positions.get(face[2][0]);
            // Independent native vector calculation, not the production mesh's computed face normal.
            Vec3d normal = new Vec3d(b[0] - a[0], b[1] - a[1], b[2] - a[2])
                .crossProduct(new Vec3d(c[0] - a[0], c[1] - a[1], c[2] - a[2])).normalize();
            float meanU = 0, meanV = 0;
            for(int[] corner : face) {
                meanU += uvs.get(corner[1])[0]; meanV += uvs.get(corner[1])[1];
                check(corner[2] >= 0 && corner[2] < normals, name + " valid supplied normal index");
            }
            meanU /= 3; meanV /= 3;
            for(int[] corner : face) {
                float[] position = positions.get(corner[0]), uv = uvs.get(corner[1]);
                int offset = vertex++ * 24;
                for(int k = 0; k < 3; k++)
                    same(data.getFloat(offset + k * 4), (float)((double)position[k] + 0D), name + " exact source position/winding");
                same(data.getFloat(offset + 12), uv[0] + (uv[0] > meanU ? -0.0005F : 0.0005F), name + " raw U with centerward inset");
                same(data.getFloat(offset + 16), uv[1] + (uv[1] > meanV ? -0.0005F : 0.0005F), name + " V flip and centerward inset");
                check(data.get(offset + 20) == (byte)((float)normal.x * 127F), name + " exact packed flat normal X");
                check(data.get(offset + 21) == (byte)((float)normal.y * 127F), name + " exact packed flat normal Y");
                check(data.get(offset + 22) == (byte)((float)normal.z * 127F), name + " exact packed flat normal Z");
                // Byte23 is native format padding, not source geometry or a color attribute.
            }
        }
        System.out.println("PASS resource " + name + ": " + faces.size() + " triangles, one fully attributed part; independent exact native vertex-byte comparison.");
    }
    public static void main(String[] args) throws Exception {
        Path root = Paths.get(args[0]);
        // Match the intentionally unchanged mixed-case production constructor strings.
        ResourceLocation ring = new ResourceLocation("hbm", "models/Ring.obj");
        ResourceLocation texture = new ResourceLocation("hbm", "textures/models/EMPBlast.png");
        canonicalResource(root, ring, "models/ring.obj",
            "8cd07021419e853cfa2d32ec8e4d00223304124133d5c15efcc43470d6b65783");
        canonicalResource(root, texture, "textures/models/empblast.png",
            "fd679b8d79dce0982091c068b628f17222fb803c83082ea14a1d27b5aaa350e8");
        mesh(root, ring, 256);
        mesh(root, new ResourceLocation("hbm", "models/mobs/plasticbag.obj"), 26);
        System.out.println("PASS: " + checks + " mesh/resource checks. No renderer initialization, client GL state, texture sampling, resource reload, or GPU acceptance claimed.");
    }
}
