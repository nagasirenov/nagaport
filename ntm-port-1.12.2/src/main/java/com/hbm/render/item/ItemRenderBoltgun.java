package com.hbm.render.item;

import javax.vecmath.Matrix4f;
import org.lwjgl.opengl.GL11;
import com.hbm.main.ResourceManager;
import com.hbm.render.anim.HbmAnimations;
import com.hbm.render.item.nativeitem.ItemTransformMatrix;
import com.hbm.render.item.nativeitem.context.ContextItemRenderer;
import com.hbm.render.item.nativeitem.context.NativeItemContext;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;

/** Context-dependent native mesh: first-person Barrel recoil is not a matrix-only callback. */
public class ItemRenderBoltgun extends ContextItemRenderer {
    @Override public Matrix4f localTransform(ItemStack stack, NativeItemContext context) {
        ItemTransformMatrix matrix = new ItemTransformMatrix();
        switch(context.transform) {
        case FIRST_PERSON_LEFT_HAND:
        case FIRST_PERSON_RIGHT_HAND:
            return matrix.translate(.5, .35, -.25F).rotateZ(15).rotateY(80).scale(.15).build();
        case THIRD_PERSON_LEFT_HAND:
        case THIRD_PERSON_RIGHT_HAND:
            return matrix.scale(.25).rotateY(10).rotateZ(10).rotateX(10).translate(1.5, -.25, 1).build();
        case GUI:
            return matrix.translate(7, 10, 0).rotateY(-90).rotateX(-135).scale(1.75, 1.75, -1.75).build();
        default: return matrix.scale(.1).build();
        }
    }
    @Override protected void draw(ItemStack stack, NativeItemContext context) {
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glShadeModel(GL11.GL_SMOOTH);
        Minecraft.getMinecraft().getTextureManager().bindTexture(ResourceManager.boltgun_tex);
        if(context.isFirstPerson()) {
            GL11.glPushMatrix();
            try {
                double[] animation = HbmAnimations.getRelevantTransformation("RECOIL");
                GL11.glTranslated(0, 0, -animation[0]);
                // Original renderer suppresses the local player's swing, not a remote holder's.
                if(animation[0] != 0) Minecraft.getMinecraft().player.isSwingInProgress = false;
                ResourceManager.boltgun.renderPart("Barrel");
            } finally { GL11.glPopMatrix(); }
        } else if(context.transform == net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType.GUI) {
            GL11.glEnable(GL11.GL_ALPHA_TEST);
            GL11.glEnable(GL11.GL_LIGHTING);
        }
        ResourceManager.boltgun.renderPart("Gun");
        if(!context.isFirstPerson()) ResourceManager.boltgun.renderPart("Barrel");
        GL11.glShadeModel(GL11.GL_FLAT);
    }
}
