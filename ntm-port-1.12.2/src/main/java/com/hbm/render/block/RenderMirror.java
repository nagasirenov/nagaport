package com.hbm.render.block;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.hbm.blocks.machine.SolarMirrorModelState.Snapshot;
import com.hbm.render.block.nativeblock.AtlasObjGeometry;
import com.hbm.render.block.nativeblock.SolarMirrorGeometry;
import com.hbm.render.mesh.ObjMesh;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

/** Original Base/Mirror part selection, translation, and target-directed mirror stream. */
public final class RenderMirror {
    private RenderMirror() { }
    public static List<BakedQuad> bake(ObjMesh mesh, TextureAtlasSprite sprite, Snapshot target) {
        if(!target.available) return Collections.emptyList();
        List<BakedQuad> quads = new ArrayList<>();
        AtlasObjGeometry.appendPart(quads, mesh, "Base", sprite, false, 0, 0, 1, .5, 0, .5);
        if(target.pitched) SolarMirrorGeometry.append(quads, mesh, sprite, target.dx, target.dy, target.dz);
        else AtlasObjGeometry.appendPart(quads, mesh, "Mirror", sprite, false, 0, 0, 1, .5, 0, .5);
        return Collections.unmodifiableList(quads);
    }
}
