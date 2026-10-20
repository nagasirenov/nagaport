package com.hbm.render.item;

import com.hbm.render.util.NativeIconExtrusion;
import org.lwjgl.opengl.GL11;

import com.hbm.render.util.RenderItemStack;


import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;

import javax.vecmath.Matrix4f;
import java.util.List;
import com.hbm.render.item.nativeitem.ItemTransformMatrix;
import com.hbm.render.item.nativeitem.context.*;
import com.hbm.render.item.nativeitem.direct.PlainItemPerspectives;
import com.hbm.render.util.NativeGuiItemDraw;
import com.hbm.render.util.NativeMachineItemRenderer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

public class ItemRenderTransformer extends ContextItemRenderer implements NativeSelectiveItemRenderer {

	double[] rtp;
	double[] ttp;
	double[] stp;
	double[] rfp;
	double[] tfp;
	double[] sfp;
	double[] rir;
	double[] tir;
	double[] sir;
	
	public ItemRenderTransformer(double[] rtp, double[] ttp, double[] stp, double[] rfp, double[] tfp, double[] sfp, double[] rir, double[] tir, double[] sir) {
		this.rtp = rtp.clone();
		this.ttp = ttp.clone();
		this.stp = stp.clone();
		this.rfp = rfp.clone();
		this.tfp = tfp.clone();
		this.sfp = sfp.clone();
		this.rir = rir.clone();
		this.tir = tir.clone();
		this.sir = sir.clone();
	}

    @Override public boolean handlesPerspective(ItemStack stack, TransformType type) {
        return type == TransformType.GUI || type == TransformType.FIRST_PERSON_RIGHT_HAND || type == TransformType.FIRST_PERSON_LEFT_HAND
                || type == TransformType.THIRD_PERSON_RIGHT_HAND || type == TransformType.THIRD_PERSON_LEFT_HAND;
    }
    @Override public Matrix4f localTransform(ItemStack stack, NativeItemContext context) {
        ItemTransformMatrix m = new ItemTransformMatrix();
        switch(context.transform) {
        case FIRST_PERSON_LEFT_HAND:
        case FIRST_PERSON_RIGHT_HAND:
            // Original tfp was unused; preserve that rather than invent a translation.
            return m.rotateY(rfp[1]).rotateZ(rfp[2]).rotateX(rfp[0]).rotateY(180).rotateZ(-90)
                    .translate(.5,.5,0).scale(sfp[0]*2,sfp[1]*2,sfp[2]*2).translate(-.5,-.5,.25).build();
        case THIRD_PERSON_LEFT_HAND:
        case THIRD_PERSON_RIGHT_HAND:
            return m.rotateY(rtp[1]).rotateZ(rtp[2]).rotateX(rtp[0]).translate(ttp[0],ttp[1],ttp[2])
                    .rotateZ(45).rotateY(180).rotateZ(-45).scale(stp[0],stp[1],stp[2]).build();
        case GUI:
            return m.rotateX(rir[0]).rotateY(rir[1]).rotateZ(rir[2]).translate(tir[0]*.0625,tir[1]*.0625,tir[2]*.0625)
                    .translate(8,8,0).scale(sir[0],sir[1],sir[2]).translate(-8,-8,0).build();
        default: return m.build();
        }
    }
    @Override public Matrix4f composePerspective(ItemStack stack, NativeItemContext context) {
        TransformType type = context.transform;
        if((type == TransformType.THIRD_PERSON_RIGHT_HAND || type == TransformType.THIRD_PERSON_LEFT_HAND) && !stack.isEmpty() && stack.getItem().isFull3D()) {
            Matrix4f inverse = new ItemTransformMatrix().rotateX(-90).rotateY(180).translate(.0625,.125,-.625).build();
            inverse.invert();
            ItemTransformMatrix m = new ItemTransformMatrix().multiply(inverse).translate(-.0625,.4375,.0625);
            if(stack.getItem().shouldRotateAroundWhenRendering()) m.rotateZ(180).translate(0,-.125,0);
            if(context.holder != null && context.holder.isHandActive() && context.holder.getActiveItemStack() == stack
                    && stack.getItemUseAction() == net.minecraft.item.EnumAction.BLOCK)
                m.translate(.05F,0,-.1F).rotateY(-50).rotateX(-10).rotateZ(-60);
            return m.translate(0,.1875,0).scale(.625,-.625,.625).rotateX(-100).rotateY(45)
                    .translate(0,-.3F,0).scale(1.5).rotateY(50).rotateZ(335).translate(-.9375,-.0625,0)
                    .multiply(localTransform(stack, context)).translate(.5,.5,.5).build();
        }
        return PlainItemPerspectives.compose(type, localTransform(stack, context));
    }
    @Override protected void draw(ItemStack item, NativeItemContext context) {
        if(context.transform != TransformType.GUI) {
            List<BakedQuad> layers = NativeMachineItemRenderer.iconLayers(NativeGuiItemDraw.baseModel(Minecraft.getMinecraft().getRenderItem(), item));
            if(layers == null || layers.isEmpty()) {
                // Retain arbitrary native geometry; never flatten its particle sprite.
                net.minecraft.client.renderer.block.model.IBakedModel base = NativeGuiItemDraw.baseModel(Minecraft.getMinecraft().getRenderItem(), item);
                Minecraft.getMinecraft().getRenderItem().renderItem(item, base);
                return;
            }
            TextureAtlasSprite icon = layers.get(0).getSprite();
            Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
            NativeIconExtrusion.draw(icon.getMaxU(), icon.getMinV(), icon.getMinU(), icon.getMaxV(), icon.getIconWidth(), icon.getIconHeight(), .0625F);
            if(item.hasEffect()) renderGlint3D(Tessellator.getInstance(), .0625F);
        } else {
            RenderItemStack.renderItemStackNoEffect(0, 0, 0, item);
            if(item.hasEffect()) renderGlintFlat();
        }
    }

	public void renderGlintFlat() {

		Minecraft mc = Minecraft.getMinecraft();
		mc.renderEngine.bindTexture(new net.minecraft.util.ResourceLocation("hbm", "textures/misc/glint.png"));

		GL11.glDepthFunc(GL11.GL_EQUAL);
		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glDepthMask(false);
		GL11.glEnable(GL11.GL_ALPHA_TEST);
		GL11.glEnable(GL11.GL_BLEND);

		for(int j1 = 0; j1 < 2; ++j1) {
			OpenGlHelper.glBlendFunc(772, 1, 0, 0);
			float scaleU = 0.00390625F;
			float scaleV = 0.00390625F;
			float anim = (float) (Minecraft.getSystemTime() % (long) (3000 + j1 * 1873)) / (3000.0F + (float) (j1 * 1873)) * 256.0F;
			float offsetV = 0.0F;
			Tessellator tessellator = Tessellator.getInstance();
			float sizeMultU = 4.0F;

			if(j1 == 1) {
				sizeMultU = -1.0F;
			}

			float in = 0.76F;
			GL11.glColor4f(0.5F * in, 0.25F * in, 0.8F * in, 1.0F);

			int x = 0;
			int sizeX = 16;
			int y = 0;
			int sizeY = 16;
			int zLevel = 0;

			BufferBuilder buffer = tessellator.getBuffer();
            buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
			buffer.pos(x + 0,		y + sizeY,	zLevel).tex(	(anim + sizeY * sizeMultU) * scaleU,			(offsetV + sizeY) * scaleV).endVertex();
			buffer.pos(x + sizeX,	y + sizeY,	zLevel).tex(	(anim + sizeX + sizeY * sizeMultU) * scaleU,	(offsetV + sizeY) * scaleV).endVertex();
			buffer.pos(x + sizeX,	y + 0,		zLevel).tex(	(anim + sizeX) * scaleU,				(offsetV + 0.0F) * scaleV).endVertex();
			buffer.pos(x + 0,		y + 0,		zLevel).tex(	(anim + 0.0F) * scaleU,					(offsetV + 0.0F) * scaleV).endVertex();
			tessellator.draw();
		}

		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		GL11.glDepthMask(true);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glDisable(GL11.GL_ALPHA_TEST);
		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glDepthFunc(GL11.GL_LEQUAL);
	}
	
	public void renderGlint3D(Tessellator tessellator, float depth) {

		Minecraft mc = Minecraft.getMinecraft();
		mc.renderEngine.bindTexture(new net.minecraft.util.ResourceLocation("hbm", "textures/misc/glint.png"));
		
		GL11.glDepthFunc(GL11.GL_EQUAL);
		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_COLOR, GL11.GL_ONE);

		float in = 0.76F;
		GL11.glColor4f(0.5F * in, 0.25F * in, 0.8F * in, 1.0F);
		
		GL11.glMatrixMode(GL11.GL_TEXTURE);
		
		GL11.glPushMatrix();
		float scale = 0.125F;
		GL11.glScalef(scale, scale, scale);
		float offset = (float) (Minecraft.getSystemTime() % 3000L) / 3000.0F * 8.0F;
		GL11.glTranslatef(offset, 0.0F, 0.0F);
		GL11.glRotatef(-50.0F, 0.0F, 0.0F, 1.0F);
		NativeIconExtrusion.draw(0.0F, 0.0F, 1.0F, 1.0F, 255, 255, depth);
		GL11.glPopMatrix();
		GL11.glPushMatrix();
		GL11.glScalef(scale, scale, scale);
		offset = (float) (Minecraft.getSystemTime() % 4873L) / 4873.0F * 8.0F;
		GL11.glTranslatef(-offset, 0.0F, 0.0F);
		GL11.glRotatef(10.0F, 0.0F, 0.0F, 1.0F);
		NativeIconExtrusion.draw(0.0F, 0.0F, 1.0F, 1.0F, 255, 255, depth);
		GL11.glPopMatrix();
		
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glDepthFunc(GL11.GL_LEQUAL);
	}
}
