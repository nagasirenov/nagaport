package com.hbm.render.item;

import javax.vecmath.Matrix4f;
import org.lwjgl.opengl.GL11;
import com.hbm.items.special.ItemHot;
import com.hbm.render.item.nativeitem.ItemTransformMatrix;
import com.hbm.render.item.nativeitem.context.*;
import com.hbm.render.item.nativeitem.direct.PlainItemPerspectives;
import com.hbm.render.util.NativeGuiItemDraw;
import com.hbm.render.util.RenderItemStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

/** The original heat PNG is a GUI-only alpha overlay; other contexts keep their complete base model. */
public class ItemRendererHot extends ContextItemRenderer implements NativeSelectiveItemRenderer {
    private final ResourceLocation overlay;
    public ItemRendererHot(String originalTexture) { overlay = new ResourceLocation("hbm","textures/items/"+originalTexture+"_hot.png"); }
    @Override public boolean handlesPerspective(ItemStack stack,TransformType type) { return type == TransformType.GUI; }
    @Override public Matrix4f localTransform(ItemStack stack,NativeItemContext context) { return new ItemTransformMatrix().build(); }
    @Override public Matrix4f composePerspective(ItemStack stack,NativeItemContext context) { return PlainItemPerspectives.compose(context.transform,localTransform(stack,context)); }
    @Override protected void draw(ItemStack item,NativeItemContext context) {
        RenderHelper.enableGUIStandardItemLighting();
        NativeGuiItemDraw.withoutEffect(RenderItemStack.renderer(),item,0,0,0);
        double heat=ItemHot.getHeat(item);
        if(heat > 0) {
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glDisable(GL11.GL_LIGHTING);
            OpenGlHelper.glBlendFunc(770,771,1,0);
            GL11.glColor4f(1,1,1,(float)heat);
            Minecraft.getMinecraft().getTextureManager().bindTexture(overlay);
            Tessellator tessellator=Tessellator.getInstance();BufferBuilder buffer=tessellator.getBuffer();
            buffer.begin(GL11.GL_QUADS,DefaultVertexFormats.POSITION_TEX);
            buffer.pos(0,16,0).tex(0,1).endVertex();buffer.pos(16,16,0).tex(1,1).endVertex();
            buffer.pos(16,0,0).tex(1,0).endVertex();buffer.pos(0,0,0).tex(0,0).endVertex();tessellator.draw();
            GL11.glEnable(GL11.GL_LIGHTING);GL11.glDisable(GL11.GL_BLEND);
        }
    }
}
