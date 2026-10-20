package com.hbm.render.tileentity;

import org.lwjgl.opengl.GL11;

import com.hbm.blocks.BlockDummyable;
import com.hbm.main.ResourceManager;
import com.hbm.tileentity.machine.oil.TileEntityMachinePumpjack;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.Vec3d;

public class RenderPumpjack extends TileEntitySpecialRenderer {

	@Override
	public void render(TileEntity tileEntity, double x, double y, double z, float f, int nativeDestroyStage, float nativeAlpha) {
		
		GL11.glPushMatrix();
		GL11.glTranslated(x + 0.5, y, z + 0.5);
		GL11.glEnable(GL11.GL_LIGHTING);
		
		switch(tileEntity.getBlockMetadata() - BlockDummyable.offset) {
		case 2: GL11.glRotatef(90, 0F, 1F, 0F); break;
		case 4: GL11.glRotatef(180, 0F, 1F, 0F); break;
		case 3: GL11.glRotatef(270, 0F, 1F, 0F); break;
		case 5: GL11.glRotatef(0, 0F, 1F, 0F); break;
		}
		
		TileEntityMachinePumpjack pj = (TileEntityMachinePumpjack) tileEntity;
		
		float rotation = pj.prevRot + (pj.rot - pj.prevRot) * f;

		GL11.glShadeModel(GL11.GL_SMOOTH);
		
		bindTexture(ResourceManager.pumpjack_tex);
		ResourceManager.pumpjack.renderPart("Base");

		GL11.glPushMatrix();
		GL11.glTranslated(0, 1.5, -5.5);
		GL11.glRotatef(rotation - 90, 1, 0, 0);
		GL11.glTranslated(0, -1.5, 5.5);
		ResourceManager.pumpjack.renderPart("Rotor");
		GL11.glPopMatrix();

		GL11.glPushMatrix();
		GL11.glTranslated(0, 3.5, -3.5);
		GL11.glRotated(Math.toDegrees(Math.sin(Math.toRadians(rotation))) * 0.25, 1, 0, 0);
		GL11.glTranslated(0, -3.5, 3.5);
		ResourceManager.pumpjack.renderPart("Head");
		GL11.glPopMatrix();

		GL11.glPushMatrix();
		GL11.glTranslated(0, -Math.sin(Math.toRadians(rotation)), 0);
		ResourceManager.pumpjack.renderPart("Carriage");
		GL11.glPopMatrix();
		
		com.hbm.util.Vec3NT backPos = new com.hbm.util.Vec3NT(0, 0, -2);
		backPos.rotateAroundXRad(-(float)Math.sin(Math.toRadians(rotation)) * 0.25F);
		
		com.hbm.util.Vec3NT rot = new com.hbm.util.Vec3NT(0, 0.5, 0);
		rot.rotateAroundXRad(-(float)Math.toRadians(rotation - 90));

		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glDisable(GL11.GL_CULL_FACE);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		
		Tessellator tess = Tessellator.getInstance();
		BufferBuilder tessBuffer = tess.getBuffer();
		tessBuffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
		int tessColor0Red = Math.max(0, Math.min(255, (int) ((0.5F) * 255.0F)));
		int tessColor0Green = Math.max(0, Math.min(255, (int) ((0.5F) * 255.0F)));
		int tessColor0Blue = Math.max(0, Math.min(255, (int) ((0.5F) * 255.0F)));
		int tessColor0Alpha = Math.max(0, Math.min(255, (int) ((1F) * 255.0F)));
		
		for(int i = -1; i <= 1; i += 2) {

			tessBuffer.pos(0.53125 * i, 1.5 + rot.y, -5.5 + rot.z - 0.0625D).color(tessColor0Red, tessColor0Green, tessColor0Blue, tessColor0Alpha).endVertex();
			tessBuffer.pos(0.53125 * i, 1.5 + rot.y, -5.5 + rot.z + 0.0625D).color(tessColor0Red, tessColor0Green, tessColor0Blue, tessColor0Alpha).endVertex();

			tessBuffer.pos(0.53125 * i, 3.5 + backPos.y, -3.5 + backPos.z + 0.0625D).color(tessColor0Red, tessColor0Green, tessColor0Blue, tessColor0Alpha).endVertex();
			tessBuffer.pos(0.53125 * i, 3.5 + backPos.y, -3.5 + backPos.z - 0.0625D).color(tessColor0Red, tessColor0Green, tessColor0Blue, tessColor0Alpha).endVertex();
		}
		
		int tessColor1Red = Math.max(0, Math.min(255, (int) ((0.2F) * 255.0F)));
		int tessColor1Green = Math.max(0, Math.min(255, (int) ((0.2F) * 255.0F)));
		int tessColor1Blue = Math.max(0, Math.min(255, (int) ((0.2F) * 255.0F)));
		int tessColor1Alpha = Math.max(0, Math.min(255, (int) ((1F) * 255.0F)));
		
		double pd = 0.03125D;
		double width = 0.25D;

		double height = -Math.sin(Math.toRadians(rotation));
		
		for(int i = -1; i <= 1; i += 2) {

			float pRot = -(float)(Math.sin(Math.toRadians(rotation)) * 0.25);
			
			com.hbm.util.Vec3NT frontPos = new com.hbm.util.Vec3NT(0, 0, 1);
			frontPos.rotateAroundXRad(pRot);

			double dist = 0.03125D;
			com.hbm.util.Vec3NT frontRad = new com.hbm.util.Vec3NT(0, 0, 2.5 + dist);
			double cutlet = 360D / 32D;
			frontRad.rotateAroundXRad(pRot);
			frontRad.rotateAroundXRad(-(float)Math.toRadians(cutlet * -3));
			
			for(int j = 0; j < 4; j++) {

				double sumY = frontPos.y + frontRad.y;
				double sumZ = frontPos.z + frontRad.z;
				if(frontRad.y < 0) sumZ = 3.5 + dist * 0.5;
	
				tessBuffer.pos((width - pd) * i, 3.5 + sumY, -3.5 + sumZ).color(tessColor1Red, tessColor1Green, tessColor1Blue, tessColor1Alpha).endVertex();
				tessBuffer.pos((width + pd) * i, 3.5 + sumY, -3.5 + sumZ).color(tessColor1Red, tessColor1Green, tessColor1Blue, tessColor1Alpha).endVertex();

				frontRad.rotateAroundXRad(-(float)Math.toRadians(cutlet));

				sumY = frontPos.y + frontRad.y;
				sumZ = frontPos.z + frontRad.z;
				if(frontRad.y < 0) sumZ = 3.5 + dist * 0.5;
	
				tessBuffer.pos((width + pd) * i, 3.5 + sumY, -3.5 + sumZ).color(tessColor1Red, tessColor1Green, tessColor1Blue, tessColor1Alpha).endVertex();
				tessBuffer.pos((width - pd) * i, 3.5 + sumY, -3.5 + sumZ).color(tessColor1Red, tessColor1Green, tessColor1Blue, tessColor1Alpha).endVertex();
			}

			double sumY = frontPos.y + frontRad.y;
			double sumZ = frontPos.z + frontRad.z;
			if(frontRad.y < 0) sumZ = 3.5 + dist * 0.5;
			
			tessBuffer.pos((width + pd) * i, 3.5 + sumY, -3.5 + sumZ).color(tessColor1Red, tessColor1Green, tessColor1Blue, tessColor1Alpha).endVertex();
			tessBuffer.pos((width - pd) * i, 3.5 + sumY, -3.5 + sumZ).color(tessColor1Red, tessColor1Green, tessColor1Blue, tessColor1Alpha).endVertex();
			
			tessBuffer.pos((width - pd) * i, 2 + height, 0).color(tessColor1Red, tessColor1Green, tessColor1Blue, tessColor1Alpha).endVertex();
			tessBuffer.pos((width + pd) * i, 2 + height, 0).color(tessColor1Red, tessColor1Green, tessColor1Blue, tessColor1Alpha).endVertex();
		}
		
		double p = 0.03125D;
		tessBuffer.pos(p, height + 1.5, p).color(tessColor1Red, tessColor1Green, tessColor1Blue, tessColor1Alpha).endVertex();
		tessBuffer.pos(-p, height + 1.5, -p).color(tessColor1Red, tessColor1Green, tessColor1Blue, tessColor1Alpha).endVertex();
		tessBuffer.pos(-p, 0.75, -p).color(tessColor1Red, tessColor1Green, tessColor1Blue, tessColor1Alpha).endVertex();
		tessBuffer.pos(p, 0.75, p).color(tessColor1Red, tessColor1Green, tessColor1Blue, tessColor1Alpha).endVertex();
		tessBuffer.pos(-p, height + 1.5, p).color(tessColor1Red, tessColor1Green, tessColor1Blue, tessColor1Alpha).endVertex();
		tessBuffer.pos(p, height + 1.5, -p).color(tessColor1Red, tessColor1Green, tessColor1Blue, tessColor1Alpha).endVertex();
		tessBuffer.pos(p, 0.75, -p).color(tessColor1Red, tessColor1Green, tessColor1Blue, tessColor1Alpha).endVertex();
		tessBuffer.pos(-p, 0.75, p).color(tessColor1Red, tessColor1Green, tessColor1Blue, tessColor1Alpha).endVertex();
		
		tess.draw();
		
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glEnable(GL11.GL_CULL_FACE);
		
		GL11.glShadeModel(GL11.GL_FLAT);
		GL11.glPopMatrix();
	}
}
