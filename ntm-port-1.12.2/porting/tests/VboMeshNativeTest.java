import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.nio.BufferOverflowException;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.hbm.render.loader.HFRWavefrontObject;
import com.hbm.render.loader.HFRWavefrontObjectVBO;
import com.hbm.render.mesh.MeshModel;
import com.hbm.render.mesh.ObjMesh;
import com.hbm.render.mesh.ObjMeshParser;
import com.hbm.render.mesh.ObjMeshVboPayload;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.data.IMetadataSection;
import net.minecraft.util.ResourceLocation;

/** Actual production classes and LWJGL buffers, no GL calls and no substituted platform classes. */
public final class VboMeshNativeTest {
    private static long assertions;
    private static long vertices;
    private static long faces;
    private static long groups;
    private static final Set<String> noUvAssets = new HashSet<>();

    private static final class RawPart {
        final String name;
        final List<float[]> positions = new ArrayList<>();
        final List<float[]> uvs = new ArrayList<>();
        final List<float[]> normals = new ArrayList<>();
        int faces;
        RawPart(String name) { this.name = name; }
    }

    /** Independent raw-file oracle: no production parser/mesh/payload values used for expected floats. */
    private static List<RawPart> raw(Reader input, String label) throws IOException {
        List<float[]> positions = new ArrayList<>(), normals = new ArrayList<>(), uvs = new ArrayList<>();
        List<RawPart> result = new ArrayList<>();
        RawPart current = null;
        BufferedReader reader = new BufferedReader(input);
        String line;
        while((line = reader.readLine()) != null) {
            line = line.trim();
            if(line.isEmpty() || line.startsWith("#")) continue;
            String[] words = line.split("\\s+");
            if(words[0].equals("v") || words[0].equals("vn")) {
                if(words.length == 4) {
                    float[] value = {Float.parseFloat(words[1]), Float.parseFloat(words[2]), Float.parseFloat(words[3])};
                    (words[0].equals("v") ? positions : normals).add(value);
                }
            } else if(words[0].equals("vt")) {
                uvs.add(new float[] {Float.parseFloat(words[1]), 1.0F - Float.parseFloat(words[2]),
                        words.length == 4 ? Float.parseFloat(words[3]) : 0.0F});
            } else if(words[0].equals("g") || words[0].equals("o")) {
                current = new RawPart(words[1]);
                result.add(current);
            } else if(words[0].equals("f")) {
                if(current == null) { current = new RawPart("Default"); result.add(current); }
                check(words.length == 4, label + " expected triangle");
                current.faces++;
                for(int i = 1; i < words.length; i++) {
                    String[] indices = words[i].split("/", -1);
                    check(indices.length == 3 && !indices[2].isEmpty(), label + " requires source normals");
                    current.positions.add(positions.get(Integer.parseInt(indices[0]) - 1));
                    current.normals.add(normals.get(Integer.parseInt(indices[2]) - 1));
                    if(indices[1].isEmpty()) {
                        current.uvs.add(new float[] {0, 0, 0});
                        noUvAssets.add(label);
                    } else current.uvs.add(uvs.get(Integer.parseInt(indices[1]) - 1));
                }
            }
        }
        return result;
    }

    private static void check(boolean result, String label) {
        assertions++;
        if(!result) throw new AssertionError(label);
    }

    private static void stream(FloatBuffer actual, List<float[]> expected, int capacity, String label) {
        check(actual.isDirect(), label + " direct buffer");
        check(actual.order().equals(ByteOrder.nativeOrder()), label + " native byte order");
        check(actual.position() == 0, label + " flipped position");
        check(actual.limit() == expected.size() * 3, label + " flipped limit");
        check(actual.capacity() == capacity, label + " original triangle capacity");
        for(int i = 0; i < expected.size(); i++) {
            for(int component = 0; component < 3; component++) {
                int want = Float.floatToRawIntBits(expected.get(i)[component]);
                int got = Float.floatToRawIntBits(actual.get(i * 3 + component));
                assertions++;
                if(want != got) throw new AssertionError(label + " corner " + i + " component " + component
                        + " float bits: expected " + Integer.toHexString(want) + ", got " + Integer.toHexString(got));
            }
        }
    }

    private static ObjMesh compare(String source, String label) throws IOException {
        ObjMesh mesh = ObjMeshParser.parse(new StringReader(source), label, false);
        List<RawPart> expected = raw(new StringReader(source), label);
        check(mesh.parts.size() == expected.size(), label + " source group count");
        for(int i = 0; i < expected.size(); i++) {
            RawPart part = expected.get(i);
            ObjMesh.Part actual = mesh.parts.get(i);
            ObjMeshVboPayload payload = ObjMeshVboPayload.pack(actual);
            check(actual.name.equals(part.name), label + " exact source name/order " + i);
            check(actual.faces.size() == part.faces, label + " face count " + i);
            check(payload.vertices == part.positions.size(), label + " draw vertex count " + i);
            stream(payload.positions, part.positions, part.faces * 9, label + "/" + part.name + "/position");
            stream(payload.uvs, part.uvs, part.faces * 9, label + "/" + part.name + "/uv");
            stream(payload.normals, part.normals, part.faces * 9, label + "/" + part.name + "/normal");
            faces += part.faces;
            vertices += payload.vertices;
            groups++;
        }
        return mesh;
    }

    private static void selectionsAndFailures() throws IOException {
        String coords = "v -0.0 2.25 -7.75\nv 4.5 -0.125 0.0\nv 8.0 1.5 9.75\nv 3.0 5.0 6.0\n"
                + "vt 1.25 -0.625 0.75\nvt -0.125 2.25 -0.875\nvt 0.12345679 0.98765433 1.25\n"
                + "vn 0.12345679 -0.98765433 1.2500001\nvn -0.0 0.33333334 -2.5\nvn 1.0000001 2.0 3.0\n";
        String textured = "f 1/1/1 2/2/2 3/3/3\n";
        String bareUv = "f 1//1 2//2 3//3\n";
        ObjMesh mesh = compare(coords + "g Wing\n" + textured + "g wing\n" + bareUv
                + "g Tail\n" + textured + "g Empty\n", "synthetic-float3");
        List<ObjMesh.Part> selected = mesh.selectParts("WING", "Tail", "wing");
        check(selected.size() == 5, "duplicate selectors multiply both case-duplicate source groups");
        int[] indices = {0, 0, 1, 1, 2};
        for(int i = 0; i < indices.length; i++)
            check(selected.get(i) == mesh.parts.get(indices[i]), "selection uses source-first nested order " + i);
        List<ObjMesh.Part> excluded = mesh.excludingParts("WING", "wing");
        check(excluded.size() == 2 && excluded.get(0) == mesh.parts.get(2)
                && excluded.get(1) == mesh.parts.get(3), "exclusion does not multiply or reorder groups");
        check(mesh.selectParts("absent").isEmpty(), "missing selection is empty");
        check(mesh.selectParts().isEmpty(), "zero selectors select nothing");
        check(mesh.getPartNames().equals(Arrays.asList("Wing", "wing", "Tail", "Empty")),
                "part names preserve case duplicates and empty groups");
        ObjMeshVboPayload texturedPayload = ObjMeshVboPayload.pack(mesh.parts.get(0));
        check(texturedPayload.uvs.get(2) == 0.75F && texturedPayload.uvs.get(5) == -0.875F,
                "nonzero W stays in float3 UV buffer");
        check(texturedPayload.normals.get(2) == 1.2500001F, "source normals not normalized or byte-quantized");
        ObjMeshVboPayload untexturedPayload = ObjMeshVboPayload.pack(mesh.parts.get(1));
        for(int i = 0; i < untexturedPayload.uvs.limit(); i++)
            check(Float.floatToRawIntBits(untexturedPayload.uvs.get(i)) == 0, "missing UV is positive zero");
        check(Float.floatToRawIntBits(untexturedPayload.normals.get(3)) == 0x80000000,
                "untextured corner normal retains signed zero");
        check(ObjMeshVboPayload.pack(mesh.parts.get(3)).vertices == 0, "empty group remains zero-size payload");
        ObjMesh quad = ObjMeshParser.parse(new StringReader(coords + "g Quad\nf 1/1/1 2/2/2 3/3/3 4/1/1\n"), "quad", false);
        try { ObjMeshVboPayload.pack(quad.parts.get(0)); throw new AssertionError("quad silently accepted"); }
        catch(BufferOverflowException expected) { assertions++; }
        for(String face : new String[] {"f 1 2 3\n", "f 1/1 2/2 3/3\n"}) {
            ObjMesh noNormal = ObjMeshParser.parse(new StringReader(coords + face), "missing-normal", false);
            try { ObjMeshVboPayload.pack(noNormal.parts.get(0)); throw new AssertionError("normal synthesized"); }
            catch(NullPointerException expected) { assertions++; }
        }
    }

    /** Real Minecraft interfaces with an explicit empty resource fixture; no GL path is reached. */
    private static final class EmptyResources implements IResourceManager {
        @Override public Set<String> getResourceDomains() { return Collections.singleton("hbm"); }
        @Override public IResource getResource(final ResourceLocation location) {
            return new IResource() {
                @Override public ResourceLocation getResourceLocation() { return location; }
                @Override public InputStream getInputStream() { return new ByteArrayInputStream(new byte[0]); }
                @Override public boolean hasMetadata() { return false; }
                @Override public <T extends IMetadataSection> T getMetadata(String section) { return null; }
                @Override public String getResourcePackName() { return "explicit-empty-test-resource"; }
                @Override public void close() { }
            };
        }
        @Override public List<IResource> getAllResources(ResourceLocation location) {
            return Collections.singletonList(getResource(location));
        }
    }

    private static void emptyOwnerLifecycle() {
        HFRWavefrontObject cpu = new HFRWavefrontObject(new EmptyResources(),
                new ResourceLocation("hbm", "empty-vbo-test.obj"), false);
        HFRWavefrontObjectVBO vbo = cpu.asVBO();
        MeshModel nativeContract = vbo;
        ObjMesh captured = cpu.getMesh();
        check(nativeContract.getMesh() == captured, "successful empty upload captures exact source snapshot");
        check(HFRWavefrontObject.allVBOs.get(vbo) == cpu, "real asVBO registers CPU owner");
        cpu.loadObjModel(new ByteArrayInputStream(("v 0 0 0\nv 1 0 0\nv 0 1 0\nvn 0 0 1\n"
                + "g Later\nf 1//1 2//1 3//1\n").getBytes(StandardCharsets.UTF_8)));
        check(!cpu.getMesh().parts.isEmpty(), "CPU owner reparsed new geometry");
        check(vbo.getMesh() == captured && vbo.getMesh().parts.isEmpty(), "VBO snapshot not a live CPU alias");
        check(vbo.getPartNames().isEmpty(), "empty upload has no buffer groups");
        vbo.renderAll(); vbo.renderOnly("Later", "later"); vbo.renderPart("Later"); vbo.renderAllExcept("Later");
        vbo.destroy();
        check(vbo.getMesh().parts.isEmpty() && vbo.getPartNames().isEmpty(), "destroy empties captured geometry");
        cpu.destroy(); vbo.load(cpu);
        check(vbo.getMesh() == cpu.getMesh(), "load after destroy captures current empty snapshot");
        HFRWavefrontObject.allVBOs.remove(vbo);
        HFRWavefrontObject.allModels.remove(cpu);
    }

    public static void main(String[] args) throws Exception {
        Path root = Paths.get(args[0]);
        List<String> assets = Files.readAllLines(Paths.get(args[1]), StandardCharsets.UTF_8);
        check(assets.size() == 293, "all 293 source-derived VBO assets supplied");
        for(String asset : assets)
            compare(new String(Files.readAllBytes(root.resolve(new ResourceLocation("hbm", asset).getPath())),
                    StandardCharsets.UTF_8), asset);
        Set<String> expectedMissing = new HashSet<>(Arrays.asList("models/machines/liquefactor.obj",
                "models/machines/solidifier.obj", "models/machines/chemical_factory.obj",
                "models/weapons/detonator_laser.obj", "models/armor/AJR.obj", "models/armor/no9.obj"));
        check(noUvAssets.equals(expectedMissing), "exact six VBO resources with absent UVs: " + noUvAssets);
        long assetGroups = groups, assetFaces = faces, assetVertices = vertices;
        selectionsAndFailures();
        emptyOwnerLifecycle();
        System.out.println("PASS actual native HFR/VBO compilation and payload oracle: " + assets.size()
                + " assets, " + assetGroups + " source groups, " + assetFaces + " triangles, "
                + assetVertices + " corners, " + assertions + " assertions. Float3 W, unquantized normals, "
                + "missing-UV zeros, duplicate selections, original shape failures and empty-owner snapshot lifecycle checked.");
        System.out.println("No live GL allocation/upload/draw/delete or client visual acceptance claimed.");
    }
}
