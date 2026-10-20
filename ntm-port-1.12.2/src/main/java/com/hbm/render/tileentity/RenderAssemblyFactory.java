package com.hbm.render.tileentity;

import com.hbm.render.util.NativeMachineItemRenderer;

import org.lwjgl.opengl.GL11;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.recipes.AssemblyMachineRecipes;
import com.hbm.inventory.recipes.loader.GenericRecipe;
import com.hbm.main.MainRegistry;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.tileentity.machine.TileEntityMachineAssemblyFactory;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;


public class RenderAssemblyFactory extends TileEntitySpecialRenderer<TileEntity> implements IItemRendererProvider {
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
		
		TileEntityMachineAssemblyFactory assemfac = (TileEntityMachineAssemblyFactory) tileEntity;
		
		bindTexture(ResourceManager.assembly_factory_tex);
		ResourceManager.assembly_factory.renderPart("Base");
		if(assemfac.frame) ResourceManager.assembly_factory.renderPart("Frame");

		double slide1 = assemfac.animations[0].getSlider(interp);
		double slide2 = assemfac.animations[1].getSlider(interp);
		double[] arm1 = assemfac.animations[0].striker.getPositions(interp);
		double[] arm2 = assemfac.animations[0].saw.getPositions(interp);
		double[] arm3 = assemfac.animations[1].striker.getPositions(interp);
		double[] arm4 = assemfac.animations[1].saw.getPositions(interp);

		GL11.glPushMatrix(); {
			GL11.glTranslated(0.5 - slide1, 0, 0);
			ResourceManager.assembly_factory.renderPart("Slider1");
			
			GL11.glTranslated(0, 1.625, -0.9375);
			GL11.glRotated(-arm1[0], 1, 0, 0);
			GL11.glTranslated(0, -1.625, 0.9375);
			ResourceManager.assembly_factory.renderPart("ArmLower1");
	
			GL11.glTranslated(0, 2.375, -0.9375);
			GL11.glRotated(-arm1[1], 1, 0, 0);
			GL11.glTranslated(0, -2.375, 0.9375);
			ResourceManager.assembly_factory.renderPart("ArmUpper1");
	
			GL11.glTranslated(0, 2.375, -0.4375);
			GL11.glRotated(-arm1[2], 1, 0, 0);
			GL11.glTranslated(0, -2.375, 0.4375);
			ResourceManager.assembly_factory.renderPart("Head1");
			GL11.glTranslated(0, arm1[3], 0);
			ResourceManager.assembly_factory.renderPart("Striker1");
		} GL11.glPopMatrix();

		GL11.glPushMatrix(); {
			GL11.glTranslated(-0.5 + slide1, 0, 0);
			ResourceManager.assembly_factory.renderPart("Slider2");
			
			GL11.glTranslated(0, 1.625, 0.9375);
			GL11.glRotated(arm2[0], 1, 0, 0);
			GL11.glTranslated(0, -1.625, -0.9375);
			ResourceManager.assembly_factory.renderPart("ArmLower2");
	
			GL11.glTranslated(0, 2.375, 0.9375);
			GL11.glRotated(arm2[1], 1, 0, 0);
			GL11.glTranslated(0, -2.375, -0.9375);
			ResourceManager.assembly_factory.renderPart("ArmUpper2");
	
			GL11.glTranslated(0, 2.375, 0.4375);
			GL11.glRotated(arm2[2], 1, 0, 0);
			GL11.glTranslated(0, -2.375, -0.4375);
			ResourceManager.assembly_factory.renderPart("Head2");
			GL11.glTranslated(0, arm2[3], 0);
			ResourceManager.assembly_factory.renderPart("Striker2");
			GL11.glTranslated(0, 1.625, 0.3125);
			GL11.glRotated(-arm2[4], 1, 0, 0);
			GL11.glTranslated(0, -1.625, -0.3125);
			ResourceManager.assembly_factory.renderPart("Blade2");
		} GL11.glPopMatrix();

		GL11.glPushMatrix(); {
			GL11.glTranslated(-0.5 + slide2, 0, 0);
			ResourceManager.assembly_factory.renderPart("Slider3");
			
			GL11.glTranslated(0, 1.625, 0.9375);
			GL11.glRotated(arm3[0], 1, 0, 0);
			GL11.glTranslated(0, -1.625, -0.9375);
			ResourceManager.assembly_factory.renderPart("ArmLower3");
	
			GL11.glTranslated(0, 2.375, 0.9375);
			GL11.glRotated(arm3[1], 1, 0, 0);
			GL11.glTranslated(0, -2.375, -0.9375);
			ResourceManager.assembly_factory.renderPart("ArmUpper3");
	
			GL11.glTranslated(0, 2.375, 0.4375);
			GL11.glRotated(arm3[2], 1, 0, 0);
			GL11.glTranslated(0, -2.375, -0.4375);
			ResourceManager.assembly_factory.renderPart("Head3");
			GL11.glTranslated(0, arm3[3], 0);
			ResourceManager.assembly_factory.renderPart("Striker3");
		} GL11.glPopMatrix();

		GL11.glPushMatrix(); {
			GL11.glTranslated(0.5 - slide2, 0, 0);
			ResourceManager.assembly_factory.renderPart("Slider4");
			
			GL11.glTranslated(0, 1.625, -0.9375);
			GL11.glRotated(-arm4[0], 1, 0, 0);
			GL11.glTranslated(0, -1.625, 0.9375);
			ResourceManager.assembly_factory.renderPart("ArmLower4");
	
			GL11.glTranslated(0, 2.375, -0.9375);
			GL11.glRotated(-arm4[1], 1, 0, 0);
			GL11.glTranslated(0, -2.375, 0.9375);
			ResourceManager.assembly_factory.renderPart("ArmUpper4");
	
			GL11.glTranslated(0, 2.375, -0.4375);
			GL11.glRotated(-arm4[2], 1, 0, 0);
			GL11.glTranslated(0, -2.375, 0.4375);
			ResourceManager.assembly_factory.renderPart("Head4");
			GL11.glTranslated(0, arm4[3], 0);
			ResourceManager.assembly_factory.renderPart("Striker4");
			GL11.glTranslated(0, 1.625, -0.3125);
			GL11.glRotated(arm4[4], 1, 0, 0);
			GL11.glTranslated(0, -1.625, 0.3125);
			ResourceManager.assembly_factory.renderPart("Blade4");
		} GL11.glPopMatrix();
		
		if(MainRegistry.proxy.me().getDistanceSq(tileEntity.getPos().getX() + 0.5, tileEntity.getPos().getY() + 1, tileEntity.getPos().getZ() + 0.5) < 35 * 35) {
			
			for(int i = 0; i < 4; i++) {
				
				GL11.glPushMatrix();
				GL11.glTranslated(1.5 - i, 0, 0);
				
				GL11.glRotated(90, 0, 1, 0);
				GL11.glTranslated(0, 1.0625, 0);

				GenericRecipe recipe = AssemblyMachineRecipes.INSTANCE.recipeNameMap.get(assemfac.assemblerModule[i].recipe);
				if(recipe != null) {
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
	
			RenderArcFurnace.fullbright(true);
			GL11.glDisable(GL11.GL_CULL_FACE);
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glAlphaFunc(GL11.GL_GREATER, 0);
			OpenGlHelper.glBlendFunc(770, 771, 1, 0);
			bindTexture(ResourceManager.assembly_factory_sparks_tex);
			
			Tessellator tess = Tessellator.getInstance();
			BufferBuilder buffer = tess.getBuffer();
			double wide = 0.1875D;
			double narrow = 0.00D;
			double length = 1.25D;
			double uMin = ((tileEntity.getWorld().getTotalWorldTime() / 10D + interp)) % 10;
			double uMax = uMin + 1;
			double epsilon = 0.01D;
			
			// renders two layers of sparks, one with regular UV and one with mirrored +0.5 offset
			// render left and right of the blade with small offset to eliminate z-fighting
			GL11.glPushMatrix(); if(arm2[3] <= -0.375D) {
				GL11.glTranslated(0.5 + slide1, 1.0625D, -arm2[2] / 45D); // arm angle/45 is a seemingly good enough approximation
				buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
				buffer.pos(-epsilon, -wide, length).tex(uMin + 0.5, 0).color(1F, 1F, 1F, 0F).endVertex();
				buffer.pos(-epsilon, wide, length).tex(uMin + 0.5, 1).color(1F, 1F, 1F, 0F).endVertex();
				buffer.pos(-epsilon, narrow, 0).tex(uMax + 0.5, 1).color(1F, 1F, 1F, 1F).endVertex();
				buffer.pos(-epsilon, -narrow, 0).tex(uMax + 0.5, 0).color(1F, 1F, 1F, 1F).endVertex();
	
				buffer.pos(epsilon, -wide, length).tex(uMin, 1).color(1F, 1F, 1F, 0F).endVertex();
				buffer.pos(epsilon, wide, length).tex(uMin, 0).color(1F, 1F, 1F, 0F).endVertex();
				buffer.pos(epsilon, narrow, 0).tex(uMax, 0).color(1F, 1F, 1F, 1F).endVertex();
				buffer.pos(epsilon, -narrow, 0).tex(uMax, 1).color(1F, 1F, 1F, 1F).endVertex();
				tess.draw();
			} GL11.glPopMatrix();
			
			GL11.glPushMatrix();  if(arm4[3] <= -0.375D) {
				GL11.glTranslated(-0.5 - slide2, 1.0625D, arm4[2] / 45D);
				buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);
				buffer.pos(-epsilon, -wide, -length).tex(uMin + 0.5, 0).color(1F, 1F, 1F, 0F).normal(0, 0, 0).endVertex();
				buffer.pos(-epsilon, wide, -length).tex(uMin + 0.5, 1).color(1F, 1F, 1F, 0F).normal(0, 0, 0).endVertex();
				buffer.pos(-epsilon, narrow, 0).tex(uMax + 0.5, 1).color(1F, 1F, 1F, 1F).normal(0, 0, 0).endVertex();
				buffer.pos(-epsilon, -narrow, 0).tex(uMax + 0.5, 0).color(1F, 1F, 1F, 1F).normal(0, 0, 0).endVertex();
	
				buffer.pos(epsilon, -wide, -length).tex(uMin, 1).color(1F, 1F, 1F, 0F).normal(-1, 0, 0).endVertex();
				buffer.pos(epsilon, wide, -length).tex(uMin, 0).color(1F, 1F, 1F, 0F).normal(-1, 0, 0).endVertex();
				buffer.pos(epsilon, narrow, 0).tex(uMax, 0).color(1F, 1F, 1F, 1F).normal(-1, 0, 0).endVertex();
				buffer.pos(epsilon, -narrow, 0).tex(uMax, 1).color(1F, 1F, 1F, 1F).normal(-1, 0, 0).endVertex();
				tess.draw();
			} GL11.glPopMatrix();
			
			GL11.glAlphaFunc(GL11.GL_GREATER, 0.1F);
			GL11.glDisable(GL11.GL_BLEND);
			GL11.glEnable(GL11.GL_CULL_FACE);
			RenderArcFurnace.fullbright(false);
		}

		GL11.glShadeModel(GL11.GL_FLAT);
		GL11.glPopMatrix();
	}

	@Override
	public Item getItemForRenderer() {
		return Item.getItemFromBlock(ModBlocks.machine_assembly_factory);
	}

	@Override
	public ItemRenderBase getRenderer() {
		
		return new ItemRenderBase() {
			
			public void renderInventory() {
				GL11.glTranslated(0, -1.5, 0);
				GL11.glScaled(3, 3, 3);
			}
			public void renderCommonWithStack(ItemStack item) {
				GL11.glScaled(0.75, 0.75, 0.75);
				GL11.glShadeModel(GL11.GL_SMOOTH);
				bindTexture(ResourceManager.assembly_factory_tex);
				ResourceManager.assembly_factory.renderAll();
				GL11.glShadeModel(GL11.GL_FLAT);
			}};
	}
}
