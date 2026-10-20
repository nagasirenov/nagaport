package com.hbm.render.tileentity;

import org.lwjgl.opengl.GL11;

import com.hbm.lib.RefStrings;
import com.hbm.render.loader.HFRWavefrontObject;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import com.hbm.render.mesh.MeshModel;

public class RenderDemonLamp extends TileEntitySpecialRenderer {
	
	public static final MeshModel demon_lamp = new HFRWavefrontObject(new ResourceLocation(RefStrings.MODID, "models/blocks/demon_lamp.obj"));
	public static final ResourceLocation tex = new ResourceLocation(RefStrings.MODID, "textures/models/machines/demon_lamp.png");

	@Override
	public void render(TileEntity tileEntity, double x, double y, double z, float f, int nativeDestroyStage, float nativeAlpha) {

		GL11.glPushMatrix();
		GL11.glTranslated(x + 0.5D, y + 0.5D, z + 0.5D);
		
		switch(tileEntity.getBlockMetadata()) {
		case 0: GL11.glRotated(180, 1, 0, 0); break;
		case 1: break;
		case 2: GL11.glRotated(90, 1, 0, 0); GL11.glRotated(180, 0, 0, 1); break;
		case 3: GL11.glRotated(90, 1, 0, 0); break;
		case 4: GL11.glRotated(90, 1, 0, 0); GL11.glRotated(90, 0, 0, 1); break;
		case 5: GL11.glRotated(90, 1, 0, 0); GL11.glRotated(270, 0, 0, 1); break;
		}

		GL11.glTranslated(0, -0.5F, 0);
		
		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glEnable(GL11.GL_CULL_FACE);

		GL11.glShadeModel(GL11.GL_SMOOTH);
		bindTexture(tex);
		demon_lamp.renderAll();
		
		Tessellator tess = Tessellator.getInstance();
		BufferBuilder tessBuffer = tess.getBuffer();
		tessBuffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
		Vec3d vec = new Vec3d(1, 0, 0);

		GL11.glDepthMask(false);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glDisable(GL11.GL_CULL_FACE);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
		GL11.glAlphaFunc(GL11.GL_GREATER, 0.0F);

		double near = 0.375D;
		double far = 15D;
		//whereeeeeeever you are
		
		for(int j = 0; j < 2; j++) {
			
			double h = 0.5;
			double height = j == 0 ? -h : h;
			
			for(int i = 0; i < 16; i++) {
				
				int tessColor0Red = Math.max(0, Math.min(255, (int) ((0F) * 255.0F)));
				int tessColor0Green = Math.max(0, Math.min(255, (int) ((0.75F) * 255.0F)));
				int tessColor0Blue = Math.max(0, Math.min(255, (int) ((1F) * 255.0F)));
				int tessColor0Alpha = Math.max(0, Math.min(255, (int) ((0.25F) * 255.0F)));
				tessBuffer.pos(vec.x * near, 0.5D + j * 0.125D, vec.z * near).color(tessColor0Red, tessColor0Green, tessColor0Blue, tessColor0Alpha).endVertex();
				int tessColor1Red = Math.max(0, Math.min(255, (int) ((0F) * 255.0F)));
				int tessColor1Green = Math.max(0, Math.min(255, (int) ((0.75F) * 255.0F)));
				int tessColor1Blue = Math.max(0, Math.min(255, (int) ((1F) * 255.0F)));
				int tessColor1Alpha = Math.max(0, Math.min(255, (int) ((0F) * 255.0F)));
				tessBuffer.pos(vec.x * far, 0.5D + j * 0.125D + height, vec.z * far).color(tessColor1Red, tessColor1Green, tessColor1Blue, tessColor1Alpha).endVertex();
				
				vec = vec.rotateYaw((float)(Math.PI * 2D / 16D));
	
				tessBuffer.pos(vec.x * far, 0.5D + j * 0.125D + height, vec.z * far).color(tessColor1Red, tessColor1Green, tessColor1Blue, tessColor1Alpha).endVertex();
				int tessColor2Red = Math.max(0, Math.min(255, (int) ((0F) * 255.0F)));
				int tessColor2Green = Math.max(0, Math.min(255, (int) ((0.75F) * 255.0F)));
				int tessColor2Blue = Math.max(0, Math.min(255, (int) ((1F) * 255.0F)));
				int tessColor2Alpha = Math.max(0, Math.min(255, (int) ((0.25F) * 255.0F)));
				tessBuffer.pos(vec.x * near, 0.5D + j * 0.125D, vec.z * near).color(tessColor2Red, tessColor2Green, tessColor2Blue, tessColor2Alpha).endVertex();
			}
		}
		tess.draw();

		GL11.glAlphaFunc(GL11.GL_GREATER, 0.1F);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glShadeModel(GL11.GL_FLAT);
		GL11.glDepthMask(true);

		GL11.glPopMatrix();
	}
}
