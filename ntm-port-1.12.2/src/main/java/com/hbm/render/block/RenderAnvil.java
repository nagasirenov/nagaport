package com.hbm.render.block;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.hbm.render.block.nativeblock.AtlasObjGeometry;
import com.hbm.render.mesh.ObjMesh;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

/** Six original named OBJ parts, top-only murky material, and original metadata rotations. */
public final class RenderAnvil {
    private RenderAnvil() { }
    public static List<BakedQuad> bake(ObjMesh mesh, int metadata, boolean inventory,
            TextureAtlasSprite top, TextureAtlasSprite side) {
        List<BakedQuad> quads = new ArrayList<>();
        float yaw = inventory ? (float)Math.PI : metadata == 2 ? 90F / 180F * (float)Math.PI
                : metadata == 3 ? 270F / 180F * (float)Math.PI
                : metadata == 4 ? 180F / 180F * (float)Math.PI : 0;
        // Native item models rotate about (.5,.5,.5); this is legacy GL translate(0,-.5,0), rotate(180).
        for(String part : new String[] {"Top", "Bottom", "Front", "Back", "Left", "Right"})
            AtlasObjGeometry.appendPart(quads, mesh, part, part.equals("Top") ? top : side,
                    inventory, yaw, 0, 1, .5, 0, .5);
        return Collections.unmodifiableList(quads);
    }
}
