package com.hbm.render.tileentity;

import com.hbm.render.util.NativeMachineItemRenderer;

import org.lwjgl.opengl.GL11;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.recipes.PrecAssRecipes;
import com.hbm.inventory.recipes.loader.GenericRecipe;
import com.hbm.main.MainRegistry;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.tileentity.machine.TileEntityMachinePrecAss;
import com.hbm.util.BobMathUtil;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;


public class RenderPrecAss extends TileEntitySpecialRenderer<TileEntity> implements IItemRendererProvider {
	private final NativeMachineItemRenderer itemRenderer = new NativeMachineItemRenderer();
	
	public static EntityItem dummy;

	@Override
	public void render(TileEntity tileEntity, double x, double y, double z, float interp, int destroyStage, float alpha) {
		GL11.glPushMatrix();
		GL11.glTranslated(x + 0.5, y, z + 0.5);
		GL11.glRotated(90, 0, 1, 0);
		GL11.glShadeModel(GL11.GL_SMOOTH);

		switch(tileEntity.getBlockMetadata() - BlockDummyable.offset) {
		case 2: GL11.glRotatef(0, 0F, 1F, 0F); break;
		case 4: GL11.glRotatef(90, 0F, 1F, 0F); break;
		case 3: GL11.glRotatef(180, 0F, 1F, 0F); break;
		case 5: GL11.glRotatef(270, 0F, 1F, 0F); break;
		}
		
		TileEntityMachinePrecAss assembler = (TileEntityMachinePrecAss) tileEntity;
		
		bindTexture(ResourceManager.precass_tex);
		ResourceManager.assembly_machine.renderPart("Base");
		if(assembler.frame) ResourceManager.assembly_machine.renderPart("Frame");
		
		GL11.glPushMatrix();

		double spin = BobMathUtil.interp(assembler.prevRing, assembler.ring, interp);
		
		double[] arm = new double[] {
				BobMathUtil.interp(assembler.prevArmAngles[0], assembler.armAngles[0], interp),
				BobMathUtil.interp(assembler.prevArmAngles[1], assembler.armAngles[1], interp),
				BobMathUtil.interp(assembler.prevArmAngles[2], assembler.armAngles[2], interp)
		};
		
		GL11.glRotated(spin, 0, 1, 0);
		ResourceManager.assembly_machine.renderPart("Ring");
		ResourceManager.assembly_machine.renderPart("Ring2");

		for(int i = 0; i < 4; i++) {
			renderArm(arm, BobMathUtil.interp(assembler.prevStrikers[i], assembler.strikers[i], interp));
			GL11.glRotated(-90, 0, 1, 0);
		}
		
		GL11.glPopMatrix();
		
		GL11.glShadeModel(GL11.GL_FLAT);
		
		GenericRecipe recipe = PrecAssRecipes.INSTANCE.recipeNameMap.get(assembler.assemblerModule.recipe);
		if(recipe != null && MainRegistry.proxy.me().getDistanceSq(tileEntity.getPos().getX() + 0.5, tileEntity.getPos().getY() + 1, tileEntity.getPos().getZ() + 0.5) < 35 * 35) {

			GL11.glRotated(90, 0, 1, 0);
			GL11.glTranslated(0, 1.0625, 0);
			
			ItemStack stack = recipe.getIcon();
			stack.setCount(1);

			if(stack.getItem() instanceof ItemBlock) {
				if(NativeMachineItemRenderer.isBlockModel(stack)) {
					GL11.glTranslated(0, -0.0625, 0);
				} else {
					GL11.glTranslated(0, -0.125, 0);
					GL11.glScaled(0.5, 0.5, 0.5);
				}
			} else {
				GL11.glRotated(-90, 1, 0, 0);
				GL11.glTranslated(0, -0.25, 0);
			}
			
			GL11.glScaled(1.25, 1.25, 1.25);

			if(dummy == null || dummy.world != tileEntity.getWorld()) dummy = new EntityItem(tileEntity.getWorld(), 0, 0, 0, stack);
			dummy.setItem(stack);
			dummy.hoverStart = 0.0F;

			itemRenderer.renderFrame(dummy, 0.0D, 0.0D, 0.0D, 0.0F, 0.0F, NativeMachineItemRenderer.Display.ENTITY_FRAME);
		}
		
		GL11.glPopMatrix();
	}
	
	public static void renderArm(double[] arm, double striker) {

		GL11.glPushMatrix(); {
			GL11.glTranslated(0, 1.625, 0.9375);
			GL11.glRotated(arm[0], 1, 0, 0);
			GL11.glTranslated(0, -1.625, -0.9375);
			ResourceManager.assembly_machine.renderPart("ArmLower1");
	
			GL11.glTranslated(0, 2.375, 0.9375);
			GL11.glRotated(arm[1], 1, 0, 0);
			GL11.glTranslated(0, -2.375, -0.9375);
			ResourceManager.assembly_machine.renderPart("ArmUpper1");
	
			GL11.glTranslated(0, 2.375, 0.4375);
			GL11.glRotated(arm[2], 1, 0, 0);
			GL11.glTranslated(0, -2.375, -0.4375);
			ResourceManager.assembly_machine.renderPart("Head1");
			GL11.glTranslated(0, striker, 0);
			ResourceManager.assembly_machine.renderPart("Spike1");
		} GL11.glPopMatrix();
	}

	@Override
	public Item getItemForRenderer() {
		return Item.getItemFromBlock(ModBlocks.machine_precass);
	}

	@Override
	public ItemRenderBase getRenderer() {
		
		return new ItemRenderBase() {
			
			public void renderInventory() {
				GL11.glTranslated(0, -2.75, 0);
				GL11.glScaled(4.5, 4.5, 4.5);
			}
			public void renderCommonWithStack(ItemStack item) {
				GL11.glRotated(90, 0, 1, 0);
				GL11.glScaled(0.75, 0.75, 0.75);
				GL11.glShadeModel(GL11.GL_SMOOTH);
				bindTexture(ResourceManager.precass_tex);
				ResourceManager.assembly_machine.renderPart("Base");
				ResourceManager.assembly_machine.renderPart("Frame");
				ResourceManager.assembly_machine.renderPart("Ring");
				ResourceManager.assembly_machine.renderPart("Ring2");
				double[] arm = new double[] {45, -30, 45};
				for(int i = 0; i < 4; i++) {
					renderArm(arm, 0);
					GL11.glRotated(90, 0, 1, 0);
				}
				GL11.glShadeModel(GL11.GL_FLAT);
			}};
	}
}
