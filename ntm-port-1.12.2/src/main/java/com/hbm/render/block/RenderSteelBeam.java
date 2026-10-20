package com.hbm.render.block;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.hbm.render.block.nativeblock.AtlasObjGeometry;
import com.hbm.render.mesh.ObjMesh;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

/** Native whole-OBJ successor for the original steel beam renderer. */
public final class RenderSteelBeam {
    private RenderSteelBeam() { }
    public static List<BakedQuad> bake(ObjMesh mesh, boolean inventory, TextureAtlasSprite sprite) {
        List<BakedQuad> quads = new ArrayList<>();
        // Original world origin is (x+.5,y,z+.5); original item GL translates y by -.5.
        // Native item coordinates add the common block center back before camera transforms.
        AtlasObjGeometry.appendWhole(quads, mesh, sprite, inventory, 0, 0, 1D, .5D, 0D, .5D);
        return Collections.unmodifiableList(quads);
    }
}
