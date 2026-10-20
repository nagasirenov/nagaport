package com.hbm.render.block;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import com.hbm.blocks.generic.BlockSteelDecoration;
import com.hbm.render.block.nativeblock.ConveyorCuboidGeometry;
import com.hbm.render.block.nativeblock.PneumaticCuboidGeometry;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;

/** Three original slabs, with RenderBlocksNT world UVs and the inventory's two omitted end faces. */
public final class RenderSteelCorner {
    private RenderSteelCorner() { }
    public static List<BakedQuad> bake(int metadata, boolean inventory, TextureAtlasSprite sprite) {
        List<AxisAlignedBB> boxes = inventory ? Arrays.asList(new AxisAlignedBB(0, 0, .875, .75, 1, 1),
                new AxisAlignedBB(.75, 0, .75, 1, 1, 1), new AxisAlignedBB(.875, 0, 0, 1, 1, .75))
                : BlockSteelDecoration.cornerBounds(metadata);
        List<BakedQuad> quads = new ArrayList<>();
        for(int index = 0; index < boxes.size(); index++) {
            AxisAlignedBB box = boxes.get(index);
            for(EnumFacing face : EnumFacing.values()) {
                if(inventory && ((index == 0 && face == EnumFacing.EAST) || (index == 2 && face == EnumFacing.SOUTH))) continue;
                quads.add(inventory ? ConveyorCuboidGeometry.face(sprite, true, face, 0,
                        box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ, 0)
                        : PneumaticCuboidGeometry.face(sprite, false, face, 0,
                        box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ, 0));
            }
        }
        return Collections.unmodifiableList(quads);
    }
}
