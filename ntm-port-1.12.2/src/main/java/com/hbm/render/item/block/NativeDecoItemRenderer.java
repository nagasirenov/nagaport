package com.hbm.render.item.block;

import javax.vecmath.Matrix4f;
import com.hbm.render.item.nativeitem.ItemTransformMatrix;
import com.hbm.render.item.nativeitem.context.*;
import com.hbm.render.item.nativeitem.direct.PlainItemPerspectives;
import com.hbm.render.item.nativeitem.staticfamily.SingleMeshEntityItemRenderer.BlockItemDraw;
import com.hbm.render.util.NativeMachineItemRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

/** Original Java-model item callbacks: GUI stays ordinary; entity has no bob,
 * spin, spread or count copies. These original non-3D block render types used
 * the ordinary equipped caller, not Forge's EQUIPPED_BLOCK helper. */
public abstract class NativeDecoItemRenderer extends ContextItemRenderer implements NativeSelectiveItemRenderer,
        BlockItemDraw, NativeMachineItemRenderer.FrameItemModel {
    private final ModelBase model;
    private final ResourceLocation texture;
    protected NativeDecoItemRenderer(ModelBase model, String texture) {
        this.model = model;
        this.texture = new ResourceLocation("hbm", "textures/models/" + texture + ".png");
    }
    @Override public boolean handlesPerspective(ItemStack stack, TransformType type) { return type != TransformType.GUI; }
    @Override public Matrix4f composePerspective(ItemStack stack, NativeItemContext context) {
        if(context.transform == TransformType.FIXED)
            // Same approved frame pivot as ordinary meshes, without original bob=.1.
            return new ItemTransformMatrix().scale(2).translate(0, -.16, 0).scale(.5).rotateY(180)
                    .multiply(localTransform(stack, context)).translate(.5, .5, .5).build();
        return PlainItemPerspectives.compose(context.transform, localTransform(stack, context));
    }
    @Override protected void draw(ItemStack stack, NativeItemContext context) {
        Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
        model.render(context.holder, 0, 0, 0, 0, 0, .0625F);
    }
    @Override public void renderDropped(EntityItem entity, double x, double y, double z, float yaw,
            float partialTicks, boolean outlines, int outlineColor) {
        if(entity.getItem().isEmpty()) return;
        GL11.glPushMatrix();
        try {
            GL11.glTranslated(x, y, z);
            GL11.glScaled(.5, .5, .5); // original non-BLOCK_3D Forge entity caller
            Matrix4f m = localTransform(entity.getItem(), new NativeItemContext(TransformType.GROUND, null));
            multMatrix(m);
            if(outlines) { GlStateManager.enableColorMaterial(); GlStateManager.enableOutlineMode(outlineColor); }
            render(entity.getItem(), new NativeItemContext(TransformType.GROUND, null));
        } finally {
            if(outlines) { GlStateManager.disableOutlineMode(); GlStateManager.disableColorMaterial(); }
            GL11.glPopMatrix();
        }
    }
    @Override public void renderFrame(EntityItem entity, double x, double y, double z, float yaw,
            float partialTicks, NativeMachineItemRenderer.Display display) {
        renderDropped(entity, x, y, z, yaw, partialTicks, false, 0);
    }
}
