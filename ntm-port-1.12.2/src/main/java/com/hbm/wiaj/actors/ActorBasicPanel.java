package com.hbm.wiaj.actors;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.lwjgl.opengl.GL11;

import com.hbm.wiaj.JarScene;
import com.hbm.wiaj.WorldInAJar;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import com.hbm.render.util.NativeGuiItemDraw;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class ActorBasicPanel implements ISpecialActor {
	
	int x;
	int y;
	List<Object[]> lines;
	
	public ActorBasicPanel(int x, int y, Object[]... objects) {
		this.x = x;
		this.y = y;
		this.lines = new ArrayList();
		
		for(Object[] o : objects) {
			this.lines.add(o);
		}
	}

	@Override
	public void drawForegroundComponent(int w, int h, int ticks, float interp) {
		drawStackText(lines, x, y, Minecraft.getMinecraft().fontRenderer, Minecraft.getMinecraft().getRenderItem(), w, h);
	}

	@Override
	public void drawBackgroundComponent(WorldInAJar world, int ticks, float interp) { }

	@Override
	public void updateActor(JarScene scene) { }

	@Override
	public void setActorData(NBTTagCompound data) { }

	@Override
	public void setDataPoint(String tag, Object o) { }

	protected void drawStackText(List lines, int x, int y, FontRenderer font, RenderItem itemRender, int w, int h) {
		
		x += w / 2;
		y += h / 2;
		
		if(!lines.isEmpty()) {
			GlStateManager.disableRescaleNormal();
			RenderHelper.disableStandardItemLighting();
			GlStateManager.disableLighting();
			GlStateManager.disableDepth();

			int height = 0;
			int longestline = 0;
			Iterator iterator = lines.iterator();

			while(iterator.hasNext()) {
				Object[] line = (Object[]) iterator.next();
				int lineWidth = 0;
				
				boolean hasStack = false;
				
				for(Object o : line) {
					
					if(o instanceof String) {
						lineWidth += font.getStringWidth((String) o);
					} else {
						lineWidth += 18;
						hasStack = true;
					}
				}
				
				if(hasStack) {
					height += 18;
				} else {
					height += 10;
				}

				if(lineWidth > longestline) {
					longestline = lineWidth;
				}
			}

			int minX = x + 12;
			int minY = y - 12;

			if(minX + longestline > w) {
				minX -= 28 + longestline;
			}

			if(minY + height + 6 > h) {
				minY = h - height - 6;
			}

			// The native renderer is shared; restore its caller-owned depth even on failure.
			float previousZ = itemRender.zLevel;
			itemRender.zLevel = 300.0F;
			try {
				//int j1 = -267386864;
				int colorBg = 0xF0100010;
				this.drawGradientRect(minX - 3, minY - 4, minX + longestline + 3, minY - 3, colorBg, colorBg);
				this.drawGradientRect(minX - 3, minY + height + 3, minX + longestline + 3, minY + height + 4, colorBg, colorBg);
				this.drawGradientRect(minX - 3, minY - 3, minX + longestline + 3, minY + height + 3, colorBg, colorBg);
				this.drawGradientRect(minX - 4, minY - 3, minX - 3, minY + height + 3, colorBg, colorBg);
				this.drawGradientRect(minX + longestline + 3, minY - 3, minX + longestline + 4, minY + height + 3, colorBg, colorBg);
				//int k1 = 1347420415;
				int color0 = 0x505000FF;
				//int l1 = (k1 & 16711422) >> 1 | k1 & -16777216;
				int color1 = (color0 & 0xFEFEFE) >> 1 | color0 & 0xFF000000;
				this.drawGradientRect(minX - 3, minY - 3 + 1, minX - 3 + 1, minY + height + 3 - 1, color0, color1);
				this.drawGradientRect(minX + longestline + 2, minY - 3 + 1, minX + longestline + 3, minY + height + 3 - 1, color0, color1);
				this.drawGradientRect(minX - 3, minY - 3, minX + longestline + 3, minY - 3 + 1, color0, color0);
				this.drawGradientRect(minX - 3, minY + height + 2, minX + longestline + 3, minY + height + 3, color1, color1);

				for(int index = 0; index < lines.size(); ++index) {
				
					Object[] line = (Object[]) lines.get(index);
					int indent = 0;
					boolean hasStack = false;
				
					for(Object o : line) {
						if(!(o instanceof String)) {
							hasStack = true;
						}
					}
				
					for(Object o : line) {
					
						if(o instanceof String) {
							font.drawStringWithShadow((String) o, minX + indent, minY + (hasStack ? 4 : 0), -1);
							indent += font.getStringWidth((String) o) + 2;
						} else {
							ItemStack stack = (ItemStack) o;
							GlStateManager.color(1F, 1F, 1F, 1F);

							if(stack.getCount() == 0) {
								this.drawGradientRect(minX + indent - 1, minY - 1, minX + indent + 17, minY + 17, 0xffff0000, 0xffff0000);
								this.drawGradientRect(minX + indent, minY, minX + indent + 16, minY + 16, 0xffb0b0b0, 0xffb0b0b0);
							}
							NativeGuiItemDraw.withOverlay(itemRender, font, stack, minX + indent, minY, 300.0F, null);
							RenderHelper.disableStandardItemLighting();
							GlStateManager.disableDepth();
							indent += 18;
						}
					}

					if(index == 0) {
						minY += 2;
					}

					minY += hasStack ? 18 : 10;
				}

			} finally {
				itemRender.zLevel = previousZ;
				GlStateManager.enableLighting();
				GlStateManager.enableDepth();
				RenderHelper.enableStandardItemLighting();
				GlStateManager.enableRescaleNormal();
			}
		}
	}

	protected void drawGradientRect(int p_73733_1_, int p_73733_2_, int p_73733_3_, int p_73733_4_, int p_73733_5_, int p_73733_6_) {
		float zLevel = 300.0F;
		float f = (float) (p_73733_5_ >> 24 & 255) / 255.0F;
		float f1 = (float) (p_73733_5_ >> 16 & 255) / 255.0F;
		float f2 = (float) (p_73733_5_ >> 8 & 255) / 255.0F;
		float f3 = (float) (p_73733_5_ & 255) / 255.0F;
		float f4 = (float) (p_73733_6_ >> 24 & 255) / 255.0F;
		float f5 = (float) (p_73733_6_ >> 16 & 255) / 255.0F;
		float f6 = (float) (p_73733_6_ >> 8 & 255) / 255.0F;
		float f7 = (float) (p_73733_6_ & 255) / 255.0F;
		GlStateManager.disableTexture2D();
		GlStateManager.enableBlend();
		GlStateManager.disableAlpha();
		GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
		GlStateManager.shadeModel(GL11.GL_SMOOTH);
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();
		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
		buffer.pos(p_73733_3_, p_73733_2_, zLevel).color(f1, f2, f3, f).endVertex();
		buffer.pos(p_73733_1_, p_73733_2_, zLevel).color(f1, f2, f3, f).endVertex();
		buffer.pos(p_73733_1_, p_73733_4_, zLevel).color(f5, f6, f7, f4).endVertex();
		buffer.pos(p_73733_3_, p_73733_4_, zLevel).color(f5, f6, f7, f4).endVertex();
		tessellator.draw();
		GlStateManager.shadeModel(GL11.GL_FLAT);
		GlStateManager.disableBlend();
		GlStateManager.enableAlpha();
		GlStateManager.enableTexture2D();
	}
}
