package com.hbm.render.mesh;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Mod-owned OBJ/HMF geometry reader, retaining the existing HFR/Hbm input semantics.
 * Materials are intentionally ignored: callers bind standalone textures. UVs are not clamped.
 * The caller owns the Reader. A parse failure never publishes partial geometry.
 */
public final class ObjMeshParser {
    private static final Pattern NUMBER = Pattern.compile("-?\\d+(?:\\.\\d+)?");
    private ObjMeshParser() { }

    public static ObjMesh parse(Reader input, String source, boolean allowMixedFaces) throws IOException {
        BufferedReader reader = input instanceof BufferedReader ? (BufferedReader) input : new BufferedReader(input);
        List<ObjMesh.Vec> positions = new ArrayList<>(), normals = new ArrayList<>(), uvs = new ArrayList<>();
        List<ObjMesh.Part> parts = new ArrayList<>();
        List<ObjMesh.Face> faces = new ArrayList<>();
        String name = null, line;
        int lineNumber = 0, partSize = 0;
        while((line = reader.readLine()) != null) {
            lineNumber++;
            line = line.replaceAll("\\s+", " ").trim();
            if(line.isEmpty() || line.startsWith("#")) continue;
            String[] words = line.split(" ");
            try {
                switch(words[0]) {
                    case "v": case "vn":
                        require(words.length == 4 || words.length == 5, "expected three or four coordinates");
                        for(int i = 1; i < words.length; i++) number(words[i]);
                        // Historical loaders accept the four-coordinate syntax but do not add a vertex.
                        if(words.length == 4) {
                            ObjMesh.Vec value = new ObjMesh.Vec(number(words[1]), number(words[2]), number(words[3]));
                            (words[0].equals("v") ? positions : normals).add(value);
                        }
                        break;
                    case "vt":
                        require(words.length == 3 || words.length == 4, "expected two or three texture coordinates");
                        uvs.add(new ObjMesh.Vec(number(words[1]), 1 - number(words[2]), words.length == 4 ? number(words[3]) : 0));
                        break;
                    case "g": case "o":
                        require(words.length == 2 && words[1].matches("[\\w\\d.]+"), "invalid part name");
                        if(name != null) parts.add(new ObjMesh.Part(name, faces));
                        name = words[1]; faces = new ArrayList<>(); partSize = 0;
                        break;
                    case "f":
                        int size = words.length - 1;
                        require(size == 3 || size == 4, "only triangles and quads are supported");
                        require(allowMixedFaces || partSize == 0 || partSize == size, "mixed primitive sizes in one part");
                        partSize = size;
                        if(name == null) name = "Default";
                        List<ObjMesh.Corner> corners = new ArrayList<>();
                        String faceLayout = null;
                        for(int i = 1; i < words.length; i++) {
                            String[] indices = words[i].split("/", -1);
                            require(indices.length <= 3, "invalid face corner");
                            String layout = indices.length + ":" + (indices.length >= 2 && !indices[1].isEmpty());
                            if(faceLayout == null) faceLayout = layout;
                            require(faceLayout.equals(layout), "inconsistent corner layout");
                            ObjMesh.Vec uv = indices.length >= 2 && !indices[1].isEmpty() ? at(uvs, indices[1]) : null;
                            require(indices.length != 2 || uv != null, "missing texture index");
                            ObjMesh.Vec normal = indices.length == 3 ? at(normals, indices[2]) : null;
                            corners.add(new ObjMesh.Corner(at(positions, indices[0]), uv, normal));
                        }
                        faces.add(new ObjMesh.Face(corners));
                        break;
                    default:
                        // mtllib, usemtl and smoothing directives were not interpreted by these loaders.
                        break;
                }
            } catch(IllegalArgumentException | IndexOutOfBoundsException e) {
                throw new IOException(source + ":" + lineNumber + ": " + line + " (" + e.getMessage() + ")", e);
            }
        }
        if(name != null) parts.add(new ObjMesh.Part(name, faces));
        return new ObjMesh(parts);
    }

    private static ObjMesh.Vec at(List<ObjMesh.Vec> values, String index) {
        require(index.matches("\\d+"), "indices must be positive integers");
        int i = Integer.parseInt(index);
        require(i > 0 && i <= values.size(), "index out of range: " + index);
        return values.get(i - 1);
    }
    private static float number(String text) {
        require(NUMBER.matcher(text).matches(), "invalid coordinate: " + text);
        float value = Float.parseFloat(text);
        require(Float.isFinite(value), "non-finite coordinate");
        return value;
    }
    private static void require(boolean condition, String message) {
        if(!condition) throw new IllegalArgumentException(message);
    }
}
