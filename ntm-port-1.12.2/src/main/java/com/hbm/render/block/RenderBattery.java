package com.hbm.render.block;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.hbm.render.block.nativeblock.AtlasObjGeometry;
import com.hbm.render.mesh.ObjMesh;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

/** Native whole-OBJ successor retaining the original HEV battery world and item transforms. */
public final class RenderBattery {
    private RenderBattery() { }
    public static List<BakedQuad> bake(ObjMesh mesh, boolean inventory, TextureAtlasSprite sprite) {
        List<BakedQuad> quads = new ArrayList<>();
        AtlasObjGeometry.appendWhole(quads, mesh, sprite, inventory, 0, 0, inventory ? 3D : 1D,
                .5D, inventory ? -0.125D : 0D, .5D);
        return Collections.unmodifiableList(quads);
    }
}
