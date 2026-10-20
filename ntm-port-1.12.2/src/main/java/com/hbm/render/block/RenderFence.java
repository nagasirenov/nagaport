package com.hbm.render.block;

import java.util.*;
import com.hbm.render.block.nativeblock.StructuralGeometry;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

/** The original two zero-width fence planes and optional post, without RenderBlocks. */
public final class RenderFence {
    private RenderFence() { }
    public static List<BakedQuad> bake(int metadata, boolean xNeg, boolean xPos, boolean zNeg, boolean zPos,
            TextureAtlasSprite fence, TextureAtlasSprite post) {
        boolean hasX = xNeg || xPos, hasZ = zNeg || zPos;
        boolean straightX = !hasZ && xNeg && xPos, straightZ = !hasX && zNeg && zPos;
        boolean showPost = metadata == 1 || (!straightX && !straightZ);
        if(!hasX && !hasZ) hasX = true;
        List<BakedQuad> quads = new ArrayList<>();
        TextureAtlasSprite panel = metadata == 0 ? fence : post;
        if(hasX) StructuralGeometry.fenceBox(quads, panel, xNeg ? 0 : .4375, 0, .5, xPos ? 1 : .5625, 1, .5);
        if(hasZ) StructuralGeometry.fenceBox(quads, panel, .5, 0, zNeg ? 0 : .4375, .5, 1, zPos ? 1 : .5625);
        if(showPost) StructuralGeometry.fenceBox(quads, post, .375, 0, .375, .625, 1, .625);
        return Collections.unmodifiableList(quads);
    }
}
