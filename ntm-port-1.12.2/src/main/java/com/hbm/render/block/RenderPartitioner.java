package com.hbm.render.block;

import java.util.*;
import com.hbm.render.block.nativeblock.AtlasObjGeometry;
import com.hbm.render.mesh.ObjMesh;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

/** Native original crane-buffer material groups and metadata rotations. */
public final class RenderPartitioner {
    private RenderPartitioner() { }
    public static List<BakedQuad> bake(ObjMesh mesh, int metadata, boolean inventory, TextureAtlasSprite[] sprites) {
        if(sprites.length != 6) throw new IllegalArgumentException("Six partitioner materials required");
        float rotation = inventory ? (float)Math.PI * .5F : rotation(metadata);
        String[] parts = {"Side", "Back", "Top_Top.001", "Inner", "InnerSide", "Belt"};
        List<BakedQuad> result = new ArrayList<>();
        for(int i = 0; i < parts.length; i++) AtlasObjGeometry.appendPart(result, mesh, parts[i], sprites[i], inventory,
                rotation, 0, 1, .5, 0, .5);
        return Collections.unmodifiableList(result);
    }
    public static float rotation(int metadata) {
        if(metadata == 2) return 90F / 180F * (float)Math.PI;
        if(metadata == 4) return 180F / 180F * (float)Math.PI;
        if(metadata == 3) return 270F / 180F * (float)Math.PI;
        return 0;
    }
}
