package com.hbm.render.block;
import java.util.*;
import com.hbm.render.mesh.ObjMesh;
import com.hbm.render.block.nativeblock.AtlasObjGeometry;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
/** Original appliance mesh selector, now consumed by native baked models. */
public final class RenderToaster {
    private RenderToaster() { }
    public static List<BakedQuad> bake(ObjMesh mesh, int metadata, boolean inventory, TextureAtlasSprite sprite) {
        float yaw = inventory ? (float)Math.PI * -.5F : metadata % 4 == 0 ? .5F * (float)Math.PI
                : metadata % 4 == 1 ? 0 : metadata % 4 == 2 ? 1.5F * (float)Math.PI : (float)Math.PI;
        List<BakedQuad> quads = new ArrayList<>();
        AtlasObjGeometry.appendWhole(quads, mesh, sprite, inventory, yaw, 0, 1, .5, inventory ? .25 : 0, .5);
        return Collections.unmodifiableList(quads);
    }
}
