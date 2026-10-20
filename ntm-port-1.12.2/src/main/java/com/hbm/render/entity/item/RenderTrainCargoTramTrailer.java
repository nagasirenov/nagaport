package com.hbm.render.entity.item;

import org.lwjgl.opengl.GL11;

import com.hbm.blocks.ModBlocks;
import com.hbm.entity.train.EntityRailCarCargo;
import com.hbm.render.util.NativeMachineItemRenderer;
import com.hbm.main.ResourceManager;

import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class RenderTrainCargoTramTrailer extends Render<EntityRailCarCargo> {

	private final NativeMachineItemRenderer items = new NativeMachineItemRenderer();

	public RenderTrainCargoTramTrailer(RenderManager renderManager) {
		super(renderManager);
	}

	@Override
	public void doRender(EntityRailCarCargo entity, double x, double y, double z, float swing, float interp) {
		GL11.glPushMatrix();
		try {
		
		EntityRailCarCargo train = entity;
		double iX = train.prevPosX + (train.posX - train.prevPosX) * interp;
		double iY = train.prevPosY + (train.posY - train.prevPosY) * interp;
		double iZ = train.prevPosZ + (train.posZ - train.prevPosZ) * interp;
		double rX = train.lastRenderX + (train.renderX - train.lastRenderX) * interp;
		double rY = train.lastRenderY + (train.renderY - train.lastRenderY) * interp;
		double rZ = train.lastRenderZ + (train.renderZ - train.lastRenderZ) * interp;
		x -= iX - rX;
		y -= iY - rY;
		z -= iZ - rZ;
		
		GL11.glTranslated(x, y, z);

		float yaw = entity.rotationYaw;
		float prevYaw = entity.prevRotationYaw;

		if(yaw - prevYaw > 180) yaw -= 360;
		if(prevYaw - yaw > 180) prevYaw -= 360;
		
		float yawInterp = prevYaw + (yaw - prevYaw) * interp - 720;

		GL11.glRotated(-yawInterp, 0, 1, 0);

		float pitch = entity.rotationPitch;
		float prevPitch = entity.prevRotationPitch;
		float pitchInterp = prevPitch + (pitch - prevPitch) * interp;
		GL11.glRotated(-pitchInterp, 1, 0, 0);
		
		GlStateManager.disableCull();
		try {
			bindTexture(ResourceManager.tram_trailer);
			ResourceManager.train_cargo_tram_trailer.renderAll();
		} finally {
			GlStateManager.enableCull();
		}
		
		int slots = entity.getSyncedOccupiedSlots();
		
		if(slots > 0) {

			EntityItem dummy = new EntityItem(entity.world, 0, 0, 0, new ItemStack(ModBlocks.crate));
			dummy.hoverStart = 0.0F;

			double scale = 2;
			GL11.glScaled(scale, scale, scale);
			
			if(slots <= 5) {
				items.renderFrame(dummy, 0.0D, 0.375D, 0.0D, 0.0F, 0.0F, NativeMachineItemRenderer.Display.ENTITY_FRAME);
			} else if(slots <= 10) {
				items.renderFrame(dummy, 0.1D, 0.375D, 0.25D, 0.0F, 0.0F, NativeMachineItemRenderer.Display.ENTITY_FRAME);
				items.renderFrame(dummy, -0.1D, 0.375D, -0.25D, 0.0F, 0.0F, NativeMachineItemRenderer.Display.ENTITY_FRAME);
			} else if(slots <= 15) {
				items.renderFrame(dummy, 0.1D, 0.375D, 0.0D, 0.0F, 0.0F, NativeMachineItemRenderer.Display.ENTITY_FRAME);
				items.renderFrame(dummy, -0.1D, 0.375D, 0.375D, 0.0F, 0.0F, NativeMachineItemRenderer.Display.ENTITY_FRAME);
				items.renderFrame(dummy, -0.1D, 0.375D, -0.375D, 0.0F, 0.0F, NativeMachineItemRenderer.Display.ENTITY_FRAME);
			} else if(slots <= 20) {
				items.renderFrame(dummy, 0.2D, 0.375D, 0.3D, 0.0F, 0.0F, NativeMachineItemRenderer.Display.ENTITY_FRAME);
				items.renderFrame(dummy, 0.2D, 0.375D, -0.2D, 0.0F, 0.0F, NativeMachineItemRenderer.Display.ENTITY_FRAME);
				items.renderFrame(dummy, -0.2D, 0.375D, 0.2D, 0.0F, 0.0F, NativeMachineItemRenderer.Display.ENTITY_FRAME);
				items.renderFrame(dummy, -0.2D, 0.375D, -0.3D, 0.0F, 0.0F, NativeMachineItemRenderer.Display.ENTITY_FRAME);
			} else if(slots <= 25) {
				items.renderFrame(dummy, 0.2D, 0.375D, 0.6D, 0.0F, 0.0F, NativeMachineItemRenderer.Display.ENTITY_FRAME);
				items.renderFrame(dummy, 0.2D, 0.375D, 0.0D, 0.0F, 0.0F, NativeMachineItemRenderer.Display.ENTITY_FRAME);
				items.renderFrame(dummy, 0.2D, 0.375D, -0.5D, 0.0F, 0.0F, NativeMachineItemRenderer.Display.ENTITY_FRAME);
				items.renderFrame(dummy, -0.2D, 0.375D, 0.2D, 0.0F, 0.0F, NativeMachineItemRenderer.Display.ENTITY_FRAME);
				items.renderFrame(dummy, -0.2D, 0.375D, -0.3D, 0.0F, 0.0F, NativeMachineItemRenderer.Display.ENTITY_FRAME);
			} else if(slots <= 30) {
				items.renderFrame(dummy, 0.2D, 0.375D, 0.6D, 0.0F, 0.0F, NativeMachineItemRenderer.Display.ENTITY_FRAME);
				items.renderFrame(dummy, 0.2D, 0.375D, 0.0D, 0.0F, 0.0F, NativeMachineItemRenderer.Display.ENTITY_FRAME);
				items.renderFrame(dummy, 0.2D, 0.375D, -0.5D, 0.0F, 0.0F, NativeMachineItemRenderer.Display.ENTITY_FRAME);
				items.renderFrame(dummy, -0.2D, 0.375D, 0.5D, 0.0F, 0.0F, NativeMachineItemRenderer.Display.ENTITY_FRAME);
				items.renderFrame(dummy, -0.2D, 0.375D, -0.1D, 0.0F, 0.0F, NativeMachineItemRenderer.Display.ENTITY_FRAME);
				items.renderFrame(dummy, -0.2D, 0.375D, -0.6D, 0.0F, 0.0F, NativeMachineItemRenderer.Display.ENTITY_FRAME);
			} else if(slots <= 35) {
				items.renderFrame(dummy, 0.2D, 0.375D, 0.4D, 0.0F, 0.0F, NativeMachineItemRenderer.Display.ENTITY_FRAME);
				items.renderFrame(dummy, 0.2D, 0.375D, 0.0D, 0.0F, 0.0F, NativeMachineItemRenderer.Display.ENTITY_FRAME);
				items.renderFrame(dummy, 0.2D, 0.375D, -0.4D, 0.0F, 0.0F, NativeMachineItemRenderer.Display.ENTITY_FRAME);
				items.renderFrame(dummy, -0.2D, 0.375D, 0.3D, 0.0F, 0.0F, NativeMachineItemRenderer.Display.ENTITY_FRAME);
				items.renderFrame(dummy, -0.2D, 0.375D, -0.1D, 0.0F, 0.0F, NativeMachineItemRenderer.Display.ENTITY_FRAME);
				items.renderFrame(dummy, -0.2D, 0.375D, -0.5D, 0.0F, 0.0F, NativeMachineItemRenderer.Display.ENTITY_FRAME);
				items.renderFrame(dummy, 0.0D, 0.6875D, -0.25D, 0.0F, 0.0F, NativeMachineItemRenderer.Display.ENTITY_FRAME);
			} else if(slots <= 40) {
				items.renderFrame(dummy, 0.2D, 0.375D, 0.4D, 0.0F, 0.0F, NativeMachineItemRenderer.Display.ENTITY_FRAME);
				items.renderFrame(dummy, 0.2D, 0.375D, 0.0D, 0.0F, 0.0F, NativeMachineItemRenderer.Display.ENTITY_FRAME);
				items.renderFrame(dummy, 0.2D, 0.375D, -0.4D, 0.0F, 0.0F, NativeMachineItemRenderer.Display.ENTITY_FRAME);
				items.renderFrame(dummy, -0.2D, 0.375D, 0.3D, 0.0F, 0.0F, NativeMachineItemRenderer.Display.ENTITY_FRAME);
				items.renderFrame(dummy, -0.2D, 0.375D, -0.1D, 0.0F, 0.0F, NativeMachineItemRenderer.Display.ENTITY_FRAME);
				items.renderFrame(dummy, -0.2D, 0.375D, -0.5D, 0.0F, 0.0F, NativeMachineItemRenderer.Display.ENTITY_FRAME);
				items.renderFrame(dummy, 0.0D, 0.6875D, -0.25D, 0.0F, 0.0F, NativeMachineItemRenderer.Display.ENTITY_FRAME);
				items.renderFrame(dummy, 0.0D, 0.6875D, 0.15D, 0.0F, 0.0F, NativeMachineItemRenderer.Display.ENTITY_FRAME);
			} else {
				items.renderFrame(dummy, 0.2D, 0.375D, 0.4D, 0.0F, 0.0F, NativeMachineItemRenderer.Display.ENTITY_FRAME);
				items.renderFrame(dummy, 0.2D, 0.375D, 0.0D, 0.0F, 0.0F, NativeMachineItemRenderer.Display.ENTITY_FRAME);
				items.renderFrame(dummy, 0.2D, 0.375D, -0.4D, 0.0F, 0.0F, NativeMachineItemRenderer.Display.ENTITY_FRAME);
				items.renderFrame(dummy, -0.2D, 0.375D, 0.3D, 0.0F, 0.0F, NativeMachineItemRenderer.Display.ENTITY_FRAME);
				items.renderFrame(dummy, -0.2D, 0.375D, -0.1D, 0.0F, 0.0F, NativeMachineItemRenderer.Display.ENTITY_FRAME);
				items.renderFrame(dummy, -0.2D, 0.375D, -0.5D, 0.0F, 0.0F, NativeMachineItemRenderer.Display.ENTITY_FRAME);
				items.renderFrame(dummy, 0.0D, 0.6875D, -0.25D, 0.0F, 0.0F, NativeMachineItemRenderer.Display.ENTITY_FRAME);
				items.renderFrame(dummy, 0.0D, 0.6875D, 0.15D, 0.0F, 0.0F, NativeMachineItemRenderer.Display.ENTITY_FRAME);
				items.renderFrame(dummy, -0.1D, 0.375D, 0.8D, 0.0F, 0.0F, NativeMachineItemRenderer.Display.ENTITY_FRAME);
			}
			
		}
		
		} finally {
			GL11.glPopMatrix();
		}
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityRailCarCargo entity) {
		return ResourceManager.tram_trailer;
	}
}
