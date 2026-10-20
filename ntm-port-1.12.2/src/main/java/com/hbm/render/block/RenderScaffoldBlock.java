package com.hbm.render.block;

import java.util.*;
import com.hbm.render.block.nativeblock.AtlasObjGeometry;
import com.hbm.render.mesh.ObjMesh;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

/** Native baked successor retaining original whole-OBJ stream, rotation and shading. */
public final class RenderScaffoldBlock {
    private RenderScaffoldBlock() { }
    public static List<BakedQuad> bake(ObjMesh mesh, int meta, boolean inventory, TextureAtlasSprite sprite) {
        float rotation = (float)Math.PI * -.5F, pitch = 0;
        double ox = .5, oy = 0, oz = .5;
        if(inventory) rotation = 0;
        else if(meta >= 12) { pitch = (float)Math.PI * .5F; rotation = (float)-Math.PI; ox = 1; oy = .5; }
        else if(meta >= 8) rotation = (float)-Math.PI;
        else if(meta >= 4) { pitch = (float)Math.PI * .5F; oy = .5; oz = 0; }
        List<BakedQuad> quads = new ArrayList<>();
        AtlasObjGeometry.appendWhole(quads, mesh, sprite, inventory, rotation, pitch, 1, ox, oy, oz);
        return Collections.unmodifiableList(quads);
    }
}
