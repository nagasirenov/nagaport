package com.hbm.render.entity.item;

import org.lwjgl.opengl.GL11;
import net.minecraft.client.renderer.GlStateManager;

import com.hbm.entity.item.EntityTNTPrimedBase;

import com.hbm.render.entity.NativeBlockEntityDraw;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.ResourceLocation;

public class RenderTNTPrimedBase extends Render<EntityTNTPrimedBase> {

	public RenderTNTPrimedBase(RenderManager manager) {
		super(manager);
		this.shadowSize = 0.5F;
	}

	@Override
	public void doRender(EntityTNTPrimedBase tnt, double x, double y, double z, float f0, float f1) {
		GlStateManager.pushMatrix();
		GlStateManager.translate((float) x, (float) y, (float) z);
		GlStateManager.rotate(-90F, 0F, 1F, 0F);
		GlStateManager.rotate(tnt.rotationYaw, 0, 1, 0);
		GlStateManager.rotate(tnt.rotationPitch, 0, 0, 1);

		float f2;

		if((float) tnt.fuse - f1 + 1.0F < 10.0F) {
			f2 = 1.0F - ((float) tnt.fuse - f1 + 1.0F) / 10.0F;

			if(f2 < 0.0F) {
				f2 = 0.0F;
			}

			if(f2 > 1.0F) {
				f2 = 1.0F;
			}

			f2 *= f2;
			f2 *= f2;
			float scale = 1.0F + f2 * 0.3F;
			GlStateManager.scale(scale, scale, scale);
		}

		f2 = (1.0F - ((float) tnt.fuse - f1 + 1.0F) / 100.0F) * 0.8F;
		this.bindEntityTexture(tnt);
		NativeBlockEntityDraw.render(tnt.getBlock(), 0, tnt.getBrightness(), true);

		if(tnt.fuse / 5 % 2 == 0) {

			GlStateManager.scale(1.01, 1.01, 1.01);
			GlStateManager.disableTexture2D();
			GlStateManager.disableLighting();
			GlStateManager.enableBlend();
			GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_DST_ALPHA);
			GlStateManager.color(1.0F, 1.0F, 1.0F, f2);
			NativeBlockEntityDraw.render(tnt.getBlock(), 0, 1.0F, false);
			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
			GlStateManager.disableBlend();
			GlStateManager.enableLighting();
			GlStateManager.enableTexture2D();
		}

		GlStateManager.popMatrix();
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityTNTPrimedBase tnt) {
		return TextureMap.LOCATION_BLOCKS_TEXTURE;
	}

}
