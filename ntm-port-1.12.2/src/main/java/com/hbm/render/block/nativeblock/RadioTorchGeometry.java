package com.hbm.render.block.nativeblock;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.hbm.render.mesh.ObjMesh;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormatElement;

/** Original whole-OBJ stream and UVs, with RenderRTTY's explicit shadow=false white color. */
public final class RadioTorchGeometry {
    private RadioTorchGeometry() { }
    public static List<BakedQuad> bake(ObjMesh mesh, TextureAtlasSprite sprite, float yaw, float flip) {
        List<BakedQuad> shaded = new ArrayList<>(), result = new ArrayList<>();
        AtlasObjGeometry.appendWhole(shaded, mesh, sprite, false, yaw, flip, 1, .5, .5, .5);
        for(BakedQuad quad : shaded) {
            int[] data = quad.getVertexData().clone();
            int stride = quad.getFormat().getIntegerSize();
            for(int e = 0; e < quad.getFormat().getElementCount(); e++) {
                if(quad.getFormat().getElement(e).getUsage() != VertexFormatElement.EnumUsage.COLOR) continue;
                int offset = quad.getFormat().getOffset(e) / 4;
                for(int vertex = 0; vertex < 4; vertex++) data[vertex * stride + offset] = 0xffffffff;
            }
            result.add(new BlockPositionLightQuad(new BakedQuad(data, -1, quad.getFace(), sprite, false, quad.getFormat())));
        }
        return Collections.unmodifiableList(result);
    }
}
