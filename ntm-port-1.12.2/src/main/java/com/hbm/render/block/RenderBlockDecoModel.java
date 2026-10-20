package com.hbm.render.block;
import java.util.*;
import com.hbm.render.mesh.ObjMesh;
import com.hbm.render.block.nativeblock.AtlasObjGeometry;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
/** Original computer whole-OBJ stream and transform, using native atlas quads. */
public final class RenderBlockDecoModel {
    private RenderBlockDecoModel() { }
    public static List<BakedQuad> bake(ObjMesh mesh, int metadata, boolean inventory, TextureAtlasSprite sprite) {
        float yaw=inventory || (metadata >> 2)==1 ? 0 : (metadata >> 2)==2 ? 1.5F*(float)Math.PI
                : (metadata >> 2)==3 ? .5F*(float)Math.PI : (float)Math.PI;
        List<BakedQuad> quads=new ArrayList<>();
        AtlasObjGeometry.appendWhole(quads,mesh,sprite,inventory,yaw,0,inventory?1.2D:1D,.5,inventory?.6D:.5D,.5);
        return Collections.unmodifiableList(quads);
    }
}
