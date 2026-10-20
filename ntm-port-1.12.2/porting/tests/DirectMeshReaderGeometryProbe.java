import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.hbm.render.mesh.ObjMesh;
import com.hbm.render.mesh.ObjMeshParser;

/**
 * Bounded, pure CPU fixture over the actual production geometry types/parser.
 * Traversal forms mirror the audited readers, but this does not compile or execute
 * ObjUtil/RenderMirror, implement a Tessellator, or prove rendering/lighting parity.
 */
public final class DirectMeshReaderGeometryProbe {
    private static final String DATA =
            "v 1 0 0\nv 2 0 0\nv 3 1 0\n"
          + "v 4 0 0\nv 5 0 0\nv 6 1 0\nv 7 1 0\n"
          + "v 8 0 0\nv 9 0 0\nv 10 1 0\nv 11 1 0\n"
          + "vt 0.125 0.25 0.5\nvt 1.5 -0.5 0.25\nvt -0.5 1.5 0.75\n"
          + "vn 1 0 0\n";
    private static final String FIRST = "g Mirror\nf 1/1/1 2/2/1 3/3/1\n";
    private static final String REST =
            "g mirror\nf 4/1/1 5/2/1 6/3/1 7/1/1\n"
          + "g Mirror\nf 8/1/1 9/2/1 10/3/1 11/1/1\ng Empty\n";

    private static ObjMesh read(String value) throws Exception {
        return ObjMeshParser.parse(new StringReader(value), "direct-reader-fixture", true);
    }

    private static void require(boolean condition, String message) {
        if(!condition) throw new AssertionError(message);
    }

    private static List<Integer> wholeOrder(ObjMesh mesh) {
        List<Integer> order = new ArrayList<>();
        for(ObjMesh.Part go : mesh.parts) {
            for(ObjMesh.Face f : go.faces) {
                for(int i = 0; i < f.corners.size(); i++) {
                    ObjMesh.Vec v = f.corners.get(i).position;
                    order.add((int)v.x);
                    if(i % 3 == 2) order.add((int)v.x);
                }
            }
        }
        return order;
    }

    private static ObjMesh.Part selectLast(ObjMesh mesh, String name) {
        ObjMesh.Part go = null;
        for(ObjMesh.Part obj : mesh.parts) {
            if(obj.name.equals(name)) go = obj;
        }
        return go;
    }

    private static List<Integer> selectedOrder(ObjMesh mesh, String name) {
        List<Integer> order = new ArrayList<>();
        ObjMesh.Part go = selectLast(mesh, name);
        if(go == null) return order;
        for(ObjMesh.Face f : go.faces) {
            for(int i = 0; i < f.corners.size(); i++) {
                ObjMesh.Vec v = f.corners.get(i).position;
                order.add((int)v.x);
                if(f.corners.size() == 3 && i % 3 == 2) order.add((int)v.x);
            }
        }
        return order;
    }

    private static List<Integer> mirrorOrder(ObjMesh mesh) {
        List<Integer> order = new ArrayList<>();
        ObjMesh.Part go = selectLast(mesh, "Mirror");
        if(go == null) return order;
        for(ObjMesh.Face f : go.faces) {
            for(int i = 0; i < f.corners.size(); i++) {
                ObjMesh.Vec v = f.corners.get(i).position;
                order.add((int)v.x);
                if(i % 3 == 2) order.add((int)v.x);
            }
        }
        return order;
    }

    public static void main(String[] args) throws Exception {
        ObjMesh mesh = read(DATA + FIRST + REST);
        require(mesh.getPartNames().equals(Arrays.asList("Mirror", "mirror", "Mirror", "Empty")),
                "source group order, duplicates, case or empty group changed");
        require(wholeOrder(mesh).equals(Arrays.asList(1, 2, 3, 3, 4, 5, 6, 6, 7, 8, 9, 10, 10, 11)),
                "whole-model source/corner order or intentional quad duplication changed");
        require(selectedOrder(mesh, "Mirror").equals(Arrays.asList(8, 9, 10, 11)),
                "selected reader must use the last exact-name part and not duplicate quads");
        require(selectedOrder(mesh, "mirror").equals(Arrays.asList(4, 5, 6, 7)),
                "selected reader must remain case-sensitive");
        require(selectedOrder(mesh, "MIRROR").isEmpty(), "wrong-case selection must not match");
        require(selectedOrder(mesh, "Missing").isEmpty(), "missing selected part must return empty");
        require(selectedOrder(mesh, "Empty").isEmpty(), "empty selected part must emit no corners");
        require(selectLast(mesh, "Missing") == null, "missing part must remain null");
        require(selectedOrder(read(DATA + FIRST), "Mirror").equals(Arrays.asList(1, 2, 3, 3)),
                "selected triangles must duplicate their third corner");
        require(mirrorOrder(mesh).equals(Arrays.asList(8, 9, 10, 10, 11)),
                "mirror reader must keep exact last-part selection and quad third-corner duplication");
        require(mirrorOrder(read(DATA + "g Other\nf 1/1/1 2/2/1 3/3/1\n")).isEmpty(),
                "mirror reader must return without a matching part");
        require(mesh.selectParts("Mirror").size() == 3,
                "fixture must distinguish renderer all-case-insensitive selection from direct readers");

        ObjMesh.Face face = mesh.parts.get(0).faces.get(0);
        require(face.normal.x == 0 && face.normal.y == 0 && face.normal.z == 1,
                "calculated face normal mismatch");
        ObjMesh.Corner corner = face.corners.get(0);
        require(corner.normal.x == 1 && corner.normal.y == 0 && corner.normal.z == 0,
                "fixture must distinguish face normal from supplied corner normal");
        require(corner.uv.x == 0.125F && corner.uv.y == 0.75F && corner.uv.z == 0.5F,
                "UV x/y/z data or original V flip changed");
        require(face.corners.get(1).uv.x == 1.5F && face.corners.get(1).uv.y == 1.5F
                && face.corners.get(2).uv.x == -0.5F && face.corners.get(2).uv.y == -0.5F,
                "out-of-range UV data must not be clamped");

        ObjMesh large = read("v 0 0 0\nv 1 0 0\nv 0 1 0\n"
                + "vt 300000000000000000000000000000000000000 0\n"
                + "g Large\nf 1/1 2/1 3/1\n");
        ObjMesh.Vec t = large.parts.get(0).faces.get(0).corners.get(0).uv;
        require(Float.isInfinite(t.x * 16), "whole-model integer literal must keep float evaluation");
        require(!Double.isInfinite(t.x * 16D), "selected-part double literal must keep double evaluation");
        System.out.println("PASS: native CPU geometry fixture (18 checks): source/corner order, last exact"
                + " selection, empty/missing parts, both duplicate guards, face/corner normals, UV data"
                + " and float-versus-double UV evaluation");
        System.out.println("BOUNDARY: fixture traversal only; no production reader compilation, Minecraft,"
                + " Forge, GL, atlas, tint, lighting or block-rendering execution");
    }
}
