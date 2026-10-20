package com.hbm.render.block;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.hbm.render.block.nativeblock.AtlasObjGeometry;
import com.hbm.render.mesh.ObjMesh;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

/** Native whole-OBJ successor retaining the original antenna top world and item transforms. */
public final class RenderAntennaTop {
    private RenderAntennaTop() { }
    public static List<BakedQuad> bake(ObjMesh mesh, boolean inventory, TextureAtlasSprite sprite) {
        List<BakedQuad> quads = new ArrayList<>();
        AtlasObjGeometry.appendWhole(quads, mesh, sprite, inventory, 0, 0, inventory ? 0.75D : 1D,
                .5D, inventory ? 0D : 0D, .5D);
        return Collections.unmodifiableList(quads);
    }
}
