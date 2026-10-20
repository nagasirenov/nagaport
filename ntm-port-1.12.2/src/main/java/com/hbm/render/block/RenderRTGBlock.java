package com.hbm.render.block;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.hbm.render.block.nativeblock.AtlasObjGeometry;
import com.hbm.render.mesh.ObjMesh;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

/** Original RTG inventory Gen mesh. World rendering remains in its tile renderer. */
public final class RenderRTGBlock {
    private RenderRTGBlock() { }
    public static List<BakedQuad> bake(ObjMesh mesh, TextureAtlasSprite sprite) {
        List<BakedQuad> quads = new ArrayList<>();
        AtlasObjGeometry.appendPart(quads, mesh, "Gen", sprite, true, 0, 0, 1, .5, 0, .5);
        return Collections.unmodifiableList(quads);
    }
}
