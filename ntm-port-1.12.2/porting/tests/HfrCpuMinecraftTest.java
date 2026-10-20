import com.hbm.render.loader.*;
import com.hbm.render.mesh.*;
import com.hbm.render.mesh.ObjMeshRenderer.Shading;
import java.io.*;
import java.nio.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.*;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;

/** Whole production native model/loaders compiled; actual BufferBuilder and resource APIs executed. */
public final class HfrCpuMinecraftTest {
    static long checks;
    static void check(boolean value, String label) { checks++; if(!value) throw new AssertionError(label); }
    static void same(float value, float expected, String label) {
        check(Float.floatToIntBits(value) == Float.floatToIntBits(expected), label + ": " + value + " != " + expected);
    }
    static final String V = "v 0 0 0\nv 1 0 0\nv 0 1 0\nv 0 0 1\nvt 0.0 0.0\nvt 2.0 0.0\nvt 0.0 -1.0\nvn 1 0 0\n";
    static final String T = "f 1/1/1 2/2/1 3/3/1\n";
    static ObjMesh parse(String text, boolean mixed) throws IOException {
        return ObjMeshParser.parse(new StringReader(text), "fixture", mixed);
    }
    static ByteBuffer all(ObjMesh mesh, Shading policy) {
        BufferBuilder buffer = new BufferBuilder(32);
        ObjMeshRenderer.buildAll(buffer, mesh, policy, 0);
        buffer.finishDrawing();
        return copy(buffer.getByteBuffer());
    }
    static ByteBuffer copy(ByteBuffer input) {
        ByteBuffer output = ByteBuffer.allocate(input.remaining()).order(ByteOrder.nativeOrder());
        output.put(input.duplicate()).flip(); return output;
    }

    static final class Resources implements IResourceManager {
        final Map<ResourceLocation,String> contents = new LinkedHashMap<>();
        final List<ResourceLocation> reads = new ArrayList<>();
        final Path root;
        Resources(Path root) { this.root = root; }
        public Set<String> getResourceDomains() { return Collections.singleton("hbm"); }
        public IResource getResource(ResourceLocation location) throws IOException {
            reads.add(location);
            InputStream stream;
            if(contents.containsKey(location)) stream = new ByteArrayInputStream(contents.get(location).getBytes(StandardCharsets.UTF_8));
            else if(root != null) stream = Files.newInputStream(root.resolve(location.getPath()));
            else throw new FileNotFoundException(location.toString());
            return new SimpleResource("native HFR fixture", location, stream, null, null);
        }
        public List<IResource> getAllResources(ResourceLocation location) throws IOException {
            return Collections.singletonList(getResource(location));
        }
    }

    static void lifecycle() throws Exception {
        HFRWavefrontObject.allModels.clear(); HFRWavefrontObject.allVBOs.clear();
        Resources resources = new Resources(null);
        ResourceLocation a = new ResourceLocation("hbm", "models/a.obj"), b = new ResourceLocation("hbm", "models/b.obj");
        resources.contents.put(a, V + "g First\n" + T);
        resources.contents.put(b, V + "g Second\n" + T);
        HFRWavefrontObject first = new HFRWavefrontObject(resources, a, false).noSmooth();
        HFRWavefrontObject second = new HFRWavefrontObject(resources, b, false);
        check(first.getCapturedShading() == Shading.HFR_SMOOTH, "late noSmooth does not modify existing faces");
        check(all(first.getMesh(), first.getCapturedShading()).get(20) == 127, "initial HFR normal is source +X");
        ObjMesh old = first.getMesh();
        resources.reads.clear();
        new HFRModelReloader().reloadModels(resources);
        check(resources.reads.equals(Arrays.asList(a,b)), "real CPU reload phase follows insertion order");
        check(first.getMesh() != old && first.getCapturedShading() == Shading.HFR_FLAT, "next actual reload captures noSmooth");
        ByteBuffer flat = all(first.getMesh(), first.getCapturedShading());
        check(flat.get(20) == 0 && flat.get(22) == 127, "reloaded HFR uses flat +Z normal");
        check(second.getCapturedShading() == Shading.HFR_SMOOTH, "other model's captured policy is unchanged");
        ObjMeshModel plain = new ObjMeshModel(resources, a);
        ObjMesh plainSnapshot = plain.getMesh();
        resources.contents.put(a, V + T + T);
        new HFRModelReloader().reloadModels(resources);
        check(plain.getMesh() == plainSnapshot && plain.getMesh().parts.get(0).faces.size() == 1, "plain eager model is not subscribed to HFR reload");
        resources.contents.remove(a);
        new HFRModelReloader().reloadModels(resources);
        check(first.getMesh().parts.isEmpty() && !second.getMesh().parts.isEmpty(), "resource-open failure leaves cleared CPU owner empty and continues");
        resources.contents.put(a, "v not_a_number 0 0\n");
        resources.reads.clear(); boolean failed = false;
        try { new HFRModelReloader().reloadModels(resources); } catch(IllegalStateException expected) { failed = true; }
        check(failed && resources.reads.equals(Collections.singletonList(a)), "parse failure reports and aborts before later CPU/VBO work");
        check(first.getMesh().parts.isEmpty(), "failed reload never presents the old geometry as newly loaded");
        resources.contents.put(a, V + T + "f 1/1/1 2/2/1 4/1/1 3/3/1\n");
        HFRWavefrontObject mixed = new HFRWavefrontObject(resources, a, true);
        check(mixed.getMesh().parts.get(0).faces.size() == 2, "mixed source is retained for direct geometry readers");
        failed = false; try { mixed.renderAll(); } catch(UnsupportedOperationException expected) { failed = true; }
        check(failed, "mixed rendering rejects before any GL call");
        HFRWavefrontObject.allModels.clear(); HFRWavefrontObject.allVBOs.clear();
    }

    static void batches() throws Exception {
        boolean rejectedNames = false;
        try { parse(V + "g first second\n" + T, false); }
        catch(IOException expected) { rejectedNames = true; }
        check(rejectedNames, "original HFR/plain Forge single-token group-name grammar remains strict");
        ObjMesh mesh = parse(V + "g first\n" + T + "g second\nf 1//1 2//1 3//1\ng FIRST\n" + T, false);
        check(mesh.getPartNames().equals(Arrays.asList("first","second","FIRST")), "ordered names retain duplicates");
        check(mesh.selectParts("FIRST", "first").size() == 4, "case-insensitive selection retains request multiplicity");
        check(mesh.excludingParts("fIrSt").size() == 1, "case-insensitive exclusions");
        ByteBuffer raw = all(mesh, Shading.HFR_SMOOTH);
        check(raw.remaining() == 9 * 24, "all groups form one raw triangle stream");
        for(int i=3;i<6;i++) {
            check(raw.get(i*24+20) == 127, "enabled smooth normal carries into UV-absent group");
            same(raw.getFloat(i*24+16), 2, "enabled UV carries into UV-absent group");
        }
        BufferBuilder untextured = new BufferBuilder(32);
        ObjMeshRenderer.buildPart(untextured, mesh.parts.get(1), Shading.HFR_SMOOTH, 0);
        check(untextured.getVertexFormat().equals(DefaultVertexFormats.POSITION), "separate smooth untextured part does not enable normal/UV arrays despite vn");
        untextured.finishDrawing();
        ObjMesh crossModes = parse(V + "g tri\n" + T + "g quad\nf 1/1/1 2/2/1 4/1/1 3/3/1\n", false);
        BufferBuilder batch = new BufferBuilder(32);
        ObjMeshRenderer.buildAll(batch, crossModes, Shading.LEGACY_FLAT, 0);
        check(batch.getDrawMode() == GL11.GL_QUADS && batch.getVertexCount() == 7, "last-group mode and raw corner count retained without implicit triangle padding");
        batch.finishDrawing();
        ObjMesh trailingEmpty = parse(V + "g tri\n" + T + "g empty\n", false);
        BufferBuilder invalidMode = new BufferBuilder(32);
        ObjMeshRenderer.buildAll(invalidMode, trailingEmpty, Shading.HFR_SMOOTH, 0);
        check(invalidMode.getDrawMode() == -1 && invalidMode.getVertexCount() == 3,
                "trailing empty group preserves legacy invalid GL mode without a new Java exception");
        invalidMode.finishDrawing();
        List<String> names = mesh.getPartNames(); names.clear();
        check(mesh.getPartNames().size() == 3, "part-name list does not mutate geometry");
        ObjMesh undefined = parse(V + "g before\nf 1//1 2//1 3//1\ng after\n" + T, false);
        BufferBuilder untouched = new BufferBuilder(32);
        untouched.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION);
        untouched.pos(12, 34, 56).endVertex();
        boolean failed = false;
        try { ObjMeshRenderer.buildAll(untouched, undefined, Shading.HFR_SMOOTH, 0, "hbm:models/undefined.obj"); }
        catch(IllegalStateException expected) {
            failed = expected.getMessage().contains("hbm:models/undefined.obj") && expected.getMessage().contains("group 'before'")
                    && expected.getMessage().contains("UV and normal");
        }
        check(failed, "late activation fails descriptively before buffer.begin even when caller is already building");
        check(untouched.getDrawMode() == GL11.GL_LINES && untouched.getVertexCount() == 1
                && untouched.getVertexFormat().equals(DefaultVertexFormats.POSITION), "rejected all-batch leaves caller state unchanged");
        untouched.pos(78, 90, 12).endVertex(); untouched.finishDrawing();
        same(untouched.getByteBuffer().getFloat(0), 12, "rejected batch preserves existing vertex bytes");
        check(untouched.getVertexCount() == 2, "caller can continue its original batch after rejection");
        ObjMesh badPart = parse(V + "g bad\nf 1//1 2//1 3//1\n" + T, false);
        failed = false;
        try { ObjMeshRenderer.drawParts(Arrays.asList(mesh.parts.get(0), badPart.parts.get(0)), Shading.HFR_SMOOTH, 0, "hbm:models/selection.obj"); }
        catch(IllegalStateException expected) { failed = expected.getMessage().contains("selection.obj") && expected.getMessage().contains("group 'bad'"); }
        check(failed, "all selected batches preflight before earlier valid group touches GL");
        ObjMesh uvOnly = parse(V + "g flat\nf 1 2 3\n" + T, false);
        failed = false;
        try { all(uvOnly, Shading.LEGACY_FLAT); }
        catch(IllegalStateException expected) { failed = expected.getMessage().contains("before UV attributes"); }
        check(failed, "flat shading rejects only undefined UV, not its already-defined face normal");
        ObjMesh normalOnly = parse(V + "g smooth\nf 1/1 2/2 3/3\n" + T, false);
        failed = false;
        try { all(normalOnly, Shading.HFR_SMOOTH); }
        catch(IllegalStateException expected) { failed = expected.getMessage().contains("before normal attributes"); }
        check(failed, "textured smooth batch detects later normal activation independently of UV");
    }

    static final class RawPart { String name; List<int[][]> faces = new ArrayList<>(); RawPart(String name) {this.name=name;} }

    /** Independent index/attribute oracle reads original text, not production parsed corner values. */
    static void resource(Path root, String asset, Shading policy, boolean mixed, Resources resources) throws Exception {
        ResourceLocation location = new ResourceLocation("hbm", asset);
        List<float[]> positions = new ArrayList<>(), uvs = new ArrayList<>(), normals = new ArrayList<>();
        List<RawPart> parts = new ArrayList<>(); RawPart current = null;
        // Use the actual target ResourceLocation-normalized path, never a case-insensitive fixture fallback.
        for(String line : Files.readAllLines(root.resolve(location.getPath()), StandardCharsets.UTF_8)) {
            String[] s = line.trim().split("\\s+");
            if(s[0].equals("v")) positions.add(new float[]{Float.parseFloat(s[1]),Float.parseFloat(s[2]),Float.parseFloat(s[3])});
            else if(s[0].equals("vn")) normals.add(new float[]{Float.parseFloat(s[1]),Float.parseFloat(s[2]),Float.parseFloat(s[3])});
            else if(s[0].equals("vt")) uvs.add(new float[]{Float.parseFloat(s[1]),1-Float.parseFloat(s[2])});
            else if(s[0].equals("g") || s[0].equals("o")) { current = new RawPart(s[1]); parts.add(current); }
            else if(s[0].equals("f")) {
                if(current == null) {current = new RawPart("Default");parts.add(current);}
                int[][] f = new int[s.length-1][];
                for(int i=1;i<s.length;i++) {String[] c=s[i].split("/",-1);f[i-1]=new int[]{Integer.parseInt(c[0])-1,c.length>1&&!c[1].isEmpty()?Integer.parseInt(c[1])-1:-1,c.length>2?Integer.parseInt(c[2])-1:-1};}
                current.faces.add(f);
            }
        }
        MeshModel model = policy == Shading.LEGACY_FLAT ? new ObjMeshModel(resources,location) : new HFRWavefrontObject(resources,location,mixed);
        ObjMesh mesh = model.getMesh();
        check(parts.size() == mesh.parts.size(), asset + " source group count");
        for(int p=0;p<parts.size();p++) {
            RawPart source = parts.get(p); ObjMesh.Part part = mesh.parts.get(p);
            check(source.name.equals(part.name) && source.faces.size() == part.faces.size(), asset + " source group/face order");
            if(mixed) continue; // The actual HFR model rejects drawing; its source remains available to direct readers.
            BufferBuilder buffer = new BufferBuilder(32);
            if(!ObjMeshRenderer.buildPart(buffer,part,policy,0)) {check(source.faces.isEmpty(), asset + " empty group");continue;}
            buffer.finishDrawing(); ByteBuffer bytes = buffer.getByteBuffer(); int stride = buffer.getVertexFormat().getSize(), vertex=0;
            boolean uvEnabled=buffer.getVertexFormat().hasUvOffset(0), normalEnabled=buffer.getVertexFormat().hasNormal();
            float lastU=0,lastV=0; byte[] normal={0,0,0}; boolean sawUv=false, sawNormal=policy!=Shading.HFR_SMOOTH;
            for(int[][] face : source.faces) {
                float[] a=positions.get(face[0][0]),b=positions.get(face[1][0]),c=positions.get(face[2][0]);
                Vec3d fn=new Vec3d(b[0]-a[0],b[1]-a[1],b[2]-a[2]).crossProduct(new Vec3d(c[0]-a[0],c[1]-a[1],c[2]-a[2])).normalize();
                boolean textured=face[0][1]>=0;float meanU=0,meanV=0;
                if(textured) {for(int[] corner:face){float[] uv=uvs.get(corner[1]);meanU+=uv[0];meanV+=uv[1];}meanU/=face.length;meanV/=face.length;}
                if(policy!=Shading.HFR_SMOOTH) normal=new byte[]{(byte)((float)fn.x*127),(byte)((float)fn.y*127),(byte)((float)fn.z*127)};
                for(int[] corner : face) {
                    float[] pos=positions.get(corner[0]);int off=vertex++*stride;
                    // Both native BufferBuilder and legacy Tessellator add their double translation,
                    // even when zero; IEEE -0.0 therefore becomes +0.0 at this emission boundary.
                    for(int axis=0;axis<3;axis++) same(bytes.getFloat(off+axis*4),(float)((double)pos[axis]+0D),asset+" source position/winding");
                    if(textured) {
                        float[] uv=uvs.get(corner[1]);float inset=policy==Shading.LEGACY_FLAT?.0005F:0;
                        lastU=uv[0]+(uv[0]>meanU?-inset:inset);lastV=uv[1]+(uv[1]>meanV?-inset:inset);sawUv=true;
                        if(policy==Shading.HFR_SMOOTH && corner[2]>=0){float[] n=normals.get(corner[2]);normal=new byte[]{(byte)(n[0]*127),(byte)(n[1]*127),(byte)(n[2]*127)};sawNormal=true;}
                    }
                    if(uvEnabled) {check(sawUv,asset+" no undefined initial UV in actual part");same(bytes.getFloat(off+12),lastU,asset+" U/inset/carry");same(bytes.getFloat(off+16),lastV,asset+" V flip/wrap/inset/carry");}
                    if(normalEnabled) {check(sawNormal,asset+" no undefined initial normal in actual part");int n=off+buffer.getVertexFormat().getNormalOffset();for(int axis=0;axis<3;axis++)check(bytes.get(n+axis)==normal[axis],asset+" exact packed native normal");}
                }
            }
            check(vertex==buffer.getVertexCount(), asset+" no corner lost or padded");
        }
        if(model instanceof HFRWavefrontObject) HFRWavefrontObject.allModels.remove(model);
    }

    public static void main(String[] args) throws Exception {
        lifecycle();batches();Path root=Paths.get(args[0]);Resources resources=new Resources(root);int assets=0,missing=0;
        for(String line:Files.readAllLines(Paths.get(args[1]),StandardCharsets.UTF_8)) {
            String[] row=line.split("\t");
            if(row[1].equals("models/BombGenericLarge.obj")) {
                ResourceLocation location = new ResourceLocation("hbm", row[1]);
                check(!Files.exists(root.resolve(location.getPath())), "known missing original BombGenericLarge remains visible, not substituted");
                boolean failed = false;
                try { new ObjMeshModel(resources, location); }
                catch(IllegalStateException expected) { failed = expected.getCause() instanceof IOException; }
                check(failed, "unregistered RenderBombMultiLarge source asset still fails actual native resource loading");
                missing++;
            } else {
                resource(root,row[1],row[0].equals("PLAIN")?Shading.LEGACY_FLAT:Shading.HFR_SMOOTH,Boolean.parseBoolean(row[2]),resources);assets++;
            }
        }
        check(missing == 1, "exactly one inventoried baseline missing asset, no silent resource skips");
        System.out.println("PASS "+checks+" assertions: full native CPU/HFR model classes, "+assets+" policy/resource combinations, actual BufferBuilder bytes, eager/reload/noSmooth lifecycle, selectors and raw shared batches.");
        System.out.println("KNOWN RESOURCE BLOCKER: models/BombGenericLarge.obj is absent in the original baseline; its native eager-loader IO failure is asserted, not counted as mesh acceptance.");
        System.out.println("LIMIT: no GL draws, live VBO upload/delete, full-mod compilation, native block-renderer bodies or arbitrary external/resource-pack late-activation parity.");
    }
}
