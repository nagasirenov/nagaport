package com.hbm.render.block;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.hbm.render.block.nativeblock.AtlasObjGeometry;
import com.hbm.render.mesh.ObjMesh;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

/** Original splitter OBJ groups, material selection and transforms in native atlas quads. */
public final class RenderSplitter {
    private RenderSplitter() { }
    public static List<BakedQuad> bake(ObjMesh mesh, int metadata, boolean inventory, TextureAtlasSprite[] sprites) {
        if(sprites.length != 11) throw new IllegalArgumentException("Eleven splitter materials required");
        List<BakedQuad> result = new ArrayList<>();
        if(inventory) {
            // Native item draw centers at (-.5,-.5,-.5). Bake the original
            // S(.625) RY(-90) T(0,-.5,.5), then the second half T(0,0,-1).
            append(result, mesh, sprites, true, true, -(float)Math.PI * .5F, .625, .1875, .1875, .5);
            append(result, mesh, sprites, false, true, -(float)Math.PI * .5F, .625, .8125, .1875, .5);
        } else {
            append(result, mesh, sprites, metadata >= 12, false, rotation(metadata), 1, .5, 0, .5);
        }
        return Collections.unmodifiableList(result);
    }
    private static void append(List<BakedQuad> result, ObjMesh mesh, TextureAtlasSprite[] sprites,
            boolean left, boolean inventory, float yaw, double scale, double x, double y, double z) {
        String[] groups = {"Top", "Bottom", left ? "Left" : "Right", "Back", "Front", "Inner", "InnerLeft", "InnerRight", "InnerTop", "InnerBottom"};
        int[] materials = {left ? 0 : 1, left ? 1 : 0, left ? 6 : 7, left ? 4 : 5, left ? 2 : 3, 9, 10, 10, 10, 8};
        for(int i = 0; i < groups.length; i++) AtlasObjGeometry.appendPart(result, mesh, groups[i], sprites[materials[i]], inventory,
                yaw, 0, scale, x, y, z);
    }
    public static float rotation(int metadata) {
        if(metadata == 12 || metadata == 4) return 90F / 180F * (float)Math.PI;
        if(metadata == 13 || metadata == 5) return 270F / 180F * (float)Math.PI;
        if(metadata == 14 || metadata == 3) return 180F / 180F * (float)Math.PI;
        return 0;
    }
}
