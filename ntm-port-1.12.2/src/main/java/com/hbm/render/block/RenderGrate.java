package com.hbm.render.block;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.hbm.blocks.generic.BlockGrate;
import com.hbm.render.block.nativeblock.ConveyorCuboidGeometry;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;

/** Original normalized standard-block faces; collision's wide-grate epsilon never altered the rendered thickness. */
public final class RenderGrate {
    private RenderGrate() { }
    public static List<BakedQuad> bake(int metadata, TextureAtlasSprite top, TextureAtlasSprite side) {
        double y = metadata == 9 ? -.125F : metadata * .125F;
        y -= BlockGrate.renderShift(metadata);
        List<BakedQuad> quads = new ArrayList<>();
        for(EnumFacing face : EnumFacing.values()) quads.add(ConveyorCuboidGeometry.face(
                face.getAxis() == EnumFacing.Axis.Y ? top : side, false, face, 0, 0, y, 0, 1, y + .125D, 1, 0));
        return Collections.unmodifiableList(quads);
    }
}
