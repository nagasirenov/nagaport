package com.hbm.render.entity.item;

import org.lwjgl.opengl.GL11;

import com.hbm.blocks.ModBlocks;
import com.hbm.render.util.NativeMachineItemRenderer;

import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class RenderMovingPackage extends Render<Entity> {

	private final NativeMachineItemRenderer items = new NativeMachineItemRenderer();

	public RenderMovingPackage(RenderManager renderManager) {
		super(renderManager);
	}

	private ItemStack dummy;

	@Override
	public void doRender(Entity entity, double x, double y, double z, float f1, float f2) {

		GL11.glPushMatrix();
		try {
			GL11.glTranslated(x, y - 0.0125, z);

			if(this.dummy == null) {
				this.dummy = new ItemStack(ModBlocks.crate);
			}

			EntityItem dummy = new EntityItem(entity.world, 0, 0, 0, this.dummy);
			dummy.hoverStart = 0.0F;

			double scale = 2;
			GL11.glScaled(scale, scale, scale);
			items.renderFrame(dummy, 0, 0, 0, 0, 0,
					NativeMachineItemRenderer.Display.ENTITY_FRAME);
		} finally {
			GL11.glPopMatrix();
		}
	}

	@Override
	protected ResourceLocation getEntityTexture(Entity entity) {
		return null;
	}
}
