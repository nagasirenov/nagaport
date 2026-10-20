package com.hbm.render.block;

import java.util.*;
import com.hbm.render.block.nativeblock.*;
import com.hbm.render.block.nativeblock.RBMKGeometry.Part;

/** Original passive-column geometry, shared by precisely seven registered owners. */
public final class RenderRBMKReflector {
    private RenderRBMKReflector() { }
    public static List<Part> bake(RBMKMaterials materials, boolean item, int lid) {
        List<Part> out = new ArrayList<>();
        if(item) for(int segment = 0; segment < 4; segment++)
            RBMKGeometry.cube(out, materials, 0, false, true, 63, 0, 0, 0, 1, 1, 1, segment, -.675);
        else {
            RBMKGeometry.cube(out, materials, 0, false, false, 63, 0, 0, 0, 1, 1, 1, 0, 0);
            if(lid != 0) RBMKGeometry.cube(out, materials, lid, false, false, 63, 0, 0, 0, 1, .25, 1, 1, 0);
        }
        return RBMKGeometry.immutable(out);
    }
}
