package com.hbm.render.item;

import com.hbm.render.item.nativeitem.StaticMeshItemRenderer;
import com.hbm.render.mesh.MeshModel;
import net.minecraft.util.ResourceLocation;

/** Native core mesh renderer. All original geometric scales are owned by AngelCorePerspectives. */
public final class ItemRenderAngelCore extends StaticMeshItemRenderer {
    public ItemRenderAngelCore(MeshModel mesh, ResourceLocation texture) {
        super(mesh, texture, "body");
    }
}
