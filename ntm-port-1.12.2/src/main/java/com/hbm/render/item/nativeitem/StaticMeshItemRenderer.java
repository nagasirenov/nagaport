package com.hbm.render.item.nativeitem;

import java.util.Objects;
import org.lwjgl.opengl.GL11;
import com.hbm.render.mesh.MeshModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

/** Complete real mesh draw. Geometric transforms are owned exclusively by the baked model.
 * No fake tile entity, vanilla item redraw, glint, overlays or legacy renderer interface.
 */
public class StaticMeshItemRenderer extends TileEntityItemStackRenderer {
    private final MeshModel mesh;
    private final ResourceLocation texture;
    private final String[] parts;
    public StaticMeshItemRenderer(MeshModel mesh, ResourceLocation texture, String... parts) {
        this.mesh = Objects.requireNonNull(mesh, "mesh");
        this.texture = Objects.requireNonNull(texture, "texture");
        if(parts.length == 0) throw new IllegalArgumentException("Select the original mesh parts explicitly");
        this.parts = parts.clone();
    }
    @Override public void renderByItem(ItemStack stack) {
        // Deliberately do not use the float overload as a frame clock: Forge 2768 supplies 1.0F.
        int boundTexture = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
        GL11.glPushMatrix();
        // Only raw-GL capabilities changed below are covered. Texture restoration uses
        // GlStateManager too so its binding cache and actual GL state agree.
        GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_LIGHTING_BIT);
        try {
            GL11.glEnable(GL11.GL_CULL_FACE);
            GL11.glEnable(GL11.GL_ALPHA_TEST);
            GL11.glShadeModel(GL11.GL_SMOOTH);
            Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
            mesh.renderOnly(parts);
        } finally {
            GlStateManager.bindTexture(boundTexture);
            GL11.glPopAttrib();
            GL11.glPopMatrix();
        }
    }
    @Override public void renderByItem(ItemStack stack, float partialTicks) { renderByItem(stack); }
}
