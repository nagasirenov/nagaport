package com.hbm.render.block;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.hbm.render.block.nativeblock.ConveyorCuboidGeometry;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

/** Original three standard-renderer cuboids, in original order, with per-face atlas materials. */
public final class RenderPedestal {
    private RenderPedestal() { }
    public static List<BakedQuad> bake(TextureAtlasSprite top, TextureAtlasSprite side, boolean inventory) {
        List<BakedQuad> result = new ArrayList<>();
        TextureAtlasSprite[] sprites = {top, top, side, side, side, side};
        int[] rotations = new int[6];
        ConveyorCuboidGeometry.append(result, inventory, sprites, rotations, 0, 0, 0, 1, .25, 1);
        ConveyorCuboidGeometry.append(result, inventory, sprites, rotations, 0, .75, 0, 1, 1, 1);
        ConveyorCuboidGeometry.append(result, inventory, sprites, rotations, .125, .25, .125, .875, .75, .875);
        return Collections.unmodifiableList(result);
    }
}
