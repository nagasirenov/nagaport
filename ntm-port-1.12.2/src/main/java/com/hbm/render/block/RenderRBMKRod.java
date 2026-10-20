package com.hbm.render.block;

import java.util.*;
import com.hbm.render.mesh.ObjMesh;
import com.hbm.render.block.nativeblock.*;
import com.hbm.render.block.nativeblock.RBMKGeometry.Part;

/** Original fuel-channel shell, cap and inner mesh; fuel rods belong only to its inventory model. */
public final class RenderRBMKRod {
    private RenderRBMKRod() { }
    public static List<Part> bake(RBMKMaterials materials, ObjMesh element, ObjMesh rods, boolean item, int lid) {
        List<Part> out = new ArrayList<>();
        if(item) for(int segment = 0; segment < 4; segment++) {
            RBMKGeometry.obj(out, element, "Inner", materials.texture("_inner"), true, false, 0xffffff, false, segment, -.675);
            RBMKGeometry.obj(out, element, "Cap", materials.face(0, 0, false), true, false, 0xffffff, true, segment, -.675);
            RBMKGeometry.cube(out, materials, 0, false, true, 60, 0, 0, 0, 1, 1, 1, segment, -.675);
            RBMKGeometry.obj(out, rods, "Rods", materials.texture("_fuel"), true, false, 0x304825, false, segment, -.675);
        } else {
            RBMKGeometry.cube(out, materials, 0, false, false, 60, 0, 0, 0, 1, 1, 1, 0, 0);
            RBMKGeometry.obj(out, element, "Cap", materials.face(0, 0, false), false, true, 0xffffff, true, 0, 0);
            RBMKGeometry.obj(out, element, "Inner", materials.texture("_inner"), false, true, 0xffffff, false, 0, 0);
            if(lid != 0) RBMKGeometry.cube(out, materials, lid, false, false, 63, 0, 0, 0, 1, .25, 1, 1, 0);
        }
        return RBMKGeometry.immutable(out);
    }
}
