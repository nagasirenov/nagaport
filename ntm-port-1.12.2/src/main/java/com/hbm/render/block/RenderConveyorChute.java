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

/** Original chute's full/neighbor-fed bottom and zero-width grate geometry. */
public final class RenderConveyorChute {
    private RenderConveyorChute() { }
    public static List<BakedQuad> bake(int metadata, int neighbors, boolean inventory, ConveyorMaterials materials) {
        List<BakedQuad> quads = new ArrayList<>();
        int selected = inventory ? 2 : metadata;
        TextureAtlasSprite[] beltMaterials = materials.beltFaces(selected,false);
        int[] zero = new int[6];
        boolean nX = !inventory && ConveyorModelState.belt(neighbors,EnumFacing.WEST);
        boolean pX = !inventory && ConveyorModelState.belt(neighbors,EnumFacing.EAST);
        boolean nZ = !inventory && ConveyorModelState.belt(neighbors,EnumFacing.NORTH);
        boolean pZ = !inventory && ConveyorModelState.belt(neighbors,EnumFacing.SOUTH);
        boolean belt = inventory;
        if(inventory || (neighbors & ConveyorModelState.ABOVE_MIN_Y) != 0) {
            if(inventory || !(ConveyorModelState.belt(neighbors,EnumFacing.DOWN) || (neighbors & ConveyorModelState.BELOW_ENTERABLE) != 0)) {
                int[] rotations = RenderConveyor.facingRotations(selected,false);
                box(quads,inventory,beltMaterials,rotations, .25,0,0, .75,.25,1);
                box(quads,inventory,beltMaterials,rotations, 0,0,.25, .25,.25,.75);
                box(quads,inventory,beltMaterials,rotations, .75,0,.25, 1,.25,.75);
                belt = true;
            } else {
                if(nX) box(quads,false,beltMaterials,top(1), 0,0,.25, .125,.25,.75);
                if(pX) box(quads,false,beltMaterials,top(2), .875,0,.25, 1,.25,.75);
                if(nZ) box(quads,false,beltMaterials,top(3), .25,0,0, .75,.25,.125);
                if(pZ) box(quads,false,beltMaterials,zero, .25,0,.875, .75,.25,1);
            }
        }
        TextureAtlasSprite[] concrete = ConveyorMaterials.uniform(materials.concrete);
        box(quads,inventory,concrete,zero, 0,0,0, .25,1,.25);
        box(quads,inventory,concrete,zero, .75,0,0, 1,1,.25);
        box(quads,inventory,concrete,zero, 0,0,.75, .25,1,1);
        box(quads,inventory,concrete,zero, .75,0,.75, 1,1,1);
        TextureAtlasSprite[] grate = ConveyorMaterials.uniform(materials.grate);
        if(!nX && (!belt || selected != 5)) box(quads,inventory,grate,zero, .125,belt?.25:0,.25, .125,1,.75);
        if(!pX && (!belt || selected != 4)) box(quads,inventory,grate,zero, .875,belt?.25:0,.25, .875,1,.75);
        if(!nZ && (!belt || selected != 3)) box(quads,inventory,grate,zero, .25,belt?.25:0,.125, .75,1,.125);
        if(!pZ && (!belt || selected != 2)) box(quads,inventory,grate,zero, .25,belt?.25:0,.875, .75,1,.875);
        return Collections.unmodifiableList(quads);
    }
    private static int[] top(int turn) { return new int[] {0,turn,0,0,0,0}; }
    private static void box(List<BakedQuad> out, boolean inventory, TextureAtlasSprite[] materials, int[] rotations,
            double x0,double y0,double z0,double x1,double y1,double z1) {
        ConveyorCuboidGeometry.append(out,inventory,materials,rotations,x0,y0,z0,x1,y1,z1);
    }
}
