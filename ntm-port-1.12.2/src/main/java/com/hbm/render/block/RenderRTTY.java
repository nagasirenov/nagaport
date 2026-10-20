package com.hbm.render.block;

import java.util.List;
import com.hbm.render.block.nativeblock.RadioTorchGeometry;
import com.hbm.render.mesh.ObjMesh;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

/** Native successor of RenderRTTY, retaining the original OBJ orientation sequence. */
public final class RenderRTTY {
    private RenderRTTY() { }
    public static List<BakedQuad> bake(ObjMesh mesh, int metadata, TextureAtlasSprite sprite) {
        float flip = 0, rotation = 0;
        if(metadata == 0) flip = (float)Math.PI;
        if(metadata == 2) rotation = 90F / 180F * (float)Math.PI;
        if(metadata == 3) rotation = 270F / 180F * (float)Math.PI;
        if(metadata == 4) rotation = 180F / 180F * (float)Math.PI;
        if(rotation != 0F || metadata == 5) flip = (float)Math.PI * .5F;
        return RadioTorchGeometry.bake(mesh, sprite, rotation, flip);
    }
}
