package com.hbm.render.mesh;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Source-order OBJ geometry. No texture atlas, material loading, GL objects or Minecraft bootstrap. */
public final class ObjMesh {
    private static final ObjMesh EMPTY = new ObjMesh(Collections.<Part>emptyList());
    public static ObjMesh empty() { return EMPTY; }
    public static final class Vec {
        public final float x, y, z;
        public Vec(float x, float y, float z) { this.x = x; this.y = y; this.z = z; }
    }

    public static final class Corner {
        public final Vec position;
        /** Already has the original loader's V flip. Null when the face has no UVs. */
        public final Vec uv;
        public final Vec normal;
        Corner(Vec position, Vec uv, Vec normal) {
            this.position = position; this.uv = uv; this.normal = normal;
        }
    }

    public static final class Face {
        public final List<Corner> corners;
        public final Vec normal;
        Face(List<Corner> corners) {
            this.corners = immutable(corners);
            Vec a = corners.get(0).position, b = corners.get(1).position, c = corners.get(2).position;
            // Subtraction was float in both old loaders before promotion to Vec3 doubles.
            double ax = b.x - a.x, ay = b.y - a.y, az = b.z - a.z;
            double bx = c.x - a.x, by = c.y - a.y, bz = c.z - a.z;
            double nx = ay * bz - az * by, ny = az * bx - ax * bz, nz = ax * by - ay * bx;
            // Vec3.normalize used MathHelper.sqrt_double, whose result is float before promotion.
            double length = (float)Math.sqrt(nx * nx + ny * ny + nz * nz);
            normal = length < 1.0E-4D ? new Vec(0, 0, 0)
                    : new Vec((float)(nx / length), (float)(ny / length), (float)(nz / length));
        }
        public boolean textured() { return corners.get(0).uv != null; }
    }

    public static final class Part {
        public final String name;
        public final List<Face> faces;
        Part(String name, List<Face> faces) { this.name = name; this.faces = immutable(faces); }
    }

    public final List<Part> parts;
    ObjMesh(List<Part> parts) { this.parts = immutable(parts); }

    /** Fresh ordered list, including duplicate names and empty source groups. */
    public List<String> getPartNames() {
        List<String> names = new ArrayList<>();
        for(Part part : parts) names.add(part.name);
        return names;
    }

    /** Source-order selection. Duplicate requested names retain the old renderOnly multiplicity. */
    public List<Part> selectParts(String... names) {
        List<Part> selected = new ArrayList<>();
        for(Part part : parts) for(String name : names)
            if(part.name.equalsIgnoreCase(name)) selected.add(part);
        return Collections.unmodifiableList(selected);
    }

    public List<Part> excludingParts(String... names) {
        List<Part> selected = new ArrayList<>();
        for(Part part : parts) {
            boolean excluded = false;
            for(String name : names) if(part.name.equalsIgnoreCase(name)) excluded = true;
            if(!excluded) selected.add(part);
        }
        return Collections.unmodifiableList(selected);
    }

    private static <T> List<T> immutable(List<T> values) {
        return Collections.unmodifiableList(new ArrayList<>(values));
    }
}
