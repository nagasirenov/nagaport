package com.hbm.render.block;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.hbm.blocks.network.ConveyorModelState;
import com.hbm.render.block.nativeblock.ConveyorCuboidGeometry;
import com.hbm.render.block.nativeblock.ConveyorMaterials;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;

/** Original lift boxes/materials, with native neighbor state rather than mutable render bounds. */
public final class RenderConveyorLift {
    private RenderConveyorLift() { }
    public static List<BakedQuad> bake(int metadata, int neighbors, ConveyorMaterials materials) {
        List<BakedQuad> quads = new ArrayList<>();
        boolean bottom = (neighbors & ConveyorModelState.ABOVE_MIN_Y) != 0 && !ConveyorModelState.belt(neighbors, EnumFacing.DOWN);
        boolean top = (neighbors & ConveyorModelState.BELOW_MAX_Y) != 0 && !ConveyorModelState.belt(neighbors, EnumFacing.UP)
                && !bottom && (neighbors & ConveyorModelState.ABOVE_ENTERABLE) == 0;
        TextureAtlasSprite[] belt = ConveyorMaterials.uniform(materials.belt);
        TextureAtlasSprite[] concrete = ConveyorMaterials.uniform(materials.concrete);
        int[] zero = new int[6];
        if(bottom) {
            if(metadata != 5) box(quads, belt, horizontal(1,1), 0,0,.25, .25,.25,.75);
            if(metadata != 4) box(quads, belt, horizontal(2,2), .75,0,.25, 1,.25,.75);
            if(metadata != 3) box(quads, belt, horizontal(0,3), .25,0,0, .75,.25,.25);
            if(metadata != 2) box(quads, belt, horizontal(3,0), .25,0,.75, .75,.25,1);
        }
        if(!top) {
            if(metadata == 2) {
                box(quads,concrete,zero, 0,0,0, .25,1,.25); box(quads,concrete,zero, .75,0,0, 1,1,.25);
                box(quads,concrete,zero, 0,0,.75, 1,1,1); box(quads,belt,horizontal(0,3), .25,0,.625, .75,1,.75);
            }
            if(metadata == 3) {
                box(quads,concrete,zero, 0,0,0, 1,1,.25); box(quads,concrete,zero, 0,0,.75, .25,1,1);
                box(quads,concrete,zero, .75,0,.75, 1,1,1); box(quads,belt,zero, .25,0,.25, .75,1,.375);
            }
            if(metadata == 4) {
                box(quads,concrete,zero, 0,0,0, .25,1,.25); box(quads,concrete,zero, 0,0,.75, .25,1,1);
                box(quads,concrete,zero, .75,0,0, 1,1,1); box(quads,belt,horizontal(0,1), .625,0,.25, .75,1,.75);
            }
            if(metadata == 5) {
                box(quads,concrete,zero, .75,0,0, 1,1,.25); box(quads,concrete,zero, .75,0,.75, 1,1,1);
                box(quads,concrete,zero, 0,0,0, .25,1,1); box(quads,belt,horizontal(0,2), .25,0,.25, .375,1,.75);
            }
        } else {
            if(metadata == 2 || metadata == 3) {
                box(quads,concrete,zero, 0,0,0, .25,.5,1); box(quads,concrete,zero, .75,0,0, 1,.5,1);
                int[] turns = zero.clone(); turns[1] = metadata == 2 ? 3 : 0; turns[metadata == 2 ? 3 : 2] = 3;
                if(metadata == 2) box(quads,materials.beltFaces(metadata,false),turns, .25,0,.625, .75,.25,1);
                else box(quads,materials.beltFaces(metadata,false),turns, .25,0,0, .75,.25,.375);
            }
            if(metadata == 4 || metadata == 5) {
                box(quads,concrete,zero, 0,0,0, 1,.5,.25); box(quads,concrete,zero, 0,0,.75, 1,.5,1);
                int[] turns = zero.clone(); turns[1] = metadata == 4 ? 1 : 2; turns[metadata == 4 ? 5 : 4] = 3;
                if(metadata == 4) box(quads,materials.beltFaces(metadata,false),turns, .625,0,.25, 1,.25,.75);
                else box(quads,materials.beltFaces(metadata,false),turns, 0,0,.25, .375,.25,.75);
            }
        }
        if(bottom) box(quads,ConveyorMaterials.uniform(materials.iron),zero,
                .25 + (metadata == 5 ? .125 : 0), 0, .25 + (metadata == 3 ? .125 : 0),
                .75 - (metadata == 4 ? .125 : 0), .25, .75 - (metadata == 2 ? .125 : 0));
        return Collections.unmodifiableList(quads);
    }
    private static int[] horizontal(int bottom, int top) { return new int[] {bottom, top, 0, 0, 0, 0}; }
    private static void box(List<BakedQuad> out, TextureAtlasSprite[] materials, int[] rotations,
            double x0,double y0,double z0,double x1,double y1,double z1) {
        ConveyorCuboidGeometry.append(out,false,materials,rotations,x0,y0,z0,x1,y1,z1);
    }
}
