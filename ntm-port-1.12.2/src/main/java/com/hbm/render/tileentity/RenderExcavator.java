package com.hbm.render.tileentity;

import org.lwjgl.opengl.GL11;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ModBlocks;
import com.hbm.lib.RefStrings;
import com.hbm.main.ResourceManager;
import com.hbm.tileentity.machine.TileEntityMachineExcavator;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

public class RenderExcavator extends TileEntitySpecialRenderer {
	
	public static final ResourceLocation cobble = new ResourceLocation(RefStrings.MODID, "textures/models/machines/cobblestone.png");
	public static final ResourceLocation gravel = new ResourceLocation(RefStrings.MODID, "textures/models/machines/gravel.png");

	@Override
	public void render(TileEntity tile, double x, double y, double z, float interp, int nativeDestroyStage, float nativeAlpha) {
		GL11.glPushMatrix();
		GL11.glTranslated(x + 0.5D, y, z + 0.5D);
		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glEnable(GL11.GL_CULL_FACE);
		
		switch(tile.getBlockMetadata() - BlockDummyable.offset) {
		case 3: GL11.glRotatef(0, 0F, 1F, 0F); break;
		case 5: GL11.glRotatef(90, 0F, 1F, 0F); break;
		case 2: GL11.glRotatef(180, 0F, 1F, 0F); break;
		case 4: GL11.glRotatef(270, 0F, 1F, 0F); break;
		}
		
		GL11.glTranslated(0, -((BlockDummyable) ModBlocks.machine_excavator).getHeightOffset(), 0);
		
		TileEntityMachineExcavator drill = (TileEntityMachineExcavator) tile;
		
		GL11.glShadeModel(GL11.GL_SMOOTH);
		bindTexture(ResourceManager.mining_drill_tex);
		ResourceManager.mining_drill.renderPart("Main");
		
		float crusher = drill.prevCrusherRotation + (drill.crusherRotation - drill.prevCrusherRotation) * interp;
		GL11.glPushMatrix();
		GL11.glTranslatef(0.0F, 2.0F, 2.8125F);
		GL11.glRotatef(-crusher, 1, 0, 0);
		GL11.glTranslatef(0.0F, -2.0F, -2.8125F);
		ResourceManager.mining_drill.renderPart("Crusher1");
		GL11.glPopMatrix();
		
		GL11.glPushMatrix();
		GL11.glTranslatef(0.0F, 2.0F, 2.1875F);
		GL11.glRotatef(crusher, 1, 0, 0);
		GL11.glTranslatef(0.0F, -2.0F, -2.1875F);
		ResourceManager.mining_drill.renderPart("Crusher2");
		GL11.glPopMatrix();

		GL11.glPushMatrix();
		GL11.glRotatef(drill.prevDrillRotation + (drill.drillRotation - drill.prevDrillRotation) * interp, 0F, -1F, 0F);
		float ext = drill.prevDrillExtension + (drill.drillExtension - drill.prevDrillExtension) * interp;
		GL11.glTranslatef(0.0F, -ext, 0.0F);
		ResourceManager.mining_drill.renderPart("Drillbit");
		
		while(ext >= -1.5) {
			ResourceManager.mining_drill.renderPart("Shaft");
			GL11.glTranslated(0.0D, 2.0D, 0.0D);
			ext -= 2;
		}
		GL11.glPopMatrix();
		
		GL11.glShadeModel(GL11.GL_FLAT);
		
		if(drill.chuteTimer > 0) {
			bindTexture(cobble);
			double widthX = 0.125;
			double widthZ = 0.125;
			double speed = 250D;
			double dropU = -System.currentTimeMillis() % speed / speed;
			double dropL = dropU + 4;
			Tessellator tess = Tessellator.getInstance();
			BufferBuilder tessBuffer = tess.getBuffer();
			tessBuffer.begin(7, DefaultVertexFormats.POSITION_TEX_NORMAL);
			float tessNormal0X = 0F, tessNormal0Y = 0F, tessNormal0Z = 1F;
			tessBuffer.pos(widthX, 3, 2.5 + widthZ).tex(0, dropU).normal(tessNormal0X, tessNormal0Y, tessNormal0Z).endVertex();
			tessBuffer.pos(-widthX, 3, 2.5 + widthZ).tex(1, dropU).normal(tessNormal0X, tessNormal0Y, tessNormal0Z).endVertex();
			tessBuffer.pos(-widthX, 2, 2.5 + widthZ).tex(1, dropL).normal(tessNormal0X, tessNormal0Y, tessNormal0Z).endVertex();
			tessBuffer.pos(widthX, 2, 2.5 + widthZ).tex(0, dropL).normal(tessNormal0X, tessNormal0Y, tessNormal0Z).endVertex();

			float tessNormal1X = 0F, tessNormal1Y = 0F, tessNormal1Z = -1F;
			tessBuffer.pos(-widthX, 3, 2.5 - widthZ).tex(1, dropU).normal(tessNormal1X, tessNormal1Y, tessNormal1Z).endVertex();
			tessBuffer.pos(widthX, 3, 2.5 - widthZ).tex(0, dropU).normal(tessNormal1X, tessNormal1Y, tessNormal1Z).endVertex();
			tessBuffer.pos(widthX, 2, 2.5 - widthZ).tex(0, dropL).normal(tessNormal1X, tessNormal1Y, tessNormal1Z).endVertex();
			tessBuffer.pos(-widthX, 2, 2.5 - widthZ).tex(1, dropL).normal(tessNormal1X, tessNormal1Y, tessNormal1Z).endVertex();

			float tessNormal2X = -1F, tessNormal2Y = 0F, tessNormal2Z = 0F;
			tessBuffer.pos(-widthX, 3, 2.5 + widthZ).tex(0, dropU).normal(tessNormal2X, tessNormal2Y, tessNormal2Z).endVertex();
			tessBuffer.pos(-widthX, 3, 2.5 - widthZ).tex(1, dropU).normal(tessNormal2X, tessNormal2Y, tessNormal2Z).endVertex();
			tessBuffer.pos(-widthX, 2, 2.5 - widthZ).tex(1, dropL).normal(tessNormal2X, tessNormal2Y, tessNormal2Z).endVertex();
			tessBuffer.pos(-widthX, 2, 2.5 + widthZ).tex(0, dropL).normal(tessNormal2X, tessNormal2Y, tessNormal2Z).endVertex();

			float tessNormal3X = 1F, tessNormal3Y = 0F, tessNormal3Z = 0F;
			tessBuffer.pos(widthX, 3, 2.5 - widthZ).tex(1, dropU).normal(tessNormal3X, tessNormal3Y, tessNormal3Z).endVertex();
			tessBuffer.pos(widthX, 3, 2.5 + widthZ).tex(0, dropU).normal(tessNormal3X, tessNormal3Y, tessNormal3Z).endVertex();
			tessBuffer.pos(widthX, 2, 2.5 + widthZ).tex(0, dropL).normal(tessNormal3X, tessNormal3Y, tessNormal3Z).endVertex();
			tessBuffer.pos(widthX, 2, 2.5 - widthZ).tex(1, dropL).normal(tessNormal3X, tessNormal3Y, tessNormal3Z).endVertex();
			tess.draw();

			boolean smoosh = drill.enableCrusher;
			widthX = smoosh ? 0.5 : 0.25;
			widthZ = 0.0625;
			double uU = smoosh ? 4 : 2;
			double uL = 0.5;
			bindTexture(smoosh ? gravel : cobble);
			tessBuffer.begin(7, DefaultVertexFormats.POSITION_TEX_NORMAL);
			float tessNormal4X = 0F, tessNormal4Y = 0F, tessNormal4Z = 1F;
			tessBuffer.pos(widthX, 2, 2.5 + widthZ).tex(0, dropU).normal(tessNormal4X, tessNormal4Y, tessNormal4Z).endVertex();
			tessBuffer.pos(-widthX, 2, 2.5 + widthZ).tex(uU, dropU).normal(tessNormal4X, tessNormal4Y, tessNormal4Z).endVertex();
			tessBuffer.pos(-widthX, 1, 2.5 + widthZ).tex(uU, dropL).normal(tessNormal4X, tessNormal4Y, tessNormal4Z).endVertex();
			tessBuffer.pos(widthX, 1, 2.5 + widthZ).tex(0, dropL).normal(tessNormal4X, tessNormal4Y, tessNormal4Z).endVertex();

			float tessNormal5X = 0F, tessNormal5Y = 0F, tessNormal5Z = -1F;
			tessBuffer.pos(-widthX, 2, 2.5 - widthZ).tex(uU, dropU).normal(tessNormal5X, tessNormal5Y, tessNormal5Z).endVertex();
			tessBuffer.pos(widthX, 2, 2.5 - widthZ).tex(0, dropU).normal(tessNormal5X, tessNormal5Y, tessNormal5Z).endVertex();
			tessBuffer.pos(widthX, 1, 2.5 - widthZ).tex(0, dropL).normal(tessNormal5X, tessNormal5Y, tessNormal5Z).endVertex();
			tessBuffer.pos(-widthX, 1, 2.5 - widthZ).tex(uU, dropL).normal(tessNormal5X, tessNormal5Y, tessNormal5Z).endVertex();

			float tessNormal6X = -1F, tessNormal6Y = 0F, tessNormal6Z = 0F;
			tessBuffer.pos(-widthX, 2, 2.5 + widthZ).tex(0, dropU).normal(tessNormal6X, tessNormal6Y, tessNormal6Z).endVertex();
			tessBuffer.pos(-widthX, 2, 2.5 - widthZ).tex(uL, dropU).normal(tessNormal6X, tessNormal6Y, tessNormal6Z).endVertex();
			tessBuffer.pos(-widthX, 1, 2.5 - widthZ).tex(uL, dropL).normal(tessNormal6X, tessNormal6Y, tessNormal6Z).endVertex();
			tessBuffer.pos(-widthX, 1, 2.5 + widthZ).tex(0, dropL).normal(tessNormal6X, tessNormal6Y, tessNormal6Z).endVertex();

			float tessNormal7X = 1F, tessNormal7Y = 0F, tessNormal7Z = 0F;
			tessBuffer.pos(widthX, 2, 2.5 - widthZ).tex(uL, dropU).normal(tessNormal7X, tessNormal7Y, tessNormal7Z).endVertex();
			tessBuffer.pos(widthX, 2, 2.5 + widthZ).tex(0, dropU).normal(tessNormal7X, tessNormal7Y, tessNormal7Z).endVertex();
			tessBuffer.pos(widthX, 1, 2.5 + widthZ).tex(0, dropL).normal(tessNormal7X, tessNormal7Y, tessNormal7Z).endVertex();
			tessBuffer.pos(widthX, 1, 2.5 - widthZ).tex(uL, dropL).normal(tessNormal7X, tessNormal7Y, tessNormal7Z).endVertex();
			tess.draw();
		}
		
		GL11.glPopMatrix();
	}


}
