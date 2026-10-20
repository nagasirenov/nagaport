package com.hbm.render.block;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.hbm.render.block.nativeblock.ConnectionQuadFactory;
import com.hbm.render.mesh.ObjMesh;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

/** Native successor for the original standard duct's base and fluid-colored OBJ passes. */
public final class RenderTestPipe {
    private RenderTestPipe() { }
    public static List<BakedQuad> bake(ObjMesh mesh, int connections, boolean inventory, int color,
            TextureAtlasSprite base, TextureAtlasSprite overlay) {
        if(connections < 0 || connections > 63) throw new IllegalArgumentException("Invalid fluid connection mask " + connections);
        List<String> groups = groups(connections, inventory);
        List<BakedQuad> result = new ArrayList<>();
        if(inventory) {
            for(String group : groups) append(result, mesh, group, true, 0xffffff, base);
            for(String group : groups) append(result, mesh, group, true, color, overlay);
        } else {
            for(String group : groups) {
                append(result, mesh, group, false, 0xffffff, base);
                append(result, mesh, group, false, color, overlay);
            }
        }
        return Collections.unmodifiableList(result);
    }
    private static List<String> groups(int mask, boolean inventory) {
        List<String> result = new ArrayList<>();
        if(inventory) Collections.addAll(result, "pX", "nX", "pZ", "nZ");
        else if(mask == 0) Collections.addAll(result, "pX", "nX", "pY", "nY", "pZ", "nZ");
        else if(mask == 32 || mask == 16) Collections.addAll(result, "pX", "nX");
        else if(mask == 8 || mask == 4) Collections.addAll(result, "pY", "nY");
        else if(mask == 2 || mask == 1) Collections.addAll(result, "pZ", "nZ");
        else {
            int[] arms = {32, 16, 8, 4, 2, 1};
            String[] names = {"pX", "nX", "pY", "nY", "nZ", "pZ"};
            for(int i = 0; i < arms.length; i++) if((mask & arms[i]) != 0) result.add(names[i]);
            int[] corners = {32 | 8 | 2, 32 | 8 | 1, 16 | 8 | 2, 16 | 8 | 1,
                             32 | 4 | 2, 32 | 4 | 1, 16 | 4 | 2, 16 | 4 | 1};
            String[] fills = {"ppn", "ppp", "npn", "npp", "pnn", "pnp", "nnn", "nnp"};
            for(int i = 0; i < corners.length; i++) if((mask & corners[i]) == 0) result.add(fills[i]);
        }
        return result;
    }
    private static void append(List<BakedQuad> result, ObjMesh mesh, String name, boolean inventory, int color, TextureAtlasSprite sprite) {
        ObjMesh.Part selected = null;
        for(ObjMesh.Part part : mesh.parts) if(name.equals(part.name)) selected = part;
        if(selected == null) throw new IllegalArgumentException("Missing standard duct OBJ part " + name);
        for(ObjMesh.Face face : selected.faces) {
            float nx = face.normal.x, ny = face.normal.y, nz = face.normal.z;
            float shade = inventory ? 1 : Math.max(.45F, ny * .3F + .7F - Math.abs(nx) * .1F + Math.abs(nz) * .1F);
            double[] vertices = new double[20];
            for(int v = 0; v < 4; v++) {
                ObjMesh.Corner corner = face.corners.get(Math.min(v, face.corners.size() - 1));
                if(corner.uv == null) throw new IllegalArgumentException("Standard duct OBJ requires UVs");
                vertices[v * 5] = .5D + corner.position.x * (inventory ? -1.25D : 1D);
                vertices[v * 5 + 1] = .5D + corner.position.y * (inventory ? 1.25D : 1D);
                vertices[v * 5 + 2] = .5D + corner.position.z * (inventory ? -1.25D : 1D);
                vertices[v * 5 + 3] = corner.uv.x * 16D;
                vertices[v * 5 + 4] = corner.uv.y * 16D;
            }
            result.add(ConnectionQuadFactory.coloredQuad(sprite, inventory, shade, color, inventory ? -nx : nx, ny,
                    inventory ? -nz : nz, vertices));
        }
    }
}
