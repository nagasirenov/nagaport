package com.hbm.render.item.nativeitem.context;

import javax.vecmath.Matrix4f;
import com.hbm.render.item.nativeitem.ItemTransformMatrix;
import com.hbm.render.item.nativeitem.staticfamily.SingleMeshEntityItemRenderer.BlockItemDraw;
import com.hbm.render.util.NativeMachineItemRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;

/** Exact-owned native callback family. Local original transforms remain in drawModel;
 * the perspective contains only caller compensation, never a legacy Forge protocol. */
public abstract class NativeOriginalItemRenderer extends ContextItemRenderer implements NativeSelectiveItemRenderer,
        BlockItemDraw, NativeMachineItemRenderer.FrameItemModel {
    private final boolean gui, bobAndSpin;
    protected NativeOriginalItemRenderer(boolean gui, boolean bobAndSpin) { this.gui = gui; this.bobAndSpin = bobAndSpin; }
    @Override public boolean handlesPerspective(ItemStack stack, TransformType type) { return type != TransformType.GUI || gui; }
    @Override public Matrix4f localTransform(ItemStack stack, NativeItemContext context) { return new ItemTransformMatrix().build(); }
    @Override public Matrix4f composePerspective(ItemStack stack, NativeItemContext context) {
        if(context.transform == TransformType.FIXED)
            return new ItemTransformMatrix().scale(2).translate(0,bobAndSpin ? -.06 : -.16,0).scale(.5).rotateY(180)
                    .multiply(localTransform(stack,context)).translate(.5,.5,.5).build();
        return NativeToolItemPerspectives.compose(stack, context, localTransform(stack, context));
    }
    protected long animationTimeMillis() { return System.currentTimeMillis(); }
    protected abstract void drawModel(ItemStack stack, NativeItemContext context);
    @Override protected final void draw(ItemStack stack, NativeItemContext context) {
        int mode = GL11.glGetInteger(GL11.GL_MATRIX_MODE);
        int depth = GL11.glGetInteger(GL11.GL_MODELVIEW_STACK_DEPTH), attributes = GL11.glGetInteger(GL11.GL_ATTRIB_STACK_DEPTH);
        float lightX = OpenGlHelper.lastBrightnessX, lightY = OpenGlHelper.lastBrightnessY;
        try { drawModel(stack, context); }
        finally {
            while(GL11.glGetInteger(GL11.GL_ATTRIB_STACK_DEPTH) > attributes) GL11.glPopAttrib();
            GL11.glMatrixMode(GL11.GL_MODELVIEW);
            while(GL11.glGetInteger(GL11.GL_MODELVIEW_STACK_DEPTH) > depth) GL11.glPopMatrix();
            GL11.glMatrixMode(mode);
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, lightX, lightY);
        }
    }
    @Override public boolean handlesDropped(ItemStack stack) { return handlesPerspective(stack, TransformType.GROUND); }
    @Override public void renderDropped(EntityItem entity, double x, double y, double z, float yaw,
            float partialTicks, boolean outlines, int outlineColor) {
        ItemStack stack = entity.getItem();
        if(stack.isEmpty() || !handlesPerspective(stack, TransformType.GROUND)) return;
        boolean rescale = GL11.glIsEnabled(org.lwjgl.opengl.GL12.GL_RESCALE_NORMAL);
        GL11.glPushMatrix();
        try {
            GlStateManager.enableRescaleNormal();
            double bob = bobAndSpin ? net.minecraft.util.math.MathHelper.sin(((float)entity.getAge()+partialTicks)/10F+entity.hoverStart)*.1F+.1F : 0;
            GL11.glTranslated(x,y+bob,z);
            if(bobAndSpin) GL11.glRotatef((((float)entity.getAge()+partialTicks)/20F+entity.hoverStart)*(180F/(float)Math.PI),0,1,0);
            GL11.glScaled(.5,.5,.5);
            if(outlines) { GlStateManager.enableColorMaterial(); GlStateManager.enableOutlineMode(outlineColor); }
            render(stack,new NativeItemContext(TransformType.GROUND,null));
        } finally {
            if(outlines) { GlStateManager.disableOutlineMode(); GlStateManager.disableColorMaterial(); }
            if(rescale) GlStateManager.enableRescaleNormal(); else GlStateManager.disableRescaleNormal();
            GL11.glPopMatrix();
        }
    }
    @Override public void renderFrame(EntityItem entity,double x,double y,double z,float yaw,float partialTicks,NativeMachineItemRenderer.Display display) {
        renderDropped(entity,x,y,z,yaw,partialTicks,false,0);
    }
}
