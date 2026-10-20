package com.hbm.render.entity.item;

import java.util.Random;

import org.lwjgl.opengl.GL11;

import com.hbm.entity.item.EntityMovingItem;
import com.hbm.render.util.NativeGuiItemDraw;
import com.hbm.render.util.NativeItemBaseModel;
import com.hbm.render.util.NativeMachineItemRenderer;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.ResourceLocation;

public class RenderMovingItem extends Render<EntityMovingItem> {

	private final NativeMachineItemRenderer items = new NativeMachineItemRenderer();

	public RenderMovingItem(RenderManager renderManager) {
		super(renderManager);
	}

	@Override
	public void doRender(EntityMovingItem item, double x, double y, double z, float f1, float f2) {

		GL11.glPushMatrix();
		try {
			GL11.glTranslated(x, y, z);

			Random rand = new Random(item.getEntityId());
			GL11.glTranslated(0, rand.nextDouble() * 0.0625, 0);

			if(!isBlockItem(item, item.getItemStack())) {
				GL11.glRotatef(90F, 1.0F, 0.0F, 0.0F);
				GL11.glTranslated(0.0, -0.1875, 0.0);

				if(!this.renderManager.options.fancyGraphics) {
					GL11.glRotatef(180F, 0.0F, 1.0F, 0.0F);
				}
			}

			if(item.cacheForRender != null) {
				item.cacheForRender.hoverStart = 0.0F;
				// This was the ordinary entity renderer, not RenderDecoItem: retain
				// its stack count, spread and bob while replacing the removed global.
				items.renderFrame(item.cacheForRender, 0, 0, 0, 0, 0,
						NativeMachineItemRenderer.Display.ENTITY_FRAME);
			}
		} finally {
			GL11.glPopMatrix();
		}
	}

	/** The original outer rotation used the block's ordinary inventory shape,
	 * not an IItemRenderer's custom mesh. Native wrappers commonly report gui3d
	 * for every item, so resolve their retained base before classifying a block.
	 */
	private static boolean isBlockItem(EntityMovingItem entity, ItemStack stack) {
		if(stack == null || stack.isEmpty() || !(stack.getItem() instanceof ItemBlock)) return false;
		ItemBlock item = (ItemBlock) stack.getItem();
		IBlockState state = item.getBlock().getStateFromMeta(item.getMetadata(stack.getMetadata()));
		if(state.getRenderType() == EnumBlockRenderType.INVISIBLE) return false;
		RenderItem renderer = Minecraft.getMinecraft().getRenderItem();
		IBakedModel model = renderer.getItemModelWithOverrides(stack, entity.world, null);
		if(model instanceof NativeMachineItemRenderer.FrameBaseModel)
			model = ((NativeMachineItemRenderer.FrameBaseModel) model).machineFrameModel(stack);
		if(model instanceof NativeItemBaseModel) model = NativeGuiItemDraw.baseModel(renderer, stack);
		return model.isGui3d();
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityMovingItem entity) {
		return null;
	}
}
