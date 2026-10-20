package com.hbm.render.block;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.hbm.render.block.nativeblock.AtlasObjGeometry;
import com.hbm.render.mesh.ObjMesh;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

/** Five original material groups, with the original six-direction Z-then-Y orientation. */
public final class RenderCapacitor {
    private RenderCapacitor() { }
    public static List<BakedQuad> bake(ObjMesh mesh, int metadata, boolean inventory, TextureAtlasSprite[] sprites) {
        if(sprites.length != 5) throw new IllegalArgumentException("Capacitor requires five material sprites");
        float pitch = 0, yaw = 0;
        if(!inventory) {
            if(metadata == 0) pitch = (float)Math.PI;
            if(metadata == 2) yaw = 90F / 180F * (float)Math.PI;
            if(metadata == 3) yaw = 270F / 180F * (float)Math.PI;
            if(metadata == 4) yaw = 180F / 180F * (float)Math.PI;
            if(yaw != 0F || metadata == 5) pitch = (float)Math.PI * .5F;
        }
        String[] names = {"Top", "Side", "Bottom", "InnerTop", "InnerSide"};
        List<BakedQuad> quads = new ArrayList<>();
        for(int i = 0; i < names.length; i++) AtlasObjGeometry.appendPart(quads, mesh, names[i], sprites[i], inventory, yaw, pitch, 1, .5, .5, .5);
        return Collections.unmodifiableList(quads);
    }
}
