package com.hbm.render.block;

import java.util.*;
import com.hbm.render.mesh.ObjMesh;
import com.hbm.render.block.nativeblock.*;
import com.hbm.render.block.nativeblock.RBMKGeometry.Part;

/** Original four pipe caps and inventory-only control lid, without global render flags. */
public final class RenderRBMKControl {
    private RenderRBMKControl() { }
    public static List<Part> bake(RBMKMaterials materials, ObjMesh control, boolean item, int lid, boolean extra) {
        List<Part> out = new ArrayList<>();
        for(int segment = 0; segment < (item ? 4 : 1); segment++)
            RBMKGeometry.cube(out, materials, 0, false, item, 63, 0, 0, 0, 1, 1, 1, segment, -.75);
        if(!item && !materials.profile.ownLid && lid != 0 && extra)
            RBMKGeometry.cube(out, materials, lid, false, false, 63, 0, 0, 0, 1, .25, 1, 1, 0);
        else if(item || extra) {
            double[][] corners = {{.0625, .0625}, {.0625, .5625}, {.5625, .5625}, {.5625, .0625}};
            for(double[] corner : corners) RBMKGeometry.cube(out, materials, 0, true, item, item ? 62 : 63,
                    corner[0], 0, corner[1], corner[0] + .375, .125, corner[1] + .375, item ? 4 : 1, -.75);
        }
        if(item && materials.profile.ownLid)
            RBMKGeometry.obj(out, control, "Lid", materials.face(1, 0, false), true, true, 0xffffff, true, 3, -.75);
        return RBMKGeometry.immutable(out);
    }
}
