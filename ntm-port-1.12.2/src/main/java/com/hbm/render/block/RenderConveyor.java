package com.hbm.render.block;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.hbm.render.block.nativeblock.ConveyorCuboidGeometry;
import com.hbm.render.block.nativeblock.ConveyorMaterials;
import net.minecraft.client.renderer.block.model.BakedQuad;

/** Native quarter-slab geometry; AO and neighbor culling belong to the native baked pipeline. */
public final class RenderConveyor {
    private RenderConveyor() { }
    public static List<BakedQuad> bake(int metadata, boolean inventory, boolean bendable, ConveyorMaterials materials) {
        int selected = inventory ? 2 : metadata;
        List<BakedQuad> quads = new ArrayList<>();
        ConveyorCuboidGeometry.append(quads, inventory, materials.beltFaces(selected, bendable), rotations(selected),
                0, 0, 0, 1, .25, 1, inventory ? .25 : 0);
        return Collections.unmodifiableList(quads);
    }
    /** Indexed by native DOWN/UP/NORTH/SOUTH/WEST/EAST, not the old misleading field names. */
    public static int[] rotations(int metadata) {
        boolean bent = metadata > 5;
        if(metadata > 9) metadata -= 8;
        if(metadata > 5) metadata -= 4;
        return facingRotations(metadata, bent);
    }
    public static int[] facingRotations(int metadata, boolean bent) {
        int[] result = new int[6];
        if(metadata == 2) { result[1] = 3; if(!bent) result[3] = 3; }
        if(metadata == 3) { result[0] = 3; if(!bent) result[2] = 3; }
        if(metadata == 4) { result[1] = 1; result[0] = 1; if(!bent) result[5] = 3; }
        if(metadata == 5) { result[1] = 2; result[0] = 2; if(!bent) result[4] = 3; }
        return result;
    }
}
