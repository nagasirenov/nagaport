package com.hbm.render.block;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.hbm.render.block.nativeblock.AtlasObjGeometry;
import com.hbm.render.block.nativeblock.CRTPackedLightQuad;
import com.hbm.render.mesh.ObjMesh;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

/** Original CRT groups, atlas mapping, part shading and inventory whole-mesh stream. */
public final class RenderCRT {
    private RenderCRT() { }
    public static List<BakedQuad> bake(ObjMesh mesh, int metadata, boolean inventory, TextureAtlasSprite sprite) {
        List<BakedQuad> quads = new ArrayList<>();
        if(inventory) {
            // Native RenderItem and the existing frame helper supply T(-.5,-.5,-.5).
            // The resulting original inventory transform is T(0,-.5,0), yaw -pi/2.
            AtlasObjGeometry.appendWhole(quads, mesh, sprite, true, (float)Math.PI * -.5F, 0, 1, .5, 0, .5);
        } else {
            float yaw;
            switch(metadata % 4) {
                default: yaw = .5F * (float)Math.PI; break;
                case 1: yaw = 0; break;
                case 2: yaw = 1.5F * (float)Math.PI; break;
                case 3: yaw = (float)Math.PI; break;
            }
            AtlasObjGeometry.appendPart(quads, mesh, "Monitor", sprite, false, yaw, 0, 1, .5, 0, .5);
            List<BakedQuad> screen = new ArrayList<>();
            AtlasObjGeometry.appendPart(screen, mesh, "Screen", sprite, false, yaw, 0, 1, .5, 0, .5);
            for(BakedQuad quad : screen) quads.add(metadata >= 8 ? new CRTPackedLightQuad(quad, 240) : quad);
        }
        return Collections.unmodifiableList(quads);
    }
}
