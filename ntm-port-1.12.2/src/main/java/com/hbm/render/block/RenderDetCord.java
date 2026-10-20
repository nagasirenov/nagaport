package com.hbm.render.block;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.hbm.blocks.network.ConnectionState;
import com.hbm.render.block.nativeblock.ConnectionQuadFactory;
import com.hbm.render.mesh.ObjMesh;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

/** Native successor of the original detonator cord renderer, using its original OBJ groups. */
public final class RenderDetCord {
    private RenderDetCord() { }
    public static List<BakedQuad> bake(ObjMesh mesh, int connections, boolean inventory, TextureAtlasSprite sprite) {
        if(connections < 0 || connections > 63) throw new IllegalArgumentException("Invalid connection mask " + connections);
        List<String> groups = new ArrayList<>();
        if(inventory) groups.add("CZ");
        else if(connections == (ConnectionState.POS_X | ConnectionState.NEG_X) || connections == ConnectionState.POS_X || connections == ConnectionState.NEG_X) groups.add("CX");
        else if(connections == (ConnectionState.POS_Y | ConnectionState.NEG_Y) || connections == ConnectionState.POS_Y || connections == ConnectionState.NEG_Y) groups.add("CY");
        else if(connections == (ConnectionState.POS_Z | ConnectionState.NEG_Z) || connections == ConnectionState.POS_Z || connections == ConnectionState.NEG_Z) groups.add("CZ");
        else {
            groups.add("Core");
            int[] bits = {32, 16, 8, 4, 1, 2};
            String[] arms = {"posX", "negX", "posY", "negY", "posZ", "negZ"};
            // The original mesh reverses the world Z-arm names.
            for(int i = 0; i < bits.length; i++) if((connections & bits[i]) != 0) groups.add(arms[i]);
        }
        List<BakedQuad> result = new ArrayList<>();
        for(String name : groups) {
            ObjMesh.Part selected = null;
            for(ObjMesh.Part part : mesh.parts) if(name.equals(part.name)) selected = part;
            if(selected == null) throw new IllegalArgumentException("Missing cable OBJ part " + name);
            for(ObjMesh.Face face : selected.faces) {
                float nx = face.normal.x, ny = face.normal.y, nz = face.normal.z;
                float shade = inventory ? 1 : Math.max(.45F, ny * .3F + .7F - Math.abs(nx) * .1F + Math.abs(nz) * .1F);
                double[] vertices = new double[20];
                for(int v = 0; v < 4; v++) {
                    ObjMesh.Corner corner = face.corners.get(Math.min(v, face.corners.size() - 1));
                    if(corner.uv == null) throw new IllegalArgumentException("Cable OBJ requires UVs");
                    vertices[v * 5] = .5D + corner.position.x * (inventory ? -1.25D : 1D);
                    vertices[v * 5 + 1] = .5D + corner.position.y * (inventory ? 1.25D : 1D);
                    vertices[v * 5 + 2] = .5D + corner.position.z * (inventory ? -1.25D : 1D);
                    vertices[v * 5 + 3] = corner.uv.x * 16D;
                    vertices[v * 5 + 4] = corner.uv.y * 16D;
                }
                result.add(ConnectionQuadFactory.quad(sprite, inventory, shade, inventory ? -nx : nx, ny, inventory ? -nz : nz, vertices));
            }
        }
        return Collections.unmodifiableList(result);
    }
}
