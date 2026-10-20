package com.hbm.render.block;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.hbm.render.block.nativeblock.AtlasObjGeometry;
import com.hbm.render.mesh.ObjMesh;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

/** Native geometry successor for all four original decorative pipe shapes. */
public final class RenderPipe {
    private RenderPipe() { }
    public static List<BakedQuad> bake(ObjMesh body, ObjMesh frame, int renderType, int metadata, boolean inventory,
            TextureAtlasSprite top, TextureAtlasSprite side, TextureAtlasSprite frameSprite, TextureAtlasSprite meshSprite) {
        if(renderType < 0 || renderType > 3) throw new IllegalArgumentException("Invalid decorative pipe shape " + renderType);
        float yaw = !inventory && metadata == 8 ? (float)(Math.PI * .5) : 0;
        float pitch = !inventory && (metadata == 8 || metadata == 4) ? (float)(Math.PI * .5) : 0;
        List<BakedQuad> quads = new ArrayList<>();
        AtlasObjGeometry.appendPart(quads, body, "Top", top, inventory, yaw, pitch, 1, .5, .5, .5);
        AtlasObjGeometry.appendPart(quads, body, "Side", side, inventory, yaw, pitch, 1, .5, .5, .5);
        if(renderType == 3) {
            AtlasObjGeometry.appendPart(quads, frame, "Frame", frameSprite, inventory, yaw, pitch, 1, .5, .5, .5);
            AtlasObjGeometry.appendPart(quads, frame, "Mesh", meshSprite, inventory, yaw, pitch, 1, .5, .5, .5);
        }
        return Collections.unmodifiableList(quads);
    }
}
