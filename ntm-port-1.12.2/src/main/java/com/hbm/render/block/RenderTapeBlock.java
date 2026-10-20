package com.hbm.render.block;
import java.util.*;
import com.hbm.render.block.nativeblock.AtlasObjGeometry;
import com.hbm.render.mesh.ObjMesh;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
/** Native whole-OBJ tape/pole rendering. The original pole rotation differs only in world context. */
public final class RenderTapeBlock {
    private RenderTapeBlock() { }
    public static List<BakedQuad> bake(ObjMesh mesh, boolean poles, int metadata, boolean inventory, TextureAtlasSprite sprite) {
        float rotation = 0;
        if(!inventory) {
            if(metadata == 2) rotation = 90F / 180F * (float)Math.PI;
            if(metadata == 3) rotation = 270F / 180F * (float)Math.PI;
            if(metadata == 4) rotation = 180F / 180F * (float)Math.PI;
            if(poles) rotation -= 90F / 180F * (float)Math.PI;
        }
        List<BakedQuad> quads = new ArrayList<>();
        AtlasObjGeometry.appendWhole(quads, mesh, sprite, inventory, rotation, 0, 1, .5, 0, .5);
        return Collections.unmodifiableList(quads);
    }
}
